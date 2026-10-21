package nro.models.map.mabu;

import nro.consts.ConstNpc;
import nro.models.boss.Boss;
import nro.models.boss.BossFactory;
import nro.models.map.Map;
import nro.models.map.Zone;
import nro.models.npc.Npc;
import nro.models.npc.NpcManager;
import nro.models.player.Player;
import nro.services.EffSkinService;
import nro.services.MapService;
import nro.services.NpcService;
import nro.services.Service;
import nro.services.func.ChangeMapService;
import nro.utils.TimeUtil;
import nro.utils.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author outcast c-cute hột me 😳
 */
public class MabuWar {

    private static MabuWar i;
    public final List<Boss> bosses = new ArrayList<>();
    public static long TIME_OPEN;

    public static long TIME_CLOSE;
    public static final byte HOUR_OPEN = 12;
    public static final byte MIN_OPEN = 0;
    public static final byte SECOND_OPEN = 0;
    public static final byte HOUR_CLOSE = 13;
    public static final byte MIN_CLOSE = 0;
    public static final byte SECOND_CLOSE = 0;
    private int day = -1;
    public boolean initBoss;
    public boolean clearBoss;

    public static MabuWar gI() {
        if (i == null) {
            i = new MabuWar();
        }
        i.setTime();
        return i;
    }

    public void setTime() {
        if (i.day == -1 || i.day != TimeUtil.getCurrDay()) {
            i.day = TimeUtil.getCurrDay();
            try {
                this.TIME_OPEN = TimeUtil.getTime(TimeUtil.getTimeNow("dd/MM/yyyy") + " " + HOUR_OPEN + ":" + MIN_OPEN + ":" + SECOND_OPEN, "dd/MM/yyyy HH:mm:ss");
                this.TIME_CLOSE = TimeUtil.getTime(TimeUtil.getTimeNow("dd/MM/yyyy") + " " + HOUR_CLOSE + ":" + MIN_CLOSE + ":" + SECOND_CLOSE, "dd/MM/yyyy HH:mm:ss");
            } catch (Exception e) {
            }
        }
    }

    public boolean isTimeMabuWar() {
        long now = System.currentTimeMillis();
        if (now > TIME_OPEN && now < TIME_CLOSE) {
            return true;
        }
        return false;
    }

    public void update(Player player) {
        if (MapService.gI().isMapMabuWar(player.zone.map.mapId)) {
            if (isTimeMabuWar()) {
                if (!initBoss) {
                    BossFactory.initBossMabuWar();
                    initBoss = true;
                }
                if (Util.canDoWithTime(player.lastTimeBabiday, 30000)) {
                    if (player.cFlag == 9) {
                        if (Util.isTrue(50, 100)) {
                            Service.getInstance().changeFlag(player, 10);
                            Service.getInstance().sendThongBao(player, "Bạn bị Babiđây thôi miên");
                        }
                    } else if (Util.isTrue(50, 100)) {
                        Service.getInstance().changeFlag(player, 9);
                        Service.getInstance().sendThongBao(player, "Bạn được Ôsin giải trừ phép thuật");
                    }
                    player.lastTimeBabiday = System.currentTimeMillis();
                }
                sendMenuGotoNextFloorMabuWar(player);
                Zone zone = player.zone;
                if (zone.map.mapId == 117) {
                    EffSkinService.gI().setSlow(player, System.currentTimeMillis(), 1000);
                }
                if (zone.map.mapId == 120) {
                    if (!zone.initBossMabu) {
                        Service.getInstance().sendPercentMabuEgg(player, zone.percentMabuEgg);
                        if (zone.percentMabuEgg == 100) {
                            zone.initBossMabu = true;
                        }
                    }
                }
                if (zone.finishMabuWar) {
                    sendMenuFinishMabuWar(player);
                }
            }
            try {
                if (!isTimeMabuWar()) {
                    kickOutOfMap(player);
                    removeAllBoss();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void kickOutOfMap(Player player) {
        Service.getInstance().sendThongBao(player, "Trận đại chiến đã kết thúc, tàu vận chuyển sẽ đưa bạn về nhà");
        ChangeMapService.gI().changeMapBySpaceShip(player, player.gender + 21, -1, 250);
    }

    public void joinMapMabuWar(Player player) {
        if (!player.isBoss) {
            Service.getInstance().changeFlag(player, 9);
        }
    }

    public void removeAllBoss() {
        if (!clearBoss) {
            for (Boss boss : bosses) {
                boss.leaveMap();
            }
            this.bosses.clear();
            clearBoss = true;
        }
    }

    public void sendMenuGotoNextFloorMabuWar(Player player) {
        if (player.zone.map.mapId != 120) {
            if (!player.sendMenuGotoNextFloorMabuWar) {
                if (player.getPowerPoint() >= 20 || player.getPercentPowerPont() >= 20) {
                    NpcService.gI().createMenuConMeo(player, ConstNpc.MENU_MABU_WAR, player.cFlag == 9 ? 4390 : 4388, "Mau theo ta xuống tầng tiếp theo",
                            "Ok");
                    player.sendMenuGotoNextFloorMabuWar = true;
                }
            }
        }
    }

    public void sendMenuFinishMabuWar(Player player) {
        if (!player.sendMenuGotoNextFloorMabuWar) {
            NpcService.gI().createMenuConMeo(player, ConstNpc.MENU_MABU_WAR, 4390, "Trận chiến đã kết thúc,mau rời khỏi đây",
                    "Ok");
            player.sendMenuGotoNextFloorMabuWar = true;
        }
    }

    public void BabidayTalk(Player player, String text) {
        Npc npc = NpcManager.getByIdAndMap(ConstNpc.BABIDAY, player.zone.map.mapId);
        npc.npcChat(text);
    }

    public Zone getMapLastFloor(int mapId) {
        Map map = MapService.gI().getMapById(mapId);
        try {
            if (map != null) {
                for (Zone zone : map.zones) {
                    if (!zone.finishMabuWar) {
                        return zone;
                    }
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    public void initMabu(Zone zone) {
        new Thread(() -> {
            try {
                Thread.sleep(4000);
                Boss boss = BossFactory.createBoss(BossFactory.MABU_MAP);
                boss.zone = zone;
                bosses.add(boss);
            } catch (Exception e) {
            }
        }).start();
    }
}
