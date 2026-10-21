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
                        pl = Client.gI().getPlayer(text[0]);
                        int idItemBuff = Integer.parseInt(text[1]);
                        String idOptionBuff = text[2].trim();
                        int slItemBuff = Integer.parseInt(text[3]);

                        try {
                            if (pl != null) {
                                String txtBuff = "Buff to player: " + pl.name + "\b";

                                Item itemBuffTemplate = ItemService.gI()
                                        .createNewItem((short) idItemBuff);
                                RewardService.gI().initBaseOptionClothes(itemBuffTemplate.template.id,
                                        itemBuffTemplate.template.type,
                                        itemBuffTemplate.itemOptions);
                                if (!idOptionBuff.isEmpty()) {
                                    String arr[] = idOptionBuff.split(" ");
                                    for (int i = 0; i < arr.length; i++) {
                                        String arr2[] = arr[i].split("-");
                                        int idoption = Integer.parseInt(arr2[0].trim());
                                        int param = Integer.parseInt(arr2[1].trim());
                                        itemBuffTemplate.itemOptions.add(new ItemOption(idoption, param));
                                    }
                                }
                                itemBuffTemplate.quantity = slItemBuff;
                                NpcService.gI().createTutorial(player, 24, txtBuff);
                                InventoryService.gI().addItemBag(pl, itemBuffTemplate, 0);
                                InventoryService.gI().sendItemBags(pl);
                                if (player.id != pl.id) {
                                    NpcService.gI().createTutorial(player, 24, txtBuff);
                                }
                            } else {
                                Service.getInstance().sendThongBao(player, "Player không online");
                            }
                        } catch (Exception e) {

                        }
                        break;
                    }
                    break;

                case CHANGE_PASSWORD:
                    Service.getInstance().changePassword(player, text[0], text[1], text[2]);
                    break;
                case GIFT_CODE:
                    GiftService.gI().use(player, text[0]);
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
                        int soVang = Integer.parseInt(text[0]);
                        if (soVang <= 0) {
                            Service.getInstance().sendThongBao(player, "Số thỏi vàng không hợp lệ");
                            return;
                        }
                        int tongTien = soVang * 10;

                        if (player.soDuVND < tongTien) {
                            Service.getInstance().sendThongBao(player, "Bạn không đủ số Dư");
                            return;
                        }
                        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
                            Service.getInstance().sendThongBao(player, "Cần 1 ô trống trong hành trang");
                            return;
                        }
                        Item thoivang = ItemService.gI().createNewConsignmentItem((short) ConstItem.THOI_VANG, soVang);
                        thoivang.itemOptions.add(new ItemOption(73, 0));
                        InventoryService.gI().addItemBag(player, thoivang, 99999);
                        PlayerDAO.subVndBar(player, tongTien);
                        player.soDuVND -= tongTien;
                        InventoryService.gI().sendItemBags(player);
                        Service.getInstance().sendThongBao(player, "Bạn nhận được " + thoivang.getName());
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
                        PlayerDAO.subVndBar(player, tongTien);
                        player.soDuVND -= tongTien;
                        InventoryService.gI().sendItemBags(player);
                        Service.getInstance().sendMoney(player);

                        Service.getInstance().sendThongBao(player, "Bạn nhận được " + tongTien + " Hồng Ngọc");
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
                    Item thoiVang = InventoryService.gI().findItemBagByTemp(player, (short) 457);
                    if (soLuong < 0) {
                        Service.getInstance().sendThongBao(player,
                                "Đã bán " + soLuong + " bãi cứt" + " thu được 1" + " vàng");
                        return;
                    }
                    if (soLuong <= thoiVang.quantity) {
                        long goldNhanDuoc = soLuong * 25_000_000;
                        long soGoldAll = goldNhanDuoc + player.inventory.gold;
                        if (soGoldAll <= player.inventory.getGoldLimit()) {
                            player.inventory.gold += (soLuong * 25_000_000);
                            InventoryService.gI().subQuantityItemsBag(player, thoiVang, (int) soLuong);
                            InventoryService.gI().sendItemBags(player);
                            Service.getInstance().sendMoney(player);
                            Service.getInstance().sendThongBao(player, "Đã bán " + soLuong + " " + thoiVang.getName()
                                    + " thu được " + Util.numberToMoney(goldNhanDuoc) + " vàng");
                        } else {
                            Service.getInstance().sendThongBao(player,
                                    "Số vàng sau khi bán vượt quá số vàng có thể lưu trữ");
                        }
                    } else {
                        Service.getInstance().sendThongBao(player, "Không đủ thỏi vàng để bán");
                    }
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
                    if (pl != null) {
                        if (numruby > 0 && player.inventory.ruby >= numruby) {
                            Item item = InventoryService.gI().findVeTangNgoc(player);
                            player.inventory.subRuby(numruby);
                            PlayerService.gI().sendInfoHpMpMoney(player);
                            pl.inventory.ruby += numruby;
                            PlayerService.gI().sendInfoHpMpMoney(pl);
                            Service.getInstance().sendThongBao(player, "Tặng Hồng ngọc thành công");
                            Service.getInstance().sendThongBao(pl,
                                    "Bạn được " + player.name + " tặng " + numruby + " Hồng ngọc");
                            InventoryService.gI().subQuantityItemsBag(player, item, 1);
                            InventoryService.gI().sendItemBags(player);
                        } else {
                            Service.getInstance().sendThongBao(player, "Không đủ Hồng ngọc để tặng");
                        }
                    } else {
                        Service.getInstance().sendThongBao(player, "Người chơi không tồn tại hoặc đang offline");
                    }
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
        createForm(pl, SEND_ITEM_OP, "SEND Vật Phẩm Option",
                new SubInput("Tên người chơi", ANY),
                new SubInput("ID item", NUMERIC),
                new SubInput("Chuỗi Option (50-30 77-20)", ANY),
                new SubInput("Số Lượng", NUMERIC));
    }

    public void createFormChangePassword(Player pl) {
        createForm(pl, CHANGE_PASSWORD, "Đổi mật khẩu", new SubInput("Mật khẩu cũ", PASSWORD),
                new SubInput("Mật khẩu mới", PASSWORD),
                new SubInput("Nhập lại mật khẩu mới", PASSWORD));
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
        createForm(pl, DOI_THOI_VANG, "1K VNĐ = 100 TV", new SubInput("Nhập Số TV Muốn Đổi", NUMERIC));
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
        createForm(pl, TANG_SKH, "-----Nro Yanii-----", new SubInput("Tên nhân vật", ANY),
                new SubInput("SGK, HP, Galick, Lienhoan, KI", ANY),
                new SubInput("0-Áo, 1-Quần; 2-Găng; 3-Giày; 4-Rada; 5-Full", NUMERIC));
    }

    public void createFormBanThoiVang(Player pl) {
        createForm(pl, BAN_THOI_VANG, "Bạn muốn bán bao nhiêu [Thỏi vàng] ?", new SubInput("Số lượng", NUMERIC));
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
