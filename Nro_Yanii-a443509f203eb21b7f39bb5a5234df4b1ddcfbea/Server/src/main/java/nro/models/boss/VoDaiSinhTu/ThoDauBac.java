package nro.models.boss.VoDaiSinhTu;

import nro.models.boss.dhvt.*;
import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.player.Player;

/**
 * @author outcast c-cute hột me 😳
 */
public class ThoDauBac extends BossVoDaiSinhTu {

    public ThoDauBac(Player player) {
        super(BossFactory.THO_DAU_BAC, BossData.THO_DAU_BAC);
        this.playerAtt = player;
    }
}