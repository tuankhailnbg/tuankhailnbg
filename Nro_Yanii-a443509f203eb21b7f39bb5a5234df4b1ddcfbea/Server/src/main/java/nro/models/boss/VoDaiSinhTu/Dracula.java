package nro.models.boss.VoDaiSinhTu;

import nro.models.boss.dhvt.*;
import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.player.Player;

/**
 * @author outcast c-cute hột me 😳
 */
public class Dracula extends BossVoDaiSinhTu {

    public Dracula(Player player) {
        super(BossFactory.DRACULA, BossData.DRACULA);
        this.playerAtt = player;
    }
}