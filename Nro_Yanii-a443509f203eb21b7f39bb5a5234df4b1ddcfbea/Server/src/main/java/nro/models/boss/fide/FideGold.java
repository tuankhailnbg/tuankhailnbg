package nro.models.boss.fide;

import nro.consts.ConstItem;
import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.boss.BossManager;
import nro.models.item.ItemOption;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.server.Manager;
import nro.services.RewardService;
import nro.services.Service;
import nro.utils.Util;

/**
 * 
 */
public class FideGold extends Boss {

    public FideGold() {
        super(BossFactory.FIDEGOLD, BossData.FIDEGOLD);
    }

    @Override
    protected boolean useSpecialSkill() {
        return false;
    }

    @Override

    public void rewards(Player pl) {
        // Cải trang thỏ
        int[] tempIds1 = new int[] { 1043 };
        int[] tempIds2 = new int[] { 17 };

        int tempId = -1;
        if (Util.isTrue(1, 10)) {
            tempId = tempIds1[Util.nextInt(0, tempIds1.length - 1)];
        } else {
            tempId = tempIds2[Util.nextInt(0, tempIds2.length - 1)];
        }
        if (Manager.EVENT_SEVER == 4 && tempId == -1) {
            tempId = ConstItem.LIST_ITEM_NLSK_TET_2023[Util.nextInt(0, ConstItem.LIST_ITEM_NLSK_TET_2023.length - 1)];
        }
        if (tempId != -1) {
            ItemMap itemMap = new ItemMap(this.zone, tempId, 1,
                    pl.location.x, this.zone.map.yPhysicInTop(pl.location.x, pl.location.y - 24), pl.id);
            if (tempId >= 2027 && tempId <= 2038) {
                itemMap.options.add(new ItemOption(74, 0));
            } else if (tempId == 1043) {
                itemMap.options.add(new ItemOption(77, Util.nextInt(20, 40)));
                itemMap.options.add(new ItemOption(103, Util.nextInt(20, 40)));
                itemMap.options.add(new ItemOption(50, Util.nextInt(20, 40)));
                itemMap.options.add(new ItemOption(117, Util.nextInt(20, 30)));
                itemMap.options.add(new ItemOption(93, Util.nextInt(1, 30)));
            }
            RewardService.gI().initBaseOptionClothes(itemMap.itemTemplate.id, itemMap.itemTemplate.type,
                    itemMap.options);
            Service.getInstance().dropItemMap(this.zone, itemMap);
        }
    }

    @Override
    public void idle() {

    }

    @Override
    public void checkPlayerDie(Player pl) {

    }

    @Override
    public void initTalk() {
        this.textTalkMidle = new String[] { "Oải rồi hả?", "Ê cố lên nhóc",
                "Chán", "Ta có nhầm không nhỉ" };

    }

    @Override
    public void leaveMap() {
        BossFactory.createBoss(BossFactory.POCTHO).setJustRest();
        super.leaveMap();
        BossManager.gI().removeBoss(this);
    }

}
