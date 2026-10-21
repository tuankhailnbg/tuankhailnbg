package nro.services;

import nro.consts.*;
import nro.models.boss.Boss;
import nro.models.boss.BossFactory;
import nro.models.item.Item;
import nro.models.item.ItemOption;
import nro.models.map.ItemMap;
import nro.models.map.Zone;
import nro.models.mob.Mob;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.task.Achivement;
import nro.models.task.SideTaskTemplate;
import nro.models.task.SubTaskMain;
import nro.models.task.TaskMain;
import nro.server.Manager;
import nro.server.io.Message;
import nro.utils.Log;
import nro.utils.Util;
import nro.consts.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import nro.jdbc.DBService;
import nro.jdbc.daos.PlayerDAO;
import nro.models.boss.BossData;
import nro.models.boss.BossManager;
import nro.models.boss.mapoffline.Boss_Tau77;
import nro.models.boss.mapoffline.Boss_ThanMeo;
import static nro.models.item.ItemTime.TEXT_NHIEM_VU_HANG_NGAY;
import nro.models.npc.NpcFactory;

/**
 * @author 💖 Nro Yanii 💖
 *
 */
public class TaskService {

    /**
     * Làm cùng số người trong bang
     */
    private static final byte NMEMBER_DO_TASK_TOGETHER = 0;

    private static TaskService i;

    public static TaskService gI() {
        if (i == null) {
            i = new TaskService();
        }
        return i;
    }

    public TaskMain getTaskMainById(Player player, int id) {
        for (TaskMain tm : Manager.TASKS) {
            if (tm.id == id) {
                TaskMain newTaskMain = new TaskMain(tm);
                newTaskMain.detail = transformName(player, newTaskMain.detail);
                for (SubTaskMain stm : newTaskMain.subTasks) {
                    stm.mapId = (short) transformMapId(player, stm.mapId);
                    stm.npcId = (byte) transformNpcId(player, stm.npcId);
                    stm.notify = transformName(player, stm.notify);
                    stm.name = transformName(player, stm.name);
                }
                return newTaskMain;
            }
        }
        return player.playerTask.taskMain;
    }

    // gửi thông tin nhiệm vụ chính
    public void sendTaskMain(Player player) {
        Message msg;
        try {
            msg = new Message(40);
            msg.writer().writeShort(player.playerTask.taskMain.id);
            // msg.writer().writeShort(12);
            msg.writer().writeByte(player.playerTask.taskMain.index);
            // msg.writer().writeUTF(player.playerTask.taskMain.name);
            msg.writer().writeUTF(player.playerTask.taskMain.name + " [" + player.playerTask.taskMain.id + "]");
            msg.writer().writeUTF(player.playerTask.taskMain.detail);
            msg.writer().writeByte(player.playerTask.taskMain.subTasks.size());
            for (SubTaskMain stm : player.playerTask.taskMain.subTasks) {
                msg.writer().writeUTF(stm.name);
                msg.writer().writeByte(stm.npcId);
                msg.writer().writeShort(stm.mapId);
                msg.writer().writeUTF(stm.notify);
            }
            msg.writer().writeShort(player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count);
            for (SubTaskMain stm : player.playerTask.taskMain.subTasks) {
                msg.writer().writeShort(stm.maxCount);
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Log.error(TaskService.class, e);
        }
    }

    // chuyển sang task mới
    public void sendNextTaskMain(Player player) {
        rewardDoneTask(player);
        player.playerTask.taskMain = TaskService.gI().getTaskMainById(player, player.playerTask.taskMain.id + 1);
        sendTaskMain(player);
        Service.getInstance().sendThongBao(player, "Nhiệm vụ tiếp theo của bạn là "
                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).name);
    }

    public void sendBackTaskMain(Player player) {
        rewardDoneTask(player);
        player.playerTask.taskMain = TaskService.gI().getTaskMainById(player, player.playerTask.taskMain.id - 1);
        sendTaskMain(player);
        Service.getInstance().sendThongBao(player, "Nhiệm vụ tiếp theo của bạn là "
                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).name);
    }

    // số lượng đã hoàn thành
    public void sendUpdateCountSubTask(Player player) {
        Message msg;
        try {
            msg = new Message(43);
            msg.writer().writeShort(player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    // chuyển sub task tiếp theo
    public void sendNextSubTask(Player player) {
        Message msg;
        try {
            msg = new Message(41);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    // gửi thông tin nhiệm vụ hiện tại
    public void sendInfoCurrentTask(Player player) {
        Service.getInstance().sendThongBao(player, "Nhiệm vụ hiện tại của bạn là "
                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).name);
    }

    public boolean checkDoneTaskTalkNpc(Player player, Npc npc) {
        switch (npc.tempId) {
            case ConstNpc.QUY_LAO_KAME:
                return (doneTask(player, ConstTask.TASK_11_0)
                        || doneTask(player, ConstTask.TASK_12_0)
                        || doneTask(player, ConstTask.TASK_12_1)
                        || doneTask(player, ConstTask.TASK_13_3)
                        || doneTask(player, ConstTask.TASK_14_2)
                        || doneTask(player, ConstTask.TASK_15_4)
                        || doneTask(player, ConstTask.TASK_16_1)
                        || doneTask(player, ConstTask.TASK_17_4)
                        || doneTask(player, ConstTask.TASK_18_2)
                        || doneTask(player, ConstTask.TASK_20_6)
                        || doneTask(player, ConstTask.TASK_21_3)
                        || doneTask(player, ConstTask.TASK_22_4)
                        || doneTask(player, ConstTask.TASK_23_3)
                        || doneTask(player, ConstTask.TASK_24_2)
                        || doneTask(player, ConstTask.TASK_19_2));
            case ConstNpc.TRUONG_LAO_GURU:
                return player.gender == ConstPlayer.NAMEC && (doneTask(player, ConstTask.TASK_11_0)
                        || doneTask(player, ConstTask.TASK_12_0)
                        || doneTask(player, ConstTask.TASK_12_1)
                        || doneTask(player, ConstTask.TASK_13_3)
                        || doneTask(player, ConstTask.TASK_14_2)
                        || doneTask(player, ConstTask.TASK_15_4)
                        || doneTask(player, ConstTask.TASK_16_1)
                        || doneTask(player, ConstTask.TASK_17_4)
                        || doneTask(player, ConstTask.TASK_18_2)
                        || doneTask(player, ConstTask.TASK_20_6)
                        || doneTask(player, ConstTask.TASK_21_3)
                        || doneTask(player, ConstTask.TASK_22_4)
                        || doneTask(player, ConstTask.TASK_23_3)
                        || doneTask(player, ConstTask.TASK_19_2));
            case ConstNpc.VUA_VEGETA:
                return player.gender == ConstPlayer.XAYDA && (doneTask(player, ConstTask.TASK_11_0)
                        || doneTask(player, ConstTask.TASK_12_0)
                        || doneTask(player, ConstTask.TASK_12_1)
                        || doneTask(player, ConstTask.TASK_13_3)
                        || doneTask(player, ConstTask.TASK_15_4)
                        || doneTask(player, ConstTask.TASK_14_2)
                        || doneTask(player, ConstTask.TASK_16_1)
                        || doneTask(player, ConstTask.TASK_18_2)
                        || doneTask(player, ConstTask.TASK_17_4)
                        || doneTask(player, ConstTask.TASK_20_6)
                        || doneTask(player, ConstTask.TASK_21_3)
                        || doneTask(player, ConstTask.TASK_22_4)
                        || doneTask(player, ConstTask.TASK_23_3)
                        || doneTask(player, ConstTask.TASK_19_2));
            case ConstNpc.ONG_GOHAN:
            case ConstNpc.ONG_MOORI:
            case ConstNpc.ONG_PARAGUS:
                return (doneTask(player, ConstTask.TASK_0_2)
                        || doneTask(player, ConstTask.TASK_0_5)
                        || doneTask(player, ConstTask.TASK_1_1)
                        || doneTask(player, ConstTask.TASK_2_1)
                        || doneTask(player, ConstTask.TASK_3_2)
                        || doneTask(player, ConstTask.TASK_4_3)
                        || doneTask(player, ConstTask.TASK_7_3)
                        || doneTask(player, ConstTask.TASK_7_2)
                        || doneTask(player, ConstTask.TASK_8_2)
                        || doneTask(player, ConstTask.TASK_10_3)
                        || doneTask(player, ConstTask.TASK_11_1)
                        || doneTask(player, ConstTask.TASK_24_0));
            case ConstNpc.BO_MONG:
                return (doneTask(player, ConstTask.TASK_9_0)
                        || doneTask(player, ConstTask.TASK_10_2));
            case ConstNpc.DR_DRIEF:
            case ConstNpc.CARGO:
            case ConstNpc.CUI:
                return (doneTask(player, ConstTask.TASK_6_1)
                        || doneTask(player, ConstTask.TASK_7_2)
                        || player.zone.map.mapId == 19 && doneTask(player, ConstTask.TASK_19_3)
                        || player.zone.map.mapId == 19 && doneTask(player, ConstTask.TASK_20_6)
                        || player.zone.map.mapId == 19 && doneTask(player, ConstTask.TASK_21_4));
            case ConstNpc.BUNMA:
            case ConstNpc.DENDE:
            case ConstNpc.APPULE:
                return doneTask(player, ConstTask.TASK_7_2);
            case ConstNpc.BUNMA_TL:
                return (doneTask(player, ConstTask.TASK_24_3)
                        || doneTask(player, ConstTask.TASK_24_5)
                        || doneTask(player, ConstTask.TASK_25_4)
                        || doneTask(player, ConstTask.TASK_26_4)
                        || doneTask(player, ConstTask.TASK_27_5)
                        || doneTask(player, ConstTask.TASK_28_5)
                        || doneTask(player, ConstTask.TASK_29_5));
            case ConstNpc.CALICK:
                return doneTask(player, ConstTask.TASK_24_1);
            case ConstNpc.THAN_MEO_KARIN:
                if (player.playerTask.taskMain.id == 29) {
                    if (player.nPoint.dameg >= 10000) {
                        return doneTask(player, ConstTask.TASK_29_0);
                    }
                }
                return doneTask(player, ConstTask.TASK_9_3);
        }
        return false;
    }

    // kiểm tra hoàn thành nhiệm vụ gia nhập bang hội
    public void checkDoneTaskJoinClan(Player player) {
        // if (!player.isBoss && !player.isPet) {
        // doneTask(player, ConstTask.TASK_12_0);
        // }
    }

    // kiểm tra hoàn thành nhiệm vụ lấy item từ rương
    public void checkDoneTaskGetItemBox(Player player) {
        if (!player.isBoss && !player.isPet) {
            doneTask(player, ConstTask.TASK_0_3);
        }
    }

    // kiểm tra hoàn thành nhiệm vụ sức mạnh
    public void checkDoneTaskPower(Player player, long power) {
        if (!player.isBoss && !player.isPet) {
            if (power >= 16000) {
                doneTask(player, ConstTask.TASK_7_0);
            }
            if (power >= 40000) {
                doneTask(player, ConstTask.TASK_8_0);
            }
            if (power >= 200000) {
                doneTask(player, ConstTask.TASK_14_0);
            }
            if (power >= 500000) {
                doneTask(player, ConstTask.TASK_15_0);
            }
            if (power >= 1500000) {
                doneTask(player, ConstTask.TASK_17_0);
            }
            if (power >= 5000000) {
                doneTask(player, ConstTask.TASK_18_0);
            }
            if (power >= 50000000) {
                doneTask(player, ConstTask.TASK_20_0);
            }
            if (power >= 15000000) {
                doneTask(player, ConstTask.TASK_19_0);
            }
        }
    }

    // kiểm tra hoàn thành nhiệm vụ khi player sử dụng tiềm năng
    public void checkDoneTaskUseTiemNang(Player player) {
        if (!player.isBoss && !player.isPet) {
            doneTask(player, ConstTask.TASK_3_0);
        }
    }

    // kiểm tra hoàn thành nhiệm vụ khi vào map nào đó
    public void checkDoneTaskGoToMap(Player player, Zone zoneJoin) {
        if (!player.isBoss && !player.isPet && !player.isMiniPet) {
            switch (zoneJoin.map.mapId) {
                case 39:
                case 40:
                case 41:
                    if (player.location.x >= 635) {
                        doneTask(player, ConstTask.TASK_0_0);
                    }
                    break;
                case 21:
                case 22:
                case 23:
                    doneTask(player, ConstTask.TASK_0_1);
                    break;
                case 47:
                    doneTask(player, ConstTask.TASK_8_3);
                    break;
                case 93:
                    doneTask(player, ConstTask.TASK_25_0);
                    break;
                case 104:
                    doneTask(player, ConstTask.TASK_26_0);
                    break;
                case 97:
                    doneTask(player, ConstTask.TASK_27_0);
                    break;
                case 100:
                    doneTask(player, ConstTask.TASK_28_0);
                    break;
                case 103:
                    doneTask(player, ConstTask.TASK_29_2);
                case 114:
                    doneTask(player, ConstTask.TASK_30_0);
                    break;
                case 46:
                    doneTask(player, ConstTask.TASK_9_2);
                    break;
            }
        }
    }

    // kiểm tra hoàn thành nhiệm vụ khi nhặt item
    public void checkDoneTaskPickItem(Player player, ItemMap item) {
        if (!player.isBoss && !player.isPet && item != null) {
            switch (item.itemTemplate.id) {
                case 73: // đùi gà
                    doneTask(player, ConstTask.TASK_2_0);
                    break;
                case 78: // em bé
                    doneTask(player, ConstTask.TASK_3_1);
                    Service.gI().sendFlagBag(player);
                    break;
                case 20:
                    doneTask(player, ConstTask.TASK_8_1);
                    break;
                case 85:
                    doneTask(player, ConstTask.TASK_14_1);
                    break;
                case 380:
                    doneTask(player, ConstTask.TASK_29_1);
                    break;

            }
        }
    }

    // //kiểm tra hoàn thành nhiệm vụ kết bạn
    public void checkDoneTaskMakeFriend(Player player, Player friend) {
    }

    // kiểm tra hoàn thành nhiệm vụ khi xác nhận menu npc nào đó
    public void checkDoneTaskConfirmMenuNpc(Player player, Npc npc, byte select) {
        if (!player.isBoss && !player.isPet) {
            switch (npc.tempId) {
                case ConstNpc.DAU_THAN:
                    switch (player.iDMark.getIndexMenu()) {
                        case ConstNpc.MAGIC_TREE_NON_UPGRADE_LEFT_PEA:
                        case ConstNpc.MAGIC_TREE_NON_UPGRADE_FULL_PEA:
                            if (select == 0) {
                                doneTask(player, ConstTask.TASK_0_4);
                            }
                    }
                    break;
            }
        }
    }

    public void checkDoneTaskKillPlayer(Player player) {
        doneTask(player, ConstTask.TASK_16_0);
    }

    // kiểm tra hoàn thành nhiệm vụ khi tiêu diệt được boss
    public void checkDoneTaskKillBoss(Player player, Boss boss) {
        if (player != null && !player.isBoss && !player.isPet) {
            switch ((int) boss.id) {
                case BossFactory.TRUNG_UY_TRANG:
                    doneTask(player, ConstTask.TASK_19_1);
                    break;
                case BossFactory.KUKU:
                    doneTask(player, ConstTask.TASK_21_0);
                    break;
                case BossFactory.MAP_DAU_DINH:
                    doneTask(player, ConstTask.TASK_21_1);
                    break;
                case BossFactory.RAMBO:
                    doneTask(player, ConstTask.TASK_21_2);
                    break;
                case BossFactory.SO4:
                    doneTask(player, ConstTask.TASK_22_0);
                    break;
                case BossFactory.SO3:
                    doneTask(player, ConstTask.TASK_22_1);
                    break;
                case BossFactory.SO1:
                    doneTask(player, ConstTask.TASK_22_2);
                    break;
                case BossFactory.TIEU_DOI_TRUONG:
                    doneTask(player, ConstTask.TASK_22_3);
                    break;
                case BossFactory.FIDE_DAI_CA_1:
                    doneTask(player, ConstTask.TASK_23_0);
                    break;
                case BossFactory.FIDE_DAI_CA_2:
                    doneTask(player, ConstTask.TASK_23_1);
                    break;
                case BossFactory.FIDE_DAI_CA_3:
                    doneTask(player, ConstTask.TASK_23_2); //
                    break;
                case BossFactory.ANDROID_19:
                    doneTask(player, ConstTask.TASK_25_1);
                    break;
                case BossFactory.ANDROID_20:
                    doneTask(player, ConstTask.TASK_25_2);
                    break;
                case BossFactory.ANDROID_15:
                    doneTask(player, ConstTask.TASK_26_1);
                    break;
                case BossFactory.ANDROID_14:
                    doneTask(player, ConstTask.TASK_26_2);
                    break;
                case BossFactory.ANDROID_13:
                    doneTask(player, ConstTask.TASK_26_3);
                    break;
                case BossFactory.POC:
                    doneTask(player, ConstTask.TASK_27_2);
                    break;
                case BossFactory.PIC:
                    doneTask(player, ConstTask.TASK_27_1);
                    break;
                case BossFactory.KINGKONG:
                    doneTask(player, ConstTask.TASK_27_3);
                    break;
                case BossFactory.XEN_BO_HUNG_1:
                    doneTask(player, ConstTask.TASK_28_1);
                    break;
                case BossFactory.XEN_BO_HUNG_2:
                    doneTask(player, ConstTask.TASK_28_2);
                    break;
                case BossFactory.XEN_BO_HUNG_HOAN_THIEN:
                    doneTask(player, ConstTask.TASK_28_3);
                    break;
                case BossFactory.XEN_CON:
                case BossFactory.XEN_CON_1:
                case BossFactory.XEN_CON_2:
                case BossFactory.XEN_CON_3:
                case BossFactory.XEN_CON_4:
                case BossFactory.XEN_CON_5:
                case BossFactory.XEN_CON_6:
                    doneTask(player, ConstTask.TASK_29_3);
                    break;
                case BossFactory.SIEU_BO_HUNG:
                    doneTask(player, ConstTask.TASK_29_4);
                    break;
                case BossFactory.DRABULA_TANG1:
                    doneTask(player, ConstTask.TASK_30_1);
                    break;
                case BossFactory.BUIBUI_TANG2:
                    doneTask(player, ConstTask.TASK_30_2);
                    break;
                case BossFactory.BUIBUI_TANG3:
                    doneTask(player, ConstTask.TASK_30_3);
                    break;
                case BossFactory.YACON_TANG4:
                    doneTask(player, ConstTask.TASK_30_4);
                    break;
                case BossFactory.DRABULA_TANG5:
                    doneTask(player, ConstTask.TASK_30_5);
                    break;
                case BossFactory.MABU_MAP:
                    doneTask(player, ConstTask.TASK_30_6);
                    break;
            }
        }
    }

    // kiểm tra hoàn thành nhiệm vụ khi giết được quái
    public void checkDoneTaskKillMob(Player player, Mob mob) {
        if (!player.isBoss && !player.isPet) {
            switch (mob.tempId) {
                case ConstMob.MOC_NHAN:
                    doneTask(player, ConstTask.TASK_1_0);
                    doneTask(player, ConstTask.TASK_5_0);
                    doneTask(player, ConstTask.TASK_6_0);
                    break;
                case ConstMob.KHUNG_LONG_ME:
                    doneTask(player, ConstTask.TASK_4_0);
                    break;
                case ConstMob.LON_LOI_ME:
                    doneTask(player, ConstTask.TASK_4_1);
                    break;
                case ConstMob.QUY_DAT_ME:
                    doneTask(player, ConstTask.TASK_4_2);
                    break;
                case ConstMob.THAN_LAN_BAY:
                case ConstMob.PHI_LONG:
                case ConstMob.QUY_BAY:
                    doneTask(player, ConstTask.TASK_7_1);
                    break;

                case ConstMob.HEO_RUNG:
                case ConstMob.HEO_DA_XANH:
                case ConstMob.HEO_XAYDA:
                    if (player.clan != null) {
                        List<Player> list = new ArrayList<>();
                        List<Player> playersMap = player.zone.getPlayers();
                        for (Player pl : playersMap) {
                            if (pl != null && pl.clan != null && pl.clan.equals(player.clan)) {
                                list.add(pl);
                            }
                        }
                        if (list.size() >= NMEMBER_DO_TASK_TOGETHER) {
                            for (Player pl : list) {
                                switch (mob.tempId) {
                                    case ConstMob.HEO_RUNG:
                                        doneTask(pl, ConstTask.TASK_13_0);
                                        break;
                                    case ConstMob.HEO_DA_XANH:
                                        doneTask(pl, ConstTask.TASK_13_1);
                                        break;
                                    case ConstMob.HEO_XAYDA:
                                        doneTask(pl, ConstTask.TASK_13_2);
                                        break;
                                }
                            }
                        }
                    }
                    break;
                case ConstMob.BULON:
                case ConstMob.UKULELE:
                case ConstMob.QUY_MAP:
                    if (player.clan != null) {
                        List<Player> list = new ArrayList<>();
                        List<Player> playersMap = player.zone.getPlayers();
                        for (Player pl : playersMap) {
                            if (pl != null && pl.clan != null && pl.clan.equals(player.clan)) {
                                list.add(pl);
                            }
                        }
                        if (list.size() >= NMEMBER_DO_TASK_TOGETHER) {
                            for (Player pl : list) {
                                switch (mob.tempId) {
                                    case ConstMob.BULON:
                                        doneTask(pl, ConstTask.TASK_15_1);
                                        break;
                                    case ConstMob.UKULELE:
                                        doneTask(pl, ConstTask.TASK_15_2);
                                        break;
                                    case ConstMob.QUY_MAP:
                                        doneTask(pl, ConstTask.TASK_15_3);
                                        break;
                                }
                            }
                        }
                    }
                    break;
                case ConstMob.TAMBOURINE:
                    doneTask(player, ConstTask.TASK_17_2);
                    break;
                case ConstMob.DRUM:
                    doneTask(player, ConstTask.TASK_17_3);
                    break;
                case ConstMob.AKKUMAN:
                    doneTask(player, ConstTask.TASK_17_1);
                    break;
                case ConstMob.NAPPA:
                    doneTask(player, ConstTask.TASK_20_1);
                    break;
                case ConstMob.SOLDIER:
                    doneTask(player, ConstTask.TASK_20_2);
                    break;
                case ConstMob.APPULE:
                    doneTask(player, ConstTask.TASK_20_3);
                    break;
                case ConstMob.RASPBERRY:
                    doneTask(player, ConstTask.TASK_20_4);
                    break;
                case ConstMob.THAN_LAN_XANH:
                    doneTask(player, ConstTask.TASK_20_5);
                    break;
                case ConstMob.XEN_CON_CAP_1:
                case ConstMob.XEN_CON_CAP_3:
                case ConstMob.XEN_CON_CAP_5:
                case ConstMob.XEN_CON_CAP_8:
                    if (player.clan != null) {
                        List<Player> list = new ArrayList<>();
                        List<Player> playersMap = player.zone.getPlayers();
                        for (Player pl : playersMap) {
                            if (pl != null && pl.clan != null && pl.clan.equals(player.clan)) {
                                list.add(pl);
                            }
                        }
                        if (list.size() >= NMEMBER_DO_TASK_TOGETHER) {
                            for (Player pl : list) {
                                switch (mob.tempId) {
                                    case ConstMob.XEN_CON_CAP_1:
                                        doneTask(player, ConstTask.TASK_24_4);
                                        break;
                                    case ConstMob.XEN_CON_CAP_3:
                                        doneTask(player, ConstTask.TASK_25_3);
                                        break;
                                    case ConstMob.XEN_CON_CAP_5:
                                        doneTask(player, ConstTask.TASK_27_4);
                                        break;
                                    case ConstMob.XEN_CON_CAP_8:
                                        doneTask(player, ConstTask.TASK_28_4);
                                        break;
                                }
                            }
                        }
                    }
            }
        }
    }

    // xong nhiệm vụ nào đó
    public boolean doneTask(Player player, int idTaskCustom) {
        if (TaskService.gI().isCurrentTask(player, idTaskCustom)) {
            this.addDoneSubTask(player, 1);
            switch (idTaskCustom) {
                // --------------------------------------------------------------
                case ConstTask.TASK_0_0:
                    NpcService.gI().createTutorial(player, -1, transformName(player, "Làm tốt lắm..\n"
                            + "Bây giờ bạn hãy vào nhà ông %2 bên phải để nhận nhiệm vụ mới nhé"));
                    break;
                case ConstTask.TASK_0_1:
                    NpcService.gI().createTutorial(player, -1, transformName(player, "Ông %2 đang đứng đợi kìa\n"
                            + "Hãy nhấn 2 lần vào để nói chuyện"));
                    break;
                case ConstTask.TASK_0_2:
                    npcSay(player, ConstTask.NPC_NHA,
                            "Con vừa đi đâu về đó?\n"
                                    + "Con hãy đến rương đồ để lấy rađa..\n"
                                    + "..sau đó thu hoạch hết đậu trên cây đậu thần đằng kia!");
                    break;
                case ConstTask.TASK_0_3:
                    break;
                case ConstTask.TASK_0_4:
                    break;
                case ConstTask.TASK_0_5:
                    npcSay(player, ConstTask.NPC_NHA,
                            "Tốt lắm, rađa sẽ giúp con thấy được lượng máu và thể lực ở bên góc trái\n"
                                    + "Bây giờ con hãy đi luyện tập\n"
                                    + "Con hãy ra %1, ở đó có những con mộc nhân cho con luyện tập dó\n"
                                    + "Hãy đốn ngã 5 con mộc nhân cho ông");
                    break;
                case ConstTask.TASK_1_0:
                case ConstTask.TASK_5_0:
                case ConstTask.TASK_6_0:
                    if (isCurrentTask(player, idTaskCustom)) {
                        Service.gI().sendThongBao(player, "Bạn đánh được "
                                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count + "/"
                                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).maxCount
                                + " mộc nhân");
                    } else {
                        if (player.gender == 0) {
                            Service.gI().sendThongBao(player,
                                    "Chúc mừng bạn đã hoàn thành nhiệm\nvụ. Nào, bây giờ bạn\n    có thể gặp ông\nGôhan để báo cáo rồi!");
                        } else if (player.gender == 1) {
                            Service.gI().sendThongBao(player,
                                    "Chúc mừng bạn đã hoàn thành nhiệm\nvụ. Nào, bây giờ bạn\n    có thể gặp ông\nMoori để báo cáo rồi!");
                        } else {
                            Service.gI().sendThongBao(player,
                                    "Chúc mừng bạn đã hoàn thành nhiệm\nvụ. Nào, bây giờ bạn\n    có thể gặp ông\nParagus để báo cáo rồi!");
                        }
                    }
                    break;
                case ConstTask.TASK_1_1:
                    npcSay(player, ConstTask.NPC_NHA,
                            "Thể lực của con cũng khá tốt\n"
                                    + "Con à, dạo gần đây dân làng của chúng ta gặp phải vài chuyện\n"
                                    + "Bên cạnh làng ta đột nhiên xuất hiện lũ quái vật\n"
                                    + "Nó tàn sát dân làng và phá hoại nông sản làng ta\n"
                                    + "Con hãy tìm đánh chúng và đem về đây 10 cái đùi gà, 2 ông cháu mình sẽ để dành ăn dần\n"
                                    + "Đây là tấm bản đồ của vùng này, con hãy xem để tìm đến %3\n"
                                    + "Con có thể sử dụng đậu thần khi hết HP hoặc KI, bằng cách nhấn vào nút có hình trái tim "
                                    + "bên góc phải dưới màn hình\n"
                                    + "Nhanh lên, ông đói lắm rồi");
                    break;
                // --------------------------------------------------------------
                case ConstTask.TASK_2_0:
                    if (isCurrentTask(player, idTaskCustom)) {
                        Service.gI().sendThongBao(player, "Bạn thu thập được "
                                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count + "/"
                                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).maxCount
                                + " đùi gà");
                    } else {
                        if (player.gender == 0) {
                            Service.gI().sendThongBao(player,
                                    "Chúc mừng bạn đã hoàn thành nhiệm\nvụ. Nào, bây giờ bạn\n    có thể gặp ông\nGôhan để báo cáo rồi!");
                        } else if (player.gender == 1) {
                            Service.gI().sendThongBao(player,
                                    "Chúc mừng bạn đã hoàn thành nhiệm\nvụ. Nào, bây giờ bạn\n    có thể gặp ông\nMoori để báo cáo rồi!");
                        } else {
                            Service.gI().sendThongBao(player,
                                    "Chúc mừng bạn đã hoàn thành nhiệm\nvụ. Nào, bây giờ bạn\n    có thể gặp ông\nParagus để báo cáo rồi!");
                        }
                    }
                    break;
                case ConstTask.TASK_2_1:
                    try {
                        InventoryService.gI().subQuantityItemsBag(player,
                                InventoryService.gI().findItemBagByTemp(player, 73), 10);
                    } catch (Exception ex) {
                    }
                    InventoryService.gI().sendItemBags(player);
                    Service.gI().dropItemMapForMe(player, player.zone.getItemMapByTempId(74));
                    npcSay(player, ConstTask.NPC_NHA,
                            "Tốt lắm, đùi gà đây rồi, haha. Ông sẽ nướng tại đống lửa gần kia con có thể ăn bất cứ lúc nào nếu muốn\n"
                                    + "À cháu này, vừa nãy ông có nghe thấy 1 tiếng động lớn, hình như có 1 vật thể rơi tại %5, con hãy đến kiểm tra xem\n"
                                    + "Con cũng có thể dùng tiềm năng bản thân để nâng HP, KI hoặc sức đánh");
                    break;
                // --------------------------------------------------------------
                case ConstTask.TASK_3_0:
                    break;
                case ConstTask.TASK_3_1:

                    Service.gI().sendThongBao(player, "Hãy bế cậu bé về\nnhà!");

                    break;
                case ConstTask.TASK_3_2:
                    try {
                        InventoryService.gI().subQuantityItemsBag(player,
                                InventoryService.gI().findItemBagByTemp(player, 78), 1);
                    } catch (Exception ex) {
                    }
                    InventoryService.gI().sendItemBags(player);
                    Service.gI().sendFlagBag(player);
                    npcSay(player, ConstTask.NPC_NHA,
                            "Có em bé trong phi thuyền rơi xuống à, ông cứ tưởng là sao băng chứ\n"
                                    + "Ông sẽ đặt tên cho em nó là Goku, từ giờ nó sẽ là thành viên trong gia đình ta\n"
                                    + "Nãy ông mới nhận được tin có bầy mãnh thú xuất hiện tại Trạm phi thuyền\n"
                                    + "Bọn chúng vừa đổ bộ xuống trái đất để trả thù việc con sát hại con chúng\n"
                                    + "Con hãy đi tiêu diệt chúng để giúp dân làng tại đó luôn nhé");
                    break;
                // --------------------------------------------------------------
                case ConstTask.TASK_4_0:
                    if (isCurrentTask(player, idTaskCustom)) {
                        Service.gI().sendThongBao(player, "Bạn đánh được "
                                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count + "/"
                                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).maxCount
                                + transformName(player, " khủng long mẹ"));
                    } else {
                        Service.gI().sendThongBao(player, "Tiếp theo là lợn lòi mẹ");
                    }
                    break;
                case ConstTask.TASK_4_1:
                    if (isCurrentTask(player, idTaskCustom)) {
                        Service.gI().sendThongBao(player, "Bạn đánh được "
                                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count + "/"
                                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).maxCount
                                + transformName(player, " lợn lòi mẹ"));
                    } else {
                        Service.gI().sendThongBao(player, "Tiếp theo là quỷ đất mẹ");
                    }
                    break;
                case ConstTask.TASK_4_2:
                    if (isCurrentTask(player, idTaskCustom)) {
                        Service.gI().sendThongBao(player, "Bạn đánh được "
                                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count + "/"
                                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).maxCount
                                + transformName(player, " quỷ đất mẹ"));
                    } else {
                        if (player.gender == 0) {
                            Service.gI().sendThongBao(player, "Xong việc rồi, về báo\n cáo với ông Gôhan nào!");
                        } else if (player.gender == 1) {
                            Service.gI().sendThongBao(player, "Xong việc rồi, về báo\n cáo với ông Moori nào!");
                        } else {
                            Service.gI().sendThongBao(player, "Xong việc rồi, về báo\n cáo với ông Paragus nào!");
                        }
                    }
                    break;
                case ConstTask.TASK_4_3:
                    npcSay(player, ConstTask.NPC_NHA,
                            "Ông rất tự hào về con\n"
                                    + "Ông cho con cuốn bí kíp này để nâng cao võ học\n"
                                    + "Hãy dùng sức mạnh của mình trừ gian diệt ác bảo vệ dân lành con nhé\n"
                                    + "Bây giờ con hãy đi tập luyện đi, khi nào mạnh hơn thì quay về đây ông giao cho nhiệm vụ mới\n"
                                    + "Đi đi..");
                    break;
                // --------------------------------------------------------------
                case ConstTask.TASK_7_0:
                    break;
                case ConstTask.TASK_7_1:
                    if (isCurrentTask(player, idTaskCustom)) {
                        Service.gI().sendThongBao(player, "Bạn đánh được "
                                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count + "/"
                                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).maxCount
                                + transformName(player, " %9"));
                    } else {
                        if (player.gender == 0) {
                            Service.gI().sendThongBao(player,
                                    "Bạn vừa giải cứu được\nBunma hãy về Làng Aru và nói\nchuyện với Bunma");
                        } else if (player.gender == 1) {
                            Service.gI().sendThongBao(player,
                                    "Bạn vừa giải cứu được\nDende hãy về Làng Mori và nói\nchuyện với Dende");
                        } else {
                            Service.gI().sendThongBao(player,
                                    "Bạn vừa giải cứu được\nAppule hãy về Làng Kakarot và nói\nchuyện với Appule");
                        }
                    }
                    break;
                case ConstTask.TASK_7_2:
                    npcSay(player, ConstTask.NPC_SHOP_LANG,
                            "Cảm ơn ngươi đã cứu ta. Ta sẽ sẵn sàng phục vụ nếu ngươi cần mua vật dụng");
                    if (player.gender == 0) {
                        Service.gI().sendThongBao(player, "Xong việc rồi, về báo\n cáo với ông Gôhan nào!");
                    } else if (player.gender == 1) {
                        Service.gI().sendThongBao(player, "Xong việc rồi, về báo\n cáo với ông Moori nào!");
                    } else {
                        Service.gI().sendThongBao(player, "Xong việc rồi, về báo\n cáo với ông Paragus nào!");
                    }
                    break;
                case ConstTask.TASK_7_3:
                    npcSay(player, ConstTask.NPC_NHA,
                            "Tốt lắm, con làm ta bất ngờ đấy. Bây giờ hãy đi lấy lại ngọc đem về đây cho ta");
                    break;
                // --------------------------------------------------------------------
                case ConstTask.TASK_8_0:
                    break;
                case ConstTask.TASK_8_1:
                    Service.gI().sendThongBao(player,
                            "Bạn đã tìm thấy ngọc rồng 7 sao rồi, hãy chạm nhanh 2 lần vào đối tượng để lấy");
                    if (player.gender == 0) {
                        Service.gI().sendThongBao(player,
                                "Tìm thấy viên ngọc rồng 7 sao rồi, đem về cho ông GôHan nào!");
                    } else if (player.gender == 1) {
                        Service.gI().sendThongBao(player,
                                "Tìm thấy viên ngọc rồng 7 sao rồi, đem về cho ông Moori nào!");
                    } else {
                        Service.gI().sendThongBao(player,
                                "Tìm thấy viên ngọc rồng 7 sao rồi, đem về cho ông Paragus nào!");
                    }
                    break;
                case ConstTask.TASK_8_2:
                    npcSay(player, ConstTask.NPC_NHA,
                            "Tốt lắm, con làm ta bất ngờ đấy. Bây giờ hãy đi đến tháp Karin");
                    break;
                case ConstTask.TASK_8_3:
                    break;
                // -----------------------------------------------------------------------
                case ConstTask.TASK_9_0:
                    npcSay(player, ConstNpc.BO_MONG,
                            "Hắn sắp đến đây, hãy giúp ta tiêu diệt hắn\nHãy giúp ta tiêu diệt hắn");
                    Service.getInstance().sendThongBao(player, "Hình như có người đang tới đây");
                    Service.getInstance().callTau77(player);
                    break;
                case ConstTask.TASK_9_2:
                    Service.getInstance().sendThongBao(player, "Hãy nói chuyện với Thần Mèo Karin");
                    break;
                case ConstTask.TASK_9_3:
                    npcSay(player, ConstNpc.THAN_MEO_KARIN, "Có phải ngươi vừa chiến đấu với Tàu Pảy Pảy không?\n"
                            + "Ta tuy mù nhưng có thể đọc được ý nghĩ của ngươi\n"
                            + "Ngươi chưa phải là đối thủ của hắn đâu\n"
                            + "Tìm ta là đúng rồi, để ta dạy ngươi vài chiêu, nhưng phải chăm chỉ mới học được đấy nhé\n"
                            + "Ngươi sẵn sàng chưa");
                    break;
                case ConstTask.TASK_10_2:
                    npcSay(player, ConstNpc.BO_MONG,
                            "Cám ơn đã giúp chúng tôi. Xin hãy nhận viên ngọc rồng 6 sao này như món quà cám ơn");
                    Item ngocrong6sao = ItemService.gI().createNewItem((short) 19);
                    InventoryService.gI().addItemBag(player, ngocrong6sao, 9999);
                    Service.getInstance().sendThongBao(player, "Bạn vừa nhận được Ngọc Rồng 6 sao");
                    if (player.gender == 0) {
                        Service.getInstance().sendThongBao(player,
                                "Viên ngọc rồng 6 sao đây rồi, đem về cho ông Gôhan nào!");
                    } else if (player.gender == 1) {
                        Service.getInstance().sendThongBao(player,
                                "Viên ngọc rồng 6 sao đây rồi, đem về cho ông Moori nào!");
                    } else {
                        Service.getInstance().sendThongBao(player,
                                "Viên ngọc rồng 6 sao đây rồi, đem về cho ông Paragus nào!");
                    }
                    break;
                case ConstTask.TASK_10_3:
                    npcSay(player, ConstTask.NPC_NHA,
                            "Ta thật sự hãnh diện về con. Giờ ta không còn gì để dạy cho con, nhưng có người còn giỏi hơn ta\n"
                                    + "Đó là sư phụ của ta "
                                    + (player.gender == ConstPlayer.TRAI_DAT ? "Quy Lão Kame"
                                            : (player.gender == ConstPlayer.NAMEC ? "Trưởng lão Guru" : "Vua Vegeta"))
                                    + ", ngài rất thích đọc truyện Đôrêmon, con hãy đem tới cho ngài\n"
                                    + "Nhất định ngài sẽ thu nhận con làm đệ tử, con ráng học thành tài nhé");
                    break;
                // -----------------------------------------------------------------------
                case ConstTask.TASK_11_0:
                    npcSay(player, ConstTask.NPC_QUY_LAO,
                            "Ta không nhận đệ tử đâu. Ồ con tặng ta truyện Doremon hả, thôi được nhưng con phải cố gắng luyện tập đó nhé. Hãy gia nhập một bang hội để luyện tập, sau đó quay lại đây gặp ta");
                    break;
                case ConstTask.TASK_11_1:
                    break;
                // ---------------------------------------------------------------------------
                case ConstTask.TASK_12_0:
                    break;
                case ConstTask.TASK_12_1:
                    npcSay(player, ConstTask.NPC_QUY_LAO,
                            "Con hãy cùng các thành viên trong bang tiêu diệt cho ta 30 con Heo rừng, 30 con Heo da xanh và 30 con Heo Xayda");
                    break;

                // =============================================================================
                case ConstTask.TASK_13_0:
                    if (isCurrentTask(player, idTaskCustom)) {
                        Service.gI().sendThongBao(player, "Bạn đánh được "
                                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count + "/"
                                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).maxCount
                                + transformName(player, " Heo rừng"));
                    } else {
                        Service.gI().sendThongBao(player, "Tiếp theo là Heo da xanh");
                    }
                    break;
                case ConstTask.TASK_13_1:
                    if (isCurrentTask(player, idTaskCustom)) {
                        Service.gI().sendThongBao(player, "Bạn đánh được "
                                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count + "/"
                                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).maxCount
                                + transformName(player, " Heo da xanh"));
                    } else {
                        Service.gI().sendThongBao(player, "Tiếp theo là Heo xayda");
                    }
                    break;
                case ConstTask.TASK_13_2:
                    if (isCurrentTask(player, idTaskCustom)) {
                        Service.gI().sendThongBao(player, "Bạn đánh được "
                                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count + "/"
                                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).maxCount
                                + transformName(player, " Heo xayda"));
                    } else {
                        if (player.gender == 0) {
                            Service.gI().sendThongBao(player, "Nhiệm vụ hoàn\nthành, báo cáo với\nQuy Lão nào!");
                        } else if (player.gender == 1) {
                            Service.gI().sendThongBao(player,
                                    "Nhiệm vụ hoàn\nthành, báo cáo với\nTrưởng Lão Guru nào!");
                        } else {
                            Service.gI().sendThongBao(player, "Nhiệm vụ hoàn\nthành, báo cáo với\nVua Vegeta nào!");
                        }
                    }
                    break;
                case ConstTask.TASK_13_3:
                    npcSay(player, ConstTask.NPC_QUY_LAO,
                            "Bang của con rất có tinh thần đồng đội, con hãy cùng các thành viên luyện tập chăm chỉ để thành tài nhé");
                    break;
                // ===================================================================================
                case ConstTask.TASK_14_0:
                    break;
                case ConstTask.TASK_14_1:
                    break;
                case ConstTask.TASK_14_2:
                    npcSay(player, ConstTask.NPC_QUY_LAO,
                            "Con đã tìm thấy truyện Doremon tập 2 rồi à, mau đưa cho ta nào");
                    break;
                // =====================================================================================
                case ConstTask.TASK_15_1:
                    if (isCurrentTask(player, idTaskCustom)) {
                        Service.gI().sendThongBao(player, "Bạn đánh được "
                                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count + "/"
                                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).maxCount
                                + transformName(player, " Bulon"));
                    } else {
                        Service.gI().sendThongBao(player, "Tiếp theo là Ukulele");
                    }
                    break;
                case ConstTask.TASK_15_2:
                    if (isCurrentTask(player, idTaskCustom)) {
                        Service.gI().sendThongBao(player, "Bạn đánh được "
                                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count + "/"
                                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).maxCount
                                + transformName(player, " Ukulele"));
                    } else {
                        Service.gI().sendThongBao(player, "Tiếp theo là Quỷ mập");
                    }
                    break;
                case ConstTask.TASK_15_3:
                    if (isCurrentTask(player, idTaskCustom)) {
                        Service.gI().sendThongBao(player, "Bạn đánh được "
                                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count + "/"
                                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).maxCount
                                + transformName(player, " Quỷ mập"));
                    } else {
                        if (player.gender == 0) {
                            Service.gI().sendThongBao(player, "Nhiệm vụ hoàn\nthành, báo cáo với\nQuy Lão nào!");
                        } else if (player.gender == 1) {
                            Service.gI().sendThongBao(player,
                                    "Nhiệm vụ hoàn\nthành, báo cáo với\nTrưởng Lão Guru nào!");
                        } else {
                            Service.gI().sendThongBao(player, "Nhiệm vụ hoàn\nthành, báo cáo với\nVua Vegeta nào!");
                        }
                    }
                    break;
                case ConstTask.TASK_15_4:
                    npcSay(player, ConstTask.NPC_QUY_LAO, "Con và bang hội làm rất tốt, ta có quà dành cho con");
                    break;
                // =====================================================================
                case ConstTask.TASK_17_0:
                    Service.gI().sendThongBao(player, "Mau đến XayDa\nđánh bại\nAkkuman tại\nThành phố Vegeta");
                    break;
                case ConstTask.TASK_17_1:
                    Service.gI().sendThongBao(player, "Mau đến Trái Đất\nđánh bại\nTambourine tại\nĐông Karin");
                    break;
                case ConstTask.TASK_17_2:
                    Service.gI().sendThongBao(player, "Mau đến Na mếc\nđánh bại\nDrum tại\nThung lũng Namếc");
                case ConstTask.TASK_17_3:
                    if (player.gender == 0) {
                        Service.gI().sendThongBao(player, "Báo cáo với\nQuy Lão nào!");
                    } else if (player.gender == 1) {
                        Service.gI().sendThongBao(player, "Báo cáo với\nTrưởng Lão Guru nào!");
                    } else {
                        Service.gI().sendThongBao(player, "Báo cáo với\nVua Vegeta nào!");
                    }
                    break;
                case ConstTask.TASK_17_4:
                    npcSay(player, ConstTask.NPC_QUY_LAO, "Con làm rất tốt, ta có quà dành cho con");
                    break;
                // =========================================================================
                case ConstTask.TASK_19_1:
                    if (player.gender == 0) {
                        Service.gI().sendThongBao(player, "Báo cáo với\nQuy Lão nào!");
                    } else if (player.gender == 1) {
                        Service.gI().sendThongBao(player, "Báo cáo với\nTrưởng Lão Guru nào!");
                    } else {
                        Service.gI().sendThongBao(player, "Báo cáo với\nVua Vegeta nào!");
                    }
                    break;
                case ConstTask.TASK_19_2:
                    npcSay(player, ConstTask.NPC_QUY_LAO,
                            "Con làm rất tốt, Trung Úy Trắng đã bị tiêu diệt. Ta có quà dành cho con");
                    // ==========================================================================
                case ConstTask.TASK_20_7:
                    npcSay(player, ConstTask.NPC_QUY_LAO, "Tốt lắm, giờ con hãy đi tiêu diệt lũ đệ tử của Fide cho ta");
                    break;
                // ==========================================================================
                case ConstTask.TASK_21_0:
                    Service.gI().sendThongBao(player, "Tiếp theo là tìm diệt Mập Đầu Đinh");
                    break;
                case ConstTask.TASK_21_1:
                    Service.gI().sendThongBao(player, "Cuối cùng là tìm diệt Rambo");
                    break;
                case ConstTask.TASK_21_2:
                    if (player.gender == 0) {
                        Service.gI().sendThongBao(player,
                                "Chúc mừng bạn, bọn\nđệ tử của Fide đã bị\ntiêu diệt, hãy báo\ncáo với Quy Lão nào!");
                    } else if (player.gender == 1) {
                        Service.gI().sendThongBao(player,
                                "Chúc mừng bạn, bọn\nđệ tử của Fide đã bị\ntiêu diệt, hãy báo\ncáo với Trưởng Lão Guru nào!");
                    } else {
                        Service.gI().sendThongBao(player,
                                "Chúc mừng bạn, bọn\nđệ tử của Fide đã bị\ntiêu diệt, hãy báo\ncáo với Vua Vegeta nào!");
                    }
                    break;
                case ConstTask.TASK_21_3:
                    npcSay(player, ConstTask.NPC_QUY_LAO, "Con làm rất tốt, ta có quà dành cho con");
                    break;

                // ==========================================================================
                case ConstTask.TASK_22_0:
                    Service.gI().sendThongBao(player, "Tiếp theo là Số 3");
                    break;
                case ConstTask.TASK_22_1:
                    Service.gI().sendThongBao(player, "Tiếp theo là Số 1");
                    break;
                case ConstTask.TASK_22_2:
                    Service.gI().sendThongBao(player, "Tiếp theo là Tiểu đội trưởng");
                    break;
                case ConstTask.TASK_22_3:
                    if (player.gender == 0) {
                        Service.gI().sendThongBao(player,
                                "Chúc mừng bạn, bọn\ntiểu đội sát thủ của\nFide đã bị tiêu diệt, hãy báo cáo với Quy\nLão nào!");
                    } else if (player.gender == 1) {
                        Service.gI().sendThongBao(player,
                                "Chúc mừng bạn, bọn\ntiểu đội sát thủ của\nFide đã bị tiêu diệt, hãy báo cáo với Trưởng\nLão Guru nào!");
                    } else {
                        Service.gI().sendThongBao(player,
                                "Chúc mừng bạn, bọn\ntiểu đội sát thủ của\nFide đã bị tiêu diệt, hãy báo cáo với Vua\nVegeta nào!");
                    }
                    break;
                case ConstTask.TASK_22_4:
                    npcSay(player, ConstTask.NPC_QUY_LAO, "Rất tốt, bọn Fide đã biết sức mạnh của chúng ta");
                    break;

                // ==========================================================================
                case ConstTask.TASK_23_0:
                    Service.gI().sendThongBao(player, "Hãy tìm diệt Fide cấp 2");
                case ConstTask.TASK_23_1:
                    Service.gI().sendThongBao(player, "Hãy tìm diệt Fide cấp 3");
                case ConstTask.TASK_23_2:
                    if (player.gender == 0) {
                        Service.gI().sendThongBao(player,
                                "Chúc mừng bạn, Fide\nđã bị tiêu diệt, hãy\nbáo cáo với Quy\nLão nào!");
                    } else if (player.gender == 1) {
                        Service.gI().sendThongBao(player,
                                "Chúc mừng bạn, Fide\nđã bị tiêu diệt, hãy\nbáo cáo với Trưởng\nLão Guru nào!");
                    } else {
                        Service.gI().sendThongBao(player,
                                "Chúc mừng bạn, Fide\nđã bị tiêu diệt, hãy\nbáo cáo với Vua\nVegeta nào!");
                    }
                    break;
                case ConstTask.TASK_23_3:
                    npcSay(player, ConstTask.NPC_QUY_LAO, "Rất tốt, bọn Fide đã biết sức mạnh thật sự của chúng ta");
                    break;
                // ==========================================================================
                case ConstTask.TASK_24_0:
                    npcSay(player, ConstTask.NPC_NHA, "Con cố gắng theo %10 học thành tài, đừng lo cho ta.");
                    Service.gI().sendThongBao(player, "Vừa có 1 phi thuyền lạ vừa đáp xuống trái đất, mau đến xem");
                    break;
                case ConstTask.TASK_24_1:
                    npcSay(player, ConstNpc.CALICK,
                            "Chào chú, thực ra cháu không phải là người của thời đại này mà là người của...\n"
                                    + "Tương lai 20 năm sắp tới\n Tên cháu là Ca lích! người Xayda\nCháu đến đây bằng 'Cổ máy thời gian'\n"
                                    + "Bố mẹ cháu vốn là bạn thân của chú\n Họ chính là Ca Đíc và Bunma!\n"
                                    + "Đây là thuốc trợ tim dành cho chú Sôngôku\n nhờ chú đưa cho Quy Lão giùm cháu nhé, cám ơn chú");
                    break;
                case ConstTask.TASK_24_2:
                    npcSay(player, ConstNpc.QUY_LAO_KAME, "Hô hô hô, cám ơn cậu...\n"
                            + "lúc nãy nó trôm mắt kính của ta, cứ tưởng hồn ma bóng quế của nó hiện về chứ");
                    break;
                case ConstTask.TASK_24_3:
                    npcSay(player, ConstNpc.BUNMA_TL, "Cám ơn bạn đã đến giúp chúng tôi\n"
                            + "Chúng tôi bị bọn bọ hung bao vây\n"
                            + "Chúng đông đến hàng trăm con\n"
                            + "Hãy giúp chúng tôi tiêu diệt hết chúng nó.");
                    break;
                case ConstTask.TASK_24_4:
                    if (isCurrentTask(player, idTaskCustom)) {
                        Service.gI().sendThongBao(player, "Bạn đánh được "
                                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count + "/"
                                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).maxCount
                                + transformName(player, " Xên con cấp 1"));
                    } else {
                        Service.gI().sendThongBao(player, "Về báo cáo với Bunma tương lai");
                    }
                    break;
                case ConstTask.TASK_24_5:
                    npcSay(player, ConstNpc.BUNMA_TL, "Cảm ơn cậu đã giải vây cho chúng tôi\n"
                            + "Hãy đến thành phố phía nam, đảo balê hoặc cao nguyên tìm và chặn đánh 2 Rôbốt Sát Thủ\n"
                            + "Cẩn thận vì bọn chúng rất mạnh");
                    break;
                // =============================================================================

                case ConstTask.TASK_25_0:
                    Service.gI().sendThongBao(player, "Tiếp theo là tiêu diệt Android 19");
                    break;
                case ConstTask.TASK_25_1:
                    Service.gI().sendThongBao(player, "Tiếp theo là tiêu diệt Android 20");
                    break;
                case ConstTask.TASK_25_2:
                    if (isCurrentTask(player, idTaskCustom)) {
                        Service.gI().sendThongBao(player, "Bạn đánh được "
                                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count + "/"
                                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).maxCount
                                + transformName(player, " Xên con cấp 3"));
                    } else {
                        Service.gI().sendThongBao(player, "Về báo cáo với Bunma tương lai");
                    }
                    break;
                case ConstTask.TASK_25_4:
                    npcSay(player, ConstNpc.BUNMA_TL, "Số 1 chính là bác học Kôrê\n"
                            + "Ông ta đã tự biến mình thành Rôbốt để được bất tử\n"
                            + "2 tên Rôbốt này không phải là Rôbốt sát thủ mà chúng tôi nói đến\n"
                            + "Có thể quá khứ đã thay đổi từ khi cậu đến đây\n"
                            + "Mau trở về quá khứ xem chuyện gì đã xảy ra");

                    if (isCurrentTask(player, idTaskCustom)) {
                        Service.gI().sendThongBao(player, "Đến sân sau siêu thị");
                    }
                    break;
                // =============================================================
                case ConstTask.TASK_26_0:
                    if (isCurrentTask(player, idTaskCustom)) {
                        Service.gI().sendThongBao(player, "Tìm và tiêu diệt Android 15");
                    }
                    break;
                case ConstTask.TASK_26_1:
                    if (isCurrentTask(player, idTaskCustom)) {
                        Service.gI().sendThongBao(player, "Tìm và tiêu diệt Android 13");
                    }
                    break;
                case ConstTask.TASK_26_2:
                    if (isCurrentTask(player, idTaskCustom)) {
                        Service.gI().sendThongBao(player, "Báo cáo với Bunma tương lai");
                    }
                    break;
                case ConstTask.TASK_26_3:
                    npcSay(player, ConstNpc.BUNMA_TL, "Bác học Kôrê thật sự là thiên tài\n"
                            + "Cả máy tính của ông ta cũng có thể\n"
                            + "tự động tạo ra Rôbốt sát thủ\n"
                            + "2 đứa Rôbốt sát thủ mà chúng tôi nói\n"
                            + "cỡ 17, 18 tuổi, 1 trai 1 gái ăn mặc như cao bồi\n"
                            + "Bề ngoài thấy hiền lành nhưng ra tay cực kì tàn độc\n"
                            + "Cậu phải cẩn thận đừng khinh địch.");
                    if (isCurrentTask(player, idTaskCustom)) {
                        Service.gI().sendThongBao(player, "Đến thành phố, ngọn núi, thung lũng phía Bắc");
                    }
                    break;

                // ====================================================//
                case ConstTask.TASK_27_0:
                    if (isCurrentTask(player, idTaskCustom)) {
                        Service.gI().sendThongBao(player, "Tiêu diệt Píc");
                    }
                    break;
                case ConstTask.TASK_27_1:
                    if (isCurrentTask(player, idTaskCustom)) {
                        Service.gI().sendThongBao(player, "Tiêu diệt Póc");
                    }
                    break;
                case ConstTask.TASK_27_2:
                    if (isCurrentTask(player, idTaskCustom)) {
                        Service.gI().sendThongBao(player, "Tiêu diệt King Kong");
                    }
                    break;
                case ConstTask.TASK_27_3:
                    if (isCurrentTask(player, idTaskCustom)) {
                        Service.gI().sendThongBao(player, "Tiêu diệt 800 xên con cấp 5");
                    }
                    break;
                case ConstTask.TASK_27_4:
                    if (isCurrentTask(player, idTaskCustom)) {
                        Service.gI().sendThongBao(player, "Bạn đánh được "
                                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count + "/"
                                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).maxCount
                                + transformName(player, " Xên con cấp 5"));
                    } else {
                        Service.gI().sendThongBao(player, "Về báo cáo với Bunma tương lai");
                    }
                    break;
                case ConstTask.TASK_27_5:
                    npcSay(player, ConstNpc.BUNMA_TL, "Tôi và Ca Lích vừa phát hiện ra 1 vỏ trứng kì lạ đã nở\n"
                            + "Gần đó còn có vỏ của một con ve sầu rất to vừa lột xác\n"
                            + "Cậu hãy đến thị trấn Ginder tọa độ 213-xyz xem thử\n"
                            + "Tôi nghi ngờ nó là 1 tác phẩm nữa của lão Kôrê\n"
                            + "Cậu cầm lấy cái này, đó là rađa rò tìm Capsule kì bí\n"
                            + "Chúc cậu tìm được vật gì đó thú vị");

                    Service.gI().sendThongBao(player, "Đến thị trận Ginder");
                    break;
                // ============================================================

                case ConstTask.TASK_28_0:
                    Service.gI().sendThongBao(player, "Tiêu diệt Xên bọ hung cấp 1");
                    break;
                case ConstTask.TASK_28_1:
                    Service.gI().sendThongBao(player, "Tiêu diệt Xên bọ hung cấp 2");
                    break;
                case ConstTask.TASK_28_2:
                    Service.gI().sendThongBao(player, "Tiêu diệt Xên bọ hung hoàn thiện");
                    break;
                case ConstTask.TASK_28_3:
                    Service.gI().sendThongBao(player, "Tiêu diệt 700 xên con cấp 8");
                    break;
                case ConstTask.TASK_28_4:
                    if (isCurrentTask(player, idTaskCustom)) {
                        Service.gI().sendThongBao(player, "Bạn đánh được "
                                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count + "/"
                                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).maxCount
                                + transformName(player, " Xên con cấp 8"));
                    } else {
                        Service.gI().sendThongBao(player, "Về báo cáo với Bunma tương lai");
                    }
                    break;
                case ConstTask.TASK_28_5:
                    npcSay(player, ConstNpc.BUNMA_TL, "Hắn sợ chúng ta quá nên bày trò câu giờ đây mà\n"
                            + "Cậu hãy tranh thủ 3 ngày này tập luyện để nâng cao sức mạnh bản thân nhé\n"
                            + "Capsule kì bí không chừng lại có ích\n"
                            + "Hãy thu thập 1 ít để phòng thân");

                    // =============================================================
                case ConstTask.TASK_29_0:
                    npcSay(player, ConstNpc.THAN_MEO_KARIN, "Wow sức mạnh của con giờ không thể đo đếm được nữa rồi\n"
                            + "Nhưng Xên Bọ Hung vẫn còn nhỉnh hơn con 1 tí");
                    Service.gI().sendThongBao(player,
                            "Đến tương lai thu\nthập Capsule kì bí\nnào, nhớ sử dụng\nmáy dò");
                    break;
                case ConstTask.TASK_29_1:
                    Service.gI().sendThongBao(player, "Hãy đến võ đài xên bọ hung");
                    break;
                case ConstTask.TASK_29_2:
                    Service.gI().sendThongBao(player, "Tiêu diệt lũ bọ hung con");
                    break;
                case ConstTask.TASK_29_3:
                    Service.gI().sendThongBao(player, "Hãy tiêu diệt Siêu Bọ Hung");
                    break;
                case ConstTask.TASK_29_4:
                    Service.gI().sendThongBao(player, "Về báo cho Bunma nào");
                    break;
                case ConstTask.TASK_29_5:
                    npcSay(player, ConstNpc.BUNMA_TL, "Chúc mừng cậu đã chiến thắng Siêu Bọ Hung\n"
                            + "Cám ơn cậu rất nhiều\n"
                            + "nếu rảnh rỗi cậu hãy đến đây tìm Capsule kì bí nhá");
                    break;

                // =======================================================//
                case ConstTask.TASK_30_0:
                    Service.gI().sendThongBao(player, "Hạ vua địa ngục\nDrabura");
                    break;
                case ConstTask.TASK_30_1:
                    Service.gI().sendThongBao(player, "Hạ Pui Pui");
                    break;
                case ConstTask.TASK_30_2:
                    Service.gI().sendThongBao(player, "Hạ Pui Pui lần 2");
                    break;
                case ConstTask.TASK_30_3:
                    Service.gI().sendThongBao(player, "Hạ Yacôn");
                    break;
                case ConstTask.TASK_30_4:
                    Service.gI().sendThongBao(player, "Hạ Drabura lần 2");
                    break;
                case ConstTask.TASK_30_5:
                    Service.gI().sendThongBao(player, "Hạ Mabư");
                    break;
                case ConstTask.TASK_30_6:
                    Service.gI().sendThongBao(player, "Báo cáo với Ôsin");
                    break;
            }
            PlayerService.gI().sendInfoHpMpMoney(player);
            return true;
        }
        return false;
    }

    private void npcSay(Player player, int npcId, String text) {
        npcId = transformNpcId(player, npcId);
        text = transformName(player, text);
        int avatar = NpcService.gI().getAvatar(npcId);
        NpcService.gI().createTutorial(player, avatar, text);
    }

    // Thưởng nhiệm vụ
    private void rewardDoneTask(Player player) {
        switch (player.playerTask.taskMain.id) {
            case 0:
                Service.getInstance().addSMTN(player, (byte) 0, 500, false);
                Service.getInstance().addSMTN(player, (byte) 1, 500, false);
                break;
            case 1:
                Service.getInstance().addSMTN(player, (byte) 0, 1000, false);
                Service.getInstance().addSMTN(player, (byte) 1, 1000, false);
                break;
            case 2:
                Service.getInstance().addSMTN(player, (byte) 0, 1200, false);
                Service.getInstance().addSMTN(player, (byte) 1, 1200, false);
                break;
            case 3:
                Service.getInstance().addSMTN(player, (byte) 0, 3000, false);
                Service.getInstance().addSMTN(player, (byte) 1, 3000, false);
                break;
            case 4:
                Service.getInstance().addSMTN(player, (byte) 0, 7000, false);
                Service.getInstance().addSMTN(player, (byte) 1, 7000, false);
                break;
            case 5:
                Service.getInstance().addSMTN(player, (byte) 0, 20000, false);
                Service.getInstance().addSMTN(player, (byte) 1, 20000, false);
                break;
        }
    }

    // vd: pem đc 1 mộc nhân -> +1 mộc nhân vào nv hiện tại
    private void addDoneSubTask(Player player, int numDone) {
        if (player.playerTask.taskMain.id == 4 && player.playerTask.taskMain.index == 3) {
            player.playerTask.taskMain.id = 6;
            player.playerTask.taskMain.index = 0;
            sendNextTaskMain(player);
        }
        if (player.playerTask.taskMain.id == 11 && player.playerTask.taskMain.index == 0) {
            sendNextTaskMain(player);
        }
        player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count += numDone;
        if (player.playerTask.taskMain.subTasks
                .get(player.playerTask.taskMain.index).count >= player.playerTask.taskMain.subTasks
                        .get(player.playerTask.taskMain.index).maxCount) {
            player.playerTask.taskMain.index++;
            if (player.playerTask.taskMain.index >= player.playerTask.taskMain.subTasks.size()) {
                this.sendNextTaskMain(player);
            } else {
                this.sendNextSubTask(player);
            }
        } else {
            this.sendUpdateCountSubTask(player);
        }
    }

    private int transformMapId(Player player, int id) {
        if (id == ConstTask.MAP_NHA) {
            return (short) (player.gender + 21);
        } else if (id == ConstTask.MAP_200) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? 1
                    : (player.gender == ConstPlayer.NAMEC
                            ? 8
                            : 15);
        } else if (id == ConstTask.MAP_VACH_NUI) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? 39
                    : (player.gender == ConstPlayer.NAMEC
                            ? 40
                            : 41);
        } else if (id == ConstTask.MAP_200) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? 2
                    : (player.gender == ConstPlayer.NAMEC
                            ? 9
                            : 16);
        } else if (id == ConstTask.MAP_TTVT) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? 24
                    : (player.gender == ConstPlayer.NAMEC
                            ? 25
                            : 26);
        } else if (id == ConstTask.MAP_QUAI_BAY_600) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? 3
                    : (player.gender == ConstPlayer.NAMEC
                            ? 11
                            : 17);
        } else if (id == ConstTask.MAP_LANG) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? 0
                    : (player.gender == ConstPlayer.NAMEC
                            ? 7
                            : 14);
        } else if (id == ConstTask.MAP_QUY_LAO) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? 5
                    : (player.gender == ConstPlayer.NAMEC
                            ? 13
                            : 20);
        }
        return id;
    }

    private int transformNpcId(Player player, int id) {
        if (id == ConstTask.NPC_NHA) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? ConstNpc.ONG_GOHAN
                    : (player.gender == ConstPlayer.NAMEC
                            ? ConstNpc.ONG_MOORI
                            : ConstNpc.ONG_PARAGUS);
        } else if (id == ConstTask.NPC_TTVT) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? ConstNpc.DR_DRIEF
                    : (player.gender == ConstPlayer.NAMEC
                            ? ConstNpc.CARGO
                            : ConstNpc.CUI);
        } else if (id == ConstTask.NPC_SHOP_LANG) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? ConstNpc.BUNMA
                    : (player.gender == ConstPlayer.NAMEC
                            ? ConstNpc.DENDE
                            : ConstNpc.APPULE);
        } else if (id == ConstTask.NPC_QUY_LAO) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? ConstNpc.QUY_LAO_KAME
                    : (player.gender == ConstPlayer.NAMEC
                            ? ConstNpc.TRUONG_LAO_GURU
                            : ConstNpc.VUA_VEGETA);
        }
        return id;
    }

    // replate %1 %2 -> chữ
    private String transformName(Player player, String text) {
        byte gender = player.gender;

        text = text.replaceAll(ConstTask.TEN_NPC_QUY_LAO, player.gender == ConstPlayer.TRAI_DAT
                ? "Quy Lão Kame"
                : (player.gender == ConstPlayer.NAMEC
                        ? "Trưởng lão Guru"
                        : "Vua Vegeta"));
        text = text.replaceAll(ConstTask.TEN_MAP_QUY_LAO, player.gender == ConstPlayer.TRAI_DAT
                ? "Đảo Kamê"
                : (player.gender == ConstPlayer.NAMEC
                        ? "Đảo Guru"
                        : "Vách núi đen"));
        text = text.replaceAll(ConstTask.TEN_QUAI_3000, player.gender == ConstPlayer.TRAI_DAT
                ? "ốc mượn hồn"
                : (player.gender == ConstPlayer.NAMEC
                        ? "ốc sên"
                        : "heo Xayda mẹ"));
        // ----------------------------------------------------------------------
        text = text.replaceAll(ConstTask.TEN_LANG, player.gender == ConstPlayer.TRAI_DAT
                ? "Làng Aru"
                : (player.gender == ConstPlayer.NAMEC
                        ? "Làng Mori"
                        : "Làng Kakarot"));
        text = text.replaceAll(ConstTask.TEN_NPC_NHA, player.gender == ConstPlayer.TRAI_DAT
                ? "ông Gôhan"
                : (player.gender == ConstPlayer.NAMEC
                        ? "ông Moori"
                        : "ông Paragus"));
        text = text.replaceAll(ConstTask.TEN_QUAI_200, player.gender == ConstPlayer.TRAI_DAT
                ? "khủng long"
                : (player.gender == ConstPlayer.NAMEC
                        ? "lợn lòi"
                        : "quỷ đất"));
        text = text.replaceAll(ConstTask.TEN_MAP_200, player.gender == ConstPlayer.TRAI_DAT
                ? "Đồi hoa cúc"
                : (player.gender == ConstPlayer.NAMEC
                        ? "Đồi nấm tím"
                        : "Đồi hoang"));
        text = text.replaceAll(ConstTask.TEN_VACH_NUI, player.gender == ConstPlayer.TRAI_DAT
                ? "Vách núi Aru"
                : (player.gender == ConstPlayer.NAMEC
                        ? "Vách núi Moori"
                        : "Vách núi Kakarot"));
        text = text.replaceAll(ConstTask.TEN_MAP_500, player.gender == ConstPlayer.TRAI_DAT
                ? "Thung lũng tre"
                : (player.gender == ConstPlayer.NAMEC
                        ? "Thị trấn Moori"
                        : "Làng Plane"));
        text = text.replaceAll(ConstTask.TEN_NPC_TTVT, player.gender == ConstPlayer.TRAI_DAT
                ? "Dr. Brief"
                : (player.gender == ConstPlayer.NAMEC
                        ? "Cargo"
                        : "Cui"));
        text = text.replaceAll(ConstTask.TEN_QUAI_BAY_600, player.gender == ConstPlayer.TRAI_DAT
                ? "thằn lằn bay"
                : (player.gender == ConstPlayer.NAMEC
                        ? "phi long"
                        : "quỷ bay"));
        text = text.replaceAll(ConstTask.TEN_NPC_SHOP_LANG, player.gender == ConstPlayer.TRAI_DAT
                ? "Bunma"
                : (player.gender == ConstPlayer.NAMEC
                        ? "Dende"
                        : "Appule"));
        return text;
    }

    public boolean isCurrentTask(Player player, int idTaskCustom) {
        switch (idTaskCustom) {
            case ConstTask.TASK_0_0:
                return player.playerTask.taskMain.id == 0 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_0_1:
                return player.playerTask.taskMain.id == 0 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_0_2:
                return player.playerTask.taskMain.id == 0 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_0_3:
                return player.playerTask.taskMain.id == 0 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_0_4:
                return player.playerTask.taskMain.id == 0 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_0_5:
                return player.playerTask.taskMain.id == 0 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_0_6:
                return player.playerTask.taskMain.id == 0 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_1_0:
                return player.playerTask.taskMain.id == 1 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_1_1:
                return player.playerTask.taskMain.id == 1 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_1_2:
                return player.playerTask.taskMain.id == 1 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_1_3:
                return player.playerTask.taskMain.id == 1 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_1_4:
                return player.playerTask.taskMain.id == 1 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_1_5:
                return player.playerTask.taskMain.id == 1 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_1_6:
                return player.playerTask.taskMain.id == 1 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_2_0:
                return player.playerTask.taskMain.id == 2 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_2_1:
                return player.playerTask.taskMain.id == 2 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_2_2:
                return player.playerTask.taskMain.id == 2 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_2_3:
                return player.playerTask.taskMain.id == 2 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_2_4:
                return player.playerTask.taskMain.id == 2 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_2_5:
                return player.playerTask.taskMain.id == 2 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_2_6:
                return player.playerTask.taskMain.id == 2 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_3_0:
                return player.playerTask.taskMain.id == 3 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_3_1:
                return player.playerTask.taskMain.id == 3 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_3_2:
                return player.playerTask.taskMain.id == 3 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_3_3:
                return player.playerTask.taskMain.id == 3 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_3_4:
                return player.playerTask.taskMain.id == 3 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_3_5:
                return player.playerTask.taskMain.id == 3 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_3_6:
                return player.playerTask.taskMain.id == 3 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_4_0:
                return player.playerTask.taskMain.id == 4 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_4_1:
                return player.playerTask.taskMain.id == 4 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_4_2:
                return player.playerTask.taskMain.id == 4 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_4_3:
                return player.playerTask.taskMain.id == 4 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_4_4:
                return player.playerTask.taskMain.id == 4 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_4_5:
                return player.playerTask.taskMain.id == 4 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_4_6:
                return player.playerTask.taskMain.id == 4 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_5_0:
                return player.playerTask.taskMain.id == 5 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_5_1:
                return player.playerTask.taskMain.id == 5 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_5_2:
                return player.playerTask.taskMain.id == 5 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_5_3:
                return player.playerTask.taskMain.id == 5 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_5_4:
                return player.playerTask.taskMain.id == 5 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_5_5:
                return player.playerTask.taskMain.id == 5 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_5_6:
                return player.playerTask.taskMain.id == 5 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_6_0:
                return player.playerTask.taskMain.id == 6 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_6_1:
                return player.playerTask.taskMain.id == 6 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_6_2:
                return player.playerTask.taskMain.id == 6 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_6_3:
                return player.playerTask.taskMain.id == 6 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_6_4:
                return player.playerTask.taskMain.id == 6 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_6_5:
                return player.playerTask.taskMain.id == 6 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_6_6:
                return player.playerTask.taskMain.id == 6 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_7_0:
                return player.playerTask.taskMain.id == 7 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_7_1:
                return player.playerTask.taskMain.id == 7 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_7_2:
                return player.playerTask.taskMain.id == 7 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_7_3:
                return player.playerTask.taskMain.id == 7 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_7_4:
                return player.playerTask.taskMain.id == 7 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_7_5:
                return player.playerTask.taskMain.id == 7 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_7_6:
                return player.playerTask.taskMain.id == 7 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_8_0:
                return player.playerTask.taskMain.id == 8 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_8_1:
                return player.playerTask.taskMain.id == 8 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_8_2:
                return player.playerTask.taskMain.id == 8 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_8_3:
                return player.playerTask.taskMain.id == 8 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_8_4:
                return player.playerTask.taskMain.id == 8 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_8_5:
                return player.playerTask.taskMain.id == 8 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_8_6:
                return player.playerTask.taskMain.id == 8 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_9_0:
                return player.playerTask.taskMain.id == 9 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_9_1:
                return player.playerTask.taskMain.id == 9 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_9_2:
                return player.playerTask.taskMain.id == 9 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_9_3:
                return player.playerTask.taskMain.id == 9 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_9_4:
                return player.playerTask.taskMain.id == 9 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_9_5:
                return player.playerTask.taskMain.id == 9 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_9_6:
                return player.playerTask.taskMain.id == 9 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_10_0:
                return player.playerTask.taskMain.id == 10 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_10_1:
                return player.playerTask.taskMain.id == 10 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_10_2:
                return player.playerTask.taskMain.id == 10 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_10_3:
                return player.playerTask.taskMain.id == 10 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_10_4:
                return player.playerTask.taskMain.id == 10 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_10_5:
                return player.playerTask.taskMain.id == 10 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_10_6:
                return player.playerTask.taskMain.id == 10 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_11_0:
                return player.playerTask.taskMain.id == 11 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_11_1:
                return player.playerTask.taskMain.id == 11 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_11_2:
                return player.playerTask.taskMain.id == 11 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_11_3:
                return player.playerTask.taskMain.id == 11 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_11_4:
                return player.playerTask.taskMain.id == 11 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_11_5:
                return player.playerTask.taskMain.id == 11 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_11_6:
                return player.playerTask.taskMain.id == 11 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_12_0:
                return player.playerTask.taskMain.id == 12 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_12_1:
                return player.playerTask.taskMain.id == 12 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_12_2:
                return player.playerTask.taskMain.id == 12 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_12_3:
                return player.playerTask.taskMain.id == 12 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_12_4:
                return player.playerTask.taskMain.id == 12 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_12_5:
                return player.playerTask.taskMain.id == 12 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_12_6:
                return player.playerTask.taskMain.id == 12 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_13_0:
                return player.playerTask.taskMain.id == 13 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_13_1:
                return player.playerTask.taskMain.id == 13 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_13_2:
                return player.playerTask.taskMain.id == 13 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_13_3:
                return player.playerTask.taskMain.id == 13 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_13_4:
                return player.playerTask.taskMain.id == 13 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_13_5:
                return player.playerTask.taskMain.id == 13 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_13_6:
                return player.playerTask.taskMain.id == 13 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_14_0:
                return player.playerTask.taskMain.id == 14 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_14_1:
                return player.playerTask.taskMain.id == 14 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_14_2:
                return player.playerTask.taskMain.id == 14 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_14_3:
                return player.playerTask.taskMain.id == 14 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_14_4:
                return player.playerTask.taskMain.id == 14 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_14_5:
                return player.playerTask.taskMain.id == 14 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_14_6:
                return player.playerTask.taskMain.id == 14 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_15_0:
                return player.playerTask.taskMain.id == 15 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_15_1:
                return player.playerTask.taskMain.id == 15 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_15_2:
                return player.playerTask.taskMain.id == 15 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_15_3:
                return player.playerTask.taskMain.id == 15 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_15_4:
                return player.playerTask.taskMain.id == 15 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_15_5:
                return player.playerTask.taskMain.id == 15 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_15_6:
                return player.playerTask.taskMain.id == 15 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_16_0:
                return player.playerTask.taskMain.id == 16 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_16_1:
                return player.playerTask.taskMain.id == 16 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_16_2:
                return player.playerTask.taskMain.id == 16 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_16_3:
                return player.playerTask.taskMain.id == 16 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_16_4:
                return player.playerTask.taskMain.id == 16 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_16_5:
                return player.playerTask.taskMain.id == 16 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_16_6:
                return player.playerTask.taskMain.id == 16 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_17_0:
                return player.playerTask.taskMain.id == 17 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_17_1:
                return player.playerTask.taskMain.id == 17 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_17_2:
                return player.playerTask.taskMain.id == 17 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_17_3:
                return player.playerTask.taskMain.id == 17 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_17_4:
                return player.playerTask.taskMain.id == 17 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_17_5:
                return player.playerTask.taskMain.id == 17 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_17_6:
                return player.playerTask.taskMain.id == 17 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_18_0:
                return player.playerTask.taskMain.id == 18 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_18_1:
                return player.playerTask.taskMain.id == 18 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_18_2:
                return player.playerTask.taskMain.id == 18 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_18_3:
                return player.playerTask.taskMain.id == 18 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_18_4:
                return player.playerTask.taskMain.id == 18 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_18_5:
                return player.playerTask.taskMain.id == 18 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_18_6:
                return player.playerTask.taskMain.id == 18 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_19_0:
                return player.playerTask.taskMain.id == 19 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_19_1:
                return player.playerTask.taskMain.id == 19 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_19_2:
                return player.playerTask.taskMain.id == 19 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_19_3:
                return player.playerTask.taskMain.id == 19 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_19_4:
                return player.playerTask.taskMain.id == 19 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_19_5:
                return player.playerTask.taskMain.id == 19 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_19_6:
                return player.playerTask.taskMain.id == 19 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_20_0:
                return player.playerTask.taskMain.id == 20 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_20_1:
                return player.playerTask.taskMain.id == 20 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_20_2:
                return player.playerTask.taskMain.id == 20 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_20_3:
                return player.playerTask.taskMain.id == 20 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_20_4:
                return player.playerTask.taskMain.id == 20 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_20_5:
                return player.playerTask.taskMain.id == 20 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_20_6:
                return player.playerTask.taskMain.id == 20 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_21_0:
                return player.playerTask.taskMain.id == 21 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_21_1:
                return player.playerTask.taskMain.id == 21 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_21_2:
                return player.playerTask.taskMain.id == 21 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_21_3:
                return player.playerTask.taskMain.id == 21 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_21_4:
                return player.playerTask.taskMain.id == 21 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_21_5:
                return player.playerTask.taskMain.id == 21 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_21_6:
                return player.playerTask.taskMain.id == 21 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_22_0:
                return player.playerTask.taskMain.id == 22 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_22_1:
                return player.playerTask.taskMain.id == 22 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_22_2:
                return player.playerTask.taskMain.id == 22 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_22_3:
                return player.playerTask.taskMain.id == 22 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_22_4:
                return player.playerTask.taskMain.id == 22 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_22_5:
                return player.playerTask.taskMain.id == 22 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_22_6:
                return player.playerTask.taskMain.id == 22 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_23_0:
                return player.playerTask.taskMain.id == 23 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_23_1:
                return player.playerTask.taskMain.id == 23 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_23_2:
                return player.playerTask.taskMain.id == 23 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_23_3:
                return player.playerTask.taskMain.id == 23 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_23_4:
                return player.playerTask.taskMain.id == 23 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_23_5:
                return player.playerTask.taskMain.id == 23 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_23_6:
                return player.playerTask.taskMain.id == 23 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_24_0:
                return player.playerTask.taskMain.id == 24 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_24_1:
                return player.playerTask.taskMain.id == 24 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_24_2:
                return player.playerTask.taskMain.id == 24 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_24_3:
                return player.playerTask.taskMain.id == 24 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_24_4:
                return player.playerTask.taskMain.id == 24 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_24_5:
                return player.playerTask.taskMain.id == 24 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_24_6:
                return player.playerTask.taskMain.id == 24 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_25_0:
                return player.playerTask.taskMain.id == 25 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_25_1:
                return player.playerTask.taskMain.id == 25 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_25_2:
                return player.playerTask.taskMain.id == 25 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_25_3:
                return player.playerTask.taskMain.id == 25 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_25_4:
                return player.playerTask.taskMain.id == 25 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_25_5:
                return player.playerTask.taskMain.id == 25 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_25_6:
                return player.playerTask.taskMain.id == 25 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_26_0:
                return player.playerTask.taskMain.id == 26 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_26_1:
                return player.playerTask.taskMain.id == 26 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_26_2:
                return player.playerTask.taskMain.id == 26 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_26_3:
                return player.playerTask.taskMain.id == 26 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_26_4:
                return player.playerTask.taskMain.id == 26 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_26_5:
                return player.playerTask.taskMain.id == 26 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_26_6:
                return player.playerTask.taskMain.id == 26 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_27_0:
                return player.playerTask.taskMain.id == 27 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_27_1:
                return player.playerTask.taskMain.id == 27 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_27_2:
                return player.playerTask.taskMain.id == 27 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_27_3:
                return player.playerTask.taskMain.id == 27 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_27_4:
                return player.playerTask.taskMain.id == 27 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_27_5:
                return player.playerTask.taskMain.id == 27 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_27_6:
                return player.playerTask.taskMain.id == 27 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_28_0:
                return player.playerTask.taskMain.id == 28 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_28_1:
                return player.playerTask.taskMain.id == 28 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_28_2:
                return player.playerTask.taskMain.id == 28 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_28_3:
                return player.playerTask.taskMain.id == 28 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_28_4:
                return player.playerTask.taskMain.id == 28 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_28_5:
                return player.playerTask.taskMain.id == 28 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_28_6:
                return player.playerTask.taskMain.id == 28 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_29_0:
                return player.playerTask.taskMain.id == 29 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_29_1:
                return player.playerTask.taskMain.id == 29 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_29_2:
                return player.playerTask.taskMain.id == 29 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_29_3:
                return player.playerTask.taskMain.id == 29 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_29_4:
                return player.playerTask.taskMain.id == 29 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_29_5:
                return player.playerTask.taskMain.id == 29 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_29_6:
                return player.playerTask.taskMain.id == 29 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_30_0:
                return player.playerTask.taskMain.id == 30 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_30_1:
                return player.playerTask.taskMain.id == 30 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_30_2:
                return player.playerTask.taskMain.id == 30 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_30_3:
                return player.playerTask.taskMain.id == 30 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_30_4:
                return player.playerTask.taskMain.id == 30 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_30_5:
                return player.playerTask.taskMain.id == 30 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_30_6:
                return player.playerTask.taskMain.id == 30 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_31_0:
                return player.playerTask.taskMain.id == 31 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_31_1:
                return player.playerTask.taskMain.id == 31 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_31_2:
                return player.playerTask.taskMain.id == 31 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_31_3:
                return player.playerTask.taskMain.id == 31 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_31_4:
                return player.playerTask.taskMain.id == 31 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_31_5:
                return player.playerTask.taskMain.id == 31 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_31_6:
                return player.playerTask.taskMain.id == 31 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_32_0:
                return player.playerTask.taskMain.id == 32 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_32_1:
                return player.playerTask.taskMain.id == 32 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_32_2:
                return player.playerTask.taskMain.id == 32 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_32_3:
                return player.playerTask.taskMain.id == 32 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_32_4:
                return player.playerTask.taskMain.id == 32 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_32_5:
                return player.playerTask.taskMain.id == 32 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_32_6:
                return player.playerTask.taskMain.id == 32 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_33_0:
                return player.playerTask.taskMain.id == 33 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_33_1:
                return player.playerTask.taskMain.id == 33 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_33_2:
                return player.playerTask.taskMain.id == 33 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_33_3:
                return player.playerTask.taskMain.id == 33 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_33_4:
                return player.playerTask.taskMain.id == 33 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_33_5:
                return player.playerTask.taskMain.id == 33 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_33_6:
                return player.playerTask.taskMain.id == 33 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_34_0:
                return player.playerTask.taskMain.id == 34 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_34_1:
                return player.playerTask.taskMain.id == 34 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_34_2:
                return player.playerTask.taskMain.id == 34 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_34_3:
                return player.playerTask.taskMain.id == 34 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_34_4:
                return player.playerTask.taskMain.id == 34 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_34_5:
                return player.playerTask.taskMain.id == 34 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_34_6:
                return player.playerTask.taskMain.id == 34 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_35_0:
                return player.playerTask.taskMain.id == 35 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_35_1:
                return player.playerTask.taskMain.id == 35 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_35_2:
                return player.playerTask.taskMain.id == 35 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_35_3:
                return player.playerTask.taskMain.id == 35 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_35_4:
                return player.playerTask.taskMain.id == 35 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_35_5:
                return player.playerTask.taskMain.id == 35 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_35_6:
                return player.playerTask.taskMain.id == 35 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_36_0:
                return player.playerTask.taskMain.id == 36 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_36_1:
                return player.playerTask.taskMain.id == 36 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_36_2:
                return player.playerTask.taskMain.id == 36 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_36_3:
                return player.playerTask.taskMain.id == 36 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_36_4:
                return player.playerTask.taskMain.id == 36 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_36_5:
                return player.playerTask.taskMain.id == 36 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_36_6:
                return player.playerTask.taskMain.id == 36 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_37_0:
                return player.playerTask.taskMain.id == 37 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_37_1:
                return player.playerTask.taskMain.id == 37 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_37_2:
                return player.playerTask.taskMain.id == 37 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_37_3:
                return player.playerTask.taskMain.id == 37 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_37_4:
                return player.playerTask.taskMain.id == 37 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_37_5:
                return player.playerTask.taskMain.id == 37 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_37_6:
                return player.playerTask.taskMain.id == 37 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_38_0:
                return player.playerTask.taskMain.id == 38 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_38_1:
                return player.playerTask.taskMain.id == 38 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_38_2:
                return player.playerTask.taskMain.id == 38 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_38_3:
                return player.playerTask.taskMain.id == 38 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_38_4:
                return player.playerTask.taskMain.id == 38 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_38_5:
                return player.playerTask.taskMain.id == 38 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_38_6:
                return player.playerTask.taskMain.id == 38 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_39_0:
                return player.playerTask.taskMain.id == 39 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_39_1:
                return player.playerTask.taskMain.id == 39 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_39_2:
                return player.playerTask.taskMain.id == 39 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_39_3:
                return player.playerTask.taskMain.id == 39 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_39_4:
                return player.playerTask.taskMain.id == 39 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_39_5:
                return player.playerTask.taskMain.id == 39 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_39_6:
                return player.playerTask.taskMain.id == 39 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_40_0:
                return player.playerTask.taskMain.id == 40 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_40_1:
                return player.playerTask.taskMain.id == 40 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_40_2:
                return player.playerTask.taskMain.id == 40 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_40_3:
                return player.playerTask.taskMain.id == 40 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_40_4:
                return player.playerTask.taskMain.id == 40 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_40_5:
                return player.playerTask.taskMain.id == 40 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_40_6:
                return player.playerTask.taskMain.id == 40 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_41_0:
                return player.playerTask.taskMain.id == 41 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_41_1:
                return player.playerTask.taskMain.id == 41 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_41_2:
                return player.playerTask.taskMain.id == 41 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_41_3:
                return player.playerTask.taskMain.id == 41 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_41_4:
                return player.playerTask.taskMain.id == 41 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_41_5:
                return player.playerTask.taskMain.id == 41 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_41_6:
                return player.playerTask.taskMain.id == 41 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_42_0:
                return player.playerTask.taskMain.id == 42 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_42_1:
                return player.playerTask.taskMain.id == 42 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_42_2:
                return player.playerTask.taskMain.id == 42 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_42_3:
                return player.playerTask.taskMain.id == 42 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_42_4:
                return player.playerTask.taskMain.id == 42 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_42_5:
                return player.playerTask.taskMain.id == 42 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_42_6:
                return player.playerTask.taskMain.id == 42 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_43_0:
                return player.playerTask.taskMain.id == 43 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_43_1:
                return player.playerTask.taskMain.id == 43 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_43_2:
                return player.playerTask.taskMain.id == 43 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_43_3:
                return player.playerTask.taskMain.id == 43 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_43_4:
                return player.playerTask.taskMain.id == 43 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_43_5:
                return player.playerTask.taskMain.id == 43 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_43_6:
                return player.playerTask.taskMain.id == 43 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_44_0:
                return player.playerTask.taskMain.id == 44 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_44_1:
                return player.playerTask.taskMain.id == 44 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_44_2:
                return player.playerTask.taskMain.id == 44 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_44_3:
                return player.playerTask.taskMain.id == 44 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_44_4:
                return player.playerTask.taskMain.id == 44 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_44_5:
                return player.playerTask.taskMain.id == 44 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_44_6:
                return player.playerTask.taskMain.id == 44 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_45_0:
                return player.playerTask.taskMain.id == 45 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_45_1:
                return player.playerTask.taskMain.id == 45 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_45_2:
                return player.playerTask.taskMain.id == 45 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_45_3:
                return player.playerTask.taskMain.id == 45 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_45_4:
                return player.playerTask.taskMain.id == 45 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_45_5:
                return player.playerTask.taskMain.id == 45 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_45_6:
                return player.playerTask.taskMain.id == 45 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_46_0:
                return player.playerTask.taskMain.id == 46 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_46_1:
                return player.playerTask.taskMain.id == 46 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_46_2:
                return player.playerTask.taskMain.id == 46 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_46_3:
                return player.playerTask.taskMain.id == 46 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_46_4:
                return player.playerTask.taskMain.id == 46 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_46_5:
                return player.playerTask.taskMain.id == 46 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_46_6:
                return player.playerTask.taskMain.id == 46 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_47_0:
                return player.playerTask.taskMain.id == 47 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_47_1:
                return player.playerTask.taskMain.id == 47 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_47_2:
                return player.playerTask.taskMain.id == 47 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_47_3:
                return player.playerTask.taskMain.id == 47 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_47_4:
                return player.playerTask.taskMain.id == 47 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_47_5:
                return player.playerTask.taskMain.id == 47 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_47_6:
                return player.playerTask.taskMain.id == 47 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_48_0:
                return player.playerTask.taskMain.id == 48 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_48_1:
                return player.playerTask.taskMain.id == 48 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_48_2:
                return player.playerTask.taskMain.id == 48 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_48_3:
                return player.playerTask.taskMain.id == 48 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_48_4:
                return player.playerTask.taskMain.id == 48 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_48_5:
                return player.playerTask.taskMain.id == 48 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_48_6:
                return player.playerTask.taskMain.id == 48 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_49_0:
                return player.playerTask.taskMain.id == 49 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_49_1:
                return player.playerTask.taskMain.id == 49 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_49_2:
                return player.playerTask.taskMain.id == 49 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_49_3:
                return player.playerTask.taskMain.id == 49 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_49_4:
                return player.playerTask.taskMain.id == 49 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_49_5:
                return player.playerTask.taskMain.id == 49 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_49_6:
                return player.playerTask.taskMain.id == 49 && player.playerTask.taskMain.index == 6;
            case ConstTask.TASK_50_0:
                return player.playerTask.taskMain.id == 50 && player.playerTask.taskMain.index == 0;
            case ConstTask.TASK_50_1:
                return player.playerTask.taskMain.id == 50 && player.playerTask.taskMain.index == 1;
            case ConstTask.TASK_50_2:
                return player.playerTask.taskMain.id == 50 && player.playerTask.taskMain.index == 2;
            case ConstTask.TASK_50_3:
                return player.playerTask.taskMain.id == 50 && player.playerTask.taskMain.index == 3;
            case ConstTask.TASK_50_4:
                return player.playerTask.taskMain.id == 50 && player.playerTask.taskMain.index == 4;
            case ConstTask.TASK_50_5:
                return player.playerTask.taskMain.id == 50 && player.playerTask.taskMain.index == 5;
            case ConstTask.TASK_50_6:
                return player.playerTask.taskMain.id == 50 && player.playerTask.taskMain.index == 6;
        }
        return false;
    }

    public int getIdTask(Player player) {
        if (player.isPet || player.isBoss || player.playerTask == null || player.playerTask.taskMain == null) {
            return -1;
        } else if (player.playerTask.taskMain.id == 0 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_0_0;
        } else if (player.playerTask.taskMain.id == 0 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_0_1;
        } else if (player.playerTask.taskMain.id == 0 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_0_2;
        } else if (player.playerTask.taskMain.id == 0 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_0_3;
        } else if (player.playerTask.taskMain.id == 0 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_0_4;
        } else if (player.playerTask.taskMain.id == 0 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_0_5;
        } else if (player.playerTask.taskMain.id == 0 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_0_6;
        } else if (player.playerTask.taskMain.id == 1 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_1_0;
        } else if (player.playerTask.taskMain.id == 1 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_1_1;
        } else if (player.playerTask.taskMain.id == 1 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_1_2;
        } else if (player.playerTask.taskMain.id == 1 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_1_3;
        } else if (player.playerTask.taskMain.id == 1 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_1_4;
        } else if (player.playerTask.taskMain.id == 1 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_1_5;
        } else if (player.playerTask.taskMain.id == 1 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_1_6;
        } else if (player.playerTask.taskMain.id == 2 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_2_0;
        } else if (player.playerTask.taskMain.id == 2 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_2_1;
        } else if (player.playerTask.taskMain.id == 2 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_2_2;
        } else if (player.playerTask.taskMain.id == 2 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_2_3;
        } else if (player.playerTask.taskMain.id == 2 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_2_4;
        } else if (player.playerTask.taskMain.id == 2 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_2_5;
        } else if (player.playerTask.taskMain.id == 2 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_2_6;
        } else if (player.playerTask.taskMain.id == 3 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_3_0;
        } else if (player.playerTask.taskMain.id == 3 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_3_1;
        } else if (player.playerTask.taskMain.id == 3 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_3_2;
        } else if (player.playerTask.taskMain.id == 3 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_3_3;
        } else if (player.playerTask.taskMain.id == 3 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_3_4;
        } else if (player.playerTask.taskMain.id == 3 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_3_5;
        } else if (player.playerTask.taskMain.id == 3 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_3_6;
        } else if (player.playerTask.taskMain.id == 4 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_4_0;
        } else if (player.playerTask.taskMain.id == 4 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_4_1;
        } else if (player.playerTask.taskMain.id == 4 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_4_2;
        } else if (player.playerTask.taskMain.id == 4 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_4_3;
        } else if (player.playerTask.taskMain.id == 4 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_4_4;
        } else if (player.playerTask.taskMain.id == 4 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_4_5;
        } else if (player.playerTask.taskMain.id == 4 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_4_6;
        } else if (player.playerTask.taskMain.id == 5 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_5_0;
        } else if (player.playerTask.taskMain.id == 5 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_5_1;
        } else if (player.playerTask.taskMain.id == 5 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_5_2;
        } else if (player.playerTask.taskMain.id == 5 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_5_3;
        } else if (player.playerTask.taskMain.id == 5 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_5_4;
        } else if (player.playerTask.taskMain.id == 5 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_5_5;
        } else if (player.playerTask.taskMain.id == 5 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_5_6;
        } else if (player.playerTask.taskMain.id == 6 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_6_0;
        } else if (player.playerTask.taskMain.id == 6 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_6_1;
        } else if (player.playerTask.taskMain.id == 6 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_6_2;
        } else if (player.playerTask.taskMain.id == 6 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_6_3;
        } else if (player.playerTask.taskMain.id == 6 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_6_4;
        } else if (player.playerTask.taskMain.id == 6 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_6_5;
        } else if (player.playerTask.taskMain.id == 6 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_6_6;
        } else if (player.playerTask.taskMain.id == 7 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_7_0;
        } else if (player.playerTask.taskMain.id == 7 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_7_1;
        } else if (player.playerTask.taskMain.id == 7 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_7_2;
        } else if (player.playerTask.taskMain.id == 7 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_7_3;
        } else if (player.playerTask.taskMain.id == 7 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_7_4;
        } else if (player.playerTask.taskMain.id == 7 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_7_5;
        } else if (player.playerTask.taskMain.id == 7 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_7_6;
        } else if (player.playerTask.taskMain.id == 8 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_8_0;
        } else if (player.playerTask.taskMain.id == 8 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_8_1;
        } else if (player.playerTask.taskMain.id == 8 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_8_2;
        } else if (player.playerTask.taskMain.id == 8 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_8_3;
        } else if (player.playerTask.taskMain.id == 8 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_8_4;
        } else if (player.playerTask.taskMain.id == 8 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_8_5;
        } else if (player.playerTask.taskMain.id == 8 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_8_6;
        } else if (player.playerTask.taskMain.id == 9 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_9_0;
        } else if (player.playerTask.taskMain.id == 9 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_9_1;
        } else if (player.playerTask.taskMain.id == 9 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_9_2;
        } else if (player.playerTask.taskMain.id == 9 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_9_3;
        } else if (player.playerTask.taskMain.id == 9 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_9_4;
        } else if (player.playerTask.taskMain.id == 9 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_9_5;
        } else if (player.playerTask.taskMain.id == 9 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_9_6;
        } else if (player.playerTask.taskMain.id == 10 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_10_0;
        } else if (player.playerTask.taskMain.id == 10 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_10_1;
        } else if (player.playerTask.taskMain.id == 10 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_10_2;
        } else if (player.playerTask.taskMain.id == 10 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_10_3;
        } else if (player.playerTask.taskMain.id == 10 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_10_4;
        } else if (player.playerTask.taskMain.id == 10 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_10_5;
        } else if (player.playerTask.taskMain.id == 10 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_10_6;
        } else if (player.playerTask.taskMain.id == 11 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_11_0;
        } else if (player.playerTask.taskMain.id == 11 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_11_1;
        } else if (player.playerTask.taskMain.id == 11 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_11_2;
        } else if (player.playerTask.taskMain.id == 11 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_11_3;
        } else if (player.playerTask.taskMain.id == 11 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_11_4;
        } else if (player.playerTask.taskMain.id == 11 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_11_5;
        } else if (player.playerTask.taskMain.id == 11 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_11_6;
        } else if (player.playerTask.taskMain.id == 12 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_12_0;
        } else if (player.playerTask.taskMain.id == 12 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_12_1;
        } else if (player.playerTask.taskMain.id == 12 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_12_2;
        } else if (player.playerTask.taskMain.id == 12 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_12_3;
        } else if (player.playerTask.taskMain.id == 12 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_12_4;
        } else if (player.playerTask.taskMain.id == 12 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_12_5;
        } else if (player.playerTask.taskMain.id == 12 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_12_6;
        } else if (player.playerTask.taskMain.id == 13 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_13_0;
        } else if (player.playerTask.taskMain.id == 13 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_13_1;
        } else if (player.playerTask.taskMain.id == 13 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_13_2;
        } else if (player.playerTask.taskMain.id == 13 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_13_3;
        } else if (player.playerTask.taskMain.id == 13 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_13_4;
        } else if (player.playerTask.taskMain.id == 13 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_13_5;
        } else if (player.playerTask.taskMain.id == 13 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_13_6;
        } else if (player.playerTask.taskMain.id == 14 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_14_0;
        } else if (player.playerTask.taskMain.id == 14 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_14_1;
        } else if (player.playerTask.taskMain.id == 14 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_14_2;
        } else if (player.playerTask.taskMain.id == 14 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_14_3;
        } else if (player.playerTask.taskMain.id == 14 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_14_4;
        } else if (player.playerTask.taskMain.id == 14 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_14_5;
        } else if (player.playerTask.taskMain.id == 14 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_14_6;
        } else if (player.playerTask.taskMain.id == 15 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_15_0;
        } else if (player.playerTask.taskMain.id == 15 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_15_1;
        } else if (player.playerTask.taskMain.id == 15 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_15_2;
        } else if (player.playerTask.taskMain.id == 15 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_15_3;
        } else if (player.playerTask.taskMain.id == 15 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_15_4;
        } else if (player.playerTask.taskMain.id == 15 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_15_5;
        } else if (player.playerTask.taskMain.id == 15 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_15_6;
        } else if (player.playerTask.taskMain.id == 16 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_16_0;
        } else if (player.playerTask.taskMain.id == 16 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_16_1;
        } else if (player.playerTask.taskMain.id == 16 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_16_2;
        } else if (player.playerTask.taskMain.id == 16 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_16_3;
        } else if (player.playerTask.taskMain.id == 16 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_16_4;
        } else if (player.playerTask.taskMain.id == 16 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_16_5;
        } else if (player.playerTask.taskMain.id == 16 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_16_6;
        } else if (player.playerTask.taskMain.id == 17 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_17_0;
        } else if (player.playerTask.taskMain.id == 17 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_17_1;
        } else if (player.playerTask.taskMain.id == 17 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_17_2;
        } else if (player.playerTask.taskMain.id == 17 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_17_3;
        } else if (player.playerTask.taskMain.id == 17 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_17_4;
        } else if (player.playerTask.taskMain.id == 17 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_17_5;
        } else if (player.playerTask.taskMain.id == 17 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_17_6;
        } else if (player.playerTask.taskMain.id == 18 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_18_0;
        } else if (player.playerTask.taskMain.id == 18 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_18_1;
        } else if (player.playerTask.taskMain.id == 18 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_18_2;
        } else if (player.playerTask.taskMain.id == 18 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_18_3;
        } else if (player.playerTask.taskMain.id == 18 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_18_4;
        } else if (player.playerTask.taskMain.id == 18 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_18_5;
        } else if (player.playerTask.taskMain.id == 18 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_18_6;
        } else if (player.playerTask.taskMain.id == 19 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_19_0;
        } else if (player.playerTask.taskMain.id == 19 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_19_1;
        } else if (player.playerTask.taskMain.id == 19 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_19_2;
        } else if (player.playerTask.taskMain.id == 19 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_19_3;
        } else if (player.playerTask.taskMain.id == 19 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_19_4;
        } else if (player.playerTask.taskMain.id == 19 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_19_5;
        } else if (player.playerTask.taskMain.id == 19 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_19_6;
        } else if (player.playerTask.taskMain.id == 20 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_20_0;
        } else if (player.playerTask.taskMain.id == 20 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_20_1;
        } else if (player.playerTask.taskMain.id == 20 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_20_2;
        } else if (player.playerTask.taskMain.id == 20 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_20_3;
        } else if (player.playerTask.taskMain.id == 20 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_20_4;
        } else if (player.playerTask.taskMain.id == 20 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_20_5;
        } else if (player.playerTask.taskMain.id == 20 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_20_6;
        } else if (player.playerTask.taskMain.id == 21 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_21_0;
        } else if (player.playerTask.taskMain.id == 21 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_21_1;
        } else if (player.playerTask.taskMain.id == 21 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_21_2;
        } else if (player.playerTask.taskMain.id == 21 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_21_3;
        } else if (player.playerTask.taskMain.id == 21 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_21_4;
        } else if (player.playerTask.taskMain.id == 21 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_21_5;
        } else if (player.playerTask.taskMain.id == 21 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_21_6;
        } else if (player.playerTask.taskMain.id == 22 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_22_0;
        } else if (player.playerTask.taskMain.id == 22 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_22_1;
        } else if (player.playerTask.taskMain.id == 22 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_22_2;
        } else if (player.playerTask.taskMain.id == 22 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_22_3;
        } else if (player.playerTask.taskMain.id == 22 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_22_4;
        } else if (player.playerTask.taskMain.id == 22 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_22_5;
        } else if (player.playerTask.taskMain.id == 22 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_22_6;
        } else if (player.playerTask.taskMain.id == 23 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_23_0;
        } else if (player.playerTask.taskMain.id == 23 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_23_1;
        } else if (player.playerTask.taskMain.id == 23 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_23_2;
        } else if (player.playerTask.taskMain.id == 23 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_23_3;
        } else if (player.playerTask.taskMain.id == 23 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_23_4;
        } else if (player.playerTask.taskMain.id == 23 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_23_5;
        } else if (player.playerTask.taskMain.id == 23 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_23_6;
        } else if (player.playerTask.taskMain.id == 24 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_24_0;
        } else if (player.playerTask.taskMain.id == 24 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_24_1;
        } else if (player.playerTask.taskMain.id == 24 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_24_2;
        } else if (player.playerTask.taskMain.id == 24 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_24_3;
        } else if (player.playerTask.taskMain.id == 24 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_24_4;
        } else if (player.playerTask.taskMain.id == 24 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_24_5;
        } else if (player.playerTask.taskMain.id == 24 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_24_6;
        } else if (player.playerTask.taskMain.id == 25 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_25_0;
        } else if (player.playerTask.taskMain.id == 25 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_25_1;
        } else if (player.playerTask.taskMain.id == 25 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_25_2;
        } else if (player.playerTask.taskMain.id == 25 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_25_3;
        } else if (player.playerTask.taskMain.id == 25 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_25_4;
        } else if (player.playerTask.taskMain.id == 25 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_25_5;
        } else if (player.playerTask.taskMain.id == 25 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_25_6;
        } else if (player.playerTask.taskMain.id == 26 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_26_0;
        } else if (player.playerTask.taskMain.id == 26 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_26_1;
        } else if (player.playerTask.taskMain.id == 26 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_26_2;
        } else if (player.playerTask.taskMain.id == 26 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_26_3;
        } else if (player.playerTask.taskMain.id == 26 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_26_4;
        } else if (player.playerTask.taskMain.id == 26 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_26_5;
        } else if (player.playerTask.taskMain.id == 26 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_26_6;
        } else if (player.playerTask.taskMain.id == 27 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_27_0;
        } else if (player.playerTask.taskMain.id == 27 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_27_1;
        } else if (player.playerTask.taskMain.id == 27 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_27_2;
        } else if (player.playerTask.taskMain.id == 27 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_27_3;
        } else if (player.playerTask.taskMain.id == 27 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_27_4;
        } else if (player.playerTask.taskMain.id == 27 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_27_5;
        } else if (player.playerTask.taskMain.id == 27 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_27_6;
        } else if (player.playerTask.taskMain.id == 28 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_28_0;
        } else if (player.playerTask.taskMain.id == 28 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_28_1;
        } else if (player.playerTask.taskMain.id == 28 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_28_2;
        } else if (player.playerTask.taskMain.id == 28 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_28_3;
        } else if (player.playerTask.taskMain.id == 28 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_28_4;
        } else if (player.playerTask.taskMain.id == 28 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_28_5;
        } else if (player.playerTask.taskMain.id == 28 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_28_6;
        } else if (player.playerTask.taskMain.id == 29 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_29_0;
        } else if (player.playerTask.taskMain.id == 29 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_29_1;
        } else if (player.playerTask.taskMain.id == 29 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_29_2;
        } else if (player.playerTask.taskMain.id == 29 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_29_3;
        } else if (player.playerTask.taskMain.id == 29 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_29_4;
        } else if (player.playerTask.taskMain.id == 29 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_29_5;
        } else if (player.playerTask.taskMain.id == 29 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_29_6;
        } else if (player.playerTask.taskMain.id == 30 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_30_0;
        } else if (player.playerTask.taskMain.id == 30 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_30_1;
        } else if (player.playerTask.taskMain.id == 30 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_30_2;
        } else if (player.playerTask.taskMain.id == 30 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_30_3;
        } else if (player.playerTask.taskMain.id == 30 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_30_4;
        } else if (player.playerTask.taskMain.id == 30 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_30_5;
        } else if (player.playerTask.taskMain.id == 30 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_30_6;
        } else if (player.playerTask.taskMain.id == 31 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_31_0;
        } else if (player.playerTask.taskMain.id == 31 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_31_1;
        } else if (player.playerTask.taskMain.id == 31 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_31_2;
        } else if (player.playerTask.taskMain.id == 31 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_31_3;
        } else if (player.playerTask.taskMain.id == 31 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_31_4;
        } else if (player.playerTask.taskMain.id == 31 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_31_5;
        } else if (player.playerTask.taskMain.id == 31 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_31_6;
        } else if (player.playerTask.taskMain.id == 32 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_32_0;
        } else if (player.playerTask.taskMain.id == 32 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_32_1;
        } else if (player.playerTask.taskMain.id == 32 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_32_2;
        } else if (player.playerTask.taskMain.id == 32 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_32_3;
        } else if (player.playerTask.taskMain.id == 32 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_32_4;
        } else if (player.playerTask.taskMain.id == 32 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_32_5;
        } else if (player.playerTask.taskMain.id == 32 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_32_6;
        } else if (player.playerTask.taskMain.id == 33 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_33_0;
        } else if (player.playerTask.taskMain.id == 33 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_33_1;
        } else if (player.playerTask.taskMain.id == 33 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_33_2;
        } else if (player.playerTask.taskMain.id == 33 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_33_3;
        } else if (player.playerTask.taskMain.id == 33 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_33_4;
        } else if (player.playerTask.taskMain.id == 33 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_33_5;
        } else if (player.playerTask.taskMain.id == 33 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_33_6;
        } else if (player.playerTask.taskMain.id == 34 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_34_0;
        } else if (player.playerTask.taskMain.id == 34 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_34_1;
        } else if (player.playerTask.taskMain.id == 34 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_34_2;
        } else if (player.playerTask.taskMain.id == 34 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_34_3;
        } else if (player.playerTask.taskMain.id == 34 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_34_4;
        } else if (player.playerTask.taskMain.id == 34 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_34_5;
        } else if (player.playerTask.taskMain.id == 34 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_34_6;
        } else if (player.playerTask.taskMain.id == 35 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_35_0;
        } else if (player.playerTask.taskMain.id == 35 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_35_1;
        } else if (player.playerTask.taskMain.id == 35 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_35_2;
        } else if (player.playerTask.taskMain.id == 35 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_35_3;
        } else if (player.playerTask.taskMain.id == 35 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_35_4;
        } else if (player.playerTask.taskMain.id == 35 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_35_5;
        } else if (player.playerTask.taskMain.id == 35 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_35_6;
        } else if (player.playerTask.taskMain.id == 36 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_36_0;
        } else if (player.playerTask.taskMain.id == 36 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_36_1;
        } else if (player.playerTask.taskMain.id == 36 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_36_2;
        } else if (player.playerTask.taskMain.id == 36 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_36_3;
        } else if (player.playerTask.taskMain.id == 36 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_36_4;
        } else if (player.playerTask.taskMain.id == 36 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_36_5;
        } else if (player.playerTask.taskMain.id == 36 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_36_6;
        } else if (player.playerTask.taskMain.id == 37 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_37_0;
        } else if (player.playerTask.taskMain.id == 37 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_37_1;
        } else if (player.playerTask.taskMain.id == 37 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_37_2;
        } else if (player.playerTask.taskMain.id == 37 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_37_3;
        } else if (player.playerTask.taskMain.id == 37 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_37_4;
        } else if (player.playerTask.taskMain.id == 37 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_37_5;
        } else if (player.playerTask.taskMain.id == 37 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_37_6;
        } else if (player.playerTask.taskMain.id == 38 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_38_0;
        } else if (player.playerTask.taskMain.id == 38 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_38_1;
        } else if (player.playerTask.taskMain.id == 38 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_38_2;
        } else if (player.playerTask.taskMain.id == 38 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_38_3;
        } else if (player.playerTask.taskMain.id == 38 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_38_4;
        } else if (player.playerTask.taskMain.id == 38 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_38_5;
        } else if (player.playerTask.taskMain.id == 38 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_38_6;
        } else if (player.playerTask.taskMain.id == 39 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_39_0;
        } else if (player.playerTask.taskMain.id == 39 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_39_1;
        } else if (player.playerTask.taskMain.id == 39 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_39_2;
        } else if (player.playerTask.taskMain.id == 39 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_39_3;
        } else if (player.playerTask.taskMain.id == 39 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_39_4;
        } else if (player.playerTask.taskMain.id == 39 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_39_5;
        } else if (player.playerTask.taskMain.id == 39 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_39_6;
        } else if (player.playerTask.taskMain.id == 40 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_40_0;
        } else if (player.playerTask.taskMain.id == 40 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_40_1;
        } else if (player.playerTask.taskMain.id == 40 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_40_2;
        } else if (player.playerTask.taskMain.id == 40 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_40_3;
        } else if (player.playerTask.taskMain.id == 40 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_40_4;
        } else if (player.playerTask.taskMain.id == 40 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_40_5;
        } else if (player.playerTask.taskMain.id == 40 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_40_6;
        } else if (player.playerTask.taskMain.id == 41 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_41_0;
        } else if (player.playerTask.taskMain.id == 41 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_41_1;
        } else if (player.playerTask.taskMain.id == 41 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_41_2;
        } else if (player.playerTask.taskMain.id == 41 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_41_3;
        } else if (player.playerTask.taskMain.id == 41 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_41_4;
        } else if (player.playerTask.taskMain.id == 41 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_41_5;
        } else if (player.playerTask.taskMain.id == 41 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_41_6;
        } else if (player.playerTask.taskMain.id == 42 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_42_0;
        } else if (player.playerTask.taskMain.id == 42 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_42_1;
        } else if (player.playerTask.taskMain.id == 42 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_42_2;
        } else if (player.playerTask.taskMain.id == 42 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_42_3;
        } else if (player.playerTask.taskMain.id == 42 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_42_4;
        } else if (player.playerTask.taskMain.id == 42 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_42_5;
        } else if (player.playerTask.taskMain.id == 42 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_42_6;
        } else if (player.playerTask.taskMain.id == 43 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_43_0;
        } else if (player.playerTask.taskMain.id == 43 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_43_1;
        } else if (player.playerTask.taskMain.id == 43 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_43_2;
        } else if (player.playerTask.taskMain.id == 43 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_43_3;
        } else if (player.playerTask.taskMain.id == 43 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_43_4;
        } else if (player.playerTask.taskMain.id == 43 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_43_5;
        } else if (player.playerTask.taskMain.id == 43 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_43_6;
        } else if (player.playerTask.taskMain.id == 44 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_44_0;
        } else if (player.playerTask.taskMain.id == 44 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_44_1;
        } else if (player.playerTask.taskMain.id == 44 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_44_2;
        } else if (player.playerTask.taskMain.id == 44 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_44_3;
        } else if (player.playerTask.taskMain.id == 44 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_44_4;
        } else if (player.playerTask.taskMain.id == 44 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_44_5;
        } else if (player.playerTask.taskMain.id == 44 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_44_6;
        } else if (player.playerTask.taskMain.id == 45 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_45_0;
        } else if (player.playerTask.taskMain.id == 45 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_45_1;
        } else if (player.playerTask.taskMain.id == 45 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_45_2;
        } else if (player.playerTask.taskMain.id == 45 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_45_3;
        } else if (player.playerTask.taskMain.id == 45 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_45_4;
        } else if (player.playerTask.taskMain.id == 45 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_45_5;
        } else if (player.playerTask.taskMain.id == 45 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_45_6;
        } else if (player.playerTask.taskMain.id == 46 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_46_0;
        } else if (player.playerTask.taskMain.id == 46 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_46_1;
        } else if (player.playerTask.taskMain.id == 46 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_46_2;
        } else if (player.playerTask.taskMain.id == 46 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_46_3;
        } else if (player.playerTask.taskMain.id == 46 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_46_4;
        } else if (player.playerTask.taskMain.id == 46 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_46_5;
        } else if (player.playerTask.taskMain.id == 46 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_46_6;
        } else if (player.playerTask.taskMain.id == 47 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_47_0;
        } else if (player.playerTask.taskMain.id == 47 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_47_1;
        } else if (player.playerTask.taskMain.id == 47 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_47_2;
        } else if (player.playerTask.taskMain.id == 47 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_47_3;
        } else if (player.playerTask.taskMain.id == 47 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_47_4;
        } else if (player.playerTask.taskMain.id == 47 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_47_5;
        } else if (player.playerTask.taskMain.id == 47 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_47_6;
        } else if (player.playerTask.taskMain.id == 48 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_48_0;
        } else if (player.playerTask.taskMain.id == 48 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_48_1;
        } else if (player.playerTask.taskMain.id == 48 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_48_2;
        } else if (player.playerTask.taskMain.id == 48 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_48_3;
        } else if (player.playerTask.taskMain.id == 48 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_48_4;
        } else if (player.playerTask.taskMain.id == 48 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_48_5;
        } else if (player.playerTask.taskMain.id == 48 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_48_6;
        } else if (player.playerTask.taskMain.id == 49 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_49_0;
        } else if (player.playerTask.taskMain.id == 49 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_49_1;
        } else if (player.playerTask.taskMain.id == 49 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_49_2;
        } else if (player.playerTask.taskMain.id == 49 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_49_3;
        } else if (player.playerTask.taskMain.id == 49 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_49_4;
        } else if (player.playerTask.taskMain.id == 49 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_49_5;
        } else if (player.playerTask.taskMain.id == 49 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_49_6;
        } else if (player.playerTask.taskMain.id == 50 && player.playerTask.taskMain.index == 0) {
            return ConstTask.TASK_50_0;
        } else if (player.playerTask.taskMain.id == 50 && player.playerTask.taskMain.index == 1) {
            return ConstTask.TASK_50_1;
        } else if (player.playerTask.taskMain.id == 50 && player.playerTask.taskMain.index == 2) {
            return ConstTask.TASK_50_2;
        } else if (player.playerTask.taskMain.id == 50 && player.playerTask.taskMain.index == 3) {
            return ConstTask.TASK_50_3;
        } else if (player.playerTask.taskMain.id == 50 && player.playerTask.taskMain.index == 4) {
            return ConstTask.TASK_50_4;
        } else if (player.playerTask.taskMain.id == 50 && player.playerTask.taskMain.index == 5) {
            return ConstTask.TASK_50_5;
        } else if (player.playerTask.taskMain.id == 50 && player.playerTask.taskMain.index == 6) {
            return ConstTask.TASK_50_6;
        } else {
            return -1;
        }
    }

    // --------------------------------------------------------------------------
    public SideTaskTemplate getSideTaskTemplateById(int id) {
        if (id != -1) {
            return Manager.SIDE_TASKS_TEMPLATE.get(id);
        }
        return null;
    }

    public void changeSideTask(Player player, byte level) {
        if (player.playerTask.sideTask.leftTask > 0) {
            player.playerTask.sideTask.reset();
            SideTaskTemplate temp = Manager.SIDE_TASKS_TEMPLATE
                    .get(Util.nextInt(0, Manager.SIDE_TASKS_TEMPLATE.size() - 1));
            player.playerTask.sideTask.template = temp;
            player.playerTask.sideTask.maxCount = Util.nextInt(temp.count[level][0], temp.count[level][1]);
            player.playerTask.sideTask.leftTask--;
            player.playerTask.sideTask.level = level;
            player.playerTask.sideTask.receivedTime = System.currentTimeMillis();
            NpcService.gI().createTutorial(player, NpcService.gI().getAvatar(ConstNpc.BO_MONG),
                    "Nhiệm vụ cấp độ " + player.playerTask.sideTask.getLevel() + ":\b"
                            + player.playerTask.sideTask.getName() + "\b"
                            + "Hiện tại đã hoàn thành: "
                            + player.playerTask.sideTask.count + "/"
                            + player.playerTask.sideTask.maxCount + "\b"
                            + "Thời gian nhận nhiệm vụ: 0 giây trước");
            send_text_time_nhiem_vu(player);
        } else {
            Service.getInstance().sendThongBao(player,
                    "Bạn đã nhận hết nhiệm vụ hôm nay. Hãy chờ tới ngày mai rồi nhận tiếp");
        }
    }

    public void send_text_time_nhiem_vu(Player player) {
        if (player.playerTask.sideTask.template != null) {
            ItemTimeService.gI().sendTextTime(player, TEXT_NHIEM_VU_HANG_NGAY,
                    "Nhiệm vụ hằng ngày: " + player.playerTask.sideTask.getName() + " ("
                            + player.playerTask.sideTask.getPercentProcess() + "%)",
                    20);
        }
        player.lastimelogin3 = System.currentTimeMillis();
    }

    public void removeSideTask(Player player) {
        Service.getInstance().sendThongBao(player,
                "Nhiệm vụ là " + player.playerTask.sideTask.getName() + " đã bị hủy bỏ");
        player.playerTask.sideTask.reset();
    }

    public void paySideTask(Player player) {
        if (player.playerTask.sideTask.template != null) {
            if (player.playerTask.sideTask.isDone()) {
                int goldReward = 0;
                switch (player.playerTask.sideTask.level) {
                    case ConstTask.EASY:
                        goldReward = ConstTask.GOLD_EASY;
                        break;
                    case ConstTask.NORMAL:
                        goldReward = ConstTask.GOLD_NORMAL;
                        break;
                    case ConstTask.HARD:
                        goldReward = ConstTask.GOLD_HARD;
                        break;
                    case ConstTask.VERY_HARD:
                        goldReward = ConstTask.GOLD_VERY_HARD;
                        break;
                    case ConstTask.HELL:
                        goldReward = ConstTask.GOLD_HELL;
                        break;
                }
                if (Manager.EVENT_SEVER == 4) {
                    Item it = ItemService.gI().createNewItem((short) ConstItem.DO_XANH);
                    it.itemOptions.add(new ItemOption(74, 0));
                    InventoryService.gI().addItemBag(player, it, 0);
                    InventoryService.gI().sendItemBags(player);
                }
                Item it = ItemService.gI().createNewItem((short) 931);
                Item it1 = ItemService.gI().createNewItem((short) 930);
                Item it2 = ItemService.gI().createNewItem((short) 929);
                Item it3 = ItemService.gI().createNewItem((short) 928);

                // Tạo mảng các item
                Item[] items = { it, it1, it2, it3 };

                // Chọn ngẫu nhiên một item từ mảng
                int randomIndex = (int) (Math.random() * items.length);
                Item selectedItem = items[randomIndex];

                // Thêm item vào túi của người chơi
                InventoryService.gI().addItemBag(player, selectedItem, 0);

                // Cập nhật và gửi thông tin túi cho người chơi
                InventoryService.gI().sendItemBags(player);
                Service.getInstance().sendThongBao(player,
                        "Bạn nhận được " + selectedItem.template.name);
                player.inventory.addGold(goldReward);
                Service.getInstance().sendMoney(player);
                Service.getInstance().sendThongBao(player, "Bạn nhận được "
                        + Util.numberToMoney(goldReward) + " vàng");
                try (
                        Connection con = DBService.gI().getConnectionForSaveData();
                        PreparedStatement ps = con
                                .prepareStatement("UPDATE account SET NangDong = NangDong + 1 WHERE id = ?")) {
                    ps.setInt(1, player.getSession().userId); // ID của người chơi
                    ps.executeUpdate();
                    Service.getInstance().sendThongBao(player, "Bạn nhận được 1 điểm năng động");
                } catch (Exception e) {
                    Log.error(PlayerDAO.class, e, "Lỗi update top năng động cho người chơi " + player.name);
                }
                player.playerTask.sideTask.reset();
            } else {
                Service.getInstance().sendThongBao(player, "Bạn chưa hoàn thành nhiệm vụ");
            }
        }
    }

    public void checkDoneSideTaskKillMob(Player player, Mob mob) {
        if (player.playerTask.sideTask.template != null) {
            if ((player.playerTask.sideTask.template.id == 0 && mob.tempId == ConstMob.KHUNG_LONG)
                    || (player.playerTask.sideTask.template.id == 1 && mob.tempId == ConstMob.LON_LOI)
                    || (player.playerTask.sideTask.template.id == 2 && mob.tempId == ConstMob.QUY_DAT)
                    || (player.playerTask.sideTask.template.id == 3 && mob.tempId == ConstMob.KHUNG_LONG_ME)
                    || (player.playerTask.sideTask.template.id == 4 && mob.tempId == ConstMob.LON_LOI_ME)
                    || (player.playerTask.sideTask.template.id == 5 && mob.tempId == ConstMob.QUY_DAT_ME)
                    || (player.playerTask.sideTask.template.id == 6 && mob.tempId == ConstMob.THAN_LAN_BAY)
                    || (player.playerTask.sideTask.template.id == 7 && mob.tempId == ConstMob.PHI_LONG)
                    || (player.playerTask.sideTask.template.id == 8 && mob.tempId == ConstMob.QUY_BAY)
                    || (player.playerTask.sideTask.template.id == 9 && mob.tempId == ConstMob.THAN_LAN_ME)
                    || (player.playerTask.sideTask.template.id == 10 && mob.tempId == ConstMob.PHI_LONG_ME)
                    || (player.playerTask.sideTask.template.id == 11 && mob.tempId == ConstMob.QUY_BAY_ME)
                    || (player.playerTask.sideTask.template.id == 12 && mob.tempId == ConstMob.HEO_RUNG)
                    || (player.playerTask.sideTask.template.id == 13 && mob.tempId == ConstMob.HEO_DA_XANH)
                    || (player.playerTask.sideTask.template.id == 14 && mob.tempId == ConstMob.HEO_XAYDA)
                    || (player.playerTask.sideTask.template.id == 15 && mob.tempId == ConstMob.OC_MUON_HON)
                    || (player.playerTask.sideTask.template.id == 16 && mob.tempId == ConstMob.OC_SEN)
                    || (player.playerTask.sideTask.template.id == 17 && mob.tempId == ConstMob.HEO_XAYDA_ME)
                    || (player.playerTask.sideTask.template.id == 18 && mob.tempId == ConstMob.KHONG_TAC)
                    || (player.playerTask.sideTask.template.id == 19 && mob.tempId == ConstMob.QUY_DAU_TO)
                    || (player.playerTask.sideTask.template.id == 20 && mob.tempId == ConstMob.QUY_DIA_NGUC)
                    || (player.playerTask.sideTask.template.id == 21 && mob.tempId == ConstMob.HEO_RUNG_ME)
                    || (player.playerTask.sideTask.template.id == 22 && mob.tempId == ConstMob.HEO_XANH_ME)
                    || (player.playerTask.sideTask.template.id == 23 && mob.tempId == ConstMob.ALIEN)
                    || (player.playerTask.sideTask.template.id == 24 && mob.tempId == ConstMob.TAMBOURINE)
                    || (player.playerTask.sideTask.template.id == 25 && mob.tempId == ConstMob.DRUM)
                    || (player.playerTask.sideTask.template.id == 26 && mob.tempId == ConstMob.AKKUMAN)
                    || (player.playerTask.sideTask.template.id == 27 && mob.tempId == ConstMob.NAPPA)
                    || (player.playerTask.sideTask.template.id == 28 && mob.tempId == ConstMob.SOLDIER)
                    || (player.playerTask.sideTask.template.id == 29 && mob.tempId == ConstMob.APPULE)
                    || (player.playerTask.sideTask.template.id == 30 && mob.tempId == ConstMob.RASPBERRY)
                    || (player.playerTask.sideTask.template.id == 31 && mob.tempId == ConstMob.THAN_LAN_XANH)
                    || (player.playerTask.sideTask.template.id == 32 && mob.tempId == ConstMob.QUY_DAU_NHON)
                    || (player.playerTask.sideTask.template.id == 33 && mob.tempId == ConstMob.QUY_DAU_VANG)
                    || (player.playerTask.sideTask.template.id == 34 && mob.tempId == ConstMob.QUY_DA_TIM)
                    || (player.playerTask.sideTask.template.id == 35 && mob.tempId == ConstMob.QUY_GIA)
                    || (player.playerTask.sideTask.template.id == 36 && mob.tempId == ConstMob.CA_SAU)
                    || (player.playerTask.sideTask.template.id == 37 && mob.tempId == ConstMob.DOI_DA_XANH)
                    || (player.playerTask.sideTask.template.id == 38 && mob.tempId == ConstMob.QUY_CHIM)
                    || (player.playerTask.sideTask.template.id == 39 && mob.tempId == ConstMob.LINH_DAU_TROC)
                    || (player.playerTask.sideTask.template.id == 40 && mob.tempId == ConstMob.LINH_TAI_DAI)
                    || (player.playerTask.sideTask.template.id == 41 && mob.tempId == ConstMob.LINH_VU_TRU)
                    || (player.playerTask.sideTask.template.id == 42 && mob.tempId == ConstMob.KHI_LONG_DEN)
                    || (player.playerTask.sideTask.template.id == 43 && mob.tempId == ConstMob.KHI_GIAP_SAT)
                    || (player.playerTask.sideTask.template.id == 44 && mob.tempId == ConstMob.KHI_LONG_DO)
                    || (player.playerTask.sideTask.template.id == 45 && mob.tempId == ConstMob.KHI_LONG_VANG)
                    || (player.playerTask.sideTask.template.id == 46 && mob.tempId == ConstMob.XEN_CON_CAP_1)
                    || (player.playerTask.sideTask.template.id == 47 && mob.tempId == ConstMob.XEN_CON_CAP_2)
                    || (player.playerTask.sideTask.template.id == 48 && mob.tempId == ConstMob.XEN_CON_CAP_3)
                    || (player.playerTask.sideTask.template.id == 49 && mob.tempId == ConstMob.XEN_CON_CAP_4)
                    || (player.playerTask.sideTask.template.id == 50 && mob.tempId == ConstMob.XEN_CON_CAP_5)
                    || (player.playerTask.sideTask.template.id == 51 && mob.tempId == ConstMob.XEN_CON_CAP_6)
                    || (player.playerTask.sideTask.template.id == 52 && mob.tempId == ConstMob.XEN_CON_CAP_7)
                    || (player.playerTask.sideTask.template.id == 53 && mob.tempId == ConstMob.XEN_CON_CAP_8)
                    || (player.playerTask.sideTask.template.id == 54 && mob.tempId == ConstMob.TAI_TIM)
                    || (player.playerTask.sideTask.template.id == 55 && mob.tempId == ConstMob.ABO)
                    || (player.playerTask.sideTask.template.id == 56 && mob.tempId == ConstMob.KADO)
                    || (player.playerTask.sideTask.template.id == 57 && mob.tempId == ConstMob.DA_XANH)) {
                player.playerTask.sideTask.count++;
                notifyProcessSideTask(player);
            }
        }
    }

    public void checkDoneSideTaskPickItem(Player player, ItemMap item) {
        if (player.playerTask.sideTask.template != null) {
            if ((player.playerTask.sideTask.template.id == 58 && item.itemTemplate.type == 9)) {
                player.playerTask.sideTask.count += item.quantity;
                notifyProcessSideTask(player);
            }
        }
    }

    private void notifyProcessSideTask(Player player) {
        int percentDone = player.playerTask.sideTask.getPercentProcess();
        boolean notify = false;
        if (percentDone != 100) {
            if (!player.playerTask.sideTask.notify90 && percentDone >= 90) {
                player.playerTask.sideTask.notify90 = true;
                notify = true;
            } else if (!player.playerTask.sideTask.notify80 && percentDone >= 80) {
                player.playerTask.sideTask.notify80 = true;
                notify = true;
            } else if (!player.playerTask.sideTask.notify70 && percentDone >= 70) {
                player.playerTask.sideTask.notify70 = true;
                notify = true;
            } else if (!player.playerTask.sideTask.notify60 && percentDone >= 60) {
                player.playerTask.sideTask.notify60 = true;
                notify = true;
            } else if (!player.playerTask.sideTask.notify50 && percentDone >= 50) {
                player.playerTask.sideTask.notify50 = true;
                notify = true;
            } else if (!player.playerTask.sideTask.notify40 && percentDone >= 40) {
                player.playerTask.sideTask.notify40 = true;
                notify = true;
            } else if (!player.playerTask.sideTask.notify30 && percentDone >= 30) {
                player.playerTask.sideTask.notify30 = true;
                notify = true;
            } else if (!player.playerTask.sideTask.notify20 && percentDone >= 20) {
                player.playerTask.sideTask.notify20 = true;
                notify = true;
            } else if (!player.playerTask.sideTask.notify10 && percentDone >= 10) {
                player.playerTask.sideTask.notify10 = true;
                notify = true;
            } else if (!player.playerTask.sideTask.notify0 && percentDone >= 0) {
                player.playerTask.sideTask.notify0 = true;
                notify = true;
            }
            if (notify) {
                Service.getInstance().sendThongBao(player, "Nhiệm vụ: "
                        + player.playerTask.sideTask.getName() + " đã hoàn thành: "
                        + player.playerTask.sideTask.count + "/" + player.playerTask.sideTask.maxCount + " ("
                        + percentDone + "%)");
            }
        } else {

            Service.getInstance().sendThongBao(player, "Chúc mừng bạn đã hoàn thành nhiệm vụ, "
                    + "bây giờ hãy quay về Bò Mộng trả nhiệm vụ.");
            ///

            ///
        }
    }

    public void sendAchivement(Player player) {
        List<Achivement> achivements = player.playerTask.achivements;
        Message m = new Message(Cmd.ACHIEVEMENT);
        DataOutputStream ds = m.writer();
        try {
            ds.writeByte(0);
            ds.writeByte(achivements.size());
            for (Achivement a : achivements) {
                String detail = String.format(a.getDetail(), a.getCount(player), a.getMaxCount());
                ds.writeUTF(a.getName());
                if (a.getName().equals("Lần đầu nạp ngọc")) {
                    ds.writeUTF("Nạp ít nhất 150 ngọc");
                } else if (a.getName().equals("Trùm nhặt ve chai")) {
                    if (player.gender == 0) {
                        ds.writeUTF("Bán cho Bunma 200 món đồ");
                    } else if (player.gender == 1) {
                        ds.writeUTF("Bán cho Dende 200 món đồ");
                    } else {
                        ds.writeUTF(detail);
                    }
                } else {
                    ds.writeUTF(detail);
                }
                ds.writeShort(a.getMoney());
                ds.writeBoolean(a.isFinish());
                ds.writeBoolean(a.isReceive());
            }
            ds.flush();
            player.sendMessage(m);
            m.cleanup();
        } catch (IOException e) {

        }
    }

    public void rewardAchivement(Player player, byte id) {
        Achivement achivement = player.playerTask.achivements.get(id);
        if (achivement.isFinish()) {
            player.inventory.ruby += achivement.getMoney();
            Service.getInstance().sendMoney(player);
            achivement.setReceive(true);
            sendAchivement(player);
            Service.getInstance().sendThongBao(player, "Bạn nhận được " + achivement.getMoney() + " hồng ngọc");
        }
    }

    public void checkDoneAchivements(Player player) {
        List<Achivement> list = player.playerTask.achivements;
        for (Achivement achivement : list) {
            if (achivement.getId() == ConstAchive.GIA_NHAP_THAN_CAP
                    || achivement.getId() == ConstAchive.SUC_MANH_GIOI_VUONG_THAN) {
                if (achivement.isDone(player)) {
                    achivement.setFinish(true);
                }
            } else if (achivement.isDone(player)) {
                achivement.setFinish(true);
            }
        }
    }
}
