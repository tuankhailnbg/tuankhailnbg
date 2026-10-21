package nro.models.boss.dhvt;

import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.player.Player;

/**
 * @author outcast c-cute hột me 😳
 */
public class SoiHecQuyn extends BossDHVT {
    public SoiHecQuyn(Player player) {
        super(BossFactory.SOI_HEC_QUYN, BossData.SOI_HEC_QUYN);
        this.playerAtt = player;
    }
}
