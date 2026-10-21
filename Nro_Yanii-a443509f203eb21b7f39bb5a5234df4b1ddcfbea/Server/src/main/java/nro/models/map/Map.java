package nro.models.map;

import nro.consts.ConstMap;
import nro.models.mob.MobTemplate;
import nro.models.map.war.BlackBallWar;
//import nro.models.map.phoban.BanDoKhoBau;
import nro.models.map.phoban.DoanhTrai;
import nro.models.map.zones.Barrack;
import nro.models.mob.Mob;
import nro.models.mob.MobFactory;
import nro.models.npc.Npc;
import nro.models.npc.NpcFactory;
import nro.models.player.Player;
import nro.server.Manager;
import nro.services.Service;
import nro.utils.Util;

import java.util.ArrayList;
import java.util.List;
import nro.models.map.phoban.BanDoKhoBau;
import nro.models.map.phoban.KhiGas;
import nro.models.npc.NpcManager;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class Map implements Runnable {

    public static final byte T_EMPTY = 0;
    public static final byte T_TOP = 2;
    private static final int SIZE = 24;

    public int mapId;
    public String mapName;

    public byte planetId;
    public String planetName;

    public byte tileId;
    public byte bgId;
    public byte bgType;
    public byte type;

    private int[][] tileMap;
    public int[] tileTop;
    public int mapWidth;
    public int mapHeight;
    public boolean isMapOffline;

    public List<Zone> zones;
    public List<WayPoint> wayPoints;
    public List<Npc> npcs;
    public List<EffectMap> effMap;

    public Map(int mapId, String mapName, byte planetId,
            byte tileId, byte bgId, byte bgType, byte type, int[][] tileMap,
            int[] tileTop, int zones, boolean isMapOffline, int maxPlayer, List<WayPoint> wayPoints,
            List<EffectMap> effMap) {
        this.mapId = mapId;
        this.mapName = mapName;
        this.planetId = planetId;
        this.planetName = Service.getInstance().get_HanhTinh(planetId);
        this.tileId = tileId;
        this.bgId = bgId;
        this.bgType = bgType;
        this.type = type;
        this.tileMap = tileMap;
        this.tileTop = tileTop;
        this.isMapOffline = isMapOffline;
        this.wayPoints = wayPoints;
        this.effMap = effMap;
        initZone(zones, maxPlayer);
        try {
            this.mapHeight = tileMap.length * SIZE;
            this.mapWidth = tileMap[0].length * SIZE;
        } catch (Exception e) {
        }
        initItem();
        initTrapMap();
    }

    public void addZone(Zone z) {
        this.zones.add(z);
    }

    public void addNpc(Npc npc) {
        this.npcs.add(npc);
    }

    public void initZone(int number, int maxPlayer) {
        this.zones = new ArrayList<>();
        int countZone = 1;
        if (this.type == ConstMap.MAP_NORMAL) {
            countZone = number;
        } else if (this.type == ConstMap.MAP_DOANH_TRAI) {
            countZone = DoanhTrai.MAX_AVAILABLE;
        } else if (this.type == ConstMap.MAP_BLACK_BALL_WAR) {
            countZone = BlackBallWar.ZONES;
            BlackBallWar.gI().addMap(this);
        } else if (this.type == ConstMap.MAP_BAN_DO_KHO_BAU) {
            countZone = BanDoKhoBau.MAX_AVAILABLE;
        } else if (this.type == ConstMap.MAP_KHI_GAS_HUY_DIET) {
            countZone = BanDoKhoBau.MAX_AVAILABLE;
        }
        for (int i = 0; i < countZone; i++) {
            Zone zone = null;
            if (this.type == ConstMap.MAP_DOANH_TRAI) {
                zone = new Barrack(this, i, maxPlayer);
                DoanhTrai.addZone(i, zone);
            } else {
                zone = new Zone(this, i, maxPlayer);
            }
            if (this.type == ConstMap.MAP_BAN_DO_KHO_BAU) {
                BanDoKhoBau.addZone(i, zone);
            }
            if (this.type == ConstMap.MAP_BAN_DO_KHO_BAU) {
                BanDoKhoBau.addZone(i, zone);
            }
            if (this.type == ConstMap.MAP_KHI_GAS_HUY_DIET) {
                KhiGas.addZone(i, zone);
            }
            this.zones.add(zone);
        }
    }

    public void initReferee() {

    }

    public void initNpc(byte[] npcId, short[] npcX, short[] npcY, short[] npcAvatar) {
        this.npcs = new ArrayList<>();
        for (int i = 0; i < npcId.length; i++) {
            this.npcs.add(NpcFactory.createNPC(this.mapId, 1, npcX[i], npcY[i], npcId[i], npcAvatar[i]));
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                long st = System.currentTimeMillis();
                for (Zone zone : this.zones) {
                    zone.update();
                }
                long timeDo = System.currentTimeMillis() - st;
                Thread.sleep(1000 - timeDo);
            } catch (Exception e) {
                // Logger.logException(Map.class, e, "Lỗi update map " + this.mapName);
            }
        }
    }

    public void initMob(byte[] mobTemp, byte[] mobLevel, int[] mobHp, short[] mobX, short[] mobY) {
        for (int i = 0; i < mobTemp.length; i++) {
            int mobTempId = mobTemp[i];
            MobTemplate temp = Manager.getMobTemplateByTemp(mobTempId);
            if (temp != null) {
                Mob mob = new Mob();
                mob.tempId = mobTemp[i];
                mob.level = mobLevel[i];
                mob.point.setHpFull((int) mobHp[i]);
                mob.location.x = mobX[i];
                mob.location.y = mobY[i];
                mob.point.setHP(mob.point.getHpFull());
                mob.pDame = temp.percentDame;
                mob.pTiemNang = temp.percentTiemNang;
                mob.setTiemNang();
                mob.status = 5;
                for (Zone zone : this.zones) {
                    Mob mobZone = MobFactory.newMob(mob);
                    mobZone.zone = zone;
                    zone.addMob(mobZone);
                }
            }
        }
    }

    public void initMob(List<Mob> mobs) {
        for (Zone zone : zones) {
            for (Mob m : mobs) {
                Mob mob = new Mob(m);
                mob.zone = zone;
                zone.addMob(mob);
            }
        }
    }

    private void initTrapMap() {
        for (Zone zone : zones) {
            TrapMap trap = null;
            switch (this.mapId) {
                case 135:
                    trap = new TrapMap();
                    trap.x = 260;
                    trap.y = 960;
                    trap.w = 740;
                    trap.h = 72;
                    trap.effectId = 49; // xiên
                    zone.trapMaps.add(trap);
                    break;
            }
        }
    }

    private void initItem() {
        for (Zone zone : zones) {
            ItemMap itemMap = null;
            switch (this.mapId) {
                case 21:
                    itemMap = new ItemMap(zone, 74, 1, 633, 315, -1);
                    break;
                case 22:
                    itemMap = new ItemMap(zone, 74, 1, 56, 315, -1);
                    break;
                case 23:
                    itemMap = new ItemMap(zone, 74, 1, 633, 320, -1);
                    break;
                case 42:
                    itemMap = new ItemMap(zone, 78, 1, 70, 288, -1);
                    break;
                case 43:
                    itemMap = new ItemMap(zone, 78, 1, 70, 264, -1);
                    break;
                case 44:
                    itemMap = new ItemMap(zone, 78, 1, 70, 288, -1);
                    break;
                case 85: // 1 sao đen
                    itemMap = new ItemMap(zone, 372, 1, 0, 0, -1);
                    break;
                case 86: // 2 sao đen
                    itemMap = new ItemMap(zone, 373, 1, 0, 0, -1);
                    break;
                case 87: // 3 sao đen
                    itemMap = new ItemMap(zone, 374, 1, 0, 0, -1);
                    break;
                case 88: // 4 sao đen
                    itemMap = new ItemMap(zone, 375, 1, 0, 0, -1);
                    break;
                case 89: // 5 sao đen
                    itemMap = new ItemMap(zone, 376, 1, 0, 0, -1);
                    break;
                case 90: // 6 sao đen
                    itemMap = new ItemMap(zone, 377, 1, 0, 0, -1);
                    break;
                case 91: // 7 sao đen
                    itemMap = new ItemMap(zone, 378, 1, 0, 0, -1);
                    break;
            }
        }

    }

    public Npc getNpc(Player player, int tempId) {
        for (Npc npc : npcs) {
            if (npc.tempId == tempId && Util.getDistance(player, npc) <= 60) {
                return npc;
            }
        }
        return null;
    }

    // --------------------------------------------------------------------------
    public int yPhysicInTop(int x, int y) {
        int rX = (int) x / SIZE;
        int rY = 0;
        if (isTileTop(tileMap[y / SIZE][rX])) {
            return y;
        }
        for (int i = y / SIZE; i < tileMap.length; i++) {
            if (isTileTop(tileMap[i][rX])) {
                rY = i * SIZE;
                break;
            }
        }
        return rY;
    }

    private boolean isTileTop(int tileMap) {
        for (int i = 0; i < tileTop.length; i++) {
            if (tileTop[i] == tileMap) {
                return true;
            }
        }
        return false;
    }

    public boolean isMapLang() {
        return mapId == ConstMap.LANG_ARU || mapId == ConstMap.LANG_KAKAROT || mapId == ConstMap.LANG_MORI;
    }

    public void removeZone(Zone z) {
        zones.remove(z);
    }
}
