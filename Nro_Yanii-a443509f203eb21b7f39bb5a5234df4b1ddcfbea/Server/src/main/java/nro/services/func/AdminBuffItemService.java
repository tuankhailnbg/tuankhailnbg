package nro.services.func;

import nro.consts.ConstNpc;
import nro.jdbc.DBService;
import nro.jdbc.daos.PlayerDAO;
import nro.models.item.Item;
import nro.models.item.ItemOption;
import nro.models.item.ItemOptionTemplate;
import nro.models.item.ItemTemplate;
import nro.models.npc.specialnpc.MabuEgg;
import nro.models.player.Player;
import nro.server.Client;
import nro.server.Manager;
import nro.services.InventoryService;
import nro.services.ItemMapService;
import nro.services.ItemService;
import nro.services.NpcService;
import nro.services.PlayerService;
import nro.services.RewardService;
import nro.services.Service;
import nro.utils.Log;
import nro.utils.Util;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import nro.services.func.webadmin.AdminWebAuth;
import nro.services.func.webadmin.AdminWebServer;
import nro.services.func.AdminBalanceService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * Service quản lý chức năng Admin Buff Vật Phẩm hoàn chỉnh
 * Hỗ trợ tìm kiếm cả Online và Offline, chọn item, chọn option và param,
 * xem trước (preview + JSON), chống duplicate option, chống double buff, audit log.
 *
 * @author Antigravity
 */
public class AdminBuffItemService {

    private static AdminBuffItemService instance;

    // Lưu phiên làm việc của từng Admin theo Admin Player ID
    private final Map<Long, BuffSession> sessions = new ConcurrentHashMap<>();

    // Set để chống xử lý đồng thời deliverPendingGrants cho cùng một Player ID
    private final Set<Long> deliveringPlayers = ConcurrentHashMap.newKeySet();

    public static class PendingCredit {
        public String recipient;
        public int amount;
        public String transCode;
        public String note;
        public AdminBalanceService.AccountInfo acc;
    }

    private final Map<Long, PendingCredit> pendingCredits = new ConcurrentHashMap<>();

    private AdminBuffItemService() {
        initTable();
    }

    private void initTable() {
        try (Connection con = DBService.gI().getConnectionForGame();
             Statement stmt = con.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `admin_item_grant` ("
                    + "`id` BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "`target_player_id` BIGINT NOT NULL, "
                    + "`admin_player_id` BIGINT DEFAULT NULL, "
                    + "`item_template_id` INT NOT NULL, "
                    + "`quantity` INT NOT NULL, "
                    + "`options_json` TEXT, "
                    + "`status` VARCHAR(20) NOT NULL DEFAULT 'PENDING', "
                    + "`created_at` DATETIME NOT NULL, "
                    + "`delivered_at` DATETIME DEFAULT NULL, "
                    + "KEY `idx_target_player_status` (`target_player_id`, `status`), "
                    + "KEY `idx_status` (`status`)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

            // Khôi phục các grant bị kẹt PROCESSING từ phiên bản trước do server tắt đột ngột
            stmt.executeUpdate("UPDATE `admin_item_grant` SET `status` = 'PENDING' WHERE `status` = 'PROCESSING'");
        } catch (Throwable e) {
            Log.error(AdminBuffItemService.class.getName() + ": Không thể khởi tạo bảng admin_item_grant: " + e.getMessage());
        }
    }

    public static AdminBuffItemService gI() {
        if (instance == null) {
            instance = new AdminBuffItemService();
        }
        return instance;
    }

    /**
     * Session lưu trữ trạng thái của Admin trong quá trình tạo và buff vật phẩm
     */
    public static class BuffSession {
        public long adminId;
        public String adminName;

        // Thông tin mục tiêu
        public long targetPlayerId;
        public String targetPlayerName;
        public boolean targetIsOnline;
        public int targetGender; // 0: Trái Đất, 1: Namếc, 2: Xayda
        public long targetPower;

        // Thông tin vật phẩm
        public ItemTemplate selectedTemplate;
        public int quantity = 1;
        public boolean keepDefaultOptions = true;

        // Custom options: LinkedHashMap để giữ thứ tự và không bị duplicate Option ID
        public Map<Integer, Integer> customOptions = new LinkedHashMap<>();

        // Danh sách kết quả tìm kiếm khi tìm theo tên
        public List<ItemTemplate> searchItemResults = new ArrayList<>();
        public int searchItemPage = 0;

        // Kết quả tìm kiếm option theo tên
        public List<ItemOptionTemplate> searchOptionResults = new ArrayList<>();
        public int searchOptionPage = 0;

        // Option đang chờ nhập param
        public int pendingOptionId = -1;

        // Chống double click / spam
        public AtomicBoolean isProcessing = new AtomicBoolean(false);
        public long lastActionTime = 0;

        public boolean startProcessing() {
            long now = System.currentTimeMillis();
            if (now - lastActionTime < 1000) {
                return false;
            }
            lastActionTime = now;
            return isProcessing.compareAndSet(false, true);
        }

        public void finishProcessing() {
            isProcessing.set(false);
        }
    }

    public BuffSession getSession(Player admin) {
        if (admin == null) return null;
        return sessions.get(admin.id);
    }

    public BuffSession getOrCreateSession(Player admin) {
        if (admin == null) return null;
        return sessions.computeIfAbsent(admin.id, k -> {
            BuffSession s = new BuffSession();
            s.adminId = admin.id;
            s.adminName = admin.name;
            return s;
        });
    }

    public void removeSession(Player admin) {
        if (admin != null) {
            sessions.remove(admin.id);
        }
    }

    /**
     * Bắt đầu luồng: mở bảng nhập vật phẩm (Bước 1: ID vật phẩm, Số lượng, Người nhận)
     */
    public void openMenuBuffItem(Player admin) {
        if (admin == null || admin.getSession() == null || !admin.isAdmin()) {
            if (admin != null) {
                Service.getInstance().sendThongBao(admin, "Bạn không có quyền thực hiện chức năng này!");
            }
            return;
        }
        Input.gI().createFormBuffStep1(admin);
    }

    /**
     * Mở form cộng số dư nạp tiền cho người chơi
     */
    public void openMenuCreditBalance(Player admin) {
        if (admin == null || admin.getSession() == null || !admin.isAdmin()) {
            if (admin != null) {
                Service.getInstance().sendThongBao(admin, "Bạn không có quyền thực hiện chức năng này!");
            }
            return;
        }
        Input.gI().createFormAdminCredit(admin);
    }

    /**
     * Gửi liên kết truy cập Web Admin Checkbox & Param trực quan cho Admin
     */
    public void sendWebAdminLink(Player admin) {
        if (admin == null || admin.getSession() == null || !admin.isAdmin()) {
            if (admin != null) {
                Service.getInstance().sendThongBao(admin, "Bạn không có quyền thực hiện chức năng này!");
            }
            return;
        }
        String token = nro.services.func.webadmin.AdminWebAuth.gI().createOneTimeLoginToken(admin);
        if (token == null) {
            Service.getInstance().sendThongBao(admin, "Không thể tạo liên kết Web Admin!");
            return;
        }
        String link = "http://localhost:8888/auth-token?token=" + token;
        Service.getInstance().sendThongBao(admin, "Đã tạo link Web Admin (hạn 5 phút, dùng 1 lần):\n" + link);
        NpcService.gI().createMenuConMeo(admin, ConstNpc.IGNORE_MENU, 17997,
                "|7|-----WEB ADMIN BUFF ĐỒ TRỰC QUAN-----\n"
                        + "|1|Truy cập bảng Checkbox & Param qua trình duyệt:\n"
                        + "|2|" + link + "\n"
                        + "|8|Link dùng 1 lần và có hiệu lực trong 5 phút.\n"
                        + "|6|Mở trình duyệt trên máy chạy server để thao tác trực quan!",
                "Đã hiểu");
    }

    /**
     * Xử lý dữ liệu từ Form Bước 1 (ID vật phẩm, Số lượng, Người nhận)
     */
    public void handleStep1Input(Player admin, int itemId, int quantity, String recipientQuery) {
        if (admin == null || admin.getSession() == null || !admin.isAdmin()) return;

        ItemTemplate template = getItemTemplate(itemId);
        if (template == null || template.name == null || template.name.contains("[Vô hiệu hóa]")) {
            Service.getInstance().sendThongBao(admin, "Vật phẩm ID " + itemId + " không tồn tại hoặc đã bị vô hiệu hóa!");
            return;
        }

        if (quantity <= 0 || quantity > 2_000_000_000) {
            Service.getInstance().sendThongBao(admin, "Số lượng không hợp lệ (nhập từ 1 đến 2,000,000,000)!");
            return;
        }

        if (recipientQuery == null || recipientQuery.trim().isEmpty()) {
            Service.getInstance().sendThongBao(admin, "Vui lòng nhập tên hoặc ID người nhận!");
            return;
        }

        AdminBalanceService.AccountInfo acc = AdminBalanceService.gI().lookupAccount(recipientQuery.trim());
        if (acc == null) {
            Service.getInstance().sendThongBao(admin, "Không tìm thấy người nhận: " + recipientQuery);
            return;
        }

        BuffSession session = getOrCreateSession(admin);
        session.customOptions.clear();
        session.selectedTemplate = template;
        session.quantity = quantity;
        session.keepDefaultOptions = true;
        session.targetPlayerId = acc.playerId != null ? acc.playerId : 0;
        session.targetPlayerName = acc.playerName != null ? acc.playerName : acc.username;
        session.targetIsOnline = acc.isOnline;
        session.targetGender = acc.gender;
        session.targetPower = acc.power;

        showMenuOptions(admin);
    }

    /**
     * Xử lý khi Admin nhập Form Cộng số dư nạp tiền
     */
    public void handleAdminCreditFormInput(Player admin, String recipient, int amount, String transCode, String note) {
        if (admin == null || admin.getSession() == null || !admin.isAdmin()) return;

        if (amount <= 0 || amount > 2_000_000_000) {
            Service.getInstance().sendThongBao(admin, "Số tiền nạp phải là số nguyên dương từ 1 đến 2,000,000,000 VNĐ!");
            return;
        }

        AdminBalanceService.AccountInfo acc = AdminBalanceService.gI().lookupAccount(recipient);
        if (acc == null) {
            Service.getInstance().sendThongBao(admin, "Không tìm thấy tài khoản hoặc người chơi: " + recipient);
            return;
        }

        if (transCode == null || transCode.trim().isEmpty()) {
            transCode = AdminBalanceService.gI().generateDefaultTransCode();
        }

        PendingCredit pc = new PendingCredit();
        pc.recipient = recipient;
        pc.amount = amount;
        pc.transCode = transCode.trim();
        pc.note = note != null ? note.trim() : "";
        pc.acc = acc;
        pendingCredits.put(admin.id, pc);

        String info = "|7|----- XÁC NHẬN CỘNG SỐ DƯ -----\n"
                + "|0|Tài khoản: |2|" + acc.username + " (ID: " + acc.accountId + ")\n"
                + "|0|Nhân vật: |2|" + (acc.playerName != null ? acc.playerName : "N/A") + "\n"
                + "|0|Trạng thái: " + (acc.isOnline ? "|1|ONLINE" : "|7|OFFLINE") + "\n"
                + "|0|Số dư hiện tại: |2|" + Util.numberToMoney(acc.currentVnd) + " VNĐ\n"
                + "|0|Tổng nạp hiện tại: |2|" + Util.numberToMoney(acc.currentTongnap) + " VNĐ\n"
                + "|0|Số tiền cộng: |1|+" + Util.numberToMoney(amount) + " VNĐ\n"
                + "|0|Mã giao dịch: |6|" + pc.transCode + "\n"
                + "|0|Ghi chú: |5|" + (pc.note.isEmpty() ? "Không có" : pc.note);

        NpcService.gI().createMenuConMeo(admin, ConstNpc.ADMIN_CONFIRM_CREDIT, 17997,
                info,
                "Xác nhận cộng", "Hủy");
    }

    /**
     * Xử lý xác nhận cộng số dư từ menu
     */
    public void handleConfirmCreditMenu(Player admin, int select) {
        if (admin == null || !admin.isAdmin()) return;
        PendingCredit pc = pendingCredits.remove(admin.id);
        if (pc == null) {
            Service.getInstance().sendThongBao(admin, "Phiên giao dịch đã hết hạn!");
            return;
        }

        if (select == 0) {
            // Chế độ A: Xác nhận nạp tiền
            AdminBalanceService.CreditResult res = AdminBalanceService.gI().creditBalance(
                    admin.name, pc.recipient, pc.amount, pc.transCode, pc.note, "DEPOSIT");
            if (res.success) {
                Service.getInstance().sendThongBao(admin, res.message);
            } else {
                Service.getInstance().sendThongBao(admin, "|7|[THẤT BẠI] " + res.message);
            }
        } else {
            Service.getInstance().sendThongBao(admin, "Đã hủy thao tác cộng số dư.");
        }
    }

    /**
     * Bắt đầu buff cho một người chơi đã biết (ví dụ từ menu tìm kiếm người chơi)
     */
    public void startBuffForPlayer(Player admin, Player target) {
        if (admin == null || admin.getSession() == null || !admin.isAdmin()) {
            return;
        }
        if (target == null) {
            Service.getInstance().sendThongBao(admin, "Không tìm thấy nhân vật.");
            return;
        }
        BuffSession session = getOrCreateSession(admin);
        session.targetPlayerId = target.id;
        session.targetPlayerName = target.name;
        session.targetIsOnline = true;
        session.targetGender = target.gender;
        session.targetPower = target.nPoint != null ? target.nPoint.power : 0;
        showTargetPlayerInfo(admin, session);
    }

    /**
     * Xử lý tìm kiếm người chơi (Online hoặc Offline)
     */
    public void searchPlayer(Player admin, String query) {
        if (admin == null || admin.getSession() == null || !admin.isAdmin()) {
            return;
        }
        if (query == null || query.trim().isEmpty()) {
            Service.getInstance().sendThongBao(admin, "Vui lòng nhập tên hoặc ID nhân vật.");
            return;
        }
        query = query.trim();

        BuffSession session = getOrCreateSession(admin);
        session.customOptions.clear();
        session.selectedTemplate = null;
        session.quantity = 1;
        session.keepDefaultOptions = true;

        // 1. Kiểm tra Online trước
        Player targetOnline = Client.gI().getPlayer(query);
        if (targetOnline == null && isNumeric(query)) {
            try {
                long searchId = Long.parseLong(query);
                targetOnline = Client.gI().getPlayer(searchId);
            } catch (Exception ignored) {
            }
        }

        if (targetOnline != null) {
            session.targetPlayerId = targetOnline.id;
            session.targetPlayerName = targetOnline.name;
            session.targetIsOnline = true;
            session.targetGender = targetOnline.gender;
            session.targetPower = targetOnline.nPoint != null ? targetOnline.nPoint.power : 0;
            showTargetPlayerInfo(admin, session);
            return;
        }

        // 2. Không online -> Tìm trong Database (Offline)
        try (Connection con = DBService.gI().getConnectionForGame()) {
            String sql;
            boolean isNum = isNumeric(query);
            if (isNum) {
                sql = "SELECT id, name, gender, power FROM player WHERE name = ? OR id = ? LIMIT 1";
            } else {
                sql = "SELECT id, name, gender, power FROM player WHERE name = ? LIMIT 1";
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, query);
                if (isNum) {
                    ps.setLong(2, Long.parseLong(query));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        session.targetPlayerId = rs.getLong("id");
                        session.targetPlayerName = rs.getString("name");
                        session.targetIsOnline = false;
                        session.targetGender = rs.getInt("gender");
                        session.targetPower = rs.getLong("power");
                        showTargetPlayerInfo(admin, session);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            Log.error(AdminBuffItemService.class, e, "Lỗi tìm kiếm player offline: " + query);
        }

        // Không tìm thấy
        Service.getInstance().sendThongBao(admin, "Không tìm thấy nhân vật.");
    }

    /**
     * Hiển thị thông tin người chơi tìm được
     */
    private void showTargetPlayerInfo(Player admin, BuffSession session) {
        String genderStr = session.targetGender == 0 ? "Trái Đất" : (session.targetGender == 1 ? "Namếc" : "Xayda");
        String statusStr = session.targetIsOnline ? "|1|ONLINE" : "|7|OFFLINE";
        String info = "|7|----- THÔNG TIN NGƯỜI CHƠI -----\n"
                + "|0|Tên: |2|" + session.targetPlayerName + "\n"
                + "|0|ID Player: |2|" + session.targetPlayerId + "\n"
                + "|0|Trạng thái: " + statusStr + "\n"
                + "|0|Hành tinh: |2|" + genderStr + "\n"
                + "|0|Sức mạnh: |2|" + Util.numberToMoney(session.targetPower) + "\n"
                + "|7|---------------------------------";

        NpcService.gI().createMenuConMeo(admin, ConstNpc.BUFF_ITEM_TARGET, 17997,
                info,
                "Chọn\nvật phẩm", "Tìm người\nkhác", "Đóng");
    }

    /**
     * Mở form tìm kiếm vật phẩm
     */
    public void openSearchItemForm(Player admin) {
        if (admin == null || admin.getSession() == null || !admin.isAdmin()) return;
        BuffSession session = getSession(admin);
        if (session == null) {
            openMenuBuffItem(admin);
            return;
        }
        Input.gI().createFormBuffSearchItem(admin);
    }

    /**
     * Xử lý tìm kiếm vật phẩm theo ID hoặc Tên
     */
    public void searchItem(Player admin, String query) {
        if (admin == null || admin.getSession() == null || !admin.isAdmin()) return;
        BuffSession session = getSession(admin);
        if (session == null) {
            openMenuBuffItem(admin);
            return;
        }
        if (query == null || query.trim().isEmpty()) {
            Service.getInstance().sendThongBao(admin, "Vui lòng nhập ID hoặc tên vật phẩm.");
            return;
        }
        query = query.trim();

        // 1. Tìm theo ID
        if (isNumeric(query)) {
            try {
                int id = Integer.parseInt(query);
                ItemTemplate it = getItemTemplate(id);
                if (it != null) {
                    selectItem(admin, session, it);
                    return;
                }
            } catch (Exception ignored) {
            }
        }

        // 2. Tìm theo tên
        String cleanQuery = stripDiacritics(query).toLowerCase();
        List<ItemTemplate> matches = new ArrayList<>();
        for (ItemTemplate it : Manager.ITEM_TEMPLATES) {
            if (it == null || it.name == null) continue;
            String cleanName = stripDiacritics(it.name).toLowerCase();
            if (cleanName.contains(cleanQuery) || it.name.toLowerCase().contains(query.toLowerCase())) {
                matches.add(it);
            }
        }

        if (matches.isEmpty()) {
            Service.getInstance().sendThongBao(admin, "Không tìm thấy vật phẩm nào phù hợp với từ khóa: " + query);
            return;
        }

        if (matches.size() == 1) {
            selectItem(admin, session, matches.get(0));
            return;
        }

        // Nhiều kết quả -> mở danh sách phân trang
        session.searchItemResults = matches;
        session.searchItemPage = 0;
        showItemSearchResults(admin, session);
    }

    /**
     * Hiển thị danh sách kết quả tìm kiếm vật phẩm (phân trang)
     */
    public void showItemSearchResults(Player admin, BuffSession session) {
        if (session.searchItemResults == null || session.searchItemResults.isEmpty()) {
            openSearchItemForm(admin);
            return;
        }
        int pageSize = 4;
        int totalItems = session.searchItemResults.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        if (session.searchItemPage >= totalPages) session.searchItemPage = totalPages - 1;
        if (session.searchItemPage < 0) session.searchItemPage = 0;

        int start = session.searchItemPage * pageSize;
        int end = Math.min(start + pageSize, totalItems);

        List<String> menuOptions = new ArrayList<>();
        for (int i = start; i < end; i++) {
            ItemTemplate it = session.searchItemResults.get(i);
            menuOptions.add(it.id + " - " + it.name);
        }

        if (session.searchItemPage < totalPages - 1) {
            menuOptions.add("Trang sau");
        }
        if (session.searchItemPage > 0) {
            menuOptions.add("Trang trước");
        }
        menuOptions.add("Tìm lại");
        menuOptions.add("Đóng");

        String info = "|7|--- DANH SÁCH VẬT PHẨM (" + (session.searchItemPage + 1) + "/" + totalPages + ") ---\n"
                + "|0|Tìm thấy |2|" + totalItems + "|0| vật phẩm phù hợp.\n"
                + "|2|Vui lòng chọn vật phẩm muốn buff:";

        NpcService.gI().createMenuConMeo(admin, ConstNpc.BUFF_ITEM_SELECT, 17997,
                info, menuOptions.toArray(new String[0]));
    }

    /**
     * Xử lý chọn item từ menu danh sách phân trang
     */
    public void handleSelectItemMenu(Player admin, int select) {
        BuffSession session = getSession(admin);
        if (session == null || session.searchItemResults == null) return;

        int pageSize = 4;
        int totalItems = session.searchItemResults.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        int start = session.searchItemPage * pageSize;
        int countInPage = Math.min(pageSize, totalItems - start);

        if (select < countInPage) {
            ItemTemplate selected = session.searchItemResults.get(start + select);
            selectItem(admin, session, selected);
            return;
        }

        int nextIdx = countInPage;
        boolean hasNext = session.searchItemPage < totalPages - 1;
        if (hasNext && select == nextIdx) {
            session.searchItemPage++;
            showItemSearchResults(admin, session);
            return;
        }
        if (hasNext) nextIdx++;

        boolean hasPrev = session.searchItemPage > 0;
        if (hasPrev && select == nextIdx) {
            session.searchItemPage--;
            showItemSearchResults(admin, session);
            return;
        }
        if (hasPrev) nextIdx++;

        if (select == nextIdx) {
            // Tìm lại
            openSearchItemForm(admin);
            return;
        }
        // Đóng
    }

    /**
     * Chọn vật phẩm thành công -> chuyển sang nhập số lượng
     */
    public void selectItem(Player admin, BuffSession session, ItemTemplate template) {
        session.selectedTemplate = template;
        Input.gI().createFormBuffQuantity(admin, template.id, template.name);
    }

    /**
     * Cập nhật số lượng vật phẩm
     */
    public void setQuantity(Player admin, String qtyStr) {
        BuffSession session = getSession(admin);
        if (session == null) return;
        try {
            long qty = Long.parseLong(qtyStr.trim());
            if (qty <= 0 || qty > 2_000_000_000L) {
                Service.getInstance().sendThongBao(admin, "Số lượng không hợp lệ. Vui lòng nhập từ 1 đến 2,000,000,000.");
                Input.gI().createFormBuffQuantity(admin, session.selectedTemplate.id, session.selectedTemplate.name);
                return;
            }
            session.quantity = (int) qty;
            showMenuOptions(admin);
        } catch (Exception e) {
            Service.getInstance().sendThongBao(admin, "Số lượng vượt quá giới hạn cho phép hoặc không đúng định dạng.");
            Input.gI().createFormBuffQuantity(admin, session.selectedTemplate.id, session.selectedTemplate.name);
        }
    }

    /**
     * Hiển thị menu cấu hình Option của vật phẩm
     */
    public void showMenuOptions(Player admin) {
        BuffSession session = getSession(admin);
        if (session == null || session.selectedTemplate == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append("|7|----- CẤU HÌNH OPTION -----\n");
        sb.append("|0|Người nhận: |2|").append(session.targetPlayerName)
                .append(session.targetIsOnline ? " (ONLINE)" : " (OFFLINE)").append("\n");
        sb.append("|0|Vật phẩm: |2|").append(session.selectedTemplate.name)
                .append(" (ID: ").append(session.selectedTemplate.id).append(")\n");
        sb.append("|0|Số lượng: |2|").append(session.quantity).append("\n");
        sb.append("|0|Giữ option gốc: ").append(session.keepDefaultOptions ? "|1|BẬT" : "|7|TẮT").append("\n");
        sb.append("|8|Các option đã chọn (").append(session.customOptions.size()).append("):\n");

        if (session.customOptions.isEmpty()) {
            sb.append("|7|(Chưa thêm option tùy chỉnh nào)\n");
        } else {
            for (Map.Entry<Integer, Integer> entry : session.customOptions.entrySet()) {
                int optId = entry.getKey();
                int param = entry.getValue();
                ItemOptionTemplate iot = getItemOptionTemplate(optId);
                String name = (iot != null) ? formatOptionName(iot.name, param) : ("Option " + optId);
                sb.append("|2|• ").append(optId).append(" - ").append(name).append(" [").append(param).append("]\n");
            }
        }

        String toggleText = session.keepDefaultOptions ? "Tắt\nOption gốc" : "Bật\nOption gốc";

        NpcService.gI().createMenuConMeo(admin, ConstNpc.BUFF_ITEM_OPTIONS, 17997,
                sb.toString(),
                "Chọn chỉ số\ntrong game",
                "Chỉ số\nphổ biến",
                "Tìm & Thêm\nOption",
                "Nhập chuỗi\nOption",
                toggleText,
                "Xóa bớt\nOption",
                "Xem trước\n& Xác nhận",
                "Hủy");
    }

    /**
     * Hiển thị menu shortcut các chỉ số phổ biến (Mục 22)
     */
    public void showPopularOptions(Player admin) {
        BuffSession session = getSession(admin);
        if (session == null) return;

        // Các chỉ số hot:
        // 50: Sức đánh +#%
        // 77: HP +#%
        // 103: KI +#%
        // 14: Chí mạng +#%
        // 5: Sức đánh chí mạng +#%
        // 95: Biến #% tấn công thành HP
        // 96: Biến #% tấn công thành KI
        // 97: Phản #% sát thương
        // 94: Giáp +#%
        // 108: Né đòn +#%
        String info = "|7|----- CHỌN NHANH CHỈ SỐ PHỔ BIẾN -----\n"
                + "|0|Chọn chỉ số bạn muốn thêm vào vật phẩm:";

        NpcService.gI().createMenuConMeo(admin, ConstNpc.BUFF_ITEM_COMMON_OPTIONS, 17997,
                info,
                "Sức đánh % (50)",
                "HP % (77)",
                "KI % (103)",
                "Chí mạng % (14)",
                "SĐ CM % (5)",
                "Hút HP % (95)",
                "Hút KI % (96)",
                "Phản ST % (97)",
                "Giáp % (94)",
                "Quay lại");
    }

    /**
     * Xử lý chọn shortcut option phổ biến
     */
    public void handleCommonOptionSelect(Player admin, int select) {
        BuffSession session = getSession(admin);
        if (session == null) return;

        int optionId = -1;
        switch (select) {
            case 0: optionId = 50; break;
            case 1: optionId = 77; break;
            case 2: optionId = 103; break;
            case 3: optionId = 14; break;
            case 4: optionId = 5; break;
            case 5: optionId = 95; break;
            case 6: optionId = 96; break;
            case 7: optionId = 97; break;
            case 8: optionId = 94; break;
            case 9:
                showMenuOptions(admin);
                return;
            default:
                showMenuOptions(admin);
                return;
        }

        session.pendingOptionId = optionId;
        ItemOptionTemplate iot = getItemOptionTemplate(optionId);
        String optName = iot != null ? iot.name : ("Option " + optionId);
        Input.gI().createFormBuffParam(admin, optionId, optName);
    }

    /**
     * Thêm một option vào custom options (tự động chống duplicate)
     */
    public void addOption(Player admin, int optionId, int param) {
        BuffSession session = getSession(admin);
        if (session == null) return;

        ItemOptionTemplate iot = getItemOptionTemplate(optionId);
        if (iot == null) {
            Service.getInstance().sendThongBao(admin, "Option ID " + optionId + " không tồn tại!");
            return;
        }

        // Tự động thay thế / cập nhật nếu đã có ID trùng (Mục 14, 24)
        session.customOptions.put(optionId, param);
        showMenuOptions(admin);
    }

    /**
     * Xử lý khi Admin nhập Form tìm option (ID/Tên + Param)
     */
    public void handleSearchOptionInput(Player admin, String query, String paramStr) {
        BuffSession session = getSession(admin);
        if (session == null) return;

        if (query == null || query.trim().isEmpty()) {
            Service.getInstance().sendThongBao(admin, "Vui lòng nhập ID hoặc tên option.");
            return;
        }
        query = query.trim();

        // 1. Nếu query là số ID
        if (isNumeric(query)) {
            try {
                int optId = Integer.parseInt(query);
                ItemOptionTemplate iot = getItemOptionTemplate(optId);
                if (iot == null) {
                    Service.getInstance().sendThongBao(admin, "Option ID " + optId + " không tồn tại!");
                    return;
                }

                // Nếu có param đi kèm
                if (paramStr != null && !paramStr.trim().isEmpty()) {
                    int param = Integer.parseInt(paramStr.trim());
                    addOption(admin, optId, param);
                    return;
                } else {
                    // Mở form nhập param
                    session.pendingOptionId = optId;
                    Input.gI().createFormBuffParam(admin, optId, iot.name);
                    return;
                }
            } catch (Exception e) {
                Service.getInstance().sendThongBao(admin, "Tham số Param không hợp lệ.");
                return;
            }
        }

        // 2. Query là tên -> Tìm kiếm theo tên
        String cleanQuery = stripDiacritics(query).toLowerCase();
        List<ItemOptionTemplate> matches = new ArrayList<>();
        for (ItemOptionTemplate iot : Manager.ITEM_OPTION_TEMPLATES) {
            if (iot == null || iot.name == null) continue;
            String cleanName = stripDiacritics(iot.name).toLowerCase();
            if (cleanName.contains(cleanQuery) || iot.name.toLowerCase().contains(query.toLowerCase())) {
                matches.add(iot);
            }
        }

        if (matches.isEmpty()) {
            Service.getInstance().sendThongBao(admin, "Không tìm thấy option nào khớp với: " + query);
            return;
        }

        if (matches.size() == 1) {
            ItemOptionTemplate iot = matches.get(0);
            session.pendingOptionId = iot.id;
            if (paramStr != null && !paramStr.trim().isEmpty()) {
                try {
                    int param = Integer.parseInt(paramStr.trim());
                    addOption(admin, iot.id, param);
                    return;
                } catch (Exception ignored) {
                }
            }
            Input.gI().createFormBuffParam(admin, iot.id, iot.name);
            return;
        }

        // Nhiều kết quả -> mở danh sách chọn option
        session.searchOptionResults = matches;
        session.searchOptionPage = 0;
        showOptionSearchResults(admin, session);
    }

    /**
     * Hiển thị danh sách kết quả tìm kiếm option
     */
    public void showOptionSearchResults(Player admin, BuffSession session) {
        if (session.searchOptionResults == null || session.searchOptionResults.isEmpty()) {
            showMenuOptions(admin);
            return;
        }
        int pageSize = 4;
        int totalItems = session.searchOptionResults.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        if (session.searchOptionPage >= totalPages) session.searchOptionPage = totalPages - 1;
        if (session.searchOptionPage < 0) session.searchOptionPage = 0;

        int start = session.searchOptionPage * pageSize;
        int end = Math.min(start + pageSize, totalItems);

        List<String> menuOptions = new ArrayList<>();
        for (int i = start; i < end; i++) {
            ItemOptionTemplate iot = session.searchOptionResults.get(i);
            menuOptions.add(iot.id + " - " + iot.name);
        }

        if (session.searchOptionPage < totalPages - 1) {
            menuOptions.add("Trang sau");
        }
        if (session.searchOptionPage > 0) {
            menuOptions.add("Trang trước");
        }
        menuOptions.add("Quay lại");

        String info = "|7|--- DANH SÁCH OPTION (" + (session.searchOptionPage + 1) + "/" + totalPages + ") ---\n"
                + "|0|Tìm thấy |2|" + totalItems + "|0| option phù hợp.\n"
                + "|2|Vui lòng chọn option muốn thêm:";

        NpcService.gI().createMenuConMeo(admin, ConstNpc.BUFF_ITEM_SELECT_OPTION_SEARCH, 17997,
                info, menuOptions.toArray(new String[0]));
    }

    /**
     * Xử lý click chọn option từ menu tìm kiếm option
     */
    public void handleSelectOptionSearchMenu(Player admin, int select) {
        BuffSession session = getSession(admin);
        if (session == null || session.searchOptionResults == null) return;

        int pageSize = 4;
        int totalItems = session.searchOptionResults.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        int start = session.searchOptionPage * pageSize;
        int countInPage = Math.min(pageSize, totalItems - start);

        if (select < countInPage) {
            ItemOptionTemplate selected = session.searchOptionResults.get(start + select);
            session.pendingOptionId = selected.id;
            Input.gI().createFormBuffParam(admin, selected.id, selected.name);
            return;
        }

        int nextIdx = countInPage;
        boolean hasNext = session.searchOptionPage < totalPages - 1;
        if (hasNext && select == nextIdx) {
            session.searchOptionPage++;
            showOptionSearchResults(admin, session);
            return;
        }
        if (hasNext) nextIdx++;

        boolean hasPrev = session.searchOptionPage > 0;
        if (hasPrev && select == nextIdx) {
            session.searchOptionPage--;
            showOptionSearchResults(admin, session);
            return;
        }

        showMenuOptions(admin);
    }

    /**
     * Xử lý nhập chuỗi Option dạng: 50-30 77-25 103-25 14-15 95-10 96-10
     */
    public void handleOptionStringInput(Player admin, String inputStr) {
        BuffSession session = getSession(admin);
        if (session == null) return;

        if (inputStr == null || inputStr.trim().isEmpty()) {
            Service.getInstance().sendThongBao(admin, "Chuỗi Option trống.");
            return;
        }

        // Tách chuỗi theo dấu cách hoặc dấu phẩy
        String[] tokens = inputStr.trim().split("[,\\s]+");
        int countSuccess = 0;
        List<String> errors = new ArrayList<>();

        for (String token : tokens) {
            if (token.isEmpty()) continue;
            String[] pair = token.split("[-:]");
            if (pair.length != 2) {
                errors.add("Sai định dạng: " + token);
                continue;
            }
            try {
                int optId = Integer.parseInt(pair[0].trim());
                int param = Integer.parseInt(pair[1].trim());

                ItemOptionTemplate iot = getItemOptionTemplate(optId);
                if (iot == null) {
                    errors.add("ID " + optId + " không tồn tại");
                    continue;
                }
                session.customOptions.put(optId, param);
                countSuccess++;
            } catch (Exception e) {
                errors.add("Lỗi số: " + token);
            }
        }

        if (!errors.isEmpty()) {
            Service.getInstance().sendThongBao(admin, "Thêm " + countSuccess + " option. Lỗi: " + String.join(", ", errors));
        } else {
            Service.getInstance().sendThongBao(admin, "Đã thêm thành công " + countSuccess + " option.");
        }
        showMenuOptions(admin);
    }

    /**
     * Xử lý khi Admin nhập param cho option đang chọn
     */
    public void handleParamInput(Player admin, String paramStr) {
        BuffSession session = getSession(admin);
        if (session == null || session.pendingOptionId == -1) return;

        try {
            int param = Integer.parseInt(paramStr.trim());
            addOption(admin, session.pendingOptionId, param);
            session.pendingOptionId = -1;
        } catch (Exception e) {
            Service.getInstance().sendThongBao(admin, "Param phải là số nguyên hợp lệ.");
            ItemOptionTemplate iot = getItemOptionTemplate(session.pendingOptionId);
            Input.gI().createFormBuffParam(admin, session.pendingOptionId, iot != null ? iot.name : "");
        }
    }

    /**
     * Xóa bớt option đã chọn
     */
    public void showDeleteOptionMenu(Player admin) {
        BuffSession session = getSession(admin);
        if (session == null || session.customOptions.isEmpty()) {
            Service.getInstance().sendThongBao(admin, "Chưa có option nào để xóa.");
            showMenuOptions(admin);
            return;
        }

        List<String> options = new ArrayList<>();
        List<Integer> optionIds = new ArrayList<>(session.customOptions.keySet());
        for (int optId : optionIds) {
            ItemOptionTemplate iot = getItemOptionTemplate(optId);
            String name = iot != null ? iot.name : ("ID " + optId);
            options.add("Xóa " + optId + " (" + session.customOptions.get(optId) + ")");
        }
        options.add("XÓA TẤT CẢ");
        options.add("Quay lại");

        String info = "|7|----- XÓA OPTION ĐÃ CHỌN -----\n"
                + "|0|Chọn option bạn muốn xóa:";

        NpcService.gI().createMenuConMeo(admin, ConstNpc.BUFF_ITEM_DELETE_OPTION, 17997,
                info, options.toArray(new String[0]));
    }

    /**
     * Xử lý xóa option từ menu
     */
    public void handleDeleteOptionMenu(Player admin, int select) {
        BuffSession session = getSession(admin);
        if (session == null) return;

        List<Integer> optionIds = new ArrayList<>(session.customOptions.keySet());
        if (select < optionIds.size()) {
            int toRemove = optionIds.get(select);
            session.customOptions.remove(toRemove);
            Service.getInstance().sendThongBao(admin, "Đã xóa Option ID: " + toRemove);
            showMenuOptions(admin);
            return;
        }

        if (select == optionIds.size()) {
            // Xóa tất cả
            session.customOptions.clear();
            Service.getInstance().sendThongBao(admin, "Đã xóa toàn bộ option tùy chỉnh.");
            showMenuOptions(admin);
            return;
        }

        // Quay lại
        showMenuOptions(admin);
    }

    /**
     * Hiển thị màn hình XEM TRƯỚC (PREVIEW) & Xác nhận (Mục 15, 16)
     */
    public void showPreview(Player admin) {
        BuffSession session = getSession(admin);
        if (session == null || session.selectedTemplate == null) return;

        List<ItemOption> finalOptions = buildFinalOptions(session);

        StringBuilder sb = new StringBuilder();
        sb.append("|7|========== XEM TRƯỚC VẬT PHẨM ==========\n");
        sb.append("|0|Người nhận: |2|").append(session.targetPlayerName).append("\n");
        sb.append("|0|Trạng thái: ").append(session.targetIsOnline ? "|1|ONLINE" : "|7|OFFLINE").append("\n");
        sb.append("|0|Vật phẩm: |2|").append(session.selectedTemplate.name)
                .append(" (ID: ").append(session.selectedTemplate.id).append(")\n");
        sb.append("|0|Số lượng: |2|").append(session.quantity).append("\n");
        sb.append("|8|DANH SÁCH OPTION:\n");

        JSONArray jsonArray = new JSONArray();
        for (ItemOption io : finalOptions) {
            String optName = io.optionTemplate != null ? io.getOptionString() : ("Option " + io.optionTemplate.id);
            sb.append("|2|• ").append(optName).append("\n");

            JSONObject obj = new JSONObject();
            obj.put("id", io.optionTemplate.id);
            obj.put("param", io.param);
            jsonArray.add(obj);
        }

        sb.append("|3|JSON DEBUG:\n|6|").append(jsonArray.toJSONString()).append("\n");
        sb.append("|7|========================================");

        NpcService.gI().createMenuConMeo(admin, ConstNpc.BUFF_ITEM_PREVIEW, 17997,
                sb.toString(),
                "XÁC NHẬN BUFF", "Thêm Option", "Quay lại", "Hủy");
    }

    /**
     * Xử lý bấm nút trong Menu Preview
     */
    public void handlePreviewMenu(Player admin, int select) {
        BuffSession session = getSession(admin);
        if (session == null) return;

        switch (select) {
            case 0:
                // XÁC NHẬN BUFF
                executeBuff(admin);
                break;
            case 1:
            case 2:
                // Thêm option / Quay lại
                showMenuOptions(admin);
                break;
            case 3:
                // Hủy
                removeSession(admin);
                Service.getInstance().sendThongBao(admin, "Đã hủy thao tác buff vật phẩm.");
                break;
        }
    }

    /**
     * Thực hiện buff vật phẩm vào nhân vật
     */
    public void executeBuff(Player admin) {
        if (admin == null || admin.getSession() == null || !admin.isAdmin()) {
            if (admin != null) {
                Service.getInstance().sendThongBao(admin, "Bạn không có quyền thực hiện!");
            }
            return;
        }

        BuffSession session = getSession(admin);
        if (session == null || session.selectedTemplate == null) {
            Service.getInstance().sendThongBao(admin, "Phiên làm việc đã hết hạn hoặc không hợp lệ.");
            return;
        }

        // Chống double click / gửi lặp packet (Mục 26)
        if (!session.startProcessing()) {
            Service.getInstance().sendThongBao(admin, "Thao tác đang được xử lý, vui lòng không nhấn liên tục!");
            return;
        }

        try {
            // Kiểm tra xem target hiện tại có đang online không
            Player targetOnline = Client.gI().getPlayer(session.targetPlayerId);
            if (targetOnline == null) {
                targetOnline = Client.gI().getPlayer(session.targetPlayerName);
            }

            if (targetOnline != null) {
                buffOnlinePlayer(admin, session, targetOnline);
            } else {
                buffOfflinePlayer(admin, session);
            }
        } finally {
            session.finishProcessing();
        }
    }

    /**
     * Buff cho người chơi ONLINE
     */
    private void buffOnlinePlayer(Player admin, BuffSession session, Player target) {
        if (session.quantity <= 0 || session.quantity > 2_000_000_000) {
            Service.getInstance().sendThongBao(admin, "Số lượng vượt quá giới hạn cho phép.");
            return;
        }

        Item item = ItemService.gI().createNewItem(session.selectedTemplate.id, session.quantity);
        item.itemOptions = buildFinalOptions(session);
        item.quantity = session.quantity;

        boolean success = InventoryService.gI().addItemBag(target, item, 0);
        if (success) {
            InventoryService.gI().sendItemBags(target);
            Service.getInstance().sendThongBao(target,
                    "Bạn nhận được " + session.quantity + "x " + item.template.name + " từ Admin.");
            Service.getInstance().sendThongBao(admin,
                    "Buff thành công " + session.quantity + "x " + item.template.name + " cho " + target.name + " (ONLINE)!");

            logAudit(admin, target.name, target.id, "ONLINE", item.template.id, item.template.name, session.quantity, item.itemOptions);
            removeSession(admin);
        } else {
            Service.getInstance().sendThongBao(admin, "Túi đồ người chơi đã đầy (hoặc đạt giới hạn).");
        }
    }

    /**
     * Buff cho người chơi OFFLINE: Lưu vào hàng đợi PENDING (admin_item_grant)
     * Tuyệt đối không can thiệp trực tiếp items_bag trong DB khi player offline để chống race condition.
     */
    private void buffOfflinePlayer(Player admin, BuffSession session) {
        // Kiểm tra xem player có vừa online hay không trước khi lưu grant
        Player onlineTarget = Client.gI().getPlayer(session.targetPlayerId);
        if (onlineTarget == null && session.targetPlayerName != null) {
            onlineTarget = Client.gI().getPlayer(session.targetPlayerName);
        }
        if (onlineTarget != null) {
            buffOnlinePlayer(admin, session, onlineTarget);
            return;
        }

        if (session.selectedTemplate == null) {
            Service.getInstance().sendThongBao(admin, "Chưa chọn vật phẩm.");
            return;
        }

        // Chặn các item world/event đặc biệt không thể deliver offline
        if (ItemMapService.gI().isBlackBall(session.selectedTemplate.id)) {
            Service.getInstance().sendThongBao(admin, "Vật phẩm Ngọc Rồng Đen chỉ có thể buff khi người chơi ONLINE!");
            return;
        }

        if (session.quantity <= 0 || session.quantity > 2_000_000_000) {
            Service.getInstance().sendThongBao(admin, "Số lượng vượt quá giới hạn cho phép.");
            return;
        }

        List<ItemOption> finalOptions = buildFinalOptions(session);

        JSONArray jsonArray = new JSONArray();
        for (ItemOption io : finalOptions) {
            if (io != null && io.optionTemplate != null) {
                JSONObject obj = new JSONObject();
                obj.put("id", io.optionTemplate.id);
                obj.put("param", io.param);
                jsonArray.add(obj);
            }
        }
        String optionsJson = jsonArray.toJSONString();

        long grantId = -1;
        String insertSql = "INSERT INTO admin_item_grant (target_player_id, admin_player_id, item_template_id, quantity, options_json, status, created_at) "
                + "VALUES (?, ?, ?, ?, ?, 'PENDING', NOW())";

        try (Connection con = DBService.gI().getConnectionForSaveData();
             PreparedStatement ps = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, session.targetPlayerId);
            ps.setLong(2, admin.id);
            ps.setInt(3, session.selectedTemplate.id);
            ps.setInt(4, session.quantity);
            ps.setString(5, optionsJson);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    grantId = rs.getLong(1);
                }
            }

            logAuditQueued(grantId, admin, session.targetPlayerName, session.targetPlayerId,
                    session.selectedTemplate.id, session.selectedTemplate.name, session.quantity, optionsJson);

            Service.getInstance().sendThongBao(admin,
                    "Đã tạo yêu cầu buff " + session.quantity + "x " + session.selectedTemplate.name
                            + " cho " + session.targetPlayerName + " (OFFLINE - Đang chờ nhận khi đăng nhập)!");
            removeSession(admin);
        } catch (Exception e) {
            Log.error(AdminBuffItemService.class, e, "Lỗi tạo pending grant cho player " + session.targetPlayerName);
            Service.getInstance().sendThongBao(admin, "Lỗi khi lưu yêu cầu buff offline: " + e.getMessage());
        }
    }

    /**
     * Snapshot trạng thái Player trong RAM để khôi phục (rollback) khi DB transaction gặp sự cố
     */
    public static class PlayerStateSnapshot {
        private final List<Item> itemsBag;
        private final List<Item> itemsBox;
        private final long gold;
        private final int gem;
        private final int ruby;
        private final long goldLimit;
        private final int hp;
        private final int mp;
        private final boolean haveTennisSpaceShip;
        private final MabuEgg mabuEgg;

        public PlayerStateSnapshot(Player player) {
            this.itemsBag = new ArrayList<>();
            if (player.inventory != null && player.inventory.itemsBag != null) {
                for (Item it : player.inventory.itemsBag) {
                    this.itemsBag.add(copyItemSafe(it));
                }
            }
            this.itemsBox = new ArrayList<>();
            if (player.inventory != null && player.inventory.itemsBox != null) {
                for (Item it : player.inventory.itemsBox) {
                    this.itemsBox.add(copyItemSafe(it));
                }
            }
            this.gold = player.inventory != null ? player.inventory.gold : 0;
            this.gem = player.inventory != null ? player.inventory.gem : 0;
            this.ruby = player.inventory != null ? player.inventory.ruby : 0;
            this.goldLimit = player.inventory != null ? player.inventory.goldLimit : 0;
            this.hp = player.nPoint != null ? player.nPoint.hp : 0;
            this.mp = player.nPoint != null ? player.nPoint.mp : 0;
            this.haveTennisSpaceShip = player.haveTennisSpaceShip;
            this.mabuEgg = player.mabuEgg;
        }

        public void restore(Player player) {
            if (player.inventory != null) {
                if (player.inventory.itemsBag != null) {
                    player.inventory.itemsBag.clear();
                    for (Item it : this.itemsBag) {
                        player.inventory.itemsBag.add(copyItemSafe(it));
                    }
                }
                if (player.inventory.itemsBox != null) {
                    player.inventory.itemsBox.clear();
                    for (Item it : this.itemsBox) {
                        player.inventory.itemsBox.add(copyItemSafe(it));
                    }
                }
                player.inventory.gold = this.gold;
                player.inventory.gem = this.gem;
                player.inventory.ruby = this.ruby;
                player.inventory.goldLimit = this.goldLimit;
            }
            if (player.nPoint != null) {
                player.nPoint.hp = this.hp;
                player.nPoint.mp = this.mp;
            }
            player.haveTennisSpaceShip = this.haveTennisSpaceShip;
            player.mabuEgg = this.mabuEgg;
        }

        public void resyncClient(Player player) {
            if (player == null || player.getSession() == null) {
                return;
            }
            try {
                InventoryService.gI().sendItemBags(player);
                Service.getInstance().sendMoney(player);
                PlayerService.gI().sendInfoHpMp(player);
            } catch (Exception e) {
                // Bỏ qua lỗi gửi packet trong unit test / mock session
            }
        }

        private static Item copyItemSafe(Item it) {
            if (it == null) {
                return null;
            }
            if (!it.isNotNullItem()) {
                Item empty = new Item();
                empty.template = null;
                empty.quantity = 0;
                return empty;
            }
            return ItemService.gI().copyItem(it);
        }
    }

    /**
     * Nhận các vật phẩm Admin đang ở trạng thái PENDING khi player đăng nhập hoặc gọi chat "nhanqua".
     * Mỗi grant được xử lý trong một DB transaction độc lập kết hợp lưu dữ liệu Player (PlayerDAO.updateAdminGrantStateOrThrow)
     * và cập nhật trạng thái DELIVERED trên cùng Connection.
     * Có cơ chế RAM Snapshot & Rollback nếu DB transaction bị lỗi.
     */
    public void deliverPendingGrants(Player player) {
        if (player == null || player.inventory == null || player.getSession() == null) {
            return;
        }

        // Chống đồng thời xử lý 2 lần trong bộ nhớ
        if (!deliveringPlayers.add(player.id)) {
            return;
        }

        try {
            List<Long> pendingIds = new ArrayList<>();
            // Load danh sách ID grant PENDING theo thứ tự FIFO (id ASC)
            try (Connection con = DBService.gI().getConnectionForSaveData();
                 PreparedStatement ps = con.prepareStatement(
                         "SELECT id FROM admin_item_grant WHERE target_player_id = ? AND status = 'PENDING' ORDER BY id ASC")) {
                ps.setLong(1, player.id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        pendingIds.add(rs.getLong("id"));
                    }
                }
            } catch (Throwable e) {
                Log.error(AdminBuffItemService.class.getName() + ": Lỗi tải danh sách pending grants cho player " + player.name + ": " + e.getMessage());
                return;
            }

            if (pendingIds.isEmpty()) {
                return;
            }

            boolean hasBagFull = false;
            int deliveredCount = 0;

            for (long grantId : pendingIds) {
                boolean acquiredSavingLock = false;
                PlayerStateSnapshot snapshot = null;
                try (Connection con = DBService.gI().getConnectionForSaveData()) {
                    con.setAutoCommit(false);

                    int itemTemplateId = -1;
                    int quantity = 0;
                    String optionsJson = null;
                    String currentStatus = null;

                    // 1. SELECT grant ... FOR UPDATE
                    try (PreparedStatement psSelect = con.prepareStatement(
                            "SELECT item_template_id, quantity, options_json, status FROM admin_item_grant WHERE id = ? FOR UPDATE")) {
                        psSelect.setLong(1, grantId);
                        try (ResultSet rs = psSelect.executeQuery()) {
                            if (rs.next()) {
                                itemTemplateId = rs.getInt("item_template_id");
                                quantity = rs.getInt("quantity");
                                optionsJson = rs.getString("options_json");
                                currentStatus = rs.getString("status");
                            }
                        }
                    }

                    // 2. Xác nhận status == PENDING
                    if (!"PENDING".equals(currentStatus)) {
                        con.rollback();
                        continue;
                    }

                    // 3. Chống concurrent normal save: cố gắng acquire saving lock của Player
                    if (!PlayerDAO.tryAcquireSaving(player)) {
                        con.rollback();
                        break;
                    }
                    acquiredSavingLock = true;

                    // 4. Validate ItemTemplate & Quantity
                    ItemTemplate template = getItemTemplate(itemTemplateId);
                    if (template == null || quantity <= 0 || quantity > 2_000_000_000) {
                        try (PreparedStatement psFail = con.prepareStatement(
                                "UPDATE admin_item_grant SET status = 'FAILED' WHERE id = ?")) {
                            psFail.setLong(1, grantId);
                            psFail.executeUpdate();
                        }
                        con.commit();
                        continue;
                    }

                    // 5. Tạo snapshot trạng thái Player trước khi thay đổi RAM
                    snapshot = new PlayerStateSnapshot(player);

                    // 6. Update status PENDING -> PROCESSING atomically
                    try (PreparedStatement psProc = con.prepareStatement(
                            "UPDATE admin_item_grant SET status = 'PROCESSING' WHERE id = ? AND status = 'PENDING'")) {
                        psProc.setLong(1, grantId);
                        if (psProc.executeUpdate() == 0) {
                            con.rollback();
                            continue;
                        }
                    }

                    // 7. Tạo Item và khôi phục options
                    Item item = ItemService.gI().createNewItem((short) itemTemplateId, quantity);
                    item.itemOptions = parseOptionsJson(optionsJson);
                    item.quantity = quantity;

                    // 8. Gọi InventoryService.gI().addItemBag(player, item, 0)
                    boolean added = false;
                    try {
                        added = InventoryService.gI().addItemBag(player, item, 0);
                    } catch (Throwable t) {
                        Log.error(AdminBuffItemService.class.getName() + ": Lỗi addItemBag cho grant " + grantId + ": " + t.getMessage());
                        added = false;
                    }

                    if (!added) {
                        // Khôi phục RAM nếu có mutation dở dang
                        snapshot.restore(player);
                        snapshot.resyncClient(player);

                        // Trả trạng thái về PENDING
                        try (PreparedStatement psRevert = con.prepareStatement(
                                "UPDATE admin_item_grant SET status = 'PENDING' WHERE id = ?")) {
                            psRevert.setLong(1, grantId);
                            psRevert.executeUpdate();
                        }
                        con.commit();

                        logAuditBagFull(grantId, player.name, player.id, item.template.id, item.template.name, quantity);
                        hasBagFull = true;
                        // Dừng xử lý các grant tiếp theo để giữ đúng FIFO
                        break;
                    }

                    // 9. Đã thêm vào RAM thành công -> persist Player bằng updateAdminGrantStateOrThrow và update DELIVERED trong CÙNG transaction!
                    try {
                        PlayerDAO.updateAdminGrantStateOrThrow(player, con);

                        try (PreparedStatement psDelivered = con.prepareStatement(
                                "UPDATE admin_item_grant SET status = 'DELIVERED', delivered_at = NOW() WHERE id = ?")) {
                            psDelivered.setLong(1, grantId);
                            int delAffected = psDelivered.executeUpdate();
                            if (delAffected != 1) {
                                throw new SQLException("Update DELIVERED affected " + delAffected + " rows for grant " + grantId);
                            }
                        }

                        // COMMIT CẢ DỮ LIỆU PLAYER VÀ TRẠNG THÁI GRANT CÙNG NHAU
                        con.commit();

                        logAuditDelivered(grantId, player.name, player.id, item.template.id, item.template.name, quantity);
                        deliveredCount++;
                    } catch (Throwable t) {
                        // Lỗi ghi DB: rollback cả player data và trạng thái grant
                        con.rollback();

                        // QUAN TRỌNG: Khôi phục RAM về snapshot để không bị dirty memory!
                        snapshot.restore(player);
                        snapshot.resyncClient(player);

                        Log.error(AdminBuffItemService.class.getName() + ": Lỗi commit grant " + grantId + ", đã khôi phục RAM và DB: " + t.getMessage());
                        break;
                    }
                } catch (Throwable e) {
                    Log.error(AdminBuffItemService.class.getName() + ": Lỗi transaction cho grant " + grantId + ": " + e.getMessage());
                    break;
                } finally {
                    if (acquiredSavingLock) {
                        PlayerDAO.releaseSaving(player);
                    }
                }
            }

            if (deliveredCount > 0) {
                InventoryService.gI().sendItemBags(player);
                Service.getInstance().sendThongBao(player,
                        "Bạn đã nhận thành công " + deliveredCount + " phần quà/vật phẩm từ Admin!");
            }

            if (hasBagFull) {
                Service.getInstance().sendThongBao(player,
                        "Bạn có vật phẩm Admin đang chờ nhận nhưng hành trang đã đầy. Vui lòng dọn túi rồi chat 'nhanqua' hoặc đăng nhập lại để nhận.");
            }
        } catch (Throwable t) {
            Log.error(AdminBuffItemService.class.getName() + ": Lỗi deliverPendingGrants cho player " + player.name + ": " + t.getMessage());
        } finally {
            deliveringPlayers.remove(player.id);
        }
    }

    /**
     * Xây dựng danh sách options cuối cùng cho item (kết hợp option mặc định nếu được bật)
     */
    public List<ItemOption> buildFinalOptions(BuffSession session) {
        Map<Integer, Integer> merged = new LinkedHashMap<>();

        // 1. Nếu giữ option mặc định của item
        if (session.keepDefaultOptions && session.selectedTemplate != null) {
            Item sample = ItemService.gI().createNewItem(session.selectedTemplate.id);
            RewardService.gI().initBaseOptionClothes(session.selectedTemplate.id, session.selectedTemplate.type, sample.itemOptions);
            for (ItemOption io : sample.itemOptions) {
                if (io != null && io.optionTemplate != null) {
                    merged.put(io.optionTemplate.id, io.param);
                }
            }
        }

        // 2. Ghi đè hoặc thêm mới bằng custom options của Admin
        for (Map.Entry<Integer, Integer> entry : session.customOptions.entrySet()) {
            merged.put(entry.getKey(), entry.getValue());
        }

        List<ItemOption> result = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : merged.entrySet()) {
            int optId = entry.getKey();
            int param = entry.getValue();
            ItemOptionTemplate iot = getItemOptionTemplate(optId);
            if (iot != null) {
                result.add(new ItemOption(optId, param));
            }
        }

        // Nếu hoàn toàn không có option nào, thêm option rỗng (73, 0) theo chuẩn NRO
        if (result.isEmpty()) {
            result.add(new ItemOption(73, 0));
        }

        return result;
    }

    /**
     * Ghi log Audit theo Yêu cầu 25
     */
    private void logAudit(Player admin, String targetName, long targetId, String status,
                          int itemId, String itemName, int quantity, List<ItemOption> options) {
        StringBuilder optSb = new StringBuilder("[");
        for (int i = 0; i < options.size(); i++) {
            ItemOption io = options.get(i);
            optSb.append(io.optionTemplate != null ? io.optionTemplate.id : -1).append(":").append(io.param);
            if (i < options.size() - 1) optSb.append(",");
        }
        optSb.append("]");

        String timeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logMsg = String.format("[ADMIN_GIVE_ITEM] Admin=%s(%d) Target=%s(%d) Status=%s Item=%d(%s) Quantity=%d Options=%s Timestamp=%s",
                admin.name, admin.id, targetName, targetId, status, itemId, itemName, quantity, optSb.toString(), timeStr);

        Log.success(logMsg);
        System.out.println(logMsg);
    }

    private void logAuditQueued(long grantId, Player admin, String targetName, long targetId,
                                int itemId, String itemName, int quantity, String optionsJson) {
        String timeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logMsg = String.format("[ADMIN_ITEM_QUEUED] GrantID=%d Admin=%s(%d) Target=%s(%d) Item=%d(%s) Quantity=%d Options=%s Timestamp=%s",
                grantId, admin.name, admin.id, targetName, targetId, itemId, itemName, quantity, optionsJson, timeStr);
        Log.success(logMsg);
        System.out.println(logMsg);
    }

    private void logAuditDelivered(long grantId, String targetName, long targetId,
                                   int itemId, String itemName, int quantity) {
        String timeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logMsg = String.format("[ADMIN_ITEM_DELIVERED] GrantID=%d Target=%s(%d) Item=%d(%s) Quantity=%d Timestamp=%s",
                grantId, targetName, targetId, itemId, itemName, quantity, timeStr);
        Log.success(logMsg);
        System.out.println(logMsg);
    }

    private void logAuditBagFull(long grantId, String targetName, long targetId,
                                 int itemId, String itemName, int quantity) {
        String timeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logMsg = String.format("[ADMIN_ITEM_PENDING_BAG_FULL] GrantID=%d Target=%s(%d) Item=%d(%s) Quantity=%d Timestamp=%s",
                grantId, targetName, targetId, itemId, itemName, quantity, timeStr);
        Log.warning(logMsg);
        System.out.println(logMsg);
    }

    /**
     * Helper parse JSON options array/object thành List<ItemOption>
     */
    public List<ItemOption> parseOptionsJson(String jsonStr) {
        List<ItemOption> list = new ArrayList<>();
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            list.add(new ItemOption(73, 0));
            return list;
        }
        try {
            JSONParser parser = new JSONParser();
            Object parsed = parser.parse(jsonStr);
            if (parsed instanceof JSONArray) {
                JSONArray arr = (JSONArray) parsed;
                for (Object o : arr) {
                    if (o instanceof JSONObject) {
                        JSONObject obj = (JSONObject) o;
                        int id = Integer.parseInt(String.valueOf(obj.get("id")));
                        int param = Integer.parseInt(String.valueOf(obj.get("param")));
                        ItemOptionTemplate iot = getItemOptionTemplate(id);
                        if (iot != null) {
                            list.add(new ItemOption(id, param));
                        }
                    } else if (o instanceof JSONArray) {
                        JSONArray pair = (JSONArray) o;
                        if (pair.size() >= 2) {
                            int id = Integer.parseInt(String.valueOf(pair.get(0)));
                            int param = Integer.parseInt(String.valueOf(pair.get(1)));
                            ItemOptionTemplate iot = getItemOptionTemplate(id);
                            if (iot != null) {
                                list.add(new ItemOption(id, param));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.error(AdminBuffItemService.class, e, "Lỗi parse options_json: " + jsonStr);
        }
        if (list.isEmpty()) {
            list.add(new ItemOption(73, 0));
        }
        return list;
    }

    /**
     * Helper parse JSON options array thành List<ItemOption> để so sánh options chuẩn xác
     */
    public List<ItemOption> parseJsonOptions(JSONArray options) {
        List<ItemOption> list = new ArrayList<>();
        if (options != null) {
            for (int j = 0; j < options.size(); j++) {
                JSONArray opt = (JSONArray) options.get(j);
                if (opt != null && opt.size() >= 2) {
                    int optId = Integer.parseInt(String.valueOf(opt.get(0)));
                    int param = Integer.parseInt(String.valueOf(opt.get(1)));
                    list.add(new ItemOption(optId, param));
                }
            }
        }
        return list;
    }

    /**
     * Helper lấy ItemTemplate an toàn
     */
    public ItemTemplate getItemTemplate(int id) {
        if (id < 0 || id >= Manager.ITEM_TEMPLATES.size()) {
            return null;
        }
        return Manager.ITEM_TEMPLATES.get(id);
    }

    /**
     * Helper lấy ItemOptionTemplate an toàn
     */
    public ItemOptionTemplate getItemOptionTemplate(int id) {
        if (id < 0 || id >= Manager.ITEM_OPTION_TEMPLATES.size()) {
            return null;
        }
        return Manager.ITEM_OPTION_TEMPLATES.get(id);
    }

    /**
     * Format option name với param
     */
    public String formatOptionName(String templateName, int param) {
        if (templateName == null) return "";
        return Util.replace(templateName, "#", String.valueOf(param));
    }

    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) return false;
        return str.matches("\\d+");
    }

    private String stripDiacritics(String str) {
        if (str == null) return "";
        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("").replaceAll("đ", "d").replaceAll("Đ", "D");
    }

    /**
     * Buff vật phẩm trực tiếp từ Web Admin API hoặc command
     */
    public JSONObject buffItemDirect(String adminName, String recipientQuery, int itemTemplateId, int quantity,
                                    Map<Integer, Integer> customOptions, boolean keepDefaultOptions) {
        JSONObject res = new JSONObject();
        if (quantity <= 0 || quantity > 2_000_000_000) {
            res.put("success", false);
            res.put("message", "Số lượng không hợp lệ (nhập từ 1 đến 2,000,000,000)!");
            return res;
        }

        ItemTemplate template = getItemTemplate(itemTemplateId);
        if (template == null || template.name == null || template.name.contains("[Vô hiệu hóa]")) {
            res.put("success", false);
            res.put("message", "Vật phẩm ID " + itemTemplateId + " không tồn tại hoặc đã bị vô hiệu hóa!");
            return res;
        }

        AdminBalanceService.AccountInfo acc = AdminBalanceService.gI().lookupAccount(recipientQuery);
        if (acc == null) {
            res.put("success", false);
            res.put("message", "Không tìm thấy người nhận: " + recipientQuery);
            return res;
        }

        // Tạo danh sách options gộp
        Map<Integer, Integer> merged = new LinkedHashMap<>();
        if (keepDefaultOptions) {
            Item sample = ItemService.gI().createNewItem(template.id);
            RewardService.gI().initBaseOptionClothes(template.id, template.type, sample.itemOptions);
            for (ItemOption io : sample.itemOptions) {
                if (io != null && io.optionTemplate != null) {
                    merged.put(io.optionTemplate.id, io.param);
                }
            }
        }
        if (customOptions != null) {
            merged.putAll(customOptions);
        }

        List<ItemOption> finalOptions = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : merged.entrySet()) {
            ItemOptionTemplate iot = getItemOptionTemplate(entry.getKey());
            if (iot != null) {
                finalOptions.add(new ItemOption(entry.getKey(), entry.getValue()));
            }
        }
        if (finalOptions.isEmpty()) {
            finalOptions.add(new ItemOption(73, 0));
        }

        // 1. Kiểm tra ONLINE trước
        Player onlinePl = null;
        if (acc.playerId != null) {
            onlinePl = Client.gI().getPlayer(acc.playerId);
        }
        if (onlinePl == null && acc.playerName != null) {
            onlinePl = Client.gI().getPlayer(acc.playerName);
        }

        if (onlinePl != null) {
            Item item = ItemService.gI().createNewItem(template.id, quantity);
            item.itemOptions = finalOptions;
            item.quantity = quantity;

            boolean added = InventoryService.gI().addItemBag(onlinePl, item, 0);
            if (added) {
                InventoryService.gI().sendItemBags(onlinePl);
                Service.getInstance().sendThongBao(onlinePl, "Bạn nhận được " + quantity + "x " + item.template.name + " từ Admin.");
                logAuditDirect(adminName, onlinePl.name, onlinePl.id, "ONLINE", item.template.id, item.template.name, quantity, item.itemOptions);
                res.put("success", true);
                res.put("message", "Đã buff thành công " + quantity + "x " + item.template.name + " cho " + onlinePl.name + " (ONLINE)!");
                return res;
            } else {
                res.put("success", false);
                res.put("message", "Hành trang của người chơi " + onlinePl.name + " đã đầy!");
                return res;
            }
        } else {
            // 2. OFFLINE: Lưu vào admin_item_grant
            JSONArray jsonArray = new JSONArray();
            for (ItemOption io : finalOptions) {
                if (io != null && io.optionTemplate != null) {
                    JSONObject obj = new JSONObject();
                    obj.put("id", io.optionTemplate.id);
                    obj.put("param", io.param);
                    jsonArray.add(obj);
                }
            }
            String optionsJson = jsonArray.toJSONString();

            long targetPId = acc.playerId != null ? acc.playerId : 0;
            if (targetPId == 0) {
                res.put("success", false);
                res.put("message", "Tài khoản chưa tạo nhân vật, không thể lưu vật phẩm!");
                return res;
            }

            long grantId = -1;
            String insertSql = "INSERT INTO admin_item_grant (target_player_id, admin_player_id, item_template_id, quantity, options_json, status, created_at) VALUES (?, ?, ?, ?, ?, 'PENDING', NOW())";
            try (Connection con = DBService.gI().getConnectionForSaveData();
                 PreparedStatement ps = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, targetPId);
                ps.setLong(2, 0); // Web Admin
                ps.setInt(3, template.id);
                ps.setInt(4, quantity);
                ps.setString(5, optionsJson);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) grantId = rs.getLong(1);
                }
                logAuditQueued(grantId, adminName, acc.playerName, targetPId, template.id, template.name, quantity, optionsJson);
                res.put("success", true);
                res.put("message", "Đã lưu yêu cầu buff " + quantity + "x " + template.name + " cho " + acc.playerName + " (OFFLINE - Sẽ nhận khi đăng nhập)!");
                return res;
            } catch (Exception e) {
                Log.error(AdminBuffItemService.class, e, "Lỗi tạo pending grant từ Web Admin: " + acc.playerName);
                res.put("success", false);
                res.put("message", "Lỗi lưu CSDL: " + e.getMessage());
                return res;
            }
        }
    }

    private void logAuditDirect(String adminName, String targetName, long targetId, String status,
                                int itemId, String itemName, int quantity, List<ItemOption> options) {
        StringBuilder optSb = new StringBuilder("[");
        for (int i = 0; i < options.size(); i++) {
            ItemOption io = options.get(i);
            optSb.append(io.optionTemplate != null ? io.optionTemplate.id : -1).append(":").append(io.param);
            if (i < options.size() - 1) optSb.append(",");
        }
        optSb.append("]");

        String timeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logMsg = String.format("[ADMIN_GIVE_ITEM] Admin=%s Target=%s(%d) Status=%s Item=%d(%s) Quantity=%d Options=%s Timestamp=%s",
                adminName, targetName, targetId, status, itemId, itemName, quantity, optSb.toString(), timeStr);
        Log.success(logMsg);
        System.out.println(logMsg);
    }

    private void logAuditQueued(long grantId, String adminName, String targetName, long targetId,
                                int itemId, String itemName, int quantity, String optionsJson) {
        String timeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logMsg = String.format("[ADMIN_ITEM_QUEUED] GrantID=%d Admin=%s Target=%s(%d) Item=%d(%s) Quantity=%d Options=%s Timestamp=%s",
                grantId, adminName, targetName, targetId, itemId, itemName, quantity, optionsJson, timeStr);
        Log.success(logMsg);
        System.out.println(logMsg);
    }
}
