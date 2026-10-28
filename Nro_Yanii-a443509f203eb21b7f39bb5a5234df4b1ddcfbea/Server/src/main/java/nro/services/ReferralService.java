package nro.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import nro.jdbc.daos.PlayerDAO;
import java.util.Map;
import java.util.Set;
import nro.services.func.Input;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import java.util.concurrent.ConcurrentHashMap;
import nro.jdbc.DBService;
import nro.models.item.Item;
import nro.models.item.ItemOption;
import nro.models.player.Player;

/**
 * Sự kiện giới thiệu vĩnh viễn. Người được mời phải đạt 50 tỷ sức mạnh,
 * 10 giờ hoạt động hợp lệ và qua nhiệm vụ Xên (task id >= 30).
 * Mã GT<player.id in base36> thuộc từng nhân vật, nhưng giới thiệu
 * được giới hạn đúng một lần trên mỗi account mới.
 */
public final class ReferralService {
    private static final ReferralService INSTANCE = new ReferralService();
    private static final int BAR_ID = 457;
    private static final int MAX_MILESTONE = 20;
    private static final int REQUIRED_SECONDS = 10 * 60 * 60;
    private static final long REQUIRED_POWER = 50_000_000_000L;
    private static final int AFTER_CELL_TASK_ID = 30;
    private static final long INPUT_WINDOW_HOURS = 24L;
    private static final Map<Long, Activity> ACTIVITY = new ConcurrentHashMap<>();
    private static final Set<Integer> PROMPTED = ConcurrentHashMap.newKeySet();

    private static final class Activity {
        long lastSample, lastActivity, lastPersist, power;
        int x, y, taskId;
        Activity(Player p, long now) {
            lastSample = lastPersist = lastActivity = now;
            x = p.location.x;
            y = p.location.y;
            power = p.nPoint.power;
            taskId = p.playerTask.taskMain == null ? -1 : p.playerTask.taskMain.id;
        }
    }

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
        try { return DBService.gI().getConnection(); }
        catch (Exception ex) { throw new SQLException("Không thể lấy DB connection", ex); }
    }

    public String status(Player p) {
        if (p == null || p.getSession() == null) return "Chưa đăng nhập";
        try (Connection c = connect()) {
            int completed = 0, claimed = 0;
            try (PreparedStatement st = c.prepareStatement(
                    "SELECT COUNT(*) FROM referral_invites WHERE inviter_player_id=? AND status=1 AND review_required=0")) {
                st.setLong(1, p.id);
                try (ResultSet rs = st.executeQuery()) { if (rs.next()) completed = rs.getInt(1); }
            }
            try (PreparedStatement st = c.prepareStatement(
                    "SELECT COUNT(*) FROM referral_claims WHERE inviter_player_id=? AND status=1")) {
                st.setLong(1, p.id);
                try (ResultSet rs = st.executeQuery()) { if (rs.next()) claimed = rs.getInt(1); }
            }
            String self = "Chưa nhập mã giới thiệu";
            try (PreparedStatement st = c.prepareStatement(
                    "SELECT status,active_seconds,review_required FROM referral_invites WHERE invited_account_id=?")) {
                st.setInt(1, p.getSession().userId);
                try (ResultSet rs = st.executeQuery()) {
                    if (rs.next()) self = rs.getInt(3) == 1 ? "Đang chờ kiểm tra chống farm"
                            : (rs.getInt(1) == 1 ? "Đã hợp lệ"
                            : "Đang làm nhiệm vụ (" + rs.getInt(2) / 3600 + "/10 giờ)");
                }
            }
            return "Mã giới thiệu của bạn: " + code(p)
                    + "\nGiới thiệu hợp lệ: " + completed
                    + "\nĐã nhận mốc: " + claimed
                    + "\nTrạng thái mã đã nhập: " + self
                    + "\nSự kiện không giới hạn thời gian.";
        } catch (SQLException ex) {
            return "Không đọc được dữ liệu giới thiệu. Kiểm tra migration SQL.";
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
    public void tick(Player p) {
        if (p == null || !p.loaded || p.getSession() == null || p.isBoss || p.isPet || p.isMiniPet
                || p.playerTask == null || p.nPoint == null || p.location == null) return;
        promptOnFirstLogin(p);
        long now = System.currentTimeMillis();
        Activity a = ACTIVITY.computeIfAbsent(p.id, id -> new Activity(p, now));
        synchronized (a) {
            int task = p.playerTask.taskMain == null ? -1 : p.playerTask.taskMain.id;
            if (Math.abs(p.location.x - a.x) >= 24 || Math.abs(p.location.y - a.y) >= 24
                    || p.nPoint.power != a.power || task != a.taskId) {
                a.lastActivity = now;
                a.x = p.location.x; a.y = p.location.y;
                a.power = p.nPoint.power; a.taskId = task;
            }
            a.lastSample = now;
            if (now - a.lastPersist < 60_000L) return;
            long elapsed = Math.min(60L, (now - a.lastPersist) / 1000L);
            a.lastPersist = now;
            if (now - a.lastActivity > 45_000L || elapsed <= 0) return;
            try (Connection c = connect(); PreparedStatement st = c.prepareStatement(
                    "UPDATE referral_invites SET active_seconds=active_seconds+? "
                    + "WHERE invited_account_id=? AND invited_player_id=? AND status=0")) {
                st.setLong(1, elapsed);
                st.setInt(2, p.getSession().userId);
                st.setLong(3, p.id);
                int changed = st.executeUpdate();
                if (changed > 0 && p.nPoint.power >= REQUIRED_POWER && task >= AFTER_CELL_TASK_ID) {
                    try (PreparedStatement complete = c.prepareStatement(
                            "UPDATE referral_invites SET status=1,counted_at=NOW() "
                            + "WHERE invited_account_id=? AND invited_player_id=? AND status=0 AND active_seconds>=?")) {
                        complete.setInt(1, p.getSession().userId);
                        complete.setLong(2, p.id);
                        complete.setInt(3, REQUIRED_SECONDS);
                        if (complete.executeUpdate() == 1) {
                            Service.gI().sendThongBao(p, "Bạn đã hoàn thành điều kiện giới thiệu!");
                        }
                    }
                }
            } catch (SQLException ignored) { /* Không làm rớt phiên game khi DB lỗi. */ }
        }
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

    /** Mỗi mốc n nhận 100+(n-1)*25 thỏi, từ 1 tới 20. */
    public String claim(Player player) {
        if (player == null || player.getSession() == null) return "Bạn chưa đăng nhập.";
        if (!PlayerDAO.tryAcquireSaving(player)) {
            return "Máy chủ đang lưu nhân vật, hãy thử nhận quà sau vài giây.";
        }
        try {
        synchronized (player.inventory) {
            try (Connection c = connect()) {
                int total = 0;
                try (PreparedStatement st = c.prepareStatement(
                        "SELECT COUNT(*) FROM referral_invites WHERE inviter_player_id=? AND status=1 AND review_required=0")) {
                    st.setLong(1, player.id);
                    try (ResultSet rs = st.executeQuery()) { if (rs.next()) total = Math.min(MAX_MILESTONE, rs.getInt(1)); }
                }
                if (total == 0) return "Chưa có người được giới thiệu đủ điều kiện.";
                int gained = 0, taken = 0;
                for (int m = 1; m <= total; m++) {
                    if (alreadyClaimed(c, player.id, m)) continue;
                    if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
                        return (taken == 0 ? "" : "Đã nhận " + gained + " Thỏi Vàng. ")
                                + "Hành trang đầy, hãy chừa ô trống rồi nhận tiếp.";
                    }
                    int bars = 100 + (m - 1) * 25;
                    List<Item> bagBefore = snapshotBag(player);
                    c.setAutoCommit(false);
                    try {
                        try (PreparedStatement st = c.prepareStatement(
                                "INSERT INTO referral_claims(inviter_player_id,milestone,gold_bars,status) VALUES(?,?,?,0)")) {
                            st.setLong(1, player.id);
                            st.setInt(2, m);
                            st.setInt(3, bars);
                            st.executeUpdate();
                        }
                        Item item = ItemService.gI().createNewItem((short) BAR_ID, bars);
                        if (item == null || item.template == null || !InventoryService.gI().addItemBag(player, item, 9999)) {
                            c.rollback();
                            restoreBag(player, bagBefore);
                            return "Không thể thêm Thỏi Vàng vào hành trang.";
                        }
                        // Lưu vào DB cùng giao dịch claim, tránh mất vật phẩm khi logout ngay.
                        try (PreparedStatement st = c.prepareStatement("UPDATE player SET items_bag=? WHERE id=?")) {
                            st.setString(1, InventoryService.gI().itemsBagToString(player));
                            st.setLong(2, player.id);
                            if (st.executeUpdate() != 1) throw new SQLException("Không lưu được hành trang");
                        }
                        try (PreparedStatement st = c.prepareStatement(
                                "UPDATE referral_claims SET status=1,claimed_at=NOW() WHERE inviter_player_id=? AND milestone=?")) {
                            st.setLong(1, player.id);
                            st.setInt(2, m);
                            st.executeUpdate();
                        }
                        c.commit(); gained += bars; taken++;
                    } catch (SQLException ex) {
                        c.rollback();
                        restoreBag(player, bagBefore);
                        InventoryService.gI().sendItemBags(player);
                        return "Không thể nhận quà mốc " + m + ", hãy thử lại.";
                    } finally { c.setAutoCommit(true); }
                }
                int costumes = claimCostumes(player, c, total);
                InventoryService.gI().sendItemBags(player);
                String result = taken == 0 ? "Bạn đã nhận tất cả mốc Thỏi Vàng hiện có."
                        : "Đã nhận " + gained + " Thỏi Vàng từ " + taken + " mốc giới thiệu!";
                if (costumes > 0) result += " Đồng thời nhận " + costumes + " cải trang sự kiện!";
                return result;
            } catch (SQLException ex) { return "Lỗi cơ sở dữ liệu khi nhận quà."; }
        }
        } finally { PlayerDAO.releaseSaving(player); }
    }

    /**
     * Chỉ phát cải trang khi admin đã gán ID item thật trong referral_costume_catalog.
     * Nếu chưa có sprite/item_template, quà vẫn chờ, không đánh dấu đã nhận.
     */
    private int claimCostumes(Player p, Connection c, int completed) throws SQLException {
        int delivered = 0;
        for (int milestone : new int[] { 5, 10, 15, 20 }) {
            if (completed < milestone) continue;
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
            if (itemId <= 0 || itemId > Short.MAX_VALUE) continue;
            try (PreparedStatement st = c.prepareStatement(
                    "SELECT 1 FROM referral_costume_claims WHERE inviter_player_id=? AND milestone=?")) {
                st.setLong(1, p.id); st.setInt(2, milestone);
                try (ResultSet rs = st.executeQuery()) { if (rs.next()) continue; }
            }
            if (InventoryService.gI().getCountEmptyBag(p) < 1) break;
            if (ItemService.gI().getTemplate((short) itemId) == null) continue;
            Item item = ItemService.gI().createNewItem((short) itemId, 1);
            if (item == null || item.template == null) continue;
            item.itemOptions.clear();
            try {
                JSONArray list = (JSONArray) JSONValue.parse(options);
                if (list == null) continue;
                for (Object obj : list) {
                    JSONArray pair = (JSONArray) obj;
                    int id = ((Number) pair.get(0)).intValue();
                    int param = ((Number) pair.get(1)).intValue();
                    item.itemOptions.add(new ItemOption(id, param));
                }
            } catch (Exception ex) { continue; }
            if (days > 0) item.itemOptions.add(new ItemOption(93, days));
            // Bắt buộc không giao dịch.
            boolean nonTrade = false;
            for (ItemOption op : item.itemOptions) {
                if (op.optionTemplate.id == 30) { nonTrade = true; break; }
            }
            if (!nonTrade) item.itemOptions.add(new ItemOption(30, 0));
            List<Item> bagBefore = snapshotBag(p);
            c.setAutoCommit(false);
            try {
                try (PreparedStatement st = c.prepareStatement(
                        "INSERT INTO referral_costume_claims(inviter_player_id,milestone,item_template_id) "
                        + "VALUES(?,?,?)")) {
                    st.setLong(1, p.id); st.setInt(2, milestone); st.setInt(3, itemId);
                    st.executeUpdate();
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
