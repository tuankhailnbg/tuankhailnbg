package nro.models.boss.mabu_war;

import nro.consts.ConstRatio;
import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.map.ItemMap;
import nro.models.map.Zone;
import nro.models.map.mabu.MabuWar;
import nro.models.player.Player;
import nro.models.skill.Skill;
import nro.services.MapService;
import nro.services.Service;
import nro.services.SkillService;
import nro.services.TaskService;
import nro.services.func.ChangeMapService;
import nro.utils.Log;
import nro.utils.SkillUtil;
import nro.utils.Util;

/**
 * @author outcast c-cute hột me 😳
 */
public class Drabula_Tang6 extends BossMabuWar {

    public Drabula_Tang6(int mapID, int zoneId) {
        super(BossFactory.DRABULA_TANG6, BossData.DRABULA_TANG6);
        this.mapID = mapID;
        this.zoneId = zoneId;
    }

    @Override
    public void idle() {

    }

    @Override
    public void attack() {
        if (this.isDie()) {
            int percent = ((100 - this.nPoint.getCurrPercentHP()) * 2);
            this.zone.percentMabuEgg = (byte) (percent > 100 ? 100 : percent);
            die();
            return;
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
            Log.error(Drabula_Tang6.class, ex);
        }
    }

    @Override
    public int injured(Player plAtt, int damage, boolean piercing, boolean isMobAttack) {
        int percent = ((100 - this.nPoint.getCurrPercentHP()) * 2);
        this.zone.percentMabuEgg = (byte) (percent > 100 ? 100 : percent);
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
            if (this.zone.percentMabuEgg == 100) {
                MabuWar.gI().BabidayTalk(this, "Mabư ! Hãy theo lệnh ta,giết hết bọn chúng đi");
                MabuWar.gI().initMabu(this.zone);
                this.leaveMap();
            }
            return dame;
        }
    }

    @Override
    public void die() {
        MabuWar.gI().BabidayTalk(this, "Mabư ! Hãy theo lệnh ta,giết hết bọn chúng đi");
        MabuWar.gI().initMabu(this.zone);
        this.leaveMap();
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
        this.nPoint.wearingDrabula = true;
    }

    @Override
    public Zone getMapCanJoin(int mapId) {
        Zone map = MapService.gI().getZoneJoinByMapIdAndZoneId(this, mapId, zoneId);
//        if (map.isBossCanJoin(this)) {
        return map;
//        } else {
//            return getMapCanJoin(mapJoin[Util.nextInt(0, mapJoin.length - 1)]);
//        }
    }

    @Override
    public void initTalk() {
        this.textTalkBefore = new String[]{};
        this.textTalkMidle = new String[]{};
        this.textTalkAfter = new String[]{};
    }

    @Override
    public void leaveMap() {
        super.leaveMap();
        this.changeToIdle();
    }
}
