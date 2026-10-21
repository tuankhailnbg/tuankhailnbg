package nro.models.mob;

import nro.consts.Cmd;
import nro.models.map.Zone;
import nro.models.player.Player;
import nro.utils.SkillUtil;
import nro.services.Service;
import nro.utils.Util;
import nro.server.io.Message;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public final class MobMe extends Mob {

    private Player player;
    private final long lastTimeSpawn;
    private final int timeSurvive;

    public MobMe(Player player) {
        super();
        this.player = player;
        this.id = (int) player.id;
        int level = player.playerSkill.getSkillbyId(12).point;
        this.tempId = SkillUtil.getTempMobMe(level);
        this.point.maxHp = SkillUtil.getHPMobMe(player.nPoint.hpMax, level);
        this.point.dame = SkillUtil.getHPMobMe(player.nPoint.getDameAttack(false), level);
        if (this.player.setClothes.pikkoroDaimao == 5) {
            this.point.dame *= 2;
        }
        this.point.hp = this.point.maxHp;
        this.zone = player.zone;
        this.lastTimeSpawn = System.currentTimeMillis();
        this.timeSurvive = SkillUtil.getTimeSurviveMobMe(level);
        spawn();
    }

    @Override
    public void update() {
        if (Util.canDoWithTime(lastTimeSpawn, timeSurvive) && this.player.setClothes.pikkoroDaimao != 5) {
            this.mobMeDie();
            this.dispose();
        }
    }

    public void attack(Player pl, Mob mob) {
        Message msg;
        try {
            if (pl != null) {
                if (pl.nPoint.hp > this.point.dame) {
                    int dameHit = pl.injured(null, this.point.dame, true, true);
                    msg = new Message(Cmd.MOB_ME_UPDATE);
                    msg.writer().writeByte(2);

                    msg.writer().writeInt(this.id);
                    msg.writer().writeInt((int) pl.id);
                    msg.writer().writeInt(dameHit);
                    msg.writer().writeInt(pl.nPoint.hp);

                    Service.getInstance().sendMessAllPlayerInMap(this.zone, msg);
                    msg.cleanup();
                }
            }

            if (mob != null) {
                if (mob.point.getHP() > this.point.dame) {
                    long tnsm = mob.getTiemNangForPlayer(this.player, this.point.dame);
                    msg = new Message(Cmd.MOB_ME_UPDATE);
                    msg.writer().writeByte(3);
                    msg.writer().writeInt(this.id);
                    msg.writer().writeInt((int) mob.id);
                    mob.point.setHP(mob.point.getHP() - this.point.dame);
                    msg.writer().writeInt(mob.point.getHP());
                    msg.writer().writeInt(this.point.dame);
                    Service.getInstance().sendMessAllPlayerInMap(this.zone, msg);
                    msg.cleanup();
                    Service.getInstance().addSMTN(player, (byte) 2, tnsm, true);
                }
            }
        } catch (Exception e) {
        }
    }

    // tạo mobme
    public void spawn() {
        Message msg;
        try {
            msg = new Message(Cmd.MOB_ME_UPDATE);
            msg.writer().writeByte(0);// type
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(this.tempId);
            msg.writer().writeInt(this.point.hp);// hp mob
            Service.getInstance().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (Exception e) {

        }
    }

    public void goToMap(Zone zone) {
        if (zone != null) {
            this.removeMobInMap();
            this.zone = zone;
        }
    }

    // xóa mobme khỏi map
    private void removeMobInMap() {
        Message msg;
        try {
            msg = new Message(Cmd.MOB_ME_UPDATE);
            msg.writer().writeByte(7);// type
            msg.writer().writeInt((int) player.id);
            Service.getInstance().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void mobMeDie() {
        Message msg;
        try {
            msg = new Message(Cmd.MOB_ME_UPDATE);
            msg.writer().writeByte(6);// type
            msg.writer().writeInt((int) player.id);
            Service.getInstance().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void dispose() {
        player.mobMe = null;
        this.player = null;
    }
}
