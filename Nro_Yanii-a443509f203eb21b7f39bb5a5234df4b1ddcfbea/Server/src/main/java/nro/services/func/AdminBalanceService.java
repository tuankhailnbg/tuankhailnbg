package nro.services.func;

import nro.jdbc.DBService;
import nro.models.player.Player;
import nro.server.Client;
import nro.services.Service;
import nro.utils.Log;
import nro.utils.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service quản lý cộng số dư nạp tiền và điều chỉnh số dư tài khoản
 * Đảm bảo:
 * - Tìm kiếm đúng account theo Tên nhân vật, Player ID hoặc Account ID
 * - Hỗ trợ cả người chơi Online và Offline
 * - Giao dịch Database an toàn (Transaction, FOR UPDATE, Rollback khi lỗi)
 * - Chống cộng trùng lặp tuyệt đối bằng Mã giao dịch duy nhất (Unique Transaction Code)
 * - Phân định rõ Chế độ A (Xác nhận nạp tiền: cộng số dư + cộng tổng nạp)
 *   và Chế độ B (Điều chỉnh số dư: chỉ cộng số dư, không cộng tổng nạp)
 * - Đồng bộ tức thời bộ nhớ RAM (Player.soDuVND, Session.vndBar, Session.tongnap) khi player Online
 * - Ghi nhật ký đầy đủ vào bảng admin_balance_credit và hệ thống Audit Log
 *
 * @author Antigravity
 */
public class AdminBalanceService {

    public static final String MODE_DEPOSIT = "DEPOSIT";
    public static final String MODE_ADJUST = "ADJUST";
    public static final String MODE_TOPUP = "DEPOSIT";

    private static AdminBalanceService instance;

    // Khóa đồng bộ theo accountId để tránh xung đột concurrent request trên cùng một tài khoản
    private final Map<Integer, Object> accountLocks = new ConcurrentHashMap<>();

    private AdminBalanceService() {
        initTable();
    }

    public static AdminBalanceService gI() {
        if (instance == null) {
            instance = new AdminBalanceService();
        }
        return instance;
    }

    private void initTable() {
        try (Connection con = DBService.gI().getConnectionForGame();
             Statement stmt = con.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `admin_balance_credit` ("
                    + "`id` BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "`trans_code` VARCHAR(64) NOT NULL, "
                    + "`account_id` INT NOT NULL, "
                    + "`player_id` BIGINT DEFAULT NULL, "
                    + "`player_name` VARCHAR(50) DEFAULT NULL, "
                    + "`admin_username` VARCHAR(50) NOT NULL, "
                    + "`balance_before` INT NOT NULL, "
                    + "`amount` INT NOT NULL, "
                    + "`balance_after` INT NOT NULL, "
                    + "`tongnap_before` INT NOT NULL, "
                    + "`tongnap_after` INT NOT NULL, "
                    + "`mode` VARCHAR(20) NOT NULL DEFAULT 'DEPOSIT', "
                    + "`note` VARCHAR(255) DEFAULT NULL, "
                    + "`created_at` DATETIME NOT NULL, "
                    + "`status` VARCHAR(20) NOT NULL DEFAULT 'SUCCESS', "
                    + "UNIQUE KEY `uk_trans_code` (`trans_code`), "
                    + "KEY `idx_account_id` (`account_id`), "
                    + "KEY `idx_player_id` (`player_id`), "
                    + "KEY `idx_created_at` (`created_at`)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
        } catch (Throwable e) {
            Log.error(AdminBalanceService.class.getName() + ": Không thể khởi tạo bảng admin_balance_credit: " + e.getMessage());
        }
    }

    /**
     * DTO chứa thông tin tài khoản tra cứu được
     */
    public static class AccountInfo {
        public int accountId;
        public String username;
        public Long playerId;
        public String playerName;
        public int currentVnd;
        public int currentTongnap;
        public boolean isOnline;
        public int gender = -1; // 0: Trái Đất, 1: Namếc, 2: Xayda
        public long power = 0;
    }

    /**
     * DTO kết quả thao tác cộng tiền
     */
    public static class CreditResult {
        public boolean success;
        public String message;
        public int accountId;
        public String username;
        public Long playerId;
        public String playerName;
        public int balanceBefore;
        public int amount;
        public int balanceAfter;
        public int tongnapBefore;
        public int tongnapAfter;
        public String mode; // DEPOSIT hoặc ADJUST
        public String transCode;
        public String note;
        public String timestamp;
        public boolean isDuplicate = false;
    }

    /**
     * DTO lịch sử giao dịch cộng tiền
     */
    public static class CreditHistoryEntry {
        public long id;
        public String transCode;
        public int accountId;
        public Long playerId;
        public String playerName;
        public String adminUsername;
        public int balanceBefore;
        public int amount;
        public int balanceAfter;
        public int tongnapBefore;
        public int tongnapAfter;
        public String mode;
        public String note;
        public String createdAt;
        public String status;
    }

    /**
     * Tra cứu thông tin tài khoản theo từ khóa (Tên nhân vật, Player ID hoặc Account ID / Username)
     */
    public AccountInfo lookupAccount(String query) {
        if (query == null || query.trim().isEmpty()) {
            return null;
        }
        query = query.trim();

        boolean forceAccount = false;
        boolean forcePlayer = false;
        if (query.toLowerCase().startsWith("acc:") || query.toLowerCase().startsWith("a:")) {
            forceAccount = true;
            query = query.substring(query.indexOf(':') + 1).trim();
        } else if (query.toLowerCase().startsWith("pl:") || query.toLowerCase().startsWith("p:")) {
            forcePlayer = true;
            query = query.substring(query.indexOf(':') + 1).trim();
        }

        // 1. Kiểm tra trong danh sách người chơi ONLINE trước (nếu không ép kiểu Account)
        if (!forceAccount) {
            Player targetOnline = Client.gI().getPlayer(query);
            if (targetOnline == null && isNumeric(query)) {
                try {
                    long searchId = Long.parseLong(query);
                    targetOnline = Client.gI().getPlayer(searchId);
                } catch (Exception ignored) {
                }
            }

            if (targetOnline != null && targetOnline.getSession() != null) {
                AccountInfo info = new AccountInfo();
                info.accountId = targetOnline.getSession().userId;
                info.username = "user_" + info.accountId;
                try (Connection con = DBService.gI().getConnectionForGame();
                     PreparedStatement ps = con.prepareStatement("SELECT username FROM account WHERE id = ?")) {
                    ps.setInt(1, info.accountId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            info.username = rs.getString("username");
                        }
                    }
                } catch (Exception ignored) {
                }
                info.playerId = targetOnline.id;
                info.playerName = targetOnline.name;
                info.currentVnd = targetOnline.soDuVND;
                info.currentTongnap = targetOnline.getSession().tongnap;
                info.isOnline = true;
                info.gender = targetOnline.gender;
                info.power = targetOnline.nPoint != null ? targetOnline.nPoint.power : 0;
                return info;
            }
        }

        // 2. Không online hoặc tìm theo Account: Tra cứu trong MariaDB (Offline)
        try (Connection con = DBService.gI().getConnectionForGame()) {
            boolean isNum = isNumeric(query);

            if (forceAccount) {
                return queryAccountDirect(con, query, isNum);
            }

            if (forcePlayer) {
                return queryPlayerDirect(con, query, isNum);
            }

            // Mặc định:
            // 2a. Nếu không phải số: Kiểm tra Player Name trước, nếu không có thì tìm theo Account Username
            if (!isNum) {
                AccountInfo plInfo = queryPlayerDirect(con, query, false);
                if (plInfo != null) return plInfo;
                return queryAccountDirect(con, query, false);
            }

            // 2b. Nếu là số: Kiểm tra Account ID trước, nếu không có thì tìm theo Player ID
            AccountInfo accInfo = queryAccountDirect(con, query, true);
            if (accInfo != null) return accInfo;
            return queryPlayerDirect(con, query, true);

        } catch (Exception e) {
            Log.error(AdminBalanceService.class, e, "Lỗi tra cứu tài khoản: " + query);
        }

        return null;
    }

    private AccountInfo queryAccountDirect(Connection con, String query, boolean isNum) throws SQLException {
        String sql = "SELECT a.id as a_id, a.username, a.vnd, a.tongnap, p.id as p_id, p.name as p_name, p.gender, p.power "
                + "FROM account a LEFT JOIN player p ON a.id = p.account_id "
                + "WHERE " + (isNum ? "a.id = ?" : "a.username = ?") + " LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            if (isNum) {
                ps.setInt(1, Integer.parseInt(query));
            } else {
                ps.setString(1, query);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    AccountInfo info = new AccountInfo();
                    info.accountId = rs.getInt("a_id");
                    info.username = rs.getString("username");
                    long pId = rs.getLong("p_id");
                    if (!rs.wasNull()) {
                        info.playerId = pId;
                        info.playerName = rs.getString("p_name");
                        info.gender = rs.getInt("gender");
                        info.power = rs.getLong("power");
                    }
                    info.currentVnd = rs.getInt("vnd");
                    info.currentTongnap = rs.getInt("tongnap");
                    info.isOnline = false;
                    return info;
                }
            }
        }
        return null;
    }

    private AccountInfo queryPlayerDirect(Connection con, String query, boolean isNum) throws SQLException {
        String sql = "SELECT p.id as p_id, p.name as p_name, p.gender, p.power, a.id as a_id, a.username, a.vnd, a.tongnap "
                + "FROM player p JOIN account a ON p.account_id = a.id "
                + "WHERE " + (isNum ? "p.id = ?" : "p.name = ?") + " LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            if (isNum) {
                ps.setLong(1, Long.parseLong(query));
            } else {
                ps.setString(1, query);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    AccountInfo info = new AccountInfo();
                    info.accountId = rs.getInt("a_id");
                    info.username = rs.getString("username");
                    info.playerId = rs.getLong("p_id");
                    info.playerName = rs.getString("p_name");
                    info.currentVnd = rs.getInt("vnd");
                    info.currentTongnap = rs.getInt("tongnap");
                    info.isOnline = false;
                    info.gender = rs.getInt("gender");
                    info.power = rs.getLong("power");
                    return info;
                }
            }
        }
        return null;
    }

    /**
     * Thực hiện cộng số dư nạp tiền hoặc điều chỉnh số dư cho tài khoản
     *
     * @param adminName    Tên admin thực hiện
     * @param queryTarget  Tên nhân vật, Player ID hoặc Account ID
     * @param amount       Số tiền VNĐ cần cộng (> 0)
     * @param transCode    Mã giao dịch duy nhất
     * @param note         Ghi chú / lý do
     * @param mode         "DEPOSIT" (Chế độ A: Nạp tiền) hoặc "ADJUST" (Chế độ B: Điều chỉnh)
     * @return CreditResult chi tiết
     */
    public CreditResult creditBalance(String adminName, String queryTarget, int amount, String transCode, String note, String mode) {
        CreditResult result = new CreditResult();
        result.transCode = transCode != null ? transCode.trim() : "";
        result.mode = (mode != null && mode.equalsIgnoreCase("ADJUST")) ? "ADJUST" : "DEPOSIT";
        result.note = note != null ? note.trim() : "";

        // 1. Kiểm tra số tiền hợp lệ
        if (amount <= 0 || amount > 2_000_000_000) {
            result.success = false;
            result.message = "Số tiền nạp không hợp lệ. Vui lòng nhập số nguyên dương từ 1 đến 2,000,000,000 VNĐ.";
            return result;
        }
        result.amount = amount;

        // 2. Kiểm tra mã giao dịch
        if (result.transCode.isEmpty()) {
            result.transCode = generateDefaultTransCode();
        }

        // 3. Tra cứu tài khoản đích
        AccountInfo target = lookupAccount(queryTarget);
        if (target == null) {
            result.success = false;
            result.message = "Không tìm thấy người chơi hoặc tài khoản phù hợp với từ khóa: " + queryTarget;
            return result;
        }

        result.accountId = target.accountId;
        result.username = target.username;
        result.playerId = target.playerId;
        result.playerName = target.playerName;

        // 4. Kiểm tra chống trùng lặp mã giao dịch (Idempotency check)
        CreditHistoryEntry existingTrans = getTransactionByCode(result.transCode);
        if (existingTrans != null) {
            result.success = false;
            result.isDuplicate = true;
            result.message = "Mã giao dịch '" + result.transCode + "' ĐÃ ĐƯỢC XỬ LÝ TRƯỚC ĐÓ vào lúc " + existingTrans.createdAt
                    + " (Số tiền: " + Util.numberToMoney(existingTrans.amount) + " VNĐ, Số dư sau: "
                    + Util.numberToMoney(existingTrans.balanceAfter) + " VNĐ). Không thể cộng tiền lần hai!";
            result.balanceBefore = existingTrans.balanceBefore;
            result.balanceAfter = existingTrans.balanceAfter;
            result.tongnapBefore = existingTrans.tongnapBefore;
            result.tongnapAfter = existingTrans.tongnapAfter;
            return result;
        }

        // 5. Khóa theo accountId để chống race condition khi nhiều request cùng nhắm vào một account
        Object lock = accountLocks.computeIfAbsent(target.accountId, k -> new Object());
        synchronized (lock) {
            // Kiểm tra lại lần nữa trong khối synchronized
            existingTrans = getTransactionByCode(result.transCode);
            if (existingTrans != null) {
                result.success = false;
                result.isDuplicate = true;
                result.message = "Mã giao dịch '" + result.transCode + "' đã tồn tại!";
                return result;
            }

            Connection con = null;
            try {
                con = DBService.gI().getConnectionForSaveData();
                con.setAutoCommit(false);

                // 5a. Khóa dòng tài khoản bằng SELECT ... FOR UPDATE
                int currentVnd = 0;
                int currentTongnap = 0;
                try (PreparedStatement psSelect = con.prepareStatement(
                        "SELECT id, vnd, tongnap FROM account WHERE id = ? FOR UPDATE")) {
                    psSelect.setInt(1, target.accountId);
                    try (ResultSet rs = psSelect.executeQuery()) {
                        if (rs.next()) {
                            currentVnd = rs.getInt("vnd");
                            currentTongnap = rs.getInt("tongnap");
                        } else {
                            con.rollback();
                            result.success = false;
                            result.message = "Tài khoản ID " + target.accountId + " không tồn tại trong hệ thống CSDL!";
                            return result;
                        }
                    }
                }

                // Kiểm tra tràn số
                if ((long) currentVnd + amount > Integer.MAX_VALUE) {
                    con.rollback();
                    result.success = false;
                    result.message = "Số dư sau khi cộng sẽ vượt quá giới hạn tối đa của hệ thống (2.14 tỷ VNĐ)!";
                    return result;
                }

                int newVnd = currentVnd + amount;
                int newTongnap = currentTongnap;

                // 5b. Xử lý theo chế độ
                if ("DEPOSIT".equals(result.mode)) {
                    // Chế độ A: Xác nhận nạp tiền -> Tăng cả vnd và tongnap
                    newTongnap = currentTongnap + amount;
                    try (PreparedStatement psUpAcc = con.prepareStatement(
                            "UPDATE account SET vnd = ?, tongnap = ? WHERE id = ?")) {
                        psUpAcc.setInt(1, newVnd);
                        psUpAcc.setInt(2, newTongnap);
                        psUpAcc.setInt(3, target.accountId);
                        psUpAcc.executeUpdate();
                    }

                    // Đồng bộ trường tongnap trong bảng player nếu có
                    try (PreparedStatement psUpPlayer = con.prepareStatement(
                            "UPDATE player SET tongnap = ? WHERE account_id = ?")) {
                        psUpPlayer.setInt(1, newTongnap);
                        psUpPlayer.setInt(2, target.accountId);
                        psUpPlayer.executeUpdate();
                    }
                } else {
                    // Chế độ B: Điều chỉnh số dư -> Chỉ cập nhật vnd
                    try (PreparedStatement psUpAcc = con.prepareStatement(
                            "UPDATE account SET vnd = ? WHERE id = ?")) {
                        psUpAcc.setInt(1, newVnd);
                        psUpAcc.setInt(2, target.accountId);
                        psUpAcc.executeUpdate();
                    }
                }

                // 5c. Ghi lịch sử giao dịch vào admin_balance_credit
                String timeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                try (PreparedStatement psInsert = con.prepareStatement(
                        "INSERT INTO admin_balance_credit (trans_code, account_id, player_id, player_name, "
                                + "admin_username, balance_before, amount, balance_after, tongnap_before, tongnap_after, "
                                + "mode, note, created_at, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), 'SUCCESS')")) {
                    psInsert.setString(1, result.transCode);
                    psInsert.setInt(2, target.accountId);
                    if (target.playerId != null) {
                        psInsert.setLong(3, target.playerId);
                    } else {
                        psInsert.setNull(3, java.sql.Types.BIGINT);
                    }
                    psInsert.setString(4, target.playerName);
                    psInsert.setString(5, adminName != null ? adminName : "ADMIN");
                    psInsert.setInt(6, currentVnd);
                    psInsert.setInt(7, amount);
                    psInsert.setInt(8, newVnd);
                    psInsert.setInt(9, currentTongnap);
                    psInsert.setInt(10, newTongnap);
                    psInsert.setString(11, result.mode);
                    psInsert.setString(12, result.note);
                    psInsert.executeUpdate();
                }

                // 5d. Commit toàn bộ giao dịch CSDL
                con.commit();

                result.success = true;
                result.balanceBefore = currentVnd;
                result.balanceAfter = newVnd;
                result.tongnapBefore = currentTongnap;
                result.tongnapAfter = newTongnap;
                result.timestamp = timeStr;
                result.message = "Cộng thành công " + Util.numberToMoney(amount) + " VNĐ cho tài khoản "
                        + target.username + " (" + (target.playerName != null ? target.playerName : "N/A") + ")!";

                // 6. ĐỒNG BỘ DỮ LIỆU RUNTIME TRONG RAM KHI NGƯỜI CHƠI ONLINE
                syncOnlinePlayer(target, newVnd, newTongnap, amount, result.mode);

                // 7. Ghi Audit Log
                logAuditCredit(adminName, target, result.transCode, amount, currentVnd, newVnd, currentTongnap, newTongnap, result.mode, result.note);

            } catch (Exception e) {
                if (con != null) {
                    try {
                        con.rollback();
                    } catch (SQLException ignored) {
                    }
                }
                Log.error(AdminBalanceService.class, e, "Lỗi thực hiện transaction cộng số dư: " + result.transCode);
                result.success = false;
                result.message = "Lỗi Database trong quá trình cộng tiền: " + e.getMessage();
            } finally {
                if (con != null) {
                    try {
                        con.setAutoCommit(true);
                        con.close();
                    } catch (SQLException ignored) {
                    }
                }
            }
        }

        return result;
    }

    /**
     * Đồng bộ số dư trong RAM cho người chơi đang ONLINE
     */
    private void syncOnlinePlayer(AccountInfo target, int newVnd, int newTongnap, int amount, String mode) {
        Player onlinePl = null;
        if (target.playerId != null) {
            onlinePl = Client.gI().getPlayer(target.playerId);
        }
        if (onlinePl == null && target.playerName != null) {
            onlinePl = Client.gI().getPlayer(target.playerName);
        }

        if (onlinePl != null) {
            synchronized (onlinePl) {
                onlinePl.soDuVND = newVnd;
                if (onlinePl.getSession() != null) {
                    onlinePl.getSession().vndBar = newVnd;
                    onlinePl.getSession().vnd = newVnd;
                    if ("DEPOSIT".equals(mode)) {
                        onlinePl.getSession().tongnap = newTongnap;
                    }
                }
            }

            String actionStr = "DEPOSIT".equals(mode) ? "nạp" : "cộng điều chỉnh";
            Service.getInstance().sendThongBao(onlinePl,
                    "|1|Tài khoản của bạn vừa được " + actionStr + " " + Util.numberToMoney(amount)
                            + " VNĐ.\n|2|Số dư hiện tại: " + Util.numberToMoney(newVnd) + " VNĐ.");
        }
    }

    /**
     * Tra cứu giao dịch theo mã để chống cộng trùng
     */
    public CreditHistoryEntry getTransactionByCode(String transCode) {
        if (transCode == null || transCode.trim().isEmpty()) return null;
        try (Connection con = DBService.gI().getConnectionForGame();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id, trans_code, account_id, player_id, player_name, admin_username, "
                             + "balance_before, amount, balance_after, tongnap_before, tongnap_after, "
                             + "mode, note, created_at, status FROM admin_balance_credit WHERE trans_code = ? LIMIT 1")) {
            ps.setString(1, transCode.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CreditHistoryEntry e = new CreditHistoryEntry();
                    e.id = rs.getLong("id");
                    e.transCode = rs.getString("trans_code");
                    e.accountId = rs.getInt("account_id");
                    e.playerId = rs.getLong("player_id");
                    if (rs.wasNull()) e.playerId = null;
                    e.playerName = rs.getString("player_name");
                    e.adminUsername = rs.getString("admin_username");
                    e.balanceBefore = rs.getInt("balance_before");
                    e.amount = rs.getInt("amount");
                    e.balanceAfter = rs.getInt("balance_after");
                    e.tongnapBefore = rs.getInt("tongnap_before");
                    e.tongnapAfter = rs.getInt("tongnap_after");
                    e.mode = rs.getString("mode");
                    e.note = rs.getString("note");
                    e.createdAt = rs.getString("created_at");
                    e.status = rs.getString("status");
                    return e;
                }
            }
        } catch (Exception e) {
            Log.error(AdminBalanceService.class, e, "Lỗi kiểm tra mã giao dịch: " + transCode);
        }
        return null;
    }

    /**
     * Lấy danh sách lịch sử cộng số dư
     */
    public List<CreditHistoryEntry> getTransactionHistory(int limit, String search) {
        List<CreditHistoryEntry> list = new ArrayList<>();
        if (limit <= 0 || limit > 500) limit = 100;

        StringBuilder sql = new StringBuilder("SELECT id, trans_code, account_id, player_id, player_name, "
                + "admin_username, balance_before, amount, balance_after, tongnap_before, tongnap_after, "
                + "mode, note, created_at, status FROM admin_balance_credit ");

        boolean hasSearch = (search != null && !search.trim().isEmpty());
        if (hasSearch) {
            sql.append("WHERE trans_code LIKE ? OR player_name LIKE ? OR admin_username LIKE ? ");
            if (isNumeric(search.trim())) {
                sql.append("OR account_id = ? OR player_id = ? ");
            }
        }
        sql.append("ORDER BY id DESC LIMIT ?");

        try (Connection con = DBService.gI().getConnectionForGame();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int idx = 1;
            if (hasSearch) {
                String pattern = "%" + search.trim() + "%";
                ps.setString(idx++, pattern);
                ps.setString(idx++, pattern);
                ps.setString(idx++, pattern);
                if (isNumeric(search.trim())) {
                    long val = Long.parseLong(search.trim());
                    ps.setLong(idx++, val);
                    ps.setLong(idx++, val);
                }
            }
            ps.setInt(idx, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CreditHistoryEntry e = new CreditHistoryEntry();
                    e.id = rs.getLong("id");
                    e.transCode = rs.getString("trans_code");
                    e.accountId = rs.getInt("account_id");
                    e.playerId = rs.getLong("player_id");
                    if (rs.wasNull()) e.playerId = null;
                    e.playerName = rs.getString("player_name");
                    e.adminUsername = rs.getString("admin_username");
                    e.balanceBefore = rs.getInt("balance_before");
                    e.amount = rs.getInt("amount");
                    e.balanceAfter = rs.getInt("balance_after");
                    e.tongnapBefore = rs.getInt("tongnap_before");
                    e.tongnapAfter = rs.getInt("tongnap_after");
                    e.mode = rs.getString("mode");
                    e.note = rs.getString("note");
                    e.createdAt = rs.getString("created_at");
                    e.status = rs.getString("status");
                    list.add(e);
                }
            }
        } catch (Exception e) {
            Log.error(AdminBalanceService.class, e, "Lỗi lấy lịch sử cộng tiền");
        }
        return list;
    }

    /**
     * Sinh mã giao dịch chuẩn mặc định
     */
    public String generateDefaultTransCode() {
        String datePart = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        int randomPart = (int) (Math.random() * 9000) + 1000;
        return "NAP" + datePart + "-" + randomPart;
    }

    private void logAuditCredit(String adminName, AccountInfo target, String transCode, int amount,
                                int balBefore, int balAfter, int tnBefore, int tnAfter, String mode, String note) {
        String timeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logMsg = String.format("[ADMIN_BALANCE_CREDIT] TransCode=%s Admin=%s Account=%s(%d) Player=%s(%s) Mode=%s Amount=%d Balance=%d->%d TongNap=%d->%d Note='%s' Time=%s",
                transCode, adminName, target.username, target.accountId,
                target.playerName != null ? target.playerName : "N/A",
                target.playerId != null ? String.valueOf(target.playerId) : "N/A",
                mode, amount, balBefore, balAfter, tnBefore, tnAfter, note, timeStr);
        Log.success(logMsg);
        System.out.println(logMsg);
    }

    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) return false;
        return str.matches("\\d+");
    }
}
