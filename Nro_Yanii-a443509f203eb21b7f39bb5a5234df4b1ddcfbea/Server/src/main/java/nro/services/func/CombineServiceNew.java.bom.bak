package nro.services.func;

import nro.consts.ConstItem;
import nro.consts.ConstNpc;
import nro.lib.RandomCollection;
import nro.models.item.Item;
import nro.models.item.ItemOption;
import nro.models.npc.Npc;
import nro.models.npc.NpcManager;
import nro.models.player.Player;
import nro.server.ServerLog;
import nro.server.ServerNotify;
import nro.server.io.Message;
import nro.services.InventoryService;
import nro.services.ItemService;
import nro.services.RewardService;
import nro.services.Service;
import nro.utils.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import nro.models.player.Inventory;

/**
 * @author 💖 Nro Yanii 💖
 *
 */
public class CombineServiceNew {

    public static final int DOI_HOP_QUA_HALLOWEEN = 5712;

    private static final int COST_DOI_VE_DOI_DO_HUY_DIET = 500000000;
    private static final long COST_DAP_DO_KICH_HOAT = 500000000;
    private static final long COST_NANG_CAP_DO_TL = 500000000;
    private static final int COST_DOI_MANH_KICH_HOAT = 500000000;
    private static final int COST_GIA_HAN_CAI_TRANG = 500000000;
    private static final int COST = 500000000;

    private static final int TIME_COMBINE = 500;

    private static final byte MAX_STAR_ITEM = 8;
    private static final byte MAX_LEVEL_ITEM = 7;

    private static final byte OPEN_TAB_COMBINE = 0;
    private static final byte REOPEN_TAB_COMBINE = 1;
    private static final byte COMBINE_SUCCESS = 2;
    private static final byte COMBINE_FAIL = 3;
    private static final byte COMBINE_CHANGE_OPTION = 4;
    private static final byte COMBINE_DRAGON_BALL = 5;
    public static final byte OPEN_ITEM = 6;

    public static final int EP_SAO_TRANG_BI = 500;
    public static final int PHA_LE_HOA_TRANG_BI = 501;
    public static final int CHUYEN_HOA_TRANG_BI = 502;
    public static final int DOI_VE_HUY_DIET = 503;
    public static final int DAP_SET_KICH_HOAT = 504;

    public static final int DOI_MANH_KICH_HOAT = 505;
    public static final int NANG_CAP_SKH = 5287;
    public static final int NANG_CAP_DO_TL = 712;
    public static final int NANG_CAP_VAT_PHAM = 506;

    public static final int NANG_CAP_BONG_TAI = 507;
    public static final int MO_CHI_SO_BONG_TAI = 519;

    public static final int LAM_PHEP_NHAP_DA = 508;
    public static final int NHAP_NGOC_RONG = 509;
    public static final int CHE_TAO_DO_THIEN_SU = 510;
    public static final int DAP_SET_KICH_HOAT_CAO_CAP = 511;
    public static final int GIA_HAN_CAI_TRANG = 512;
    public static final int NANG_CAP_DO_THIEN_SU = 513;
    public static final int PHA_LE_HOA_TRANG_BI_X10 = 514;

    // START _ SÁCH TUYỆT KỸ //
    public static final int GIAM_DINH_SACH = 515;
    public static final int TAY_SACH = 516;
    public static final int NANG_CAP_SACH_TUYET_KY = 517;
    public static final int PHUC_HOI_SACH = 518;
    public static final int PHAN_RA_SACH = 520;
    // END _ SÁCH TUYỆT KỸ //s

    // START _ PHA LÊ HÓA NEW //
    public static final int NANG_CAP_SAO_PHA_LE = 521;
    public static final int DANH_BONG_SAO_PHA_LE = 522;
    public static final int CUONG_HOA_LO_SAO_PHA_LE = 523;
    public static final int TAO_DA_HEMATILE = 524;
    // END _ PHA LÊ HÓA NEW //

    // START _ CHUYEN HOA TRANG BI //
    public static final int CHUYEN_HOA_BANG_VANG = 525;
    public static final int CHUYEN_HOA_BANG_NGOC = 526;

    // END _ CHUYEN HOA TRANG BI //
    private static final int GOLD_MOCS_BONG_TAI = 500_000_000;
    private static final int Gem_MOCS_BONG_TAI = 250;

    private static final int GOLD_BONG_TAI2 = 5_000_000;
    private static final int GEM_BONG_TAI2 = 20;

    private static final int GOLD_BONG_TAI = 500_000_000;
    private static final int GEM_BONG_TAI = 5_000;
    private static final int RATIO_BONG_TAI = 15;
    private static final int RATIO_NANG_CAP = 22;

    private final Npc baHatMit;

    private final Npc obito;

    private final Npc whis;

    private static CombineServiceNew i;

    public CombineServiceNew() {
        this.baHatMit = NpcManager.getNpc(ConstNpc.BA_HAT_MIT);

        this.whis = NpcManager.getNpc(ConstNpc.WHIS);

        this.obito = NpcManager.getNpc(ConstNpc.OBITO);

    }

    public static CombineServiceNew gI() {
        if (i == null) {
            i = new CombineServiceNew();
        }
        return i;
    }

    /**
     * Mở tab đập đồ
     *
     * @param player
     * @param type   kiểu đập đồ
     */
    public void openTabCombine(Player player, int type) {
        player.combineNew.setTypeCombine(type);
        Message msg;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(OPEN_TAB_COMBINE);
            msg.writer().writeUTF(getTextInfoTabCombine(type));
            msg.writer().writeUTF(getTextTopTabCombine(type));
            if (player.iDMark.getNpcChose() != null) {
                msg.writer().writeShort(player.iDMark.getNpcChose().tempId);
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    // public int calculateChiSO1(Item trangbiChuyenHoa, int levelTrangBi, int
    // soLanRotCap) {
    //
    // int chiSO1_trangBiCanChuyenHoa = 0;
    //
    // int chiSO_DaTangCap = trangbiChuyenHoa.itemOptions.get(0).param +
    // (trangbiChuyenHoa.itemOptions.get(0).param * (levelTrangBi * 10 / 100));
    //
    // if (soLanRotCap != 0) {
    // chiSO1_trangBiCanChuyenHoa = chiSO_DaTangCap - chiSO_DaTangCap * (
    // soLanRotCap * 10 / 100);
    // } else {
    // chiSO1_trangBiCanChuyenHoa = chiSO_DaTangCap;
    // }
    //
    // System.out.println(chiSO1_trangBiCanChuyenHoa);
    //
    // return chiSO1_trangBiCanChuyenHoa;
    //
    // }
    private float getRationangbt(int lvbt) { // tile dap do chi hat mit
        return 50f;
    }

    /**
     * Hiển thị thông tin đập đồ
     *
     * @param player
     */
    public void showInfoCombine(Player player, int[] index) {
        player.combineNew.clearItemCombine();
        if (index.length > 0) {
            for (int i = 0; i < index.length; i++) {
                player.combineNew.itemsCombine.add(player.inventory.itemsBag.get(index[i]));
            }
        }
        switch (player.combineNew.typeCombine) {
            case EP_SAO_TRANG_BI:
                if (player.combineNew.itemsCombine.size() == 2) {
                    Item trangBi = null;
                    Item daPhaLe = null;
                    for (Item item : player.combineNew.itemsCombine) {
                        if (isTrangBiPhaLeHoa(item)) {
                            trangBi = item;
                        } else if (isDaPhaLe(item)) {
                            daPhaLe = item;
                        }
                    }
                    int star = 0; // sao pha lê đã ép
                    int starEmpty = 0; // lỗ sao pha lê
                    if (trangBi != null && daPhaLe != null) {
                        for (ItemOption io : trangBi.itemOptions) {
                            if (io.optionTemplate.id == 102) {
                                star = io.param;
                            } else if (io.optionTemplate.id == 107) {
                                starEmpty = io.param;
                            }
                        }
                        if (star < starEmpty) {
                            player.combineNew.gemCombine = getGemEpSao(star);
                            String npcSay = trangBi.template.name + "\n|2|";
                            for (ItemOption io : trangBi.itemOptions) {
                                if (io.optionTemplate.id != 102) {
                                    npcSay += io.getOptionString() + "\n";
                                }
                            }
                            if (daPhaLe.template.type == 30) {
                                for (ItemOption io : daPhaLe.itemOptions) {
                                    npcSay += "|7|" + io.getOptionString() + "\n";
                                }
                            } else {
                                npcSay += "|7|" + ItemService.gI().getItemOptionTemplate(getOptionDaPhaLe(daPhaLe)).name
                                        .replaceAll("#", getParamDaPhaLe(daPhaLe) + "") + "\n";
                            }
                            npcSay += "|1|Cần " + Util.numberToMoney(player.combineNew.gemCombine) + " ngọc";
                            baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                    "Nâng cấp\ncần " + player.combineNew.gemCombine + " ngọc");

                        } else {
                            this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                    "Cần 1 trang bị có lỗ sao pha lê và 1 loại đá pha lê để ép vào", "Đóng");
                        }
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Cần 1 trang bị có lỗ sao pha lê và 1 loại đá pha lê để ép vào", "Đóng");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Cần 1 trang bị có lỗ sao pha lê và 1 loại đá pha lê để ép vào", "Đóng");
                }
                break;
            case PHA_LE_HOA_TRANG_BI:
                if (player.combineNew.itemsCombine.size() == 1) {
                    Item item = player.combineNew.itemsCombine.get(0);
                    if (isTrangBiPhaLeHoa(item)) {
                        int star = 0;
                        for (ItemOption io : item.itemOptions) {
                            if (io.optionTemplate.id == 107) {
                                star = io.param;
                                break;
                            }
                        }
                        if (star < MAX_STAR_ITEM) {
                            player.combineNew.goldCombine = getGoldPhaLeHoa(star);
                            player.combineNew.gemCombine = getGemPhaLeHoa(star);
                            player.combineNew.ratioCombine = getRatioPhaLeHoa(star);

                            String npcSay = "|2|" + item.template.name + "\n";
                            for (ItemOption io : item.itemOptions) {
                                if (io.optionTemplate.id != 102 && io.optionTemplate.id != 107) {
                                    npcSay += "|0|" + io.getOptionString() + "\n";
                                }
                                if (io.optionTemplate.id == 107) {
                                    npcSay += "|1|" + io.getOptionString() + "\n";
                                }
                            }
                            npcSay += "|2|Tỉ lệ thành công: " + (player.combineNew.ratioCombine + 1) + "%" + "\n";
                            npcSay += "|2|Cần " + Util.numberToMoney(player.combineNew.goldCombine) + " vàng";

                            baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                    "Nâng cấp\n1 ngọc\nx100 lần", "Nâng cấp\n1 ngọc\nx10 lần", "Nâng cấp\n1 ngọc",
                                    "Từ chối");

                        } else {
                            this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                    "Vật phẩm đã đạt tối đa sao pha lê", "Đóng");
                        }
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Vật phẩm này không thể đục lỗ",
                                "Đóng");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hãy hãy chọn 1 vật phẩm để pha lê hóa",
                            "Đóng");
                }
                break;
            case CHE_TAO_DO_THIEN_SU:
                if (player.combineNew.itemsCombine.size() == 0) {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Hãy đưa ta 2 món Hủy Diệt bất kì và 1 món Thần Linh cùng loại", "Đóng");
                    return;
                }
                if (player.combineNew.itemsCombine.size() == 3) {
                    if (player.combineNew.itemsCombine.stream()
                            .filter(item -> item.isNotNullItem() && item.isCongThuc() && item.quantity >= 99)
                            .count() < 1) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Thiếu mảnh Công thức hoặc số lượng không đủ", "Đóng");
                        return;
                    }
                    if (player.combineNew.itemsCombine.stream()
                            .filter(item -> item.isNotNullItem() && item.template.id == 1311 && item.quantity >= 99)
                            .count() < 1) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Thiếu Đá Thiên Sứ hoặc số lượng không đủ", "Đóng");
                        return;
                    }
                    if (player.combineNew.itemsCombine.stream()
                            .filter(item -> item.isNotNullItem() && item.isManhTS() && item.quantity >= 999)
                            .count() < 1) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu mảnh thiên sứ", "Đóng");
                        return;
                    }

                    String npcSay = "|2|Con Có chắc chắn Nâng cấp Không ?\n"
                            + "|1|Cần " + Util.numberToMoney(COST) + " vàng";

                    if (player.inventory.gold < COST) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hết tiền rồi\nẢo ít thôi con",
                                "Đóng");
                        return;
                    }
                    this.baHatMit.createOtherMenu(player, ConstNpc.MENU_NANG_CAP_DO_TS,
                            npcSay, "Nâng cấp\n" + Util.numberToMoney(COST) + " vàng", "Từ chối");
                } else {
                    if (player.combineNew.itemsCombine.size() > 3) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Cất đi con ta không thèm", "Đóng");
                        return;
                    }
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Còn thiếu nguyên liệu để nâng cấp hãy quay lại sau", "Đóng");
                }
                break;

            // START _ SÁCH TUYỆT KỸ //
            case GIAM_DINH_SACH:
                if (player.combineNew.itemsCombine.size() == 2) {
                    Item sachTuyetKy = null;
                    Item buaGiamDinh = null;
                    for (Item item : player.combineNew.itemsCombine) {
                        if (issachTuyetKy(item)) {
                            sachTuyetKy = item;
                        } else if (item.template.id == 1283) {
                            buaGiamDinh = item;
                        }
                    }
                    if (sachTuyetKy != null && buaGiamDinh != null) {

                        String npcSay = "|1|" + sachTuyetKy.getName() + "\n";
                        npcSay += "|2|" + buaGiamDinh.getName() + " " + buaGiamDinh.quantity + "/1";
                        baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                "Giám định", "Từ chối");
                    } else {
                        Service.getInstance().sendThongBaoOK(player, "Cần Sách Tuyệt Kỹ và bùa giám định");
                        return;
                    }
                } else {
                    Service.getInstance().sendThongBaoOK(player, "Cần Sách Tuyệt Kỹ và bùa giám định");
                    return;
                }
                break;

            case CHUYEN_HOA_BANG_VANG:
                if (player.combineNew.itemsCombine.size() == 2) {
                    Item trangBiGoc = player.combineNew.itemsCombine.get(0);
                    Item trangBiCanChuyenHoa = player.combineNew.itemsCombine.get(1);

                    int goldChuyenHoa = 2_000_000_000;

                    int levelTrangBi = 0;
                    int soLanRotCap = 0;
                    int chiSO1_trangBiCanChuyenHoa = 0;

                    for (ItemOption io : trangBiGoc.itemOptions) {
                        if (io.optionTemplate.id == 72) {
                            levelTrangBi = io.param;
                        } else if (io.optionTemplate.id == 232) {
                            soLanRotCap += io.param;
                        }
                    }

                    // START Tính chỉ số nhân với số cấp và trừ với số lần rớt cấp //
                    int chisogoc = trangBiCanChuyenHoa.itemOptions.get(0).param;

                    chisogoc += chisogoc * (levelTrangBi * 0.1);

                    chisogoc -= chisogoc * (soLanRotCap * 0.1);
                    // END Tính chỉ số nhân với số cấp và trừ với số lần rớt cấp //

                    boolean trangBi_daNangCap_daPhaLeHoa = false;

                    for (int so = 0; so < trangBiCanChuyenHoa.itemOptions.size(); so++) {
                        if (trangBiCanChuyenHoa.itemOptions.get(so).optionTemplate.id == 72
                                || trangBiCanChuyenHoa.itemOptions.get(so).optionTemplate.id == 102) {
                            trangBi_daNangCap_daPhaLeHoa = true;
                            break;
                        }
                    }

                    if (!isTrangBiGoc(trangBiGoc)) {
                        Service.getInstance().sendThongBaoOK(player,
                                "Trang bị phải từ bậc lưỡng long, Jean hoặc Zelot trở lên");
                        return;
                    } else if (levelTrangBi < 4) {
                        Service.getInstance().sendThongBaoOK(player, "Trang bị gốc có cấp từ [+4]");
                        return;
                    } else if (!isTrangBiChuyenHoa(trangBiCanChuyenHoa)) {
                        Service.getInstance().sendThongBaoOK(player,
                                "Trang bị phải từ bậc lưỡng long, Jean hoặc Zelot trở lên");
                        return;
                    } else if (trangBi_daNangCap_daPhaLeHoa) {
                        Service.getInstance().sendThongBaoOK(player,
                                "Trang bị nhập thể phải chưa nâng cấp và pha lê hóa trang bị");
                        return;
                    } else if (!isCheckTrungTypevsGender(trangBiGoc, trangBiCanChuyenHoa)) {
                        Service.getInstance().sendThongBaoOK(player,
                                "Trang bị gốc và Trang bị nhập thể phải cùng loại và hành tinh");
                        return;
                    } else {
                        String NpcSay = "|2|Hiện tại " + trangBiCanChuyenHoa.getName() + "\n";
                        if (trangBiCanChuyenHoa.itemOptions != null) {
                            for (ItemOption io : trangBiCanChuyenHoa.itemOptions) {
                                if (io.optionTemplate.id != 72) {
                                    NpcSay += "|0|" + io.getOptionString() + "\n";
                                }
                            }
                        }
                        NpcSay += "|2|Sau khi nâng cấp (+" + levelTrangBi + ")\n";
                        for (ItemOption io : trangBiCanChuyenHoa.itemOptions) {
                            if (io.optionTemplate.id != 72) {
                                if (io.optionTemplate.id == 0 || io.optionTemplate.id == 47 || io.optionTemplate.id == 6
                                        || io.optionTemplate.id == 7 || io.optionTemplate.id == 14
                                        || io.optionTemplate.id == 22 || io.optionTemplate.id == 23) {
                                    NpcSay += "|1|" + io.getOptionString(chisogoc) + "\n";
                                } else {
                                    NpcSay += "|1|" + io.getOptionString() + "\n";
                                }
                            }
                        }
                        for (ItemOption io : trangBiGoc.itemOptions) {
                            if (io.optionTemplate.id != 72 && io.optionTemplate.id != 102 && io.optionTemplate.id != 107
                                    && io.optionTemplate.id != 0 && io.optionTemplate.id != 47
                                    && io.optionTemplate.id != 6 && io.optionTemplate.id != 7
                                    && io.optionTemplate.id != 14 && io.optionTemplate.id != 22
                                    && io.optionTemplate.id != 23) {
                                NpcSay += io.getOptionString() + "\n";
                            } else {
                            }
                        }
                        NpcSay += "Chuyển qua tất cả sao pha lê\n";
                        NpcSay += "|2|Cần 2 tỷ vàng";
                        baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, NpcSay,
                                "Nâng cấp\n2 tỷ\nvàng", "Từ chối");
                    }
                } else {
                    Service.getInstance().sendThongBaoOK(player,
                            "Cần 1 trang bị có cấp từ [+4] và 1 trang bị không có cấp nhưng cao hơn 1 bậc");
                    return;
                }
                break;

            case CHUYEN_HOA_BANG_NGOC:
                if (player.combineNew.itemsCombine.size() == 2) {
                    Item trangBiGoc = player.combineNew.itemsCombine.get(0);
                    Item trangBiCanChuyenHoa = player.combineNew.itemsCombine.get(1);

                    int ngocChuyenHoa = 5_000;

                    int levelTrangBi = 0;
                    int soLanRotCap = 0;
                    int chiSO1_trangBiCanChuyenHoa = 0;

                    for (ItemOption io : trangBiGoc.itemOptions) {
                        if (io.optionTemplate.id == 72) {
                            levelTrangBi = io.param - 1;
                        } else if (io.optionTemplate.id == 232) {
                            soLanRotCap += io.param;
                        }
                    }

                    // START Tính chỉ số nhân với số cấp và trừ với số lần rớt cấp //
                    int chisogoc = trangBiCanChuyenHoa.itemOptions.get(0).param;

                    chisogoc += chisogoc * (levelTrangBi * 0.1);

                    chisogoc -= chisogoc * (soLanRotCap * 0.1);
                    // END Tính chỉ số nhân với số cấp và trừ với số lần rớt cấp //

                    boolean trangBi_daNangCap_daPhaLeHoa = false;

                    for (int so = 0; so < trangBiCanChuyenHoa.itemOptions.size(); so++) {
                        if (trangBiCanChuyenHoa.itemOptions.get(so).optionTemplate.id == 72
                                || trangBiCanChuyenHoa.itemOptions.get(so).optionTemplate.id == 102) {
                            trangBi_daNangCap_daPhaLeHoa = true;
                            break;
                        }
                    }

                    if (!isTrangBiGoc(trangBiGoc)) {
                        Service.getInstance().sendThongBaoOK(player,
                                "Trang bị phải từ bậc lưỡng long, Jean hoặc Zelot trở lên");
                        return;
                    } else if (levelTrangBi < 4) {
                        Service.getInstance().sendThongBaoOK(player, "Trang bị gốc có cấp từ [+4]");
                        return;
                    } else if (!isTrangBiChuyenHoa(trangBiCanChuyenHoa)) {
                        Service.getInstance().sendThongBaoOK(player,
                                "Trang bị phải từ bậc lưỡng long, Jean hoặc Zelot trở lên");
                        return;
                    } else if (trangBi_daNangCap_daPhaLeHoa) {
                        Service.getInstance().sendThongBaoOK(player,
                                "Trang bị nhập thể phải chưa nâng cấp và pha lê hóa trang bị");
                        return;
                    } else if (!isCheckTrungTypevsGender(trangBiGoc, trangBiCanChuyenHoa)) {
                        Service.getInstance().sendThongBaoOK(player,
                                "Trang bị gốc và Trang bị nhập thể phải cùng loại và hành tinh");
                        return;
                    } else {
                        String NpcSay = "|2|Hiện tại " + trangBiCanChuyenHoa.getName() + "\n";
                        if (trangBiCanChuyenHoa.itemOptions != null) {
                            for (ItemOption io : trangBiCanChuyenHoa.itemOptions) {
                                if (io.optionTemplate.id != 72) {
                                    NpcSay += "|0|" + io.getOptionString() + "\n";
                                }
                            }
                        }
                        NpcSay += "|2|Sau khi nâng cấp (+" + levelTrangBi + ")\n";
                        for (ItemOption io : trangBiCanChuyenHoa.itemOptions) {
                            if (io.optionTemplate.id != 72) {
                                if (io.optionTemplate.id == 0 || io.optionTemplate.id == 47 || io.optionTemplate.id == 6
                                        || io.optionTemplate.id == 7 || io.optionTemplate.id == 14
                                        || io.optionTemplate.id == 22 || io.optionTemplate.id == 23) {
                                    NpcSay += "|1|" + io.getOptionString(chisogoc) + "\n";
                                } else {
                                    NpcSay += "|1|" + io.getOptionString() + "\n";
                                }
                            }
                        }
                        for (ItemOption io : trangBiGoc.itemOptions) {
                            if (io.optionTemplate.id != 72 && io.optionTemplate.id != 102 && io.optionTemplate.id != 107
                                    && io.optionTemplate.id != 0 && io.optionTemplate.id != 47
                                    && io.optionTemplate.id != 6 && io.optionTemplate.id != 7
                                    && io.optionTemplate.id != 14 && io.optionTemplate.id != 22
                                    && io.optionTemplate.id != 23) {
                                NpcSay += io.getOptionString() + "\n";
                            } else {
                            }
                        }
                        NpcSay += "Chuyển qua tất cả sao pha lê\n";
                        NpcSay += "|2|Cần 5000 ngọc";
                        baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, NpcSay,
                                "Nâng cấp\n5000\nngọc", "Từ chối");
                    }
                } else {
                    Service.getInstance().sendThongBaoOK(player,
                            "Cần 1 trang bị có cấp từ [+4] và 1 trang bị không có cấp nhưng cao hơn 1 bậc");
                    return;
                }
                break;

            case TAY_SACH:
                if (player.combineNew.itemsCombine.size() == 1) {
                    Item sachTuyetKy = null;
                    for (Item item : player.combineNew.itemsCombine) {
                        if (issachTuyetKy(item)) {
                            sachTuyetKy = item;
                        }
                    }
                    if (sachTuyetKy != null) {
                        String npcSay = "|2|Tẩy Sách Tuyệt Kỹ";
                        baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                "Đồng ý", "Từ chối");
                    } else {
                        Service.getInstance().sendThongBaoOK(player, "Cần Sách Tuyệt Kỹ để tẩy");
                        return;
                    }
                } else {
                    Service.getInstance().sendThongBaoOK(player, "Cần Sách Tuyệt Kỹ để tẩy");
                    return;
                }
                break;

            case NANG_CAP_SACH_TUYET_KY:
                if (player.combineNew.itemsCombine.size() == 2) {
                    Item sachTuyetKy = null;
                    Item kimBamGiay = null;
                    for (Item item : player.combineNew.itemsCombine) {
                        if (issachTuyetKy(item)
                                && (item.template.id == 1285 || item.template.id == 1287 || item.template.id == 1289)) {
                            sachTuyetKy = item;
                        } else if (item.template.id == 1282) {
                            kimBamGiay = item;
                        }
                    }
                    if (sachTuyetKy != null && kimBamGiay != null) {
                        String npcSay = "|2|Nâng cấp sách tuyệt kỹ\n";
                        npcSay += "Cần 10 Kìm bấm giấy\n"
                                + "Tỉ lệ thành công: 10%\n"
                                + "Nâng cấp thất bại sẽ mất 10 Kìm bấm giấy";
                        baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                "Nâng cấp", "Từ chối");
                    } else {
                        Service.getInstance().sendThongBaoOK(player, "Cần Sách Tuyệt Kỹ 1 và 10 Kìm bấm giấy.");
                        return;
                    }
                } else {
                    Service.getInstance().sendThongBaoOK(player, "Cần Sách Tuyệt Kỹ 1 và 10 Kìm bấm giấy.");
                    return;
                }
                break;
            case PHUC_HOI_SACH:
                if (player.combineNew.itemsCombine.size() == 1) {
                    Item sachTuyetKy = null;
                    for (Item item : player.combineNew.itemsCombine) {
                        if (issachTuyetKy(item)) {
                            sachTuyetKy = item;
                        }
                    }
                    if (sachTuyetKy != null) {
                        String npcSay = "|2|Phục hồi " + sachTuyetKy.getName() + "\n"
                                + "Cần 10 cuốn sách cũ\n"
                                + "Phí phục hồi 10 triệu vàng";
                        baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                "Đồng ý", "Từ chối");
                    } else {
                        Service.getInstance().sendThongBaoOK(player, "Không tìm thấy vật phẩm");
                        return;
                    }
                } else {
                    Service.getInstance().sendThongBaoOK(player, "Không tìm thấy vật phẩm");
                    return;
                }
                break;
            case PHAN_RA_SACH:
                if (player.combineNew.itemsCombine.size() == 1) {
                    Item sachTuyetKy = null;
                    for (Item item : player.combineNew.itemsCombine) {
                        if (issachTuyetKy(item)) {
                            sachTuyetKy = item;
                        }
                    }
                    if (sachTuyetKy != null) {
                        String npcSay = "|2|Phân rã sách\n"
                                + "Nhận lại 5 cuốn sách cũ\n"
                                + "Phí rã 10 triệu vàng";
                        baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                "Đồng ý", "Từ chối");
                    } else {
                        Service.getInstance().sendThongBaoOK(player, "Không tìm thấy vật phẩm");
                        return;
                    }
                } else {
                    Service.getInstance().sendThongBaoOK(player, "Không tìm thấy vật phẩm");
                    return;
                }
                break;

            // END _ SÁCH TUYỆT KỸ //
            case NHAP_NGOC_RONG:
                if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                    if (player.combineNew.itemsCombine.size() == 1) {
                        Item item = player.combineNew.itemsCombine.get(0);
                        if (item != null && item.isNotNullItem()) {
                            if ((item.template.id > 14 && item.template.id <= 20) && item.quantity >= 7) {
                                String npcSay = "|2|Con có muốn biến 7 " + item.template.name + " thành\n" + "1 viên "
                                        + ItemService.gI().getTemplate((short) (item.template.id - 1)).name + "\n"
                                        + "|7|Cần 7 " + item.template.name;
                                this.baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay, "Làm phép",
                                        "Từ chối");
                            } else if ((item.template.id == 14 && item.quantity >= 7)) {
                                String npcSay = "|2|Con có muốn biến 7 " + item.template.name + " thành\n" + "1 viên "
                                        + ItemService.gI().getTemplate((short) (925)).name + "\n" + "\n|7|Cần 7 "
                                        + item.template.name + "\n|7|Cần 500tr Vàng";
                                this.baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay, "Làm phép",
                                        "Từ chối");
                            } else if (item.template.id == 926 && item.quantity >= 7) {
                                String npcSay = "|2|Con có muốn biến 7 " + item.template.name + " thành\n" + "1 viên "
                                        + ItemService.gI().getTemplate((short) (925)).name + "\n" + "\n|7|Cần 7 "
                                        + item.template.name + "\n|7|Cần 500tr Vàng";
                                this.baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay, "Làm phép",
                                        "Từ chối");
                            } else {
                                this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                        "Cần 7 viên ngọc rồng 2 sao trở lên", "Đóng");
                            }
                        }
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Cần 7 viên ngọc rồng 2 sao trở lên", "Đóng");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hành trang cần ít nhất 1 chỗ trống",
                            "Đóng");
                }
                break;
            case NANG_CAP_BONG_TAI:
                if (player.combineNew.itemsCombine.size() == 2) {
                    Item bongtai = null;
                    Item manhvobt = null;
                    for (Item item : player.combineNew.itemsCombine) {
                        if (checkBongTaiNangCap(item)) {
                            bongtai = item;
                        } else if (item.template.id == 933) {
                            manhvobt = item;
                        }
                    }

                    if (bongtai != null && manhvobt != null) {
                        int level = 0;
                        for (ItemOption io : bongtai.itemOptions) {
                            if (io.optionTemplate.id == 72) {
                                level = io.param;
                                break;
                            }
                        }
                        int lvbt = lvbt(bongtai);
                        int countmvbt = 9999;
                        player.combineNew.goldCombine = 5_000_000;
                        player.combineNew.gemCombine = 20;
                        player.combineNew.ratioCombine = 50;

                        int soluongMVBT = 0;
                        for (ItemOption io : manhvobt.itemOptions) {
                            if (io.optionTemplate.id == 31) {
                                soluongMVBT = io.param;
                                break;
                            }
                        }

                        String npcSay = "|2|Bông tai Porata [+2]\n";
                        for (ItemOption io : bongtai.itemOptions) {
                            npcSay += io.getOptionString() + "\n";
                        }
                        npcSay += "Tỉ lệ thành công: 50%" + "\n";
                        if (soluongMVBT < countmvbt) {
                            npcSay += "|7|Cần 9999 Mảnh vỡ bông tai\n";
                        } else {
                            npcSay += "Cần 9999 Mảnh vỡ bông tai\n";
                        }
                        if (player.inventory.gold <= player.combineNew.goldCombine) {
                            npcSay += "|7|Cần 5 Tr vàng\n";
                        } else {
                            npcSay += "|2|Cần 5 Tr vàng\n";
                        }
                        if (player.inventory.gem <= player.combineNew.gemCombine) {
                            npcSay += "|7|Cần 20 ngọc\n";
                        } else {
                            npcSay += "|2|Cần 20 ngọc\n";
                        }
                        npcSay += "|7|Thất bại -99 mảnh vỡ bông tai";

                        if (soluongMVBT >= countmvbt) {
                            if (player.combineNew.goldCombine <= player.inventory.gold) {
                                if (player.combineNew.gemCombine <= player.inventory.gem) {
                                    baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                            "Nâng cấp\n5 Tr vàng\n20 ngọc", "Từ chối");
                                } else {
                                    baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                                }
                            } else {
                                baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                            }
                        } else {
                            baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                        }
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Cần 1 bông tai Porata và 9999 Mảnh vỡ bông tai", "Đóng");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Cần 1 bông tai Porata và 9999 Mảnh vỡ bông tai", "Đóng");
                }
                break;
            case MO_CHI_SO_BONG_TAI:
                if (player.combineNew.itemsCombine.size() == 3) {
                    Item bongTai = null;
                    Item manhHon = null;
                    Item daXanhLam = null;
                    for (Item item : player.combineNew.itemsCombine) {
                        if (item.template.id == 921) {
                            bongTai = item;
                        } else if (item.template.id == 934) {
                            manhHon = item;
                        } else if (item.template.id == 935) {
                            daXanhLam = item;
                        }
                    }
                    if (bongTai != null && manhHon != null && daXanhLam != null && manhHon.quantity >= 99) {

                        player.combineNew.gemCombine = Gem_MOCS_BONG_TAI;
                        player.combineNew.ratioCombine = RATIO_NANG_CAP;

                        String npcSay = "|2|Bông tai Porata [+2]\n";

                        npcSay += "\n";

                        npcSay += "Tỉ lệ thành công: 50%" + "\n";
                        if (manhHon.quantity < 99) {
                            npcSay += "|7|Cần 99 Mảnh hồn bông tai\n";
                        } else {
                            npcSay += "Cần 99 Mảnh hồn bông tai\n";
                        }
                        npcSay += "|2|Cần 1 Đá xanh lam\n";
                        if (player.inventory.gem < 250) {
                            npcSay += "|7|Cần 250 ngọc\n";
                        } else {
                            npcSay += "Cần 250 ngọc\n";
                        }
                        npcSay += "|1|+1 Chỉ số ngẫu nhiên\n";
                        if (player.combineNew.gemCombine <= player.inventory.gem) {
                            baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                    "Nâng cấp\n" + player.combineNew.gemCombine + " ngọc", "Từ chối");
                        } else {
                            baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                        }
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Cần 1 Bông tai Porata cấp 2 hoặc 3, X99 Mảnh hồn bông tai và 1 Đá xanh lam", "Đóng");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Cần 1 Bông tai Porata cấp 2 hoặc 3, X99 Mảnh hồn bông tai và 1 Đá xanh lam", "Đóng");
                }
                break;
            case NANG_CAP_SKH:
                if (player.combineNew.itemsCombine.size() == 3) {
                    Item isItemSKH = null;
                    Item isItemThanLinh = null;
                    Item daNangCap = null;

                    int levelTrangBi = 0;
                    player.combineNew.goldCombine = 2_000_000_000;
                    int goldCombie = player.combineNew.goldCombine;
                    for (Item item : player.combineNew.itemsCombine) {
                        if (isHuyDiet(item)) {
                            isItemThanLinh = item;
                        }
                        if (item.template.id == 1314) {
                            daNangCap = item;
                        }
                        for (int i = 0; i < item.itemOptions.size(); i++) {
                            for (int option = 127; option <= 135; option++) {
                                if (checkHaveOption(item, i, option)) {
                                    isItemSKH = item;
                                    break;
                                }
                            }
                        }
                    }
                    if (isItemSKH != null) {
                        for (ItemOption io : isItemSKH.itemOptions) {
                            for (int option = 127; option <= 135; option++) {
                                if (io.optionTemplate.id == option) {
                                    levelTrangBi = io.param;
                                }
                            }
                        }
                    }
                    if (levelTrangBi >= 10) {
                        Service.getInstance().sendThongBaoOK(player, "SKH đã đạt level tối đa");
                        return;
                    }
                    if (daNangCap != null && isItemThanLinh != null && isItemSKH != null) {
                        String npcSay = "Vật phẩm SKH được nâng cấp: " + isItemSKH.getName() + "\n";
                        npcSay += "\n|1|Sau khi nâng cấp SKH sẽ được tăng 10% chỉ số kích hoạt\n";
                        npcSay += "\n|2|Tỷ lệ thành công: 50%";
                        npcSay += "\n|2|Cần " + Util.numberToMoney(goldCombie) + " vàng";
                        npcSay += "\n|7|Thất bại sẽ mất đồ Huỷ Diệt và 2 tỷ vàng";

                        baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay, "Nâng cấp", "Từ chối");
                    } else {
                        Service.getInstance().sendThongBaoOK(player,
                                "Cần một món Huỷ Diệt, Đá nâng cấp kích hoạt và một món kích hoạt");
                        return;
                    }
                } else {
                    Service.getInstance().sendThongBaoOK(player,
                            "Cần một món Huỷ Diệt, Đá nâng cấp kích hoạt và một món kích hoạt");
                    return;
                }
                break;

            case NANG_CAP_DO_TL: {
                List<Item> items = player.combineNew.itemsCombine;
                if (items.isEmpty()) {
                    baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Hãy đưa ta 3 món thần linh, ta sẽ cho 1 món huỷ diệt tương ứng", "Đóng");
                    return;
                }

                if (items.size() == 3) {
                    long dtlCount = items.stream().filter(item -> item.isNotNullItem() && item.isDTL()).count();

                    if (dtlCount != 3) {
                        baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu đồ thần linh rồi", "Đóng");
                        return;
                    }

                    if (player.inventory.gold < COST_NANG_CAP_DO_TL) {
                        baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hết tiền rồi\nẢo ít thôi con", "Đóng");
                        return;
                    }

                    String typeName = items.stream().filter(Item::isDTL).findFirst().get().typeName();
                    String npcSay = "|2|Con có muốn đổi các món nguyên liệu ?\n|7|Nhận được " + typeName
                            + " Huỷ diệt tương ứng\n"
                            + "|1|Cần " + Util.numberToMoney(COST_NANG_CAP_DO_TL) + " vàng";

                    baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
                            npcSay, "Nâng cấp \nCần " + Util.numberToMoney(COST_NANG_CAP_DO_TL) + " Vàng", "Từ chối");
                    return;
                }

                if (items.size() > 1) {
                    baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Nguyên liệu không phù hợp", "Đóng");
                } else {
                    baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Còn thiếu nguyên liệu để nâng cấp hãy quay lại sau", "Đóng");
                }
            }
                break;

            case NANG_CAP_VAT_PHAM:
                if (player.combineNew.itemsCombine.size() >= 2 && player.combineNew.itemsCombine.size() < 4) {
                    if (player.combineNew.itemsCombine.stream()
                            .filter(item -> item.isNotNullItem() && item.template.type < 5).count() < 1) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu đồ nâng cấp", "Đóng");
                        break;
                    }
                    if (player.combineNew.itemsCombine.stream()
                            .filter(item -> item.isNotNullItem() && item.template.type == 14).count() < 1) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu đá nâng cấp", "Đóng");
                        break;
                    }
                    if (player.combineNew.itemsCombine.size() == 3 && player.combineNew.itemsCombine.stream()
                            .filter(item -> item.isNotNullItem() && item.template.id == 987).count() < 1) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu đồ nâng cấp", "Đóng");
                        break;
                    }
                    Item itemDo = null;
                    Item itemDNC = null;
                    Item itemDBV = null;
                    for (int j = 0; j < player.combineNew.itemsCombine.size(); j++) {
                        if (player.combineNew.itemsCombine.get(j).isNotNullItem()) {
                            if (player.combineNew.itemsCombine.size() == 3
                                    && player.combineNew.itemsCombine.get(j).template.id == 987) {
                                itemDBV = player.combineNew.itemsCombine.get(j);
                                continue;
                            }
                            if (player.combineNew.itemsCombine.get(j).template.type < 5) {
                                itemDo = player.combineNew.itemsCombine.get(j);
                            } else {
                                itemDNC = player.combineNew.itemsCombine.get(j);
                            }
                        }
                    }
                    if (isCoupleItemNangCapCheck(itemDo, itemDNC)) {
                        int level = 0;
                        for (ItemOption io : itemDo.itemOptions) {
                            if (io.optionTemplate.id == 72) {
                                level = io.param;
                                break;
                            }
                        }
                        if (level < MAX_LEVEL_ITEM) {
                            player.combineNew.goldCombine = getGoldNangCapDo(level);
                            player.combineNew.ratioCombine = (float) getTileNangCapDo(level);
                            player.combineNew.countDaNangCap = getCountDaNangCapDo(level);
                            player.combineNew.countDaBaoVe = (short) getCountDaBaoVe(level);
                            String npcSay = "|2|Hiện tại " + itemDo.template.name + " (+" + level + ")\n|0|";
                            for (ItemOption io : itemDo.itemOptions) {
                                if (io.optionTemplate.id != 72) {
                                    npcSay += io.getOptionString() + "\n";
                                }
                            }
                            String option = null;
                            int param = 0;
                            for (ItemOption io : itemDo.itemOptions) {
                                if (io.optionTemplate.id == 47
                                        || io.optionTemplate.id == 6
                                        || io.optionTemplate.id == 0
                                        || io.optionTemplate.id == 7
                                        || io.optionTemplate.id == 14
                                        || io.optionTemplate.id == 22
                                        || io.optionTemplate.id == 23) {
                                    option = io.optionTemplate.name;
                                    param = io.param + (io.param * 10 / 100);
                                    break;
                                }
                            }
                            npcSay += "|2|Sau khi nâng cấp (+" + (level + 1) + ")\n|7|"
                                    + option.replaceAll("#", String.valueOf(param))
                                    + "\n|7|Tỉ lệ thành công: " + player.combineNew.ratioCombine + "%\n"
                                    + (player.combineNew.countDaNangCap > itemDNC.quantity ? "|7|" : "|1|")
                                    + "Cần " + player.combineNew.countDaNangCap + " " + itemDNC.template.name
                                    + "\n" + (player.combineNew.goldCombine > player.inventory.gold ? "|7|" : "|1|")
                                    + "Cần " + Util.numberToMoney(player.combineNew.goldCombine) + " vàng";

                            String daNPC = player.combineNew.itemsCombine.size() == 3 && itemDBV != null
                                    ? String.format("\nCần tốn %s đá bảo vệ", player.combineNew.countDaBaoVe)
                                    : "";
                            if ((level == 2 || level == 4 || level == 6)
                                    && !(player.combineNew.itemsCombine.size() == 3 && itemDBV != null)) {
                                npcSay += "\nNếu thất bại sẽ rớt xuống (+" + (level - 1) + ")";
                            }
                            if (player.combineNew.countDaNangCap > itemDNC.quantity) {
                                this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                        npcSay, "Còn thiếu\n" + (player.combineNew.countDaNangCap - itemDNC.quantity)
                                                + " " + itemDNC.template.name);
                            } else if (player.combineNew.goldCombine > player.inventory.gold) {
                                this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                        npcSay,
                                        "Còn thiếu\n"
                                                + Util.numberToMoney(
                                                        (player.combineNew.goldCombine - player.inventory.gold))
                                                + " vàng");
                            } else if (player.combineNew.itemsCombine.size() == 3 && Objects.nonNull(itemDBV)
                                    && itemDBV.quantity < player.combineNew.countDaBaoVe) {
                                this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                        npcSay, "Còn thiếu\n" + (player.combineNew.countDaBaoVe - itemDBV.quantity)
                                                + " đá bảo vệ");
                            } else {
                                this.baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
                                        npcSay, "Nâng cấp\n" + Util.numberToMoney(player.combineNew.goldCombine)
                                                + " vàng" + daNPC,
                                        "Từ chối");
                            }
                        } else {
                            this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                    "Trang bị của ngươi đã đạt cấp tối đa", "Đóng");
                        }
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Hãy chọn 1 trang bị và 1 loại đá nâng cấp", "Đóng");
                    }
                } else {
                    if (player.combineNew.itemsCombine.size() > 3) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Cất đi con ta không thèm", "Đóng");
                        break;
                    }
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Hãy chọn 1 trang bị và 1 loại đá nâng cấp", "Đóng");
                }
                break;
            case DOI_VE_HUY_DIET:
                if (player.combineNew.itemsCombine.size() == 1) {
                    Item item = player.combineNew.itemsCombine.get(0);
                    if (item.isNotNullItem() && item.template.id >= 555 && item.template.id <= 567) {
                        String ticketName = "Vé đổi " + (item.template.type == 0 ? "áo"
                                : item.template.type == 1 ? "quần"
                                        : item.template.type == 2 ? "găng" : item.template.type == 3 ? "giày" : "nhẫn")
                                + " hủy diệt";
                        String npcSay = "|6|Ngươi có chắc chắn muốn đổi\n|7|" + item.template.name + "\n";
                        for (ItemOption io : item.itemOptions) {
                            npcSay += "|2|" + io.getOptionString() + "\n";
                        }
                        npcSay += "|6|Lấy\n|7|" + ticketName + "\n|6|Với giá "
                                + Util.numberToMoney(COST_DOI_VE_DOI_DO_HUY_DIET) + " vàng không?";
                        if (player.inventory.gold >= COST_DOI_VE_DOI_DO_HUY_DIET) {
                            this.baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay, "Đổi",
                                    "Từ chối");
                        } else {
                            this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Còn thiếu\n"
                                    + Util.numberToMoney(COST_DOI_VE_DOI_DO_HUY_DIET - player.inventory.gold) + " vàng",
                                    "Đóng");
                        }

                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Hãy chọn 1 trang bị thần linh ngươi muốn trao đổi", "Đóng");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Hãy chọn 1 trang bị thần linh ngươi muốn trao đổi", "Đóng");
                }
                break;
            case DOI_HOP_QUA_HALLOWEEN:
                if (player.combineNew.itemsCombine.size() == 5) {
                    Item bikiep = player.combineNew.itemsCombine.stream()
                            .filter(item -> item.isNotNullItem() && item.template.id == 590).findFirst().orElse(null);
                    Item vpsk1 = player.combineNew.itemsCombine.stream()
                            .filter(item -> item.isNotNullItem() && item.template.id == 2138).findFirst().orElse(null);
                    Item vpsk2 = player.combineNew.itemsCombine.stream()
                            .filter(item -> item.isNotNullItem() && item.template.id == 900).findFirst().orElse(null);
                    Item vpsk3 = player.combineNew.itemsCombine.stream()
                            .filter(item -> item.isNotNullItem() && item.template.id == 903).findFirst().orElse(null);
                    Item vpsk4 = player.combineNew.itemsCombine.stream()
                            .filter(item -> item.isNotNullItem() && item.template.id == 899).findFirst().orElse(null);
                    // Item dhd = player.combineNew.itemsCombine.stream().filter(item ->
                    // item.isNotNullItem() && item.template.id >= 650 && item.template.id <=
                    // 662).findFirst().orElse(null);

                    if (bikiep == null || bikiep.quantity < 999) {
                        this.obito.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Ngươi còn thiếu bí kiếp", "OK");
                        return;
                    }

                    if (vpsk1 == null || vpsk1.quantity < 99) {
                        this.obito.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Ngươi còn thiếu bí ngô", "OK");
                        return;
                    }

                    if (vpsk2 == null || vpsk2.quantity < 99) {
                        this.obito.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Ngươi còn thiếu súp bí hắc ám", "OK");
                        return;
                    }

                    if (vpsk3 == null || vpsk3.quantity < 10) {
                        this.obito.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Ngươi còn thiếu Hamburger sâu", "OK");
                        return;
                    }
                    if (vpsk4 == null || vpsk4.quantity < 30) {
                        this.obito.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Ngươi còn thiếu kẹo 1 mắt", "OK");
                        return;
                    }
                    // if (dhd == null || dhd.quantity < 1) {
                    // this.obito.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Ngươi còn thiếu một
                    // món đồ hủy diệt", "OK");
                    // return;
                    // }

                    String npcSay = "|7| Ngươi có muốn đổi hộp quà Halloween ";
                    npcSay += "\n|2|Cần " + Util.numberToMoney(COST_DAP_DO_KICH_HOAT) + " vàng";
                    if (player.inventory.gold >= COST_DAP_DO_KICH_HOAT) {
                        this.obito.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                "Cần " + Util.numberToMoney(COST_DAP_DO_KICH_HOAT) + " vàng");
                    } else {
                        this.obito.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay,
                                "Còn thiếu\n" + Util.numberToMoney(player.inventory.gold - COST_DAP_DO_KICH_HOAT)
                                        + " vàng");
                    }
                } else {
                    this.obito.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu vật phảm", "OK");
                }
                break;

            case DAP_SET_KICH_HOAT:
                if (player.combineNew.itemsCombine.size() == 1 || player.combineNew.itemsCombine.size() == 2) {
                    Item dhd = null, dtl = null;
                    for (Item item : player.combineNew.itemsCombine) {
                        if (item.isNotNullItem()) {
                            if (item.template.id >= 650 && item.template.id <= 662) {
                                dhd = item;
                            } else if (item.template.id >= 555 && item.template.id <= 567) {
                                dtl = item;
                            }
                        }
                    }
                    if (dhd != null) {
                        String npcSay = "|6|" + dhd.template.name + "\n";
                        for (ItemOption io : dhd.itemOptions) {
                            npcSay += "|2|" + io.getOptionString() + "\n";
                        }
                        if (dtl != null) {
                            npcSay += "|6|" + dtl.template.name + "\n";
                            for (ItemOption io : dtl.itemOptions) {
                                npcSay += "|2|" + io.getOptionString() + "\n";
                            }
                        }
                        npcSay += "Ngươi có muốn chuyển hóa thành\n";
                        npcSay += "|1|" + getNameItemC0(dhd.template.gender, dhd.template.type)
                                + " (ngẫu nhiên kích hoạt)\n|7|Tỉ lệ thành công " + (dtl != null ? "100%" : "40%")
                                + "\n|2|Cần " + Util.numberToMoney(COST_DAP_DO_KICH_HOAT) + " vàng";
                        if (player.inventory.gold >= COST_DAP_DO_KICH_HOAT) {
                            this.baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                    "Cần " + Util.numberToMoney(COST_DAP_DO_KICH_HOAT) + " vàng");
                        } else {
                            this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Còn thiếu\n"
                                    + Util.numberToMoney(player.inventory.gold - COST_DAP_DO_KICH_HOAT) + " vàng");
                        }
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Ta cần 1 món đồ hủy diệt của ngươi để có thể chuyển hóa 1", "Đóng");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Ta cần 1 món đồ hủy diệt của ngươi để có thể chuyển hóa 2", "Đóng");
                }
                break;
            // case DOI_MANH_KICH_HOAT:
            // if (player.combineNew.itemsCombine.size() == 2 ||
            // player.combineNew.itemsCombine.size() == 3) {
            // Item nr1s = null, doThan = null, buaBaoVe = null;
            // for (Item it : player.combineNew.itemsCombine) {
            // if (it.template.id == 14) {
            // nr1s = it;
            // } else if (it.template.id == 2010) {
            // buaBaoVe = it;
            // } else if (it.template.id >= 555 && it.template.id <= 567) {
            // doThan = it;
            // }
            // }
            //
            // if (nr1s != null && doThan != null) {
            // int tile = 50;
            // String npcSay = "|6|Ngươi có muốn trao đổi\n|7|" + nr1s.template.name +
            // "\n|7|" + doThan.template.name
            // + "\n";
            // for (ItemOption io : doThan.itemOptions) {
            // npcSay += "|2|" + io.getOptionString() + "\n";
            // }
            // if (buaBaoVe != null) {
            // tile = 100;
            // npcSay += buaBaoVe.template.name
            // + "\n";
            // for (ItemOption io : buaBaoVe.itemOptions) {
            // npcSay += "|2|" + io.getOptionString() + "\n";
            // }
            // }
            //
            // npcSay += "|6|Lấy\n|7|Mảnh kích hoạt\n"
            // + "|1|Tỉ lệ " + tile + "%\n"
            // + "|6|Với giá " + Util.numberToMoney(COST_DOI_MANH_KICH_HOAT) + " vàng
            // không?";
            // if (player.inventory.gold >= COST_DOI_MANH_KICH_HOAT) {
            // this.baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
            // npcSay, "Đổi", "Từ chối");
            // } else {
            // this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
            // npcSay, "Còn thiếu\n"
            // + Util.numberToMoney(COST_DOI_MANH_KICH_HOAT - player.inventory.gold) + "
            // vàng", "Đóng");
            // }
            // } else {
            // this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hãy chọn 1 trang
            // bị thần linh và 1 viên ngọc rồng 1 sao", "Đóng");
            // }
            // } else {
            // this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hãy chọn 1 trang
            // bị thần linh và 1 viên ngọc rồng 1 sao", "Đóng");
            // }
            // break;
            case DAP_SET_KICH_HOAT_CAO_CAP: {
                if (player.combineNew.itemsCombine.size() == 2) {
                    List<Item> items = player.combineNew.itemsCombine;
                    boolean allAreDestroyClothes = items.stream().allMatch(it -> isDestroyClothes(it.template.id));

                    if (allAreDestroyClothes) {
                        String npcSay = items.stream()
                                .map(it -> "|1|" + it.template.name)
                                .collect(Collectors.joining("\n"));
                        npcSay += "\nNgươi có muốn chuyển hóa thành\n";
                        npcSay += "|7|" + getTypeTrangBi(items.get(0).template.type)
                                + " cấp bậc ngẫu nhiên (set kích hoạt ngẫu nhiên)\n|2|Cần "
                                + Util.numberToMoney(COST_DAP_DO_KICH_HOAT) + " vàng";

                        if (player.inventory.gold >= COST_DAP_DO_KICH_HOAT) {
                            this.baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                    "Cần " + Util.numberToMoney(COST_DAP_DO_KICH_HOAT) + " vàng");
                        } else {
                            long thieu = COST_DAP_DO_KICH_HOAT - player.inventory.gold;
                            this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay,
                                    "Còn thiếu\n" + Util.numberToMoney(thieu) + " vàng");
                        }
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Cần đủ 2 món đồ hủy diệt để chuyển hóa!", "Đóng");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Cần đúng 2 món đồ hủy diệt để chuyển hóa!", "Đóng");
                }
            }
                break;
            case GIA_HAN_CAI_TRANG:
                if (player.combineNew.itemsCombine.size() == 2) {
                    Item caitrang = null, vegiahan = null;
                    for (Item item : player.combineNew.itemsCombine) {
                        if (item.isNotNullItem()) {
                            if (item.template.type == 5) {
                                caitrang = item;
                            } else if (item.template.id == 2022) {
                                vegiahan = item;
                            }
                        }
                    }
                    int expiredDate = 0;
                    boolean canBeExtend = true;
                    if (caitrang != null && vegiahan != null) {
                        for (ItemOption io : caitrang.itemOptions) {
                            if (io.optionTemplate.id == 93) {
                                expiredDate = io.param;
                            }
                            if (io.optionTemplate.id == 199) {
                                canBeExtend = false;
                            }
                        }
                        if (canBeExtend) {
                            if (expiredDate > 0) {
                                String npcSay = "|2|" + caitrang.template.name + "\n"
                                        + "Sau khi gia hạn +1 ngày \n Tỷ lệ thành công: 101% \n" + "|7|Cần 500tr vàng";
                                if (player.inventory.gold >= COST_GIA_HAN_CAI_TRANG) {
                                    this.baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                            "Gia hạn");
                                } else {
                                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay,
                                            "Còn thiếu\n"
                                                    + Util.numberToMoney(player.inventory.gold - COST_GIA_HAN_CAI_TRANG)
                                                    + " vàng");
                                }
                            } else {
                                this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                        "Cần cải trang có hạn sử dụng và thẻ gia hạn", "Đóng");
                            }
                        } else {
                            this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                    "Vật phẩm này không thể gia hạn", "Đóng");
                        }
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Ta Cần cải trang có hạn sử dụng và thẻ gia hạn", "Đóng");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Ta Cần cải trang có hạn sử dụng và thẻ gia hạn", "Đóng");
                }
                break;
            case NANG_CAP_DO_THIEN_SU:
                if (player.combineNew.itemsCombine.size() > 1) {
                    int ratioLuckyStone = 0, ratioRecipe = 0, ratioUpgradeStone = 0, countLuckyStone = 0,
                            countUpgradeStone = 0;
                    Item angelClothes = null;
                    Item craftingRecipe = null;
                    for (Item item : player.combineNew.itemsCombine) {
                        int id = item.template.id;
                        if (item.isNotNullItem()) {
                            if (isAngelClothes(id)) {
                                angelClothes = item;
                            } else if (isLuckyStone(id)) {
                                ratioLuckyStone += getRatioLuckyStone(id);
                                countLuckyStone++;
                            } else if (isUpgradeStone(id)) {
                                ratioUpgradeStone += getRatioUpgradeStone(id);
                                countUpgradeStone++;
                            } else if (isCraftingRecipe(id)) {
                                ratioRecipe += getRatioCraftingRecipe(id);
                                craftingRecipe = item;
                            }
                        }
                    }
                    if (angelClothes == null) {
                        return;
                    }
                    boolean canUpgrade = true;
                    for (ItemOption io : angelClothes.itemOptions) {
                        int optId = io.optionTemplate.id;
                        if (optId == 41) {
                            canUpgrade = false;
                        }
                    }
                    if (angelClothes.template.gender != craftingRecipe.template.gender) {
                        this.whis.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Vui lòng chọn đúng công thức chế tạo",
                                "Đóng");
                        return;
                    }
                    if (canUpgrade) {
                        if (craftingRecipe != null) {
                            if (countLuckyStone < 2 && countUpgradeStone < 2) {
                                int ratioTotal = (20 + ratioUpgradeStone + ratioRecipe);
                                int ratio = ratioTotal > 75 ? ratio = 75 : ratioTotal;
                                String npcSay = "|1| Nâng cấp " + angelClothes.template.name + "\n|7|";
                                npcSay += ratioRecipe > 0 ? " Công thức VIP (+" + ratioRecipe + "% tỉ lệ thành công)\n"
                                        : "";
                                npcSay += ratioUpgradeStone > 0
                                        ? "Đá nâng cấp cấp " + ratioUpgradeStone / 10 + " (+" + ratioUpgradeStone
                                                + "% tỉ lệ thành công)\n"
                                        : "";
                                npcSay += ratioLuckyStone > 0
                                        ? "Đá nâng may mắn cấp " + ratioLuckyStone / 10 + " (+" + ratioLuckyStone
                                                + "% tỉ lệ tối đa các chỉ số)\n"
                                        : "";
                                npcSay += "Tỉ lệ thành công: " + ratio + "%\n";
                                npcSay += "Phí nâng cấp: " + Util.numberToMoney(COST_DAP_DO_KICH_HOAT) + " vàng";
                                if (player.inventory.gold >= COST_DAP_DO_KICH_HOAT) {
                                    this.whis.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay, "Nâng cấp");
                                } else {
                                    this.whis.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay,
                                            "Còn thiếu\n"
                                                    + Util.numberToMoney(player.inventory.gold - COST_DAP_DO_KICH_HOAT)
                                                    + " vàng");
                                }
                            } else {
                                this.whis.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                        "Chỉ có thể sự dụng tối đa 1 loại nâng cấp và đá may mắn", "Đóng");
                            }
                        } else {
                            this.whis.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                    "Người cần ít nhất 1 trang bị thiên sứ và 1 công thức để có thể nâng cấp", "Đóng");
                        }
                    } else {
                        this.whis.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Mỗi vật phẩm chỉ có thể nâng cấp 1 lần", "Đóng");
                    }
                } else {
                    this.whis.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Người cần ít nhất 1 trang bị thiên sứ và 1 công thức để có thể nâng cấp", "Đóng");
                }
                break;
        }
    }

    /**
     * Bắt đầu đập đồ - điều hướng từng loại đập đồ
     *
     * @param player
     */
    public void startCombine(Player player, int select) {
        if (Util.canDoWithTime(player.combineNew.lastTimeCombine, TIME_COMBINE)) {
            switch (player.combineNew.typeCombine) {
                case NANG_CAP_DO_TL:
                    nangCapThanLinhLenHuyDiet(player);
                    break;
                case EP_SAO_TRANG_BI:
                    epSaoTrangBi(player);
                    break;
                case PHA_LE_HOA_TRANG_BI:
                    switch (select) {
                        case 0:
                            phaLeHoaTrangBix100(player);
                            break;
                        case 1:
                            phaLeHoaTrangBix10(player);
                            break;
                        case 2:
                            phaLeHoaTrangBi(player);
                            break;
                    }
                    break;
                case NHAP_NGOC_RONG:
                    nhapNgocRong(player);
                    break;
                case NANG_CAP_VAT_PHAM:
                    nangCapVatPham(player);
                    break;
                case NANG_CAP_SKH:
                    nangCapSKH(player);
                    break;
                case DOI_VE_HUY_DIET:
                    doiVeHuyDiet(player);
                    break;
                case DOI_HOP_QUA_HALLOWEEN:
                    doihopquahalloween(player);
                    break;
                case DAP_SET_KICH_HOAT:
                    dapDoKichHoat(player);
                    break;

                case DAP_SET_KICH_HOAT_CAO_CAP:
                    dapDoKichHoatCaoCap(player);
                    break;
                case GIA_HAN_CAI_TRANG:
                    giaHanCaiTrang(player);
                    break;
                case NANG_CAP_DO_THIEN_SU:
                    nangCapDoThienSu(player);
                    break;
                case CHE_TAO_DO_THIEN_SU:
                    openCreateItemAngel(player);
                    break;
                case NANG_CAP_BONG_TAI:
                    nangCapBongTai(player);
                    break;
                case MO_CHI_SO_BONG_TAI:
                    moChiSoBongTai(player);
                    break;

                case CHUYEN_HOA_BANG_VANG:
                    chuyenHoaTrangBiVang(player);
                    break;
                case CHUYEN_HOA_BANG_NGOC:
                    chuyenHoaTrangBiNgoc(player);
                    break;
                // START _ SÁCH TUYỆT KỸ //
                case GIAM_DINH_SACH:
                    giamDinhSach(player);
                    break;
                case TAY_SACH:
                    taySach(player);
                    break;
                case NANG_CAP_SACH_TUYET_KY:
                    nangCapSachTuyetKy(player);
                    break;
                case PHUC_HOI_SACH:
                    phucHoiSach(player);
                    break;
                case PHAN_RA_SACH:
                    phanRaSach(player);
                    break;
                // END _ SÁCH TUYỆT KỸ //
            }
            player.iDMark.setIndexMenu(ConstNpc.IGNORE_MENU);
            player.combineNew.clearParamCombine();
            player.combineNew.lastTimeCombine = System.currentTimeMillis();
        }
    }

    private void nangCapThanLinhLenHuyDiet(Player player) {
        List<Item> items = player.combineNew.itemsCombine;
        if (items.size() != 3) {
            Service.getInstance().sendThongBao(player, "Cần đúng 3 món để nâng cấp!");
            return;
        }
        List<Item> dtlItems = items.stream()
                .filter(item -> item != null && item.isDTL())
                .toList();

        if (dtlItems.size() != 3) {
            Service.getInstance().sendThongBao(player, "Cần 3 món thần linh để nâng cấp!");
            return;
        }
        Item dtl = dtlItems.get(0);
        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.getInstance().sendThongBao(player, "Hành trang không đủ chỗ trống!");
            return;
        }

        if (player.inventory.gold < COST_NANG_CAP_DO_TL) {
            Service.getInstance().sendThongBao(player, "Con cần 12.5 tỷ vàng để nâng cấp!");
            return;
        }
        player.inventory.subGold(COST_NANG_CAP_DO_TL);
        byte itemGender = (dtl.template.type != 4) ? dtl.template.gender : player.gender;

        if (Util.isTrue(100, 100)) {
            sendEffectSuccessCombine(player);
            ItemService.gI().OpenDHD2(player, itemGender, dtl.template.type);
        } else {
            sendEffectFailCombine(player);
        }

        for (Item item : items) {
            InventoryService.gI().subQuantityItemsBag(player, item, 1);
        }

        InventoryService.gI().sendItemBags(player);
        Service.getInstance().sendMoney(player);
        reOpenItemCombine(player);
    }

    private void nangCapBongTai(Player player) {
        if (player.combineNew.itemsCombine.size() == 2) {
            int gold = player.combineNew.goldCombine;
            if (player.inventory.gold < gold) {
                Service.getInstance().sendThongBao(player, "Không đủ vàng để thực hiện");
                return;
            }
            int gem = player.combineNew.gemCombine;
            if (player.inventory.gem < gem) {
                Service.getInstance().sendThongBao(player, "Không đủ ngọc để thực hiện");
                return;
            }
            Item bongtai = null;
            Item manhvobt = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (checkBongTaiNangCap(item)) {
                    bongtai = item;
                } else if (item.template.id == 933) {
                    manhvobt = item;
                }
            }
            if (bongtai != null && manhvobt != null) {
                int level = 0;
                for (ItemOption io : bongtai.itemOptions) {
                    if (io.optionTemplate.id == 72) {
                        level = io.param;
                        break;
                    }
                }
                if (level < 2) {
                    int lvbt = lvbt(bongtai);
                    int soluongMVBT = 0;

                    for (ItemOption io : manhvobt.itemOptions) {
                        if (io.optionTemplate.id == 31) {
                            soluongMVBT = io.param;
                            break;
                        }
                    }
                    int countmvbt = getcountmvbtnangbt(lvbt);
                    if (countmvbt > soluongMVBT) {
                        Service.getInstance().sendThongBao(player, "Không đủ Mảnh vỡ bông tai");
                        return;
                    }
                    player.inventory.gold -= gold;
                    player.inventory.gem -= gem;
                    if (Util.isTrue(player.combineNew.ratioCombine, 100)) {
                        bongtai.template = ItemService.gI().getTemplate(getidbtsaukhilencap(lvbt));
                        bongtai.itemOptions.clear();
                        bongtai.itemOptions.add(new ItemOption(72, lvbt + 1));
                        sendEffectSuccessCombine(player);
                        InventoryService.gI().subQuantityItemsBag(player, manhvobt, countmvbt);
                    } else {
                        sendEffectFailCombine(player);
                        InventoryService.gI().subQuantityItemsBag(player, manhvobt, 99);
                    }
                    InventoryService.gI().sendItemBags(player);
                    Service.getInstance().sendMoney(player);
                    reOpenItemCombine(player);
                }
            }
        }
    }

    // START _ SÁCH TUYỆT KỸ
    private void giamDinhSach(Player player) {
        if (player.combineNew.itemsCombine.size() == 2) {

            Item sachTuyetKy = null;
            Item buaGiamDinh = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (issachTuyetKy(item)) {
                    sachTuyetKy = item;
                } else if (item.template.id == 1283) {
                    buaGiamDinh = item;
                }
            }
            if (sachTuyetKy != null && buaGiamDinh != null) {
                Item sachTuyetKy_2 = ItemService.gI().createNewItem((short) sachTuyetKy.template.id);
                if (checkHaveOption(sachTuyetKy, 0, 229)) {
                    int tyle = new Random().nextInt(10);
                    if (tyle >= 0 && tyle <= 33) {
                        sachTuyetKy_2.itemOptions.add(new ItemOption(50, new Random().nextInt(5, 10)));
                    } else if (tyle > 33 && tyle <= 66) {
                        sachTuyetKy_2.itemOptions.add(new ItemOption(77, new Random().nextInt(10, 15)));
                    } else {
                        sachTuyetKy_2.itemOptions.add(new ItemOption(103, new Random().nextInt(10, 15)));
                    }
                    for (int i = 1; i < sachTuyetKy.itemOptions.size(); i++) {
                        sachTuyetKy_2.itemOptions.add(new ItemOption(sachTuyetKy.itemOptions.get(i).optionTemplate.id,
                                sachTuyetKy.itemOptions.get(i).param));
                    }
                    sendEffectSuccessCombine(player);
                    InventoryService.gI().addItemBag(player, sachTuyetKy_2, 1);
                    InventoryService.gI().subQuantityItemsBag(player, sachTuyetKy, 1);
                    InventoryService.gI().subQuantityItemsBag(player, buaGiamDinh, 1);
                    InventoryService.gI().sendItemBags(player);
                    reOpenItemCombine(player);
                } else {
                    Service.getInstance().sendThongBao(player, "Còn cái nịt mà giám");
                    return;
                }
            }
        }
    }

    private void nangCapSachTuyetKy(Player player) {
        if (player.combineNew.itemsCombine.size() == 2) {

            Item sachTuyetKy = null;
            Item kimBamGiay = null;

            for (Item item : player.combineNew.itemsCombine) {
                if (issachTuyetKy(item)) {
                    sachTuyetKy = item;
                } else if (item.template.id == 1282) {
                    kimBamGiay = item;
                }
            }
            Item sachTuyetKy_2 = ItemService.gI().createNewItem((short) ((short) sachTuyetKy.template.id + 1));
            if (sachTuyetKy != null && kimBamGiay != null) {
                if (kimBamGiay.quantity < 10) {
                    Service.getInstance().sendThongBao(player, "Không đủ Kìm bấm giấy mà đòi nâng cấp");
                    return;
                }
                if (checkHaveOption(sachTuyetKy, 0, 229)) {
                    Service.getInstance().sendThongBao(player, "Chưa giám định mà đòi nâng cấp");
                    return;
                }
                for (int i = 0; i < sachTuyetKy.itemOptions.size(); i++) {
                    sachTuyetKy_2.itemOptions.add(new ItemOption(sachTuyetKy.itemOptions.get(i).optionTemplate.id,
                            sachTuyetKy.itemOptions.get(i).param));
                }
                sendEffectSuccessCombine(player);
                InventoryService.gI().addItemBag(player, sachTuyetKy_2, 1);
                InventoryService.gI().subQuantityItemsBag(player, sachTuyetKy, 1);
                InventoryService.gI().subQuantityItemsBag(player, kimBamGiay, 10);
                InventoryService.gI().sendItemBags(player);
                reOpenItemCombine(player);

            }
        }
    }

    private void phucHoiSach(Player player) {
        if (player.combineNew.itemsCombine.size() == 1) {
            Item cuonSachCu = InventoryService.gI().findItemBagByTemp(player, (short) 1284);
            int goldPhanra = 10_000_000;
            Item sachTuyetKy = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (issachTuyetKy(item)) {
                    sachTuyetKy = item;
                }
            }
            if (sachTuyetKy != null) {
                int doBen = 0;
                ItemOption optionLevel = null;
                for (ItemOption io : sachTuyetKy.itemOptions) {
                    if (io.optionTemplate.id == 231) {
                        doBen = io.param;
                        optionLevel = io;
                        break;
                    }
                }
                if (cuonSachCu == null) {
                    Service.getInstance().sendThongBaoOK(player, "Cần sách tuyệt kỹ và 10 cuốn sách cũ");
                    return;
                }
                if (cuonSachCu.quantity < 10) {
                    Service.getInstance().sendThongBaoOK(player, "Cần sách tuyệt kỹ và 10 cuốn sách cũ");
                    return;
                }
                if (player.inventory.gold < goldPhanra) {
                    Service.getInstance().sendThongBao(player, "Không có tiền mà đòi phục hồi à");
                    return;
                }
                if (doBen != 1000) {
                    for (int i = 0; i < sachTuyetKy.itemOptions.size(); i++) {
                        if (sachTuyetKy.itemOptions.get(i).optionTemplate.id == 231) {
                            sachTuyetKy.itemOptions.get(i).param = 1000;
                            break;
                        }
                    }
                    player.inventory.gold -= 10_000_000;
                    InventoryService.gI().subQuantityItemsBag(player, cuonSachCu, 10);
                    InventoryService.gI().sendItemBags(player);
                    Service.getInstance().sendMoney(player);
                    sendEffectSuccessCombine(player);
                    reOpenItemCombine(player);
                } else {
                    Service.getInstance().sendThongBao(player, "Còn dùng được phục hồi ăn cứt à");
                    return;
                }
            }
        }
    }

    private void phanRaSach(Player player) {
        if (player.combineNew.itemsCombine.size() == 1) {
            Item cuonSachCu = ItemService.gI().createNewItem((short) 1284, 5);
            int goldPhanra = 10_000_000;
            Item sachTuyetKy = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (issachTuyetKy(item)) {
                    sachTuyetKy = item;
                }
            }
            if (sachTuyetKy != null) {
                int luotTay = 0;
                ItemOption optionLevel = null;
                for (ItemOption io : sachTuyetKy.itemOptions) {
                    if (io.optionTemplate.id == 230) {
                        luotTay = io.param;
                        optionLevel = io;
                        break;
                    }
                }
                if (player.inventory.gold < goldPhanra) {
                    Service.getInstance().sendThongBao(player, "Không có tiền mà đòi phân rã à");
                    return;
                }
                if (luotTay == 0) {

                    player.inventory.gold -= goldPhanra;
                    InventoryService.gI().subQuantityItemsBag(player, sachTuyetKy, 1);
                    InventoryService.gI().addItemBag(player, cuonSachCu, 999);
                    InventoryService.gI().sendItemBags(player);
                    Service.getInstance().sendMoney(player);
                    sendEffectSuccessCombine(player);
                    reOpenItemCombine(player);

                } else {
                    Service.getInstance().sendThongBao(player, "Còn dùng được phân rã ăn cứt à");
                    return;
                }
            }
        }
    }

    private void taySach(Player player) {
        if (player.combineNew.itemsCombine.size() == 1) {
            Item sachTuyetKy = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (issachTuyetKy(item)) {
                    sachTuyetKy = item;
                }
            }
            if (sachTuyetKy != null) {
                int luotTay = 0;
                ItemOption optionLevel = null;
                for (ItemOption io : sachTuyetKy.itemOptions) {
                    if (io.optionTemplate.id == 230) {
                        luotTay = io.param;
                        optionLevel = io;
                        break;
                    }
                }
                if (luotTay == 0) {
                    Service.getInstance().sendThongBao(player, "Còn cái nịt mà tẩy");
                    return;
                }
                Item sachTuyetKy_2 = ItemService.gI().createNewItem((short) sachTuyetKy.template.id);
                if (checkHaveOption(sachTuyetKy, 0, 229)) {
                    Service.getInstance().sendThongBao(player, "Còn cái nịt mà tẩy");
                    return;
                }
                int tyle = new Random().nextInt(10);
                for (int i = 1; i < sachTuyetKy.itemOptions.size(); i++) {
                    if (sachTuyetKy.itemOptions.get(i).optionTemplate.id == 230) {
                        sachTuyetKy.itemOptions.get(i).param -= 1;
                    }
                }
                sachTuyetKy_2.itemOptions.add(new ItemOption(229, 0));
                for (int i = 1; i < sachTuyetKy.itemOptions.size(); i++) {
                    sachTuyetKy_2.itemOptions.add(new ItemOption(sachTuyetKy.itemOptions.get(i).optionTemplate.id,
                            sachTuyetKy.itemOptions.get(i).param));
                }
                sendEffectSuccessCombine(player);
                InventoryService.gI().addItemBag(player, sachTuyetKy_2, 1);
                InventoryService.gI().subQuantityItemsBag(player, sachTuyetKy, 1);
                InventoryService.gI().sendItemBags(player);
                reOpenItemCombine(player);
            }
        }
    }

    // END _ SÁCH TUYỆT KỸ
    private short getidbtsaukhilencap(int lvbtcu) {
        switch (lvbtcu) {
            case 1:
                return 921;
            case 2:
                return 1128;
        }
        return 0;
    }

    private void moChiSoBongTai(Player player) {
        if (player.combineNew.itemsCombine.size() == 3) {
            int gold = player.combineNew.goldCombine;
            int gem = player.combineNew.gemCombine;
            if (player.inventory.gem < gem) {
                Service.getInstance().sendThongBao(player, "Không đủ ngọc để thực hiện");
                return;
            }
            Item bongTai = null;
            Item manhHon = null;
            Item daXanhLam = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (item.template.id == 921) {
                    bongTai = item;
                } else if (item.template.id == 934) {
                    manhHon = item;
                } else if (item.template.id == 935) {
                    daXanhLam = item;
                }
            }
            if (bongTai != null && daXanhLam != null && manhHon.quantity >= 99) {
                player.inventory.gem -= gem;
                InventoryService.gI().subQuantityItemsBag(player, manhHon, 99);
                InventoryService.gI().subQuantityItemsBag(player, daXanhLam, 1);
                if (Util.isTrue(player.combineNew.ratioCombine, 100)) {

                    bongTai.itemOptions.clear();

                    int rdUp = Util.nextInt(0, 7);
                    switch (rdUp) {
                        case 0:
                            bongTai.itemOptions.add(new ItemOption(50, Util.nextInt(5, 25)));
                            break;
                        case 1:
                            bongTai.itemOptions.add(new ItemOption(77, Util.nextInt(5, 25)));
                            break;
                        case 2:
                            bongTai.itemOptions.add(new ItemOption(103, Util.nextInt(5, 25)));
                            break;
                        case 3:
                            bongTai.itemOptions.add(new ItemOption(108, Util.nextInt(5, 25)));
                            break;
                        case 4:
                            bongTai.itemOptions.add(new ItemOption(94, Util.nextInt(5, 15)));
                            break;
                        case 5:
                            bongTai.itemOptions.add(new ItemOption(14, Util.nextInt(5, 15)));
                            break;
                        case 6:
                            bongTai.itemOptions.add(new ItemOption(80, Util.nextInt(5, 25)));
                            break;
                        case 7:
                            bongTai.itemOptions.add(new ItemOption(81, Util.nextInt(5, 25)));
                            break;
                    }
                    bongTai.itemOptions.add(new ItemOption(38, 0));
                    bongTai.itemOptions.add(new ItemOption(72, 2));
                    sendEffectSuccessCombine(player);
                } else {
                    sendEffectFailCombine(player);
                }
                InventoryService.gI().sendItemBags(player);
                Service.getInstance().sendMoney(player);
                reOpenItemCombine(player);
            }
        }
    }

    private void openCreateItemAngel(Player player) {
        if (player.combineNew.itemsCombine.size() != 3) {
            Service.getInstance().sendThongBao(player, "Thiếu đồ");
            return;
        }
        if (player.inventory.gold < COST) {
            Service.getInstance().sendThongBao(player, "Ảo ít thôi con...");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) < 1) {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            return;
        }
        Item itemTL = player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isCongThuc())
                .findFirst().get();
        Item itemHDs = player.combineNew.itemsCombine.stream()
                .filter(item -> item.isNotNullItem() && item.template.id == 1311).findFirst().get();
        Item itemManh = player.combineNew.itemsCombine.stream()
                .filter(item -> item.isNotNullItem() && item.isManhTS() && item.quantity >= 999).findFirst().get();

        player.inventory.gold -= COST;
        sendEffectSuccessCombine(player);
        short[][] itemIds = { { 1048, 1051, 1054, 1057, 1060 }, { 1049, 1052, 1055, 1058, 1061 },
                { 1050, 1053, 1056, 1059, 1062 } }; // thứ tự td - 0,nm - 1, xd - 2

        Item itemTS = ItemService.gI().DoThienSu(
                itemIds[itemTL.template.gender > 2 ? player.gender : itemTL.template.gender][itemManh.typeIdManh()],
                itemTL.template.gender);
        InventoryService.gI().addItemBag(player, itemTS, 1);
        InventoryService.gI().subQuantityItemsBag(player, itemTL, 99);
        InventoryService.gI().subQuantityItemsBag(player, itemManh, 999);
        InventoryService.gI().subQuantityItemsBag(player, itemHDs, 99);
        InventoryService.gI().sendItemBags(player);
        Service.getInstance().sendMoney(player);
        Service.getInstance().sendThongBao(player, "Bạn đã nhận được " + itemTS.template.name);
        player.combineNew.itemsCombine.clear();
        reOpenItemCombine(player);
    }

    private void nangCapDoThienSu(Player player) {
        if (player.combineNew.itemsCombine.size() > 1) {
            int ratioLuckyStone = 0, ratioRecipe = 0, ratioUpgradeStone = 0;
            List<Item> list = new ArrayList<>();
            Item angelClothes = null;
            Item craftingRecipe = null;
            for (Item item : player.combineNew.itemsCombine) {
                int id = item.template.id;
                if (item.isNotNullItem()) {
                    if (isAngelClothes(id)) {
                        angelClothes = item;
                    } else if (isLuckyStone(id)) {
                        ratioLuckyStone += getRatioLuckyStone(id);
                        list.add(item);
                    } else if (isUpgradeStone(id)) {
                        ratioUpgradeStone += getRatioUpgradeStone(id);
                        list.add(item);
                    } else if (isCraftingRecipe(id)) {
                        ratioRecipe += getRatioCraftingRecipe(id);
                        craftingRecipe = item;
                        list.add(item);
                    }
                }
            }
            boolean canUpgrade = true;
            for (ItemOption io : angelClothes.itemOptions) {
                int optId = io.optionTemplate.id;
                if (optId == 41) {
                    canUpgrade = false;
                }
            }
            if (canUpgrade) {
                if (angelClothes != null && craftingRecipe != null) {
                    int ratioTotal = (20 + ratioUpgradeStone + ratioRecipe);
                    int ratio = ratioTotal > 75 ? ratio = 75 : ratioTotal;
                    if (player.inventory.gold >= COST_DAP_DO_KICH_HOAT) {
                        if (Util.isTrue(ratio, 150)) {
                            int num = 0;
                            if (Util.isTrue(ratioLuckyStone, 150)) {
                                num = 15;
                            } else if (Util.isTrue(5, 100)) {
                                num = Util.nextInt(10, 15);
                            } else if (Util.isTrue(20, 100)) {
                                num = Util.nextInt(1, 10);
                            }
                            RandomCollection<Integer> rd = new RandomCollection<>();
                            rd.add(50, 1);
                            rd.add(25, 2);
                            rd.add(10, 3);
                            rd.add(5, 4);
                            int color = rd.next();
                            for (ItemOption io : angelClothes.itemOptions) {
                                int optId = io.optionTemplate.id;
                                switch (optId) {
                                    case 47: // giáp
                                    case 6: // hp
                                    case 26: // hp/30s
                                    case 22: // hp k
                                    case 0: // sức đánh
                                    case 7: // ki
                                    case 28: // ki/30s
                                    case 23: // ki k
                                    case 14: // crit
                                        io.param += ((long) io.param * num / 100);
                                        break;
                                }
                            }
                            angelClothes.itemOptions.add(new ItemOption(41, color));
                            for (int i = 0; i < color; i++) {
                                angelClothes.itemOptions
                                        .add(new ItemOption(Util.nextInt(201, 212), Util.nextInt(1, 10)));
                            }
                            sendEffectSuccessCombine(player);
                            Service.getInstance().sendThongBao(player, "Chúc mừng bạn đã nâng cấp thành công");
                        } else {
                            sendEffectFailCombine(player);
                            Service.getInstance().sendThongBao(player, "Chúc bạn đen nốt lần sau");
                        }
                        for (Item it : list) {
                            InventoryService.gI().subQuantityItemsBag(player, it, 1);
                        }
                        player.inventory.subGold(COST_DAP_DO_KICH_HOAT);
                        InventoryService.gI().sendItemBags(player);
                        Service.getInstance().sendMoney(player);
                        reOpenItemCombine(player);
                    }
                }
            }
        }
    }

    private void chuyenHoaTrangBiVang(Player player) {
        if (player.combineNew.itemsCombine.size() == 2) {

            Item trangBiGoc = player.combineNew.itemsCombine.get(0);
            Item trangBiCanChuyenHoa = player.combineNew.itemsCombine.get(1);

            Item trangBiCanChuyenHoa_2 = ItemService.gI()
                    .createNewItem(player.combineNew.itemsCombine.get(1).template.id);
            int goldChuyenHoa = 2_000_000_000;

            int levelTrangBi = 0;
            int soLanRotCap = 0;
            int chiSO1_trangBiCanChuyenHoa = 0;

            for (ItemOption io : trangBiGoc.itemOptions) {
                if (io.optionTemplate.id == 72) {
                    levelTrangBi = io.param;
                } else if (io.optionTemplate.id == 232) {
                    soLanRotCap += io.param;
                }
            }

            // START Tính chỉ số nhân với số cấp và trừ với số lần rớt cấp //
            int chisogoc = trangBiCanChuyenHoa.itemOptions.get(0).param;

            chisogoc += chisogoc * (levelTrangBi * 0.1);

            chisogoc -= chisogoc * (soLanRotCap * 0.1);
            // END Tính chỉ số nhân với số cấp và trừ với số lần rớt cấp //

            boolean trangBi_daNangCap_daPhaLeHoa = false;
            if (player.inventory.gold >= goldChuyenHoa) {
                if (!isTrangBiGoc(trangBiGoc)) {
                    Service.getInstance().sendThongBaoOK(player,
                            "Trang bị phải từ bậc lưỡng long, Jean hoặc Zelot trở lên");
                    return;
                } else if (levelTrangBi < 4) {
                    Service.getInstance().sendThongBaoOK(player, "Trang bị gốc có cấp từ [+4]");
                    return;
                } else if (!isTrangBiChuyenHoa(trangBiCanChuyenHoa)) {
                    Service.getInstance().sendThongBaoOK(player,
                            "Trang bị phải từ bậc lưỡng long, Jean hoặc Zelot trở lên");
                    return;
                } else if (trangBi_daNangCap_daPhaLeHoa) {
                    Service.getInstance().sendThongBaoOK(player,
                            "Trang bị nhập thể phải chưa nâng cấp và pha lê hóa trang bị");
                    return;
                } else if (!isCheckTrungTypevsGender(trangBiGoc, trangBiCanChuyenHoa)) {
                    Service.getInstance().sendThongBaoOK(player,
                            "Trang bị gốc và Trang bị nhập thể phải cùng loại và hành tinh");
                    return;
                } else {

                    trangBiCanChuyenHoa.itemOptions.get(0).param = chisogoc;

                    for (int i = 1; i < trangBiGoc.itemOptions.size(); i++) {
                        trangBiCanChuyenHoa.itemOptions.add(new ItemOption(
                                trangBiGoc.itemOptions.get(i).optionTemplate.id, trangBiGoc.itemOptions.get(i).param));
                    }

                    for (int i = 0; i < trangBiCanChuyenHoa.itemOptions.size(); i++) {
                        trangBiCanChuyenHoa_2.itemOptions
                                .add(new ItemOption(trangBiCanChuyenHoa.itemOptions.get(i).optionTemplate.id,
                                        trangBiCanChuyenHoa.itemOptions.get(i).param));
                    }

                    player.inventory.gold -= 2_000_000_000;
                    Service.getInstance().sendMoney(player);
                    InventoryService.gI().addItemBag(player, trangBiCanChuyenHoa_2, 1);
                    InventoryService.gI().subQuantityItemsBag(player, trangBiGoc, 1);
                    InventoryService.gI().subQuantityItemsBag(player, trangBiCanChuyenHoa, 1);
                    InventoryService.gI().sendItemBags(player);
                    reOpenItemCombine(player);
                    sendEffectSuccessCombine(player);
                }
            } else {
                Service.getInstance().sendThongBao(player, "Không có tiền mà đòi chuyển hóa cái gì?");
                return;
            }
        }
    }

    private void chuyenHoaTrangBiNgoc(Player player) {
        if (player.combineNew.itemsCombine.size() == 2) {

            Item trangBiGoc = player.combineNew.itemsCombine.get(0);
            Item trangBiCanChuyenHoa = player.combineNew.itemsCombine.get(1);

            Item trangBiCanChuyenHoa_2 = ItemService.gI()
                    .createNewItem(player.combineNew.itemsCombine.get(1).template.id);

            int ngocChuyenHoa = 5000;

            int levelTrangBi = 0;
            int soLanRotCap = 0;
            int chiSO1_trangBiCanChuyenHoa = 0;

            for (ItemOption io : trangBiGoc.itemOptions) {
                if (io.optionTemplate.id == 72) {
                    levelTrangBi = io.param - 1;
                } else if (io.optionTemplate.id == 232) {
                    soLanRotCap += io.param;
                }
            }

            // START Tính chỉ số nhân với số cấp và trừ với số lần rớt cấp //
            int chisogoc = trangBiCanChuyenHoa.itemOptions.get(0).param;

            chisogoc += chisogoc * (levelTrangBi * 0.1);

            chisogoc -= chisogoc * (soLanRotCap * 0.1);
            // END Tính chỉ số nhân với số cấp và trừ với số lần rớt cấp //

            boolean trangBi_daNangCap_daPhaLeHoa = false;
            if (player.inventory.gem >= ngocChuyenHoa) {
                if (!isTrangBiGoc(trangBiGoc)) {
                    Service.getInstance().sendThongBaoOK(player,
                            "Trang bị phải từ bậc lưỡng long, Jean hoặc Zelot trở lên");
                    return;
                } else if (levelTrangBi < 4) {
                    Service.getInstance().sendThongBaoOK(player, "Trang bị gốc có cấp từ [+4]");
                    return;
                } else if (!isTrangBiChuyenHoa(trangBiCanChuyenHoa)) {
                    Service.getInstance().sendThongBaoOK(player,
                            "Trang bị phải từ bậc lưỡng long, Jean hoặc Zelot trở lên");
                    return;
                } else if (trangBi_daNangCap_daPhaLeHoa) {
                    Service.getInstance().sendThongBaoOK(player,
                            "Trang bị nhập thể phải chưa nâng cấp và pha lê hóa trang bị");
                    return;
                } else if (!isCheckTrungTypevsGender(trangBiGoc, trangBiCanChuyenHoa)) {
                    Service.getInstance().sendThongBaoOK(player,
                            "Trang bị gốc và Trang bị nhập thể phải cùng loại và hành tinh");
                    return;
                } else {

                    trangBiCanChuyenHoa.itemOptions.get(0).param = chisogoc;

                    for (int i = 1; i < trangBiGoc.itemOptions.size(); i++) {
                        trangBiCanChuyenHoa.itemOptions.add(new ItemOption(
                                trangBiGoc.itemOptions.get(i).optionTemplate.id, trangBiGoc.itemOptions.get(i).param));
                    }

                    for (int i = 0; i < trangBiCanChuyenHoa.itemOptions.size(); i++) {
                        trangBiCanChuyenHoa_2.itemOptions
                                .add(new ItemOption(trangBiCanChuyenHoa.itemOptions.get(i).optionTemplate.id,
                                        trangBiCanChuyenHoa.itemOptions.get(i).param));
                    }

                    player.inventory.gem -= ngocChuyenHoa;
                    Service.getInstance().sendMoney(player);
                    InventoryService.gI().addItemBag(player, trangBiCanChuyenHoa_2, 1);
                    InventoryService.gI().subQuantityItemsBag(player, trangBiGoc, 1);
                    InventoryService.gI().subQuantityItemsBag(player, trangBiCanChuyenHoa, 1);
                    InventoryService.gI().sendItemBags(player);
                    reOpenItemCombine(player);
                    sendEffectSuccessCombine(player);
                }
            } else {
                Service.getInstance().sendThongBao(player, "Không có tiền mà đòi chuyển hóa cái gì?");
                return;
            }
        }
    }

    private void giaHanCaiTrang(Player player) {
        if (player.combineNew.itemsCombine.size() == 2) {
            Item caitrang = null, vegiahan = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (item.isNotNullItem()) {
                    if (item.template.type == 5) {
                        caitrang = item;
                    } else if (item.template.id == 2022) {
                        vegiahan = item;
                    }
                }
            }
            if (caitrang != null && vegiahan != null) {
                if (InventoryService.gI().getCountEmptyBag(player) > 0
                        && player.inventory.gold >= COST_GIA_HAN_CAI_TRANG) {
                    ItemOption expiredDate = null;
                    boolean canBeExtend = true;
                    for (ItemOption io : caitrang.itemOptions) {
                        if (io.optionTemplate.id == 93) {
                            expiredDate = io;
                        }
                        if (io.optionTemplate.id == 199) {
                            canBeExtend = false;
                        }
                    }
                    if (canBeExtend) {
                        if (expiredDate.param > 0) {
                            player.inventory.subGold(COST_GIA_HAN_CAI_TRANG);
                            sendEffectSuccessCombine(player);
                            expiredDate.param++;
                            InventoryService.gI().subQuantityItemsBag(player, vegiahan, 1);
                            InventoryService.gI().sendItemBags(player);
                            Service.getInstance().sendMoney(player);
                            reOpenItemCombine(player);
                        }
                    }
                }
            }
        }
    }

    private void dapDoKichHoatCaoCap(Player player) {
        if (player.combineNew.itemsCombine.size() == 2) {
            List<Item> items = player.combineNew.itemsCombine;
            boolean hasAo = false, hasQuan = false, hasGang = false, hasGiay = false, hasRada = false;
            for (Item it : items) {
                if (it == null || !isDestroyClothes(it.template.id))
                    continue;
                switch (it.template.type) {
                    case 0:
                        hasAo = true;
                        break;
                    case 1:
                        hasQuan = true;
                        break;
                    case 2:
                        hasGang = true;
                        break;
                    case 3:
                        hasGiay = true;
                        break;
                    case 4:
                        hasRada = true;
                        break;
                }
            }
            if (hasAo && hasQuan && hasGang && hasGiay && hasRada) {
                if (InventoryService.gI().getCountEmptyBag(player) > 0
                        && player.inventory.gold >= COST_DAP_DO_KICH_HOAT) {

                    player.inventory.subGold(COST_DAP_DO_KICH_HOAT);

                    int soluongitem = ConstItem.LIST_ITEM_CLOTHES[0][0].length;
                    int id;

                    if (Util.isTrue(98, 100)) {
                        if (Util.isTrue(60, 100)) {
                            id = (Util.nextInt(0, soluongitem - 7));
                        } else {
                            id = (Util.nextInt(5, soluongitem - 2));
                        }
                    } else {
                        id = soluongitem - 1;
                    }

                    sendEffectSuccessCombine(player);

                    Item firstItem = items.get(0);
                    int gender = firstItem.template.gender;
                    if (gender == 3)
                        gender = player.gender;

                    Item item = ItemService.gI().createNewItem(
                            (short) ConstItem.LIST_ITEM_CLOTHES[gender][firstItem.template.type][id]);

                    RewardService.gI().initBaseOptionClothes(item.template.id, item.template.type, item.itemOptions);
                    RewardService.gI().initActivationOption(gender, item.template.type, item.itemOptions);

                    InventoryService.gI().addItemBag(player, item, 0);

                    for (Item it : items) {
                        InventoryService.gI().subQuantityItemsBag(player, it, 1);
                    }

                    InventoryService.gI().sendItemBags(player);
                    Service.getInstance().sendMoney(player);
                    reOpenItemCombine(player);
                } else {
                    Service.getInstance().sendThongBao(player, "Hành trang đã đầy hoặc không đủ vàng");
                }
            } else {
                Service.getInstance().sendThongBao(player, "Cần đủ 2 món huỷ diệt: Áo, Quần, Găng, Giày, Rada");
            }
        }
    }

    private void doiManhKichHoat(Player player) {
        if (player.combineNew.itemsCombine.size() == 2 || player.combineNew.itemsCombine.size() == 3) {
            Item nr1s = null, doThan = null, buaBaoVe = null;
            for (Item it : player.combineNew.itemsCombine) {
                if (it.template.id == 14) {
                    nr1s = it;
                } else if (it.template.id == 2010) {
                    buaBaoVe = it;
                } else if (it.template.id >= 555 && it.template.id <= 567) {
                    doThan = it;
                }
            }
            if (nr1s != null && doThan != null) {
                if (InventoryService.gI().getCountEmptyBag(player) > 0
                        && player.inventory.gold >= COST_DOI_MANH_KICH_HOAT) {
                    player.inventory.gold -= COST_DOI_MANH_KICH_HOAT;
                    int tiLe = buaBaoVe != null ? 100 : 50;
                    if (Util.isTrue(tiLe, 100)) {
                        sendEffectSuccessCombine(player);
                        Item item = ItemService.gI().createNewItem((short) 2009);
                        item.itemOptions.add(new ItemOption(30, 0));
                        InventoryService.gI().addItemBag(player, item, 0);
                    } else {
                        sendEffectFailCombine(player);
                    }
                    InventoryService.gI().subQuantityItemsBag(player, nr1s, 1);
                    InventoryService.gI().subQuantityItemsBag(player, doThan, 1);
                    if (buaBaoVe != null) {
                        InventoryService.gI().subQuantityItemsBag(player, buaBaoVe, 1);
                    }
                    InventoryService.gI().sendItemBags(player);
                    Service.getInstance().sendMoney(player);
                    reOpenItemCombine(player);
                }
            } else {
                this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        "Hãy chọn 1 trang bị thần linh và 1 viên ngọc rồng 1 sao", "Đóng");
            }
        }
    }

    private void doihopquahalloween(Player player) {
        if (player.combineNew.itemsCombine.size() == 5) {
            Item bikiep = player.combineNew.itemsCombine.stream()
                    .filter(item -> item.isNotNullItem() && item.template.id == 590).findFirst().orElse(null);
            Item vpsk1 = player.combineNew.itemsCombine.stream()
                    .filter(item -> item.isNotNullItem() && item.template.id == 2138).findFirst().orElse(null);
            Item vpsk2 = player.combineNew.itemsCombine.stream()
                    .filter(item -> item.isNotNullItem() && item.template.id == 900).findFirst().orElse(null);
            Item vpsk3 = player.combineNew.itemsCombine.stream()
                    .filter(item -> item.isNotNullItem() && item.template.id == 903).findFirst().orElse(null);
            Item vpsk4 = player.combineNew.itemsCombine.stream()
                    .filter(item -> item.isNotNullItem() && item.template.id == 899).findFirst().orElse(null);
            // Item dhd = player.combineNew.itemsCombine.stream().filter(item ->
            // item.isNotNullItem() && item.template.id >= 650 && item.template.id <=
            // 662).findFirst().orElse(null);

            if (bikiep == null || bikiep.quantity < 999) {
                Service.gI().sendThongBao(player, "Ngươi còn thiếu bí kiếp");
                return;
            }

            if (vpsk1 == null || vpsk1.quantity < 99) {
                Service.gI().sendThongBao(player, "Ngươi còn thiếu bí ngô");
                return;
            }

            if (vpsk2 == null || vpsk2.quantity < 99) {
                Service.gI().sendThongBao(player, "Ngươi còn thiếu súp bí hắc ám");
                return;
            }

            if (vpsk3 == null || vpsk3.quantity < 10) {
                Service.gI().sendThongBao(player, "Ngươi còn thiếu Hamburger sâu");
                return;
            }
            if (vpsk4 == null || vpsk4.quantity < 30) {
                Service.gI().sendThongBao(player, "Ngươi còn thiếu kẹo 1 mắt");
                return;
            }
            // if (dhd == null || dhd.quantity < 1) {
            // Service.gI().sendThongBao(player, "Ngươi còn thiếu một món đồ hủy diệt");
            // return;
            // }

            if (Util.isTrue(100, 100)) {
                Item item = ItemService.gI().createNewItem((short) 2139);
                item.itemOptions.add(new ItemOption(30, 1));

                InventoryService.gI().addItemBag(player, item, 0);
                sendEffectSuccessCombine(player);

            } else {
                sendEffectFailCombine(player);
            }

            InventoryService.gI().subQuantityItemsBag(player, bikiep, 999);
            InventoryService.gI().subQuantityItemsBag(player, vpsk1, 99);
            InventoryService.gI().subQuantityItemsBag(player, vpsk2, 99);
            InventoryService.gI().subQuantityItemsBag(player, vpsk3, 10);
            InventoryService.gI().subQuantityItemsBag(player, vpsk4, 30);
            // InventoryService.gI().subQuantityItemsBag(player, dhd, 1);

            InventoryService.gI().sendItemBags(player);

            reOpenItemCombine(player);

        } else {
            Service.gI().sendThongBao(player, "Thiếu vật phảm");
        }
    }

    private void dapDoKichHoat(Player player) {
        if (player.combineNew.itemsCombine.size() == 1 || player.combineNew.itemsCombine.size() == 2) {
            Item dhd = null, dtl = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (item.isNotNullItem()) {
                    if (item.template.id >= 650 && item.template.id <= 662) {
                        dhd = item;
                    } else if (item.template.id >= 555 && item.template.id <= 567) {
                        dtl = item;
                    }
                }
            }
            if (dhd != null) {
                if (InventoryService.gI().getCountEmptyBag(player) > 0
                        && player.inventory.gold >= COST_DAP_DO_KICH_HOAT) {
                    player.inventory.gold -= COST_DAP_DO_KICH_HOAT;
                    int tiLe = dtl != null ? 100 : 40;
                    if (Util.isTrue(tiLe, 100)) {
                        sendEffectSuccessCombine(player);
                        Item item = ItemService.gI()
                                .createNewItem((short) getTempIdItemC0(dhd.template.gender, dhd.template.type));
                        RewardService.gI().initBaseOptionClothes(item.template.id, item.template.type,
                                item.itemOptions);
                        RewardService.gI().initActivationOption(
                                item.template.gender < 3 ? item.template.gender : player.gender, item.template.type,
                                item.itemOptions);
                        InventoryService.gI().addItemBag(player, item, 0);
                    } else {
                        sendEffectFailCombine(player);
                    }
                    InventoryService.gI().subQuantityItemsBag(player, dhd, 1);
                    if (dtl != null) {
                        InventoryService.gI().subQuantityItemsBag(player, dtl, 1);
                    }
                    InventoryService.gI().sendItemBags(player);
                    Service.getInstance().sendMoney(player);
                    reOpenItemCombine(player);
                }
            }
        }
    }

    private void doiVeHuyDiet(Player player) {
        if (player.combineNew.itemsCombine.size() == 1) {
            Item item = player.combineNew.itemsCombine.get(0);
            if (item.isNotNullItem() && item.template.id >= 555 && item.template.id <= 567) {
                if (InventoryService.gI().getCountEmptyBag(player) > 0
                        && player.inventory.gold >= COST_DOI_VE_DOI_DO_HUY_DIET) {
                    player.inventory.gold -= COST_DOI_VE_DOI_DO_HUY_DIET;
                    Item ticket = ItemService.gI().createNewItem((short) (2001 + item.template.type));
                    ticket.itemOptions.add(new ItemOption(30, 0));
                    InventoryService.gI().subQuantityItemsBag(player, item, 1);
                    InventoryService.gI().addItemBag(player, ticket, 99);
                    sendEffectOpenItem(player, item.template.iconID, ticket.template.iconID);

                    InventoryService.gI().sendItemBags(player);
                    Service.getInstance().sendMoney(player);
                    reOpenItemCombine(player);
                }
            }
        }
    }

    private void epSaoTrangBi(Player player) {
        if (player.combineNew.itemsCombine.size() == 2) {
            int gem = player.combineNew.gemCombine;
            if (player.inventory.gem < gem) {
                Service.getInstance().sendThongBao(player, "Không đủ ngọc để thực hiện");
                return;
            }
            Item trangBi = null;
            Item daPhaLe = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (isTrangBiPhaLeHoa(item)) {
                    trangBi = item;
                } else if (isDaPhaLe(item)) {
                    daPhaLe = item;
                }
            }
            int star = 0; // sao pha lê đã ép
            int starEmpty = 0; // lỗ sao pha lê
            if (trangBi != null && daPhaLe != null) {
                ItemOption optionStar = null;
                for (ItemOption io : trangBi.itemOptions) {
                    if (io.optionTemplate.id == 102) {
                        star = io.param;
                        optionStar = io;
                    } else if (io.optionTemplate.id == 107) {
                        starEmpty = io.param;
                    }
                }
                if (star < starEmpty) {
                    player.inventory.subGem(gem);
                    int optionId = getOptionDaPhaLe(daPhaLe);
                    int param = getParamDaPhaLe(daPhaLe);
                    ItemOption option = null;
                    for (ItemOption io : trangBi.itemOptions) {
                        if (io.optionTemplate.id == optionId) {
                            option = io;
                            break;
                        }
                    }
                    if (option != null) {
                        option.param += param;
                    } else {
                        trangBi.itemOptions.add(new ItemOption(optionId, param));
                    }
                    if (optionStar != null) {
                        optionStar.param++;
                    } else {
                        trangBi.itemOptions.add(new ItemOption(102, 1));
                    }

                    InventoryService.gI().subQuantityItemsBag(player, daPhaLe, 1);
                    sendEffectSuccessCombine(player);
                }
                InventoryService.gI().sendItemBags(player);
                Service.getInstance().sendMoney(player);
                reOpenItemCombine(player);
            }
        }
    }

    private void phaLeHoaTrangBi(Player player) {
        if (!player.combineNew.itemsCombine.isEmpty()) {
            int gold = player.combineNew.goldCombine;
            int gem = player.combineNew.gemCombine;
            if (player.inventory.gold < gold) {
                Service.getInstance().sendThongBao(player,
                        "Bạn không đủ vàng, còn thiếu " + Util.numberToMoney(gold) + " vàng nữa");
                return;
            } else if (player.inventory.gem < gem) {
                Service.getInstance().sendThongBao(player,
                        "Bạn không đủ vàng, còn thiếu " + Util.mumberToLouis(gem) + " ngọc nữa");
                return;
            }
            Item item = player.combineNew.itemsCombine.get(0);
            if (isTrangBiPhaLeHoa(item)) {
                int star = 0;
                ItemOption optionStar = null;
                for (ItemOption io : item.itemOptions) {
                    if (io.optionTemplate.id == 107) {
                        star = io.param;
                        optionStar = io;
                        break;
                    }
                }
                if (star < MAX_STAR_ITEM) {
                    player.inventory.gold -= gold;
                    player.inventory.subGem(gem);
                    if (Util.isTrue(player.combineNew.ratioCombine, 400)) {
                        if (optionStar == null) {
                            item.itemOptions.add(new ItemOption(107, 1));
                        } else {
                            optionStar.param++;
                        }
                        sendEffectSuccessCombine(player);
                        if (optionStar != null && optionStar.param >= 7) {
                            ServerNotify.gI().notify("Chúc mừng " + player.name + " vừa pha lê hóa " + "thành công "
                                    + item.template.name + " lên " + optionStar.param + " sao pha lê");
                            ServerLog.logCombine(player.name, item.template.name, optionStar.param);
                        }
                    } else {
                        sendEffectFailCombine(player);
                    }
                }
                InventoryService.gI().sendItemBags(player);
                Service.getInstance().sendMoney(player);
                reOpenItemCombine(player);
            }
        }
    }

    private void phaLeHoaTrangBix10(Player player) {
        if (!player.combineNew.itemsCombine.isEmpty()) {
            long gold = player.combineNew.goldCombine;
            int gem = player.combineNew.gemCombine;
            if (player.inventory.gold < (gold * 100)) {
                Service.getInstance().sendThongBao(player,
                        "Bạn không đủ vàng, còn thiếu " + Util.numberToMoney(gold * 10) + " vàng nữa");
                return;
            } else if (player.inventory.gem < (gem * 100)) {
                Service.getInstance().sendThongBao(player,
                        "Bạn không đủ vàng, còn thiếu " + Util.mumberToLouis(gem * 10) + " ngọc nữa");
                return;
            }
            Item item = player.combineNew.itemsCombine.get(0);
            if (isTrangBiPhaLeHoa(item)) {
                int star = 0;
                ItemOption optionStar = null;
                for (ItemOption io : item.itemOptions) {
                    if (io.optionTemplate.id == 107) {
                        star = io.param;
                        optionStar = io;
                        break;
                    }
                }
                if (star < MAX_STAR_ITEM) {
                    for (int i = 0; i < 10; i++) {
                        player.inventory.gold -= gold;
                        player.inventory.subGem(gem);
                        if (Util.isTrue(player.combineNew.ratioCombine, 400)) {
                            if (optionStar == null) {
                                item.itemOptions.add(new ItemOption(107, 1));
                            } else {
                                optionStar.param++;
                            }
                            sendEffectSuccessCombine(player);
                            if (optionStar != null && optionStar.param >= 7) {
                                ServerNotify.gI().notify("Chúc mừng " + player.name + " vừa pha lê hóa " + "thành công "
                                        + item.template.name + " lên " + optionStar.param + " sao pha lê");
                                ServerLog.logCombine(player.name, item.template.name, optionStar.param);
                            }
                            Service.getInstance().sendThongBao(player, "Bạn đã đập " + (i + 1) + " lần");
                            break;
                        } else {
                            if (i == 9) {
                                sendEffectFailCombine(player);
                                Service.getInstance().sendThongBao(player, "Bạn đã đập " + (i + 1) + " lần");
                            }
                        }
                    }
                }
                InventoryService.gI().sendItemBags(player);
                Service.getInstance().sendMoney(player);
                reOpenItemCombine(player);
            }
        }
    }

    private void phaLeHoaTrangBix100(Player player) {
        if (!player.combineNew.itemsCombine.isEmpty()) {
            long gold = player.combineNew.goldCombine;
            int gem = player.combineNew.gemCombine;
            if (player.inventory.gold < (gold * 100)) {
                Service.getInstance().sendThongBao(player,
                        "Bạn không đủ vàng, còn thiếu " + Util.numberToMoney(gold * 100) + " vàng nữa");
                return;
            } else if (player.inventory.gem < (gem * 100)) {
                Service.getInstance().sendThongBao(player,
                        "Bạn không đủ vàng, còn thiếu " + Util.mumberToLouis(gem * 100) + " ngọc nữa");
                return;
            }
            Item item = player.combineNew.itemsCombine.get(0);
            if (isTrangBiPhaLeHoa(item)) {
                int star = 0;
                ItemOption optionStar = null;
                for (ItemOption io : item.itemOptions) {
                    if (io.optionTemplate.id == 107) {
                        star = io.param;
                        optionStar = io;
                        break;
                    }
                }
                if (star < MAX_STAR_ITEM) {
                    for (int i = 0; i < 100; i++) {
                        player.inventory.gold -= gold;
                        player.inventory.subGem(gem);
                        if (Util.isTrue(player.combineNew.ratioCombine, 400)) {
                            if (optionStar == null) {
                                item.itemOptions.add(new ItemOption(107, 1));
                            } else {
                                optionStar.param++;
                            }
                            sendEffectSuccessCombine(player);
                            if (optionStar != null && optionStar.param >= 7) {
                                ServerNotify.gI().notify("Chúc mừng " + player.name + " vừa pha lê hóa " + "thành công "
                                        + item.template.name + " lên " + optionStar.param + " sao pha lê");
                                ServerLog.logCombine(player.name, item.template.name, optionStar.param);
                            }
                            Service.getInstance().sendThongBao(player, "Bạn đã đập " + (i + 1) + " lần");
                            break;
                        } else {
                            if (i == 99) {
                                sendEffectFailCombine(player);
                                Service.getInstance().sendThongBao(player, "Bạn đã đập " + (i + 1) + " lần");
                            }
                        }
                    }
                }
                InventoryService.gI().sendItemBags(player);
                Service.getInstance().sendMoney(player);
                reOpenItemCombine(player);
            }
        }
    }

    private void nhapNgocRong(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
            if (!player.combineNew.itemsCombine.isEmpty()) {
                Item item = player.combineNew.itemsCombine.get(0);
                if (item != null && item.isNotNullItem()) {
                    if ((item.template.id > 14 && item.template.id <= 20) && item.quantity >= 7) {
                        Item nr = ItemService.gI().createNewItem((short) (item.template.id - 1));
                        InventoryService.gI().addItemBag(player, nr, 0);
                        InventoryService.gI().subQuantityItemsBag(player, item, 7);
                        InventoryService.gI().sendItemBags(player);
                        reOpenItemCombine(player);
                        sendEffectCombineDB(player, item.template.iconID);
                        return;
                    }
                    if (player.inventory.gold >= 500000000) {
                        if (item.template.id == 14 && item.quantity >= 7) {
                            Item nr = ItemService.gI().createNewItem((short) (1015));
                            InventoryService.gI().addItemBag(player, nr, 0);
                            sendEffectCombineDB(player, (short) 9650);
                        } else if (item.template.id == 926 && item.quantity >= 7) {
                            Item nr = ItemService.gI().createNewItem((short) (925));
                            nr.itemOptions.add(new ItemOption(93, 70));
                            InventoryService.gI().addItemBag(player, nr, 0);
                            sendEffectCombineDB(player, item.template.iconID);
                        }
                        InventoryService.gI().subQuantityItemsBag(player, item, 7);
                        player.inventory.gold -= 500000000;
                        Service.getInstance().sendMoney(player);
                        InventoryService.gI().sendItemBags(player);
                        reOpenItemCombine(player);
                    } else {
                        Service.getInstance().sendThongBao(player, "Không đủ vàng, còn thiếu "
                                + Util.numberToMoney(500000000 - player.inventory.gold) + " vàng");
                    }
                }
            }
        }
    }

    private void nangCapSKH(Player player) {
        if (player.combineNew.itemsCombine.size() == 3) {

            Item SKH = null, doThan = null, daNangCap = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (item.isNotNullItem()) {
                    if (isHuyDiet(item)) {
                        doThan = item;
                    }
                    if (item.template.id == 1314) {
                        daNangCap = item;
                    }
                    for (int i = 0; i < item.itemOptions.size(); i++) {
                        for (int option = 127; option <= 135; option++) {
                            if (checkHaveOption(item, i, option)) {
                                SKH = item;
                                break;
                            }
                        }
                    }
                }
            }

            int levelTrangBi = 0;

            int paramSKH = 0;

            for (ItemOption io : SKH.itemOptions) {
                for (int option = 127; option <= 135; option++) {
                    if (io.optionTemplate.id == option) {
                        levelTrangBi = io.param;
                    }
                }
                for (int option = 136; option <= 144; option++) {
                    if (io.optionTemplate.id == option) {
                        paramSKH = io.param;
                    }
                }
            }
            // Start Combie//
            if (SKH != null && doThan != null && daNangCap != null) {
                if (player.inventory.gold >= player.combineNew.goldCombine) {
                    if (Util.isTrue(50, 100)) {
                        for (int i = 1; i < SKH.itemOptions.size(); i++) {
                            if (SKH.itemOptions.get(i).optionTemplate.id >= 127
                                    && SKH.itemOptions.get(i).optionTemplate.id <= 135) {
                                SKH.itemOptions.get(i).param += 1;
                            }
                            if (SKH.itemOptions.get(i).optionTemplate.id >= 136
                                    && SKH.itemOptions.get(i).optionTemplate.id <= 144) {
                                SKH.itemOptions.get(i).param += 10;
                            }
                        }
                        InventoryService.gI().subQuantityItemsBag(player, doThan, 1);
                        InventoryService.gI().subQuantityItemsBag(player, daNangCap, 1);
                        InventoryService.gI().sendItemBags(player);
                        player.inventory.gold -= 2000000000L;
                        Service.getInstance().sendMoney(player);
                        sendEffectSuccessCombine(player);
                        return;
                    } else {
                        InventoryService.gI().subQuantityItemsBag(player, doThan, 1);
                        InventoryService.gI().subQuantityItemsBag(player, daNangCap, 1);
                        InventoryService.gI().sendItemBags(player);
                        player.inventory.gold -= 2000000000L;
                        Service.getInstance().sendMoney(player);
                        sendEffectFailCombine(player);
                        return;
                    }
                } else {
                    Service.getInstance().sendThongBao(player, "Không đủ vàng");
                }
            } else {
                Service.getInstance().sendThongBao(player, "Không đủ vật phẩm");
            }
            // End Combie//
        }
    }

    private void nangCapVatPham(Player player) {
        if (player.combineNew.itemsCombine.size() >= 2 && player.combineNew.itemsCombine.size() < 4) {
            if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.template.type < 5)
                    .count() != 1) {
                return;
            }
            if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.template.type == 14)
                    .count() != 1) {
                return;
            }
            if (player.combineNew.itemsCombine.size() == 3 && player.combineNew.itemsCombine.stream()
                    .filter(item -> item.isNotNullItem() && item.template.id == 987).count() != 1) {
                return;// admin
            }
            Item itemDo = null;
            Item itemDNC = null;
            Item itemDBV = null;
            for (int j = 0; j < player.combineNew.itemsCombine.size(); j++) {
                if (player.combineNew.itemsCombine.get(j).isNotNullItem()) {
                    if (player.combineNew.itemsCombine.size() == 3
                            && player.combineNew.itemsCombine.get(j).template.id == 987) {
                        itemDBV = player.combineNew.itemsCombine.get(j);
                        continue;
                    }
                    if (player.combineNew.itemsCombine.get(j).template.type < 5) {
                        itemDo = player.combineNew.itemsCombine.get(j);
                    } else {
                        itemDNC = player.combineNew.itemsCombine.get(j);
                    }
                }
            }
            if (isCoupleItemNangCapCheck(itemDo, itemDNC)) {
                int countDaNangCap = player.combineNew.countDaNangCap;
                int gold = player.combineNew.goldCombine;
                short countDaBaoVe = player.combineNew.countDaBaoVe;
                if (player.inventory.gold < gold) {
                    Service.getInstance().sendThongBao(player, "Không đủ vàng để thực hiện");
                    return;
                }

                if (itemDNC.quantity < countDaNangCap) {
                    return;
                }
                if (player.combineNew.itemsCombine.size() == 3) {
                    if (Objects.isNull(itemDBV)) {
                        return;
                    }
                    if (itemDBV.quantity < countDaBaoVe) {
                        return;
                    }
                }

                int level = 0;
                ItemOption optionLevel = null;
                for (ItemOption io : itemDo.itemOptions) {
                    if (io.optionTemplate.id == 72) {
                        level = io.param;
                        optionLevel = io;
                        break;
                    }
                }
                if (level < MAX_LEVEL_ITEM) {
                    player.inventory.gold -= gold;
                    ItemOption option = null;
                    ItemOption option2 = null;
                    for (ItemOption io : itemDo.itemOptions) {
                        if (io.optionTemplate.id == 47
                                || io.optionTemplate.id == 6
                                || io.optionTemplate.id == 0
                                || io.optionTemplate.id == 7
                                || io.optionTemplate.id == 14
                                || io.optionTemplate.id == 22
                                || io.optionTemplate.id == 23) {
                            option = io;
                        } else if (io.optionTemplate.id == 27
                                || io.optionTemplate.id == 28) {
                            option2 = io;
                        }
                    }
                    if (Util.isTrue(player.combineNew.ratioCombine, 100)) {
                        if (option.optionTemplate.id == 14) {
                            option.param += 1;
                        } else {
                            option.param += (option.param * 10 / 100);
                        }
                        if (option2 != null) {
                            option2.param += (option2.param * 10 / 100);
                        }
                        if (optionLevel == null) {
                            itemDo.itemOptions.add(new ItemOption(72, 1));
                        } else {
                            optionLevel.param++;
                        }
                        if (optionLevel != null && optionLevel.param >= 5) {
                            ServerNotify.gI().notify("Chúc mừng " + player.name + " vừa nâng cấp "
                                    + "thành công " + itemDo.template.name + " lên +" + optionLevel.param);
                        }
                        sendEffectSuccessCombine(player);
                    } else {
                        if ((level == 2 || level == 4 || level == 6) && (player.combineNew.itemsCombine.size() != 3)) {
                            if (option.optionTemplate.id == 14) {
                                option.param -= 1;
                            } else {
                                option.param -= (option.param * 15 / 100);
                            }
                            if (option2 != null) {
                                option2.param -= (option2.param * 15 / 100);
                            }
                            optionLevel.param--;
                        }
                        sendEffectFailCombine(player);
                    }
                    if (player.combineNew.itemsCombine.size() == 3) {
                        InventoryService.gI().subQuantityItemsBag(player, itemDBV, countDaBaoVe);
                    }
                    InventoryService.gI().subQuantityItemsBag(player, itemDNC, player.combineNew.countDaNangCap);
                    InventoryService.gI().sendItemBags(player);
                    Service.getInstance().sendMoney(player);
                    reOpenItemCombine(player);
                }
            }
        }
    }

    // --------------------------------------------------------------------------
    private boolean isCoupleItemNangCapCheck(Item trangBi, Item daNangCap) {
        if (trangBi != null && daNangCap != null) {
            if (trangBi.template.type == 0 && daNangCap.template.id == 223) {
                return true;
            } else if (trangBi.template.type == 1 && daNangCap.template.id == 222) {
                return true;
            } else if (trangBi.template.type == 2 && daNangCap.template.id == 224) {
                return true;
            } else if (trangBi.template.type == 3 && daNangCap.template.id == 221) {
                return true;
            } else if (trangBi.template.type == 4 && daNangCap.template.id == 220) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Hiệu ứng mở item
     *
     * @param player
     */
    public void sendEffectOpenItem(Player player, short icon1, short icon2) {
        Message msg;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(OPEN_ITEM);
            msg.writer().writeShort(icon1);
            msg.writer().writeShort(icon2);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    /**
     * Hiệu ứng đập đồ thành công
     *
     * @param player
     */
    public void sendEffectSuccessCombine(Player player) {
        Message msg;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(COMBINE_SUCCESS);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void sendeffcombine7(Player player, short iconIdWhenSucces, int idNpc) {
        Message msg;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(8);
            msg.writer().writeShort(iconIdWhenSucces);
            msg.writer().writeShort(idNpc);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    private void sendEffectCombine(Player player) {
        Message msg;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(8);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Hiệu ứng đập đồ thất bại
     *
     * @param player
     */
    private void sendEffectFailCombine(Player player) {
        Message msg;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(COMBINE_FAIL);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    /**
     * Gửi lại danh sách đồ trong tab combine
     *
     * @param player
     */
    public void reOpenItemCombine(Player player) {
        Message msg;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(REOPEN_TAB_COMBINE);
            msg.writer().writeByte(player.combineNew.itemsCombine.size());
            for (Item it : player.combineNew.itemsCombine) {
                for (int j = 0; j < player.inventory.itemsBag.size(); j++) {
                    if (it == player.inventory.itemsBag.get(j)) {
                        msg.writer().writeByte(j);
                    }
                }
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    /**
     * Hiệu ứng ghép ngọc rồng
     *
     * @param player
     * @param icon
     */
    private void sendEffectCombineDB(Player player, short icon) {
        Message msg;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(COMBINE_DRAGON_BALL);
            msg.writer().writeShort(icon);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    // --------------------------------------------------------------------------Ratio,
    private int getCountDaBaoVe(int level) {
        return level + 1;
    }

    // cost combine
    private int getRatioDaMayMan(int id) {
        switch (id) {
            case 1079:
                return 10;
            case 1080:
                return 20;
            case 1081:
                return 30;
            case 1082:
                return 40;
            case 1083:
                return 50;
        }
        return 0;
    }

    private int getRatioDaNangCap(int id) {
        switch (id) {
            case 1074:
                return 10;
            case 1075:
                return 20;
            case 1076:
                return 30;
            case 1077:
                return 40;
            case 1078:
                return 50;
        }
        return 0;
    }

    private int getGoldPhaLeHoa(int star) {
        switch (star) {
            case 0:
                return 5000000;
            case 1:
                return 10000000;
            case 2:
                return 20000000;
            case 3:
                return 40000000;
            case 4:
                return 60000000;
            case 5:
                return 90000000;
            case 6:
                return 120000000;
            case 7:
                return 200000000;
        }
        return 0;
    }

    private float getRatioPhaLeHoa(int star) {
        switch (star) {
            case 0:
                return 79f;
            case 1:
                return 49f;
            case 2:
                return 19f;
            case 3:
                return 4f;
            case 4:
                return 1f;
            case 5:
                return 1f;
            case 6:
                return 1f;
            case 7:
                return 0.5f;
        }
        return 0;
    }
    /// so sao pha le

    private int getGemPhaLeHoa(int star) {
        switch (star) {
            case 0:
                return 10;
            case 1:
                return 20;
            case 2:
                return 30;
            case 3:
                return 40;
            case 4:
                return 50;
            case 5:
                return 60;
            case 6:
                return 70;
            case 7:
                return 80;
            case 8:
                return 90;
        }
        return 0;
    }

    private int getGemEpSao(int star) {
        switch (star) {
            case 0:
                return 1;
            case 1:
                return 2;
            case 2:
                return 5;
            case 3:
                return 10;
            case 4:
                return 25;
            case 5:
                return 50;
            case 6:
                return 100;
        }
        return 0;
    }

    private int getTileNangCapDo(int level) {
        switch (level) {
            case 0:
                return 80;
            case 1:
                return 50;
            case 2:
                return 20;
            case 3:
                return 10;
            case 4:
                return 7;
            case 5:
                return 3;
            case 6:
                return 1;
        }
        return 0;
    }

    private int getCountDaNangCapDo(int level) {
        switch (level) {
            case 0:
                return 3;
            case 1:
                return 7;
            case 2:
                return 11;
            case 3:
                return 17;
            case 4:
                return 23;
            case 5:
                return 35;
            case 6:
                return 50;
        }
        return 0;
    }

    private int lvbt(Item bongtai) {
        switch (bongtai.template.id) {
            case 454:
                return 1;
            case 921:
                return 2;
        }
        return 0;

    }

    private int getGoldNangCapDo(int level) {
        switch (level) {
            case 0:
                return 10000;
            case 1:
                return 70000;
            case 2:
                return 300000;
            case 3:
                return 1500000;
            case 4:
                return 7000000;
            case 5:
                return 23000000;
            case 6:
                return 100000000;
        }
        return 0;
    }

    // --------------------------------------------------------------------------check
    public boolean isAngelClothes(int id) {
        if (id >= 1048 && id <= 1062) {
            return true;
        }
        return false;
    }

    public boolean isDestroyClothes(int id) {
        if (id >= 650 && id <= 662) {
            return true;
        }
        return false;
    }

    private String getTypeTrangBi(int type) {
        switch (type) {
            case 0:
                return "Áo";
            case 1:
                return "Quần";
            case 2:
                return "Găng";
            case 3:
                return "Giày";
            case 4:
                return "Nhẫn";
        }
        return "";
    }

    public boolean isManhTrangBi(Item it) {
        switch (it.template.id) {
            case 1066:
            case 1067:
            case 1068:
            case 1069:
            case 1070:
                return true;
        }
        return false;
    }

    public boolean isCraftingRecipe(int id) {
        switch (id) {
            case 1071:
            case 1072:
            case 1073:
            case 1084:
            case 1085:
            case 1086:
                return true;
        }
        return false;
    }

    public int getRatioCraftingRecipe(int id) {
        switch (id) {
            case 1071:
                return 0;
            case 1072:
                return 0;
            case 1073:
                return 0;
            case 1084:
                return 10;
            case 1085:
                return 10;
            case 1086:
                return 10;
        }
        return 0;
    }

    public boolean isUpgradeStone(int id) {
        switch (id) {
            case 1074:
            case 1075:
            case 1076:
            case 1077:
            case 1078:
                return true;
        }
        return false;
    }

    public int getRatioUpgradeStone(int id) {
        switch (id) {
            case 1074:
                return 10;
            case 1075:
                return 20;
            case 1076:
                return 30;
            case 1077:
                return 40;
            case 1078:
                return 50;
        }
        return 0;
    }

    public boolean isLuckyStone(int id) {
        switch (id) {
            case 1079:
            case 1080:
            case 1081:
            case 1082:
            case 1083:
                return true;
        }
        return false;
    }

    private int getGoldnangbt(int lvbt) {
        return GOLD_BONG_TAI2;
    }

    private int getgemdnangbt(int lvbt) {
        return GEM_BONG_TAI2;
    }

    private int getcountmvbtnangbt(int lvbt) {
        return 9999;
    }

    private boolean checkBongTaiNangCap(Item item) {
        if (item.template.id == 454) {
            return true;
        }
        return false;
    }

    public int getRatioLuckyStone(int id) {
        switch (id) {
            case 1079:
                return 10;
            case 1080:
                return 20;
            case 1081:
                return 30;
            case 1082:
                return 40;
            case 1083:
                return 50;
        }
        return 0;
    }

    private boolean isCoupleItemNangCap(Item item1, Item item2) {
        Item trangBi = null;
        Item daNangCap = null;
        if (item1 != null && item1.isNotNullItem()) {
            if (item1.template.type < 5) {
                trangBi = item1;
            } else if (item1.template.type == 14) {
                daNangCap = item1;
            }
        }
        if (item2 != null && item2.isNotNullItem()) {
            if (item2.template.type < 5) {
                trangBi = item2;
            } else if (item2.template.type == 14) {
                daNangCap = item2;
            }
        }
        if (trangBi != null && daNangCap != null) {
            if (trangBi.template.type == 0 && daNangCap.template.id == 223) {
                return true;
            } else if (trangBi.template.type == 1 && daNangCap.template.id == 222) {
                return true;
            } else if (trangBi.template.type == 2 && daNangCap.template.id == 224) {
                return true;
            } else if (trangBi.template.type == 3 && daNangCap.template.id == 221) {
                return true;
            } else if (trangBi.template.type == 4 && daNangCap.template.id == 220) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isDaPhaLe(Item item) {
        return item.template.type == 30 || (item.template.id >= 14 && item.template.id <= 20);
    }

    public boolean isDTL(Item item) {
        if (item.template == null) {
            return false;
        }
        if (item.template.id == 555 || item.template.id == 556 || item.template.id == 562 || item.template.id == 563
                || item.template.id == 561) {
            return true;
        }
        if (item.template.id == 559 || item.template.id == 560 || item.template.id == 566 || item.template.id == 567
                || item.template.id == 561) {
            return true;
        }
        if (item.template.id == 557 || item.template.id == 558 || item.template.id == 564 || item.template.id == 565
                || item.template.id == 561) {
            return true;
        }
        return false;
    }

    public boolean isHuyDiet(Item item) {
        if (item.template == null) {
            return false;
        }
        if (item.template.id >= 650 && item.template.id <= 662) {
            return true;
        }
        return false;
    }
    /// type để đục lỗ

    private boolean isTrangBiPhaLeHoa(Item item) {
        if (item != null && item.isNotNullItem()) {
            if (item.template.type < 5 || item.template.type == 32) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isTrangBiGoc(Item item) {
        if (item != null && item.isNotNullItem()) {
            if (isDoLuongLong(item) || isDoJean(item) || isDoZelot(item) || isDoThanXD(item) || isDoThanTD(item)
                    || isDoThanNM(item)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isTrangBiChuyenHoa(Item item) {
        if (item != null && item.isNotNullItem()) {
            if (isDoThanXD(item) || isDoThanTD(item) || isDoThanNM(item)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isCheckTrungTypevsGender(Item item, Item item2) {
        if (item != null && item.isNotNullItem() && item2 != null && item2.isNotNullItem()) {
            if (item.template.type == item2.template.type && item.template.gender == item2.template.gender) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isDoLuongLong(Item item) {
        if (item != null && item.isNotNullItem()) {
            if (item.template.id == 241 || item.template.id == 253 || item.template.id == 265 || item.template.id == 277
                    || item.template.id == 281) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isDoZelot(Item item) {
        if (item != null && item.isNotNullItem()) {
            if (item.template.id == 241 - 4 || item.template.id == 253 - 4 || item.template.id == 265 - 4
                    || item.template.id == 277 - 4 || item.template.id == 281) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isDoJean(Item item) {
        if (item != null && item.isNotNullItem()) {
            if (item.template.id == 241 - 8 || item.template.id == 253 - 8 || item.template.id == 265 - 8
                    || item.template.id == 277 - 8 || item.template.id == 281) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isDoThanXD(Item item) {
        if (item != null && item.isNotNullItem()) {
            if (item.template.id == 559 || item.template.id == 560 || item.template.id == 566 || item.template.id == 567
                    || item.template.id == 561) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isDoThanTD(Item item) {
        if (item != null && item.isNotNullItem()) {
            if (item.template.id == 559 - 4 || item.template.id == 560 - 4 || item.template.id == 566 - 4
                    || item.template.id == 567 - 4 || item.template.id == 561) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isDoThanNM(Item item) {
        if (item != null && item.isNotNullItem()) {
            if (item.template.id == 559 - 2 || item.template.id == 560 - 2 || item.template.id == 566 - 2
                    || item.template.id == 567 - 2 || item.template.id == 561) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean issachTuyetKy(Item item) {
        if (item != null && item.isNotNullItem()) {
            if (item.template.type == 35) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean checkHaveOption(Item item, int viTriOption, int idOption) {
        if (item != null && item.isNotNullItem()) {
            if (item.itemOptions.get(viTriOption).optionTemplate.id == idOption) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private int getParamDaPhaLe(Item daPhaLe) {
        if (daPhaLe.template.type == 30) {
            return daPhaLe.itemOptions.get(0).param;
        }
        switch (daPhaLe.template.id) {
            case 20:
                return 5; // +5%hp
            case 19:
                return 5; // +5%ki
            case 18:
                return 5; // +5%hp/30s
            case 17:
                return 5; // +5%ki/30s
            case 16:
                return 3; // +3%sđ
            case 15:
                return 2; // +2%giáp
            case 14:
                return 2; // +2%né đòn
            default:
                return -1;
        }
    }

    private int getOptionDaPhaLe(Item daPhaLe) {
        if (daPhaLe.template.type == 30) {
            return daPhaLe.itemOptions.get(0).optionTemplate.id;
        }
        switch (daPhaLe.template.id) {
            case 20:
                return 77;
            case 19:
                return 103;
            case 18:
                return 80;
            case 17:
                return 81;
            case 16:
                return 50;
            case 15:
                return 94;
            case 14:
                return 108;
            default:
                return -1;
        }
    }

    /**
     * Trả về id item c0
     *
     * @param gender
     * @param type
     * @return
     */
    private int getTempIdItemC0(int gender, int type) {
        if (type == 4) {
            return 12;
        }
        switch (gender) {
            case 0:
                switch (type) {
                    case 0:
                        return 0;
                    case 1:
                        return 6;
                    case 2:
                        return 21;
                    case 3:
                        return 27;
                }
                break;
            case 1:
                switch (type) {
                    case 0:
                        return 1;
                    case 1:
                        return 7;
                    case 2:
                        return 22;
                    case 3:
                        return 28;
                }
                break;
            case 2:
                switch (type) {
                    case 0:
                        return 2;
                    case 1:
                        return 8;
                    case 2:
                        return 23;
                    case 3:
                        return 29;
                }
                break;
        }
        return -1;
    }

    // Trả về tên đồ c0
    private String getNameItemC0(int gender, int type) {
        if (type == 4) {
            return "Rada cấp 1";
        }
        switch (gender) {
            case 0:
                switch (type) {
                    case 0:
                        return "Áo vải 3 lỗ";
                    case 1:
                        return "Quần vải đen";
                    case 2:
                        return "Găng thun đen";
                    case 3:
                        return "Giầy nhựa";
                }
                break;
            case 1:
                switch (type) {
                    case 0:
                        return "Áo sợi len";
                    case 1:
                        return "Quần sợi len";
                    case 2:
                        return "Găng sợi len";
                    case 3:
                        return "Giầy sợi len";
                }
                break;
            case 2:
                switch (type) {
                    case 0:
                        return "Áo vải thô";
                    case 1:
                        return "Quần vải thô";
                    case 2:
                        return "Găng vải thô";
                    case 3:
                        return "Giầy vải thô";
                }
                break;
        }
        return "";
    }

    // --------------------------------------------------------------------------Text
    // tab combine
    private String getTextTopTabCombine(int type) {
        switch (type) {
            case EP_SAO_TRANG_BI:
                return "Ta sẽ phù phép\ncho trang bị của ngươi\ntrở lên mạnh mẽ";
            case PHA_LE_HOA_TRANG_BI:
            case PHA_LE_HOA_TRANG_BI_X10:
                return "Ta sẽ phù phép\ncho trang bị của ngươi\ntrở thành trang bị pha lê";
            case NHAP_NGOC_RONG:
                return "Ta sẽ phù phép\ncho 7 viên Ngọc Rồng\nthành 1 viên Ngọc Rồng cấp cao";
            case CHE_TAO_DO_THIEN_SU:
                return "Whis Đã Ban phép cho ta";
            // START_ SÁCH TUYỆT KỸ //
            case GIAM_DINH_SACH:
                return "Ta sẽ phù phép\ngiám định sách đó cho ngươi";
            case TAY_SACH:
                return "Ta sẽ phù phép\ntẩy sách đó cho ngươi";
            case NANG_CAP_SACH_TUYET_KY:
                return "Ta sẽ phù phép\nnâng cấp Sách Tuyệt Kỹ cho ngươi";
            case PHUC_HOI_SACH:
                return "Ta sẽ phù phép\nphục hồi sách cho ngươi";
            case PHAN_RA_SACH:
                return "Ta sẽ phù phép\nphân rã sách cho ngươi";
            // END _ SÁCH TUYỆT KỸ //

            case NANG_CAP_VAT_PHAM:
                return "Ta sẽ phù phép\ncho trang bị của ngươi\ntrở lên mạnh mẽ";
            case NANG_CAP_SKH:
                return "Ta sẽ nâng cấp\ncho trang bị của ngươi\ntrở lên mạnh mẽ";
            case NANG_CAP_DO_TL:
                return "Ta sẽ nâng cấp\ncho trang bị của ngươi\ntrở lên mạnh mẽ";
            case DOI_VE_HUY_DIET:
                return "Ta sẽ đưa ngươi 1 vé đổi đồ\nhủy diệt, đổi lại ngươi phải đưa ta\n 1 món đồ thần linh tương ứng";
            case DAP_SET_KICH_HOAT:
                return "Ta sẽ giúp ngươi chuyển hóa\n1 món đồ hủy diệt\nthành 1 món đồ kích hoạt";
            case DOI_HOP_QUA_HALLOWEEN:
                return "Ta sẽ giúp ngươi đổi các vật phẩm sự kiện\nthành một phần quà vô cùng giá trị";
            // case DOI_MANH_KICH_HOAT:
            // return "Ta sẽ giúp ngươi biến hóa\nviên ngọc 1 sao và 1 món đồ\nthần linh
            // thành mảnh kích hoạt";
            case DAP_SET_KICH_HOAT_CAO_CAP:
                return "Ta sẽ giúp ngươi chuyển hóa\n2 món đồ hủy diệt (đủ 2 loại khác nhau)\nthành 1 món đồ kích hoạt cao cấp";
            case GIA_HAN_CAI_TRANG:
                return "Ta sẽ phù phép\n cho trang bị của mi\n thêm hạn sử dụng";
            case NANG_CAP_DO_THIEN_SU:
                return "Nâng cấp\n trang bị thiên sứ";
            case NANG_CAP_BONG_TAI:
                return "Ta sẽ phù phép\ncho bông tai Porata của ngươi\nthành cấp 2";
            case MO_CHI_SO_BONG_TAI:
                return "Ta sẽ phù phép\ncho bông tai Porata cấp 2 của ngươi\ncó 1 chỉ số ngẫu nhiên";
            case CHUYEN_HOA_BANG_NGOC:
            case CHUYEN_HOA_BANG_VANG:
                return "Lưu ý trang bị mới\nphải hơn trang bị gốc\n1 bậc";

            // START _ NEW PHA LÊ HÓA //
            case NANG_CAP_SAO_PHA_LE:
                return "Ta sẽ phù phép\nnâng cấp Sao Pha Lê\nthành cấp 2";
            case DANH_BONG_SAO_PHA_LE:
                return "Đánh bóng\nSao pha lê cấp 2";
            case CUONG_HOA_LO_SAO_PHA_LE:
                return "Cường hóa\nÔ Sao Pha lê";
            case TAO_DA_HEMATILE:
                return "Ta sẽ phù phép\n"
                        + "tạo đá Hematite";
            // END _ NEW PHA LÊ HÓA //
            default:
                return "";
        }
    }

    private String getTextInfoTabCombine(int type) {
        switch (type) {
            case EP_SAO_TRANG_BI:
                return "Chọn trang bị\n(Áo, quần, găng, giày hoặc rađa) có ô đặt sao pha lê\nChọn loại sao pha lê\n"
                        + "Sau đó chọn 'Nâng cấp'";
            case PHA_LE_HOA_TRANG_BI:
                return "Chọn trang bị\n(Áo, quần, găng, giày hoặc rađa)\nSau đó chọn 'Nâng cấp'";
            case PHA_LE_HOA_TRANG_BI_X10:
                return "Chọn trang bị\n(Áo, quần, găng, giày hoặc rađa)\nSau đó chọn 'Nâng cấp'\n Khi nâng cấp thành công hoặc đủ 5 lần thì sẽ dừng lại";
            case NHAP_NGOC_RONG:
                return "Vào hành trang\nChọn 7 viên ngọc cùng sao\nSau đó chọn 'Làm phép'";
            case CHE_TAO_DO_THIEN_SU:
                return "Vào hành trang\nChọn 99 Công thức theo Hành tinh\nkèm 99 đá thiên sứ\n và 999 mảnh thiên sứ\n "
                        + "sẽ cho ra đồ thiên sứ từ 0-15% chỉ số"
                        + "\n(Có tỉ lệ thêm dòng chỉ số ẩn)"
                        + "\nSau đó chọn 'Nâng Cấp'";
            // START_ SÁCH TUYỆT KỸ //
            case GIAM_DINH_SACH:
                return "Vào hành trang chọn\n1 sách cần giám định";
            case TAY_SACH:
                return "Vào hành trang chọn\n1 sách cần tẩy";
            case NANG_CAP_SACH_TUYET_KY:
                return "Vào hành trang chọn\nSách Tuyệt Kỹ 1 cần nâng cấp và 10 Kìm bấm giấy";
            case PHUC_HOI_SACH:
                return "Vào hành trang chọn\nCác Sách Tuyệt Kỹ cần phục hồi";
            case PHAN_RA_SACH:
                return "Vào hành trang chọn\n1 sách cần phân rã";
            // END _ SÁCH TUYỆT KỸ //
            case NANG_CAP_SKH:
                return "Vào hành trang chọn\n1 món kích hoạt, một món Hủy Diệt và\nmột đá nâng cấp SKH";
            case NANG_CAP_DO_TL:
                return "Vào hành trang\nChọn 3 trang bị thần linh\n(Áo,quần,găng,giày hoặc rada)\nđể nâng cấp\n"
                        + "Sau đó chọn 'Nâng cấp'";
            case NANG_CAP_VAT_PHAM:
                return "Vào hành trang\nChọn trang bị\n(Áo,quần,găng,giày hoặc rada)\nChọn loại đá để nâng cấp\n"
                        + "Sau đó chọn 'Nâng cấp'";
            case DOI_VE_HUY_DIET:
                return "Vào hành trang\nChọn món đồ thần linh tương ứng\n(Áo, quần, găng, giày hoặc nhẫn)\nSau đó chọn 'Đổi'";
            case DAP_SET_KICH_HOAT:
                return "Vào hành trang\nChọn món đồ hủy diệt tương ứng\n(Áo, quần, găng, giày hoặc nhẫn)\n(Có thể thêm 1 món đồ thần linh bất kỳ để tăng tỉ lệ)\nSau đó chọn 'Đập'";
            case DOI_HOP_QUA_HALLOWEEN:
                return "Số lượng cần đổi quà bao gồm\n999 bí kiếp 99 bí ngô 99 súp bí hắc ám 10 Hamburger sâu và 30 kẹo 1 mắt. \nSau đó chọn 'Đập'";

            // case DOI_MANH_KICH_HOAT:
            // return "Vào hành trang\nChọn món đồ thần linh tương ứng\n(Áo, quần, găng,
            // giày hoặc nhẫn)\nSau đó chọn 'Đổi'";
            case DAP_SET_KICH_HOAT_CAO_CAP:
                return "Vào hành trang\nChọn 2 món đồ hủy diệt khác nhau\n(Áo, quần, găng, giày hoặc nhẫn)\nSau đó chọn 'Đập'";
            case GIA_HAN_CAI_TRANG:
                return "Vào hành trang \n Chọn cải trang có hạn sử dụng \n Chọn thẻ gia hạn \n Sau đó chọn gia hạn";
            case NANG_CAP_DO_THIEN_SU:
                return "Cần 1 công thức\nTrang bị thiên sứ\nĐá nâng cấp (tùy chọn)\nĐá may mắn (tùy chọn)";
            case NANG_CAP_BONG_TAI:
                return "Vào hành trang\nChọn bông tai Porata\nChọn mảnh bông tai để nâng cấp, số lượng\n9999 cái\nSau đó chọn 'Nâng cấp'";
            case MO_CHI_SO_BONG_TAI:
                return "Vào hành trang\nChọn bông tai Porata\nChọn mảnh hồn bông tai số lượng 99 cái\nvà đá xanh lam để nâng cấp\nSau đó chọn 'Nâng cấp'";
            case CHUYEN_HOA_BANG_NGOC:
            case CHUYEN_HOA_BANG_VANG:
                return "Vào hành trang\nChọn trang bị gốc\n(Áo,quần,găng,giày hoặc rada)\ntừ cấp[+4] trở lên\nChọn tiếp trang bị mới\nchưa nâng cấp cần nhập thể\nsau đó chọn 'Nâng cấp'";
            // START _ NEW PHA LÊ HÓA //
            case NANG_CAP_SAO_PHA_LE:
                return "Vào hành trang\nChọn đá Hematite\n Chọn loại sao pha lê (cấp 1)\nSau đó chọn 'Nâng cấp'";
            case DANH_BONG_SAO_PHA_LE:
                return "Vào hành trang\nChọn loại sao pha lê cấp 2 có từ 2 viên trở\nlên\nChọn 1 loại đá mài\nSau đó chọn 'Đánh bóng'";
            case CUONG_HOA_LO_SAO_PHA_LE:
                return "Vào hành trang\n"
                        + "Chọn trang bị có Ô sao thứ 8 trở lên chưa\n"
                        + "cường hóa\n"
                        + "Chọn đá Hematite\n"
                        + "Chọn dùi đục\n"
                        + "Sau đó chọn 'Cường hóa'";
            case TAO_DA_HEMATILE:
                return "Vào hành trang\n"
                        + "Chọn 5 sao pha lê cấp 2 cùng màu\n"
                        + "Chọn 'Tạo đá Hematite'";
            // END _ NEW PHA LÊ HÓA //
            default:
                return "";
        }
    }

}
