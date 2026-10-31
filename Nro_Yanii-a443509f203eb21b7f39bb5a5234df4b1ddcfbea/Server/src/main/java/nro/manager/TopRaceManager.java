package nro.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import nro.jdbc.DBService;
import nro.jdbc.daos.PlayerDAO;
import nro.models.item.Item;
import nro.models.item.ItemOption;
import nro.models.item.TrainingArmorLevel4;
import nro.models.player.Player;
import nro.services.InventoryService;
import nro.services.ItemService;
import nro.services.Service;
import nro.utils.Log;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Su kien dua Top 4 bang: POWER, TASK, PET, NAP.
 *
 * Top NAP khong reset tongnap cu. Khi mo mua, server snapshot account.tongnap
 * vao top_race_nap_baseline va chi tinh phan tang them trong mua.
 */
public class TopRaceManager {

    public static final String POWER = "POWER";
    public static final String TASK = "TASK";
    public static final String PET = "PET";
    public static final String NAP = "NAP";

    private static final int MAX_TOP = 50;
    private static final long NEW_ACCOUNT_TONGNAP_DEFAULT = 1000L;
    private static final TopRaceManager INSTANCE = new TopRaceManager();

    public static TopRaceManager gI() {
        return INSTANCE;
    }

    private TopRaceManager() {
    }

    public static class Season {
        public int id;
        public String name;
        public Timestamp startTime;
        public Timestamp endTime;
        public String status;
    }

    public static class RankEntry {
        public int rank;
        public Player player;
        public long score;
        public String detail;

        public RankEntry(int rank, Player player, long score, String detail) {
            this.rank = rank;
            this.player = player;
            this.score = score;
            this.detail = detail;
        }
    }

    public static class RewardConfig {
        public int goldBar;
        public int ruby;
        public int costumeItemId;
        public String costumeOptions;
        public int titleItemId;
        public String titleOptions;
        public long minScore;
    }

    public void tick() {
        try {
            Season season = getActiveSeason();
            if (season != null && season.endTime != null
                    && System.currentTimeMillis() >= season.endTime.getTime()) {
                finalizeActiveSeason();
            }
        } catch (Exception e) {
            Log.error(TopRaceManager.class, e);
        }
    }

    public synchronized String startSeason(int days) {
        if (days <= 0 || days > 365) {
            return "Số ngày phải từ 1 đến 365.";
        }
        try {
            Season current = getActiveSeason();
            if (current != null) {
                return "Đang có mùa hoạt động: " + current.name + " - kết thúc " + formatTime(current.endTime);
            }

            long now = System.currentTimeMillis();
            Timestamp start = new Timestamp(now);
            Timestamp end = new Timestamp(now + days * 86_400_000L);
            String name = "Đua Top " + new SimpleDateFormat("dd/MM/yyyy").format(start);

            try (Connection con = DBService.gI().getConnection()) {
                con.setAutoCommit(false);
                int seasonId;
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO top_race_season(name,start_time,end_time,status) VALUES(?,?,?,'ACTIVE')",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, name);
                    ps.setTimestamp(2, start);
                    ps.setTimestamp(3, end);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) {
                            con.rollback();
                            return "Không tạo được mùa đua Top.";
                        }
                        seasonId = rs.getInt(1);
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO top_race_nap_baseline(season_id,account_id,baseline_tongnap) "
                                + "SELECT ?,id,tongnap FROM account")) {
                    ps.setInt(1, seasonId);
                    ps.executeUpdate();
                }
                con.commit();
            }
            return "Đã mở " + name + " trong " + days + " ngày. Kết thúc: " + formatTime(end);
        } catch (Exception e) {
            Log.error(TopRaceManager.class, e);
            return "Không thể mở đua Top. Hãy kiểm tra đã import SQL sự kiện chưa.";
        }
    }

    public synchronized String finalizeActiveSeason() {
        Season season = getActiveSeason();
        if (season == null) {
            return "Không có mùa đua Top đang hoạt động.";
        }
        try {
            String[] types = {POWER, TASK, PET, NAP};
            List<List<RankEntry>> all = new ArrayList<>();
            for (String type : types) {
                all.add(queryLiveRanking(season.id, type));
            }

            try (Connection con = DBService.gI().getConnection()) {
                con.setAutoCommit(false);
                try (PreparedStatement del = con.prepareStatement(
                        "DELETE FROM top_race_result WHERE season_id=?")) {
                    del.setInt(1, season.id);
                    del.executeUpdate();
                }

                String sql = "INSERT INTO top_race_result"
                        + "(season_id,race_type,rank_no,player_id,account_id,score,detail,reward_status) "
                        + "VALUES(?,?,?,?,?,?,?,?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    for (int t = 0; t < types.length; t++) {
                        String type = types[t];
                        for (RankEntry entry : all.get(t)) {
                            int rewardStatus = 0;
                            RewardConfig cfg = getRewardConfig(type, entry.rank);
                            if (cfg == null || entry.score < cfg.minScore) {
                                rewardStatus = 2;
                            }
                            ps.setInt(1, season.id);
                            ps.setString(2, type);
                            ps.setInt(3, entry.rank);
                            ps.setInt(4, (int) entry.player.id);
                            ps.setInt(5, getAccountId((int) entry.player.id));
                            ps.setLong(6, entry.score);
                            ps.setString(7, entry.detail);
                            ps.setInt(8, rewardStatus);
                            ps.addBatch();
                        }
                    }
                    ps.executeBatch();
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE top_race_season SET status='FINALIZED', finalized_at=NOW() WHERE id=?")) {
                    ps.setInt(1, season.id);
                    ps.executeUpdate();
                }
                con.commit();
            }
            return "Đã chốt " + season.name + ". Người chơi có thể nhận thưởng tại Trọng Tài.";
        } catch (Exception e) {
            Log.error(TopRaceManager.class, e);
            return "Chốt đua Top thất bại: " + e.getMessage();
        }
    }

    private int getAccountId(int playerId) {
        try (Connection con = DBService.gI().getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT account_id FROM player WHERE id=? LIMIT 1")) {
            ps.setInt(1, playerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    public Season getActiveSeason() {
        return getSeasonByStatus("ACTIVE");
    }

    public Season getLatestFinalizedSeason() {
        return getSeasonByStatus("FINALIZED");
    }

    private Season getSeasonByStatus(String status) {
        String sql = "SELECT id,name,start_time,end_time,status FROM top_race_season "
                + "WHERE status=? ORDER BY id DESC LIMIT 1";
        try (Connection con = DBService.gI().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Season season = new Season();
                    season.id = rs.getInt("id");
                    season.name = rs.getString("name");
                    season.startTime = rs.getTimestamp("start_time");
                    season.endTime = rs.getTimestamp("end_time");
                    season.status = rs.getString("status");
                    return season;
                }
            }
        } catch (Exception e) {
            // SQL event may not have been imported yet.
        }
        return null;
    }

    public String getStatusText() {
        Season active = getActiveSeason();
        if (active != null) {
            return active.name + "\nBắt đầu: " + formatTime(active.startTime)
                    + "\nKết thúc: " + formatTime(active.endTime)
                    + "\nTop Nạp tính riêng số tiền phát sinh trong mùa.";
        }
        Season last = getLatestFinalizedSeason();
        if (last != null) {
            return "Hiện không có mùa đang chạy.\nMùa gần nhất: " + last.name
                    + "\nĐã chốt, có thể nhận thưởng.";
        }
        return "Chưa có mùa đua Top. Admin dùng: duatop start <số ngày>";
    }

    public List<RankEntry> getRanking(String type) {
        tick();
        Season active = getActiveSeason();
        if (active != null) {
            return queryLiveRanking(active.id, type);
        }
        Season last = getLatestFinalizedSeason();
        if (last != null) {
            return queryFinalRanking(last.id, type);
        }
        return Collections.emptyList();
    }

    private List<RankEntry> queryLiveRanking(int seasonId, String type) {
        List<RankEntry> list = new ArrayList<>();
        String sql;
        switch (type) {
            case TASK:
                sql = "SELECT p.*, "
                        + "CAST(REPLACE(SUBSTRING_INDEX(p.data_task, ',', 1), '[', '') AS UNSIGNED) AS task_count, "
                        + "CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(p.data_task, ',', 2), ',', -1) AS UNSIGNED) AS task_id, "
                        + "CAST(REPLACE(SUBSTRING_INDEX(SUBSTRING_INDEX(p.data_task, ',', 3), ',', -1), ']', '') AS UNSIGNED) AS task_index "
                        + "FROM player p JOIN account a ON a.id=p.account_id "
                        + "WHERE a.is_admin=0 AND a.ban=0 "
                        + "ORDER BY task_id DESC, task_index DESC, task_count DESC, p.power DESC LIMIT " + MAX_TOP;
                break;
            case PET:
                sql = "SELECT p.*, p.pet_power AS race_score FROM player p "
                        + "JOIN account a ON a.id=p.account_id "
                        + "WHERE a.is_admin=0 AND a.ban=0 AND p.pet_power>0 "
                        + "ORDER BY p.pet_power DESC, p.power DESC LIMIT " + MAX_TOP;
                break;
            case NAP:
                sql = "SELECT p.*, GREATEST(a.tongnap-COALESCE(b.baseline_tongnap,"
                        + NEW_ACCOUNT_TONGNAP_DEFAULT + "),0) AS race_score "
                        + "FROM player p JOIN account a ON a.id=p.account_id "
                        + "LEFT JOIN top_race_nap_baseline b ON b.season_id=? AND b.account_id=a.id "
                        + "WHERE a.is_admin=0 AND a.ban=0 "
                        + "AND GREATEST(a.tongnap-COALESCE(b.baseline_tongnap,"
                        + NEW_ACCOUNT_TONGNAP_DEFAULT + "),0)>0 "
                        + "ORDER BY race_score DESC, p.power DESC LIMIT " + MAX_TOP;
                break;
            case POWER:
            default:
                sql = "SELECT p.*, p.power AS race_score FROM player p "
                        + "JOIN account a ON a.id=p.account_id "
                        + "WHERE a.is_admin=0 AND a.ban=0 "
                        + "ORDER BY p.power DESC LIMIT " + MAX_TOP;
                break;
        }

        try (Connection con = DBService.gI().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            if (NAP.equals(type)) {
                ps.setInt(1, seasonId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    Player player = buildTopPlayer(rs);
                    long score;
                    String detail;
                    if (TASK.equals(type)) {
                        long taskId = rs.getLong("task_id");
                        long taskIndex = rs.getLong("task_index");
                        long taskCount = rs.getLong("task_count");
                        score = taskId * 1_000_000_000L + taskIndex * 1_000_000L
                                + Math.min(taskCount, 999_999L);
                        detail = "NV " + taskId + "." + taskIndex + " - tiến độ " + taskCount;
                    } else {
                        score = rs.getLong("race_score");
                        if (NAP.equals(type)) {
                            detail = score + " VNĐ";
                        } else if (PET.equals(type)) {
                            detail = "Sức mạnh đệ tử: " + score;
                        } else {
                            detail = "Sức mạnh: " + score;
                        }
                    }
                    list.add(new RankEntry(rank++, player, score, detail));
                }
            }
        } catch (Exception e) {
            Log.error(TopRaceManager.class, e);
        }
        return list;
    }

    private List<RankEntry> queryFinalRanking(int seasonId, String type) {
        List<RankEntry> list = new ArrayList<>();
        String sql = "SELECT p.*,r.rank_no,r.score AS race_score,r.detail AS race_detail "
                + "FROM top_race_result r JOIN player p ON p.id=r.player_id "
                + "WHERE r.season_id=? AND r.race_type=? ORDER BY r.rank_no";
        try (Connection con = DBService.gI().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, seasonId);
            ps.setString(2, type);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new RankEntry(rs.getInt("rank_no"), buildTopPlayer(rs),
                            rs.getLong("race_score"), rs.getString("race_detail")));
                }
            }
        } catch (Exception e) {
            Log.error(TopRaceManager.class, e);
        }
        return list;
    }

    private Player buildTopPlayer(ResultSet rs) throws Exception {
        Player player = new Player();
        player.id = rs.getInt("id");
        player.name = rs.getString("name");
        player.head = rs.getShort("head");
        player.gender = rs.getByte("gender");

        JSONArray dataPoint = (JSONArray) JSONValue.parse(rs.getString("data_point"));
        if (dataPoint != null && dataPoint.size() > 11) {
            player.nPoint.power = Long.parseLong(dataPoint.get(11).toString());
        }

        JSONArray dataArray = (JSONArray) JSONValue.parse(rs.getString("items_body"));
        JSONObject dataObject = null;
        if (dataArray != null) {
            for (Object raw : dataArray) {
                Item item;
                dataObject = (JSONObject) JSONValue.parse(raw.toString());
                short tempId = Short.parseShort(String.valueOf(dataObject.get("temp_id")));
                if (tempId != -1) {
                    item = ItemService.gI().createNewItem(tempId,
                            Integer.parseInt(String.valueOf(dataObject.get("quantity"))));
                    JSONArray options = (JSONArray) JSONValue.parse(
                            String.valueOf(dataObject.get("option")).replaceAll("\"", ""));
                    if (options != null) {
                        for (Object optionRaw : options) {
                            JSONArray opt = (JSONArray) JSONValue.parse(String.valueOf(optionRaw));
                            item.itemOptions.add(new ItemOption(
                                    Integer.parseInt(String.valueOf(opt.get(0))),
                                    Integer.parseInt(String.valueOf(opt.get(1)))));
                        }
                    }
                    item.createTime = Long.parseLong(String.valueOf(dataObject.get("create_time")));
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

    public RewardConfig getRewardConfig(String type, int rank) {
        String sql = "SELECT gold_bar,ruby,costume_item_id,costume_options,title_item_id,title_options,min_score "
                + "FROM top_race_reward_config WHERE race_type=? AND ? BETWEEN rank_from AND rank_to LIMIT 1";
        try (Connection con = DBService.gI().getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, type);
            ps.setInt(2, rank);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    RewardConfig cfg = new RewardConfig();
                    cfg.goldBar = rs.getInt("gold_bar");
                    cfg.ruby = rs.getInt("ruby");
                    cfg.costumeItemId = rs.getInt("costume_item_id");
                    cfg.costumeOptions = rs.getString("costume_options");
                    cfg.titleItemId = rs.getInt("title_item_id");
                    cfg.titleOptions = rs.getString("title_options");
                    cfg.minScore = rs.getLong("min_score");
                    return cfg;
                }
            }
        } catch (Exception e) {
            Log.error(TopRaceManager.class, e);
        }
        return null;
    }

    /**
     * Top 1/2/3 of each seasonal race receives one additional training armor.
     * The existing title/costume/currency rewards are left untouched.
     */
    public static boolean awardsTrainingArmorLevel4(String type, int rank) {
        return rank >= 1 && rank <= 3
                && (POWER.equals(type) || TASK.equals(type)
                    || PET.equals(type) || NAP.equals(type));
    }

    public synchronized String claimRewards(Player player) {
        class Pending {
            long resultId;
            int seasonId;
            String type;
            int rank;
            long score;
        }

        List<Pending> pendings = new ArrayList<>();
        try (Connection con = DBService.gI().getConnection();
                PreparedStatement ps = con.prepareStatement(
                        "SELECT r.id,r.season_id,r.race_type,r.rank_no,r.score "
                                + "FROM top_race_result r JOIN top_race_season s ON s.id=r.season_id "
                                + "WHERE r.player_id=? AND r.reward_status=0 AND s.status='FINALIZED' "
                                + "ORDER BY r.season_id ASC,r.race_type ASC")) {
            ps.setInt(1, (int) player.id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Pending p = new Pending();
                    p.resultId = rs.getLong("id");
                    p.seasonId = rs.getInt("season_id");
                    p.type = rs.getString("race_type");
                    p.rank = rs.getInt("rank_no");
                    p.score = rs.getLong("score");
                    pendings.add(p);
                }
            }
        } catch (Exception e) {
            Log.error(TopRaceManager.class, e);
            return "Không đọc được phần thưởng đua Top. Hãy kiểm tra đã import SQL sự kiện chưa.";
        }

        if (pendings.isEmpty()) {
            return "Bạn không có phần thưởng đua Top chưa nhận.";
        }

        int claimed = 0;
        StringBuilder detail = new StringBuilder();
        for (Pending pending : pendings) {
            RewardConfig cfg = getRewardConfig(pending.type, pending.rank);
            if (cfg == null || pending.score < cfg.minScore) {
                updateRewardStatus(pending.resultId, 2);
                continue;
            }

            boolean awardTrainingArmor = awardsTrainingArmorLevel4(pending.type, pending.rank);
            int trainingArmorId = awardTrainingArmor ? TrainingArmorLevel4.findTemplateId() : -1;
            if (awardTrainingArmor && trainingArmorId < 0) {
                return "Chưa tải được Giáp tập luyện cấp 4. Báo Admin kiểm tra dữ liệu vật phẩm.";
            }

            int requiredSlots = 1;
            if (awardTrainingArmor) {
                requiredSlots++;
            }
            if (cfg.costumeItemId > 0) {
                requiredSlots++;
            }
            if (cfg.titleItemId > 0) {
                requiredSlots++;
            }
            int empty = InventoryService.gI().getCountEmptyBag(player)
                    + InventoryService.gI().getCountEmptyListItem(player.inventory.itemsBox);
            if (empty < requiredSlots) {
                return "Cần ít nhất " + requiredSlots
                        + " ô trống trong hành trang + rương để nhận trọn gói thưởng.";
            }

            boolean ok = true;
            if (cfg.goldBar > 0) {
                Item goldBar = ItemService.gI().createNewItem((short) 457, cfg.goldBar);
                ok = addBagOrBox(player, goldBar);
            }
            if (ok && cfg.costumeItemId > 0) {
                Item costume = createRewardItem(cfg.costumeItemId, cfg.costumeOptions);
                ok = addBagOrBox(player, costume);
            }
            if (ok && cfg.titleItemId > 0) {
                Item title = createRewardItem(cfg.titleItemId, cfg.titleOptions);
                ok = addBagOrBox(player, title);
            }
            if (ok && awardTrainingArmor) {
                Item armor = ItemService.gI().createNewItem((short) trainingArmorId, 1);
                // Existing NPoint training-armor logic uses option 9 after
                // unequipping, and option 30 prevents player-to-player trade.
                armor.itemOptions.add(new ItemOption(9, 1));
                // Bonus 10% maximum HP while the armor is equipped.
                armor.itemOptions.add(new ItemOption(77, 10));
                armor.itemOptions.add(new ItemOption(30, 1));
                ok = addBagOrBox(player, armor);
            }
            if (!ok) {
                return "Không đủ chỗ chứa phần thưởng. Hãy dọn hành trang/rương rồi nhận lại.";
            }

            player.inventory.addRuby(cfg.ruby);
            Service.gI().sendMoney(player);
            InventoryService.gI().sendItemBags(player);
            InventoryService.gI().sendItemBox(player);

            try (Connection saveCon = DBService.gI().getConnection()) {
                PlayerDAO.updatePlayer(player, saveCon);
            } catch (Exception e) {
                Log.error(TopRaceManager.class, e);
            }

            updateRewardStatus(pending.resultId, 1);
            claimed++;
            if (detail.length() > 0) {
                detail.append("\n");
            }
            detail.append(getRaceName(pending.type)).append(" Top ").append(pending.rank)
                    .append(": ").append(cfg.goldBar).append(" Thỏi Vàng + ")
                    .append(cfg.ruby).append(" Hồng Ngọc");
            if (awardTrainingArmor) {
                detail.append(" + Giáp tập luyện cấp 4");
            }
        }

        return claimed > 0
                ? "Nhận thưởng thành công:\n" + detail
                : "Bạn không có giải đủ điều kiện nhận thưởng.";
    }

    private boolean addBagOrBox(Player player, Item item) {
        if (InventoryService.gI().addItemBag(player, item, 0)) {
            return true;
        }
        return InventoryService.gI().addItemBox(player, item, 0);
    }

    private Item createRewardItem(int itemId, String optionsJson) {
        Item item = ItemService.gI().createNewItem((short) itemId, 1);
        if (optionsJson != null && !optionsJson.isBlank()) {
            JSONArray options = (JSONArray) JSONValue.parse(optionsJson);
            if (options != null) {
                for (Object raw : options) {
                    JSONArray opt = (JSONArray) raw;
                    item.itemOptions.add(new ItemOption(
                            Integer.parseInt(String.valueOf(opt.get(0))),
                            Integer.parseInt(String.valueOf(opt.get(1)))));
                }
            }
        }
        return item;
    }

    private void updateRewardStatus(long resultId, int status) {
        try (Connection con = DBService.gI().getConnection();
                PreparedStatement ps = con.prepareStatement(
                        "UPDATE top_race_result SET reward_status=?, claimed_at=IF(?=1,NOW(),claimed_at) WHERE id=?")) {
            ps.setInt(1, status);
            ps.setInt(2, status);
            ps.setLong(3, resultId);
            ps.executeUpdate();
        } catch (Exception e) {
            Log.error(TopRaceManager.class, e);
        }
    }

    public String getRaceName(String type) {
        switch (type) {
            case TASK:
                return "Nhiệm Vụ";
            case PET:
                return "Sức Mạnh Đệ Tử";
            case NAP:
                return "Nạp";
            case POWER:
            default:
                return "Sức Mạnh";
        }
    }

    private String formatTime(Timestamp time) {
        if (time == null) {
            return "?";
        }
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(time);
    }
}
