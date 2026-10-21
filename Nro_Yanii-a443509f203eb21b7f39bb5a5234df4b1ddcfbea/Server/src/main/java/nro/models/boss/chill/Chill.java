package nro.models.boss.chill;

import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.boss.FutureBoss;
import nro.models.item.ItemOption;
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
public class Chill extends FutureBoss {

    public Chill() {
        super(BossFactory.CHILL, BossData.CHILL);
    }

    @Override
    public void rewards(Player pl) {
        // do than 1/35
        int[] tempIds1 = new int[]{555, 556, 563, 557, 558, 559, 567};
        // Nhan, gang than 1/100
        int[] tempIds2 = new int[]{565, 560, 562, 564, 566, 561};

        int[] tempIds3 = new int[]{15};

        int tempId = -1;

        if (Util.isTrue(1, 100)) {
            tempId = tempIds3[Util.nextInt(0, tempIds3.length - 1)];
        } else if (Util.isTrue(1, 35)) {
            tempId = tempIds1[Util.nextInt(0, tempIds1.length - 1)];
        } else if (Util.isTrue(1, 100)) {
            tempId = tempIds2[Util.nextInt(0, tempIds2.length - 1)];
        }

        if (tempId != -1) {
            ItemMap itemMap = new ItemMap(this.zone, tempId, 1,
                    pl.location.x, this.zone.map.yPhysicInTop(pl.location.x, pl.location.y - 24), pl.id);
            if (tempId == 985) {
                itemMap.options.add(new ItemOption(77, 40));
                itemMap.options.add(new ItemOption(103, 40));
                itemMap.options.add(new ItemOption(50, 35));
                itemMap.options.add(new ItemOption(93, Util.nextInt(1, 30)));
            }
            RewardService.gI().initBaseOptionClothes(itemMap.itemTemplate.id, itemMap.itemTemplate.type,
                    itemMap.options);
            Service.getInstance().dropItemMap(this.zone, itemMap);

        }
        generalRewards(pl);
    }

    @Override
    public void checkPlayerDie(Player pl) {

    }

    @Override
    public void initTalk() {
        this.textTalkAfter = new String[]{"Các ngươi chờ đấy, ta sẽ quay lại sau"};
    }

    @Override
    public void leaveMap() {
        Boss chill2 = BossFactory.createBoss(BossFactory.CHILL2);
        chill2.zone = this.zone;
        this.setJustRestToFuture();
        super.leaveMap();
    }

}
