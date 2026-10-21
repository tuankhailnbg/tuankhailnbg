package nro.services;

import nro.consts.ConstMob;
import nro.consts.ConstTask;
import nro.models.boss.BossFactory;
import nro.models.clan.Clan;
import nro.models.clan.ClanMember;
import nro.models.map.ItemMap;
import nro.models.mob.Mob;
import nro.models.player.Pet;
import nro.models.player.Player;
import nro.server.io.Message;
import nro.utils.Log;
import nro.utils.Util;

import java.util.ArrayList;
import java.util.List;
import nro.consts.ConstItem;
import nro.models.map.Zone;
import nro.models.mob.MobFactory;
import nro.models.mob.MobTemplate;
import nro.server.Manager;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class MobService {

    private static MobService i;

    private MobService() {

    }

    public static MobService gI() {
        if (i == null) {
            i = new MobService();
        }
        return i;
    }

    public void sendMobStillAliveAffterAttacked(Mob mob, int dameHit, boolean crit) {
        Message msg;
        try {
            msg = new Message(-9);
            msg.writer().writeByte(mob.id);
            msg.writer().writeInt(mob.point.getHP());
            msg.writer().writeInt(dameHit);
            msg.writer().writeBoolean(crit); // chí mạng
            msg.writer().writeInt(-1);
            Service.getInstance().sendMessAllPlayerInMap(mob.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
            Log.error(MobService.class, e);
        }
    }

    public void sendMobDieAffterAttacked(Mob mob, Player plKill, int dameHit) {
        Message msg;
        try {
            msg = new Message(-12);
            msg.writer().writeByte(mob.id);
            msg.writer().writeInt(dameHit);
            msg.writer().writeBoolean(plKill.nPoint.isCrit); // crit
            List<ItemMap> items = mobReward(mob, plKill, msg);
            Service.getInstance().sendMessAllPlayerInMap(mob.zone, msg);
            msg.cleanup();
            hutItem(plKill, items);
        } catch (Exception e) {
            // Logger.logException(MobService.class, e);
            e.printStackTrace();
        }
    }

    public void callBigBoss(Zone zone2, int idBigBoss, int hp, int x, int y, int mapID) {
        MobTemplate temp = Manager.getMobTemplateByTemp(idBigBoss);
        if (temp != null) {
            Mob mob = new Mob();
            mob.tempId = idBigBoss;
            mob.level = 10;
            mob.point.setHpFull((int) hp);
            mob.location.x = 827;
            mob.location.y = 336;
            mob.point.setHP(mob.point.getHpFull());
            mob.pDame = temp.percentDame;
            mob.pTiemNang = temp.percentTiemNang;
            mob.setTiemNang();
            mob.status = 5;
            for (Zone zone : zone2.map.zones) {
                if (!zone.isZoneHaveBigBoss) {
                    Mob mobZone = MobFactory.newMob(mob);
                    mobZone.zone = zone;
                    zone.addMob(mobZone);
                    zone.isZoneHaveBigBoss = true;
                }

            }
        }
    }

    private void hutItem(Player player, List<ItemMap> items) {
        if (!player.isPet) {
            if (player.charms.tdThuHut > System.currentTimeMillis()) {
                for (ItemMap item : items) {
                    if (item.itemTemplate.id != 590) {
                        ItemMapService.gI().pickItem(player, item.itemMapId);
                    }
                }
            }
        } else {
            if (((Pet) player).master.charms.tdThuHut > System.currentTimeMillis()) {
                for (ItemMap item : items) {
                    if (item.itemTemplate.id != 590) {
                        ItemMapService.gI().pickItem(((Pet) player).master, item.itemMapId);
                    }
                }
            }
        }
    }

    private List<ItemMap> mobReward(Mob mob, Player player, Message msg) {
        List<ItemMap> itemReward = new ArrayList<>();
        try {
            itemReward = RewardService.gI().getRewardItems(player, mob,
                    mob.location.x + Util.nextInt(-10, 10), mob.zone.map.yPhysicInTop(mob.location.x, mob.location.y));
            msg.writer().writeByte(itemReward.size()); // sl item roi
            for (ItemMap itemMap : itemReward) {
                msg.writer().writeShort(itemMap.itemMapId);// itemmapid
                msg.writer().writeShort(itemMap.itemTemplate.id); // id item
                msg.writer().writeShort(itemMap.x); // xend item
                msg.writer().writeShort(itemMap.y); // yend item
                msg.writer().writeInt((int) itemMap.playerId); // id nhan nat
            }
        } catch (Exception e) {
            Log.error(MobService.class, e);
        }
        return itemReward;
    }

    public int mobAttackPlayer(Mob mob, Player player) {
        int dameMob = mob.point.getDameAttack();
        if (player.charms.tdDaTrau > System.currentTimeMillis()) {
            dameMob /= 2;
        }
        return player.injured(null, dameMob, false, true);
    }

    public void sendMobAttackMe(Mob mob, Player player, int dame) {
        if (!player.isPet) {
            Message msg;
            try {
                msg = new Message(-11);
                msg.writer().writeByte(mob.id);
                msg.writer().writeInt(dame); // dame
                player.sendMessage(msg);
                msg.cleanup();
            } catch (Exception e) {
                Log.error(MobService.class, e);
            }
        }
    }

    public void sendMobAttackPlayer(Mob mob, Player player) {
        Message msg;
        try {
            msg = new Message(-10);
            msg.writer().writeByte(mob.id);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeInt(player.nPoint.hp);
            Service.getInstance().sendMessAnotherNotMeInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            Log.error(MobService.class, e);
        }
    }

    public void hoiSinhMob(Mob mob) {
        boolean isDie = mob.isDie();
        mob.point.hp = mob.point.maxHp;
        mob.setTiemNang();
        if (isDie) {
            if (mob.tempId == 77 || mob.tempId == 83) {
                mob.zone.mobs.remove(mob);
                return;
            }
            Message msg;
            try {
                msg = new Message(-13);
                msg.writer().writeByte(mob.id);
                msg.writer().writeByte(mob.tempId);
                msg.writer().writeByte(mob.lvMob); // level mob
                msg.writer().writeInt((mob.point.hp));
                Service.getInstance().sendMessAllPlayerInMap(mob.zone, msg);
                msg.cleanup();
            } catch (Exception e) {
                Log.error(MobService.class, e);
            }
        }
    }

    public void hoiSinhMobDoanhTrai(Mob mob) {
        if (mob.tempId == ConstMob.BULON) {
            boolean haveTrungUyTrang = false;
            List<Player> bosses = mob.zone.getBosses();
            for (Player boss : bosses) {
                if (boss.id == BossFactory.TRUNG_UY_TRANG) {
                    haveTrungUyTrang = true;
                    break;
                }
            }
            if (haveTrungUyTrang) {
                hoiSinhMob(mob);
            }
        }
    }

    public void initMobDoanhTrai(Mob mob, Clan clan) {
        for (ClanMember cm : clan.getMembers()) {
            for (Player pl : clan.membersInGame) {
                if (pl.id == cm.id && pl.nPoint.hpMax >= mob.point.clanMemHighestHp) {
                    mob.point.clanMemHighestHp = pl.nPoint.hpMax;
                }
            }
        }
        mob.point.dame = mob.point.clanMemHighestHp / mob.point.xHpForDame;
        for (ClanMember cm : clan.getMembers()) {
            for (Player pl : clan.membersInGame) {
                if (pl.id == cm.id && pl.nPoint.dame >= mob.point.clanMemHighestDame) {
                    mob.point.clanMemHighestDame = pl.nPoint.dame;
                }
            }
        }
        mob.point.hp = mob.point.clanMemHighestDame * mob.point.xDameForHp;
    }

    public void initMobDoanhTrai(Mob mob, long point) {
        mob.point.hp = mob.point.maxHp = (int) (point / 10);
        mob.point.dame = mob.point.dame = (int) (point / 200);
    }

    public void initMobBanDoKhoBau(Mob mob, byte level) {
        mob.point.dame = level * 1250 * mob.level * 4;
        mob.point.maxHp = level * 9472 * mob.level * 2 + level * 4263 * mob.tempId;
        if (mob.tempId == 71 || mob.tempId == 72) {
            mob.point.maxHp *= 18;
        }
        if (mob.level == 9) {
            mob.lvMob = 1;
            mob.point.maxHp *= 10;
        }
    }

    public void initMobKhiGas(Mob mob, byte level) {

        mob.point.dame *= level;
        mob.point.maxHp *= level;

        if (mob.level == 10) {
            mob.lvMob = 1;
            mob.point.maxHp *= 10;
        }

    }

    public static void main(String[] args) {
        int level = 110;
        int tn = 100;
        tn += (level / 5 * 50);
        System.out.println(tn);
    }

    public void dropItemTask(Player player, Mob mob) {
        ItemMap itemMap = null;
        switch (mob.tempId) {
            case ConstMob.KHUNG_LONG:
            case ConstMob.LON_LOI:
            case ConstMob.QUY_DAT:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_2_0) {
                    itemMap = new ItemMap(mob.zone, 73, 1, mob.location.x, mob.location.y, player.id);
                }
                break;
            case ConstMob.THAN_LAN_ME:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_8_1) {
                    if (Util.isTrue(1, 3)) {
                        itemMap = new ItemMap(mob.zone, ConstItem.NGOC_RONG_7_SAO, 1, mob.location.x, mob.location.y,
                                player.id);
                    } else {
                        Service.getInstance().sendThongBao(player,
                                "Con thằn lằn mẹ này không giữ ngọc, hãy tìm con thằn lằn mẹ khác");
                    }
                }
            case ConstMob.OC_MUON_HON:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_14_1) {
                    // if (Util.isTrue(1, 3)) {
                    itemMap = new ItemMap(mob.zone, ConstItem.TRUYEN_TRANH, 1, mob.location.x, mob.location.y,
                            player.id);
                    // } else {
                    // Service.getInstance().sendThongBao(player, "Con ốc mượn hồn này không giữ
                    // truyện tranh, hãy thử tìm con ốc mượn hồn khác");
                    // }
                }
            case ConstMob.HEO_XAYDA_ME:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_14_1) {
                    // if (Util.isTrue(1, 3)) {
                    itemMap = new ItemMap(mob.zone, ConstItem.TRUYEN_TRANH, 1, mob.location.x, mob.location.y,
                            player.id);
                    // } else {
                    // Service.getInstance().sendThongBao(player, "Con heo xayda mẹ này không giữ
                    // truyện tranh, hãy thử tìm con heo xayda mẹ khác");
                    // }
                }
            case ConstMob.OC_SEN:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_14_1) {
                    // if (Util.isTrue(1, 3)) {
                    itemMap = new ItemMap(mob.zone, ConstItem.TRUYEN_TRANH, 1, mob.location.x, mob.location.y,
                            player.id);
                    // } else {
                    // Service.getInstance().sendThongBao(player, "Con ốc xên này không giữ truyện
                    // tranh, hãy thử tìm con ốc xên khác");
                    // }
                }
        }
        if (itemMap != null) {
            Service.getInstance().dropItemMap(mob.zone, itemMap);
        }
    }

    public boolean isMonterFly(int tempId) {
        return tempId >= 7 && tempId <= 12 || tempId == 25 || tempId >= 28 && tempId <= 31 || tempId == 33
                || tempId == 43 || tempId == 49 || tempId == 50 || tempId == 69 || tempId == 75 || tempId == 79;
    }
}
