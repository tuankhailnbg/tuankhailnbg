package nro.services.func;

import nro.consts.Cmd;
import nro.consts.ConstNpc;
import nro.consts.ConstPlayer;
import nro.models.item.Item;
import nro.models.item.ItemOption;
import nro.models.map.Zone;
import nro.models.player.Player;
import nro.server.io.Message;
import nro.services.*;
import nro.utils.Log;
import nro.utils.Util;
import nro.services.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author 💖 Nro Yanii 💖
 *
 *
 */
public class SummonDragon {

    public static final byte WISHED = 0;
    public static final byte TIME_UP = 1;

    public static final byte DRAGON_SHENRON = 0;
    public static final byte DRAGON_PORUNGA = 1;
    public static final byte DRAGON_BLACK_SHENRON = 2;
    public static final byte DRAGON_ICE_SHENRON = 3;

    public static final short NGOC_RONG_1_SAO = 14;
    public static final short NGOC_RONG_2_SAO = 15;
    public static final short NGOC_RONG_3_SAO = 16;
    public static final short NGOC_RONG_4_SAO = 17;
    public static final short NGOC_RONG_5_SAO = 18;
    public static final short NGOC_RONG_6_SAO = 19;
    public static final short NGOC_RONG_7_SAO = 20;

    public static final short[] NGOC_RONG_HALLOWEEN = {702, 703, 704, 705, 706, 707, 708};
    public static final short[] NGOC_RONG_DEN = {807, 808, 809, 810, 811, 812, 813};
    public static final short[] NGOC_RONG_BANG = {925, 926, 927, 928, 929, 930, 931};
    public static final short[] NGOC_RONG_NAMEK = {353, 354, 355, 356, 357, 358, 359};
    public static final short NGOC_RONG_SIEU_CAP = 1015;
    public static final String SUMMON_SHENRON_TUTORIAL = "Có 3 cách gọi rồng thần. Gọi từ ngọc 1 sao, gọi từ ngọc 2 sao, hoặc gọi từ ngọc 3 sao\n"
            + "Các ngọc 4 sao đến 7 sao không thể gọi rồng thần được\n"
            + "Để gọi rồng 1 sao cần ngọc từ 1 sao đến 7 sao\n"
            + "Để gọi rồng 2 sao cần ngọc từ 2 sao đến 7 sao\n"
            + "Để gọi rồng 3 sao cần ngọc từ 3 sao đến 7sao\n"
            + "Điều ước rồng 3 sao: Capsule 3 sao, hoặc 2 triệu sức mạnh, hoặc 200k vàng\n"
            + "Điều ước rồng 2 sao: Capsule 2 sao, hoặc 20 triệu sức mạnh, hoặc 2 triệu vàng\n"
            + "Điều ước rồng 1 sao: Capsule 1 sao, hoặc 200 triệu sức mạnh, hoặc 20 triệu vàng, hoặc đẹp trai, hoặc....\n"
            + "Ngọc rồng sẽ mất ngay khi gọi rồng dù bạn có ước hay không\n"
            + "Quá 5 phút nếu không ước rồng thần sẽ bay mất";
    public static final String SHENRON_SAY = "Ta sẽ ban cho người 1 điều ước, ngươi có 5 phút, hãy suy nghĩ thật kỹ trước khi quyết định";

    public static final String[] SHENRON_1_STAR_WISHES_1 = new String[]{"Giàu có\n+30 Tỏi\nVàng",
        "Găng tay\nđang mang\nlên 1 cấp", "Chí mạng\nGốc +2%",
        "Thay\nChiêu 2-3\nĐệ tử", "Điều ước\nkhác"};
    public static final String[] SHENRON_1_STAR_WISHES_2 = new String[]{"Đẹp trai\nnhất\nVũ trụ",
        "Giàu có\n+10K\nNgọc", "+200 Tr\nSức mạnh\nvà tiềm\nnăng",
        "Găng tay đệ\nđang mang\nlên 1 cấp",
        "Điều ước\nkhác"};
    public static final String[] SHENRON_2_STARS_WHISHES = new String[]{"Giàu có\n+2K\nNgọc",
        "+20 Tr\nSức mạnh\nvà tiềm năng", "Giàu có\n+200 Tr\nVàng"};
    public static final String[] SHENRON_3_STARS_WHISHES = new String[]{"Giàu có\n+200\nNgọc",
        "+2 Tr\nSức mạnh\nvà tiềm năng", "Giàu có\n+20 Tr\nVàng"};
    // --------------------------------------------------------------------------
    public static final String BLACK_SHENRON_SAY = "Ta sẽ ban cho người 1 điều ước, ngươi có 5 phút, hãy suy nghĩ thật kỹ trước khi quyết định"
            + "\n 1) Cải trang Gohan Siêu Nhân (Chỉ số ngẫu nhiên 10 Ngày) "
            + "\n 2) Cải trang Biden Siêu Nhân (Chỉ số ngẫu nhiên 10 Ngày) "
            + "\n 3) Cải trang Cô nương Siêu Nhân (Chỉ số ngẫu nhiên 10 Ngày) "
            + "\n 4) Pet Thỏ Ốm (Chỉ số ngẫu nhiên 10 Ngày) "
            + "\n 5) Pet Thỏ Mập (Chỉ số ngẫu nhiên 10 Ngày) ";

    public static final String[] BLACK_SHENRON_WISHES = new String[]{"Điều\nước 1", "Điều\nước 2", "Điều\nước 3",
        "Điều\nước 4", "Điều\nước 5"};
    // -----------------------------------------------------------------------------
    public static final String ICE_SHENRON_SAY = "Ta sẽ ban cho người 1 điều ước, ngươi có 5 phút, hãy suy nghĩ thật kỹ trước khi quyết định"
            + "\n 1) Cải trang chichi noel chỉ số SĐ: 20-35%, HP: 12%, KI: 25%, SĐCM: 10-35%, Kháng Lạnh."
            + "\n 2) Cải trang videl noel chỉ số SĐ: 25%, HP: 12%, KI: 30-50%, Cute hồi 2% Ki, Kháng lạnh."
            + "\n 3) Cải trang mabu noel chỉ số SĐ: 25%, HP: 1-20%. KI: 25%, HP+10%/30s, Kháng Lạnh.";

    public static final String[] ICE_SHENRON_WISHES = new String[]{"Điều\nước 1", "Điều\nước 2", "Điều\nước 3"};

    private static SummonDragon instance;
    private final Map pl_dragonStar;
    private long lastTimeShenronAppeared;
    private long lastTimeShenronWait;
    private final int timeResummonShenron = 0;
    // private final int timeResummonShenron = 0;
    private boolean isShenronAppear;
    public boolean isBlackShenronAppear;
    public boolean isIcecShenronAppear;
    private final int timeShenronWait = 300000;

    private final Thread update;
    private boolean active;

    public boolean isPlayerDisconnect;
    public Player playerSummonShenron;
    private int playerSummonShenronId;
    private Zone mapShenronAppear;
    private byte shenronStar;
    private int menuShenron;
    private byte select;

    private SummonDragon() {
        this.pl_dragonStar = new HashMap<>();
        this.update = new Thread(() -> {
            while (active) {
                try {
                    if (isShenronAppear) {
                        if (isPlayerDisconnect) {

                            List<Player> players = mapShenronAppear.getPlayers();
                            synchronized (players) {
                                for (Player plMap : players) {
                                    if (plMap.id == playerSummonShenronId) {
                                        playerSummonShenron = plMap;
                                        reSummonShenron(DRAGON_SHENRON);
                                        isPlayerDisconnect = false;
                                        break;
                                    }
                                }
                            }

                        }
                        if (Util.canDoWithTime(lastTimeShenronWait, timeShenronWait)) {
                            shenronLeave(playerSummonShenron, TIME_UP);
                        }
                    } else if (isBlackShenronAppear) {
                        if (isPlayerDisconnect) {

                            List<Player> players = mapShenronAppear.getPlayers();
                            synchronized (players) {
                                for (Player plMap : players) {
                                    if (plMap.id == playerSummonShenronId) {
                                        playerSummonShenron = plMap;
                                        reSummonShenron(DRAGON_BLACK_SHENRON);
                                        isPlayerDisconnect = false;
                                        break;
                                    }
                                }
                            }

                        }
                        if (Util.canDoWithTime(lastTimeShenronWait, timeShenronWait)) {
                            shenronLeave(playerSummonShenron, TIME_UP);
                        }
                    } else if (isIcecShenronAppear) {
                        if (isPlayerDisconnect) {

                            List<Player> players = mapShenronAppear.getPlayers();
                            synchronized (players) {
                                for (Player plMap : players) {
                                    if (plMap.id == playerSummonShenronId) {
                                        playerSummonShenron = plMap;
                                        reSummonShenron(DRAGON_ICE_SHENRON);
                                        isPlayerDisconnect = false;
                                        break;
                                    }
                                }
                            }

                        }
                        if (Util.canDoWithTime(lastTimeShenronWait, timeShenronWait)) {
                            shenronLeave(playerSummonShenron, TIME_UP);
                        }
                    }
                    Thread.sleep(1000);
                } catch (Exception e) {
                    Log.error(SummonDragon.class, e);
                }
            }
        });
        this.active();
    }

    private void active() {
        if (!active) {
            active = true;
            this.update.start();
        }
    }

    public static SummonDragon gI() {
        if (instance == null) {
            instance = new SummonDragon();
        }
        return instance;
    }

    public void openMenuSummonShenron(Player pl, byte dragonBallStar, byte Dragon) {
        switch (Dragon) {
            case DRAGON_SHENRON:
                this.pl_dragonStar.put(pl, dragonBallStar);
                NpcService.gI().createMenuConMeo(pl, ConstNpc.SUMMON_SHENRON, -1, "Bạn muốn gọi rồng thần ?",
                        "Hướng\ndẫn thêm\n(mới)", "Gọi\nRồng Thần\n" + dragonBallStar + " Sao");
                break;
            case DRAGON_BLACK_SHENRON:
                this.pl_dragonStar.put(pl, dragonBallStar);
                NpcService.gI().createMenuConMeo(pl, ConstNpc.SUMMON_BLACK_SHENRON, -1, "Bạn muốn gọi Rồng Siêu Cấp ?",
                        "Đồng ý", "Chê");
                break;
            case DRAGON_ICE_SHENRON:
                this.pl_dragonStar.put(pl, dragonBallStar);
                NpcService.gI().createMenuConMeo(pl, ConstNpc.SUMMON_ICE_SHENRON, -1, "Bạn muốn gọi Rồng Băng ?",
                        "Đồng ý", "Chê");
                break;
        }

    }

    public void summonShenron(Player pl) {
        if (pl.zone.map.mapId == 0 || pl.zone.map.mapId == 7 || pl.zone.map.mapId == 14) {
            if (checkShenronBall(pl, DRAGON_SHENRON)) {
                if (isShenronAppear) {
                    Service.getInstance().sendThongBao(pl, "Không thể thực hiện");
                    return;
                }

                if (Util.canDoWithTime(lastTimeShenronAppeared, timeResummonShenron)) {
                    // gọi rồng
                    playerSummonShenron = pl;
                    playerSummonShenronId = (int) pl.id;
                    mapShenronAppear = pl.zone;
                    byte dragonStar = (byte) pl_dragonStar.get(playerSummonShenron);
                    int begin = NGOC_RONG_1_SAO;
                    switch (dragonStar) {
                        case 2:
                            begin = NGOC_RONG_2_SAO;
                            break;
                        case 3:
                            begin = NGOC_RONG_3_SAO;
                            break;
                    }
                    for (int i = begin; i <= NGOC_RONG_7_SAO; i++) {
                        InventoryService.gI().subQuantityItemsBag(pl, InventoryService.gI().findItemBagByTemp(pl, i),
                                1);
                    }
                    InventoryService.gI().sendItemBags(pl);
                    sendNotifyShenronAppear(DRAGON_SHENRON);
                    activeShenron(pl, true);
                    sendWhishesShenron(pl);
                } else {
                    int timeLeft = (int) ((timeResummonShenron - (System.currentTimeMillis() - lastTimeShenronAppeared))
                            / 1000);
                    Service.getInstance().sendThongBao(pl,
                            "Vui lòng đợi " + (timeLeft < 60 ? timeLeft + " giây" : timeLeft / 60 + " phút") + " nữa");
                }
            }
        } else {
            Service.getInstance().sendThongBao(pl, "Chỉ được gọi rồng thần ở ngôi làng trước nhà");
        }
    }

    public void summonBlackShenron(Player pl) {
        if (pl.zone.map.mapId == 5) {
            if (checkShenronBall(pl, DRAGON_BLACK_SHENRON)) {
                if (isBlackShenronAppear) {
                    Service.getInstance().sendThongBao(pl, "Không thể thực hiện");
                    return;
                }
                if (Util.canDoWithTime(lastTimeShenronAppeared, timeResummonShenron)) {
                    playerSummonShenron = pl;
                    playerSummonShenronId = (int) pl.id;
                    mapShenronAppear = pl.zone;
                    InventoryService.gI().subQuantityItemsBag(pl,
                            InventoryService.gI().findItemBagByTemp(pl, NGOC_RONG_SIEU_CAP), 1);
                    InventoryService.gI().sendItemBags(pl);
                    sendNotifyShenronAppear(DRAGON_BLACK_SHENRON);
                    isBlackShenronAppear = true;
                    activeDragonNew(pl, true, (byte) 60);
                    sendWhishesShenron(pl);
                } else {
                    int timeLeft = (int) ((timeResummonShenron - (System.currentTimeMillis() - lastTimeShenronAppeared))
                            / 1000);
                    Service.getInstance().sendThongBao(pl,
                            "Vui lòng đợi " + (timeLeft < 60 ? timeLeft + " giây" : timeLeft / 60 + " phút") + " nữa");
                }
            }
        } else {
            Service.getInstance().sendThongBao(pl, "Chỉ được gọi rồng siêu cấp ở Đảo Kame");
        }
    }

    public void summonIceShenron(Player pl) {
        if (pl.zone.map.mapId == 5) {
            if (checkShenronBall(pl, DRAGON_ICE_SHENRON)) {
                if (isIcecShenronAppear) {
                    Service.getInstance().sendThongBao(pl, "Không thể thực hiện");
                    return;
                }
                if (Util.canDoWithTime(lastTimeShenronAppeared, timeResummonShenron)) {
                    playerSummonShenron = pl;
                    playerSummonShenronId = (int) pl.id;
                    mapShenronAppear = pl.zone;

                    for (int i : NGOC_RONG_BANG) {
                        InventoryService.gI().subQuantityItemsBag(pl, InventoryService.gI().findItemBagByTemp(pl, i),
                                1);
                    }
                    InventoryService.gI().sendItemBags(pl);
                    sendNotifyShenronAppear(DRAGON_ICE_SHENRON);
                    isIcecShenronAppear = true;
                    activeDragonNew(pl, true, (byte) 59);
                    sendWhishesShenron(pl);
                } else {
                    int timeLeft = (int) ((timeResummonShenron - (System.currentTimeMillis() - lastTimeShenronAppeared))
                            / 1000);
                    Service.getInstance().sendThongBao(pl,
                            "Vui lòng đợi " + (timeLeft < 60 ? timeLeft + " giây" : timeLeft / 60 + " phút") + " nữa");
                }
            }
        } else {
            Service.getInstance().sendThongBao(pl, "Chỉ được gọi rồng siêu cấp ở Đảo Kame");
        }
    }

    private void reSummonShenron(byte Dragon) {
        switch (Dragon) {
            case DRAGON_SHENRON:
                activeShenron(playerSummonShenron, true);
                sendWhishesShenron(playerSummonShenron);
                break;
            case DRAGON_BLACK_SHENRON:
                isBlackShenronAppear = true;
                activeDragonNew(playerSummonShenron, true, (byte) 60);
                sendWhishesShenron(playerSummonShenron);
                break;
            case DRAGON_ICE_SHENRON:
                isIcecShenronAppear = true;
                activeDragonNew(playerSummonShenron, true, (byte) 59);
                sendWhishesShenron(playerSummonShenron);
                break;
        }

    }

    private void sendWhishesShenron(Player pl) {
        byte dragonStar;// id item ngọc rồng
        try {
            dragonStar = (byte) pl_dragonStar.get(pl);
            this.shenronStar = dragonStar;
        } catch (Exception e) {
            dragonStar = this.shenronStar;
        }
        switch (dragonStar) {
            case 1:
                NpcService.gI().createMenuRongThieng(pl, ConstNpc.SHENRON_1_1, SHENRON_SAY, SHENRON_1_STAR_WISHES_1);
                break;
            case 2:
                NpcService.gI().createMenuRongThieng(pl, ConstNpc.SHENRON_2, SHENRON_SAY, SHENRON_2_STARS_WHISHES);
                break;
            case 3:
                NpcService.gI().createMenuRongThieng(pl, ConstNpc.SHENRON_3, SHENRON_SAY, SHENRON_3_STARS_WHISHES);
                break;
            case (byte) NGOC_RONG_SIEU_CAP:
                NpcService.gI().createMenuRongThieng(pl, ConstNpc.BLACK_SHENRON, BLACK_SHENRON_SAY,
                        BLACK_SHENRON_WISHES);
                break;
            case (byte) 925:
                NpcService.gI().createMenuRongThieng(pl, ConstNpc.ICE_SHENRON, ICE_SHENRON_SAY, ICE_SHENRON_WISHES);
                break;
        }
    }

    private void activeShenron(Player pl, boolean appear) {
        Message msg;
        try {
            msg = new Message(-83);
            msg.writer().writeByte(appear ? 0 : (byte) 1);
            if (appear) {
                msg.writer().writeShort(pl.zone.map.mapId);
                msg.writer().writeShort(pl.zone.map.bgId);
                msg.writer().writeByte(pl.zone.zoneId);
                msg.writer().writeInt((int) pl.id);
                msg.writer().writeUTF("");
                msg.writer().writeShort(pl.location.x);
                msg.writer().writeShort(pl.location.y);
                msg.writer().writeByte(DRAGON_SHENRON);
                lastTimeShenronWait = System.currentTimeMillis();
                isShenronAppear = true;
            }
            Service.getInstance().sendMessAllPlayer(msg);
        } catch (Exception e) {
        }
    }

    public void activeDragonNew(Player pl, boolean appear, byte eff) {
        Message msg;
        try {
            msg = new Message(Cmd.CALL_DRAGON);
            msg.writer().writeByte(appear ? 0 : (byte) 1);
            if (appear) {
                msg.writer().writeShort(pl.zone.map.mapId);
                msg.writer().writeShort(pl.zone.map.bgId);
                msg.writer().writeByte(pl.zone.zoneId);
                msg.writer().writeInt((int) pl.id);
                msg.writer().writeUTF("");
                msg.writer().writeShort(pl.location.x);
                msg.writer().writeShort(pl.location.y);
                msg.writer().writeByte(DRAGON_PORUNGA);
                pl.zone.effDragon = eff;
                lastTimeShenronWait = System.currentTimeMillis();
            }
            Service.getInstance().sendMessAllPlayer(msg);
        } catch (Exception e) {
        }
    }

    private boolean checkShenronBall(Player pl, byte Dragon) {
        byte dragonStar = (byte) this.pl_dragonStar.get(pl);
        switch (Dragon) {
            case DRAGON_SHENRON:
                if (dragonStar == 1) {
                    if (!InventoryService.gI().existItemBag(pl, NGOC_RONG_2_SAO)) {
                        Service.getInstance().sendThongBao(pl, "Bạn còn thiếu 1 viên ngọc rồng 2 sao");
                        return false;
                    }
                    if (!InventoryService.gI().existItemBag(pl, NGOC_RONG_3_SAO)) {
                        Service.getInstance().sendThongBao(pl, "Bạn còn thiếu 1 viên ngọc rồng 3 sao");
                        return false;
                    }
                } else if (dragonStar == 2) {
                    if (!InventoryService.gI().existItemBag(pl, NGOC_RONG_3_SAO)) {
                        Service.getInstance().sendThongBao(pl, "Bạn còn thiếu 1 viên ngọc rồng 3 sao");
                        return false;
                    }
                }
                if (!InventoryService.gI().existItemBag(pl, NGOC_RONG_4_SAO)) {
                    Service.getInstance().sendThongBao(pl, "Bạn còn thiếu 1 viên ngọc rồng 4 sao");
                    return false;
                }
                if (!InventoryService.gI().existItemBag(pl, NGOC_RONG_5_SAO)) {
                    Service.getInstance().sendThongBao(pl, "Bạn còn thiếu 1 viên ngọc rồng 5 sao");
                    return false;
                }
                if (!InventoryService.gI().existItemBag(pl, NGOC_RONG_6_SAO)) {
                    Service.getInstance().sendThongBao(pl, "Bạn còn thiếu 1 viên ngọc rồng 6 sao");
                    return false;
                }
                if (!InventoryService.gI().existItemBag(pl, NGOC_RONG_7_SAO)) {
                    Service.getInstance().sendThongBao(pl, "Bạn còn thiếu 1 viên ngọc rồng 7 sao");
                    return false;
                }
                return true;
            case DRAGON_BLACK_SHENRON:
                if (!InventoryService.gI().existItemBag(pl, NGOC_RONG_SIEU_CAP)) {
                    Service.getInstance().sendThongBao(pl, "Bạn còn thiếu 1 viên ngọc rồng siêu cấp");
                    return false;
                }
                return true;
            case DRAGON_ICE_SHENRON:
                for (int i : NGOC_RONG_BANG) {
                    if (!InventoryService.gI().existItemBag(pl, i)) {
                        Service.getInstance().sendThongBao(pl,
                                "Bạn còn thiếu 1 viên ngọc rồng băng " + (i - 924) + " sao");
                        return false;
                    }
                }
                return true;
        }
        return false;
    }

    private void sendNotifyShenronAppear(int Dragon) {
        Message msg;
        try {
            msg = new Message(-25);
            switch (Dragon) {
                case DRAGON_SHENRON:
                    msg.writer().writeUTF(playerSummonShenron.name + " vừa gọi rồng thần tại "
                            + playerSummonShenron.zone.map.mapName + " khu vực " + playerSummonShenron.zone.zoneId);
                    Service.getInstance().sendMessAllPlayerIgnoreMe(playerSummonShenron, msg);
                    break;
                case DRAGON_BLACK_SHENRON:
                    msg.writer().writeUTF(playerSummonShenron.name + " vừa gọi rồng siêu cấp tại "
                            + playerSummonShenron.zone.map.mapName + " khu vực " + playerSummonShenron.zone.zoneId);
                    Service.getInstance().sendMessAllPlayerIgnoreMe(playerSummonShenron, msg);
                    break;
                case DRAGON_ICE_SHENRON:
                    msg.writer().writeUTF(playerSummonShenron.name + " vừa gọi rồng băng tại "
                            + playerSummonShenron.zone.map.mapName + " khu vực " + playerSummonShenron.zone.zoneId);
                    Service.getInstance().sendMessAllPlayerIgnoreMe(playerSummonShenron, msg);
                    break;
            }

            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void confirmWish() {
        switch (this.menuShenron) {
            case ConstNpc.SHENRON_1_1:
                switch (this.select) {
                    case 0: // 30 tỷ
                        this.playerSummonShenron.inventory.addGold(30000000000L);
                        PlayerService.gI().sendInfoHpMpMoney(this.playerSummonShenron);
                        break;

                    case 1: // găng tay đang đeo lên 1 cấp
                        Item item = this.playerSummonShenron.inventory.itemsBody.get(2);
                        if (item.isNotNullItem()) {
                            int level = 0;
                            for (ItemOption io : item.itemOptions) {
                                if (io.optionTemplate.id == 72) {
                                    level = io.param;
                                    if (level < 7) {
                                        io.param++;
                                    }
                                    break;
                                }
                            }
                            if (level < 7) {
                                if (level == 0) {
                                    item.itemOptions.add(new ItemOption(72, 1));
                                }
                                for (ItemOption io : item.itemOptions) {
                                    if (io.optionTemplate.id == 0) {
                                        io.param += (io.param * 10 / 100);
                                        break;
                                    }
                                }
                                InventoryService.gI().sendItemBody(playerSummonShenron);
                            } else {
                                Service.getInstance().sendThongBao(playerSummonShenron,
                                        "Găng tay của ngươi đã đạt cấp tối đa");
                                reOpenShenronWishes(playerSummonShenron);
                                return;
                            }
                        } else {
                            Service.getInstance().sendThongBao(playerSummonShenron, "Ngươi hiện tại có đeo găng đâu");
                            reOpenShenronWishes(playerSummonShenron);
                            return;
                        }
                        break;
                    case 2: // chí mạng +2%
                        if (this.playerSummonShenron.nPoint.critg < 9) {
                            this.playerSummonShenron.nPoint.critg += 2;
                        } else {
                            Service.getInstance().sendThongBao(playerSummonShenron,
                                    "Điều ước này đã quá sức với ta, ta sẽ cho ngươi chọn lại");
                            reOpenShenronWishes(playerSummonShenron);
                            return;
                        }
                        break;
                    case 3: // thay chiêu 2-3 đệ tử
                        if (playerSummonShenron.pet != null) {
                            if (playerSummonShenron.pet.playerSkill.skills.get(1).skillId != -1) {
                                playerSummonShenron.pet.openSkill2();
                                if (playerSummonShenron.pet.playerSkill.skills.get(2).skillId != -1) {
                                    playerSummonShenron.pet.openSkill3();
                                }
                            } else {
                                Service.getInstance().sendThongBao(playerSummonShenron,
                                        "Ít nhất đệ tử ngươi phải có chiêu 2 chứ!");
                                reOpenShenronWishes(playerSummonShenron);
                                return;
                            }
                        } else {
                            Service.getInstance().sendThongBao(playerSummonShenron, "Ngươi làm gì có đệ tử?");
                            reOpenShenronWishes(playerSummonShenron);
                            return;
                        }
                        break;
                }
                break;
            case ConstNpc.SHENRON_1_2:
                switch (this.select) {
                    case 0: // đẹp trai nhất vũ trụ
                        if (InventoryService.gI().getCountEmptyBag(playerSummonShenron) > 0) {
                            Item avtVip = ItemService.gI().createNewItem((short) 2142, 1);
                            if (Util.isTrue(1, 100)) {
                                avtVip.itemOptions.add(new ItemOption(50, Util.nextInt(30, 35)));
                                avtVip.itemOptions.add(new ItemOption(77, Util.nextInt(10, 15)));
                                avtVip.itemOptions.add(new ItemOption(103, Util.nextInt(30, 35)));
                            } else {
                                avtVip.itemOptions.add(new ItemOption(50, Util.nextInt(10, 30)));
                                avtVip.itemOptions.add(new ItemOption(77, Util.nextInt(1, 9)));
                                avtVip.itemOptions.add(new ItemOption(103, Util.nextInt(10, 30)));
                            }
                            InventoryService.gI().addItemBag(playerSummonShenron, avtVip, 0);
                            InventoryService.gI().sendItemBags(playerSummonShenron);
                        } else {
                            Service.getInstance().sendThongBao(playerSummonShenron, "Hành trang đã đầy");
                            reOpenShenronWishes(playerSummonShenron);
                            return;
                        }
                        break;
                    case 1: // +1,5 ngọc
                        this.playerSummonShenron.inventory.gem += 10000;
                        PlayerService.gI().sendInfoHpMpMoney(this.playerSummonShenron);
                        break;
                    case 2: // +200 tr smtn
                        if (false) {
                            Service.getInstance().addSMTN(this.playerSummonShenron, (byte) 2, 200000000, false);
                        } else {
                            Service.getInstance().sendThongBao(playerSummonShenron,
                                    "Xin lỗi, điều ước này khó quá, ta không thể thực hiện.");
                            reOpenShenronWishes(playerSummonShenron);
                            return;
                        }
                        break;
                    case 3: // găng tay đệ lên 1 cấp
                        if (this.playerSummonShenron != null && this.playerSummonShenron.pet != null) {
                            Item item = this.playerSummonShenron.pet.inventory.itemsBody.get(2);
                            if (item.isNotNullItem()) {
                                int level = 0;
                                for (ItemOption io : item.itemOptions) {
                                    if (io.optionTemplate.id == 72) {
                                        level = io.param;
                                        if (level < 7) {
                                            io.param++;
                                        }
                                        break;
                                    }
                                }
                                if (level < 7) {
                                    if (level == 0) {
                                        item.itemOptions.add(new ItemOption(72, 1));
                                    }
                                    for (ItemOption io : item.itemOptions) {
                                        if (io.optionTemplate.id == 0) {
                                            io.param += (io.param * 10 / 100);
                                            break;
                                        }
                                    }
                                    Service.getInstance().point(playerSummonShenron);
                                } else {
                                    Service.getInstance().sendThongBao(playerSummonShenron,
                                            "Găng tay của đệ ngươi đã đạt cấp tối đa");
                                    reOpenShenronWishes(playerSummonShenron);
                                    return;
                                }
                            } else {
                                Service.getInstance().sendThongBao(playerSummonShenron,
                                        "Đệ ngươi hiện tại có đeo găng đâu");
                                reOpenShenronWishes(playerSummonShenron);
                                return;
                            }
                        } else {
                            Service.getInstance().sendThongBao(playerSummonShenron, "Ngươi đâu có đệ tử");
                            reOpenShenronWishes(playerSummonShenron);
                            return;
                        }
                        break;
                }
                break;
            case ConstNpc.SHENRON_2:
                switch (this.select) {
                    case 0: // +150 ngọc
                        this.playerSummonShenron.inventory.gem += 2000;
                        PlayerService.gI().sendInfoHpMpMoney(this.playerSummonShenron);
                        break;
                    case 1: // +20 tr smtn
                        if (false) {
                            Service.getInstance().addSMTN(this.playerSummonShenron, (byte) 2, 20000000, false);
                        } else {
                            Service.getInstance().sendThongBao(playerSummonShenron,
                                    "Xin lỗi, điều ước này khó quá, ta không thể thực hiện.");
                            reOpenShenronWishes(playerSummonShenron);
                            return;
                        }

                        break;
                    case 2: // 2 tr vàng
                        this.playerSummonShenron.inventory.addGold(200000000);
                        PlayerService.gI().sendInfoHpMpMoney(this.playerSummonShenron);
                        break;
                }
                break;
            case ConstNpc.SHENRON_3:
                switch (this.select) {
                    case 0: // +15 ngọc
                        this.playerSummonShenron.inventory.gem += 200;
                        PlayerService.gI().sendInfoHpMpMoney(this.playerSummonShenron);
                        break;
                    case 1: // +2 tr smtn
                        if (false) {
                            Service.getInstance().addSMTN(this.playerSummonShenron, (byte) 2, 2000000, false);
                        } else {
                            Service.getInstance().sendThongBao(playerSummonShenron,
                                    "Xin lỗi, điều ước này khó quá, ta không thể thực hiện.");
                            reOpenShenronWishes(playerSummonShenron);
                            return;
                        }
                        break;
                    case 2: // 200k vàng
                        this.playerSummonShenron.inventory.addGold(20000000);
                        PlayerService.gI().sendInfoHpMpMoney(this.playerSummonShenron);
                        break;
                }
                break;
            case ConstNpc.BLACK_SHENRON:
                switch (this.select) {
                    case 0:
                        if (InventoryService.gI().getCountEmptyBag(playerSummonShenron) > 0) {
                            Item ctGohan = ItemService.gI().createNewItem((short) 989);
                            ctGohan.itemOptions.add(new ItemOption(50, Util.nextInt(20, 30)));
                            ctGohan.itemOptions.add(new ItemOption(77, Util.nextInt(20, 30)));
                            ctGohan.itemOptions.add(new ItemOption(103, Util.nextInt(20, 30)));
                            ctGohan.itemOptions.add(new ItemOption(5, Util.nextInt(20, 30)));
                            ctGohan.itemOptions.add(new ItemOption(47, Util.nextInt(5, 15)));

                            ctGohan.itemOptions.add(new ItemOption(93, 10));
                            InventoryService.gI().addItemBag(playerSummonShenron, ctGohan, 0);
                            InventoryService.gI().sendItemBags(playerSummonShenron);
                        } else {
                            Service.getInstance().sendThongBao(playerSummonShenron, "Hành trang đã đầy");
                            reOpenShenronWishes(playerSummonShenron);
                            return;
                        }
                        break;
                    case 1:
                        if (InventoryService.gI().getCountEmptyBag(playerSummonShenron) > 0) {
                            Item ctBiden = ItemService.gI().createNewItem((short) 990);
                            ctBiden.itemOptions.add(new ItemOption(50, Util.nextInt(20, 30)));
                            ctBiden.itemOptions.add(new ItemOption(77, Util.nextInt(20, 30)));
                            ctBiden.itemOptions.add(new ItemOption(103, Util.nextInt(20, 30)));
                            ctBiden.itemOptions.add(new ItemOption(5, Util.nextInt(20, 30)));
                            ctBiden.itemOptions.add(new ItemOption(47, Util.nextInt(5, 15)));

                            ctBiden.itemOptions.add(new ItemOption(93, 10));
                            InventoryService.gI().addItemBag(playerSummonShenron, ctBiden, 0);
                            InventoryService.gI().sendItemBags(playerSummonShenron);
                        } else {
                            Service.getInstance().sendThongBao(playerSummonShenron, "Hành trang đã đầy");
                            reOpenShenronWishes(playerSummonShenron);
                            return;
                        }
                        break;
                    case 2:
                        if (InventoryService.gI().getCountEmptyBag(playerSummonShenron) > 0) {
                            Item ctConuong = ItemService.gI().createNewItem((short) 991);
                            ctConuong.itemOptions.add(new ItemOption(50, Util.nextInt(20, 30)));
                            ctConuong.itemOptions.add(new ItemOption(77, Util.nextInt(20, 30)));
                            ctConuong.itemOptions.add(new ItemOption(103, Util.nextInt(20, 30)));
                            ctConuong.itemOptions.add(new ItemOption(5, Util.nextInt(20, 30)));
                            ctConuong.itemOptions.add(new ItemOption(47, Util.nextInt(5, 15)));

                            ctConuong.itemOptions.add(new ItemOption(93, 10));
                            InventoryService.gI().addItemBag(playerSummonShenron, ctConuong, 0);
                            InventoryService.gI().sendItemBags(playerSummonShenron);
                        } else {
                            Service.getInstance().sendThongBao(playerSummonShenron, "Hành trang đã đầy");
                            reOpenShenronWishes(playerSummonShenron);
                            return;
                        }
                        break;
                    case 3:
                        if (InventoryService.gI().getCountEmptyBag(playerSummonShenron) > 0) {
                            Item PetThoOm = ItemService.gI().createNewItem((short) 1039);
                            PetThoOm.itemOptions.add(new ItemOption(50, Util.nextInt(5, 10)));
                            PetThoOm.itemOptions.add(new ItemOption(77, Util.nextInt(5, 10)));
                            PetThoOm.itemOptions.add(new ItemOption(103, Util.nextInt(5, 10)));
                            PetThoOm.itemOptions.add(new ItemOption(93, 10));
                            InventoryService.gI().addItemBag(playerSummonShenron, PetThoOm, 0);
                            InventoryService.gI().sendItemBags(playerSummonShenron);
                        } else {
                            Service.getInstance().sendThongBao(playerSummonShenron, "Hành trang đã đầy");
                            reOpenShenronWishes(playerSummonShenron);
                            return;
                        }
                        break;
                    case 4:
                        if (InventoryService.gI().getCountEmptyBag(playerSummonShenron) > 0) {
                            Item PetThoMap = ItemService.gI().createNewItem((short) 1040);
                            PetThoMap.itemOptions.add(new ItemOption(50, Util.nextInt(5, 10)));
                            PetThoMap.itemOptions.add(new ItemOption(77, Util.nextInt(5, 10)));
                            PetThoMap.itemOptions.add(new ItemOption(103, Util.nextInt(5, 10)));
                            PetThoMap.itemOptions.add(new ItemOption(93, 10));
                            InventoryService.gI().addItemBag(playerSummonShenron, PetThoMap, 0);
                            InventoryService.gI().sendItemBags(playerSummonShenron);
                        } else {
                            Service.getInstance().sendThongBao(playerSummonShenron, "Hành trang đã đầy");
                            reOpenShenronWishes(playerSummonShenron);
                            return;
                        }
                        break;
                }
                break;
            case ConstNpc.ICE_SHENRON:
                switch (this.select) {
                    case 0:
                        if (InventoryService.gI().getCountEmptyBag(playerSummonShenron) > 0) {
                            Item it = ItemService.gI().createNewItem((short) 827);
                            it.itemOptions.add(new ItemOption(50, Util.nextInt(20, 25)));
                            it.itemOptions.add(new ItemOption(77, 12));
                            it.itemOptions.add(new ItemOption(103, 25));
                            it.itemOptions.add(new ItemOption(5, Util.nextInt(10, 35)));
                            it.itemOptions.add(new ItemOption(106, 0));
                            InventoryService.gI().addItemBag(playerSummonShenron, it, 0);
                            InventoryService.gI().sendItemBags(playerSummonShenron);
                        } else {
                            Service.getInstance().sendThongBao(playerSummonShenron, "Hành trang đã đầy");
                            reOpenShenronWishes(playerSummonShenron);
                            return;
                        }
                        break;
                    case 1:
                        if (InventoryService.gI().getCountEmptyBag(playerSummonShenron) > 0) {
                            Item it = ItemService.gI().createNewItem((short) 827);
                            it.itemOptions.add(new ItemOption(50, 25));
                            it.itemOptions.add(new ItemOption(77, 12));
                            it.itemOptions.add(new ItemOption(103, Util.nextInt(30, 50)));
                            it.itemOptions.add(new ItemOption(162, 2));
                            it.itemOptions.add(new ItemOption(106, 0));
                            InventoryService.gI().addItemBag(playerSummonShenron, it, 0);
                            InventoryService.gI().sendItemBags(playerSummonShenron);
                        } else {
                            Service.getInstance().sendThongBao(playerSummonShenron, "Hành trang đã đầy");
                            reOpenShenronWishes(playerSummonShenron);
                            return;
                        }
                        break;
                    case 2:
                        if (InventoryService.gI().getCountEmptyBag(playerSummonShenron) > 0) {
                            Item it = ItemService.gI().createNewItem((short) 937);
                            it.itemOptions.add(new ItemOption(50, 25));
                            it.itemOptions.add(new ItemOption(77, Util.nextInt(1, 20)));
                            it.itemOptions.add(new ItemOption(103, 25));
                            it.itemOptions.add(new ItemOption(80, 10));
                            it.itemOptions.add(new ItemOption(106, 0));
                            InventoryService.gI().addItemBag(playerSummonShenron, it, 0);
                            InventoryService.gI().sendItemBags(playerSummonShenron);
                        } else {
                            Service.getInstance().sendThongBao(playerSummonShenron, "Hành trang đã đầy");
                            reOpenShenronWishes(playerSummonShenron);
                            return;
                        }
                        break;
                }
                break;
        }
        shenronLeave(this.playerSummonShenron, WISHED);
    }

    public void showConfirmShenron(Player pl, int menu, byte select) {
        this.menuShenron = menu;
        this.select = select;
        String wish = null;
        switch (menu) {
            case ConstNpc.SHENRON_1_1:
                wish = SHENRON_1_STAR_WISHES_1[select];
                break;
            case ConstNpc.SHENRON_1_2:
                wish = SHENRON_1_STAR_WISHES_2[select];
                break;
            case ConstNpc.SHENRON_2:
                wish = SHENRON_2_STARS_WHISHES[select];
                break;
            case ConstNpc.SHENRON_3:
                wish = SHENRON_3_STARS_WHISHES[select];
                break;
            case ConstNpc.BLACK_SHENRON:
                wish = BLACK_SHENRON_WISHES[select];
                break;
            case ConstNpc.ICE_SHENRON:
                wish = ICE_SHENRON_WISHES[select];
                break;
        }
        NpcService.gI().createMenuRongThieng(pl, ConstNpc.SHENRON_CONFIRM, "Ngươi có chắc muốn ước?", wish, "Từ chối");
    }

    public void reOpenShenronWishes(Player pl) {
        switch (menuShenron) {
            case ConstNpc.SHENRON_1_1:
                NpcService.gI().createMenuRongThieng(pl, ConstNpc.SHENRON_1_1, SHENRON_SAY, SHENRON_1_STAR_WISHES_1);
                break;
            case ConstNpc.SHENRON_1_2:
                NpcService.gI().createMenuRongThieng(pl, ConstNpc.SHENRON_1_2, SHENRON_SAY, SHENRON_1_STAR_WISHES_2);
                break;
            case ConstNpc.SHENRON_2:
                NpcService.gI().createMenuRongThieng(pl, ConstNpc.SHENRON_2, SHENRON_SAY, SHENRON_2_STARS_WHISHES);
                break;
            case ConstNpc.SHENRON_3:
                NpcService.gI().createMenuRongThieng(pl, ConstNpc.SHENRON_3, SHENRON_SAY, SHENRON_3_STARS_WHISHES);
                break;
            case ConstNpc.BLACK_SHENRON:
                NpcService.gI().createMenuRongThieng(pl, ConstNpc.BLACK_SHENRON, BLACK_SHENRON_SAY,
                        BLACK_SHENRON_WISHES);
                break;
            case ConstNpc.ICE_SHENRON:
                NpcService.gI().createMenuRongThieng(pl, ConstNpc.ICE_SHENRON, ICE_SHENRON_SAY, ICE_SHENRON_WISHES);
                break;
        }
    }

    public void shenronLeave(Player pl, byte type) {

        if (type == WISHED) {
            NpcService.gI().createTutorial(pl, -1,
                    "Điều ước của ngươi đã trở thành sự thật\nHẹn gặp ngươi lần sau, ta đi ngủ đây, bái bai");
        } else {
            NpcService.gI().createMenuRongThieng(pl, ConstNpc.IGNORE_MENU,
                    "Ta buồn ngủ quá rồi\nHẹn gặp ngươi lần sau, ta đi đây, bái bai");
        }
        activeShenron(pl, false);
        this.isBlackShenronAppear = false;
        this.isShenronAppear = false;
        this.isIcecShenronAppear = false;
        this.menuShenron = -1;
        this.select = -1;
        this.playerSummonShenron = null;
        this.playerSummonShenronId = -1;
        this.shenronStar = -1;
        this.mapShenronAppear = null;
        lastTimeShenronAppeared = System.currentTimeMillis();
        if (pl.zone != null) {
            pl.zone.effDragon = -1;
        }
    }

    // --------------------------------------------------------------------------
}
