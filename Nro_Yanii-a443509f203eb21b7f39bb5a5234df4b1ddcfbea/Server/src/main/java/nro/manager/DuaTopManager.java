package nro.manager;

import nro.consts.Cmd;
import nro.jdbc.DBService;
import nro.jdbc.daos.PlayerDAO;
import nro.models.Part;
import nro.models.PartManager;
import nro.models.item.Item;
import nro.models.item.ItemOption;
import nro.models.player.Player;
import nro.server.Client;
import nro.server.io.Message;
import nro.services.InventoryService;
import nro.services.ItemService;
import nro.services.Service;
import nro.utils.Log;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Su kien dua top theo mua.
 *
 * Bang xep hang:
 * - POWER: suc manh nhan vat
 * - TASK: nhiem vu chinh
 * - PET: suc manh de tu
 * - RECHARGE: tong nap phat sinh trong mua (tongnap - baseline)
 *
 * Reward duoc cau hinh trong DB (dua_top_reward), khong hard-code trong Java.
 */
public final class DuaTopManager {

    public static final String POWER = "POWER";
    public static final String TASK = "TASK";
    public static final String PET = "PET";
    public static final String RECHARGE = "RECHARGE";
    private static final int TOP_LIMIT = 50;

    private static final DuaTopManager INSTANCE = new DuaTopManager();

    public static DuaTopManager getInstance() {
        return INSTANCE;
    }

    private DuaTopManager() {
    }

    public synchronized void tick() {
        try {
            EventInfo event = getLatestEvent();
            if (event == null) {
                return;
            }
            long now = System.currentTimeMillis();
            if (event.status == 1) {
                ensureRechargeBaseline(event.id);
                if (now >= event.endTime.getTime()) {
                    finalizeEvent(event);
                }
            } else if (event.status == 2 && now > event.claimEnd.getTime()) {
                try (Connection con = DBService.gI().getConnection();
                     PreparedStatement ps = con.prepareStatement(
                             "UPDATE dua_top_event SET status=3 WHERE id=? AND status=2")) {
                    ps.setInt(1, event.id);
                    ps.executeUpdate();
                }
            }
        } catch (Exception e) {
            Log.error(DuaTopManager.class, e);
        }
    }

    public void showTop(Player viewer, String category) {
        try {
            EventInfo event = getLatestEvent();
            if (event == null) {
                Service.getInstance().sendThongBaoOK(viewer, "Hiện chưa có mùa đua Top.");
                return;
            }
            if (event.status == 1) {
                ensureRechargeBaseline(event.id);
            }
            List<TopEntry> entries = loadTop(event, category);
            Message msg = new Message(Cmd.TOP);
            msg.writer().writeByte(0);
            msg.writer().writeUTF(getTitle(category, event.status));
            msg.writer().writeByte(entries.size());
            for (TopEntry entry : entries) {
                Player pl = entry.player;
                msg.writer().writeInt(entry.rank);
                msg.writer().writeInt((int) pl.id);
                short head = pl.getHead();
                msg.writer().writeShort(head);
                if (viewer.isVersionAbove(220)) {
                    Part part = PartManager.getInstance().find(head);
                    msg.writer().writeShort(part != null ? part.getIcon(0) : 0);
                }
                msg.writer().writeShort(pl.getBody());
                msg.writer().writeShort(pl.getLeg());
                msg.writer().writeUTF(pl.name);
                msg.writer().writeUTF(Client.gI().getPlayer(pl.id) != null ? "Online" : "");
                msg.writer().writeUTF(entry.detail);
            }
            viewer.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Log.error(DuaTopManager.class, e);
            Service.getInstance().sendThongBaoOK(viewer, "Không thể tải bảng đua Top lúc này.");
        }
    }

    public void showInfo(Player player) {
        try {
            EventInfo e = getLatestEvent();
            if (e == null) {
                Service.getInstance().sendThongBaoOK(player, "Hiện chưa có mùa đua Top.");
                return;
            }
            String state = e.status == 1 ? "ĐANG ĐUA" : e.status == 2 ? "ĐANG NHẬN THƯỞNG" : "ĐÃ KẾT THÚC";
            String text = "|1|SỰ KIỆN ĐUA TOP\n"
                    + "Trạng thái: " + state + "\n"
                    + "Bắt đầu: " + e.startTime + "\n"
                    + "Kết thúc: " + e.endTime + "\n"
                    + "Hạn nhận: " + e.claimEnd + "\n\n"
                    + "|2|4 BẢNG XẾP HẠNG\n"
                    + "- Top Sức Mạnh\n"
                    + "- Top Nhiệm Vụ\n"
                    + "- Top Sức Mạnh Đệ Tử\n"
                    + "- Top Nạp trong mùa\n\n"
                    + "|7|Top Nạp chỉ tính tiền phát sinh sau khi mùa bắt đầu.\n"
                    + "Top 1 Nạp phải đạt tối thiểu " + money(e.minTop1Recharge) + " VND.\n"
                    + "Top 2 trở xuống không có mốc tối thiểu nhưng phải có phát sinh nạp.\n\n"
                    + "|3|Chỉ Top 1-3 có Danh Hiệu. Top 1-10 có Cải Trang. "
                    + "Top 11-50 nhận Thỏi Vàng + Hồng Ngọc.";
            Service.getInstance().sendThongBaoOK(player, text);
        } catch (Exception ex) {
            Log.error(DuaTopManager.class, ex);
        }
    }

    /**
     * Nhan tat ca cac giai ma player dat duoc trong mua hien tai.
     * Moi category chi nhan 1 lan nho dua_top_result.claimed.
     */
    public synchronized void claimAll(Player player) {
        try {
            EventInfo event = getLatestEvent();
            if (event == null || event.status != 2) {
                Service.getInstance().sendThongBaoOK(player,
                        "Chưa đến thời gian nhận thưởng hoặc mùa đua Top chưa chốt.");
                return;
            }
            if (System.currentTimeMillis() > event.claimEnd.getTime()) {
                Service.getInstance().sendThongBaoOK(player, "Đã hết hạn nhận thưởng đua Top.");
                return;
            }

            List<ResultInfo> wins = loadUnclaimedResults(event.id, (int) player.id);
            if (wins.isEmpty()) {
                String noReward = hasIneligibleTop1Recharge(event.id, (int) player.id)
                        ? "Bạn đứng Top 1 Nạp nhưng chưa đạt mốc tối thiểu "
                            + money(event.minTop1Recharge) + " VND nên không đủ điều kiện nhận giải Top 1."
                        : "Bạn không có phần thưởng đua Top chưa nhận.";
                Service.getInstance().sendThongBaoOK(player, noReward);
                return;
            }

            int claimedCount = 0;
            StringBuilder received = new StringBuilder("|1|NHẬN THƯỞNG ĐUA TOP\n");
            for (ResultInfo result : wins) {
                RewardInfo reward = loadReward(event.id, result.category, result.rank);
                if (reward == null) {
                    continue;
                }
                int needSlots = (reward.costumeId > 0 ? 1 : 0) + (reward.titleId > 0 ? 1 : 0);
                if (InventoryService.gI().getCountEmptyBag(player) < needSlots) {
                    received.append("\n").append(categoryName(result.category))
                            .append(" Top ").append(result.rank)
                            .append(": cần trống ").append(needSlots).append(" ô hành trang.");
                    continue;
                }

                Item costume = reward.costumeId > 0
                        ? createRewardItem(reward.costumeId, reward.costumeOptions) : null;
                Item title = reward.titleId > 0
                        ? createRewardItem(reward.titleId, reward.titleOptions) : null;

                if (costume != null && !InventoryService.gI().canAddItemBag(player, costume, 99)) {
                    received.append("\n").append(categoryName(result.category))
                            .append(": không thể thêm cải trang vào hành trang.");
                    continue;
                }
                if (title != null && !InventoryService.gI().canAddItemBag(player, title, 99)) {
                    received.append("\n").append(categoryName(result.category))
                            .append(": không thể thêm danh hiệu vào hành trang.");
                    continue;
                }

                if (costume != null) {
                    InventoryService.gI().addItemBag(player, costume, 99);
                }
                if (title != null) {
                    InventoryService.gI().addItemBag(player, title, 99);
                }
                if (reward.ruby > 0) {
                    player.inventory.addRuby(reward.ruby);
                }

                // Luu vat pham + ruby + thoi vang + trang thai claimed trong mot transaction.
                try (Connection con = DBService.gI().getConnection()) {
                    con.setAutoCommit(false);
                    try {
                        if (reward.goldBar > 0) {
                            try (PreparedStatement ps = con.prepareStatement(
                                    "UPDATE account SET thoi_vang=thoi_vang+? WHERE id=?")) {
                                ps.setInt(1, reward.goldBar);
                                ps.setInt(2, player.getSession().userId);
                                ps.executeUpdate();
                            }
                            player.getSession().goldBar += reward.goldBar;
                        }

                        PlayerDAO.updatePlayer(player, con);

                        try (PreparedStatement ps = con.prepareStatement(
                                "UPDATE dua_top_result SET claimed=1,claimed_at=NOW() "
                                        + "WHERE id=? AND claimed=0 AND eligible=1")) {
                            ps.setLong(1, result.resultId);
                            if (ps.executeUpdate() != 1) {
                                throw new IllegalStateException("Reward already claimed");
                            }
                        }
                        con.commit();
                    } catch (Exception ex) {
                        con.rollback();
                        // Hoan tien memory neu transaction bi loi. Vat pham da add vao bag
                        // se duoc reload tu DB sau khi reconnect; khong mark claimed.
                        if (reward.goldBar > 0) {
                            player.getSession().goldBar -= reward.goldBar;
                        }
                        throw ex;
                    } finally {
                        con.setAutoCommit(true);
                    }
                }

                claimedCount++;
                received.append("\n")
                        .append(categoryName(result.category)).append(" Top ").append(result.rank)
                        .append(": +").append(reward.goldBar).append(" Thỏi Vàng")
                        .append(", +").append(money(reward.ruby)).append(" Hồng Ngọc");
                if (reward.costumeId > 0) {
                    received.append(", Cải Trang #").append(reward.costumeId);
                }
                if (reward.titleId > 0) {
                    received.append(", Danh Hiệu #").append(reward.titleId);
                }
            }

            InventoryService.gI().sendItemBags(player);
            Service.getInstance().sendMoney(player);
            Service.getInstance().point(player);

            if (claimedCount == 0 && received.indexOf("cần trống") < 0
                    && received.indexOf("không thể") < 0) {
                Service.getInstance().sendThongBaoOK(player, "Không có phần thưởng hợp lệ để nhận.");
            } else {
                Service.getInstance().sendThongBaoOK(player, received.toString());
            }
        } catch (Exception e) {
            Log.error(DuaTopManager.class, e);
            Service.getInstance().sendThongBaoOK(player,
                    "Có lỗi khi nhận thưởng. Phần thưởng chưa nhận vẫn được giữ lại.");
        }
    }

    private Item createRewardItem(int itemId, String optionString) {
        Item item = ItemService.gI().createNewItem((short) itemId, 1);
        item.itemOptions.clear();
        if (optionString != null && !optionString.trim().isEmpty()) {
            String[] options = optionString.split(";");
            for (String option : options) {
                String[] pair = option.split(":");
                if (pair.length == 2) {
                    int id = Integer.parseInt(pair[0].trim());
                    int param = Integer.parseInt(pair[1].trim());
                    item.itemOptions.add(new ItemOption(id, param));
                }
            }
        }
        return item;
    }

    private void ensureRechargeBaseline(int eventId) throws Exception {
        try (Connection con = DBService.gI().getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT IGNORE INTO dua_top_recharge_baseline(event_id,account_id,tongnap_start) "
                             + "SELECT ?,id,GREATEST(COALESCE(tongnap,0),0) FROM account")) {
            ps.setInt(1, eventId);
            ps.executeUpdate();
        }
    }

    private void finalizeEvent(EventInfo event) throws Exception {
        try (Connection con = DBService.gI().getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM dua_top_result WHERE event_id=?")) {
                    ps.setInt(1, event.id);
                    ps.executeUpdate();
                }

                finalizeCategory(con, event, POWER);
                finalizeCategory(con, event, TASK);
                finalizeCategory(con, event, PET);
                finalizeCategory(con, event, RECHARGE);

                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE dua_top_event SET status=2 WHERE id=? AND status=1")) {
                    ps.setInt(1, event.id);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    private void finalizeCategory(Connection con, EventInfo event, String category) throws Exception {
        String sql = liveTopSql(category);
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            if (RECHARGE.equals(category)) {
                ps.setInt(1, event.id);
            }
            try (ResultSet rs = ps.executeQuery();
                 PreparedStatement ins = con.prepareStatement(
                         "INSERT INTO dua_top_result(event_id,category,rank_no,player_id,account_id,"
                                 + "score,detail,eligible,claimed) VALUES(?,?,?,?,?,?,?,?,0)")) {
                int rank = 0;
                while (rs.next() && rank < TOP_LIMIT) {
                    rank++;
                    long score = rs.getLong("score");
                    int eligible = 1;
                    if (RECHARGE.equals(category) && rank == 1 && score < event.minTop1Recharge) {
                        eligible = 0;
                    }
                    ins.setInt(1, event.id);
                    ins.setString(2, category);
                    ins.setInt(3, rank);
                    ins.setInt(4, rs.getInt("id"));
                    ins.setInt(5, rs.getInt("account_id"));
                    ins.setLong(6, score);
                    ins.setString(7, buildDetail(category, rs, score));
                    ins.setInt(8, eligible);
                    ins.addBatch();
                }
                ins.executeBatch();
            }
        }
    }

    private List<TopEntry> loadTop(EventInfo event, String category) throws Exception {
        List<TopEntry> list = new ArrayList<>();
        String sql;
        boolean finalized = event.status >= 2;
        if (finalized) {
            sql = "SELECT p.*,r.rank_no,r.score,r.detail FROM dua_top_result r "
                    + "JOIN player p ON p.id=r.player_id "
                    + "WHERE r.event_id=? AND r.category=? ORDER BY r.rank_no ASC LIMIT 50";
        } else {
            sql = liveTopSql(category);
        }

        try (Connection con = DBService.gI().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (finalized) {
                ps.setInt(1, event.id);
                ps.setString(2, category);
            } else if (RECHARGE.equals(category)) {
                ps.setInt(1, event.id);
            }

            try (ResultSet rs = ps.executeQuery()) {
                int rank = 0;
                while (rs.next() && rank < TOP_LIMIT) {
                    rank++;
                    Player pl = readDisplayPlayer(rs);
                    long score = rs.getLong("score");
                    String detail = finalized
                            ? rs.getString("detail")
                            : buildDetail(category, rs, score);
                    list.add(new TopEntry(finalized ? rs.getInt("rank_no") : rank, pl, score, detail));
                }
            }
        }
        return list;
    }

    private String liveTopSql(String category) {
        switch (category) {
            case TASK:
                return "SELECT p.*, "
                        + "(CAST(split_str(p.data_task,',',2) AS UNSIGNED)*1000000 "
                        + "+ CAST(split_str(p.data_task,',',3) AS UNSIGNED)) AS score, "
                        + "CAST(split_str(p.data_task,',',2) AS UNSIGNED) AS task_main, "
                        + "CAST(split_str(p.data_task,',',3) AS UNSIGNED) AS task_sub "
                        + "FROM player p "
                        + "ORDER BY task_main DESC,task_sub DESC,p.power DESC LIMIT 50";
            case PET:
                return "SELECT p.*,GREATEST(COALESCE(p.pet_power,0),0) AS score "
                        + "FROM player p WHERE COALESCE(p.pet_power,0)>0 "
                        + "ORDER BY score DESC,p.power DESC LIMIT 50";
            case RECHARGE:
                return "SELECT p.*,GREATEST(COALESCE(a.tongnap,0)-COALESCE(b.tongnap_start,0),0) AS score "
                        + "FROM player p JOIN account a ON a.id=p.account_id "
                        + "JOIN dua_top_recharge_baseline b ON b.account_id=a.id AND b.event_id=? "
                        + "WHERE GREATEST(COALESCE(a.tongnap,0)-COALESCE(b.tongnap_start,0),0)>0 "
                        + "ORDER BY score DESC,p.power DESC LIMIT 50";
            case POWER:
            default:
                return "SELECT p.*,GREATEST(COALESCE(p.power,0),0) AS score "
                        + "FROM player p ORDER BY score DESC LIMIT 50";
        }
    }

    private String buildDetail(String category, ResultSet rs, long score) throws Exception {
        switch (category) {
            case TASK:
                return "NV: " + rs.getLong("task_main") + " - bước " + rs.getLong("task_sub");
            case PET:
                return "SM đệ tử: " + money(score);
            case RECHARGE:
                return "Nạp mùa: " + money(score) + " VND";
            case POWER:
            default:
                return "Sức mạnh: " + money(score);
        }
    }

    private Player readDisplayPlayer(ResultSet rs) throws Exception {
        Player player = new Player();
        player.id = rs.getInt("id");
        player.name = rs.getString("name");
        player.head = rs.getShort("head");
        player.gender = rs.getByte("gender");

        String bodyData = rs.getString("items_body");
        JSONArray dataArray = (JSONArray) JSONValue.parse(bodyData);
        if (dataArray != null) {
            for (Object obj : dataArray) {
                JSONObject dataObject = (JSONObject) JSONValue.parse(String.valueOf(obj));
                short tempId = Short.parseShort(String.valueOf(dataObject.get("temp_id")));
                Item item;
                if (tempId != -1) {
                    item = ItemService.gI().createNewItem(tempId,
                            Integer.parseInt(String.valueOf(dataObject.get("quantity"))));
                    Object optionData = dataObject.get("option");
                    JSONArray options = optionData instanceof JSONArray
                            ? (JSONArray) optionData
                            : (JSONArray) JSONValue.parse(String.valueOf(optionData).replace("\\\"", ""));
                    if (options != null) {
                        for (Object optionObj : options) {
                            JSONArray opt = optionObj instanceof JSONArray
                                    ? (JSONArray) optionObj
                                    : (JSONArray) JSONValue.parse(String.valueOf(optionObj));
                            if (opt != null && opt.size() >= 2) {
                                item.itemOptions.add(new ItemOption(
                                        Integer.parseInt(String.valueOf(opt.get(0))),
                                        Integer.parseInt(String.valueOf(opt.get(1)))));
                            }
                        }
                    }
                    Object createTime = dataObject.get("create_time");
                    if (createTime != null) {
                        item.createTime = Long.parseLong(String.valueOf(createTime));
                    }
                    if (ItemService.gI().isOutOfDateTime(item)) {
                        item = ItemService.gI().createItemNull();
                    }
                } else {
                    item = ItemService.gI().createItemNull();
                }
                player.inventory.itemsBody.add(item);
            }
        }
        return player;
    }

    private List<ResultInfo> loadUnclaimedResults(int eventId, int playerId) throws Exception {
        List<ResultInfo> list = new ArrayList<>();
        try (Connection con = DBService.gI().getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id,category,rank_no,score FROM dua_top_result "
                             + "WHERE event_id=? AND player_id=? AND eligible=1 AND claimed=0 "
                             + "ORDER BY FIELD(category,'POWER','TASK','PET','RECHARGE')")) {
            ps.setInt(1, eventId);
            ps.setInt(2, playerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ResultInfo(rs.getLong("id"), rs.getString("category"),
                            rs.getInt("rank_no"), rs.getLong("score")));
                }
            }
        }
        return list;
    }

    private boolean hasIneligibleTop1Recharge(int eventId, int playerId) throws Exception {
        try (Connection con = DBService.gI().getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT 1 FROM dua_top_result WHERE event_id=? AND player_id=? "
                             + "AND category='RECHARGE' AND rank_no=1 AND eligible=0 LIMIT 1")) {
            ps.setInt(1, eventId);
            ps.setInt(2, playerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private RewardInfo loadReward(int eventId, String category, int rank) throws Exception {
        try (Connection con = DBService.gI().getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT * FROM dua_top_reward WHERE event_id=? AND category=? "
                             + "AND ? BETWEEN rank_from AND rank_to LIMIT 1")) {
            ps.setInt(1, eventId);
            ps.setString(2, category);
            ps.setInt(3, rank);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new RewardInfo(
                        rs.getInt("gold_bar"), rs.getInt("ruby"),
                        rs.getInt("costume_id"), rs.getString("costume_options"),
                        rs.getInt("title_id"), rs.getString("title_options"));
            }
        }
    }

    private EventInfo getLatestEvent() throws Exception {
        try (Connection con = DBService.gI().getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT * FROM dua_top_event WHERE status IN (1,2) ORDER BY id DESC LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                return null;
            }
            return new EventInfo(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getTimestamp("start_time"),
                    rs.getTimestamp("end_time"),
                    rs.getTimestamp("claim_end"),
                    rs.getInt("status"),
                    rs.getInt("min_top1_recharge"));
        }
    }

    private String getTitle(String category, int status) {
        String prefix = status >= 2 ? "KẾT QUẢ " : "";
        switch (category) {
            case TASK:
                return prefix + "TOP 50 NHIỆM VỤ";
            case PET:
                return prefix + "TOP 50 SỨC MẠNH ĐỆ TỬ";
            case RECHARGE:
                return prefix + "TOP 50 NẠP MÙA";
            case POWER:
            default:
                return prefix + "TOP 50 SỨC MẠNH";
        }
    }

    private String categoryName(String category) {
        switch (category) {
            case TASK:
                return "Nhiệm Vụ";
            case PET:
                return "Đệ Tử";
            case RECHARGE:
                return "Nạp";
            case POWER:
            default:
                return "Sức Mạnh";
        }
    }

    private String money(long value) {
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(value);
    }

    private static final class EventInfo {
        final int id;
        final String name;
        final Timestamp startTime;
        final Timestamp endTime;
        final Timestamp claimEnd;
        final int status;
        final int minTop1Recharge;

        EventInfo(int id, String name, Timestamp startTime, Timestamp endTime,
                  Timestamp claimEnd, int status, int minTop1Recharge) {
            this.id = id;
            this.name = name;
            this.startTime = startTime;
            this.endTime = endTime;
            this.claimEnd = claimEnd;
            this.status = status;
            this.minTop1Recharge = minTop1Recharge;
        }
    }

    private static final class TopEntry {
        final int rank;
        final Player player;
        final long score;
        final String detail;

        TopEntry(int rank, Player player, long score, String detail) {
            this.rank = rank;
            this.player = player;
            this.score = score;
            this.detail = detail;
        }
    }

    private static final class ResultInfo {
        final long resultId;
        final String category;
        final int rank;
        final long score;

        ResultInfo(long resultId, String category, int rank, long score) {
            this.resultId = resultId;
            this.category = category;
            this.rank = rank;
            this.score = score;
        }
    }

    private static final class RewardInfo {
        final int goldBar;
        final int ruby;
        final int costumeId;
        final String costumeOptions;
        final int titleId;
        final String titleOptions;

        RewardInfo(int goldBar, int ruby, int costumeId, String costumeOptions,
                   int titleId, String titleOptions) {
            this.goldBar = goldBar;
            this.ruby = ruby;
            this.costumeId = costumeId;
            this.costumeOptions = costumeOptions;
            this.titleId = titleId;
            this.titleOptions = titleOptions;
        }
    }
}
