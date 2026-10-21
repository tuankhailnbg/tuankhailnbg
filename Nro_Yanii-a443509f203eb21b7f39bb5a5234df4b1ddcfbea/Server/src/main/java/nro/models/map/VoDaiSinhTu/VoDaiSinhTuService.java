package nro.models.map.VoDaiSinhTu;

import nro.consts.ConstMap;
import nro.models.map.Zone;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.server.io.Message;
import nro.services.MapService;
import nro.services.Service;
import nro.services.func.ChangeMapService;
import nro.utils.Log;
import nro.utils.Util;

/**
 * @author outcast c-cute hột me 😳
 */
public class VoDaiSinhTuService {

    private static VoDaiSinhTuService i;

    public static VoDaiSinhTuService gI() {
        if (i == null) {
            i = new VoDaiSinhTuService();
        }
        return i;
    }

    public void startChallenge(Player player) {
        Zone zone = getMapChalllenge(112);
        if (zone != null) {
            Util.setTimeout(() -> {
                VoDaiSinhTu mc = new VoDaiSinhTu();
                mc.setPlayer(player);
                mc.setNpc(zone.getReferee());
                mc.toTheNextRound();
                VoDaiSinhTuManager.gI().add(mc);
                this.npcChat(player, "Số thứ tự của ngươi là 1\n chuẩn bị thi đấu nhé", player.zone.map.npcs.get(0));
            }, 500,"start vo dai st");
        } else {

        }
    }

    public void npcChat(Player player, String text, Npc npc) {
        Message msg;
        try {
            msg = new Message(124);
            msg.writer().writeShort(npc.tempId);
            msg.writer().writeUTF(text);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Log.error(Service.class, e);
        }
    }

    public void moveFast(Player pl, int x, int y) {
        Message msg;
        try {
            msg = new Message(58);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeShort(x);
            msg.writer().writeShort(y);
            msg.writer().writeInt((int) pl.id);
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void sendTypePK(Player player, Player boss) {
        Message msg;
        try {
            msg = Service.getInstance().messageSubCommand((byte) 35);
            msg.writer().writeInt((int) boss.id);
            msg.writer().writeByte(3);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public Zone getMapChalllenge(int mapId) {
        Zone map = MapService.gI().getMapWithRandZone(mapId);
        if (map.getNumOfBosses() < 1) {
            return map;
        }
        return null;
    }
}
