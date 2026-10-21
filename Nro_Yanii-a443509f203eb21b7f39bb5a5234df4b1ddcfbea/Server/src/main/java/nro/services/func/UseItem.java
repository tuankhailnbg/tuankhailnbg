package nro.services.func;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import nro.consts.*;
import nro.dialog.MenuDialog;
import nro.dialog.MenuRunable;
import nro.event.Event;
import nro.lib.RandomCollection;
import nro.manager.MiniPetManager;
import nro.manager.NamekBallManager;
import nro.manager.PetFollowManager;
import nro.models.item.Item;
import nro.models.item.ItemOption;
import nro.models.item.MinipetTemplate;
import nro.models.map.*;
import nro.models.map.dungeon.zones.ZSnakeRoad;
import nro.models.map.war.NamekBallWar;
import nro.models.player.Inventory;
import nro.models.player.MiniPet;
import nro.models.player.PetFollow;
import nro.models.player.Player;
import nro.models.skill.Skill;
import nro.server.Manager;
import nro.server.io.Message;
import nro.server.io.Session;
import nro.services.*;
import nro.utils.Log;
import nro.utils.SkillUtil;
import nro.utils.TimeUtil;
import nro.utils.Util;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import nro.jdbc.DBService;
import nro.jdbc.daos.PlayerDAO;
import nro.models.Part;
import nro.models.PartManager;
import nro.models.boss.Boss;
import nro.models.boss.BossFactory;
import nro.models.boss.BossManager;
import static nro.models.boss.BossManager.BOSSES_IN_GAME;
import nro.models.boss.td.TuanLocEvent;

/**
 * @author 💖 Nro Yanii 💖
 *
 */
public class UseItem {

    private static final int ITEM_BOX_TO_BODY_OR_BAG = 0;
    private static final int ITEM_BAG_TO_BOX = 1;
    private static final int ITEM_BODY_TO_BOX = 3;
    private static final int ITEM_BAG_TO_BODY = 4;
    private static final int ITEM_BODY_TO_BAG = 5;
    private static final int ITEM_BAG_TO_PET_BODY = 6;
    private static final int ITEM_BODY_PET_TO_BAG = 7;

    private static final byte DO_USE_ITEM = 0;
    private static final byte DO_THROW_ITEM = 1;
    private static final byte ACCEPT_THROW_ITEM = 2;
    private static final byte ACCEPT_USE_ITEM = 3;

    private static UseItem instance;
    private static final Logger logger = Logger.getLogger(UseItem.class);

    private UseItem() {

    }

    public static UseItem gI() {
        if (instance == null) {
            instance = new UseItem();
        }
        return instance;
    }

    public void getItem(Session session, Message msg) {
        Player player = session.player;
        TransactionService.gI().cancelTrade(player);
        try {
            int type = msg.reader().readByte();
            int index = msg.reader().readByte();
            switch (type) {
                case ITEM_BOX_TO_BODY_OR_BAG:
                    InventoryService.gI().itemBoxToBodyOrBag(player, index);
                    TaskService.gI().checkDoneTaskGetItemBox(player);
                    break;
                case ITEM_BAG_TO_BOX:
                    InventoryService.gI().itemBagToBox(player, index);
                    break;
                case ITEM_BODY_TO_BOX:
                    InventoryService.gI().itemBodyToBox(player, index);
                    break;
                case ITEM_BAG_TO_BODY:
                    InventoryService.gI().itemBagToBody(player, index);
                    break;
                case ITEM_BODY_TO_BAG:
                    InventoryService.gI().itemBodyToBag(player, index);
                    break;
                case ITEM_BAG_TO_PET_BODY:
                    InventoryService.gI().itemBagToPetBody(player, index);
                    break;
                case ITEM_BODY_PET_TO_BAG:
                    InventoryService.gI().itemPetBodyToBag(player, index);
                    break;
            }
            player.setClothes.setup();
            if (player.pet != null) {
                player.pet.setClothes.setup();
            }
            player.setClanMember();
            PlayerService.gI().sendPetFollow(player);
            Service.getInstance().point(player);
        } catch (Exception e) {
            Log.error(UseItem.class, e);

        }
    }

    public void doItem(Player player, Message _msg) {
        TransactionService.gI().cancelTrade(player);
        Message msg;
        try {
            byte type = _msg.reader().readByte();
            int where = _msg.reader().readByte();
            int index = _msg.reader().readByte();

            switch (type) {
                case DO_USE_ITEM:
                    if (player != null && player.inventory != null) {
                        if (index != -1) {
                            if (index >= 0 && index < player.inventory.itemsBag.size()) {
                                Item item = player.inventory.itemsBag.get(index);
                                if (item.isNotNullItem()) {
                                    if (item.template.type == 21) {
                                        MiniPet.callMiniPet(player, item.template.id);
                                        InventoryService.gI().itemBagToBody(player, index);
                                        break;
                                    }
                                    if (item.template.type == 22) {
                                        msg = new Message(-43);
                                        msg.writer().writeByte(type);
                                        msg.writer().writeByte(where);
                                        msg.writer().writeByte(index);
                                        msg.writer().writeUTF("Bạn có muốn dùng "
                                                + player.inventory.itemsBag.get(index).template.name + "?");
                                        player.sendMessage(msg);
                                        msg.cleanup();
                                    } else if (item.template.type == 7) {
                                        msg = new Message(-43);
                                        msg.writer().writeByte(type);
                                        msg.writer().writeByte(where);
                                        msg.writer().writeByte(index);
                                        msg.writer().writeUTF("Bạn chắc chắn học "
                                                + player.inventory.itemsBag.get(index).template.name + "?");
                                        player.sendMessage(msg);
                                    } else if (player.isVersionAbove(220) && item.template.type == 23
                                            || item.template.type == 24 || item.template.type == 11
                                            || item.template.type == 35) {
                                        InventoryService.gI().itemBagToBody(player, index);
                                    } else if (item.template.id == 401) {
                                        msg = new Message(-43);
                                        msg.writer().writeByte(type);
                                        msg.writer().writeByte(where);
                                        msg.writer().writeByte(index);
                                        msg.writer().writeUTF(
                                                "Sau khi đổi đệ sẽ mất toàn bộ trang bị trên người đệ tử nếu chưa tháo");
                                        player.sendMessage(msg);
                                    } else if (item.template.type == 72) {
                                        PetFollow pet = PetFollowManager.gI().findByID(item.getId());
                                        player.setPetFollow(pet);
                                        InventoryService.gI().itemBagToBody(player, index);
                                        PlayerService.gI().sendPetFollow(player);
                                    } else {
                                        useItem(player, item, index);
                                    }
                                }
                            }
                        } else {
                            InventoryService.gI().eatPea(player);
                        }
                    }
                    break;
                case DO_THROW_ITEM:
                    if (!(player.zone.map.mapId == 21 || player.zone.map.mapId == 22 || player.zone.map.mapId == 23)) {
                        Item item = null;
                        if (where == 0) {
                            if (index >= 0 && index < player.inventory.itemsBody.size()) {
                                item = player.inventory.itemsBody.get(index);
                            }
                        } else {
                            if (index >= 0 && index < player.inventory.itemsBag.size()) {
                                item = player.inventory.itemsBag.get(index);
                            }
                        }
                        if (item != null && item.isNotNullItem()) {
                            msg = new Message(-43);
                            msg.writer().writeByte(type);
                            msg.writer().writeByte(where);
                            msg.writer().writeByte(index);
                            msg.writer().writeUTF("Bạn chắc chắn muốn vứt " + item.template.name + "?");
                            player.sendMessage(msg);
                        }
                    } else {
                        Service.getInstance().sendThongBao(player, "Không thể thực hiện");
                    }
                    break;
                case ACCEPT_THROW_ITEM:
                    if (player.isUseMaBaoVe) {
                        Service.getInstance().sendThongBao(player,
                                "Chức năng bảo vệ đã được bật. Bạn vui lòng kiểm tra lại");
                        return;
                    }
                    InventoryService.gI().throwItem(player, where, index);
                    break;
                case ACCEPT_USE_ITEM:
                    if (index >= 0 && index < player.inventory.itemsBag.size()) {
                        Item item = player.inventory.itemsBag.get(index);
                        if (item.isNotNullItem()) {
                            useItem(player, item, index);
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            Log.error(UseItem.class, e);
        }
    }

    public void useSatellite(Player player, Item item) {
        Satellite satellite = null;
        if (player.zone != null) {
            int count = player.zone.getSatellites().size();
            if (count < 3) {
                switch (item.template.id) {
                    case ConstItem.VE_TINH_TRI_LUC:
                        satellite = new SatelliteMP(player.zone, ConstItem.VE_TINH_TRI_LUC, player.location.x,
                                player.location.y, player);
                        break;

                    case ConstItem.VE_TINH_TRI_TUE:
                        satellite = new SatelliteExp(player.zone, ConstItem.VE_TINH_TRI_TUE, player.location.x,
                                player.location.y, player);
                        break;

                    case ConstItem.VE_TINH_PHONG_THU:
                        satellite = new SatelliteDefense(player.zone, ConstItem.VE_TINH_PHONG_THU, player.location.x,
                                player.location.y, player);
                        break;

                    case ConstItem.VE_TINH_SINH_LUC:
                        satellite = new SatelliteHP(player.zone, ConstItem.VE_TINH_SINH_LUC, player.location.x,
                                player.location.y, player);
                        break;
                }
                if (satellite != null) {
                    InventoryService.gI().subQuantityItemsBag(player, item, 1);
                    Service.getInstance().dropItemMapForMe(player, satellite);
                    Service.getInstance().dropItemMap(player.zone, satellite);
                }
            } else {
                Service.getInstance().sendThongBaoOK(player,
                        "Số lượng vệ tinh có thể đặt trong khu vực đã đạt mức tối đa.");
            }
        }
    }

    private void useItem(Player pl, Item item, int indexBag) {
        if (pl.isUseMaBaoVe) {
            Service.getInstance().sendThongBao(pl, "Chức năng bảo vệ đã được bật. Bạn vui lòng kiểm tra lại");
            return;
        }
        if (Event.isEvent() && Event.getInstance().useItem(pl, item)) {
            return;
        }
        if (item.template.strRequire <= pl.nPoint.power) {
            int type = item.getType();
            if (type == 6) {
                InventoryService.gI().eatPea(pl);
            } else if (type == 33) {
                RadaService.getInstance().useItemCard(pl, item);
            } else if (type == 22) {
                useSatellite(pl, item);
            } else if (type == 99) {
                if (item.template.id == 1313) {
                    InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    Service.getInstance().point(pl);
                }
                if (item.template.id == 1318) {
                    short[] listCaiTrang = { 1271, 1270 };
                    Item caiTrang = ItemService.gI()
                            .createNewItem(listCaiTrang[Util.nextInt(0, listCaiTrang.length - 1)]);
                    caiTrang.itemOptions.add(new ItemOption(50, Util.nextInt(7, 30)));
                    caiTrang.itemOptions.add(new ItemOption(77, Util.nextInt(7, 30)));
                    caiTrang.itemOptions.add(new ItemOption(103, Util.nextInt(7, 30)));
                    caiTrang.itemOptions.add(new ItemOption(5, Util.nextInt(1, 5)));
                    if (Util.isTrue(5, 100)) {
                        caiTrang.itemOptions.add(new ItemOption(93, Util.nextInt(10, 20)));
                    } else {
                        caiTrang.itemOptions.add(new ItemOption(93, Util.nextInt(1, 10)));
                    }
                    InventoryService.gI().addItemBag(pl, caiTrang, 9999);
                    InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    InventoryService.gI().sendItemBags(pl);
                    Service.getInstance().sendThongBao(pl, "Bạn nhận được " + caiTrang.Name() + " từ " + item.Name());
                }
            } else {
                switch (item.template.id) {
                    case ConstItem.GOI_10_RADA_DO_NGOC:
                        findNamekBall(pl, item);
                        break;
                    case 2052:
                        capsule8thang3(pl, item);
                        break;
                    case ConstItem.CAPSULE_THOI_TRANG_30_NGAY:
                        capsuleThoiTrang(pl, item);
                        break;
                    case 2084:
                        if (pl.pet != null) {
                            if (pl.pet.playerSkill.skills.get(1).skillId != -1) {
                                pl.pet.openSkill2();
                                Service.getInstance().sendThongBao(pl,
                                        "Thay thành công chiêu 2 đệ tử!");
                            } else {
                                Service.getInstance().sendThongBao(pl,
                                        "Ít nhất đệ tử ngươi phải có chiêu 2 chứ!");
                                return;
                            }
                        } else {
                            Service.getInstance().sendThongBao(pl, "Ngươi làm gì có đệ tử?");
                            return;
                        }
                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                        InventoryService.gI().sendItemBags(pl);
                        break;
                    case 2085:
                        if (pl.pet != null) {
                            if (pl.pet.playerSkill.skills.get(2).skillId != -1) {
                                pl.pet.openSkill3();
                                Service.getInstance().sendThongBao(pl,
                                        "Thay thành công chiêu 3 đệ tử!");
                            } else {
                                Service.getInstance().sendThongBao(pl,
                                        "Ít nhất đệ tử ngươi phải có chiêu 3 chứ!");

                                return;
                            }
                        } else {
                            Service.getInstance().sendThongBao(pl, "Ngươi làm gì có đệ tử?");
                            return;
                        }
                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                        InventoryService.gI().sendItemBags(pl);
                        break;
                    case 2086:
                        if (pl.pet != null) {
                            if (pl.pet.playerSkill.skills.get(3).skillId != -1) {
                                pl.pet.openSkill3();
                                Service.getInstance().sendThongBao(pl,
                                        "Thay thành công chiêu 4 đệ tử!");
                            } else {
                                Service.getInstance().sendThongBao(pl,
                                        "Ít nhất đệ tử ngươi phải có chiêu 4 chứ!");

                                return;
                            }
                        } else {
                            Service.getInstance().sendThongBao(pl, "Ngươi làm gì có đệ tử?");
                            return;
                        }
                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                        InventoryService.gI().sendItemBags(pl);
                        break;
                    case 2039:
                        openboxsukien(pl, item, ConstEvent.SU_KIEN_TET);
                        break;
                    //////
                    case 1312: // hộp quà tân thủ
                        Service.gI().sendThongBao(pl, "Bạn Nhận Được Cái Nịt Tân Thủ");
                        break;
                    ////
                    case 570:
                        openWoodChest(pl, item);
                        break;
                    case 648:
                        openboxsukien(pl, item, 3);
                        break;
                    case 2135:
                        hopquatrungthu(pl, 1, item);
                        break;
                    case 2149:
                        hopquanoel(pl, 1, item);
                        break;
                    case 2144:
                        // Service.gI().sendThongBao(pl, "Chức năng tạm đóng");
                        Skill skill;
                        for (int i = 0; i < pl.playerSkill.skills.size(); i++) {
                            skill = pl.playerSkill.skills.get(i);
                            skill.lastTimeUseThisSkill = System.currentTimeMillis()
                                    - (long) skill.coolDown;
                        }
                        Service.getInstance().sendTimeSkill(pl);
                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                        break;
                    case 2150:
                        Chuong(pl, item);
                        break;
                    case 2139:
                        hopquaHalloween(pl, 1, item);
                        break;
                    case 2024:
                        // hopQuaTanThu(pl, item);
                        break;
                    case 992:
                        nhanthoikhong(pl, item);
                        break;
                    case 2023:
                        // Input.gI().createFormTangRuby(pl);
                        break;
                    case 2006: // phiếu cải trang hải tặc
                    case 2007: // phiếu cải trang hải tặc
                    case 2008: // phiếu cải trang hải tặc
                        openPhieuCaiTrangHaiTac(pl, item);
                        break;
                    case 2012: // Hop Qua Kich Hoat
                        openboxsukien(pl, item, 1);
                        break;
                    case 2020: // phiếu cải trang 20/10
                        openbox2010(pl, item);
                        break;
                    case 2021:
                        openboxsukien(pl, item, 2);
                        break;
                    case 211: // nho tím
                    case 212: // nho xanh
                        eatGrapes(pl, item);
                        break;
                    case 380: // cskb
                        openCSKB(pl, item);
                        break;
                    case 381: // cuồng nộ
                    case 382: // bổ huyết
                    case 383: // bổ khí
                    case 384: // giáp xên
                    case 385: // ẩn danh
                    case 379: // máy dò
                    case 663: // bánh pudding
                    case 664: // xúc xíc
                    case 665: // kem dâu
                    case 666: // mì ly
                    case 667: // sushi
                    case ConstItem.BANH_CHUNG_CHIN:
                    case ConstItem.BANH_TET_CHIN:
                    case ConstItem.CUONG_NO_2:
                    case ConstItem.BO_HUYET_2:
                    case ConstItem.GIAP_XEN_BO_HUNG_2:
                    case ConstItem.BO_KHI_2:
                        useItemTime(pl, item);
                        break;
                    case 521: // tdlt
                        useTDLT(pl, item);
                        break;
                    case 454: // bông tai
                        usePorata(pl);
                        break;
                    case 921: // bông tai
                        usePorata2(pl);
                        break;
                    case 1280: // bông tai
                        // NpcService.gI().createMenuConMeo(pl, ConstNpc.HOP_QUA_THAN_LINH, 9840,
                        // "Đây là hộp quà để giúp các chiến binh trải nghiệm game một cách tốt hơn, Hộp
                        // quà này chỉ có hiệu lực tại phiên bản Open Test và open sẽ được xóa.",
                        // "Trái đất", "Namek", "Xayda");
                        break;
                    case 193: // gói 10 viên capsule
                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    case 194: // capsule đặc biệt
                        openCapsuleUI(pl);
                        break;
                    case 2126: // rada dò boss
                        showListBoss1(pl, item);
                        // Service.getInstance().sendThongBao(pl, "Chức năng tạm đóng để đua top nhiệm
                        // vụ");
                        break;
                    case 401: // đổi đệ tử
                        changePet(pl, item);
                        break;
                    case 402: // sách nâng chiêu 1 đệ tử
                    case 403: // sách nâng chiêu 2 đệ tử
                    case 404: // sách nâng chiêu 3 đệ tử
                    case 759: // sách nâng chiêu 4 đệ tử
                        upSkillPet(pl, item);
                        break;
                    case ConstItem.CAPSULE_TET_2022:
                        // openCapsuleTet2022(pl, item);
                        break;
                    case 1400: // đổi đệ tử
                        changePetBuLo(pl, item);
                        break;
                    case 1401: // đổi đệ tử
                        changePetCellBao(pl, item);
                        break;
                    default:
                        switch (item.template.type) {
                            case 7: // sách học, nâng skill
                                learnSkill(pl, item);
                                break;
                            case 12: // ngọc rồng các loại
                                Service.getInstance().sendThongBaoOK(pl, "Bảo trì tính năng.");
                                // controllerCallRongThan(pl, item);
                                break;
                            case 11: // item flag bag
                                useItemChangeFlagBag(pl, item);
                                break;
                            case 39:
                                InventoryService.gI().itemBagToBody(pl, indexBag);
                                Service.getInstance().sendTitle1(pl, item.template.part);
                                break;
                        }
                }
            }
            InventoryService.gI().sendItemBags(pl);
        } else {
            Service.getInstance().sendThongBaoOK(pl, "Sức mạnh không đủ yêu cầu");
        }
    }

    private void findNamekBall(Player pl, Item item) {
        List<Integer> usedMaps = new ArrayList<>(); // Danh sách các Map đã sử dụng
        int randomtoado = 0;

        List<NamekBall> balls = NamekBallManager.gI().getList();
        StringBuffer sb = new StringBuffer();
        for (NamekBall namekBall : balls) {
            Map m = namekBall.zone.map;
            int z = namekBall.zone.zoneId;
            if (pl.zone.map.mapId == m.mapId) {
                if (pl.zone.zoneId == z) {
                    sb.append(namekBall.getIndex() + 1).append(" Sao: " + "(" + Util.nextInt(80, 200) + "m) ")
                            .append(namekBall.getHolderName() == null ? "" : namekBall.getHolderName())
                            .append("đây" + "\n");
                } else {
                    sb.append(namekBall.getIndex() + 1).append(" Sao: " + "(" + Util.nextInt(80, 200) + "m) ")
                            .append(namekBall.getHolderName() == null ? "" : namekBall.getHolderName())
                            .append("đây kv " + z + "\n");
                }
            } else {
                sb.append(namekBall.getIndex() + 1).append(" Sao: " + "(" + Util.nextInt(80, 200) + "m) ")
                        .append(m.mapName).append(namekBall.getHolderName() == null ? "" : namekBall.getHolderName())
                        .append("\n");
            }
        }
        final int star = Util.nextInt(0, 6);
        final NamekBall ball = NamekBallManager.gI().findByIndex(star);
        final Inventory inventory = pl.inventory;
        MenuDialog menu = new MenuDialog(sb.toString(), new String[] { "Ok", "Đóng" }, new MenuRunable() {
            @Override
            public void run() {
                switch (getIndexSelected()) {
                    case 0:
                        break;
                    case 1:
                        break;
                }
                if (pl.isHoldNamecBall) {
                    NamekBallWar.gI().dropBall(pl);
                }
            }
        });
        menu.show(pl);
        InventoryService.gI().sendItemBags(pl);
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
    }

    private void capsuleThoiTrang(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            Item it = ItemService.gI().createNewItem(
                    (short) Util.nextInt(ConstItem.CAI_TRANG_GOKU_THOI_TRANG, ConstItem.CAI_TRANG_CA_DIC_THOI_TRANG));
            it.itemOptions.add(new ItemOption(50, 30));
            it.itemOptions.add(new ItemOption(77, 30));
            it.itemOptions.add(new ItemOption(103, 30));
            it.itemOptions.add(new ItemOption(106, 0));
            InventoryService.gI().addItemBag(pl, it, 0);
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);
            short icon1 = item.template.iconID;
            short icon2 = it.template.iconID;
            CombineServiceNew.gI().sendEffectOpenItem(pl, icon1, icon2);
        } else {
            Service.getInstance().sendThongBao(pl, "Hãy chừa 1 ô trống để mở.");
        }

    }

    private void openCapsuleTet2022(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) == 0) {
            Service.getInstance().sendThongBao(pl, "Hãy chừa 1 ô trống để mở.");
            return;
        }
        RandomCollection<Integer> rdItemID = new RandomCollection<>();
        rdItemID.add(1, ConstItem.PHAO_HOA);
        rdItemID.add(1, ConstItem.CAY_TRUC);
        rdItemID.add(1, ConstItem.NON_HO_VANG);
        if (pl.gender == 0) {
            rdItemID.add(1, ConstItem.NON_TRAU_MAY_MAN);
            rdItemID.add(1, ConstItem.NON_CHUOT_MAY_MAN);
        } else if (pl.gender == 1) {
            rdItemID.add(1, ConstItem.NON_TRAU_MAY_MAN_847);
            rdItemID.add(1, ConstItem.NON_CHUOT_MAY_MAN_755);
        } else {
            rdItemID.add(1, ConstItem.NON_TRAU_MAY_MAN_848);
            rdItemID.add(1, ConstItem.NON_CHUOT_MAY_MAN_756);
        }
        rdItemID.add(1, ConstItem.CAI_TRANG_HO_VANG);
        rdItemID.add(1, ConstItem.HO_MAP_VANG);
        // rdItemID.add(2, ConstItem.SAO_PHA_LE);
        // rdItemID.add(2, ConstItem.SAO_PHA_LE_442);
        // rdItemID.add(2, ConstItem.SAO_PHA_LE_443);
        // rdItemID.add(2, ConstItem.SAO_PHA_LE_444);
        // rdItemID.add(2, ConstItem.SAO_PHA_LE_445);
        // rdItemID.add(2, ConstItem.SAO_PHA_LE_446);
        // rdItemID.add(2, ConstItem.SAO_PHA_LE_447);
        rdItemID.add(2, ConstItem.DA_LUC_BAO);
        rdItemID.add(2, ConstItem.DA_SAPHIA);
        rdItemID.add(2, ConstItem.DA_TITAN);
        rdItemID.add(2, ConstItem.DA_THACH_ANH_TIM);
        rdItemID.add(2, ConstItem.DA_RUBY);
        rdItemID.add(3, ConstItem.VANG_190);
        int itemID = rdItemID.next();
        Item newItem = ItemService.gI().createNewItem((short) itemID);
        if (newItem.template.type == 9) {
            newItem.quantity = Util.nextInt(10, 50) * 1000000;
        } else if (newItem.template.type == 14 || newItem.template.type == 30) {
            newItem.quantity = 10;
        } else {
            switch (itemID) {
                case ConstItem.CAY_TRUC: {
                    RandomCollection<ItemOption> rdOption = new RandomCollection<>();
                    rdOption.add(2, new ItemOption(77, 15));// %hp
                    rdOption.add(2, new ItemOption(103, 15));// %hp
                    rdOption.add(1, new ItemOption(50, 15));// %hp
                    newItem.itemOptions.add(rdOption.next());
                }
                    break;

                case ConstItem.HO_MAP_VANG: {
                    newItem.itemOptions.add(new ItemOption(77, Util.nextInt(10, 20)));
                    newItem.itemOptions.add(new ItemOption(103, Util.nextInt(10, 20)));
                    newItem.itemOptions.add(new ItemOption(50, Util.nextInt(10, 20)));
                }
                    break;

                case ConstItem.NON_HO_VANG:
                case ConstItem.CAI_TRANG_HO_VANG:
                case ConstItem.NON_TRAU_MAY_MAN:
                case ConstItem.NON_TRAU_MAY_MAN_847:
                case ConstItem.NON_TRAU_MAY_MAN_848:
                case ConstItem.NON_CHUOT_MAY_MAN:
                case ConstItem.NON_CHUOT_MAY_MAN_755:
                case ConstItem.NON_CHUOT_MAY_MAN_756:
                    newItem.itemOptions.add(new ItemOption(77, 30));
                    newItem.itemOptions.add(new ItemOption(103, 30));
                    newItem.itemOptions.add(new ItemOption(50, 30));
                    break;
            }
            RandomCollection<Integer> rdDay = new RandomCollection<>();
            rdDay.add(6, 3);
            rdDay.add(3, 7);
            rdDay.add(1, 15);
            int day = rdDay.next();
            newItem.itemOptions.add(new ItemOption(93, day));
        }
        short icon1 = item.template.iconID;
        short icon2 = newItem.template.iconID;
        if (newItem.template.type == 9) {
            Service.getInstance().sendThongBao(pl,
                    "Bạn nhận được " + Util.numberToMoney(newItem.quantity) + " " + newItem.template.name);
        } else if (newItem.quantity == 1) {
            Service.getInstance().sendThongBao(pl, "Bạn nhận được " + newItem.template.name);
        } else {
            Service.getInstance().sendThongBao(pl, "Bạn nhận được x" + newItem.quantity + " " + newItem.template.name);
        }
        CombineServiceNew.gI().sendEffectOpenItem(pl, icon1, icon2);
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        InventoryService.gI().addItemBag(pl, newItem, 99);
        InventoryService.gI().sendItemBags(pl);
    }

    private int randClothes(int level) {
        return ConstItem.LIST_ITEM_CLOTHES[Util.nextInt(0, 2)][Util.nextInt(0, 4)][level - 1];
    }

    private void openWoodChest(Player pl, Item item) {
        int time = (int) TimeUtil.diffDate(new Date(), new Date(item.createTime), TimeUtil.DAY);
        if (time != 0) {
            Item itemReward = null;
            int param = item.itemOptions.get(0).param;
            int gold = 0;
            int[] listItem = { 441, 442, 443, 444, 445, 446, 447, 220, 221, 222, 223, 224, 225 };
            int[] listClothesReward;
            int[] listItemReward;
            String text = "Bạn nhận được\n";
            if (param < 8) {
                gold = 100000 * param;
                listClothesReward = new int[] { randClothes(param) };
                listItemReward = Util.pickNRandInArr(listItem, 3);
            } else if (param < 10) {
                gold = 250000 * param;
                listClothesReward = new int[] { randClothes(param), randClothes(param) };
                listItemReward = Util.pickNRandInArr(listItem, 4);
            } else {
                gold = 500000 * param;
                listClothesReward = new int[] { randClothes(param), randClothes(param), randClothes(param) };
                listItemReward = Util.pickNRandInArr(listItem, 5);
                int ruby = Util.nextInt(1, 5);
                pl.inventory.ruby += ruby;
                pl.textRuongGo.add(text + "|1| " + ruby + " Hồng Ngọc");
            }
            for (var i : listClothesReward) {
                itemReward = ItemService.gI().createNewItem((short) i);
                RewardService.gI().initBaseOptionClothes(itemReward.template.id, itemReward.template.type,
                        itemReward.itemOptions);
                RewardService.gI().initStarOption(itemReward, new RewardService.RatioStar[] {
                        new RewardService.RatioStar((byte) 1, 1, 2), new RewardService.RatioStar((byte) 2, 1, 3),
                        new RewardService.RatioStar((byte) 3, 1, 4), new RewardService.RatioStar((byte) 4, 1, 5), });
                InventoryService.gI().addItemBag(pl, itemReward, 0);
                pl.textRuongGo.add(text + itemReward.getInfoItem());
            }
            for (var i : listItemReward) {
                itemReward = ItemService.gI().createNewItem((short) i);
                RewardService.gI().initBaseOptionSaoPhaLe(itemReward);
                itemReward.quantity = Util.nextInt(1, 5);
                InventoryService.gI().addItemBag(pl, itemReward, 0);
                pl.textRuongGo.add(text + itemReward.getInfoItem());
            }
            if (param == 11) {
                itemReward = ItemService.gI().createNewItem((short) ConstItem.MANH_NHAN);
                itemReward.quantity = Util.nextInt(1, 3);
                InventoryService.gI().addItemBag(pl, itemReward, 0);
                pl.textRuongGo.add(text + itemReward.getInfoItem());
            }
            NpcService.gI().createMenuConMeo(pl, ConstNpc.RUONG_GO, -1,
                    "Bạn nhận được\n|1|+" + Util.numberToMoney(gold) + " vàng", "OK [" + pl.textRuongGo.size() + "]");
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            pl.inventory.addGold(gold);
            InventoryService.gI().sendItemBags(pl);
            PlayerService.gI().sendInfoHpMpMoney(pl);
        } else {
            Service.getInstance().sendThongBao(pl, "Vì bạn quên không lấy chìa nên cần đợi 24h để bẻ khóa");
        }
    }

    private void useItemChangeFlagBag(Player player, Item item) {
        switch (item.template.id) {
            case 865: // kiem Z
                if (!player.effectFlagBag.useKiemz) {
                    player.effectFlagBag.reset();
                    player.effectFlagBag.useKiemz = !player.effectFlagBag.useKiemz;
                } else {
                    player.effectFlagBag.reset();
                }
                break;
            case 994: // vỏ ốc
                break;
            case 995: // cây kem
                break;
            case 996: // cá heo
                break;
            case 997: // con diều
                break;
            case 998: // diều rồng
                if (!player.effectFlagBag.useDieuRong) {
                    player.effectFlagBag.reset();
                    player.effectFlagBag.useDieuRong = !player.effectFlagBag.useDieuRong;
                } else {
                    player.effectFlagBag.reset();
                }
                break;
            case 999: // mèo mun
                if (!player.effectFlagBag.useMeoMun) {
                    player.effectFlagBag.reset();
                    player.effectFlagBag.useMeoMun = !player.effectFlagBag.useMeoMun;
                } else {
                    player.effectFlagBag.reset();
                }
                break;
            case 1000: // xiên cá
                if (!player.effectFlagBag.useXienCa) {
                    player.effectFlagBag.reset();
                    player.effectFlagBag.useXienCa = !player.effectFlagBag.useXienCa;
                } else {
                    player.effectFlagBag.reset();
                }
                break;
            case 1001: // phóng heo
                if (!player.effectFlagBag.usePhongHeo) {
                    player.effectFlagBag.reset();
                    player.effectFlagBag.usePhongHeo = !player.effectFlagBag.usePhongHeo;
                } else {
                    player.effectFlagBag.reset();
                }
                break;
            case 954:
                if (!player.effectFlagBag.useHoaVang) {
                    player.effectFlagBag.reset();
                    player.effectFlagBag.useHoaVang = !player.effectFlagBag.useHoaVang;
                } else {
                    player.effectFlagBag.reset();
                }
                break;
            case 955:
                if (!player.effectFlagBag.useHoaHong) {
                    player.effectFlagBag.reset();
                    player.effectFlagBag.useHoaHong = !player.effectFlagBag.useHoaHong;
                } else {
                    player.effectFlagBag.reset();
                }
                break;
            case 852:
                if (!player.effectFlagBag.useGayTre) {
                    player.effectFlagBag.reset();
                    player.effectFlagBag.useGayTre = !player.effectFlagBag.useGayTre;
                } else {
                    player.effectFlagBag.reset();
                }
                break;
        }
        Service.getInstance().point(player);
        Service.getInstance().sendFlagBag(player);
    }

    private void changePet(Player player, Item item) {
        if (player.pet != null) {
            int gender = player.pet.gender + 1;
            if (gender > 2) {
                gender = 0;
            }
            PetService.gI().changeNormalPet(player, gender);
            InventoryService.gI().subQuantityItemsBag(player, item, 1);
        } else {
            Service.getInstance().sendThongBao(player, "Không thể thực hiện");
        }
    }

    private void changePetBuLo(Player player, Item item) {
        if (player.pet != null) {
            if (player.pet.isMabu) {
                int gender = player.pet.gender;
                PetService.gI().changeSuperPet(player, gender, 0);
                InventoryService.gI().subQuantityItemsBag(player, item, 1);
            } else {
                Service.getInstance().sendThongBao(player, "Yêu Cầu Có Đệ Tử Ma Bư");
            }
        } else {
            Service.getInstance().sendThongBao(player, "Không thể thực hiện");
        }
    }

    private void changePetCellBao(Player player, Item item) {
        if (player.pet != null) {
            if (player.pet.isMabu) {
                int gender = player.pet.gender;
                PetService.gI().changeSuperPet(player, gender, 1);
                InventoryService.gI().subQuantityItemsBag(player, item, 1);
            } else {
                Service.getInstance().sendThongBao(player, "Yêu Cầu Có Đệ Tử Ma Bư");
            }
        } else {
            Service.getInstance().sendThongBao(player, "Không thể thực hiện");
        }
    }

    private void showListBoss1(Player player, Item item) {
        final byte OPCODE_SHOW_BOSS_LIST = -96;

        Message msg = null;
        try {
            InventoryService.gI().subQuantityItemsBag(player, item, 1);

            msg = new Message(OPCODE_SHOW_BOSS_LIST);
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Boss (SL: " + BossManager.BOSSES_IN_GAME.size() + ")");

            // Đếm số boss sống, không phải Yar
            long aliveBossCount = BossManager.BOSSES_IN_GAME.stream()
                    .filter(boss -> boss != null && !BossFactory.isYar((byte) boss.id))
                    .count();
            msg.writer().writeByte((int) aliveBossCount);

            for (Boss boss : BossManager.BOSSES_IN_GAME) {
                if (boss == null || BossFactory.isYar((byte) boss.id)) {
                    continue;
                }

                msg.writer().writeInt((int) boss.id);
                msg.writer().writeInt((int) boss.id); // Có thể bạn nên dùng id và model id khác nhau?
                msg.writer().writeShort(boss.getHead());

                if (player.isVersionAbove(220)) {
                    Part part = PartManager.getInstance().find(boss.getHead());
                    msg.writer().writeShort(part != null ? part.getIcon(0) : -1);
                }

                msg.writer().writeShort(boss.getBody());
                msg.writer().writeShort(boss.getLeg());
                msg.writer().writeUTF(boss.name);

                if (boss.zone != null && boss.zone.map != null) {
                    msg.writer().writeUTF("Sống");
                    msg.writer().writeUTF(
                            player.getSession().actived ? boss.zone.map.mapName + " (" + boss.zone.map.mapId + ")"
                                    : boss.zone.map.mapName + " (" + boss.zone.map.mapId + ") khu " + boss.zone.zoneId);
                } else {
                    msg.writer().writeUTF("Chết");
                    msg.writer().writeUTF("Chết rồi");
                }
            }

            player.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace(); // Hoặc Log.error(...)
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void nhanthoikhong(Player pl, Item it) {
        ChangeMapService.gI().goToPrimaryForest(pl);
    }

    public void hopquaHalloween(Player pl, int num, Item item) {

        // Kiểm tra túi có đủ chỗ không
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            // Random chọn vật phẩm từ 0 đến 2 (3 vật phẩm)
            int randomItemIndex = Util.nextInt(0, 3);
            Item vatpham = null;

            switch (randomItemIndex) {
                case 0:
                    vatpham = ItemService.gI().createNewItem((short) 2140, 1);
                    vatpham.itemOptions.add(new ItemOption(50, Util.nextInt(5, 15)));
                    vatpham.itemOptions.add(new ItemOption(77, Util.nextInt(5, 15)));
                    vatpham.itemOptions.add(new ItemOption(103, Util.nextInt(5, 15)));
                    break;
                case 1:
                    vatpham = ItemService.gI().createNewItem((short) 2141, 1);
                    vatpham.itemOptions.add(new ItemOption(50, Util.nextInt(5, 15)));
                    vatpham.itemOptions.add(new ItemOption(77, Util.nextInt(5, 15)));
                    vatpham.itemOptions.add(new ItemOption(103, Util.nextInt(5, 15)));
                    break;
                case 2:
                    vatpham = ItemService.gI().createNewItem((short) 1299, 1);
                    vatpham.itemOptions.add(new ItemOption(50, Util.nextInt(5, 15)));
                    vatpham.itemOptions.add(new ItemOption(77, Util.nextInt(5, 15)));
                    vatpham.itemOptions.add(new ItemOption(103, Util.nextInt(5, 15)));
                    break;
                case 3:
                    vatpham = ItemService.gI().createNewItem((short) 1325, 1);
                    vatpham.itemOptions.add(new ItemOption(50, Util.nextInt(5, 15)));
                    vatpham.itemOptions.add(new ItemOption(77, Util.nextInt(5, 15)));
                    vatpham.itemOptions.add(new ItemOption(103, Util.nextInt(5, 15)));
                    break;
            }

            // Thêm item vào túi của người chơi
            if (vatpham != null) {
                if (Math.random() < 0.01) {
                    // Xử lý trường hợp đặc biệt nếu cần
                } else {
                    vatpham.itemOptions.add(new ItemOption(93, Util.nextInt(1, 3)));
                }
                InventoryService.gI().addItemBag(pl, vatpham, 1);
                InventoryService.gI().sendItemBags(pl);
            }

            // Gửi hiệu ứng mở item cho người chơi
            CombineServiceNew.gI().sendEffectOpenItem(pl, item.template.iconID, (short) 11740);
            // Giảm số lượng item trong túi
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            // Cộng điểm vào SQL
            try (
                    Connection con = DBService.gI().getConnectionForSaveData();
                    PreparedStatement ps = con.prepareStatement(
                            "UPDATE account SET TopQuaHalloween = TopQuaHalloween + 1 WHERE id = ?")) {
                ps.setInt(1, pl.getSession().userId); // ID của người chơi
                ps.executeUpdate();
                Service.getInstance().sendThongBao(pl, "Bạn nhận được 1 điểm mở quà Halloween ");
            } catch (Exception e) {
                Log.error(PlayerDAO.class, e, "Lỗi update top mở quà Halloween cho người chơi " + pl.name);
            }
        } else {
            Service.getInstance().sendThongBao(pl, "Hành trang đã đầy");
        }
    }

    public void hopquatrungthu(Player pl, int num, Item item) {
        /// Cộng điẻm vô sql
        PreparedStatement ps = null;
        Connection con = null;
        try {
            con = DBService.gI().getConnectionForSaveData();
            ps = con.prepareStatement("UPDATE account SET toptrungthu = toptrungthu + ? WHERE id = ?");
            ps.setInt(1, num); // Số điểm để cộng thêm
            ps.setInt(2, pl.getSession().userId); // ID của người chơi
            ps.executeUpdate();
        } catch (Exception e) {
            Log.error(PlayerDAO.class, e, "Lỗi update top trung thu cho người chơi " + pl.name);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
                java.util.logging.Logger.getLogger(PlayerDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        /// Quà
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            Item vatpham = ItemService.gI().createNewItem((short) 2136, 1);
            double tile = Math.random();
            if (tile < 0.01) {
                // vv
            } else {
                vatpham.itemOptions.add(new ItemOption(93, Util.nextInt(1, 10)));
            }
            vatpham.itemOptions.add(new ItemOption(84, Util.nextInt(0, 0)));
            vatpham.itemOptions.add(new ItemOption(50, Util.nextInt(5, 15)));
            vatpham.itemOptions.add(new ItemOption(77, Util.nextInt(5, 15)));
            vatpham.itemOptions.add(new ItemOption(103, Util.nextInt(5, 15)));
            // Thêm item vào túi của người chơi
            InventoryService.gI().addItemBag(pl, vatpham, 1);
            InventoryService.gI().sendItemBags(pl);
            // Gửi hiệu ứng mở item cho người chơi
            CombineServiceNew.gI().sendEffectOpenItem(pl, item.template.iconID, (short) 32083);
            // Giảm số lượng item trong túi
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        } else {
            Service.getInstance().sendThongBao(pl, "Hành trang đã đầy");
        }
    }

    public void hopquanoel(Player pl, int num, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            if (Util.isTrue(40, 100)) { // 40% xác suất để tạo ra nrobang
                // Tạo item nrobang
                Item nrobang = ItemService.gI().createNewItem((short) 925, 1);
                InventoryService.gI().addItemBag(pl, nrobang, 1); // Thêm nrobang vào túi
                InventoryService.gI().sendItemBags(pl);
                short icon1 = item.template.iconID;
                short icon2 = nrobang.template.iconID;
            }
            Item vatpham = ItemService.gI().createNewItem((short) 746, 1);
            // Kiểm tra tỷ lệ để mở ra nrobang (40% xác suất)
            vatpham.itemOptions.add(new ItemOption(50, Util.nextInt(5, 15)));
            vatpham.itemOptions.add(new ItemOption(77, Util.nextInt(5, 15)));
            vatpham.itemOptions.add(new ItemOption(103, Util.nextInt(5, 15)));
            // Cơ hội đặc biệt cho vatpham
            if (Util.isTrue(1, 100)) {
            } else {
                vatpham.itemOptions.add(new ItemOption(93, 1)); // Thêm option cơ bản
            }
            // Thêm vatpham vào túi
            InventoryService.gI().addItemBag(pl, vatpham, 1);
            InventoryService.gI().sendItemBags(pl);
            // Cập nhật điểm sự kiện noel cho người chơi
            try (Connection con = DBService.gI().getConnectionForSaveData();
                    PreparedStatement ps = con
                            .prepareStatement("UPDATE account SET TopNoel = TopNoel + 2 WHERE id = ?")) {
                ps.setInt(1, pl.getSession().userId); // ID của người chơi
                ps.executeUpdate();
                Service.getInstance().sendThongBao(pl, "Bạn nhận được 2 điểm sự kiện noel ");
            } catch (Exception e) {
                Log.error(PlayerDAO.class, e, "Lỗi update top boss cho người chơi " + pl.name);
            }
            // Gửi hiệu ứng mở item cho người chơi
            short icon1 = item.template.iconID;
            short icon2 = vatpham.template.iconID;
            CombineServiceNew.gI().sendEffectOpenItem(pl, icon1, icon2);
            // Giảm số lượng item trong túi
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        } else {
            Service.getInstance().sendThongBao(pl, "Hành trang đã đầy");
        }
    }

    public void hopQuaTanThu(Player pl, Item it) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 14) {
            int gender = pl.gender;
            int[] id = { gender, 6 + gender, 21 + gender, 27 + gender, 12, 194, 441, 442, 443, 444, 445, 446, 447 };
            int[] soluong = { 1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10, 10 };
            int[] option = { 0, 0, 0, 0, 0, 73, 95, 96, 97, 98, 99, 100, 101 };
            int[] param = { 0, 0, 0, 0, 0, 0, 5, 5, 5, 3, 3, 5, 5 };
            int arrLength = id.length - 1;

            for (int i = 0; i < arrLength; i++) {
                if (i < 5) {
                    Item item = ItemService.gI().createNewItem((short) id[i]);
                    RewardService.gI().initBaseOptionClothes(item.template.id, item.template.type, item.itemOptions);
                    item.itemOptions.add(new ItemOption(107, 3));
                    InventoryService.gI().addItemBag(pl, item, 0);
                } else {
                    Item item = ItemService.gI().createNewItem((short) id[i]);
                    item.quantity = soluong[i];
                    item.itemOptions.add(new ItemOption(option[i], param[i]));
                    InventoryService.gI().addItemBag(pl, item, 0);
                }
            }

            int[] idpet = { 916, 917, 918, 942, 943, 944, 1046, 1039, 1040 };

            Item item = ItemService.gI().createNewItem((short) idpet[Util.nextInt(0, idpet.length - 1)]);
            item.itemOptions.add(new ItemOption(50, Util.nextInt(5, 10)));
            item.itemOptions.add(new ItemOption(77, Util.nextInt(5, 10)));
            item.itemOptions.add(new ItemOption(103, Util.nextInt(5, 10)));
            item.itemOptions.add(new ItemOption(93, 3));
            InventoryService.gI().addItemBag(pl, item, 0);

            InventoryService.gI().subQuantityItemsBag(pl, it, 1);
            InventoryService.gI().sendItemBags(pl);
            Service.getInstance().sendThongBao(pl, "Chúc bạn chơi game vui vẻ");
        } else {
            Service.getInstance().sendThongBao(pl, "Cần tối thiểu 14 ô trống để nhận thưởng");
        }
    }

    private void openbox2010(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            short[] temp = { 17, 16, 15, 675, 676, 677, 678, 679, 680, 681, 580, 581, 582 };
            int[][] gold = { { 5000, 20000 } };
            byte index = (byte) Util.nextInt(0, temp.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;

            Item it = ItemService.gI().createNewItem(temp[index]);

            if (temp[index] >= 15 && temp[index] <= 17) {
                it.itemOptions.add(new ItemOption(73, 0));

            } else if (temp[index] >= 580 && temp[index] <= 582 || temp[index] >= 675 && temp[index] <= 681) { // cải
                // trang

                it.itemOptions.add(new ItemOption(77, Util.nextInt(20, 30)));
                it.itemOptions.add(new ItemOption(103, Util.nextInt(20, 30)));
                it.itemOptions.add(new ItemOption(50, Util.nextInt(20, 30)));
                it.itemOptions.add(new ItemOption(95, Util.nextInt(5, 15)));
                it.itemOptions.add(new ItemOption(96, Util.nextInt(5, 15)));

                if (Util.isTrue(1, 200)) {
                    it.itemOptions.add(new ItemOption(74, 0));
                } else {
                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                }

            } else {
                it.itemOptions.add(new ItemOption(73, 0));
            }
            InventoryService.gI().addItemBag(pl, it, 0);
            icon[1] = it.template.iconID;

            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);

            CombineServiceNew.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.getInstance().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void capsule8thang3(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            short[] temp = { 17, 16, 15, 675, 676, 677, 678, 679, 680, 681, 580, 581, 582, 1154, 1155, 1156, 860, 1041,
                    1042, 1043, 1103, 1104, 1105, 1106, 954, 955 };
            byte index = (byte) Util.nextInt(0, temp.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;

            Item it = ItemService.gI().createNewItem(temp[index]);

            if (Util.isTrue(30, 100)) {
                int ruby = Util.nextInt(1, 5);
                pl.inventory.ruby += ruby;
                CombineServiceNew.gI().sendEffectOpenItem(pl, item.template.iconID, (short) 7743);
                PlayerService.gI().sendInfoHpMpMoney(pl);
                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                InventoryService.gI().sendItemBags(pl);
                Service.getInstance().sendThongBao(pl, "Bạn nhận được " + ruby + " Hồng Ngọc");
                return;
            }
            if (it.template.type == 5) { // cải trang

                it.itemOptions.add(new ItemOption(50, Util.nextInt(20, 35)));
                it.itemOptions.add(new ItemOption(77, Util.nextInt(20, 35)));
                it.itemOptions.add(new ItemOption(103, Util.nextInt(20, 35)));
                it.itemOptions.add(new ItemOption(117, Util.nextInt(10, 20)));

            } else if (it.template.id == 954 || it.template.id == 955) {
                it.itemOptions.add(new ItemOption(50, Util.nextInt(10, 20)));
                it.itemOptions.add(new ItemOption(77, Util.nextInt(10, 20)));
                it.itemOptions.add(new ItemOption(103, Util.nextInt(10, 20)));
            }

            if (it.template.type == 5 || it.template.id == 954 || it.template.id == 955) {
                if (Util.isTrue(1, 200)) {
                    it.itemOptions.add(new ItemOption(74, 0));
                } else {
                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                }
            }
            InventoryService.gI().addItemBag(pl, it, 0);
            icon[1] = it.template.iconID;

            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);

            CombineServiceNew.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.getInstance().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    public void openboxsukien(Player pl, Item item, int idsukien) {
        try {
            switch (idsukien) {
                case 1:
                    if (Manager.EVENT_SEVER == idsukien) {
                        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
                            short[] temp = { 16, 15, 865, 999, 1000, 1001, 739, 742, 743 };
                            int[][] gold = { { 5000, 20000 } };
                            byte index = (byte) Util.nextInt(0, temp.length - 1);
                            short[] icon = new short[2];
                            icon[0] = item.template.iconID;

                            Item it = ItemService.gI().createNewItem(temp[index]);

                            if (temp[index] >= 15 && temp[index] <= 16) {
                                it.itemOptions.add(new ItemOption(73, 0));

                            } else if (temp[index] == 865) {

                                it.itemOptions.add(new ItemOption(30, 0));

                                if (Util.isTrue(1, 30)) {
                                    it.itemOptions.add(new ItemOption(93, 365));
                                } else {
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                }
                            } else if (temp[index] == 999) { // mèo mun
                                it.itemOptions.add(new ItemOption(77, 15));
                                it.itemOptions.add(new ItemOption(30, 0));
                                if (Util.isTrue(1, 50)) {
                                    it.itemOptions.add(new ItemOption(74, 0));
                                } else {
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                }
                            } else if (temp[index] == 1000) { // xiên cá
                                it.itemOptions.add(new ItemOption(103, 15));
                                it.itemOptions.add(new ItemOption(30, 0));
                                if (Util.isTrue(1, 50)) {
                                    it.itemOptions.add(new ItemOption(74, 0));
                                } else {
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                }
                            } else if (temp[index] == 1001) { // Phóng heo
                                it.itemOptions.add(new ItemOption(50, 15));
                                it.itemOptions.add(new ItemOption(30, 0));
                                if (Util.isTrue(1, 50)) {
                                    it.itemOptions.add(new ItemOption(74, 0));
                                } else {
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                }

                            } else if (temp[index] == 739) { // cải trang Billes

                                it.itemOptions.add(new ItemOption(77, Util.nextInt(30, 40)));
                                it.itemOptions.add(new ItemOption(103, Util.nextInt(30, 40)));
                                it.itemOptions.add(new ItemOption(50, Util.nextInt(30, 45)));

                                if (Util.isTrue(1, 100)) {
                                    it.itemOptions.add(new ItemOption(74, 0));
                                } else {
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                }

                            } else if (temp[index] == 742) { // cải trang Caufila

                                it.itemOptions.add(new ItemOption(77, Util.nextInt(30, 40)));
                                it.itemOptions.add(new ItemOption(103, Util.nextInt(30, 40)));
                                it.itemOptions.add(new ItemOption(50, Util.nextInt(30, 45)));

                                if (Util.isTrue(1, 100)) {
                                    it.itemOptions.add(new ItemOption(74, 0));
                                } else {
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                }
                            } else if (temp[index] == 743) { // chổi bay
                                it.itemOptions.add(new ItemOption(30, 0));
                                if (Util.isTrue(1, 50)) {
                                    it.itemOptions.add(new ItemOption(74, 0));
                                } else {
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                }

                            } else {
                                it.itemOptions.add(new ItemOption(73, 0));
                            }
                            InventoryService.gI().addItemBag(pl, it, 0);
                            icon[1] = it.template.iconID;

                            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                            InventoryService.gI().sendItemBags(pl);

                            CombineServiceNew.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
                        } else {
                            Service.getInstance().sendThongBao(pl, "Hàng trang đã đầy");
                        }
                        break;
                    } else {
                        Service.getInstance().sendThongBao(pl, "Sự kiện đã kết thúc");
                    }
                case ConstEvent.SU_KIEN_20_11:
                    if (Manager.EVENT_SEVER == idsukien) {
                        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
                            short[] temp = { 16, 15, 1039, 954, 955, 710, 711, 1040, 2023, 999, 1000, 1001 };
                            byte index = (byte) Util.nextInt(0, temp.length - 1);
                            short[] icon = new short[2];
                            icon[0] = item.template.iconID;
                            Item it = ItemService.gI().createNewItem(temp[index]);
                            if (temp[index] >= 15 && temp[index] <= 16) {
                                it.itemOptions.add(new ItemOption(73, 0));
                            } else if (temp[index] == 1039) {
                                it.itemOptions.add(new ItemOption(50, 10));
                                it.itemOptions.add(new ItemOption(77, 10));
                                it.itemOptions.add(new ItemOption(103, 10));
                                it.itemOptions.add(new ItemOption(30, 0));
                                it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                            } else if (temp[index] == 954) {
                                it.itemOptions.add(new ItemOption(50, 15));
                                it.itemOptions.add(new ItemOption(77, 15));
                                it.itemOptions.add(new ItemOption(103, 15));
                                it.itemOptions.add(new ItemOption(30, 0));
                                if (Util.isTrue(79, 80)) {
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                }
                            } else if (temp[index] == 955) {
                                it.itemOptions.add(new ItemOption(50, 20));
                                it.itemOptions.add(new ItemOption(77, 20));
                                it.itemOptions.add(new ItemOption(103, 20));
                                it.itemOptions.add(new ItemOption(30, 0));
                                if (Util.isTrue(79, 80)) {
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                }
                            } else if (temp[index] == 710) {// cải trang quy lão kame
                                it.itemOptions.add(new ItemOption(50, 22));
                                it.itemOptions.add(new ItemOption(77, 20));
                                it.itemOptions.add(new ItemOption(103, 20));
                                it.itemOptions.add(new ItemOption(194, 0));
                                it.itemOptions.add(new ItemOption(160, 35));
                                if (Util.isTrue(99, 100)) {
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                }
                            } else if (temp[index] == 711) { // cải trang jacky chun
                                it.itemOptions.add(new ItemOption(50, 23));
                                it.itemOptions.add(new ItemOption(77, 21));
                                it.itemOptions.add(new ItemOption(103, 21));
                                it.itemOptions.add(new ItemOption(195, 0));
                                it.itemOptions.add(new ItemOption(160, 50));
                                if (Util.isTrue(99, 100)) {
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                }
                            } else if (temp[index] == 1040) {
                                it.itemOptions.add(new ItemOption(50, 10));
                                it.itemOptions.add(new ItemOption(77, 10));
                                it.itemOptions.add(new ItemOption(103, 10));
                                it.itemOptions.add(new ItemOption(30, 0));
                                it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                            } else if (temp[index] == 2023) {
                                it.itemOptions.add(new ItemOption(30, 0));
                            } else if (temp[index] == 999) { // mèo mun
                                it.itemOptions.add(new ItemOption(77, 15));
                                it.itemOptions.add(new ItemOption(30, 0));
                                if (Util.isTrue(1, 50)) {
                                    it.itemOptions.add(new ItemOption(74, 0));
                                } else {
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                }
                            } else if (temp[index] == 1000) { // xiên cá
                                it.itemOptions.add(new ItemOption(103, 15));
                                it.itemOptions.add(new ItemOption(30, 0));
                                if (Util.isTrue(1, 50)) {
                                    it.itemOptions.add(new ItemOption(74, 0));
                                } else {
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                }
                            } else if (temp[index] == 1001) { // Phóng heo
                                it.itemOptions.add(new ItemOption(50, 15));
                                it.itemOptions.add(new ItemOption(30, 0));
                                if (Util.isTrue(1, 50)) {
                                    it.itemOptions.add(new ItemOption(74, 0));
                                } else {
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                }
                            } else {
                                it.itemOptions.add(new ItemOption(73, 0));
                            }
                            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                            icon[1] = it.template.iconID;
                            CombineServiceNew.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
                            InventoryService.gI().addItemBag(pl, it, 0);
                            int ruby = Util.nextInt(1, 5);
                            pl.inventory.ruby += ruby;
                            InventoryService.gI().sendItemBags(pl);
                            PlayerService.gI().sendInfoHpMpMoney(pl);
                            Service.getInstance().sendThongBao(pl, "Bạn được tặng kèm " + ruby + " Hồng Ngọc");
                        } else {
                            Service.getInstance().sendThongBao(pl, "Hàng trang đã đầy");
                        }
                    } else {
                        Service.getInstance().sendThongBao(pl, "Sự kiện đã kết thúc");
                    }
                    break;
                case ConstEvent.SU_KIEN_NOEL:
                    if (Manager.EVENT_SEVER == idsukien) {
                        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
                            int spl = Util.nextInt(441, 445);
                            int dnc = Util.nextInt(220, 224);
                            int nr = Util.nextInt(16, 18);
                            int nrBang = Util.nextInt(926, 931);

                            if (Util.isTrue(5, 90)) {
                                int ruby = Util.nextInt(1, 3);
                                pl.inventory.ruby += ruby;
                                CombineServiceNew.gI().sendEffectOpenItem(pl, item.template.iconID, (short) 7743);
                                PlayerService.gI().sendInfoHpMpMoney(pl);
                                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                InventoryService.gI().sendItemBags(pl);
                                Service.getInstance().sendThongBao(pl, "Bạn nhận được " + ruby + " Hồng Ngọc");
                            } else {
                                int[] temp = { spl, dnc, nr, nrBang, 387, 390, 393, 821, 822, 746, 380, 999, 1000, 1001,
                                        936, 2022 };
                                byte index = (byte) Util.nextInt(0, temp.length - 1);
                                short[] icon = new short[2];
                                icon[0] = item.template.iconID;
                                Item it = ItemService.gI().createNewItem((short) temp[index]);

                                if (temp[index] >= 441 && temp[index] <= 443) {// sao pha le
                                    it.itemOptions.add(new ItemOption(temp[index] - 346, 5));
                                    it.quantity = 10;
                                } else if (temp[index] >= 444 && temp[index] <= 445) {
                                    it.itemOptions.add(new ItemOption(temp[index] - 346, 3));
                                    it.quantity = 10;
                                } else if (temp[index] >= 220 && temp[index] <= 224) { // da nang cap
                                    it.quantity = 10;
                                } else if (temp[index] >= 387 && temp[index] <= 393) { // mu noel do
                                    it.itemOptions.add(new ItemOption(50, Util.nextInt(30, 40)));
                                    it.itemOptions.add(new ItemOption(77, Util.nextInt(30, 40)));
                                    it.itemOptions.add(new ItemOption(103, Util.nextInt(30, 40)));
                                    it.itemOptions.add(new ItemOption(80, Util.nextInt(10, 20)));
                                    it.itemOptions.add(new ItemOption(106, 0));
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 3)));
                                    it.itemOptions.add(new ItemOption(199, 0));
                                } else if (temp[index] == 936) { // tuan loc
                                    it.itemOptions.add(new ItemOption(50, Util.nextInt(5, 10)));
                                    it.itemOptions.add(new ItemOption(77, Util.nextInt(5, 10)));
                                    it.itemOptions.add(new ItemOption(103, Util.nextInt(5, 10)));
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(3, 30)));
                                } else if (temp[index] == 822) { // cay thong noel
                                    it.itemOptions.add(new ItemOption(50, Util.nextInt(10, 20)));
                                    it.itemOptions.add(new ItemOption(77, Util.nextInt(10, 20)));
                                    it.itemOptions.add(new ItemOption(103, Util.nextInt(10, 20)));
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(3, 30)));
                                    it.itemOptions.add(new ItemOption(30, 0));
                                    it.itemOptions.add(new ItemOption(74, 0));
                                } else if (temp[index] == 746) { // xe truot tuyet
                                    it.itemOptions.add(new ItemOption(74, 0));
                                    it.itemOptions.add(new ItemOption(30, 0));
                                    if (Util.isTrue(99, 100)) {
                                        it.itemOptions.add(new ItemOption(93, Util.nextInt(30, 360)));
                                    }
                                } else if (temp[index] == 999) { // mèo mun
                                    it.itemOptions.add(new ItemOption(77, 15));
                                    it.itemOptions.add(new ItemOption(74, 0));
                                    it.itemOptions.add(new ItemOption(30, 0));
                                    if (Util.isTrue(99, 100)) {
                                        it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                    }
                                } else if (temp[index] == 1000) { // xiên cá
                                    it.itemOptions.add(new ItemOption(103, 15));
                                    it.itemOptions.add(new ItemOption(74, 0));
                                    it.itemOptions.add(new ItemOption(30, 0));
                                    if (Util.isTrue(99, 100)) {
                                        it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                    }
                                } else if (temp[index] == 1001) { // Phóng heo
                                    it.itemOptions.add(new ItemOption(50, 15));
                                    it.itemOptions.add(new ItemOption(74, 0));
                                    it.itemOptions.add(new ItemOption(30, 0));
                                    if (Util.isTrue(99, 100)) {
                                        it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                    }
                                } else if (temp[index] == 2022 || temp[index] == 821) {
                                    it.itemOptions.add(new ItemOption(30, 0));
                                } else {
                                    it.itemOptions.add(new ItemOption(73, 0));
                                }
                                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                icon[1] = it.template.iconID;
                                CombineServiceNew.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
                                InventoryService.gI().addItemBag(pl, it, 0);
                                InventoryService.gI().sendItemBags(pl);
                            }
                        } else {
                            Service.getInstance().sendThongBao(pl, "Hàng trang đã đầy");
                        }
                    } else {
                        Service.getInstance().sendThongBao(pl, "Sự kiện đã kết thúc");
                    }
                    break;
                case ConstEvent.SU_KIEN_TET:
                    if (Manager.EVENT_SEVER == idsukien) {
                        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
                            short[] icon = new short[2];
                            icon[0] = item.template.iconID;
                            RandomCollection<Integer> rd = Manager.HOP_QUA_TET;
                            int tempID = rd.next();
                            Item it = ItemService.gI().createNewItem((short) tempID);
                            if (it.template.type == 11) {// FLAGBAG
                                it.itemOptions.add(new ItemOption(50, Util.nextInt(5, 20)));
                                it.itemOptions.add(new ItemOption(77, Util.nextInt(5, 20)));
                                it.itemOptions.add(new ItemOption(103, Util.nextInt(5, 20)));
                            } else if (tempID >= 1159 && tempID <= 1161) {
                                it.itemOptions.add(new ItemOption(50, Util.nextInt(20, 30)));
                                it.itemOptions.add(new ItemOption(77, Util.nextInt(20, 30)));
                                it.itemOptions.add(new ItemOption(103, Util.nextInt(20, 30)));
                                it.itemOptions.add(new ItemOption(106, 0));
                            } else if (tempID == ConstItem.CAI_TRANG_SSJ_3_WHITE) {
                                it.itemOptions.add(new ItemOption(50, Util.nextInt(30, 40)));
                                it.itemOptions.add(new ItemOption(77, Util.nextInt(30, 40)));
                                it.itemOptions.add(new ItemOption(103, Util.nextInt(30, 40)));
                                it.itemOptions.add(new ItemOption(5, Util.nextInt(10, 25)));
                                it.itemOptions.add(new ItemOption(104, Util.nextInt(5, 15)));
                            }
                            int type = it.template.type;
                            if (type == 5 || type == 11) {// cải trang & flagbag
                                if (Util.isTrue(199, 200)) {
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                }
                                it.itemOptions.add(new ItemOption(199, 0));// KHÔNG THỂ GIA HẠN
                            } else if (type == 23) {// thú cưỡi
                                if (Util.isTrue(199, 200)) {
                                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 5)));
                                }
                            }
                            if (tempID >= ConstItem.MANH_AO && tempID <= ConstItem.MANH_GANG_TAY) {
                                it.quantity = Util.nextInt(5, 15);
                            } else {
                                it.itemOptions.add(new ItemOption(74, 0));
                            }
                            icon[1] = it.template.iconID;
                            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                            CombineServiceNew.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
                            InventoryService.gI().addItemBag(pl, it, 0);
                            InventoryService.gI().sendItemBags(pl);
                            break;
                        } else {
                            Service.getInstance().sendThongBao(pl, "Hàng trang đã đầy");
                        }
                    } else {
                        Service.getInstance().sendThongBao(pl, "Sự kiện đã kết thúc");
                    }
                    break;
            }
        } catch (Exception e) {
            logger.error("Lỗi mở hộp quà", e);
        }
    }

    private void openboxkichhoat(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            short[] temp = { 76, 188, 189, 190, 441, 442, 447, 2010, 2009, 865, 938, 939, 940, 16, 17, 18, 19, 20, 946,
                    947, 948, 382, 383, 384, 385 };
            int[][] gold = { { 5000, 20000 } };
            byte index = (byte) Util.nextInt(0, temp.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;
            if (index <= 3 && index >= 0) {
                pl.inventory.addGold(Util.nextInt(gold[0][0], gold[0][1]));
                PlayerService.gI().sendInfoHpMpMoney(pl);
                icon[1] = 930;
            } else {

                Item it = ItemService.gI().createNewItem(temp[index]);
                if (temp[index] == 441) {
                    it.itemOptions.add(new ItemOption(95, 5));
                } else if (temp[index] == 442) {
                    it.itemOptions.add(new ItemOption(96, 5));
                } else if (temp[index] == 447) {
                    it.itemOptions.add(new ItemOption(101, 5));
                } else if (temp[index] >= 2009 && temp[index] <= 2010) {
                    it.itemOptions.add(new ItemOption(30, 0));
                } else if (temp[index] == 865) {
                    it.itemOptions.add(new ItemOption(30, 0));
                    if (Util.isTrue(1, 20)) {
                        it.itemOptions.add(new ItemOption(93, 365));
                    } else {
                        it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                    }
                } else if (temp[index] >= 938 && temp[index] <= 940) {
                    it.itemOptions.add(new ItemOption(77, 35));
                    it.itemOptions.add(new ItemOption(103, 35));
                    it.itemOptions.add(new ItemOption(50, 35));
                    if (Util.isTrue(1, 50)) {
                        it.itemOptions.add(new ItemOption(116, 0));
                    } else {
                        it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                    }
                } else if (temp[index] >= 946 && temp[index] <= 948) {
                    it.itemOptions.add(new ItemOption(77, 35));
                    it.itemOptions.add(new ItemOption(103, 35));
                    it.itemOptions.add(new ItemOption(50, 35));
                    if (Util.isTrue(1, 20)) {
                        it.itemOptions.add(new ItemOption(93, 365));
                    } else {
                        it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                    }
                } else {
                    it.itemOptions.add(new ItemOption(73, 0));
                }
                InventoryService.gI().addItemBag(pl, it, 0);
                icon[1] = it.template.iconID;

            }
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);

            CombineServiceNew.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.getInstance().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void openPhieuCaiTrangHaiTac(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            Item ct = ItemService.gI().createNewItem((short) Util.nextInt(618, 626));
            ct.itemOptions.add(new ItemOption(147, 3));
            ct.itemOptions.add(new ItemOption(77, 3));
            ct.itemOptions.add(new ItemOption(103, 3));
            ct.itemOptions.add(new ItemOption(149, 0));
            if (item.template.id == 2006) {
                ct.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
            } else if (item.template.id == 2007) {
                ct.itemOptions.add(new ItemOption(93, Util.nextInt(7, 30)));
            }
            InventoryService.gI().addItemBag(pl, ct, 0);
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);
            CombineServiceNew.gI().sendEffectOpenItem(pl, item.template.iconID, ct.template.iconID);
        } else {
            Service.getInstance().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void eatGrapes(Player pl, Item item) {
        int percentCurrentStatima = pl.nPoint.stamina * 100 / pl.nPoint.maxStamina;
        if (percentCurrentStatima > 50) {
            Service.getInstance().sendThongBao(pl, "Thể lực vẫn còn trên 50%");
            return;
        } else if (item.template.id == 211) {
            pl.nPoint.stamina = pl.nPoint.maxStamina;
            Service.getInstance().sendThongBao(pl, "Thể lực của bạn đã được hồi phục 100%");
        } else if (item.template.id == 212) {
            pl.nPoint.stamina += (pl.nPoint.maxStamina * 20 / 100);
            Service.getInstance().sendThongBao(pl, "Thể lực của bạn đã được hồi phục 20%");
        }
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        InventoryService.gI().sendItemBags(pl);
        PlayerService.gI().sendCurrentStamina(pl);
    }

    private void openCSKB(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            short[] temp = { 76, 188, 189, 190, 381, 382, 383, 384, 385 };
            int[][] gold = { { 5000, 20000 } };
            byte index = (byte) Util.nextInt(0, temp.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;
            if (index <= 3) {
                pl.inventory.addGold(Util.nextInt(gold[0][0], gold[0][1]));
                PlayerService.gI().sendInfoHpMpMoney(pl);
                icon[1] = 930;
            } else {
                Item it = ItemService.gI().createNewItem(temp[index]);
                it.itemOptions.add(new ItemOption(73, 0));
                InventoryService.gI().addItemBag(pl, it, 0);
                icon[1] = it.template.iconID;
            }
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);

            CombineServiceNew.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.getInstance().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void useItemTime(Player pl, Item item) {
        boolean updatePoint = false;
        switch (item.template.id) {
            case 382: // bổ huyết
                if (pl.itemTime.isUseBoHuyet2) {
                    Service.getInstance().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeBoHuyet = System.currentTimeMillis();
                pl.itemTime.isUseBoHuyet = true;
                updatePoint = true;
                break;
            case 383: // bổ khí
                if (pl.itemTime.isUseBoKhi2) {
                    Service.getInstance().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeBoKhi = System.currentTimeMillis();
                pl.itemTime.isUseBoKhi = true;
                updatePoint = true;
                break;
            case 384: // giáp xên
                if (pl.itemTime.isUseGiapXen2) {
                    Service.getInstance().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeGiapXen = System.currentTimeMillis();
                pl.itemTime.isUseGiapXen = true;
                updatePoint = true;
                break;
            case 381: // cuồng nộ
                if (pl.itemTime.isUseCuongNo2) {
                    Service.getInstance().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeCuongNo = System.currentTimeMillis();
                pl.itemTime.isUseCuongNo = true;
                updatePoint = true;
                break;
            case 385: // ẩn danh
                pl.itemTime.lastTimeAnDanh = System.currentTimeMillis();
                pl.itemTime.isUseAnDanh = true;
                break;
            case ConstItem.BO_HUYET_2: // bổ huyết 2
                if (pl.itemTime.isUseBoHuyet) {
                    Service.getInstance().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeBoHuyet2 = System.currentTimeMillis();
                pl.itemTime.isUseBoHuyet2 = true;
                updatePoint = true;
                break;
            case ConstItem.BO_KHI_2: // bổ khí 2
                if (pl.itemTime.isUseBoKhi) {
                    Service.getInstance().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeBoKhi2 = System.currentTimeMillis();
                pl.itemTime.isUseBoKhi2 = true;
                updatePoint = true;
                break;
            case ConstItem.GIAP_XEN_BO_HUNG_2: // giáp xên 2
                if (pl.itemTime.isUseGiapXen) {
                    Service.getInstance().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeGiapXen2 = System.currentTimeMillis();
                pl.itemTime.isUseGiapXen2 = true;
                updatePoint = true;
                break;
            case ConstItem.CUONG_NO_2: // cuồng nộ 2
                if (pl.itemTime.isUseCuongNo) {
                    Service.getInstance().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeCuongNo2 = System.currentTimeMillis();
                pl.itemTime.isUseCuongNo2 = true;
                updatePoint = true;
                break;
            case 379: // máy dò
                pl.itemTime.lastTimeUseMayDo = System.currentTimeMillis();
                pl.itemTime.isUseMayDo = true;
                break;
            case 663: // bánh pudding
            case 664: // xúc xíc
            case 665: // kem dâu
            case 666: // mì ly
            case 667: // sushi
                pl.itemTime.lastTimeEatMeal = System.currentTimeMillis();
                pl.itemTime.isEatMeal = true;
                ItemTimeService.gI().removeItemTime(pl, pl.itemTime.iconMeal);
                pl.itemTime.iconMeal = item.template.iconID;
                updatePoint = true;
                break;
            case ConstItem.BANH_CHUNG_CHIN:
                pl.itemTime.lastTimeBanhChung = System.currentTimeMillis();
                pl.itemTime.isUseBanhChung = true;
                updatePoint = true;
                break;
            case ConstItem.BANH_TET_CHIN:
                pl.itemTime.lastTimeBanhTet = System.currentTimeMillis();
                pl.itemTime.isUseBanhTet = true;
                updatePoint = true;
        }
        if (updatePoint) {
            Service.getInstance().point(pl);
        }
        ItemTimeService.gI().sendAllItemTime(pl);
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        InventoryService.gI().sendItemBags(pl);
    }

    private void controllerCallRongThan(Player pl, Item item) {
        int tempId = item.template.id;
        if (tempId >= SummonDragon.NGOC_RONG_1_SAO && tempId <= SummonDragon.NGOC_RONG_7_SAO) {
            switch (tempId) {
                case SummonDragon.NGOC_RONG_1_SAO:
                case SummonDragon.NGOC_RONG_2_SAO:
                case SummonDragon.NGOC_RONG_3_SAO:
                    SummonDragon.gI().openMenuSummonShenron(pl, (byte) (tempId - 13), SummonDragon.DRAGON_SHENRON);
                    break;
                default:
                    NpcService.gI().createMenuConMeo(pl, ConstNpc.TUTORIAL_SUMMON_DRAGON, -1,
                            "Bạn chỉ có thể gọi rồng từ ngọc 3 sao, 2 sao, 1 sao", "Hướng\ndẫn thêm\n(mới)", "OK");
                    break;
            }
        } else if (tempId == SummonDragon.NGOC_RONG_SIEU_CAP) {
            SummonDragon.gI().openMenuSummonShenron(pl, (byte) 1015, SummonDragon.DRAGON_BLACK_SHENRON);
        } else if (tempId >= SummonDragon.NGOC_RONG_BANG[0] && tempId <= SummonDragon.NGOC_RONG_BANG[6]) {
            switch (tempId) {
                case 925:
                    SummonDragon.gI().openMenuSummonShenron(pl, (byte) 925, SummonDragon.DRAGON_ICE_SHENRON);
                    break;
                default:
                    Service.getInstance().sendThongBao(pl, "Bạn chỉ có thể gọi rồng băng từ ngọc 1 sao");
                    break;
            }
        }
    }

    private void learnSkill(Player pl, Item item) {
        Message msg;
        try {
            if (item.template.gender == pl.gender || item.template.gender == 3) {
                String[] subName = item.template.name.split("");
                byte level = Byte.parseByte(subName[subName.length - 1]);
                Skill curSkill = SkillUtil.getSkillByItemID(pl, item.template.id);
                if (curSkill.point == 7) {
                    Service.getInstance().sendThongBao(pl, "Kỹ năng đã đạt tối đa!");
                } else {
                    if (curSkill.point == 0) {
                        if (level == 1) {
                            curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id),
                                    level);
                            SkillUtil.setSkill(pl, curSkill);
                            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                            msg = Service.getInstance().messageSubCommand((byte) 23);
                            msg.writer().writeShort(curSkill.skillId);
                            pl.sendMessage(msg);
                            msg.cleanup();
                        } else {
                            Skill skillNeed = SkillUtil
                                    .createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id), level);
                            Service.getInstance().sendThongBao(pl,
                                    "Vui lòng học " + skillNeed.template.name + " cấp " + skillNeed.point + " trước!");
                        }
                    } else {
                        if (curSkill.point + 1 == level) {
                            curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id),
                                    level);
                            // System.out.println(curSkill.template.name + " - " + curSkill.point);
                            SkillUtil.setSkill(pl, curSkill);
                            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                            msg = Service.getInstance().messageSubCommand((byte) 62);
                            msg.writer().writeShort(curSkill.skillId);
                            pl.sendMessage(msg);
                            msg.cleanup();
                        } else {
                            Service.getInstance().sendThongBao(pl, "Vui lòng học " + curSkill.template.name + " cấp "
                                    + (curSkill.point + 1) + " trước!");
                        }
                    }
                    InventoryService.gI().sendItemBags(pl);
                }
            } else {
                Service.getInstance().sendThongBao(pl, "Không thể thực hiện");

            }
        } catch (Exception e) {
            Log.error(UseItem.class, e);
        }
    }

    private void useTDLT(Player pl, Item item) {
        if (pl.itemTime.isUseTDLT) {
            ItemTimeService.gI().turnOffTDLT(pl, item);
        } else {
            ItemTimeService.gI().turnOnTDLT(pl, item);
        }
    }

    private void usePorata(Player pl) {
        if (pl.pet == null || pl.fusion.typeFusion == 4) {
            Service.getInstance().sendThongBao(pl, "Không thể thực hiện");
        } else {
            if (pl.fusion.typeFusion == ConstPlayer.NON_FUSION) {
                pl.pet.fusion(true);
            } else {
                pl.pet.unFusion();
            }
        }
    }

    private void usePorata2(Player pl) {
        if (pl.pet == null || pl.fusion.typeFusion == 4 || pl.fusion.typeFusion == 6 || pl.fusion.typeFusion == 10
                || pl.fusion.typeFusion == 12) {
            Service.getInstance().sendThongBao(pl, "Không thể thực hiện");
        } else if (pl.fusion.typeFusion == ConstPlayer.NON_FUSION) {
            pl.pet.fusion2(true);
        } else {
            pl.pet.unFusion();
        }
    }

    private void openCapsuleUI(Player pl) {
        if (pl.isHoldNamecBall) {
            NamekBallWar.gI().dropBall(pl);
            Service.getInstance().sendFlagBag(pl);
        }
        pl.iDMark.setTypeChangeMap(ConstMap.CHANGE_CAPSULE);
        ChangeMapService.gI().openChangeMapTab(pl);
    }

    public void choseMapCapsule(Player pl, int index) {
        int zoneId = -1;
        if (index < 0 || index >= pl.mapCapsule.size()) {
            return;
        }
        Zone zoneChose = pl.mapCapsule.get(index);
        if (index != 0 || zoneChose.map.mapId == 21 || zoneChose.map.mapId == 22 || zoneChose.map.mapId == 23) {
            if (!(pl.zone != null && pl.zone instanceof ZSnakeRoad)) {
                pl.mapBeforeCapsule = pl.zone;
            } else {
                pl.mapBeforeCapsule = null;
            }
        } else {
            zoneId = pl.mapBeforeCapsule != null ? pl.mapBeforeCapsule.zoneId : -1;
            pl.mapBeforeCapsule = null;
        }
        ChangeMapService.gI().changeMapBySpaceShip(pl, pl.mapCapsule.get(index).map.mapId, zoneId, -1);
    }

    private void upSkillPet(Player pl, Item item) {
        if (pl.pet == null) {
            Service.getInstance().sendThongBao(pl, "Không thể thực hiện");
            return;
        }
        try {
            switch (item.template.id) {
                case 402: // skill 1
                    if (SkillUtil.upSkillPet(pl.pet.playerSkill.skills, 0)) {
                        Service.getInstance().chatJustForMe(pl, pl.pet, "Cảm ơn sư phụ");
                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        Service.getInstance().sendThongBao(pl, "Không thể thực hiện");
                    }
                    break;
                case 403: // skill 2
                    if (SkillUtil.upSkillPet(pl.pet.playerSkill.skills, 1)) {
                        Service.getInstance().chatJustForMe(pl, pl.pet, "Cảm ơn sư phụ");
                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        Service.getInstance().sendThongBao(pl, "Không thể thực hiện");
                    }
                    break;
                case 404: // skill 3
                    if (SkillUtil.upSkillPet(pl.pet.playerSkill.skills, 2)) {
                        Service.getInstance().chatJustForMe(pl, pl.pet, "Cảm ơn sư phụ");
                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        Service.getInstance().sendThongBao(pl, "Không thể thực hiện");
                    }
                    break;
                case 759: // skill 4
                    if (SkillUtil.upSkillPet(pl.pet.playerSkill.skills, 3)) {
                        Service.getInstance().chatJustForMe(pl, pl.pet, "Cảm ơn sư phụ");
                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        Service.getInstance().sendThongBao(pl, "Không thể thực hiện");
                    }
                    break;
            }
        } catch (Exception e) {
            Service.getInstance().sendThongBao(pl, "Không thể thực hiện");
        }
    }

    private void Chuong(Player player, Item item) {
        // Kiểm tra hành trang trước tiên
        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.getInstance().sendThongBao(player, "Hành trang không đủ chỗ trống.");
            return;
        }
        List<Player> bosses = player.zone.getBosses();
        Boss tuanLoc = (Boss) bosses.stream()
                .filter(boss -> boss instanceof Boss && boss.id == BossFactory.TUAN_LOC_EVENT && !player.isDie())
                .findFirst()
                .orElse(null);
        if (tuanLoc == null) {
            Service.getInstance().sendThongBao(player, "Không tìm thấy tuần lộc");
            return;
        }
        TuanLocEvent tuanlocEvent = (TuanLocEvent) tuanLoc;
        if (tuanlocEvent.checkNhatChuong()) {
            Service.getInstance().sendThongBao(player, "Đã có người đến trước, hãy đến sớm hơn");
            return;
        }

        // Xử lý tỷ lệ xịt
        int successRate = 10;
        boolean isSuccess = Util.nextInt(100) < successRate;

        // Sói nhặt xương
        tuanlocEvent.NhatChuong();
        Service.getInstance().chat(tuanLoc, "Ứ ứ nhạc đâu mà bay vậy");

        // Xác định vị trí thả vật phẩm
        int x = player.location.x;
        if (x < 0 || x >= player.zone.map.mapWidth) {
            return;
        }
        int y = player.zone.map.yPhysicInTop(x, player.location.y - 24);
        ItemMap itemMap = new ItemMap(player.zone, 2150, 1, x, y, player.id);
        itemMap.isPickedUp = true;
        itemMap.createTime -= 23000;
        Service.getInstance().dropItemMap(player.zone, itemMap);

        // Trừ vật phẩm khỏi túi đồ
        InventoryService.gI().subQuantityItemsBag(player, item, 1);
        InventoryService.gI().sendItemBags(player);

        if (isSuccess) {
            Item vatpham = ItemService.gI().createNewItem((short) 2152, 1);
            if (Util.isTrue(1, 100)) {
                // vv
            } else {
                vatpham.itemOptions.add(new ItemOption(93, 1));
            }
            vatpham.itemOptions.add(new ItemOption(50, Util.nextInt(5, 13)));
            vatpham.itemOptions.add(new ItemOption(77, Util.nextInt(5, 13)));
            vatpham.itemOptions.add(new ItemOption(103, Util.nextInt(5, 13)));

            InventoryService.gI().addItemBag(player, vatpham, 0);
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    Service.getInstance().sendThongBao(player, "Bạn vừa nhận được " + vatpham.template.name);
                    InventoryService.gI().sendItemBags(player);
                } catch (InterruptedException e) {

                }
            }).start();
        } else {
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    Service.getInstance().sendThongBao(player, "Chúc bạn may mắn lần sau ");
                } catch (InterruptedException e) {
                }
            }).start();
        }
    }
}
