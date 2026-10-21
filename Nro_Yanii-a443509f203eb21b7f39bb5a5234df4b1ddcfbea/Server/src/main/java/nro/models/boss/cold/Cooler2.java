package nro.models.boss.cold;

import nro.models.boss.*;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.services.RewardService;
import nro.services.Service;
import nro.services.func.ChangeMapService;
import nro.utils.Util;

/**
 *
 * @author 💖 Nro Yanii 💖
 *
 *
 */
public class Cooler2 extends FutureBoss {

    public Cooler2() {
        super(BossFactory.COOLER2, BossData.COOLER2);
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
            } else if (rand <= 20) {
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
        textTalkMidle = new String[] { "Ta chính là đệ nhất vũ trụ cao thủ" };
        textTalkAfter = new String[] { "Ác quỷ biến hình aaa..." };
    }

    @Override
    public void leaveMap() {
        BossManager.gI().getBossById(BossFactory.COOLER).setJustRest();
        super.leaveMap();
        BossManager.gI().removeBoss(this);
    }
}
