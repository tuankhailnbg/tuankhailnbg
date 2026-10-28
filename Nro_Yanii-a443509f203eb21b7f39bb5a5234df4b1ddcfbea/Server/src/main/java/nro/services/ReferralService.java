package nro.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.io.DataOutputStream;
import java.io.IOException;
import nro.consts.Cmd;
import nro.models.Part;
import nro.models.PartManager;
import nro.server.Client;
import nro.server.io.Message;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import nro.jdbc.daos.PlayerDAO;
import java.util.Map;
import nro.services.func.Input;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import java.util.concurrent.ConcurrentHashMap;
import nro.jdbc.DBService;
import nro.models.item.Item;
import nro.models.item.ItemOption;
import nro.models.player.Player;

/** NPC giới thiệu vĩnh viễn: mã GT, điểm do quản trị viên cấp, mốc nhận Thỏi Vàng. */
public final class ReferralService {
    private static final ReferralService INSTANCE = new ReferralService();
    private static final int BAR_ID = 457;
    private static final int MAX_MILESTONE = 20;
    private static final int REQUIRED_SECONDS = 10 * 60 * 60;
    private static final long REQUIRED_POWER = 50_000_000_000L;
    private static final int AFTER_CELL_TASK_ID = 30;
    private static final long INPUT_WINDOW_HOURS = 24L;
    private static final Map<Long, Long> REWARD_PANELS = new ConcurrentHashMap<>();
    private static final Set<Integer> PROMPTED = ConcurrentHashMap.newKeySet();
    private static final Object SCHEMA_LOCK = new Object();
    private static volatile boolean SCHEMA_READY = false;

    private ReferralService() { }
    public static ReferralService gI() { return INSTANCE; }

    public String code(Player p) {
        return "GT" + Long.toString(p.id, 36).toUpperCase(Locale.ROOT);
    }

    private Long parseCode(String raw) {
        if (raw == null) return null;
        String code = raw.trim().toUpperCase(Locale.ROOT);
        if (!code.matches("GT[0-9A-Z]{1,12}")) return null;
        try {
            long id = Long.parseLong(code.substring(2), 36);
            return id > 0 ? id : null;
        } catch (NumberFormatException ex) { return null; }
    }

    private Connection connect() throws SQLException {
        // Dùng connection riêng (không dùng connection chia sẻ giữa các luồng).
        try {
            Connection c = DBService.gI().getConnection();
            try {
                ensureSchema(c);
                return c;
            } catch (SQLException ex) {
                c.close();
                throw ex;
            }
        } catch (Exception ex) {
            if (ex instanceof SQLException sql) throw sql;
            throw new SQLException("Không thể lấy DB connection", ex);
        }
    }

    /**
     * Tự tạo các bảng cốt lõi nếu VPS chỉ cập nhật JAR mà chưa chạy migration.
     * Phần item/sprite cải trang 2154..2157 vẫn do migration costume quản lý,
     * vì ItemService nạp item_template vào RAM lúc khởi động.
     */
    private void ensureSchema(Connection c) throws SQLException {
        if (SCHEMA_READY) return;
        synchronized (SCHEMA_LOCK) {
            if (SCHEMA_READY) return;
            try (Statement st = c.createStatement()) {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS referral_invites ("
                        + "invited_account_id INT NOT NULL PRIMARY KEY,"
                        + "invited_player_id BIGINT NOT NULL,"
                        + "inviter_player_id BIGINT NOT NULL,"
                        + "referral_code VARCHAR(32) NOT NULL,"
                        + "active_seconds INT UNSIGNED NOT NULL DEFAULT 0,"
                        + "status TINYINT NOT NULL DEFAULT 0,"
                        + "review_required TINYINT NOT NULL DEFAULT 0,"
                        + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                        + "counted_at TIMESTAMP NULL DEFAULT NULL,"
                        + "KEY idx_inviter_status (inviter_player_id,status),"
                        + "KEY idx_invited_player (invited_player_id)"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

                st.executeUpdate("CREATE TABLE IF NOT EXISTS referral_claims ("
                        + "inviter_player_id BIGINT NOT NULL,"
                        + "milestone INT NOT NULL,"
                        + "gold_bars INT NOT NULL,"
                        + "status TINYINT NOT NULL DEFAULT 0,"
                        + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                        + "claimed_at TIMESTAMP NULL DEFAULT NULL,"
                        + "PRIMARY KEY (inviter_player_id,milestone)"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

                st.executeUpdate("CREATE TABLE IF NOT EXISTS referral_costume_catalog ("
                        + "milestone INT NOT NULL PRIMARY KEY,"
                        + "item_template_id INT NULL,"
                        + "costume_name VARCHAR(100) NOT NULL,"
                        + "duration_days INT NULL,"
                        + "options_json TEXT NOT NULL"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

                st.executeUpdate("CREATE TABLE IF NOT EXISTS referral_costume_claims ("
                        + "inviter_player_id BIGINT NOT NULL,"
                        + "milestone INT NOT NULL,"
                        + "item_template_id INT NOT NULL,"
                        + "claimed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                        + "PRIMARY KEY (inviter_player_id,milestone)"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

                st.executeUpdate("CREATE TABLE IF NOT EXISTS referral_points ("
                        + "player_id BIGINT NOT NULL PRIMARY KEY,"
                        + "points INT UNSIGNED NOT NULL DEFAULT 0,"
                        + "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

                st.executeUpdate("CREATE TABLE IF NOT EXISTS referral_points_audit ("
                        + "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,"
                        + "admin_player_id BIGINT NOT NULL,"
                        + "player_id BIGINT NOT NULL,"
                        + "delta_points INT NOT NULL,"
                        + "previous_points INT NOT NULL,"
                        + "current_points INT NOT NULL,"
                        + "reason VARCHAR(255) NOT NULL,"
                        + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                        + "KEY idx_referral_player (player_id,created_at),"
                        + "KEY idx_referral_admin (admin_player_id,created_at)"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

                st.executeUpdate("INSERT IGNORE INTO referral_costume_catalog "
                        + "(milestone,item_template_id,costume_name,duration_days,options_json) VALUES "
                        + "(5,NULL,'Goku SSJ3',7,'[[77,20],[103,20],[50,20],[14,4],[101,10],[30,0]]'),"
                        + "(10,NULL,'Vegito Blue',7,'[[77,23],[103,23],[50,23],[14,6],[101,13],[8,5],[30,0]]'),"
                        + "(15,NULL,'Gogeta Blue',NULL,'[[77,25],[103,25],[50,25],[14,8],[101,17],[8,8],[30,0]]'),"
                        + "(20,NULL,'Zeno',NULL,'[[77,27],[103,27],[50,27],[14,10],[101,20],[8,10],[30,0]]')");

                // Không tự ghi đè item_template_id của các mốc bằng item shop cũ.
                // Cải trang sự kiện mới được gắn hoàn toàn từ migration SQL.
                // Nếu catalog chưa có item_template_id thì để nguyên NULL để admin biết cần import migration.
            }
            SCHEMA_READY = true;
        }
    }

    private String diagnostic(Player player, String action, Exception ex) {
        Throwable root = ex;
        while (root.getCause() != null && root != root.getCause()) root = root.getCause();
        System.err.println("[REFERRAL][" + action + "] " + ex);
        ex.printStackTrace(System.err);
        if (player != null && player.isAdmin()) {
            if (root instanceof SQLException sql) {
                return "Lỗi " + action + " - SQL " + sql.getErrorCode() + " / "
                        + sql.getSQLState() + ": " + sql.getMessage();
            }
            return "Lỗi " + action + ": " + root.getClass().getSimpleName()
                    + " - " + root.getMessage();
        }
        return "Không thể " + action + ". Hãy báo quản trị viên kiểm tra log [REFERRAL].";
    }

    public String status(Player p) {
        if (p == null || p.getSession() == null) return "Chưa đăng nhập";
        try (Connection c = connect()) {
            int points = pointsOf(c, p.id);
            String self = "Chưa nhập mã giới thiệu";
            try (PreparedStatement st = c.prepareStatement(
                    "SELECT referral_code FROM referral_invites WHERE invited_account_id=?")) {
                st.setInt(1, p.getSession().userId);
                try (ResultSet rs = st.executeQuery()) {
                    if (rs.next()) self = "Đã nhập mã " + rs.getString(1);
                }
            }
            return "Mã giới thiệu của bạn: " + code(p)
                    + "\nĐiểm giới thiệu: " + points
                    + "\n" + self
                    + "\nĐiều kiện duyệt: 50 tỷ sức mạnh, 10 giờ hoạt động, qua nhiệm vụ Cell."
                    + "\nAdmin kiểm tra và cấp điểm thủ công."
                    + "\nSự kiện vĩnh viễn.";
        } catch (SQLException ex) {
            return "Mã: " + code(p) + "\n" + diagnostic(p, "đọc điểm giới thiệu", ex);
        }
    }

    private int pointsOf(Connection c, long playerId) throws SQLException {
        try (PreparedStatement st = c.prepareStatement(
                "SELECT points FROM referral_points WHERE player_id=?")) {
            st.setLong(1, playerId);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Nhập mã ngay sau tạo tài khoản, trước khi tài khoản đủ 24 giờ. */
    public String register(Player invitee, String raw) {
        if (invitee == null || invitee.getSession() == null) return "Bạn chưa đăng nhập.";
        Long inviterId = parseCode(raw);
        if (inviterId == null || inviterId == invitee.id) return "Mã giới thiệu không hợp lệ.";
        int accountId = invitee.getSession().userId;
        try (Connection c = connect()) {
            c.setAutoCommit(false);
            try {
                int inviterAccount;
                try (PreparedStatement st = c.prepareStatement(
                        "SELECT account_id FROM player WHERE id=? LIMIT 1")) {
                    st.setLong(1, inviterId);
                    try (ResultSet rs = st.executeQuery()) {
                        if (!rs.next()) { c.rollback(); return "Nhân vật giới thiệu không tồn tại."; }
                        inviterAccount = rs.getInt(1);
                    }
                }
                if (inviterAccount == accountId) { c.rollback(); return "Không thể tự giới thiệu tài khoản của mình."; }
                try (PreparedStatement st = c.prepareStatement(
                        "SELECT id FROM account WHERE id=? AND create_time >= DATE_SUB(NOW(),INTERVAL 24 HOUR) FOR UPDATE")) {
                    st.setInt(1, accountId);
                    try (ResultSet rs = st.executeQuery()) {
                        if (!rs.next()) { c.rollback(); return "Chỉ tài khoản mới trong vòng 24 giờ được nhập mã."; }
                    }
                }
                // Chỉ đánh dấu kiểm tra khi nhiều tài khoản chung IP.
                // Không khóa vĩnh viễn các bạn chơi cùng Wi-Fi.
                int review = 0;
                try (PreparedStatement st = c.prepareStatement(
                        "SELECT (a1.ip_address=a2.ip_address AND a1.ip_address<>''),"
                        + "(SELECT COUNT(*) FROM account ax WHERE ax.ip_address=a1.ip_address "
                        + "AND ax.create_time>=DATE_SUB(NOW(),INTERVAL 3 DAY)) "
                        + "FROM account a1 JOIN account a2 ON a2.id=? WHERE a1.id=?")) {
                    st.setInt(1, inviterAccount);
                    st.setInt(2, accountId);
                    try (ResultSet rs = st.executeQuery()) {
                        if (rs.next() && (rs.getBoolean(1) || rs.getInt(2) >= 4)) review = 1;
                    }
                }
                try (PreparedStatement st = c.prepareStatement(
                        "INSERT INTO referral_invites(invited_account_id,invited_player_id,inviter_player_id,"
                        + "referral_code,review_required) VALUES(?,?,?,?,?)")) {
                    st.setInt(1, accountId);
                    st.setLong(2, invitee.id);
                    st.setLong(3, inviterId);
                    st.setString(4, codeOf(inviterId));
                    st.setInt(5, review);
                    st.executeUpdate();
                }
                c.commit();
                return "Nhập mã thành công! Đạt 50 tỷ sức mạnh, 10 giờ hoạt động và xong nhiệm vụ Cell để được tính lượt."
                        + (review == 1 ? " Tài khoản cần được xác minh chống farm trước khi cộng mốc." : "");
            } catch (SQLException ex) {
                c.rollback();
                if ("23000".equals(ex.getSQLState())) return "Tài khoản đã liên kết một mã giới thiệu.";
                return "Không thể lưu mã giới thiệu. Hãy báo quản trị viên.";
            } finally { c.setAutoCommit(true); }
        } catch (SQLException ex) {
            return "Lỗi kết nối cơ sở dữ liệu.";
        }
    }

    private String codeOf(long id) { return "GT" + Long.toString(id, 36).toUpperCase(Locale.ROOT); }

    /**
     * Gọi từ Player.update(); không cộng thời gian chỉ vì treo online.
     * Chỉ tăng khi có di chuyển đủ 24px, thay đổi sức mạnh hoặc nhiệm vụ.
     * Mỗi lần cộng tối đa 60 giây, không đếm thời gian offline.
     */
    /** Chỉ hiện ô nhập mã tài khoản mới; không tự tính hoặc cấp điểm. */
    public void tick(Player p) {
        if (p == null || !p.loaded || p.getSession() == null
                || p.isBoss || p.isPet || p.isMiniPet) return;
        promptOnFirstLogin(p);
    }

    // Để người mới biết có ô nhập mã ngay trong game, không cần sửa client.
    private void promptOnFirstLogin(Player p) {
        int accountId = p.getSession().userId;
        if (!PROMPTED.add(accountId)) return;
        try (Connection c = connect(); PreparedStatement st = c.prepareStatement(
                "SELECT a.id FROM account a WHERE a.id=? "
                + "AND a.create_time >= DATE_SUB(NOW(),INTERVAL 24 HOUR) "
                + "AND NOT EXISTS (SELECT 1 FROM referral_invites r WHERE r.invited_account_id=a.id)")) {
            st.setInt(1, accountId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) Input.gI().createFormReferralCode(p);
            }
        } catch (SQLException ignored) { }
    }


    /**
     * Cấp/trừ điểm trực tiếp qua form chỉ admin được dùng.
     * Log trước và sau mỗi lần cấp; không cộng tự động từ đăng ký/tác vụ.
     */
    public String adminGrantPoints(Player admin, String target, String deltaText, String reason) {
        if (admin == null || admin.getSession() == null || !admin.isAdmin()) return "Không đủ quyền Admin.";
        if (target == null || target.isBlank()) return "Nhập ID hoặc tên chính xác của nhân vật.";
        int delta;
        try { delta = Integer.parseInt(deltaText.trim()); }
        catch (Exception e) { return "Điểm phải là số nguyên (+1 hoặc -1)."; }
        if (delta == 0 || delta < -100 || delta > 100)
            return "Mỗi lần chỉ được cộng/trừ 1–100 điểm.";
        if (reason == null || reason.isBlank() || reason.trim().length() > 255)
            return "Phải nhập lý do, tối đa 255 ký tự.";
        try (Connection c = connect()) {
            c.setAutoCommit(false);
            try {
                long targetId = -1;
                try (PreparedStatement st = c.prepareStatement(
                        "SELECT id FROM player WHERE id=CAST(? AS UNSIGNED) OR name=? ORDER BY id LIMIT 2")) {
                    st.setString(1, target.trim().matches("[0-9]+") ? target.trim() : "0");
                    st.setString(2, target.trim());
                    try (ResultSet rs = st.executeQuery()) {
                        if (!rs.next()) { c.rollback(); return "Không tìm thấy nhân vật."; }
                        targetId = rs.getLong(1);
                        if (rs.next()) { c.rollback(); return "Tên không duy nhất, hãy nhập ID nhân vật."; }
                    }
                }
                try (PreparedStatement st = c.prepareStatement(
                        "INSERT INTO referral_points(player_id,points) VALUES(?,0) "
                        + "ON DUPLICATE KEY UPDATE player_id=VALUES(player_id)")) {
                    st.setLong(1,targetId); st.executeUpdate();
                }
                int old;
                try (PreparedStatement st = c.prepareStatement(
                        "SELECT points FROM referral_points WHERE player_id=? FOR UPDATE")) {
                    st.setLong(1,targetId);
                    try (ResultSet rs = st.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Không có dòng điểm");
                        old=rs.getInt(1);
                    }
                }
                long after=(long)old+delta;
                if(after<0 || after>100_000) { c.rollback(); return "Điểm sau thay đổi phải trong khoảng 0–100.000."; }
                try(PreparedStatement st=c.prepareStatement(
                        "UPDATE referral_points SET points=? WHERE player_id=?")) {
                    st.setLong(1,after);st.setLong(2,targetId);st.executeUpdate();
                }
                try(PreparedStatement st=c.prepareStatement(
                        "INSERT INTO referral_points_audit(admin_player_id,player_id,delta_points,"
                        + "previous_points,current_points,reason) VALUES(?,?,?,?,?,?)")) {
                    st.setLong(1,admin.id);st.setLong(2,targetId);st.setInt(3,delta);
                    st.setInt(4,old);st.setLong(5,after);st.setString(6,reason.trim());st.executeUpdate();
                }
                c.commit();
                return "Đã "+(delta>0?"cộng ":"trừ ")+Math.abs(delta)
                       +" điểm cho ID "+targetId+". Tổng: "+after+" điểm.";
            } catch(SQLException e) {
                c.rollback();
                return "Lỗi khi cấp điểm; đã hủy thao tác. Kiểm tra DB/migration.";
            } finally { c.setAutoCommit(true); }
        } catch(SQLException e){return "Lỗi kết nối DB cấp điểm.";}
    }

    private static final class RankRow {
        long id;
        int points;
        short head;
        byte gender;
        String name;
        RankRow(ResultSet rs) throws SQLException {
            id=rs.getLong("id");points=rs.getInt("points");
            head=rs.getShort("head");gender=rs.getByte("gender");
            name=rs.getString("name");
        }
    }

    public void sendLeaderboard(Player viewer) {
        if (viewer==null || viewer.getSession()==null) return;
        List<RankRow> rows=new ArrayList<>();
        try(Connection c=connect();
            PreparedStatement st=c.prepareStatement(
                "SELECT p.id,p.name,p.head,p.gender,r.points FROM referral_points r "
                + "JOIN player p ON p.id=r.player_id WHERE r.points>0 "
                + "ORDER BY r.points DESC,r.updated_at ASC,p.id ASC LIMIT 30");
            ResultSet rs=st.executeQuery()) {
            while(rs.next())rows.add(new RankRow(rs));
        } catch(SQLException e) {
            Service.gI().sendThongBao(viewer, diagnostic(viewer, "tải Top giới thiệu", e));return;
        }
        try {
            Message msg=new Message(Cmd.TOP);
            msg.writer().writeByte(0);
            msg.writer().writeUTF("TOP GIỚI THIỆU");
            msg.writer().writeByte(rows.size());
            for(int i=0;i<rows.size();i++){
                RankRow row=rows.get(i);
                Player online=Client.gI().getPlayer(row.id);
                short h=online==null?row.head:online.getHead();
                short body=online==null?(short)(row.gender==1?59:57):online.getBody();
                short leg=online==null?(short)(row.gender==1?60:58):online.getLeg();
                msg.writer().writeInt(i+1);
                msg.writer().writeInt((int)row.id);
                msg.writer().writeShort(h);
                if(viewer.isVersionAbove(220)){
                    Part part=PartManager.getInstance().find(h);
                    msg.writer().writeShort(part==null?h:part.getIcon(0));
                }
                msg.writer().writeShort(body);
                msg.writer().writeShort(leg);
                msg.writer().writeUTF(row.name);
                msg.writer().writeUTF(online==null?"Offline":"Online");
                msg.writer().writeUTF(row.points+" Điểm giới thiệu");
            }
            viewer.sendMessage(msg);msg.cleanup();
        }catch(IOException e){Service.gI().sendThongBao(viewer,"Không hiển thị được bảng xếp hạng.");}
    }

    public void closeRewardPanel(Player player) {
        if(player!=null) REWARD_PANELS.remove(player.id);
    }

    public boolean isRewardPanel(Player p) {
        if (p == null) return false;
        Long expiresAt = REWARD_PANELS.get(p.id);
        if (expiresAt == null) return false;
        if (expiresAt < System.currentTimeMillis()) {
            REWARD_PANELS.remove(p.id, expiresAt);
            return false;
        }
        return true;
    }

    public void sendRewardPanel(Player p){
        if(p==null||p.getSession()==null)return;
        try(Connection c=connect()){
            int points=pointsOf(c,p.id);
            Set<Integer> gotBars=new HashSet<>(), gotCostumes=new HashSet<>(), mappedCostumes=new HashSet<>();
            try(PreparedStatement st=c.prepareStatement(
                    "SELECT milestone FROM referral_claims WHERE inviter_player_id=? AND status=1")){
                st.setLong(1,p.id);
                try(ResultSet rs=st.executeQuery()){while(rs.next())gotBars.add(rs.getInt(1));}
            }
            try(PreparedStatement st=c.prepareStatement(
                    "SELECT milestone FROM referral_costume_claims WHERE inviter_player_id=?")){
                st.setLong(1,p.id);
                try(ResultSet rs=st.executeQuery()){while(rs.next())gotCostumes.add(rs.getInt(1));}
            }
            try(PreparedStatement st=c.prepareStatement(
                    "SELECT milestone FROM referral_costume_catalog WHERE item_template_id IS NOT NULL")){
                try(ResultSet rs=st.executeQuery()){while(rs.next())mappedCostumes.add(rs.getInt(1));}
            }
            // Cùng kiểu bảng thành tựu 'Nhận ngọc miễn phí' ở Bò Mộng.
            Message m=new Message(Cmd.ACHIEVEMENT);
            DataOutputStream ds=m.writer();
            ds.writeByte(0);
            ds.writeByte(MAX_MILESTONE);
            for(int n=1;n<=MAX_MILESTONE;n++){
                int bars=100+(n-1)*25;
                boolean hasCostume=n==5||n==10||n==15||n==20;
                String costume=n==5?"Goku SSJ3 (7 ngày)":
                               n==10?"Vegito Blue (7 ngày)":
                               n==15?"Gogeta Blue (vĩnh viễn)":
                               n==20?"Zeno (vĩnh viễn)":"";
                // Một mốc đặc biệt chỉ hoàn tất khi cả Thỏi Vàng và cải trang đều đã nhận.
                boolean claimed=gotBars.contains(n) &&
                     (!hasCostume||gotCostumes.contains(n));
                String detail="Điểm của bạn: "+points+"/"+n+". Thưởng "+bars+" Thỏi Vàng"
                        +(hasCostume?"; "+costume:"")
                        +(gotBars.contains(n)?". Đã nhận Thỏi Vàng.":"")
                        +(hasCostume&&!gotCostumes.contains(n)&&!mappedCostumes.contains(n)
                                ? ". Cải trang chưa có ID, phần quà được bảo lưu." : "");
                ds.writeUTF("Mốc giới thiệu "+n);
                ds.writeUTF(detail);
                ds.writeShort(bars); // số hiển thị trên kiểu bảng thành tựu
                ds.writeBoolean(points>=n);
                ds.writeBoolean(claimed);
            }
            ds.flush();
            // Đánh dấu trước khi gửi để gói click trả về luôn được định tuyến đúng.
            REWARD_PANELS.put(p.id,System.currentTimeMillis()+5*60_000L);
            p.sendMessage(m);m.cleanup();
        }catch(Exception e){closeRewardPanel(p);Service.gI().sendThongBaoOK(p, diagnostic(p, "mở bảng nhận quà giới thiệu", e));}
    }

    /** Xử lý một dòng trên bảng thành tựu; mỗi mốc chỉ nhận một lần. */
    public void onRewardSelection(Player player, byte selected) {
        if (!isRewardPanel(player)) return;
        int milestone=Byte.toUnsignedInt(selected)+1;
        if(milestone<1||milestone>MAX_MILESTONE)return;
        String result=claimMilestone(player,milestone);
        // Thông báo nhỏ trong game, không tạo modal OK che bảng nhận thưởng.
        // claimMilestone đã cập nhật hành trang và lưu DB trước khi đến đây.
        Service.gI().sendThongBao(player,result);
        sendRewardPanel(player);
    }

    public String claimMilestone(Player player, int milestone) {
        if(player==null || player.getSession()==null || milestone<1||milestone>MAX_MILESTONE)
            return "Mốc nhận quà không hợp lệ.";
        if(!PlayerDAO.tryAcquireSaving(player))
            return "Nhân vật đang được lưu, hãy thử lại.";
        try {
            synchronized(player.inventory) {
                try(Connection c=connect()){
                    int points=pointsOf(c,player.id);
                    if(points<milestone)return "Chưa đạt mốc "+milestone+". Hiện có "+points+" điểm.";
                    boolean paid=alreadyClaimed(c,player.id,milestone);
                    int bars=100+(milestone-1)*25;
                    if(!paid){
                        if(InventoryService.gI().getCountEmptyBag(player)<1)
                            return "Hành trang cần còn ít nhất 1 ô trống để nhận quà.";
                        List<Item> backup=snapshotBag(player);
                        c.setAutoCommit(false);
                        try {
                            try(PreparedStatement st=c.prepareStatement(
                                    "INSERT INTO referral_claims(inviter_player_id,milestone,gold_bars,status) "
                                    +"VALUES(?,?,?,0)")){
                                st.setLong(1,player.id);st.setInt(2,milestone);st.setInt(3,bars);
                                st.executeUpdate();
                            }
                            if(ItemService.gI().getTemplate((short)BAR_ID)==null)
                                throw new SQLException("Thỏi vàng ID 457 chưa tồn tại.");
                            Item reward=ItemService.gI().createNewItem((short)BAR_ID,bars);
                            if(!InventoryService.gI().addItemBag(player,reward,9999))
                                throw new SQLException("Không thể thêm Thỏi Vàng vào túi");
                            try(PreparedStatement st=c.prepareStatement(
                                    "UPDATE player SET items_bag=? WHERE id=?")){
                                st.setString(1,InventoryService.gI().itemsBagToString(player));
                                st.setLong(2,player.id);
                                if(st.executeUpdate()!=1)throw new SQLException("Không lưu được túi");
                            }
                            try(PreparedStatement st=c.prepareStatement(
                                    "UPDATE referral_claims SET status=1,claimed_at=NOW() "
                                    +"WHERE inviter_player_id=? AND milestone=?")){
                                st.setLong(1,player.id);st.setInt(2,milestone);
                                st.executeUpdate();
                            }
                            c.commit();
                        } catch(Exception ex) {
                            c.rollback();restoreBag(player,backup);
                            InventoryService.gI().sendItemBags(player);
                            return "Không thể nhận mốc "+milestone+". Hãy thử lại sau.";
                        } finally {c.setAutoCommit(true);}
                    }
                    int costumes=0;
                    try {costumes=claimCostumes(player,c,milestone);}
                    catch(Exception ex) {
                        InventoryService.gI().sendItemBags(player);
                        return diagnostic(player, "nhận cải trang mốc " + milestone, ex);
                    }
                    InventoryService.gI().sendItemBags(player);
                    String goldMessage = paid ? "Đã nhận Thỏi Vàng ở lần trước."
                            : "Đã nhận " + bars + " Thỏi Vàng ở mốc " + milestone + ".";
                    if (isSpecialMilestone(milestone)
                            && !costumeAlreadyClaimed(c,player.id,milestone)) {
                        return goldMessage + " Cải trang CHƯA được nhận: "
                                + costumeBlocker(player,c,milestone);
                    }
                    if (paid && costumes == 0) return "Bạn đã nhận đủ quà mốc " + milestone + ".";
                    return goldMessage + (costumes > 0 ? " Đã nhận thêm cải trang." : "");
                }catch(SQLException ex){return "Lỗi cơ sở dữ liệu khi nhận quà.";}
            }
        }finally {PlayerDAO.releaseSaving(player);}
    }

    /**
     * Chỉ phát cải trang khi admin đã gán ID item thật trong referral_costume_catalog.
     * Nếu chưa có sprite/item_template, quà vẫn chờ, không đánh dấu đã nhận.
     */
    private String costumeBlocker(Player p, Connection c, int milestone) throws SQLException {
        if (InventoryService.gI().getCountEmptyBag(p) < 1)
            return "Hành trang đầy, cần một ô trống để nhận cải trang.";
        validateCatalogCostume(c, milestone);
        try (PreparedStatement st=c.prepareStatement(
                "SELECT item_template_id FROM referral_costume_catalog WHERE milestone=?")) {
            st.setInt(1,milestone);
            try (ResultSet rs=st.executeQuery()) {
                if (!rs.next() || rs.getObject(1)==null)
                    return "Chưa gắn ID cải trang. Import script sửa liên kết SQL trên VPS.";
                int id=rs.getInt(1);
                if (id <= 0 || id > Short.MAX_VALUE) return "ID cải trang không hợp lệ: " + id;
                try {
                    var template = ItemService.gI().getTemplate(id);
                    if (template == null || template.type != 5)
                        return "Server chưa nạp cải trang ID " + id + ". Hãy restart Server sau khi import SQL.";
                } catch (RuntimeException ex) {
                    return "Server chưa nạp cải trang ID " + id + ". Hãy restart Server sau khi import SQL.";
                }
            }
        }
        return "Cần kiểm tra các option hoặc thao tác lưu túi; xem log [REFERRAL] trên server.";
    }

    /**
     * Chỉ xác minh catalog, tuyệt đối không đổi item_template_id sang item shop.
     */
    private void validateCatalogCostume(Connection c, int milestone) throws SQLException {
        try (PreparedStatement st = c.prepareStatement(
                "SELECT c.item_template_id,i.TYPE "
                + "FROM referral_costume_catalog c "
                + "LEFT JOIN item_template i ON i.id=c.item_template_id "
                + "WHERE c.milestone=?")) {
            st.setInt(1, milestone);
            try (ResultSet rs = st.executeQuery()) {
                if (!rs.next() || rs.getObject(1) == null) {
                    throw new SQLException("Mốc " + milestone + " chưa gắn item_template_id cải trang");
                }
                int itemId = rs.getInt(1);
                int type = rs.getInt(2);
                if (itemId <= 0 || itemId > Short.MAX_VALUE || type != 5) {
                    throw new SQLException("Mốc " + milestone
                            + " đang gắn item không hợp lệ: " + itemId + ", TYPE=" + type);
                }
            }
        }
    }

    private int claimCostumes(Player p, Connection c, int selectedMilestone) throws SQLException {
        int delivered = 0;
        for (int milestone : new int[] { 5, 10, 15, 20 }) {
            if (selectedMilestone != milestone) continue;
            validateCatalogCostume(c, milestone);
            int itemId, days;
            String options;
            try (PreparedStatement st = c.prepareStatement(
                    "SELECT item_template_id,duration_days,options_json FROM referral_costume_catalog "
                    + "WHERE milestone=? AND item_template_id IS NOT NULL")) {
                st.setInt(1, milestone);
                try (ResultSet rs = st.executeQuery()) {
                    if (!rs.next()) continue;
                    itemId = rs.getInt(1);
                    days = rs.getInt(2); // 0 = vĩnh viễn
                    options = rs.getString(3);
                }
            }
            if (itemId <= 0 || itemId > Short.MAX_VALUE) {
                System.err.println("[REFERRAL] Invalid costume ID for milestone " + milestone + ": " + itemId);
                continue;
            }
            boolean staleClaim = false;
            try (PreparedStatement st = c.prepareStatement(
                    "SELECT item_template_id FROM referral_costume_claims "
                    + "WHERE inviter_player_id=? AND milestone=?")) {
                st.setLong(1, p.id);
                st.setInt(2, milestone);
                try (ResultSet rs = st.executeQuery()) {
                    if (rs.next()) {
                        int claimedItemId = rs.getInt(1);
                        if (claimedItemId == itemId) {
                            continue;
                        }
                        staleClaim = true;
                    }
                }
            }
            if (InventoryService.gI().getCountEmptyBag(p) < 1) break;
            if (ItemService.gI().getTemplate((short) itemId) == null) {
                System.err.println("[REFERRAL] Costume item " + itemId + " not loaded. Restart after importing SQL.");
                continue;
            }
            Item item = ItemService.gI().createNewItem((short) itemId, 1);
            if (item == null || item.template == null || item.template.type != 5) {
                // Không phát nhầm vật phẩm khác loại cải trang (5).
                System.err.println("[REFERRAL] Costume template " + itemId + " missing or not TYPE=5.");
                continue;
            }
            item.itemOptions.clear();
            try {
                JSONArray list = (JSONArray) JSONValue.parse(options);
                if (list == null) continue;
                for (Object obj : list) {
                    JSONArray pair = (JSONArray) obj;
                    int id = ((Number) pair.get(0)).intValue();
                    int param = ((Number) pair.get(1)).intValue();
                    if (ItemService.gI().getItemOptionTemplate(id) == null)
                        throw new IllegalArgumentException("Option ID " + id + " không có trong server");
                    item.itemOptions.add(new ItemOption(id, param));
                }
                if (days > 0) {
                    if (ItemService.gI().getItemOptionTemplate(93) == null)
                        throw new IllegalArgumentException("Thiếu option thời hạn ID 93");
                    if (item.itemOptions.stream().noneMatch(op -> op.optionTemplate.id == 93))
                        item.itemOptions.add(new ItemOption(93, days));
                }
            } catch (Exception ex) {
                throw new SQLException("Lỗi chỉ số cải trang mốc " + milestone
                        + ", item " + itemId + ": " + ex.getMessage(), ex);
            }
            item.createTime = System.currentTimeMillis();
            // Bắt buộc không giao dịch.
            boolean nonTrade = false;
            for (ItemOption op : item.itemOptions) {
                if (op.optionTemplate.id == 30) { nonTrade = true; break; }
            }
            if (!nonTrade) item.itemOptions.add(new ItemOption(30, 0));
            List<Item> bagBefore = snapshotBag(p);
            c.setAutoCommit(false);
            try {
                if (staleClaim) {
                    try (PreparedStatement st = c.prepareStatement(
                            "UPDATE referral_costume_claims "
                            + "SET item_template_id=?, claimed_at=CURRENT_TIMESTAMP "
                            + "WHERE inviter_player_id=? AND milestone=?")) {
                        st.setInt(1, itemId);
                        st.setLong(2, p.id);
                        st.setInt(3, milestone);
                        st.executeUpdate();
                    }
                } else {
                    try (PreparedStatement st = c.prepareStatement(
                            "INSERT INTO referral_costume_claims(inviter_player_id,milestone,item_template_id) "
                            + "VALUES(?,?,?)")) {
                        st.setLong(1, p.id);
                        st.setInt(2, milestone);
                        st.setInt(3, itemId);
                        st.executeUpdate();
                    }
                }
                if (!InventoryService.gI().addItemBag(p, item, 9999)) {
                    c.rollback();
                    break;
                }
                try (PreparedStatement st = c.prepareStatement(
                        "UPDATE player SET items_bag=? WHERE id=?")) {
                    st.setString(1, InventoryService.gI().itemsBagToString(p));
                    st.setLong(2, p.id);
                    if (st.executeUpdate() != 1) throw new SQLException("Không lưu được cải trang");
                }
                c.commit();
                delivered++;
            } catch (SQLException ex) {
                c.rollback();
                restoreBag(p, bagBefore);
                throw ex;
            } finally { c.setAutoCommit(true); }
        }
        return delivered;
    }

    private boolean isSpecialMilestone(int milestone) {
        return milestone == 5 || milestone == 10 || milestone == 15 || milestone == 20;
    }

    private boolean costumeAlreadyClaimed(Connection c, long playerId, int milestone) throws SQLException {
        try (PreparedStatement st = c.prepareStatement(
                "SELECT 1 FROM referral_costume_claims WHERE inviter_player_id=? AND milestone=?")) {
            st.setLong(1, playerId);
            st.setInt(2, milestone);
            try (ResultSet rs = st.executeQuery()) { return rs.next(); }
        }
    }

    private List<Item> snapshotBag(Player p) {
        List<Item> copy = new ArrayList<>(p.inventory.itemsBag.size());
        for (Item current : p.inventory.itemsBag) {
            copy.add(current == null ? ItemService.gI().createItemNull() : ItemService.gI().copyItem(current));
        }
        return copy;
    }

    private void restoreBag(Player p, List<Item> original) {
        p.inventory.itemsBag.clear();
        p.inventory.itemsBag.addAll(original);
    }

    private boolean alreadyClaimed(Connection c, long id, int milestone) throws SQLException {
        try (PreparedStatement st = c.prepareStatement(
                "SELECT 1 FROM referral_claims WHERE inviter_player_id=? AND milestone=?")) {
            st.setLong(1, id); st.setInt(2, milestone);
            try (ResultSet rs = st.executeQuery()) { return rs.next(); }
        }
    }
}
