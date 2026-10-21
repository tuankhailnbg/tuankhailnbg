package nro.models.boss.mabu_war;

import nro.consts.ConstItem;
import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.item.ItemOption;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.server.Manager;
import nro.services.RewardService;
import nro.services.Service;
import nro.services.SkillService;
import nro.utils.Util;

/**
 * @author outcast c-cute hột me 😳
 */
public class BossMabuWar  extends Boss {

    protected int mapID;

    protected int zoneId;

    public BossMabuWar(int id, BossData data) {
        super(id, data);
    }

    @Override
    protected boolean useSpecialSkill() {
        this.playerSkill.skillSelect = this.getSkillSpecial();
        if (SkillService.gI().canUseSkillWithCooldown(this)) {
            SkillService.gI().useSkill(this, null, null, null);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void rewards(Player pl) {
        // do than 1/20
        int[] tempIds1 = new int[]{563, 565, 567};
        int tempId = -1;
        if (Util.isTrue(1, 40)) {
            tempId = 992;
        } else if (Util.isTrue(1, 40)) {
            tempId = tempIds1[Util.nextInt(0, tempIds1.length - 1)];
        }
        if (Manager.EVENT_SEVER == 4 && tempId == -1) {
            tempId = ConstItem.LIST_ITEM_NLSK_TET_2023[Util.nextInt(0, ConstItem.LIST_ITEM_NLSK_TET_2023.length - 1)];
        }
        if (tempId != -1) {
            ItemMap itemMap = new ItemMap(this.zone, tempId, 1,
                    pl.location.x, this.zone.map.yPhysicInTop(pl.location.x, pl.location.y - 24), pl.id);
            if (tempId >= 2027 && tempId <= 2038) {
                itemMap.options.add(new ItemOption(74, 0));
            } else {
                RewardService.gI().initBaseOptionClothes(itemMap.itemTemplate.id, itemMap.itemTemplate.type, itemMap.options);
                RewardService.gI().initStarOption(itemMap, new RewardService.RatioStar[]{
                        new RewardService.RatioStar((byte) 1, 1, 2),
                        new RewardService.RatioStar((byte) 2, 1, 3),
                        new RewardService.RatioStar((byte) 3, 1, 4),
                        new RewardService.RatioStar((byte) 4, 1, 5),
                        new RewardService.RatioStar((byte) 5, 1, 6),
                        new RewardService.RatioStar((byte) 6, 1, 7),
                        new RewardService.RatioStar((byte) 7, 1, 8)
                });
            }
            Service.getInstance().dropItemMap(this.zone, itemMap);
        }
        generalRewards(pl);
    }

    @Override
    public void initTalk() {

    }

    @Override
    public void idle() {

    }

    @Override
    public void checkPlayerDie(Player pl) {

    }
}
