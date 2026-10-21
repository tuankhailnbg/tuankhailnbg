/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nro.models.map;

import nro.models.player.Player;
import nro.services.ItemMapService;
import nro.utils.Util;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author 💖 Nro Yanii 💖
 */
public abstract class Satellite extends ItemMap {

    protected long ownerID;
    protected int clanID;
    protected long lastTimeEffect;
    protected long delayBuff;

    public Satellite(Zone zone, int itemID, int x, int y, Player player) {
        super(zone, itemID, 1, x, y, player.id);
        this.playerId = -2;
        this.range = 250;
        this.ownerID = player.id;
        this.clanID = player.clan != null ? player.clan.id : -1;
    }

    protected List<Player> getPlayersSameClan(int clanID) {
        List<Player> list = new ArrayList<>();
        List<Player> players = zone.getPlayers();
        synchronized (players) {
            for (Player pl : players) {
                if (pl.clan != null
                        && pl.clan.id == clanID) {
                    list.add(pl);
                }
            }
        }
        return list;
    }

    @Override
    public void update() {
        if (Util.canDoWithTime(createTime, 1800000)) {
            ItemMapService.gI().removeItemMapAndSendClient(this);
        } else {
            if (Util.canDoWithTime(lastTimeEffect, delayBuff)) {
                lastTimeEffect = System.currentTimeMillis();
                if (clanID != -1) {
                    List<Player> list = getPlayersSameClan(clanID);
                    synchronized (list) {
                        for (Player pl : list) {
                            buff(pl);
                        }
                    }
                } else {
                    Player owner = zone.getPlayerInMap((int) ownerID);
                    if (owner != null) {
                        buff(owner);
                    }
                }
            }
        }
    }

    public abstract void buff(Player player);
}
