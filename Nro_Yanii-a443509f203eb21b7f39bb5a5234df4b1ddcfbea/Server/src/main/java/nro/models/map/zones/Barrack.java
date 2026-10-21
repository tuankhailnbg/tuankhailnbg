/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nro.models.map.zones;

import nro.models.map.Map;
import nro.models.map.Zone;
import nro.models.mob.Mob;
import nro.models.player.MiniPet;
import nro.models.player.Player;
import nro.services.MobService;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author 💖 Nro Yanii 💖
 */
public class Barrack extends Zone {

    public Barrack(Map map, int zoneId, int maxPlayer) {
        super(map, zoneId, maxPlayer);
    }

    @Override
    public void addPlayer(Player player) {
        super.addPlayer(player);
        if (!player.isBoss && !player.isMiniPet) {
            adjustMonsterAttributes();
        }
    }

    private void adjustMonsterAttributes() {
        long maxPoint = getMaxPoint();
        if (maxPoint < 300000) {
            maxPoint = 300000;
        }
        synchronized (mobs) {
            for (Mob m : mobs) {
                if (!m.isDie()) {
                    MobService.gI().initMobDoanhTrai(m, maxPoint);
                }
            }
        }
        List<Player> bosses = getBosses();
        synchronized (bosses) {
            for (Player pl : bosses) {
                if (!pl.isDie()) {
                    if (pl.nPoint != null) {
                        pl.nPoint.hp = pl.nPoint.hpMax = (int) maxPoint;
                        pl.nPoint.dame = (int) (maxPoint / 40);
                        pl.nPoint.setAttributeOverLimit();
                    }
                }
            }
        }

    }

    private long getMaxPoint() {
        long max = 0;
        List<Player> list = getNotBosses();
        synchronized (list) {
            List<Player> newlist = list.stream().filter(p -> !(p instanceof MiniPet)).collect(Collectors.toList());
            for (Player p : newlist) {
                long point = 0;
                if (p.nPoint != null) {
                    point += p.nPoint.hpMax;
                    point += p.nPoint.dame * 500;
                }
                if (point > max) {
                    max = point;
                }
            }

        }
        return max;
    }

}
