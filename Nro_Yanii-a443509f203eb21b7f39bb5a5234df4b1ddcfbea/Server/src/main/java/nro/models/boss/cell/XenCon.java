package nro.models.boss.cell;

import nro.models.boss.*;
import nro.models.map.ItemMap;
import nro.models.map.Zone;
import nro.models.player.Player;
import nro.services.RewardService;
import nro.services.Service;
import nro.services.TaskService;
import nro.utils.Util;

/**
 *
 * VMN
 *
 */
public class XenCon extends FutureBoss {

    public XenCon() {
        super(BossFactory.XEN_CON, BossData.XEN_CON);
    }

    public XenCon(byte id, Zone zone) {
        super(id, BossData.XEN_CON);
        this.zone = zone;
    }

    @Override
    public void rewards(Player pl) {
        ItemMap itemMap = null;
        int x = this.location.x;
        int y = this.zone.map.yPhysicInTop(x, this.location.y - 24);
        if (Util.isTrue(1, 100)) {
            int[] set1 = {565, 560, 562, 564, 566, 561};
            itemMap = new ItemMap(this.zone, set1[Util.nextInt(0, set1.length - 1)], 1, x, y, pl.id);
            RewardService.gI().initBaseOptionClothes(itemMap.itemTemplate.id, itemMap.itemTemplate.type,
                    itemMap.options);
        } else if (Util.isTrue(1, 100)) {
            int[] set2 = {555, 556, 563, 557, 558, 559, 567};
            itemMap = new ItemMap(this.zone, set2[Util.nextInt(0, set2.length - 1)], 1, x, y, pl.id);
            RewardService.gI().initBaseOptionClothes(itemMap.itemTemplate.id, itemMap.itemTemplate.type,
                    itemMap.options);
        } else if (Util.isTrue(1, 35)) {
            itemMap = new ItemMap(this.zone, 15, 1, x, y, pl.id);
        } else if (Util.isTrue(1, 35)) {
            itemMap = new ItemMap(this.zone, 16, 1, x, y, pl.id);
        }
        if (itemMap != null) {
            Service.getInstance().dropItemMap(zone, itemMap);
        }

        TaskService.gI().checkDoneTaskKillBoss(pl, this);

        generalRewards(pl);
    }

    @Override
    public void idle() {
    }

    public void leaveMap() {
        this.setJustRestToFuture();
        super.leaveMap();
        BossManager.gI().removeBoss(this);
    }

  

    @Override
    public void checkPlayerDie(Player pl) {
    }

    @Override
    public void initTalk() {
        this.textTalkBefore = new String[]{};
        this.textTalkMidle = new String[]{};
        this.textTalkAfter = new String[]{};
    }
}
