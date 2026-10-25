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
public class XenCon4 extends FutureBoss {

    public XenCon4() {
        super(BossFactory.XEN_CON_4, BossData.XEN_CON_4);
    }

  
    @Override
    public void rewards(Player pl) {
        TaskService.gI().checkDoneTaskKillBoss(pl, this);
        generalRewards(pl);
    }

    @Override
    public void idle() {
    }

 public void leaveMap() {
        super.leaveMap();
        this.changeToIdle();
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
