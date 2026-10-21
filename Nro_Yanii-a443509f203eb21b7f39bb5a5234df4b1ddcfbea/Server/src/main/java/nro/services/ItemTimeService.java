package nro.services;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import nro.consts.ConstAchive;
import nro.consts.ConstPlayer;
import nro.models.item.Item;
import nro.models.item.ItemOption;
//import nro.models.map.phoban.BanDoKhoBau;
import nro.models.map.phoban.DoanhTrai;
import nro.models.player.Fusion;
import nro.models.player.Player;
import nro.server.io.Message;
import nro.utils.Log;

import static nro.models.item.ItemTime.*;
import nro.models.map.phoban.BanDoKhoBau;
import nro.models.map.phoban.KhiGas;
import nro.utils.Util;

/**
 *
 * @author 💖 Nro Yanii 💖
 *
 *
 */
public class ItemTimeService {

    private static ItemTimeService i;

    public static ItemTimeService gI() {
        if (i == null) {
            i = new ItemTimeService();
        }
        return i;
    }

    public void sendItemTimeBienHinh(Player player, int level) {
        int iconLvFirst = switch (player.gender) {
            case 0 ->
                31254;
            case 1 ->
                31266;
            case 2 ->
                31260;
            default ->
                0; // Giá trị mặc định nếu gender không hợp lệ
        };
        int timeIcon = player.effectSkill.timeBienHinh / 1000;

        if (level == 1) {
            sendItemTime(player, iconLvFirst, timeIcon);
        } else {
            int previousIcon = iconLvFirst + level - 2;
            int currentIcon = iconLvFirst + level - 1;
            removeItemTime(player, previousIcon);
            sendItemTime(player, currentIcon, timeIcon);
        }
    }

    // gửi cho client
    public void sendAllItemTime(Player player) {
        sendTextDoanhTrai(player);
        sendTextBanDoKhoBau(player);
        sendTextKhiGas(player);
        if (player.clan != null) {
            if (player.clan.banDoKhoBau != null) {
                // player.clan.banDoKhoBau.sendThongBaoBanDoKhoBau();
            }
        }
        if (player.fusion.typeFusion == ConstPlayer.LUONG_LONG_NHAT_THE) {
            sendItemTime(player, player.gender == ConstPlayer.NAMEC ? 3901 : 3790,
                    (int) ((Fusion.TIME_FUSION - (System.currentTimeMillis() - player.fusion.lastTimeFusion)) / 1000));
        }
        if (player.itemTime.isUseBoHuyet) {
            sendItemTime(player, 2755,
                    (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBoHuyet)) / 1000));
        }
        if (player.itemTime.isUseBoKhi) {
            sendItemTime(player, 2756,
                    (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBoKhi)) / 1000));
        }
        if (player.itemTime.isUseGiapXen) {
            sendItemTime(player, 2757,
                    (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeGiapXen)) / 1000));
        }
        if (player.itemTime.isDanhNhanBan) {
            sendItemTime(player, 2295,
                    (int) ((TIME_DANH_NHAN_BAN - (System.currentTimeMillis() - player.itemTime.lasttimeDanhNhanBan))
                    / 1000));
        }
        if (player.itemTime.isUseCuongNo) {
            sendItemTime(player, 2754,
                    (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeCuongNo)) / 1000));
        }
        if (player.itemTime.isUseAnDanh) {
            sendItemTime(player, 2760,
                    (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeAnDanh)) / 1000));
        }
        if (player.itemTime.isOpenPower) {
            sendItemTime(player, 3783,
                    (int) ((TIME_OPEN_POWER - (System.currentTimeMillis() - player.itemTime.lastTimeOpenPower))
                    / 1000));
        }
        if (player.itemTime.isUseMayDo) {
            sendItemTime(player, 2758,
                    (int) ((TIME_MAY_DO - (System.currentTimeMillis() - player.itemTime.lastTimeUseMayDo)) / 1000));
        }
        if (player.itemTime.isEatMeal) {
            sendItemTime(player, player.itemTime.iconMeal,
                    (int) ((TIME_EAT_MEAL - (System.currentTimeMillis() - player.itemTime.lastTimeEatMeal)) / 1000));
        }
        if (player.itemTime.isUseBanhChung) {
            sendItemTime(player, 15043,
                    (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBanhChung)) / 1000));
        }
        if (player.itemTime.isUseBanhTet) {
            sendItemTime(player, 15041,
                    (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBanhTet)) / 1000));
        }
        if (player.itemTime.isUseBoHuyet2) {
            sendItemTime(player, 10714,
                    (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBoHuyet2)) / 1000));
        }
        if (player.itemTime.isUseBoKhi2) {
            sendItemTime(player, 10715,
                    (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBoKhi2)) / 1000));
        }
        if (player.itemTime.isUseGiapXen2) {
            sendItemTime(player, 10712,
                    (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeGiapXen2)) / 1000));
        }
        if (player.itemTime.isUseCuongNo2) {
            sendItemTime(player, 10716,
                    (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeCuongNo2)) / 1000));
        }
        // BiNgo
        if (player.effectSkill.isBiNgo) {
            sendItemTime(player, 7057, (int) ((30_000 - (System.currentTimeMillis() - player.effectSkill.lastBiNgo)) / 1000));
        }
        // BiNgo
    }

    // bật tđlt
    public void turnOnTDLT(Player player, Item item) {
        int min = 0;
        for (ItemOption io : item.itemOptions) {
            if (io.optionTemplate.id == 1) {
                min = io.param;
                io.param = 0;
                break;
            }
        }
        player.itemTime.isUseTDLT = true;
        player.itemTime.timeTDLT = min * 60 * 1000;
        player.itemTime.lastTimeUseTDLT = System.currentTimeMillis();
        sendCanAutoPlay(player);
        sendItemTime(player, 4387, player.itemTime.timeTDLT / 1000);
        InventoryService.gI().sendItemBags(player);
    }

    // tắt tđlt
    public void turnOffTDLT(Player player, Item item) {
        player.itemTime.isUseTDLT = false;
        for (ItemOption io : item.itemOptions) {
            if (io.optionTemplate.id == 1) {
                io.param = (short) ((player.itemTime.timeTDLT
                        - (System.currentTimeMillis() - player.itemTime.lastTimeUseTDLT)) / 60 / 1000);
                break;
            }
        }
        sendCanAutoPlay(player);
        removeItemTime(player, 4387);
        InventoryService.gI().sendItemBags(player);
    }

    public void sendCanAutoPlay(Player player) {
        Message msg;
        try {
            msg = new Message(-116);
            msg.writer().writeByte(player.itemTime.isUseTDLT ? 1 : 0);
            player.sendMessage(msg);
        } catch (Exception e) {
            Log.error(ItemTimeService.class, e);
        }
    }

    public void sendTextDoanhTrai(Player player) {
        if (player.clan != null && !player.clan.haveGoneDoanhTrai && player.clan.timeOpenDoanhTrai != 0) {
            if (player.clan.doanhTrai.isHaveDoneDoanhTrai) {
                sendTextTime(player, DOANH_TRAI, "Trại độc nhãn: ", 300000);
            } else {
                int secondPassed = (int) ((System.currentTimeMillis() - player.clan.timeOpenDoanhTrai) / 1000);
                int secondsLeft = (DoanhTrai.TIME_DOANH_TRAI / 1000) - secondPassed;
                sendTextTime(player, DOANH_TRAI, "Trại độc nhãn:", secondsLeft);
            }
        }
    }

    public void sendTextBanDoKhoBau(Player player) {
        if (player.clan != null && player.clan.banDoKhoBau != null && player.clan.banDoKhoBau.isOpened
                && player.clan.timeOpenBanDoKhoBau != 0) {
            int secondPassed = (int) ((System.currentTimeMillis() - player.clan.timeOpenBanDoKhoBau) / 1000);
            int secondsLeft = (BanDoKhoBau.TIME_BAN_DO_KHO_BAU / 1000) - secondPassed;
            sendTextTime(player, BAN_DO_KHO_BAU, "Hang kho báu: ", secondsLeft);
            return;
        }
    }

    public void sendTextKhiGas(Player player) {
        if (player.clan != null && player.clan.khiGas != null && player.clan.khiGas.isOpened
                && player.clan.timeOpenKhiGas != 0) {
            int secondPassed = (int) ((System.currentTimeMillis() - player.clan.timeOpenKhiGas) / 1000);
            int secondsLeft = (KhiGas.TIME_KHI_GAS / 1000) - secondPassed;
            sendTextTime(player, KHI_GAS, "Khí gas hủy diệt: ", secondsLeft);
            return;
        }
    }

    public void removeTextBanDo(Player player) {
        removeTextTime(player, BAN_DO_KHO_BAU);
    }

    public void removeTextKhiGas(Player player) {
        removeTextTime(player, KHI_GAS);
    }

    public void removeTextDoanhTrai(Player player) {
        removeTextTime(player, DOANH_TRAI);
    }

    public void removeTextTime(Player player, byte id) {
        sendTextTime(player, id, "", 0);
    }

    public void sendTextTime(Player player, byte id, String text, int seconds) {
        Message msg;
        try {
            msg = new Message(65);
            msg.writer().writeByte(id);
            msg.writer().writeUTF(text);
            msg.writer().writeShort(seconds);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void sendItemTime(Player player, int itemId, int time) {
        Message msg;
        try {
            msg = new Message(-106);
            msg.writer().writeShort(itemId);
            msg.writer().writeShort(time);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void removeItemTime(Player player, int itemTime) {
        sendItemTime(player, itemTime, 0);
    }
}
