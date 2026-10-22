package nro.services.func;

import nro.consts.ConstNpc;
import nro.jdbc.DBService;
import nro.models.item.Item;
import nro.models.item.ItemOption;
import nro.models.item.ItemOptionTemplate;
import nro.models.item.ItemTemplate;
import nro.models.player.Player;
import nro.server.Client;
import nro.server.Manager;
import nro.services.InventoryService;
import nro.services.ItemService;
import nro.services.NpcService;
import nro.services.RewardService;
import nro.services.Service;
import nro.utils.Log;
import nro.utils.Util;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

    private AdminBuffItemService() {
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
     * Bắt đầu luồng: mở form tìm kiếm người chơi
     */
    public void openMenuBuffItem(Player admin) {
        if (admin == null || admin.getSession() == null || !admin.isAdmin()) {
            if (admin != null) {
                Service.getInstance().sendThongBao(admin, "Bạn không có quyền thực hiện chức năng này!");
            }
            return;
        }
        Input.gI().createFormBuffFindPlayer(admin);
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
            int qty = Integer.parseInt(qtyStr);
            if (qty <= 0) {
                Service.getInstance().sendThongBao(admin, "Số lượng không hợp lệ (phải >= 1).");
                Input.gI().createFormBuffQuantity(admin, session.selectedTemplate.id, session.selectedTemplate.name);
                return;
            }
            session.quantity = qty;
            showMenuOptions(admin);
        } catch (Exception e) {
            Service.getInstance().sendThongBao(admin, "Số lượng phải là số nguyên dương.");
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
     * Buff cho người chơi ONLINE (Mục 6, 17, 19)
     */
    private void buffOnlinePlayer(Player admin, BuffSession session, Player target) {
        Item item = ItemService.gI().createNewItem(session.selectedTemplate.id, session.quantity);
        item.itemOptions = buildFinalOptions(session);
        item.quantity = session.quantity;

        // Kiểm tra túi đồ người chơi
        if (InventoryService.gI().getCountEmptyBag(target) <= 0) {
            boolean canStack = false;
            if (InventoryService.gI().existItemBag(target, item.template.id) && isItemIncremental(item)) {
                canStack = true;
            }
            if (!canStack) {
                Service.getInstance().sendThongBao(admin, "Túi đồ người chơi đã đầy.");
                return;
            }
        }

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
            Service.getInstance().sendThongBao(admin, "Túi đồ người chơi đã đầy.");
        }
    }

    /**
     * Buff cho người chơi OFFLINE (Mục 7, 18, 19, 20)
     */
    private void buffOfflinePlayer(Player admin, BuffSession session) {
        List<ItemOption> finalOptions = buildFinalOptions(session);

        try (Connection con = DBService.gI().getConnectionForSaveData()) {
            con.setAutoCommit(false);
            try (PreparedStatement psSelect = con.prepareStatement("SELECT items_bag FROM player WHERE id = ? FOR UPDATE")) {
                psSelect.setLong(1, session.targetPlayerId);
                try (ResultSet rs = psSelect.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        Service.getInstance().sendThongBao(admin, "Không tìm thấy dữ liệu người chơi trong database.");
                        return;
                    }

                    String itemsBagStr = rs.getString("items_bag");
                    JSONParser parser = new JSONParser();
                    JSONArray dataBag = (JSONArray) parser.parse(itemsBagStr);

                    boolean stacked = false;
                    // Kiểm tra xem item có cộng dồn được không
                    if (isIncrementalTemplate(session.selectedTemplate)) {
                        for (int i = 0; i < dataBag.size(); i++) {
                            JSONObject obj = (JSONObject) dataBag.get(i);
                            int tempId = Integer.parseInt(String.valueOf(obj.get("temp_id")));
                            if (tempId == session.selectedTemplate.id) {
                                int currentQty = Integer.parseInt(String.valueOf(obj.get("quantity")));
                                obj.put("quantity", currentQty + session.quantity);
                                stacked = true;
                                break;
                            }
                        }
                    }

                    if (!stacked) {
                        // Tìm 1 slot trống (temp_id == -1)
                        int emptyIndex = -1;
                        for (int i = 0; i < dataBag.size(); i++) {
                            JSONObject obj = (JSONObject) dataBag.get(i);
                            int tempId = Integer.parseInt(String.valueOf(obj.get("temp_id")));
                            if (tempId == -1) {
                                emptyIndex = i;
                                break;
                            }
                        }

                        if (emptyIndex == -1) {
                            con.rollback();
                            Service.getInstance().sendThongBao(admin, "Túi đồ người chơi đã đầy.");
                            return;
                        }

                        JSONObject slotObj = (JSONObject) dataBag.get(emptyIndex);
                        slotObj.put("temp_id", (int) session.selectedTemplate.id);
                        slotObj.put("quantity", session.quantity);
                        slotObj.put("create_time", System.currentTimeMillis());

                        JSONArray optionsArr = new JSONArray();
                        for (ItemOption io : finalOptions) {
                            JSONArray optPair = new JSONArray();
                            optPair.add(io.optionTemplate.id);
                            optPair.add(io.param);
                            optionsArr.add(optPair);
                        }
                        slotObj.put("option", optionsArr);
                    }

                    // Cập nhật lại vào DB
                    try (PreparedStatement psUpdate = con.prepareStatement("UPDATE player SET items_bag = ? WHERE id = ?")) {
                        psUpdate.setString(1, dataBag.toJSONString());
                        psUpdate.setLong(2, session.targetPlayerId);
                        psUpdate.executeUpdate();
                    }

                    con.commit();
                    Service.getInstance().sendThongBao(admin,
                            "Buff thành công " + session.quantity + "x " + session.selectedTemplate.name + " cho " + session.targetPlayerName + " (OFFLINE)!");

                    logAudit(admin, session.targetPlayerName, session.targetPlayerId, "OFFLINE",
                            session.selectedTemplate.id, session.selectedTemplate.name, session.quantity, finalOptions);
                    removeSession(admin);
                }
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        } catch (Exception e) {
            Log.error(AdminBuffItemService.class, e, "Lỗi buff offline cho player " + session.targetPlayerName);
            Service.getInstance().sendThongBao(admin, "Lỗi khi cập nhật dữ liệu người chơi offline: " + e.getMessage());
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

    /**
     * Helper kiểm tra item có cộng dồn số lượng hay không
     */
    private boolean isItemIncremental(Item item) {
        if (item == null || item.template == null) return false;
        return isIncrementalTemplate(item.template);
    }

    private boolean isIncrementalTemplate(ItemTemplate template) {
        if (template == null) return false;
        if (template.id == 454 || template.id == 921) {
            return false;
        }
        switch (template.type) {
            case 99: // vật phẩm hỗ trợ
            case 8: // vật phẩm nhiệm vụ
            case 12: // ngọc rồng
            case 30: // sao pha lê
            case 6: // đậu thần
            case 29: // item time, đồ ăn
            case 25: // rađa dò ngọc namếc
            case 27: // đồ tạp
            case 33: // mảnh rada
                return true;
            default:
                return false;
        }
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
}
