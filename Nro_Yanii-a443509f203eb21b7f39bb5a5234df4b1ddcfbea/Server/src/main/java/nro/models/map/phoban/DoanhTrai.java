package nro.models.map.phoban;

import nro.models.boss.BossFactory;
import nro.models.boss.boss_doanh_trai.BossDoanhTrai;
import nro.models.boss.boss_doanh_trai.NinjaAoTim;
import nro.models.boss.boss_doanh_trai.RobotVeSi;
import nro.models.boss.boss_doanh_trai.TrungUyThep;
import nro.models.boss.boss_doanh_trai.TrungUyTrang;
import nro.models.boss.boss_doanh_trai.TrungUyXanhLo;
import nro.models.clan.Clan;
import nro.models.map.Zone;
import nro.models.mob.Mob;
import nro.models.player.Player;
import nro.services.ItemTimeService;
import nro.services.MobService;
import nro.services.func.ChangeMapService;
import nro.utils.Util;

import java.util.ArrayList;
import java.util.List;
import nro.models.map.ItemMap;
import nro.services.Service;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class DoanhTrai {

    public static final int DATE_WAIT_FROM_JOIN_CLAN = 1;

    public static final List<DoanhTrai> DOANH_TRAIS;
    public static final int MAX_AVAILABLE = 50;
    public static final int TIME_DOANH_TRAI = 1800000;

    static {
        DOANH_TRAIS = new ArrayList<>();
        for (int i = 0; i < MAX_AVAILABLE; i++) {
            DOANH_TRAIS.add(new DoanhTrai(i));
        }
    }

    public final int id;
    public final List<Zone> zones;
    public final List<BossDoanhTrai> bosses;

    public Clan clan;
    public boolean isOpened;
    private long lastTimeOpen;

    public boolean isHaveDoneDoanhTrai = false;

    public long lastTimeDoneDoanhTrai;

    public DoanhTrai(int id) {
        this.id = id;
        this.zones = new ArrayList<>();
        this.bosses = new ArrayList<>();
    }

    public boolean doneSendTextAgain = false;

    public void update() {
        if (this.isOpened) {
            if (!this.isHaveDoneDoanhTrai) {
                if (Util.canDoWithTime(lastTimeOpen, TIME_DOANH_TRAI)) {
                    this.finish();
                }
            } else {
                if (doneSendTextAgain = false) {
                    sendTextDoanhTraiAllClan();
                    doneSendTextAgain = true;
                }
                if (Util.canDoWithTime(lastTimeDoneDoanhTrai, 300000)) {
                    this.finish();
                }
            }
        }
    }

    public void openDoanhTrai(Player plOpen, Clan clan) {
        this.lastTimeOpen = System.currentTimeMillis();
        this.isOpened = true;
        this.clan = clan;
        this.clan.timeOpenDoanhTrai = this.lastTimeOpen;
        this.clan.playerOpenDoanhTrai = plOpen;
        this.clan.doanhTrai = this;
        resetDoanhTrai();
        List<Player> plJoinDT = new ArrayList();
        List<Player> players = plOpen.zone.getPlayers();
        synchronized (players) {
            for (Player pl : players) {
                if (pl.clan != null && pl.clan.id == plOpen.clan.id
                        && pl.location.x >= 1285 && pl.location.x <= 1645) {
                    plJoinDT.add(pl);
                }

            }
        }
        for (Player pl : plJoinDT) {
            if (pl.isAdmin() || pl.clanMember.getNumDateFromJoinTimeToToday() >= DATE_WAIT_FROM_JOIN_CLAN) {
                ChangeMapService.gI().changeMap(pl, 53, -1, 35, 432);
            }
        }
        sendTextDoanhTraiAllClan();
    }

    // kết thúc doanh trại
    private void finish() {
        List<Player> plOutDT = new ArrayList();
        for (Zone zone : zones) {
            List<Player> players = zone.getPlayers();
            synchronized (players) {
                for (Player pl : players) {
                    plOutDT.add(pl);
                }
            }

        }
        for (Player pl : plOutDT) {
            ChangeMapService.gI().changeMapBySpaceShip(pl, pl.gender + 21, -1, -1);
        }
        this.clan.haveGoneDoanhTrai = true;
        this.clan.doanhTrai = null;
        this.clan = null;
        this.isOpened = false;
    }

    private void resetDoanhTrai() {
        for (Zone zone : zones) {
            for (Mob m : zone.mobs) {
                MobService.gI().initMobDoanhTrai(m, this.clan);
                MobService.gI().hoiSinhMob(m);
            }
        }
        for (BossDoanhTrai boss : bosses) {
            boss.leaveMap();
        }
        this.bosses.clear();
        initBoss();
    }

    public void DropNgocRong() {
        for (Zone zone : zones) {
            ItemMap itemMap = null;

            switch (zone.map.mapId) {
                case 53:
                    itemMap = new ItemMap(zone, Util.nextInt(16, 20), 1, 917, 384, -1);
                    itemMap.isDoanhTraiBall = true;
                    Service.getInstance().dropItemMap(zone, itemMap);
                    break;
                case 58:
                    itemMap = new ItemMap(zone, Util.nextInt(16, 20), 1, 658, 336, -1);
                    itemMap.isDoanhTraiBall = true;
                    Service.getInstance().dropItemMap(zone, itemMap);
                    break;
                case 59:
                    itemMap = new ItemMap(zone, Util.nextInt(16, 20), 1, 675, 240, -1);
                    itemMap.isDoanhTraiBall = true;
                    Service.getInstance().dropItemMap(zone, itemMap);
                    break;
                case 60:
                    itemMap = new ItemMap(zone, Util.nextInt(16, 20), 1, Util.nextInt(725, 1241), 384, -1);
                    itemMap.isDoanhTraiBall = true;
                    Service.getInstance().dropItemMap(zone, itemMap);
                    break;
                case 61:
                    itemMap = new ItemMap(zone, Util.nextInt(16, 20), 1, 789, 264, -1);
                    itemMap.isDoanhTraiBall = true;
                    Service.getInstance().dropItemMap(zone, itemMap);
                    break;
                case 62:
                    itemMap = new ItemMap(zone, Util.nextInt(16, 20), 1, Util.nextInt(197, 1294), 384, -1);
                    itemMap.isDoanhTraiBall = true;
                    Service.getInstance().dropItemMap(zone, itemMap);
                    break;
                case 55:
                    itemMap = new ItemMap(zone, Util.nextInt(16, 20), 1, 422, 288, -1);
                    itemMap.isDoanhTraiBall = true;
                    Service.getInstance().dropItemMap(zone, itemMap);
                    break;
                case 56:
                    itemMap = new ItemMap(zone, Util.nextInt(16, 20), 1, 789, 312, -1);
                    itemMap.isDoanhTraiBall = true;
                    Service.getInstance().dropItemMap(zone, itemMap);
                    break;
                case 54:
                    itemMap = new ItemMap(zone, Util.nextInt(16, 20), 1, 211, 1228, -1);
                    itemMap.isDoanhTraiBall = true;
                    Service.getInstance().dropItemMap(zone, itemMap);
                    break;
            }

        }
    }

    public Zone getMapById(int mapId) {
        for (Zone zone : zones) {
            if (zone.map.mapId == mapId) {
                return zone;
            }
        }
        return null;
    }

    private void initBoss() {
        this.bosses.add(new TrungUyTrang(this));
        this.bosses.add(new TrungUyXanhLo(this));
        this.bosses.add(new TrungUyThep(this));
        this.bosses.add(new NinjaAoTim(this));
        this.bosses.add(new RobotVeSi(BossFactory.ROBOT_VE_SI_1, this));
        this.bosses.add(new RobotVeSi(BossFactory.ROBOT_VE_SI_2, this));
        this.bosses.add(new RobotVeSi(BossFactory.ROBOT_VE_SI_3, this));
        this.bosses.add(new RobotVeSi(BossFactory.ROBOT_VE_SI_4, this));
    }

    private void sendTextDoanhTraiAllClan() {
        for (Player pl : this.clan.membersInGame) {
            ItemTimeService.gI().sendTextDoanhTrai(pl);
        }
    }

    private void sendRemoveTextDoanhTraiAllClan() {
        for (Player pl : this.clan.membersInGame) {
            ItemTimeService.gI().removeTextDoanhTrai(pl);
        }
    }

    public static void addZone(int idDoanhTrai, Zone zone) {
        DOANH_TRAIS.get(idDoanhTrai).zones.add(zone);
    }
}
