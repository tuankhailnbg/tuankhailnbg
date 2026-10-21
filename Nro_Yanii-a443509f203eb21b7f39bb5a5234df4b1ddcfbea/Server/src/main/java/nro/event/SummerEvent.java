package nro.event;

import nro.consts.*;
import nro.dialog.ConfirmDialog;
import nro.lib.RandomCollection;
import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.event.Beetle;
import nro.models.boss.event.NightLord;
import nro.models.item.Item;
import nro.models.item.ItemOption;
import nro.models.map.ItemMap;
import nro.models.map.Map;
import nro.models.map.Zone;
import nro.models.mob.Mob;
import nro.models.npc.Npc;
import nro.models.player.Inventory;
import nro.models.player.Pet;
import nro.models.player.Player;
import nro.models.skill.Skill;
import nro.services.*;
import nro.utils.Log;
import nro.utils.Util;

import java.util.List;

/**
 * @author outcast c-cute hột me 😳
 */
public class SummerEvent extends Event {

    private int uniqueID;

    @Override
    public void init() {
        uniqueID = -99999;
        initNpc();
    }

    private long generateUniqueID() {
        return uniqueID++;
    }

    @Override
    public void initNpc() {
        Map map = MapService.gI().getMapById(ConstMap.LANG_ARU);
        if (map != null) {
            Npc npc = new Npc(map.mapId, 1, 731, 432, ConstNpc.EVENT, 10812) {
                @Override
                public void openBaseMenu(Player player) {
                    if (canOpenNpc(player)) {
                        this.createOtherMenu(player, ConstNpc.BASE_MENU, "Xin chào, sự kiện hè 2023 đang được diễn ra\n PHÁ TAN CƠN NÓNG MÙA HÈ, Chúc các cư dân vui vẻ.", "Đổi Quà\nSự Kiện", "Tắm\nNước Nóng", "Bắt\nSâu Bọ", "Từ chối");
                    }
                }

                @Override
                public void confirmMenu(Player player, int select) {
                    int menuID = player.iDMark.getIndexMenu();
                    if (player.iDMark.isBaseMenu()) {
                        switch (select) {
                            case 0:
                                this.createOtherMenu(player, ConstNpc.ORTHER_MENU, "Xin chào, sự kiện hè 2023 đang được diễn ra", "Đổi\n Vỏ Ốc", "Đổi Sò", "Đổi Cua", "Đổi \nSao Biển", "Đổi Quà\n Đặc Biệt", "Từ Chối");
                                break;
                            case 1:
                                this.createOtherMenu(player, ConstNpc.ORTHER_MENU1, "Xin chào, sự kiện hè 2023 đang được diễn ra", "Chế Tạo Bồn\n Tắm Gỗ", "Chế Tạo Bồn\n Tắm Vàng", "Từ chối");
                                break;
                            case 2:
                                this.createOtherMenu(player, ConstNpc.ORTHER_MENU2, "Xin chào, sự kiện hè 2023 đang được diễn ra", "Tặng Bọ\nCánh Cứng", "Tặng\nNgài Đêm", "Từ chối");
                                break;
                        }
                    } else if (menuID == ConstNpc.ORTHER_MENU) {
                        int itemExchange = -1;
                        if (select < 3) {
                            switch (select) {
                                case 0:
                                    itemExchange = ConstItem.VO_OC;
                                    break;
                                case 1:
                                    itemExchange = ConstItem.VO_SO;
                                    break;
                                case 2:
                                    itemExchange = ConstItem.CON_CUA;
                                    break;
                                case 3:
                                    itemExchange = ConstItem.SAO_BIEN;
                                    break;
                            }
                            Item item = InventoryService.gI().findItem(player, itemExchange, 99);
                            if (item == null) {
                                Service.getInstance().sendThongBao(player, "Không đủ vật phẩm");
                                return;
                            }
                            InventoryService.gI().subQuantityItemsBag(player, item, 99);
                        } else {
                            Item voOc = InventoryService.gI().findItem(player, ConstItem.VO_OC, 99);
                            Item voSo = InventoryService.gI().findItem(player, ConstItem.VO_SO, 99);
                            Item conCua = InventoryService.gI().findItem(player, ConstItem.CON_CUA, 99);
                            Item saoBien = InventoryService.gI().findItem(player, ConstItem.SAO_BIEN, 99);
                            if (voOc == null || voSo == null || conCua == null || saoBien == null) {
                                Service.getInstance().sendThongBao(player, "Không đủ vật phẩm");
                                return;
                            }
                            InventoryService.gI().subQuantityItemsBag(player, voOc, 99);
                            InventoryService.gI().subQuantityItemsBag(player, voSo, 99);
                            InventoryService.gI().subQuantityItemsBag(player, conCua, 99);
                            InventoryService.gI().subQuantityItemsBag(player, saoBien, 99);
                        }
                        RandomCollection<Integer> rd = new RandomCollection<>();
                        if (select == 0) {// đổi vỏ ốc
                            rd.add(1, ConstItem.BO_HOA_HONG);
                            rd.add(1, ConstItem.BO_HOA_VANG);
                        } else if (select == 1) {// đổi vỏ sò
                            rd.add(1, ConstItem.PET_BO_CANH_CUNG);
                            rd.add(1, ConstItem.PET_NGAI_DEM);
                        } else if (select == 2) { // đổi con cua
                            rd.add(1, 1144); // Phượng hoàng lửa
                            rd.add(1, 897); // rùa bay
                        } else if (select == 3) { // đổi sao biển
                            rd.add(1, ConstItem.MANH_AO);
                            rd.add(1, ConstItem.MANH_QUAN);
                            rd.add(1, ConstItem.MANH_GIAY);
                        } else {// đổi cả 4
                            rd.add(1, ConstItem.CAI_TRANG_AO_VIT_CAM);
                            rd.add(1, ConstItem.CAI_TRANG_AO_TRANG_HOA);
                            rd.add(1, ConstItem.CAI_TRANG_NON_ROM_MUA_HE);
                        }

                        int rwID = rd.next();
                        Item rw = ItemService.gI().createNewItem((short) rwID);
                        if (rw.template.type == 11) {// đồ đeo lưng
                            //option
                            rw.itemOptions.add(new ItemOption(50, Util.nextInt(5, 15)));
                            rw.itemOptions.add(new ItemOption(77, Util.nextInt(5, 15)));
                            rw.itemOptions.add(new ItemOption(103, Util.nextInt(5, 15)));

                            if (rw.template.id != ConstItem.MANH_AO || rw.template.id != ConstItem.MANH_QUAN || rw.template.id != ConstItem.MANH_GIAY) {
                                rw.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                            }

                            if (Util.isTrue(1, 30)) {
                                rw.itemOptions.add(new ItemOption(174, 2023));
                            } else {
                                rw.itemOptions.add(new ItemOption(174, 2023));
                                rw.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                            }

                        } else if (rw.template.type == 5) {// cải trang
                            rw.itemOptions.add(new ItemOption(50, Util.nextInt(20, 40)));
                            rw.itemOptions.add(new ItemOption(77, Util.nextInt(20, 40)));
                            rw.itemOptions.add(new ItemOption(103, Util.nextInt(20, 40)));

                            if (Util.isTrue(1, 30)) {
                                rw.itemOptions.add(new ItemOption(174, 2023));
                            } else {
                                rw.itemOptions.add(new ItemOption(174, 2023));
                                rw.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                            }

                        }

                        InventoryService.gI().addItemBag(player, rw, 1);
                        InventoryService.gI().sendItemBags(player);
                    } else if (menuID == ConstNpc.ORTHER_MENU1) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("|2|Chế tạo Bồn Tắm ").append(select == 1 ? "gỗ" : "vàng\n").append("|1|Cành khô: ").append(InventoryService.gI().getQuantity(player, ConstItem.CANH_KHO)).append("/50\n").append("Nước suối tinh khiết: ").append(InventoryService.gI().getQuantity(player, ConstItem.NUOC_SUOI_TINH_KHIET)).append("/20\n").append("Gỗ lớn: ").append(InventoryService.gI().getQuantity(player, ConstItem.GO_LON)).append("/20\n").append("Que đốt: ").append(InventoryService.gI().getQuantity(player, ConstItem.QUE_DOT)).append("/2\n");

                        Item canhKho = InventoryService.gI().findItem(player, ConstItem.CANH_KHO, 50);
                        Item nuocSuoi = InventoryService.gI().findItem(player, ConstItem.NUOC_SUOI_TINH_KHIET, 20);
                        Item goLon = InventoryService.gI().findItem(player, ConstItem.GO_LON, 20);
                        Item queDot = InventoryService.gI().findItem(player, ConstItem.QUE_DOT, 2);
                        Inventory inv = player.inventory;
                        if (select == 0) {
                            sb.append("Giá vàng: 150.000.000\n");
                        }
                        sb.append("Giá vàng: 300.000.000\n").append("Giá hồng ngọc: 15");
                        ConfirmDialog confDialog = new ConfirmDialog(sb.toString(), () -> {
                            if (canhKho != null && nuocSuoi != null && goLon != null && queDot != null) {
                                int cost = 150000000;
                                if (select == 1) {
                                    if (inv.ruby < 15) {
                                        Service.getInstance().sendThongBao(player, "Bạn không đủ tiền");
                                        return;
                                    }
                                    inv.subRuby(15);
                                    cost = 300000000;
                                }
                                if (inv.gold < cost) {
                                    Service.getInstance().sendThongBao(player, "Bạn không đủ tiền");
                                    return;
                                }
                                inv.subGold(cost);
                                InventoryService.gI().subQuantityItemsBag(player, canhKho, 50);
                                InventoryService.gI().subQuantityItemsBag(player, nuocSuoi, 20);
                                InventoryService.gI().subQuantityItemsBag(player, goLon, 20);
                                InventoryService.gI().subQuantityItemsBag(player, queDot, 2);

                                int rwID = (select == 0 ? ConstItem.BON_TAM_GO : ConstItem.BON_TAM_VANG);
                                Item rw = ItemService.gI().createNewItem((short) rwID);
                                InventoryService.gI().addItemBag(player, rw, 99);
                                InventoryService.gI().sendItemBags(player);
                                Service.getInstance().sendThongBao(player, "Bạn Nhận được " + rw.template.name);
                            } else {
                                Service.getInstance().sendThongBao(player, "Không đủ vật phẩm");
                            }
                        });
                        confDialog.show(player);
                    } else if (menuID == ConstNpc.ORTHER_MENU2) {
                        switch (select) {
                            case 0: {
                                Item beetle = InventoryService.gI().findItem(player, ConstItem.BO_CANH_CUNG, 1);
                                if (beetle == null) {
                                    Service.getInstance().sendThongBao(player, "Không đủ vật phẩm");
                                    return;
                                }
                                InventoryService.gI().subQuantityItemsBag(player, beetle, 1);
                                RandomCollection<Integer> rd = new RandomCollection();

                                rd.add(1, 1252); // Ve Sầu Xên
                                rd.add(1, 1253); // Ve Sầu Xên Tiến Hóa
                                rd.add(1, ConstItem.CAY_KEM);
                                rd.add(1, ConstItem.CA_HEO);
                                rd.add(1, ConstItem.DIEU_RONG);
                                rd.add(1, ConstItem.CON_DIEU);

                                int rwID = rd.next();
                                Item rw = ItemService.gI().createNewItem((short) rwID);

                                if (rw.template.type == 11) {// đồ đeo lưng
                                    //option
                                    rw.itemOptions.add(new ItemOption(50, Util.nextInt(5, 15)));
                                    rw.itemOptions.add(new ItemOption(77, Util.nextInt(5, 15)));
                                    rw.itemOptions.add(new ItemOption(103, Util.nextInt(5, 15)));
                                } else if (rw.template.type == 23) {// thú bay

                                    rw.itemOptions.add(new ItemOption(50, Util.nextInt(5, 10)));
                                    rw.itemOptions.add(new ItemOption(77, Util.nextInt(5, 10)));
                                    rw.itemOptions.add(new ItemOption(103, Util.nextInt(5, 10)));

                                } else {// cải trang
                                    //option
                                    rw.itemOptions.add(new ItemOption(50, Util.nextInt(20, 40)));
                                    rw.itemOptions.add(new ItemOption(77, Util.nextInt(20, 40)));
                                    rw.itemOptions.add(new ItemOption(103, Util.nextInt(20, 40)));
                                    rw.itemOptions.add(new ItemOption(199, 0));
                                }
                                // hsd - vĩnh viễn
                                if (Util.isTrue(1, 30)) {
                                    rw.itemOptions.add(new ItemOption(174, 2023));
                                } else {
                                    rw.itemOptions.add(new ItemOption(174, 2023));
                                    rw.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                }

                                InventoryService.gI().addItemBag(player, rw, 1);
                                InventoryService.gI().sendItemBags(player);
                                Service.getInstance().sendThongBao(player, "Bạn nhận được " + rw.template.name);
                            }
                            break;
                            case 1:
                                Item nightLord = InventoryService.gI().findItem(player, ConstItem.NGAI_DEM, 1);
                                if (nightLord == null) {
                                    Service.getInstance().sendThongBao(player, "Không đủ vật phẩm");
                                    return;
                                }
                                InventoryService.gI().subQuantityItemsBag(player, nightLord, 1);
                                RandomCollection<Integer> rd = new RandomCollection();

                                rd.add(1, 1252); // Ve Sầu Xên
                                rd.add(1, 1253); // Ve Sầu Xên Tiến Hóa
                                rd.add(1, ConstItem.CAY_KEM);
                                rd.add(1, ConstItem.CA_HEO);
                                rd.add(1, ConstItem.DIEU_RONG);
                                rd.add(1, ConstItem.CON_DIEU);
                                // quà

                                int rwID = rd.next();
                                Item rw = ItemService.gI().createNewItem((short) rwID);

                                if (rw.template.type == 11) {// đồ đeo lưng
                                    //option
                                    rw.itemOptions.add(new ItemOption(50, Util.nextInt(5, 15)));
                                    rw.itemOptions.add(new ItemOption(77, Util.nextInt(5, 15)));
                                    rw.itemOptions.add(new ItemOption(103, Util.nextInt(5, 15)));
                                } else if (rw.template.type == 23) {// thú bay
                                    rw.itemOptions.add(new ItemOption(50, Util.nextInt(5, 10)));
                                    rw.itemOptions.add(new ItemOption(77, Util.nextInt(5, 10)));
                                    rw.itemOptions.add(new ItemOption(103, Util.nextInt(5, 10)));
                                } else {// khác
                                    //option+
                                    rw.itemOptions.add(new ItemOption(188, 0));
                                }
                                // hsd - vĩnh viễn
                                if (Util.isTrue(1, 30)) {
                                    rw.itemOptions.add(new ItemOption(174, 2023));
                                } else {
                                    rw.itemOptions.add(new ItemOption(174, 2023));
                                    rw.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
                                }

                                InventoryService.gI().addItemBag(player, rw, 1);
                                InventoryService.gI().sendItemBags(player);
                                Service.getInstance().sendThongBao(player, "Bạn nhận được " + rw.template.name);
                                break;

                        }
                    }
                }
            };
            map.addNpc(npc);
        }
    }

    @Override
    public void initMap() {

    }

    @Override
    public void dropItem(Player player, Mob mob, List<ItemMap> list, int x, int yEnd) {
        Zone z = mob.zone;
        int mapID = z.map.mapId;
        int itemID = -1;
        byte[] rwLimit = player.getRewardLimit();
        if (player instanceof Pet pet) {
            rwLimit = pet.master.getRewardLimit();
        }
        if (Util.isTrue(50, 50)) {

            if (mob.tempId == ConstMob.MOC_NHAN && rwLimit[ConstRewardLimit.CANH_KHO] < 100) {
                rwLimit[ConstRewardLimit.CANH_KHO]++;
                itemID = ConstItem.CANH_KHO;
            }
        }
        if (itemID != -1) {
            ItemMap itemMap = new ItemMap(z, itemID, 1, x, yEnd, player.id);
            list.add(itemMap);
        }
        if (Util.isTrue(1, 50) && player.event.isUseQuanHoa()) {
            RandomCollection<Integer> rd = new RandomCollection<>();
            rd.add(1, ConstItem.VO_OC);
            rd.add(1, ConstItem.VO_SO);
            rd.add(1, ConstItem.CON_CUA);
            rd.add(1, ConstItem.SAO_BIEN);
            itemID = rd.next();
            list.add(new ItemMap(z, itemID, 1, x, yEnd, player.id));
        }
    }

    @Override
    public boolean useItem(Player player, Item item) {
        int itemID = item.template.id;
        switch (itemID) {
            case ConstItem.BON_TAM_GO:
            case ConstItem.BON_TAM_VANG:
                useBonTam(player, item);
                break;
            case ConstItem.HU_MAT_ONG:
            case ConstItem.VOT_BAT_BO:
                insectTrapping(player, item);
                break;
            default:
                return false;
        }
        return true;
    }

    private void useBonTam(Player player, Item item) {
        if (!player.zone.map.isMapLang()) {
            Service.getInstance().sendThongBaoOK(player, "Chỉ có thể sự dụng ở các làng");
            return;
        }
        if (player.event.isUseBonTam()) {
            Service.getInstance().sendThongBaoOK(player, "Không thể sử dụng khi đang tắm");
            return;
        }
        int itemID = item.template.id;
        RandomCollection<Integer> rd = new RandomCollection<>();
        rd.add(1, ConstItem.QUAT_BA_TIEU);
        rd.add(1, ConstItem.CAY_KEM);
        rd.add(1, ConstItem.CA_HEO);
        rd.add(1, ConstItem.DIEU_RONG);

        if (itemID == ConstItem.BON_TAM_GO) {
            rd.add(1, ConstItem.CON_DIEU);
        } else {//bồn tắm vàng
            rd.add(1, ConstItem.XIEN_CA);
            rd.add(1, ConstItem.PHONG_LON);
            rd.add(1, ConstItem.CAI_TRANG_POC_BIKINI_2023);
            rd.add(1, ConstItem.CAI_TRANG_PIC_THO_LAN_2023);
            rd.add(1, ConstItem.CAI_TRANG_KING_KONG_SANH_DIEU_2023);
        }

        int rwID = rd.next();
        Item rw = ItemService.gI().createNewItem((short) rwID);
        if (rw.template.type == 11) {// đồ đeo lưng
            //option
            rw.itemOptions.add(new ItemOption(50, Util.nextInt(5, 15)));
            rw.itemOptions.add(new ItemOption(77, Util.nextInt(5, 15)));
            rw.itemOptions.add(new ItemOption(103, Util.nextInt(5, 15)));
            rw.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
        } else {// cải trang
            //option
            rw.itemOptions.add(new ItemOption(50, Util.nextInt(20, 40)));
            rw.itemOptions.add(new ItemOption(77, Util.nextInt(20, 40)));
            rw.itemOptions.add(new ItemOption(103, Util.nextInt(20, 40)));
            rw.itemOptions.add(new ItemOption(199, 0));
            rw.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
        }

        if (rwID == ConstItem.QUAT_BA_TIEU || rwID == ConstItem.VO_OC || rwID == ConstItem.CAY_KEM || rwID == ConstItem.CA_HEO) {
            //hsd
            rw.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));

        } else if (rwID == ConstItem.XIEN_CA || rwID == ConstItem.PHONG_LON || rwID == ConstItem.CAI_TRANG_POC_BIKINI_2023
                || rwID == ConstItem.CAI_TRANG_PIC_THO_LAN_2023 || rwID == ConstItem.CAI_TRANG_KING_KONG_SANH_DIEU_2023) {
            // hsd - vinh vien
            if (Util.isTrue(1, 30)) {
                rw.itemOptions.add(new ItemOption(174, 2023));
            } else {
                rw.itemOptions.add(new ItemOption(174, 2023));
                rw.itemOptions.add(new ItemOption(93, Util.nextInt(1, 30)));
            }

        }

        int delay = itemID == ConstItem.BON_TAM_GO ? 3 : 1;
        ItemTimeService.gI().sendItemTime(player, 3779, 60 * delay);
        EffectSkillService.gI().startStun(player, System.currentTimeMillis(), 60000 * delay);
        InventoryService.gI().subQuantityItemsBag(player, item, 1);
        InventoryService.gI().sendItemBags(player);
        player.event.setUseBonTam(true);
        Util.setTimeout(() -> {
            InventoryService.gI().addItemBag(player, rw, 99);
            InventoryService.gI().sendItemBags(player);
            player.event.setUseBonTam(false);
        }, 60000 * delay,"event");
    }

    private void insectTrapping(Player player, Item item) {
        int itemID = item.getId();
        boolean flag = false;
        int mapID = player.zone.map.mapId;
        if (mapID == ConstMap.DOI_NAM_TIM || mapID == ConstMap.THUNG_LUNG_NAMEC || mapID == ConstMap.RUNG_THONG_XAYDA || mapID == ConstMap.RUNG_BAMBOO || mapID == ConstMap.RUNG_DUONG_XI) {
            flag = true;
        }
        StringBuilder sb = new StringBuilder();
        int nBoKienVuong = InventoryService.gI().getQuantity(player, ConstItem.BO_KIEN_VUONG_HAI_SUNG);
        int nBoHung = InventoryService.gI().getQuantity(player, ConstItem.BO_HUNG_TE_GIAC);
        int nBoKepKim = InventoryService.gI().getQuantity(player, ConstItem.BO_KEP_KIM);

        sb.append("|1|Làm mồi nhử ").append(itemID == ConstItem.HU_MAT_ONG ? "Hũ mật ong \n" : "Vợt bắt bọ \n")
                .append("|").append(nBoKienVuong < 10 ? ConstTextColor.BLUE : ConstTextColor.RED).append("|").append("Bọ Kiến Vương: ").append(nBoKienVuong).append("/10\n")
                .append("|").append(nBoHung < 10 ? ConstTextColor.BLUE : ConstTextColor.RED).append("|").append("Bọ Hung Tê Giác: ").append(nBoHung).append("/10\n")
                .append("|").append(nBoKepKim < 10 ? ConstTextColor.BLUE : ConstTextColor.RED).append("|").append("Bọ Kẹp Kìm: ").append(nBoKepKim).append("/10\n")
                .append("|").append(flag ? ConstTextColor.BLUE : ConstTextColor.RED).append("|").append("Đồi nấm tím, Thung lũng Namec,Rừng nguyên sinh, Rừng thông Xayda ,Rừng Bamboo, Rừng dương xỉ");
        if (!flag) {
            Service.getInstance().sendBigMessage(player, 0, sb.toString());
            // Service.getInstance().sendThongBaoOK(player, sb.toString());
            return;
        }
        ConfirmDialog confirmDialog = new ConfirmDialog(sb.toString(), () -> {
            Item boKienVuong = InventoryService.gI().findItem(player, ConstItem.BO_KIEN_VUONG_HAI_SUNG, 10);
            Item boHung = InventoryService.gI().findItem(player, ConstItem.BO_HUNG_TE_GIAC, 10);
            Item boKepKim = InventoryService.gI().findItem(player, ConstItem.BO_KEP_KIM, 10);
            if (boKienVuong != null && boHung != null && boKepKim != null) {
                if (itemID == ConstItem.HU_MAT_ONG) {
                    BossData beetle = BossData.builder()
                            .name("Bọ cánh cứng")
                            .gender(ConstPlayer.TRAI_DAT)
                            .typeDame(Boss.DAME_NORMAL)
                            .typeHp(Boss.HP_NORMAL)
                            .dame(1)
                            .hp(new int[][]{{1500}})
                            .outfit(new short[]{1245, 1246, 1247})
                            .skillTemp(new int[][]{{Skill.DRAGON, 1, 100}})
                            .secondsRest(BossData._0_GIAY)
                            .build();
                    new Beetle((byte) generateUniqueID(), beetle, player);
                } else {
                    BossData nl = BossData.builder()
                            .name("Ngài đêm")
                            .gender(ConstPlayer.TRAI_DAT)
                            .typeDame(Boss.DAME_NORMAL)
                            .typeHp(Boss.HP_NORMAL)
                            .dame(1)
                            .hp(new int[][]{{1500}})
                            .outfit(new short[]{1248, 1249, 1250})
                            .skillTemp(new int[][]{{Skill.DRAGON, 1, 100}})
                            .secondsRest(BossData._0_GIAY)
                            .build();
                    new NightLord((byte) generateUniqueID(), nl, player);
                }
                InventoryService.gI().subQuantityItemsBag(player, item, 1);
                InventoryService.gI().subQuantityItemsBag(player, boKienVuong, 10);
                InventoryService.gI().subQuantityItemsBag(player, boHung, 10);
                InventoryService.gI().subQuantityItemsBag(player, boKepKim, 10);
                InventoryService.gI().sendItemBags(player);
            }
        });
        confirmDialog.show(player);
    }

}
