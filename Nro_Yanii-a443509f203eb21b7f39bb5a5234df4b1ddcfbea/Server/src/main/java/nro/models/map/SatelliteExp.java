/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nro.models.map;

import nro.models.player.NPoint;
import nro.models.player.Player;
import nro.utils.Util;

/**
 *
 * @author 💖 Nro Yanii 💖
 */
public class SatelliteExp extends Satellite {

    public SatelliteExp(Zone zone, int itemID, int x, int y, Player player) {
        super(zone, itemID, x, y, player);
        this.delayBuff = 1000;
    }

    @Override
    public void buff(Player pl) {
        int r = Util.getDistance(pl.location.x, pl.location.y, x, y);
        NPoint n = pl.nPoint;
        if (n != null) {
            if (r <= range) {
                n.buffExpSatellite = true;
            } else {
                n.buffExpSatellite = false;
            }
        }
    }

}
