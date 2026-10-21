package nro.models.boss.mapoffline;

import nro.models.player.*;
import nro.models.map.Map;
import nro.models.map.Zone;
import nro.server.Manager;
import nro.services.MapService;

/**
 * @author outcast c-cute hột me 😳
 */
public class NPC_ToSuKaio extends Player {

    private long lastTimeChat;

    public void NPC_ToSuKaio(Player pl) {
        init(pl);
    }
    private static NPC_ToSuKaio instance;

    public static NPC_ToSuKaio getInstance() {
        if (instance == null) {
            instance = new NPC_ToSuKaio();
        }
        return instance;
    }

    private Zone z;

    @Override
    public short getHead() {
        return 448;
    }

    @Override
    public short getBody() {
        return 449;
    }

    @Override
    public short getLeg() {
        return 450;
    }

    public void joinMap(Zone z, Player player) {
        MapService.gI().goToMap(player, z);
        z.load_Me_To_Another(player);
    }

    @Override
    public int version() {
        return 214;
    }

    @Override
    public void update() {
        Player pl = super.zone.findPlayerByID(-super.id);
        if (pl == null) {
            MapService.gI().exitMap(this);
        }
    }

    private void init(Player player) {
        long id = -player.id;
        for (Map m : Manager.MAPS) {
            if (m.mapId == 50) {
                for (Zone z : m.zones) {
                    NPC_ToSuKaio pl = new NPC_ToSuKaio();
                    pl.name = "Tổ sư kaio";
                    pl.gender = 0;
                    pl.id = id;
                    pl.nPoint.hpMax = 5100;
                    pl.nPoint.hpg = 5100;
                    pl.nPoint.hp = 5100;
                    pl.nPoint.setFullHpMp();
                    pl.location.x = 400;
                    pl.location.y = 336;
                    joinMap(z, pl);
                    z.setReferee(pl);
                }
            }
        }
    }
}
