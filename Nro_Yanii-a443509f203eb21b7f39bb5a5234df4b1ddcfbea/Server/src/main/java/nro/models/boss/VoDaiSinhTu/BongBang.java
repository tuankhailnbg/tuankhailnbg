package nro.models.boss.VoDaiSinhTu;

import nro.models.boss.dhvt.*;
import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.player.Player;

/**
 * @author outcast c-cute hột me 😳
 */
public class BongBang extends BossVoDaiSinhTu {

    public BongBang(Player player) {
        super(BossFactory.BONG_BANG, BossData.BONG_BANG);
        this.playerAtt = player;
    }
}