/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nro.models.mob;

import nro.consts.Cmd;
import nro.models.player.Player;
import nro.server.io.Message;
import nro.services.MobService;
import nro.services.Service;
import nro.utils.Util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author 💖 Nro Yanii 💖
 */
public class Octopus extends BigBoss {

    public Octopus(Mob mob) {
        super(mob);
    }

    @Override
    public Player getPlayerCanAttack() {
        int distance = 500;
        Player plAttack = null;
        try {
            List<Player> players = this.zone.getNotBosses();
            for (Player pl : players) {

                if (!pl.isDie() && !pl.isBoss && !pl.effectSkin.isVoHinh && !pl.isMiniPet) {
                    int x = pl.location.x;
                    int y = pl.location.y;
                    if (x >= 442 && x <= 960 && y >= 400) {
                        int dis = Util.getDistance(pl, this);
                        if (dis <= distance) {
                            plAttack = pl;
                            distance = dis;
                        }
                    }
                }
            }
        } catch (Exception e) {

        }
        return plAttack;
    }

    @Override
    public void move(int x, int y) {
        super.move(x, y);
        if (x > 0 && y > 0) {
            moveX((short) x);
        }
    }

    public void hide() {
        try {
            move(-1000, -1000);
            Message ms = new Message(Cmd.BIG_BOSS_2);
            DataOutputStream ds = ms.writer();
            ds.writeByte(7);
            ds.flush();
            Service.getInstance().sendMessAllPlayerInMap(zone, ms);
            ms.cleanup();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void send(Player cAttack, long damage, byte type) {
        try {
            Message ms = new Message(Cmd.BIG_BOSS_2);
            DataOutputStream ds = ms.writer();
            ds.writeByte(type);
            ds.writeByte(1);
            ds.writeInt((int) cAttack.id);
            ds.writeLong(damage);
            ds.flush();
            Service.getInstance().sendMessAllPlayerInMap(zone, ms);
            ms.cleanup();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public void moveX(short x) {
        try {
            Message ms = new Message(Cmd.BIG_BOSS_2);
            DataOutputStream ds = ms.writer();
            ds.writeByte(5);
            ds.writeShort(x);
            ds.flush();
            Service.getInstance().sendMessAllPlayerInMap(zone, ms);
            ms.cleanup();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void attack(Player target) {
        byte action = (byte) Util.nextInt(3, 5);
        if ((action == 3 || action == 5) && target.location.y != 576) {
            action = 4;
        }
        if (action == 5) {
            move(target.location.x, 576);
            return;
        }
        if (action == 3) {
            location.x += (target.location.x - location.x) / 4;
            location.y += (target.location.y - location.y) / 4;
        }
        long damage = MobService.gI().mobAttackPlayer(this, target);
        send(target, damage, action);
    }

    @Override
    public void setDie() {
        super.setDie();
        Util.setTimeout(() -> {
            hide();
        }, 3000, "octopus");
    }

}
