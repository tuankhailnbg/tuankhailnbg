package nro.models.boss.NgucTu;

import nro.consts.ConstItem;
import nro.models.boss.*;
import nro.models.item.ItemOption;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.server.Manager;
import nro.services.RewardService;
import nro.services.Service;
import nro.utils.Util;

/**
 * @author 💖 Nothing 💖
 */
public class SuperCumber extends FutureBoss {

    public SuperCumber() {
        super(BossFactory.CUMBER2, BossData.CUMBER2);
    }

    @Override
    public void rewards(Player pl) {
        // do than 1/100
        int[] tempIds1 = new int[]{565, 560, 562, 564, 566, 561};
        // Nhan, gang than 1/100
        int[] tempIds2 = new int[]{565, 560, 562, 564, 566, 561};

        int[] tempIds3 = new int[]{15};

        int tempId = -1;
        if (Util.isTrue(1, 100)) {
            tempId = tempIds3[Util.nextInt(0, tempIds3.length - 1)];
        } else if (Util.isTrue(1, 100)) {
            tempId = tempIds1[Util.nextInt(0, tempIds1.length - 1)];
        } else if (Util.isTrue(1, 100)) {
            tempId = tempIds2[Util.nextInt(0, tempIds2.length - 1)];
        }
        if (Manager.EVENT_SEVER == 4 && tempId == -1) {
            tempId = ConstItem.LIST_ITEM_NLSK_TET_2023[Util.nextInt(0, ConstItem.LIST_ITEM_NLSK_TET_2023.length - 1)];
        }
        if (tempId != -1) {
            ItemMap itemMap = new ItemMap(this.zone, tempId, 1,
                    pl.location.x, this.zone.map.yPhysicInTop(pl.location.x, pl.location.y - 24), pl.id);
            if (tempId == 2046) {
                itemMap.options.add(new ItemOption(77, 40));
                itemMap.options.add(new ItemOption(103, 40));
                itemMap.options.add(new ItemOption(50, 40));
                itemMap.options.add(new ItemOption(93, Util.nextInt(1, 15)));
                itemMap.options.add(new ItemOption(199, 0));
            } else {
                RewardService.gI().initBaseOptionClothes(itemMap.itemTemplate.id, itemMap.itemTemplate.type, itemMap.options);
                RewardService.gI().initStarOption(itemMap, new RewardService.RatioStar[]{
                    new RewardService.RatioStar((byte) 1, 1, 2),
                    new RewardService.RatioStar((byte) 2, 1, 3),
                    new RewardService.RatioStar((byte) 3, 1, 4),
                    new RewardService.RatioStar((byte) 4, 1, 5),
                    new RewardService.RatioStar((byte) 5, 1, 6),});
            }
            Service.getInstance().dropItemMap(this.zone, itemMap);
        }
        generalRewards(pl);
    }

    @Override
    public void checkPlayerDie(Player pl) {

    }

    @Override
    public void initTalk() {
        textTalkMidle = new String[]{"Ta chính là đệ nhất vũ trụ cao thủ"};
        textTalkAfter = new String[]{"Ác quỷ biến hình aaa..."};
    }

    @Override
    public void leaveMap() {

        BossManager.gI().getBossById(BossFactory.CUMBER).setJustRest();
        super.leaveMap();
        BossManager.gI().removeBoss(this);
    }
}
