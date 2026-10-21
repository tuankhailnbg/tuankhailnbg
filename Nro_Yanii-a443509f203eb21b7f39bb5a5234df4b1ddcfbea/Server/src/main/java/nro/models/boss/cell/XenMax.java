package nro.models.boss.cell;

import nro.consts.ConstItem;
import nro.consts.ConstRatio;
import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.boss.FutureBoss;
import nro.models.item.ItemOption;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.server.Manager;
import nro.services.RewardService;
import nro.services.Service;
import nro.services.SkillService;
import nro.utils.Log;
import nro.utils.SkillUtil;
import nro.utils.Util;

/**
 * @author outcast c-cute hột me 😳
 */
public class XenMax extends FutureBoss {

    public XenMax() {
        super(BossFactory.XEN_MAX, BossData.XEN_MAX);
    }

    @Override
    protected boolean useSpecialSkill() {
        return false;
    }

    @Override
    public void attack() {
        try {
            Player pl = getPlayerAttack();
            if (pl != null) {
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
        } catch (Exception ex) {
            Log.error(XenMax.class, ex);
        }
    }

    @Override
    public void idle() {
    }

    @Override
    public void rewards(Player pl) {
        if (pl != null) {
            int[] tempIds1 = new int[] { 555, 556, 563, 557, 558, 559, 567 };
            int[] tempIds2 = new int[] { 565, 560, 562, 564, 566, 561 };
            int[] tempIds3 = new int[] { 561 };
            int tempId = -1;
            int rand = Util.nextInt(1, 100);

            if (rand <= 2) {
                tempId = tempIds3[Util.nextInt(0, tempIds3.length - 1)];
            } else if (rand <= 10) {
                tempId = tempIds1[Util.nextInt(0, tempIds1.length - 1)];
            } else if (rand <= 15) {
                tempId = tempIds2[Util.nextInt(0, tempIds2.length - 1)];
            }

            if (tempId != -1) {
                ItemMap itemMap = new ItemMap(this.zone, tempId, 1,
                        pl.location.x, this.zone.map.yPhysicInTop(pl.location.x, pl.location.y - 24), pl.id);
                RewardService.gI().initBaseOptionClothes(itemMap.itemTemplate.id, itemMap.itemTemplate.type,
                        itemMap.options);
                Service.getInstance().dropItemMap(this.zone, itemMap);
            }
        }
        generalRewards(pl);
    }

    @Override
    public void checkPlayerDie(Player pl) {

    }

    @Override
    public void initTalk() {
        this.textTalkBefore = new String[] {};
        this.textTalkMidle = new String[] { "Kame Kame Haaaaa!!", "Mi khá đấy nhưng so với ta chỉ là hạng tôm tép",
                "Tất cả nhào vô hết đi", "Cứ chưởng tiếp đi. haha", "Các ngươi yếu thế này sao hạ được ta đây. haha",
                "Khi công pháo!!", "Cho mi biết sự lợi hại của ta" };
        this.textTalkAfter = new String[] { "Các ngươi được lắm", "Hãy đợi đấy thời gian tới ta sẽ quay lại.." };
    }
}
