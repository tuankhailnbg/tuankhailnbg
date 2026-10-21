package nro.models.map;

import nro.models.player.Player;
import nro.services.PlayerService;
import nro.services.func.EffectMapService;
import nro.utils.Util;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class TrapMap {

    public int x;
    public int y;
    public int w;
    public int h;
    public int effectId;
    public int dame;

    public void doPlayer(Player player) {
        switch (this.effectId) {
            case 49:
                if (!player.isDie() && !player.isMiniPet && Util.canDoWithTime(player.lastTimeAnXienTrapBDKB, 1000)) {
                    player.injured(null, dame + (Util.nextInt(-10, 10) * dame / 100), false, false);
                    PlayerService.gI().sendInfoHp(player);
                    EffectMapService.gI().sendEffectMapToAllInMap(player.zone,
                            effectId, 2, 1, player.location.x - 32, 1040, 1);
                    player.lastTimeAnXienTrapBDKB = System.currentTimeMillis();
                }
                break;
        }
    }

}
