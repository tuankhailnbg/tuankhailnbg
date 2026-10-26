package nro.models.boss.cold;

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
public class Cooler extends FutureBoss {

    public Cooler() {
        super(BossFactory.COOLER, BossData.COOLER);
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
        textTalkMidle = new String[] { "Ta chính là đệ nhất vũ trụ cao thủ" };
        textTalkAfter = new String[] { "Ác quỷ biến hình aaa..." };
    }

    @Override
    public void leaveMap() {
        Boss cooler2 = BossFactory.createBoss(BossFactory.COOLER2);
        cooler2.zone = this.zone;
        cooler2.secondTimeRestToNextTimeAppear = 0;
        this.setJustRestToFuture();
        super.leaveMap();
    }

}
