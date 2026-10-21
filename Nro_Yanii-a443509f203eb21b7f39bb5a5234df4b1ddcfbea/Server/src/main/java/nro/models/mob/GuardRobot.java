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

/**
 *
 * @author 💖 Nro Yanii 💖
 */
public class GuardRobot extends BigBoss {

    public GuardRobot(Mob mob) {
        super(mob);
    }

    @Override
    public void attack(Player target) {

        byte action = (byte) Util.nextInt(3);
        if (action != 1 && target.location.y != 336) {
            action = 1;
        }
        if (action == 0 || action == 2) {
            location.x += (target.location.x - location.x) / 4;
        }
        if (action == 0) {
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
        }, 3000, "robot");
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

    public void hide() {
        try {
            move(-1000, -1000);
            Message ms = new Message(Cmd.BIG_BOSS_2);
            DataOutputStream ds = ms.writer();
            ds.writeByte(6);
            ds.flush();
            Service.getInstance().sendMessAllPlayerInMap(zone, ms);
            ms.cleanup();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
