package nro.models.boss.dhvt;

import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.player.Player;

/**
 * @author outcast c-cute hột me 😳
 */
public class PonPut extends BossDHVT {

    public PonPut(Player player) {
        super(BossFactory.PON_PUT, BossData.PON_PUT);
        this.playerAtt = player;
    }
}