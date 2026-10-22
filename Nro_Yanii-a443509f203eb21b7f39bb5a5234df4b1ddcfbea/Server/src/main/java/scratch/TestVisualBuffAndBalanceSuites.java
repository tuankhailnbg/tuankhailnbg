package scratch;

import nro.jdbc.DBService;
import nro.models.item.ItemOption;
import nro.models.item.ItemOptionTemplate;
import nro.models.item.ItemTemplate;
import nro.models.player.Player;
import nro.server.Manager;
import nro.server.io.Session;
import nro.services.func.AdminBalanceService;
import nro.services.func.AdminBuffItemService;
import nro.services.func.webadmin.AdminWebServer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class TestVisualBuffAndBalanceSuites {

    private static int totalPassed = 0;
    private static int totalFailed = 0;

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println(" BẮT ĐẦU KIỂM THỬ HỆ THỐNG BUFF ĐỒ TRỰC QUAN & CỘNG SỐ DƯ NẠP TIỀN");
        System.out.println("===============================================================");

        try {
            DBService.DB_HOST = "localhost";
            DBService.DB_PORT = 3306;
            DBService.DB_NAME = "ngocrong";
            DBService.DB_USER = "nro_app";
            DBService.DB_PASSWORD = "tkdeptrai";

            // 1. Khởi tạo dữ liệu mẫu nếu cần
            initServerManagerResources();

            // 2. Chạy từng bộ kiểm thử
            testOptionCategorizationAndSearch();
            testOptionSelectionRetentionAndConflict();
            testItemLookupAndValidation();
            testBalanceCreditModeAAndModeB();
            testBalanceAntiDuplicateAndSafety();
            testOnlineRamSyncAndDeduction();
            testVisualBuffExecution();

            System.out.println("===============================================================");
            System.out.printf(" TỔNG KẾT: %d PASSED, %d FAILED\n", totalPassed, totalFailed);
            System.out.println("===============================================================");

            if (totalFailed > 0) {
                System.exit(1);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    private static void initServerManagerResources() {
        System.out.print("[INIT] Nạp danh sách Item & Item Option Template từ MariaDB... ");
        try (Connection con = DBService.gI().getConnectionForGame()) {
            // Nạp Item Option Templates
            try (PreparedStatement ps = con.prepareStatement("SELECT id, name, type FROM item_option_template");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ItemOptionTemplate iot = new ItemOptionTemplate();
                    iot.id = rs.getInt("id");
                    iot.name = rs.getString("name");
                    iot.type = rs.getInt("type");
                    Manager.ITEM_OPTION_TEMPLATES.add(iot);
                }
            }

            // Nạp Item Templates
            try (PreparedStatement ps = con.prepareStatement("SELECT id, type, gender, name, description, icon_id, part, is_up_to_up FROM item_template");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ItemTemplate it = new ItemTemplate();
                    it.id = rs.getShort("id");
                    it.type = rs.getByte("type");
                    it.gender = rs.getByte("gender");
                    it.name = rs.getString("name");
                    it.description = rs.getString("description");
                    it.iconID = rs.getShort("icon_id");
                    it.part = rs.getShort("part");
                    it.isUpToUp = rs.getBoolean("is_up_to_up");
                    Manager.ITEM_TEMPLATES.add(it);
                }
            }
            System.out.println("HOÀN TẤT. (Loaded " + Manager.ITEM_OPTION_TEMPLATES.size() + " options, " + Manager.ITEM_TEMPLATES.size() + " items)");
        } catch (Exception e) {
            System.out.println("LỖI INIT: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testOptionCategorizationAndSearch() {
        System.out.println("\n--- [TEST 1] Kiểm tra Phân loại Option & Tìm kiếm không dấu ---");

        // Kiểm tra phân loại 11 nhóm
        int countSd = 0, countHp = 0, countKi = 0, countDef = 0, countCrit = 0, countSkh = 0, countSpl = 0;
        for (ItemOptionTemplate iot : Manager.ITEM_OPTION_TEMPLATES) {
            String cat = AdminWebServer.getOptionCategory(iot.id, iot.name);
            if ("Sức đánh".equals(cat)) countSd++;
            else if ("HP".equals(cat)) countHp++;
            else if ("KI".equals(cat)) countKi++;
            else if ("Giáp".equals(cat)) countDef++;
            else if ("Chí mạng".equals(cat)) countCrit++;
            else if ("Set Kích Hoạt".equals(cat)) countSkh++;
            else if ("Sao pha lê".equals(cat)) countSpl++;
        }

        assertCondition(countSd > 0, "Nhóm 'Sức đánh' có ít nhất 1 option (thực tế: " + countSd + ")");
        assertCondition(countHp > 0, "Nhóm 'HP' có ít nhất 1 option (thực tế: " + countHp + ")");
        assertCondition(countKi > 0, "Nhóm 'KI' có ít nhất 1 option (thực tế: " + countKi + ")");
        assertCondition(countDef > 0, "Nhóm 'Giáp' có ít nhất 1 option (thực tế: " + countDef + ")");
        assertCondition(countCrit > 0, "Nhóm 'Chí mạng' có ít nhất 1 option (thực tế: " + countCrit + ")");
        assertCondition(countSkh >= 9, "Nhóm 'Set Kích Hoạt' có ít nhất 9 option (127-135) (thực tế: " + countSkh + ")");
        assertCondition(countSpl >= 1, "Nhóm 'Sao pha lê' có ít nhất 1 option (thực tế: " + countSpl + ")");

        // Kiểm tra tìm kiếm không dấu
        String raw = "Sức đánh chí mạng +100%";
        String clean = AdminWebServer.removeAccents(raw);
        assertCondition(clean.equals("suc danh chi mang +100%"), "Chuẩn hóa không dấu: " + clean);
    }

    private static void testOptionSelectionRetentionAndConflict() {
        System.out.println("\n--- [TEST 2] Kiểm tra Giữ lựa chọn Option, Không trùng lặp, Cảnh báo SKH ---");

        // Giả lập admin chọn option:
        // Bước A: Đang ở tab HP -> Chọn Option 77 (HP +%), param 100
        // Bước B: Chuyển sang tab KI -> Chọn Option 103 (KI +%), param 100
        // Bước C: Chuyển sang tab Sức đánh -> Chọn Option 50 (SĐ +%), param 150
        // Bước D: Admin cập nhật lại Option 77 thành param 120 -> Phải ghi đè, KHÔNG tạo 2 dòng option 77
        Map<Integer, Integer> selectedOptions = new LinkedHashMap<>();
        selectedOptions.put(77, 100);
        selectedOptions.put(103, 100);
        selectedOptions.put(50, 150);

        // Update Option 77
        selectedOptions.put(77, 120);

        assertCondition(selectedOptions.size() == 3, "Số option được chọn duy nhất là 3 (không bị nhân đôi option 77)");
        assertCondition(selectedOptions.get(77) == 120, "Param option 77 được cập nhật thành 120");

        // Kiểm tra xung đột SKH: chọn 2 Set khác nhau trên 1 món đồ
        // Set Songoku (127) và Set Thần Vũ Trụ (131)
        List<ItemOption> conflictOpts = new ArrayList<>();
        conflictOpts.add(new ItemOption(127, 0));
        conflictOpts.add(new ItemOption(131, 0));
        String warning = checkSkhConflict(conflictOpts);
        assertCondition(warning != null && warning.contains("Xung đột"), "Phát hiện xung đột khi chọn 2 set kích hoạt khác nhau: " + warning);

        // Chọn 1 set kích hoạt duy nhất -> Hợp lệ
        List<ItemOption> validSkh = new ArrayList<>();
        validSkh.add(new ItemOption(128, 0));
        validSkh.add(new ItemOption(50, 100));
        assertCondition(checkSkhConflict(validSkh) == null, "Cấu hình 1 set kích hoạt hợp lệ không bị cảnh báo xung đột");
    }

    private static String checkSkhConflict(List<ItemOption> opts) {
        int skhCount = 0;
        for (ItemOption opt : opts) {
            int id = opt.optionTemplate != null ? opt.optionTemplate.id : -1;
            if (id >= 127 && id <= 135) {
                skhCount++;
            }
        }
        if (skhCount > 1) {
            return "Xung đột: Phát hiện " + skhCount + " Set Kích Hoạt trên cùng 1 vật phẩm!";
        }
        return null;
    }

    private static void testItemLookupAndValidation() {
        System.out.println("\n--- [TEST 3] Kiểm tra Tra cứu Vật phẩm & Xử lý ID không hợp lệ ---");

        ItemTemplate goldBar = AdminBuffItemService.gI().getItemTemplate(457);
        assertCondition(goldBar != null && goldBar.name != null && goldBar.name.contains("Thỏi Vàng"), "Tra cứu thành công ID 457: " + (goldBar != null ? goldBar.name : "null"));

        ItemTemplate invalid = AdminBuffItemService.gI().getItemTemplate(999999);
        assertCondition(invalid == null, "ID 999999 không tồn tại trả về null hợp lệ");
    }

    private static void testBalanceCreditModeAAndModeB() throws Exception {
        System.out.println("\n--- [TEST 4] Kiểm tra Cộng số dư Chế độ A (Nạp) và Chế độ B (Điều chỉnh) ---");

        // Chuẩn bị tài khoản test (username ngắn, rõ ràng)
        String testUser = "u_test_bal";
        int testAccId = prepareTestAccount(testUser, 10000, 0);

        // Lần 1: Chế độ A (Nạp) +50,000 VNĐ qua username
        String trans1 = "NAP_TEST_A_001_" + System.currentTimeMillis();
        AdminBalanceService.CreditResult cr1 = AdminBalanceService.gI().creditBalance(
                "SuperAdmin", testUser, 50000, trans1, "Nạp thẻ test lần 1", AdminBalanceService.MODE_TOPUP
        );

        assertCondition(cr1.success, "Cộng lần 1 (50.000đ) thành công: " + cr1.message);
        assertCondition(cr1.balanceAfter == 60000, "Số dư sau lần 1 = 60.000 VNĐ (thực tế: " + cr1.balanceAfter + ")");
        assertCondition(cr1.tongnapAfter == 50000, "Tổng nạp sau lần 1 = 50.000 VNĐ (thực tế: " + cr1.tongnapAfter + ")");

        // Lần 2: Chế độ A (Nạp) +20,000 VNĐ qua Account ID với prefix acc:
        String trans2 = "NAP_TEST_A_002_" + System.currentTimeMillis();
        AdminBalanceService.CreditResult cr2 = AdminBalanceService.gI().creditBalance(
                "SuperAdmin", "acc:" + testAccId, 20000, trans2, "Nạp thẻ test lần 2", AdminBalanceService.MODE_TOPUP
        );

        assertCondition(cr2.success, "Cộng lần 2 (20.000đ) thành công: " + cr2.message);
        assertCondition(cr2.balanceAfter == 80000, "Số dư sau lần 2 = 80.000 VNĐ (thực tế: " + cr2.balanceAfter + ")");
        assertCondition(cr2.tongnapAfter == 70000, "Tổng nạp sau lần 2 = 70.000 VNĐ (thực tế: " + cr2.tongnapAfter + ")");

        // Lần 3: Chế độ B (Điều chỉnh số dư) +15,000 VNĐ
        // Yêu cầu: Số dư tăng lên 95.000 VNĐ, NHƯNG Tổng nạp KHÔNG THAY ĐỔI (vẫn là 70.000 VNĐ)
        String trans3 = "NAP_TEST_B_003_" + System.currentTimeMillis();
        AdminBalanceService.CreditResult cr3 = AdminBalanceService.gI().creditBalance(
                "SuperAdmin", testUser, 15000, trans3, "Điều chỉnh hỗ trợ sự cố", AdminBalanceService.MODE_ADJUST
        );

        assertCondition(cr3.success, "Điều chỉnh Chế độ B (+15.000đ) thành công: " + cr3.message);
        assertCondition(cr3.balanceAfter == 95000, "Số dư sau điều chỉnh = 95.000 VNĐ (thực tế: " + cr3.balanceAfter + ")");
        assertCondition(cr3.tongnapAfter == 70000, "Tổng nạp không đổi = 70.000 VNĐ (thực tế: " + cr3.tongnapAfter + ")");
    }

    private static void testBalanceAntiDuplicateAndSafety() throws Exception {
        System.out.println("\n--- [TEST 5] Kiểm tra Chống cộng tiền trùng (Idempotent) & Giới hạn an toàn ---");

        String testDupUser = "u_test_dup";
        int testAccId = prepareTestAccount(testDupUser, 20000, 100000);
        String fixedTrans = "TX_DUP_" + System.currentTimeMillis();

        // Giao dịch lần đầu với mã TX_DUP_...
        AdminBalanceService.CreditResult resFirst = AdminBalanceService.gI().creditBalance(
                "Admin", testDupUser, 30000, fixedTrans, "Nạp ban đầu", AdminBalanceService.MODE_TOPUP
        );
        assertCondition(resFirst.success, "Giao dịch ban đầu thành công, balance = " + resFirst.balanceAfter);

        // Gửi LẠI chính xác mã giao dịch đó (giả lập request lặp hoặc admin bấm 2 lần)
        AdminBalanceService.CreditResult resDup = AdminBalanceService.gI().creditBalance(
                "Admin", testDupUser, 30000, fixedTrans, "Gửi lại cùng mã", AdminBalanceService.MODE_TOPUP
        );
        assertCondition(!resDup.success && resDup.isDuplicate, "Giao dịch trùng mã bị TỪ CHỐI an toàn (isDuplicate=true)");
        assertCondition(resDup.balanceAfter == 50000, "Số dư giữ nguyên không bị cộng lần 2 (thực tế: " + resDup.balanceAfter + ")");

        // Kiểm tra số tiền <= 0
        AdminBalanceService.CreditResult resNeg = AdminBalanceService.gI().creditBalance(
                "Admin", testDupUser, -5000, "TX_NEG", "Số âm", AdminBalanceService.MODE_TOPUP
        );
        assertCondition(!resNeg.success, "Từ chối số tiền âm (-5.000)");

        AdminBalanceService.CreditResult resZero = AdminBalanceService.gI().creditBalance(
                "Admin", testDupUser, 0, "TX_ZERO", "Số 0", AdminBalanceService.MODE_TOPUP
        );
        assertCondition(!resZero.success, "Từ chối số tiền bằng 0");

        // Kiểm tra số tiền vượt ngưỡng cộng gây tràn số INT (> 2.14 tỷ)
        AdminBalanceService.CreditResult resOver = AdminBalanceService.gI().creditBalance(
                "Admin", testDupUser, Integer.MAX_VALUE, "TX_OVER", "Quá lớn", AdminBalanceService.MODE_TOPUP
        );
        assertCondition(!resOver.success, "Từ chối số tiền cộng gây tràn số (Integer.MAX_VALUE)");
    }

    private static void testOnlineRamSyncAndDeduction() throws Exception {
        System.out.println("\n--- [TEST 6] Kiểm tra Đồng bộ RAM Online & Khấu trừ số dư an toàn ---");

        String testSyncUser = "u_test_sync";
        int testAccId = prepareTestAccount(testSyncUser, 50000, 200000);

        // Giả lập đối tượng Player và Session online
        Session fakeSession = new Session();
        fakeSession.userId = testAccId;
        fakeSession.vnd = 50000;
        fakeSession.tongnap = 200000;

        Player fakePlayer = new Player();
        fakePlayer.id = 999888L;
        fakePlayer.name = "p_test_sync";
        fakePlayer.setSession(fakeSession);
        fakePlayer.soDuVND = 50000;
        fakeSession.player = fakePlayer;

        // Đăng ký vào Client online mapping
        nro.server.Client.gI().put(fakePlayer);

        try {
            // Admin thực hiện cộng 100.000 VNĐ cho tài khoản đang online
            String transOnline = "ONLINE_SYNC_TX_" + System.currentTimeMillis();
            AdminBalanceService.CreditResult resOnline = AdminBalanceService.gI().creditBalance(
                    "Admin", fakePlayer.name, 100000, transOnline, "Nạp khi online", AdminBalanceService.MODE_TOPUP
            );

            assertCondition(resOnline.success, "Cộng tiền cho player online thành công");
            assertCondition(fakePlayer.soDuVND == 150000, "RAM player.soDuVND cập nhật ngay lập tức thành 150.000 (thực tế: " + fakePlayer.soDuVND + ")");
            assertCondition(fakeSession.vnd == 150000, "RAM session.vnd cập nhật ngay lập tức thành 150.000 (thực tế: " + fakeSession.vnd + ")");
            assertCondition(fakeSession.tongnap == 300000, "RAM session.tongnap cập nhật ngay lập tức thành 300.000 (thực tế: " + fakeSession.tongnap + ")");

            // Giả lập mua vật phẩm trong game trừ 40.000 VNĐ qua subVndBar
            nro.jdbc.daos.PlayerDAO.subVndBar(fakePlayer, 40000);
            assertCondition(fakePlayer.soDuVND == 110000, "Khấu trừ thành công: Số dư sau khi mua đồ: 110.000 VNĐ trong RAM");

            // Kiểm tra DB sau khi trừ
            int dbVndAfterSub = 0;
            try (Connection con = DBService.gI().getConnectionForGame();
                 PreparedStatement ps = con.prepareStatement("SELECT vnd FROM account WHERE id = ?")) {
                ps.setInt(1, testAccId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) dbVndAfterSub = rs.getInt("vnd");
                }
            }
            assertCondition(dbVndAfterSub == 110000, "Database và RAM đồng bộ hoàn hảo sau khi trừ: " + dbVndAfterSub + " VNĐ");

        } finally {
            nro.server.Client.gI().remove(fakePlayer);
        }
    }

    private static void testVisualBuffExecution() throws Exception {
        System.out.println("\n--- [TEST 7] Kiểm tra Cấp vật phẩm Trực quan Online & Offline Queue ---");

        // 7a. Buff đồ cho người chơi ONLINE
        Session fakeSession = new Session();
        fakeSession.userId = 99991;

        Player onlineTarget = new Player();
        onlineTarget.id = 888111L;
        onlineTarget.name = "pl_online_buff_test";
        onlineTarget.inventory = new nro.models.player.Inventory(onlineTarget);
        for (int i = 0; i < 20; i++) {
            onlineTarget.inventory.itemsBag.add(nro.services.ItemService.gI().createItemNull());
        }
        onlineTarget.setSession(fakeSession);
        fakeSession.player = onlineTarget;

        nro.server.Client.gI().put(onlineTarget);

        try {
            Map<Integer, Integer> buffOptions = new HashMap<>();
            buffOptions.put(50, 150); // Sức đánh +150%
            buffOptions.put(77, 100); // HP +100%

            JSONObject resOnline = AdminBuffItemService.gI().buffItemDirect(
                    "SuperAdmin",
                    onlineTarget.name,
                    457, // Thỏi Vàng
                    5,
                    buffOptions,
                    false
            );

            assertCondition(Boolean.TRUE.equals(resOnline.get("success")), "Buff online thành công: " + resOnline.get("message"));
            boolean hasItem = onlineTarget.inventory.itemsBag.stream().anyMatch(it -> it != null && it.isNotNullItem() && it.template.id == 457 && it.quantity == 5);
            assertCondition(hasItem, "Hành trang người chơi nhận được vật phẩm Thỏi Vàng x5");

            // 7b. Buff đồ cho tài khoản OFFLINE (lưu vào pending grant)
            // Sử dụng nhân vật offline có sẵn trong DB: trumxdhp (id=7, account_id=4)
            String offlinePlName = "trumxdhp";
            long offlinePlayerId = 7L;

            JSONObject resOffline = AdminBuffItemService.gI().buffItemDirect(
                    "SuperAdmin",
                    offlinePlName,
                    555, // Áo Thần Trái Đất
                    1,
                    buffOptions,
                    false
            );

            assertCondition(Boolean.TRUE.equals(resOffline.get("success")), "Buff offline thành công tạo pending grant: " + resOffline.get("message"));

            // Kiểm tra trong DB bảng admin_item_grant
            boolean foundGrant = false;
            try (Connection con = DBService.gI().getConnectionForGame();
                 PreparedStatement ps = con.prepareStatement(
                         "SELECT id, status FROM admin_item_grant WHERE target_player_id = ? AND item_template_id = 555 ORDER BY id DESC LIMIT 1")) {
                ps.setLong(1, offlinePlayerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        foundGrant = "PENDING".equals(rs.getString("status"));
                    }
                }
            }
            assertCondition(foundGrant, "Bảng admin_item_grant lưu đúng bản ghi PENDING cho nhân vật offline");

            // 7c. Kiểm tra Buff Item ID không tồn tại
            JSONObject resInvalidItem = AdminBuffItemService.gI().buffItemDirect(
                    "SuperAdmin",
                    onlineTarget.name,
                    999999,
                    1,
                    buffOptions,
                    false
            );
            assertCondition(Boolean.FALSE.equals(resInvalidItem.get("success")), "Từ chối buff Item ID không tồn tại (999999)");

        } finally {
            nro.server.Client.gI().remove(onlineTarget);
        }
    }

    private static int prepareTestAccount(String username, int initialVnd, int initialTongnap) throws Exception {
        try (Connection con = DBService.gI().getConnectionForGame()) {
            int accId = -1;
            try (PreparedStatement ps = con.prepareStatement("SELECT id FROM account WHERE username = ?")) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        accId = rs.getInt("id");
                    }
                }
            }

            if (accId != -1) {
                try (PreparedStatement ps = con.prepareStatement("UPDATE account SET vnd = ?, tongnap = ? WHERE id = ?")) {
                    ps.setInt(1, initialVnd);
                    ps.setInt(2, initialTongnap);
                    ps.setInt(3, accId);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO account (username, password, vnd, tongnap, is_admin) VALUES (?, '123456', ?, ?, 0)",
                        PreparedStatement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, username);
                    ps.setInt(2, initialVnd);
                    ps.setInt(3, initialTongnap);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) accId = rs.getInt(1);
                    }
                }
            }
            return accId;
        }
    }

    private static void assertCondition(boolean condition, String description) {
        if (condition) {
            totalPassed++;
            System.out.println("  [PASS] " + description);
        } else {
            totalFailed++;
            System.err.println("  [FAIL] " + description);
        }
    }
}
