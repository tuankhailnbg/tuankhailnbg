package nro.manager;

import java.util.ArrayList;
import java.util.List;
import nro.consts.ConstItem;
import nro.consts.ConstPlayer;
import nro.models.map.Map;
import nro.models.map.NamekBall;
import nro.models.map.Zone;
import nro.models.map.war.NamekBallWar;
import nro.models.player.Player;
import nro.services.ItemMapService;
import nro.services.MapService;
import nro.services.PlayerService;
import nro.services.Service;
import nro.utils.Util;

/**
 * @author outcast c-cute hột me 😳
 */
public class NamekBallManager extends AbsManager<NamekBall> {

    private static final NamekBallManager INSTANCE = new NamekBallManager();

    public static NamekBallManager gI() {
        return INSTANCE;
    }

    public void initBall() {
        int id = -9999;
        List<Map> usedMaps = new ArrayList<>(); // Danh sách các Map đã sử dụng
        for (int i = 0; i < 7; i++) {
            Map m;
            Zone z;

            // Lặp cho đến khi có một Map chưa được sử dụng
            do {
                m = MapService.gI().getMapById(Util.nextInt(7, 13));
            } while (usedMaps.contains(m));
            
            usedMaps.add(m);
            
            z = m.zones.get(Util.nextInt(0, m.zones.size() - 1));

            int y = m.yPhysicInTop(m.mapWidth / 2, m.mapHeight / 2);
            NamekBall ball = new NamekBall(z, ConstItem.NGOC_RONG_NAMEK_1_SAO + i, 1, m.mapWidth / 2, y, -1);
            ball.itemMapId = id++;
            ball.setIndex(i);
            add(ball);
        }
    }

    public void initFossil() {
        Player[] holders = NamekBallWar.gI().getHolders();
        for (Player p : holders) {
            if (p != null) {
                p.isHoldNamecBall = false;
                Service.getInstance().sendFlagBag(p);
                PlayerService.gI().changeAndSendTypePK(p, ConstPlayer.NON_PK);
            }
        }
        for (NamekBall ball : getList()) {
            ItemMapService.gI().removeItemMap(ball);
            ItemMapService.gI().sendItemMapDisappear(ball);
        }
        for (int i = 0; i < 7; i++) {
            Map m = MapService.gI().getMapById(Util.nextInt(7, 13));
            Zone z = m.zones.get(Util.nextInt(0, m.zones.size()));
            int y = m.yPhysicInTop(m.mapWidth / 2, m.mapHeight / 2);
            NamekBall ball = new NamekBall(z, ConstItem.HOA_THACH_NGOC_RONG, 1, m.mapWidth / 2, y, -1);
            ball.setStone(true);
            add(ball);
            Service.getInstance().dropItemMap(z, ball);
        }
        getList().clear();
    }

    @Override
    public NamekBall findByID(int id) {
        return list.get(getIndex(id));
    }

    public NamekBall findByIndex(int index) {
        return list.get(index);
    }

    public int getIndex(int id) {
        return id - 353;
    }
}
