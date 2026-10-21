package nro.services.func;

import nro.consts.Cmd;
import nro.models.map.EffectEventTemplate;
import nro.models.map.Zone;
import nro.models.map.EffectEventManager;
import nro.models.player.Player;
import nro.server.Manager;
import nro.server.io.Message;
import nro.services.Service;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class EffectMapService {

    private static EffectMapService i;

    private EffectMapService() {

    }

    public static EffectMapService gI() {
        if (i == null) {
            i = new EffectMapService();
        }
        return i;
    }

    public void sendEffEvent(Player pl) {
        int plmapid = pl.zone.map.mapId;
        for (EffectEventTemplate i : EffectEventManager.gI().getList()) {
            if (Manager.EVENT_SEVER == i.getEventId()) {
                if (plmapid == i.getMapId()) {
                    EffectMapService.gI().sendEffectMapToPlayer(pl, i.getEffId(),
                            i.getLayer(), i.getLoop(), i.getX(), i.getY(), i.getDelay());
                }
            }
        }
    }

    public void sendEffectMapToPlayer(Player player, int id, int layer, int loop, int x, int y, int delay) {
        Message msg;
        try {
            msg = new Message(113);
            msg.writer().writeByte(loop);
            msg.writer().writeByte(layer);
            msg.writer().writeByte(id);
            msg.writer().writeShort(x);
            msg.writer().writeShort(y);
            msg.writer().writeShort(delay);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void sendCharEffect(Player player, byte type, short id, byte layer, byte loop, short delay,
            boolean isStand) {
        Message msg;
        try {
            msg = new Message(Cmd.CHAR_EFFECT);
            msg.writer().writeByte(type);
            msg.writer().writeInt((int) player.id);
            if (type == 0) {
                msg.writer().writeShort(id);
                msg.writer().writeByte(layer);
                msg.writer().writeByte(loop);
                msg.writer().writeShort(delay);
                msg.writer().writeBoolean(isStand);
            } else if (type == 1) {
                msg.writer().writeShort(id);
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void sendCharEffect(Player player, byte type, short id, byte layer, byte loop, short delay, byte isStand) {
        Message msg;
        try {
            msg = new Message(Cmd.CHAR_EFFECT);
            msg.writer().writeByte(type);// 0 : adđ
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(id);
            msg.writer().writeByte(layer);
            msg.writer().writeByte(loop);
            msg.writer().writeShort(delay);
            msg.writer().writeByte(isStand);
            Service.getInstance().sendMessAllPlayer(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void sendCharEffect_HoaBang(Player player) {
        Message msg;
        try {
            msg = new Message(Cmd.CHAR_EFFECT);
            msg.writer().writeByte(0);// 0 : adđ
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(202);
            msg.writer().writeByte(1);
            msg.writer().writeByte(-1);
            msg.writer().writeShort(10);
            msg.writer().writeByte(0);
            Service.getInstance().sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void Remove_sendCharEffect_HoaBang(Player player) {
        Message msg;
        try {
            msg = new Message(Cmd.CHAR_EFFECT);
            msg.writer().writeByte(1);// 0 : adđ
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(202);
            msg.writer().writeByte(1);
            msg.writer().writeByte(-1);
            msg.writer().writeShort(10);
            msg.writer().writeByte(1);
            player.sendMessage(msg);
            Service.getInstance().sendMessAllPlayer(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void sendEffectMapToAllInMap(Zone zone, int id, int layer, int loop, int x, int y, int delay) {
        Message msg;
        try {
            msg = new Message(113);
            msg.writer().writeByte(loop);
            msg.writer().writeByte(layer);
            msg.writer().writeByte(id);
            msg.writer().writeShort(x);
            msg.writer().writeShort(y);
            msg.writer().writeShort(delay);
            Service.getInstance().sendMessAllPlayerInMap(zone, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

}
