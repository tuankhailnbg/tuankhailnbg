package nro.models.boss.dhvt;

import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.player.Player;

/**
 * @author outcast c-cute hột me 😳
 */
public class ChanXu extends BossDHVT {

    public ChanXu(Player player) {
        super(BossFactory.CHAN_XU, BossData.CHAN_XU);
        this.playerAtt = player;
    }
}