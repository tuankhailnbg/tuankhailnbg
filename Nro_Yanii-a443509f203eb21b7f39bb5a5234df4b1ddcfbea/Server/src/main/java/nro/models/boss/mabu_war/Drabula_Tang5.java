package nro.models.boss.mabu_war;

import nro.consts.ConstRatio;
import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.map.ItemMap;
import nro.models.map.Zone;
import nro.models.player.Player;
import nro.models.skill.Skill;
import nro.services.MapService;
import nro.services.Service;
import nro.services.SkillService;
import nro.services.func.ChangeMapService;
import nro.utils.Log;
import nro.utils.SkillUtil;
import nro.utils.Util;
import nro.models.boss.cell.XenBoHung;
import nro.services.TaskService;

/**
 * @author outcast c-cute hột me 😳
 */
public class Drabula_Tang5 extends BossMabuWar {

    public Drabula_Tang5(int mapID, int zoneId) {
        super(BossFactory.DRABULA_TANG5, BossData.DRABULA_TANG5);
        this.mapID = mapID;
        this.zoneId = zoneId;
    }

    @Override
    public void attack() {
        if (isDie()) {
            die();
        }
        try {
            Player pl = getPlayerAttack();
            if (pl != null) {
                if (!useSpecialSkill()) {
                    this.playerSkill.skillSelect = this.getSkillAttack();
                    if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                        if (Util.isTrue(15, ConstRatio.PER100) && SkillUtil.isUseSkillChuong(this)) {
                            goToXY(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 80)),
                                    Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 50), false);
                        }
                        SkillService.gI().useSkill(this, pl, null, null);
                        checkPlayerDie(pl);
                    } else {
                        goToPlayer(pl, false);
                    }
                }
            }
        } catch (Exception ex) {
            Log.error(XenBoHung.class, ex);
        }
    }

    @Override
    public void idle() {

    }

    @Override
    public int injured(Player plAtt, int damage, boolean piercing, boolean isMobAttack) {
        if (this.nPoint != null) {
            if (this.nPoint.hp < (this.nPoint.hpg * 50 / 100)) {
                changeStatus(TALK_BEFORE);
            }
        }
        if (this.isDie()) {
            return 0;
        } else {
            if (plAtt != null) {
                if (Util.isTrue(20, 100)) {
                    plAtt.addPercentPowerPoint(1);
                    Service.getInstance().sendPowerInfo(plAtt, "%", plAtt.getPercentPowerPont());
                }
                int skill = plAtt.playerSkill.skillSelect.template.id;
                if (skill == Skill.KAMEJOKO || skill == Skill.ANTOMIC || skill == Skill.MASENKO || skill == Skill.LIEN_HOAN) {
                    damage = 1;
                    Service.getInstance().chat(this, "Chưởng trúng cho con bò..");
                } else if (Util.isTrue(25,100)) {
                    damage = 1;
                    Service.getInstance().chat(this, "Xí hụt..");
                }
            }
            int dame = super.injuredNotCheckDie(plAtt, damage, piercing);
            if (this.isDie()) {
                rewards(plAtt);
            }
            if (this.nPoint.getCurrPercentHP() < 50) {
                this.leaveMap();
            }
            return dame;
        }
    }

    @Override
    public void changeToAttack() {
        this.nPoint.wearingDrabula = true;
        Service.getInstance().changeFlag(this, 10);
        changeStatus(ATTACK);
    }

    @Override
    public void rewards(Player pl) {
        if (Util.isTrue(1, 15)) {
            ItemMap itemMap = new ItemMap(this.zone, 861, 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x, 100), -1);
            Service.getInstance().dropItemMap(this.zone, itemMap);
        }

        TaskService.gI().checkDoneTaskKillBoss(pl, this);
        generalRewards(pl);
    }

    @Override
    public void checkPlayerDie(Player pl) {
        pl.getPowerPoint();
    }

    @Override
    public void joinMap() {
        this.zone = getMapCanJoin(mapID);
        int x = Util.nextInt(50, this.zone.map.mapWidth - 50);
        ChangeMapService.gI().changeMap(this, this.zone, x, this.zone.map.yPhysicInTop(x, 0));
    }

    @Override
    public Zone getMapCanJoin(int mapId) {
        Zone map = MapService.gI().getZoneJoinByMapIdAndZoneId(this, mapId, zoneId);
        if (map.isBossCanJoin(this)) {
            return map;
        } else {
            return getMapCanJoin(mapJoin[Util.nextInt(0, mapJoin.length - 1)]);
        }
    }

    @Override
    public void initTalk() {
        this.textTalkBefore = new String[]{"Hế nhô mấy cưngggg"};
        this.textTalkMidle = new String[]{};
        this.textTalkAfter = new String[]{"Hêhê ta chẳng cần tốn sức đánh với các ngươi nữa", "Mà sẽ để cho các ngươi tự thanh toán lẫn nhau,xin chào"};
    }

    @Override
    public boolean talk() {
        switch (status) {
            case TALK_AFTER:
                if (this.textTalkAfter == null || this.textTalkAfter.length == 0) {
                    return true;
                }
                if (Util.canDoWithTime(lastTimeTalk, 5000)) {
                    this.chat(textTalkAfter[indexTalkAfter++]);
                    if (indexTalkAfter >= textTalkAfter.length) {
                        return true;
                    }
                    lastTimeTalk = System.currentTimeMillis();
                }
                break;
        }
        return false;
    }

    @Override
    public void leaveMap() {
        Boss boss = BossFactory.createBoss(BossFactory.GOKU_TANG5);
        boss.zone = this.zone;
        boss = BossFactory.createBoss(BossFactory.CADIC_TANG5);
        boss.zone = this.zone;
        super.leaveMap();
        this.changeToIdle();
    }
}
