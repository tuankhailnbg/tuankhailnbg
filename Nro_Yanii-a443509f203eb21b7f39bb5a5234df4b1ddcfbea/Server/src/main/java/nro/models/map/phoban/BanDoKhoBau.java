package nro.models.map.phoban;

import nro.models.boss.boss_ban_do_kho_bau.BossBanDoKhoBau;
import nro.models.boss.boss_ban_do_kho_bau.TrungUyXanhLo;
import nro.models.clan.Clan;
import nro.models.map.TrapMap;
import nro.models.map.Zone;
import nro.models.mob.GuardRobot;
import nro.models.mob.Mob;
import nro.models.mob.Octopus;
import nro.models.player.Player;
import nro.services.ItemTimeService;
import nro.services.MobService;
import nro.services.func.ChangeMapService;
import nro.utils.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static nro.models.item.ItemTime.BAN_DO_KHO_BAU;
import nro.services.MapService;
import nro.services.Service;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class BanDoKhoBau {

    public static final long POWER_CAN_GO_TO_DBKB = 2000000000;

    public static final List<BanDoKhoBau> BAN_DO_KHO_BAUS;

    public static final int MAX_AVAILABLE = 50;

    public static final int TIME_BAN_DO_KHO_BAU = 1800000; // 30 phút

    public static int TIME_DELAY_DONE_BDKB_SOM = 70;

    static {
        BAN_DO_KHO_BAUS = new ArrayList<>();
        for (int i = 0; i < MAX_AVAILABLE; i++) {
            BAN_DO_KHO_BAUS.add(new BanDoKhoBau(i));
        }
    }

    public int id;
    public byte level;
    public final List<Zone> zones;
    public final List<BossBanDoKhoBau> bosses;

    public Clan clan;
    public boolean isOpened;
    private long lastTimeOpen;

    public boolean doneBDKBSom = false;

    public long lasTimeDoneDoanhTraiSom;

    public boolean removeText = false;

    public boolean sendTextThongBaoSapKetThuc = false;

    public long timeSendThongBaoBanDo;

    public boolean trungUyIsDie;

    public BanDoKhoBau(int id) {
        this.id = id;
        this.zones = new ArrayList<>();
        this.bosses = new ArrayList<>();
    }

    public void update() {
        if (this.isOpened) {
            if (Util.canDoWithTime(lastTimeOpen, TIME_BAN_DO_KHO_BAU)) {
                finish();
            }
        }
    }

    /*
     * public void update() {
     * if (this.isOpened) {
     * if (doneBDKBSom == true) {
     * if (TIME_DELAY_DONE_BDKB_SOM > 0) {
     * for (Player pl : this.clan.membersInGame) {
     * if (removeText == false) {
     * ItemTimeService.gI().removeTextBanDo(pl);
     * sendThanhTichBanDoKhoBau(pl);
     * }
     * pl.clan.banDoKhoBau.sendTextThongBaoSapKetThuc = true;
     * sendThongBaoBanDoKhoBau();
     * removeText = true;
     * }
     * } else {
     * finish();
     * }
     * } else {
     * if (Util.canDoWithTime(lastTimeOpen, TIME_BAN_DO_KHO_BAU)) {
     * checkTimeBanDoKhoBau();
     * }
     * if (TIME_DELAY_DONE_BDKB_SOM == 0) {
     * finish();
     * }
     * }
     * }
     * }
     */
    public void openBanDoKhoBau(Player plOpen, Clan clan, byte level) {
        this.level = level;
        this.lastTimeOpen = System.currentTimeMillis();
        this.isOpened = true;
        this.clan = clan;
        this.clan.timeOpenBanDoKhoBau = this.lastTimeOpen;
        this.clan.playerOpenBanDoKhoBau = plOpen;
        this.clan.banDoKhoBau = this;
        trungUyIsDie = false;
        TIME_DELAY_DONE_BDKB_SOM = 60;
        resetBanDo();
        ChangeMapService.gI().goToDBKB(plOpen);
        sendTextBanDoKhoBau();
    }

    private void resetBanDo() {
        for (Zone zone : zones) {
            for (TrapMap trap : zone.trapMaps) {
                trap.dame = this.level * 10000;
            }
        }
        for (Zone zone : zones) {
            for (Mob m : zone.mobs) {
                if (m instanceof Octopus) {
                    m.location.x = 740;
                    m.location.y = 576;
                }
                if (m instanceof GuardRobot) {
                    m.location.x = 550;
                    m.location.y = 336;
                }
                MobService.gI().initMobBanDoKhoBau(m, this.level);
                MobService.gI().hoiSinhMob(m);
            }
        }
        for (BossBanDoKhoBau boss : bosses) {
            boss.leaveMap();
        }
        this.bosses.clear();
        initBoss();
    }

    private void initBoss() {
        this.bosses.add(new TrungUyXanhLo(this));
    }

    public void doneBDKB_byAdmin() {
        finish();
    }

    // kết thúc bản đồ kho báu
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
            ChangeMapService.gI().changeMap(pl, 5, -1, 1120, -1);
            ItemTimeService.gI().removeTextBanDo(pl);
        }
        this.clan.banDoKhoBau = null;
        this.clan = null;
        this.isOpened = false;

    }

    public Zone getMapById(int mapId) {
        for (Zone zone : zones) {
            if (zone.map.mapId == mapId) {
                return zone;
            }
        }
        return null;
    }

    /*
     * public void checkTimeBanDoKhoBau() {
     * for (Player player : this.clan.membersInGame) {
     * player.clan.banDoKhoBau.sendTextThongBaoSapKetThuc = true;
     * sendThongBaoBanDoKhoBau();
     * }
     * }
     */
    public void sendThanhTichBanDoKhoBau(Player pl) {
        long timeDoneBDKB;
        timeDoneBDKB = System.currentTimeMillis() - pl.clan.timeOpenBanDoKhoBau;
        int levelDoneBDKB;
        levelDoneBDKB = pl.clan.banDoKhoBau.level;
        if (levelDoneBDKB > pl.clan.levelDoneBanDoKhoBau) {
            pl.clan.levelDoneBanDoKhoBau = levelDoneBDKB;
            pl.clan.thoiGianHoanThanhBDKB = timeDoneBDKB;
            System.out.println("levelDoneBDKB: " + levelDoneBDKB);
            System.out.println("timeDoneBDKB: " + timeDoneBDKB);
        } else if (levelDoneBDKB == pl.clan.levelDoneBanDoKhoBau) {
            if (timeDoneBDKB < pl.clan.thoiGianHoanThanhBDKB) {
                pl.clan.thoiGianHoanThanhBDKB = timeDoneBDKB;
            }
        }
        pl.clan.updatethanhTichBDKB(pl.clan.id);
        pl.clan.updatethanhTichBDKBForLeader();
        pl.clan.updateThongTinLeader(pl.clan.id);
    }

    /*
     * public void sendThongBaoBanDoKhoBau() {
     * for (Player player : this.clan.membersInGame) {
     * if (sendTextThongBaoSapKetThuc = true) {
     * if (player.clan != null && player.clan.banDoKhoBau != null &&
     * player.clan.banDoKhoBau.isOpened && player.clan.timeOpenBanDoKhoBau != 0) {
     * if (MapService.gI().isMapBanDoKhoBau(player.zone.map.mapId)) {
     * if (Util.canDoWithTime(timeSendThongBaoBanDo, 1000 * 10)) {
     * TIME_DELAY_DONE_BDKB_SOM -= 10;
     * if (TIME_DELAY_DONE_BDKB_SOM > 0) {
     * Service.getInstance().sendThongBao(player,
     * "Cái hang này sắp sập rồi, chúng ta phải rời khỏi đây ngay " +
     * TIME_DELAY_DONE_BDKB_SOM + " giây nữa");
     * timeSendThongBaoBanDo = System.currentTimeMillis();
     * }
     * }
     * }
     * }
     * }
     * }
     * }
     */
    public static void addZone(int idBanDo, Zone zone) {
        BAN_DO_KHO_BAUS.get(idBanDo).zones.add(zone);
    }

    private void sendTextBanDoKhoBau() {
        for (Player pl : this.clan.membersInGame) {
            ItemTimeService.gI().sendTextBanDoKhoBau(pl);
        }
    }
}
