package nro.models.boss.mabu_war;

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
import nro.utils.Util;

/**
 * @author outcast c-cute hột me 😳
 */
public class Goku_Tang5 extends BossMabuWar {

    public Goku_Tang5(int mapID, int zoneId) {
        super(BossFactory.GOKU_TANG5, BossData.GOKU_TANG5);
        this.mapID = mapID;
        this.zoneId = zoneId;
    }

    @Override
    public void idle() {

    }

    @Override
    public int injured(Player plAtt, int damage, boolean piercing, boolean isMobAttack) {
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
            int dame = super.injured(plAtt, damage, piercing, isMobAttack);
            if (this.isDie()) {
                rewardOnDefeat(plAtt);
            }
            return dame;
        }
    }

    @Override
    public void changeToAttack() {
        Service.getInstance().changeFlag(this, 9);
        changeStatus(ATTACK);
    }

    @Override
    public void rewards(Player pl) {
        // Hồng Ngọc được quay một lần trong phần thưởng chung của mọi boss.
        generalRewards(pl);
    }

    @Override
    public void checkPlayerDie(Player pl) {

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
//        if (map.isBossCanJoin(this)) {
        return map;
//        } else {
//            return getMapCanJoin(mapJoin[Util.nextInt(0, mapJoin.length - 1)]);
//        }
    }

    @Override
    public void initTalk() {
        this.textTalkBefore = new String[]{"Xin chào,ta đã quay trở lại rồi đây"};
        this.textTalkMidle = new String[]{};
        this.textTalkAfter = new String[]{"Đừng vội mừng,ta sẽ hồi sinh và giết hết bọn mi"};
    }

    @Override
    public void leaveMap() {
        super.leaveMap();
        this.changeToIdle();
    }
}
