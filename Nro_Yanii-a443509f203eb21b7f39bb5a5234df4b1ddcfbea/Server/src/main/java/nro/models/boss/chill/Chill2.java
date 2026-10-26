package nro.models.boss.chill;

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
public class Chill2 extends FutureBoss {

    public Chill2() {
        super(BossFactory.CHILL2, BossData.CHILL2);
    }

    @Override
    public void rewards(Player pl) {
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
        BossManager.gI().getBossById(BossFactory.CHILL).setJustRest();
        super.leaveMap();
        BossManager.gI().removeBoss(this);
    }
}
