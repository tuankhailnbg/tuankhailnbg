package nro.services.func;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import nro.consts.ConstNpc;
import nro.jdbc.daos.PlayerDAO;
import nro.models.item.Item;
import nro.models.map.Zone;
import nro.models.npc.Npc;
import nro.models.npc.NpcManager;
import nro.models.player.Player;
import nro.server.Client;
import nro.server.io.Message;
import nro.services.*;
import nro.services.*;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import nro.consts.ConstItem;
import nro.jdbc.DBService;
import nro.models.item.ItemOption;
import nro.models.player.Inventory;
import nro.utils.Util;

/**
 * @author 💖 Nro Yanii 💖
 *
 */
public class Input {

    private static final Map<Integer, Object> PLAYER_ID_OBJECT = new HashMap<Integer, Object>();

    public static final int CHANGE_PASSWORD = 500;
    public static final int GIFT_CODE = 501;
    public static final int FIND_PLAYER = 502;
    public static final int CHANGE_NAME = 503;
    public static final int CHOOSE_LEVEL_BDKB = 5066;
    public static final int CHOOSE_LEVEL_KGHD = 5088;
    public static final int CHOOSE_LEVEL_CDRD = 7700;
    public static final int TANG_NGOC_HONG = 505;
    public static final int ADD_ITEM = 506;
    public static final int GIAI_TAN_BANG = 509;
    public static final int DOI_THOI_VANG = 510;
    public static final int BAN_THOI_VANG = 511;
    public static final int SEND_ITEM_OP = 512;
    public static final int TANG_SKH = 513;
    public static final int DOI_HONG_NGOC = 514;
    public static final int BUFF_FIND_PLAYER = 520;
    public static final int BUFF_SEARCH_ITEM = 521;
    public static final int BUFF_INPUT_QUANTITY = 522;
    public static final int BUFF_INPUT_OPTION = 523;
    public static final int BUFF_INPUT_OPTION_STRING = 524;
    public static final int BUFF_INPUT_PARAM = 525;
    public static final int BUFF_STEP_1 = 528;
    public static final int ADMIN_CREDIT_FORM = 529;
    // Form ID 530 đã bỏ; giữ nguyên ID form cấp điểm admin.
    public static final int ADMIN_REFERRAL_POINTS = 531;
    public static final byte NUMERIC = 0;
    public static final byte ANY = 1;
    public static final byte PASSWORD = 2;
    private static Input intance;

    private Input() {

    }

    public static Input gI() {
        if (intance == null) {
            intance = new Input();
        }
        return intance;
    }

    public void doInput(Player player, Message msg) {
        try {
            Player pl = null;
            String[] text = new String[msg.reader().readByte()];
            for (int i = 0; i < text.length; i++) {
                text[i] = msg.reader().readUTF();
            }
            switch (player.iDMark.getTypeInput()) {
                case SEND_ITEM_OP:
                    if (player.isAdmin()) {
                        AdminBuffItemService.gI().openMenuBuffItem(player);
                    }
                    break;
                case BUFF_FIND_PLAYER:
                    if (player.isAdmin()) {
                        AdminBuffItemService.gI().searchPlayer(player, text[0]);
                    }
                    break;
                case BUFF_SEARCH_ITEM:
                    if (player.isAdmin()) {
                        AdminBuffItemService.gI().searchItem(player, text[0]);
                    }
                    break;
                case BUFF_INPUT_QUANTITY:
                    if (player.isAdmin()) {
                        AdminBuffItemService.gI().setQuantity(player, text[0]);
                    }
                    break;
                case BUFF_INPUT_OPTION:
                    if (player.isAdmin()) {
                        AdminBuffItemService.gI().handleSearchOptionInput(player, text[0], text.length > 1 ? text[1] : "");
                    }
                    break;
                case BUFF_INPUT_OPTION_STRING:
                    if (player.isAdmin()) {
                        AdminBuffItemService.gI().handleOptionStringInput(player, text[0]);
                    }
                    break;
                case BUFF_INPUT_PARAM:
                    if (player.isAdmin()) {
                        AdminBuffItemService.gI().handleParamInput(player, text[0]);
                    }
                    break;
                case BUFF_STEP_1:
                    if (player.isAdmin()) {
                        try {
                            int itemId = Integer.parseInt(text[0].trim());
                            int qty = (text.length > 1 && !text[1].trim().isEmpty()) ? Integer.parseInt(text[1].trim()) : 1;
                            String recipient = text.length > 2 ? text[2].trim() : "";
                            AdminBuffItemService.gI().handleStep1Input(player, itemId, qty, recipient);
                        } catch (NumberFormatException e) {
                            Service.getInstance().sendThongBao(player, "ID vật phẩm hoặc số lượng phải là số!");
                        }
                    }
                    break;
                case ADMIN_CREDIT_FORM:
                    if (player.isAdmin()) {
                        try {
                            String recipient = text != null && text.length > 0 ? text[0].trim() : "";
                            int amount = Integer.parseInt(text != null && text.length > 1 ? text[1].trim() : "");
                            String transCode = AdminBalanceService.gI().generateDefaultTransCode();
                            AdminBuffItemService.gI().handleAdminCreditFormInput(player, recipient, amount,
                                    transCode, "Admin cộng số dư từ game");
                        } catch (NumberFormatException e) {
                            Service.getInstance().sendThongBao(player, "Số tiền nạp phải là số hợp lệ!");
                        }
                    }
                    break;

                case CHANGE_PASSWORD:
                    Service.getInstance().changePassword(player, text[0], text[1], text[2]);
                    break;
                case GIFT_CODE:
                    GiftService.gI().use(player, text[0]);
                    break;
                case ADMIN_REFERRAL_POINTS:
                    if (player != null && player.getSession() != null && player.isAdmin()) {
                        Service.gI().sendThongBaoOK(player,
                                nro.services.ReferralService.gI().adminGrantPoints(player,
                                        text.length>0?text[0]:"",
                                        text.length>1?text[1]:"",
                                        text.length>2?text[2]:""));
                    }
                    break;
                case FIND_PLAYER:
                    pl = Client.gI().getPlayer(text[0]);
                    if (pl != null) {
                        NpcService.gI().createMenuConMeo(player, ConstNpc.MENU_FIND_PLAYER, -1, "Ngài muốn..?",
                                new String[] { "Đi tới\n" + pl.name, "Gọi " + pl.name + "\ntới đây", "Đổi tên", "Ban" },
                                pl);
                    } else {
                        Service.getInstance().sendThongBao(player, "Người chơi không tồn tại hoặc đang offline");
                    }
                    break;
                case DOI_THOI_VANG:
                    try {
                        // Người chơi nhập số K muốn đổi.
                        // Ví dụ: nhập 10 = 10.000 VNĐ = 1.000 Thỏi Vàng.
                        int soK = Integer.parseInt(text[0].trim());
                        if (soK <= 0) {
                            Service.getInstance().sendThongBao(player, "Số K muốn đổi không hợp lệ");
                            return;
                        }

                        long tongTienLong = (long) soK * 1_000L;
                        long soVangLong = (long) soK * 100L;
                        if (tongTienLong > Integer.MAX_VALUE || soVangLong > Integer.MAX_VALUE) {
                            Service.getInstance().sendThongBao(player, "Số lượng muốn đổi quá lớn");
                            return;
                        }

                        int tongTien = (int) tongTienLong;
                        int soVang = (int) soVangLong;

                        if (player.soDuVND < tongTien) {
                            Service.getInstance().sendThongBao(player,
                                    "Bạn không đủ số dư. Cần " + Util.numberToMoney(tongTien) + " VNĐ");
                            return;
                        }
                        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
                            Service.getInstance().sendThongBao(player, "Cần 1 ô trống trong hành trang");
                            return;
                        }

                        Item thoivang = ItemService.gI().createNewConsignmentItem(
                                (short) ConstItem.THOI_VANG, soVang);
                        thoivang.itemOptions.add(new ItemOption(73, 0));
                        InventoryService.gI().addItemBag(player, thoivang, 99999);

                        // subVndBar đã tự trừ player.soDuVND và đồng bộ session,
                        // không được trừ player.soDuVND thêm lần nữa.
                        PlayerDAO.subVndBar(player, tongTien);

                        InventoryService.gI().sendItemBags(player);
                        Service.getInstance().sendMoney(player);
                        Service.getInstance().sendThongBao(player,
                                "Đã đổi " + Util.numberToMoney(tongTien) + " VNĐ nhận "
                                        + Util.numberToMoney(soVang) + " Thỏi Vàng.\n"
                                        + "Số dư còn lại: " + Util.numberToMoney(player.soDuVND) + " VNĐ");
                    } catch (NumberFormatException e) {
                        Service.getInstance().sendThongBao(player, "Định dạng số không hợp lệ");
                    }
                    break;

                case DOI_HONG_NGOC:
                    try {
                        int soLuong = Integer.parseInt(text[0]);
                        if (soLuong <= 0) {
                            Service.getInstance().sendThongBao(player, "Số Hồng Ngọc không hợp lệ");
                            return;
                        }
                        if (soLuong > 1_000_000) {
                            Service.getInstance().sendThongBao(player, "Số lượng quá lớn");
                            return;
                        }
                        int tongTien = soLuong;
                        if (player.soDuVND < tongTien) {
                            Service.getInstance().sendThongBao(player, "Bạn không đủ số Dư");
                            return;
                        }
                        player.inventory.ruby += tongTien;

                        // subVndBar đã tự trừ player.soDuVND và đồng bộ session,
                        // không được trừ player.soDuVND thêm lần nữa.
                        PlayerDAO.subVndBar(player, tongTien);

                        InventoryService.gI().sendItemBags(player);
                        Service.getInstance().sendMoney(player);

                        Service.getInstance().sendThongBao(player,
                                "Bạn nhận được " + Util.numberToMoney(tongTien) + " Hồng Ngọc.\n"
                                        + "Số dư còn lại: " + Util.numberToMoney(player.soDuVND) + " VNĐ");
                    } catch (NumberFormatException e) {
                        Service.getInstance().sendThongBao(player, "Định dạng số không hợp lệ");
                    }

                    break;
                case CHANGE_NAME:
                    if (player.soDuVND >= 50000) {
                        Player plChanged = (Player) PLAYER_ID_OBJECT.get((int) player.id);
                        if (plChanged != null) {
                            // Kiểm tra xem tên mới có bị trùng không
                            if (PlayerDAO.isExistName(text[0])) {
                                Service.getInstance().sendThongBao(player, "Tên nhân vật đã tồn tại");
                            } else {
                                player.soDuVND -= 50000;
                                // Cập nhật tên người chơi
                                plChanged.name = text[0];
                                PlayerDAO.saveName(plChanged);
                                Service.getInstance().player(plChanged);
                                Service.getInstance().Send_Caitrang(plChanged);
                                Service.getInstance().sendFlagBag(plChanged);

                                // Chuyển đổi bản đồ của người chơi
                                Zone zone = plChanged.zone;
                                ChangeMapService.gI().changeMap(plChanged, zone, plChanged.location.x,
                                        plChanged.location.y);

                                // Gửi thông báo thành công
                                Service.getInstance().sendThongBao(plChanged,
                                        "Chúc mừng bạn đã có cái tên mới đẹp đẽ hơn tên ban đầu");
                                Service.getInstance().sendThongBao(player, "Đổi tên người chơi thành công");
                            }
                        }
                    } else {
                        // Thông báo không đủ số dư
                        Service.getInstance().sendThongBao(player, "Số dư tài khoản của con không đử");
                    }
                    break;
                case CHOOSE_LEVEL_BDKB: {
                    int level = Integer.parseInt(text[0]);
                    if (level == 110) {
                        Npc npc = NpcManager.getByIdAndMap(ConstNpc.QUY_LAO_KAME, player.zone.map.mapId);
                        if (npc != null) {
                            npc.createOtherMenu(player, ConstNpc.MENU_ACCEPT_GO_TO_BDKB,
                                    "Con có chắc muốn đến\nhang kho báu cấp độ " + level + "?",
                                    new String[] { "Đồng ý", "Từ chối" }, level);
                        }
                    } else {
                        Service.getInstance().sendThongBao(player, "Chỉ có thể mở cấp độ 110");
                    }
                }
                    break;
                case CHOOSE_LEVEL_KGHD: {
                    int level = Integer.parseInt(text[0]);
                    if (level >= 1 && level <= 110) {
                        Npc npc = NpcManager.getByIdAndMap(ConstNpc.MR_POPO, player.zone.map.mapId);
                        if (npc != null) {
                            npc.createOtherMenu(player, ConstNpc.MENU_ACCEPT_GO_TO_KGHD,
                                    "Cậu có chắc muốn đến\nDestron Gas cấp độ " + level + "?",
                                    new String[] { "Đồng ý", "Từ chối" }, level);
                        }
                    } else {
                        Service.getInstance().sendThongBao(player, "Không thể thực hiện");
                    }
                }
                    break;
                case CHOOSE_LEVEL_CDRD: {
                    int level = Integer.parseInt(text[0]);
                    if (level >= 1 && level <= 110) {
                        Npc npc = NpcManager.getByIdAndMap(ConstNpc.THAN_VU_TRU, player.zone.map.mapId);
                        if (npc != null) {
                            npc.createOtherMenu(player, ConstNpc.MENU_ACCEPT_GO_TO_CDRD,
                                    "Con có chắc chắn muốn đến con đường rắn độc cấp độ " + level + "?",
                                    new String[] { "Đồng ý", "Từ chối" }, level);
                        }
                    } else {
                        Service.getInstance().sendThongBao(player, "Không thể thực hiện");
                    }
                }
                    break;

                case BAN_THOI_VANG:
                    long soLuong = Long.parseLong(text[0]);
                    if (soLuong <= 0) {
                        return;
                    }
                    Item thoiVang = InventoryService.gI().findItemBagByTemp(player, (short) 457);
                    if (thoiVang == null || soLuong > thoiVang.quantity) {
                        Service.getInstance().sendThongBao(player, "Không đủ thỏi vàng để bán");
                        return;
                    }
                    long goldNhanDuoc;
                    try {
                        goldNhanDuoc = Math.multiplyExact(soLuong, 25_000_000L);
                    } catch (ArithmeticException e) {
                        Service.getInstance().sendThongBao(player, "Số lượng quá lớn");
                        return;
                    }
                    if (player.inventory.gold + goldNhanDuoc > player.inventory.getGoldLimit()) {
                        Service.getInstance().sendThongBao(player,
                                "Số vàng sau khi bán vượt quá số vàng có thể lưu trữ");
                        return;
                    }
                    player.inventory.addGold(goldNhanDuoc);
                    InventoryService.gI().subQuantityItemsBag(player, thoiVang, (int) soLuong);
                    InventoryService.gI().sendItemBags(player);
                    Service.getInstance().sendMoney(player);
                    Service.getInstance().sendThongBao(player, "Đã bán " + soLuong + " " + thoiVang.getName()
                            + " thu được " + Util.numberToMoney(goldNhanDuoc) + " vàng");
                    break;
                case GIAI_TAN_BANG:
                    if (text[0] != null) {
                        String OK = text[0].toString();
                        if ("OK".equalsIgnoreCase(OK)) {
                            ClanService.gI().RemoveClanAll(player);
                        }
                    }
                    break;
                case TANG_NGOC_HONG:
                    pl = Client.gI().getPlayer(text[0]);
                    int numruby = Integer.parseInt((text[1]));
                    if (pl == null) {
                        Service.getInstance().sendThongBao(player, "Người chơi không tồn tại hoặc đang offline");
                        return;
                    }
                    if (numruby <= 0) {
                        return;
                    }
                    Item veTangNgoc = InventoryService.gI().findVeTangNgoc(player);
                    if (veTangNgoc == null) {
                        Service.getInstance().sendThongBao(player, "Cần vé tặng ngọc");
                        return;
                    }
                    if (!player.inventory.hasRuby(numruby)) {
                        Service.getInstance().sendThongBao(player, "Không đủ Hồng ngọc để tặng");
                        return;
                    }
                    if (!player.inventory.trySpendRuby(numruby)) {
                        Service.getInstance().sendThongBao(player, "Không đủ Hồng ngọc để tặng");
                        return;
                    }
                    pl.inventory.addRuby(numruby);
                    InventoryService.gI().subQuantityItemsBag(player, veTangNgoc, 1);
                    InventoryService.gI().sendItemBags(player);
                    PlayerService.gI().sendInfoHpMpMoney(player);
                    PlayerService.gI().sendInfoHpMpMoney(pl);
                    Service.getInstance().sendThongBao(player, "Tặng Hồng ngọc thành công");
                    Service.getInstance().sendThongBao(pl,
                            "Bạn được " + player.name + " tặng " + numruby + " Hồng ngọc");
                    break;

                case TANG_SKH:
                    pl = Client.gI().getPlayer(text[0]);
                    String skh = (text[1]);
                    int mon = Integer.parseInt((text[2]));
                    int gender = 0;
                    int[] idOption = null;

                    if (skh.equalsIgnoreCase("sgk")) {
                        gender = 0;
                        idOption = RewardService.ACTIVATION_SET[gender][0];
                    } else if (skh.equalsIgnoreCase("lienhoan") || skh.equalsIgnoreCase("ki")) {
                        gender = 1;
                        if (skh.equalsIgnoreCase("lienhoan")) {
                            idOption = RewardService.ACTIVATION_SET[gender][0];
                        } else if (skh.equalsIgnoreCase("ki")) {
                            idOption = RewardService.ACTIVATION_SET[gender][2];
                        }
                    } else if (skh.equalsIgnoreCase("hp") || skh.equals("galick")) {
                        gender = 2;
                        if (skh.equalsIgnoreCase("hp")) {
                            idOption = RewardService.ACTIVATION_SET[gender][0];
                        } else if (skh.equalsIgnoreCase("galick")) {
                            idOption = RewardService.ACTIVATION_SET[gender][1];
                        }
                    }

                    Item itemSKH = ItemService.gI()
                            .createNewItem((short) ConstItem.LIST_ITEM_CLOTHES[gender][mon][0]);
                    RewardService.gI().initBaseOptionClothes(itemSKH.template.id, itemSKH.template.type,
                            itemSKH.itemOptions);
                    itemSKH.itemOptions.add(new ItemOption(idOption[0], 1)); // tên set
                    itemSKH.itemOptions.add(new ItemOption(idOption[1], 100)); // hiệu ứng set
                    itemSKH.itemOptions.add(new ItemOption(30, 0)); // không thể giao dịch
                    itemSKH.itemOptions.add(new ItemOption(73, 0));

                    InventoryService.gI().addItemBag(pl, itemSKH, 0);
                    InventoryService.gI().sendItemBags(pl);
                    Service.getInstance().sendThongBao(pl,
                            "Bạn nhận được đồ kích hoạt " + itemSKH.template.name);
                    Service.getInstance().sendThongBao(player,
                            "Tặng thành công SKH " + skh);
                    break;

                case ADD_ITEM:
                    short id = Short.parseShort((text[0]));
                    int quantity = Integer.parseInt(text[1]);

                    if (player != null) {
                        if (id < 555 || (id > 568 && id < 650) || id > 662) {
                            Item item = ItemService.gI().createNewItem(((short) id));
                            if (id == 381 || id == 382 || id == 383) {
                                item.quantity = quantity;
                            } else {
                                item.quantity = 1;
                            }
                            InventoryService.gI().addItemBag(player, item, 0);
                            InventoryService.gI().sendItemBags(player);
                            Service.getInstance().sendThongBao(player,
                                    "Bạn nhận được " + item.template.name + " Số lượng: " + quantity);
                        } else {
                            Service.gI().sendThongBaoOK(player, "Vui lòng nhập lại");
                        }
                    }
            }
        } catch (Exception e) {
        }
    }

    public void createForm(Player pl, int typeInput, String title, SubInput... subInputs) {
        pl.iDMark.setTypeInput(typeInput);
        Message msg;
        try {
            msg = new Message(-125);
            msg.writer().writeUTF(title);
            msg.writer().writeByte(subInputs.length);
            for (SubInput si : subInputs) {
                msg.writer().writeUTF(si.name);
                msg.writer().writeByte(si.typeInput);
            }
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void createFormSenditem1(Player pl) {
        AdminBuffItemService.gI().openMenuBuffItem(pl);
    }

    public void createFormBuffFindPlayer(Player pl) {
        createForm(pl, BUFF_FIND_PLAYER, "TÌM NGƯỜI CHƠI", new SubInput("Tên nhân vật (hoặc ID)", ANY));
    }

    public void createFormBuffSearchItem(Player pl) {
        createForm(pl, BUFF_SEARCH_ITEM, "TÌM KIẾM VẬT PHẨM", new SubInput("Nhập ID hoặc Tên vật phẩm", ANY));
    }

    public void createFormBuffQuantity(Player pl, int itemId, String itemName) {
        createForm(pl, BUFF_INPUT_QUANTITY, "SỐ LƯỢNG: " + itemName + " (" + itemId + ")", new SubInput("Nhập số lượng (>= 1)", NUMERIC));
    }

    public void createFormBuffOption(Player pl) {
        createForm(pl, BUFF_INPUT_OPTION, "THÊM OPTION CHO VẬT PHẨM",
                new SubInput("ID hoặc Tên Option", ANY),
                new SubInput("Giá trị Param (tùy chọn)", ANY));
    }

    public void createFormBuffOptionString(Player pl) {
        createForm(pl, BUFF_INPUT_OPTION_STRING, "NHẬP CHUỖI OPTION",
                new SubInput("Chuỗi Option (VD: 50-30 77-25 14-15 95-10)", ANY));
    }

    public void createFormBuffParam(Player pl, int optId, String optName) {
        createForm(pl, BUFF_INPUT_PARAM, "NHẬP PARAM", new SubInput("Param cho " + optName + " (" + optId + ")", NUMERIC));
    }

    public void createFormBuffStep1(Player pl) {
        createForm(pl, BUFF_STEP_1, "BUFF VẬT PHẨM",
                new SubInput("ID vật phẩm", NUMERIC),
                new SubInput("Số lượng (Mặc định 1)", NUMERIC),
                new SubInput("Người nhận (Tên hoặc ID)", ANY));
    }

    public void createFormAdminCredit(Player pl) {
        createForm(pl, ADMIN_CREDIT_FORM, "CỘNG SỐ DƯ NẠP TIỀN",
                new SubInput("Tên hoặc ID người nhận", ANY),
                new SubInput("Số tiền cần cộng (VNĐ)", NUMERIC));
    }

    public void createFormChangePassword(Player pl) {
        createForm(pl, CHANGE_PASSWORD, "Đổi mật khẩu", new SubInput("Mật khẩu cũ", PASSWORD),
                new SubInput("Mật khẩu mới", PASSWORD),
                new SubInput("Nhập lại mật khẩu mới", PASSWORD));
    }

    public void createFormAdminReferralPoints(Player pl) {
        if (pl == null || pl.getSession() == null || !pl.isAdmin()) return;
        createForm(pl, ADMIN_REFERRAL_POINTS, "ADMIN - CẤP ĐIỂM GIỚI THIỆU",
                new SubInput("ID / tên chính xác nhân vật", ANY),
                new SubInput("Điểm cộng/trừ (+1 / -1)", ANY),
                new SubInput("Lý do xác nhận giới thiệu", ANY));
    }

    public void createFormGiftCode(Player pl) {
        if (pl.zone.map.mapId == 5 || pl.zone.map.mapId == 20 || pl.zone.map.mapId == 13) {
            createForm(pl, GIFT_CODE, "Mã quà tặng gồm 12 ký tự", new SubInput("Gift Code", ANY));
        } else {
            createForm(pl, GIFT_CODE, "Mã quà tặng", new SubInput("Nhập mã quà tặng", ANY));
        }
    }

    public void createFormFindPlayer(Player pl) {
        createForm(pl, FIND_PLAYER, "Tìm kiếm người chơi", new SubInput("Tên người chơi", ANY));
    }

    public void createFormDoiThoiVang(Player pl) {
        createForm(pl, DOI_THOI_VANG,
                "1K VNĐ = 100 Thỏi Vàng\nVí dụ: nhập 10 = 10K = 1.000 Thỏi Vàng",
                new SubInput("Nhập số K muốn đổi", NUMERIC));
    }

    public void createFormDoiHongNgoc(Player pl) {
        createForm(pl, DOI_HONG_NGOC, "1K VNĐ = 1k Hồng Ngọc", new SubInput("Nhập Số Hồng Ngọc Muốn Đổi", NUMERIC));
    }

    public void createFormChangeName(Player pl, Player plChanged) {
        PLAYER_ID_OBJECT.put((int) pl.id, plChanged);
        createForm(pl, CHANGE_NAME, "Đổi tên " + plChanged.name, new SubInput("Tên mới", ANY));
    }

    public void createFormChangeName1(Player pl, Player plChanged) {
        PLAYER_ID_OBJECT.put((int) pl.id, plChanged);
        createForm(pl, CHANGE_NAME, "Đổi tên " + plChanged.name + " với giá 50k vnd?", new SubInput("Tên mới", ANY));
    }

    public void createFormChooseLevelBDKB(Player pl) {
        createForm(pl, CHOOSE_LEVEL_BDKB, "Hãy chọn cấp độ hang kho báu từ 1-110", new SubInput("Cấp độ", NUMERIC));
    }

    public void createFormChooseLevelKhiGas(Player pl) {
        createForm(pl, CHOOSE_LEVEL_KGHD, "Hãy chọn cấp độ từ 1-110", new SubInput("Cấp độ", NUMERIC));
    }

    public void createFormChooseLevelCDRD(Player pl) {
        createForm(pl, CHOOSE_LEVEL_CDRD, "Chọn cấp độ", new SubInput("Cấp độ (1-110)", NUMERIC));
    }

    public void createFormGiaiTanBang(Player pl) {
        createForm(pl, GIAI_TAN_BANG, "Nhập OK để xác nhận giải tán bang hội.", new SubInput("", ANY));
    }

    public void createFormTangRuby(Player pl) {
        createForm(pl, TANG_NGOC_HONG, "Tặng ngọc", new SubInput("Tên nhân vật", ANY),
                new SubInput("Số Hồng Ngọc Muốn Tặng", NUMERIC));
    }

    public void createFormTangSKH(Player pl) {
        createForm(pl, TANG_SKH, "TẶNG SKH", new SubInput("Tên nhân vật", ANY),
                new SubInput("SGK, HP, Galick, Lienhoan, KI", ANY),
                new SubInput("0-Áo, 1-Quần; 2-Găng; 3-Giày; 4-Rada; 5-Full", NUMERIC));
    }

    public void createFormBanThoiVang(Player pl) {
        createForm(pl, BAN_THOI_VANG, "Bạn muốn bán bao nhiêu [Thỏi Vàng] ?", new SubInput("Số lượng", NUMERIC));
    }

    public void createFormAddItem(Player pl) {
        createForm(pl, ADD_ITEM, "Add Item", new SubInput("ID VẬT PHẨM", NUMERIC),
                new SubInput("SỐ LƯỢNG", NUMERIC));
    }

    public class SubInput {

        private String name;
        private byte typeInput;

        public SubInput(String name, byte typeInput) {
            this.name = name;
            this.typeInput = typeInput;
        }
    }

}
