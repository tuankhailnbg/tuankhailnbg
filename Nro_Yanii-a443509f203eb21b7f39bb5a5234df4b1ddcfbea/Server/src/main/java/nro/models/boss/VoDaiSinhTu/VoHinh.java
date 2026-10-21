package nro.models.boss.VoDaiSinhTu;

import nro.models.boss.dhvt.*;
import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.player.Player;

/**
 * @author outcast c-cute hột me 😳
 */
public class VoHinh extends BossVoDaiSinhTu {

    public VoHinh(Player player) {
        super(BossFactory.VO_HINH, BossData.VO_HINH);
        this.playerAtt = player;
    }
}