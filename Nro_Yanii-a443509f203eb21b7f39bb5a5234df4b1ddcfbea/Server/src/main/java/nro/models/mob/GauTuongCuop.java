package nro.models.mob;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import nro.consts.Cmd;
import nro.models.mob.Mob;
import nro.models.player.Player;
import nro.server.io.Message;
import nro.services.Service;
import nro.utils.Util;

public class GauTuongCuop extends BigBoss {

    public GauTuongCuop(Mob mob) {
        super(mob);
    }

    @Override
    public void attack(Player plll) {
        if (!isDie() && !effectSkill.isHaveEffectSkill() && Util.canDoWithTime(lastBigBossAttackTime, 3000)) {
            // 10 : di chuyển, 11 - 20 : tấn công, 21 : bay, 22 : ..., 23 : die

            if (this.zone.getNotBosses().isEmpty()) {
                return;
            }
            List<Player> players = new ArrayList<>();

            action = Util.nextInt(11, 15);

            switch (action) {
                case 11:
                    for (Player pl : this.zone.getNotBosses()) {
                        if (Util.getDistance(pl, this) < 50) {
                            players.add(pl);
                            break;
                        }
                    }
                    break;
                case 12:
                    for (Player pl : this.zone.getNotBosses()) {
                        if (Util.getDistance(pl, this) < 100) {
                            players.add(pl);
                            break;
                        }
                    }
                    break;
                case 13:
                case 14:
                    for (Player pl : this.zone.getNotBosses()) {
                        if (Util.getDistance(pl, this) < 150) {
                            players.add(pl);
                        }
                    }
                    break;
                case 15:
                    for (Player pl : this.zone.getNotBosses()) {
                        if (Util.getDistance(pl, this) < 200) {
                            players.add(pl);
                        }
                    }
                    break;
            }

            if (players.isEmpty()) {
                int index = Util.nextInt(0, this.zone.getNotBosses().size() - 1);
                players.add(this.zone.getNotBosses().get(index));
                action = 10;
//                return;
            }

            Message msg = null;
            try {
                msg = new Message(102);
                msg.writer().writeByte(action);
                msg.writer().writeByte(this.id);
                switch (action) {
                    case 10:
                    case 21:
                        for (Player player : players) {
                            this.location.x = player.location.x + Util.nextInt(-10, 10);
                            this.location.y = player.location.y;
                        }
                        msg.writer().writeShort(this.location.x);
                        msg.writer().writeShort(this.location.y);
                        break;
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                    case 15:
                    case 16:
                    case 17:
                    case 18:
                    case 19:
                    case 20:
                        msg.writer().writeByte(players.size()); // sl player;
                        int dir = 0;
                        for (Player pl : players) {
                            int dame = pl.injured(null, this.point.getDameAttack(), false, true);
                            msg.writer().writeInt((int) pl.id); // id player
                            msg.writer().writeInt(dame); // dame
                            dir = pl.location.x < this.location.x ? -1 : 1;
                        }
                        msg.writer().writeByte(dir); // dir
                        break;
                    case 22:
                        break;
                    case 23:
                        break;
                    default:
                        break;
                }
                Service.gI().sendMessAllPlayerInMap(this.zone, msg);
                lastBigBossAttackTime = System.currentTimeMillis();
            } catch (Exception e) {
            } finally {
                if (msg != null) {
                    msg.cleanup();
                    msg = null;
                }
            }
        }
    }
}
