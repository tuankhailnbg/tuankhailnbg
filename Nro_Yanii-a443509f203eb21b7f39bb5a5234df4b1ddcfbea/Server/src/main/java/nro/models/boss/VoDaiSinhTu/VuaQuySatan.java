package nro.models.boss.VoDaiSinhTu;

import nro.models.boss.dhvt.*;
import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.player.Player;

/**
 * @author outcast c-cute hột me 😳
 */
public class VuaQuySatan extends BossVoDaiSinhTu {

    public VuaQuySatan(Player player) {
        super(BossFactory.VUA_QUY_SATAN, BossData.VUA_QUY_SATAN);
        this.playerAtt = player;
    }
}