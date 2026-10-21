/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nro.models.map;

import nro.models.player.Player;
import nro.services.PlayerService;
import nro.utils.Util;

/**
 *
 * @author 💖 Nro Yanii 💖
 */
public class SatelliteHP extends Satellite {

    public SatelliteHP(Zone zone, int itemID, int x, int y, Player player) {
        super(zone, itemID, x, y, player);
        this.delayBuff = 30000;
    }

    @Override
    public void buff(Player pl) {
        int r = Util.getDistance(pl.location.x, pl.location.y, x, y);
        if (r <= range) {
            PlayerService.gI().hoiPhuc(pl, pl.nPoint.hpMax * 5 / 100, 0);
        }
    }
}
