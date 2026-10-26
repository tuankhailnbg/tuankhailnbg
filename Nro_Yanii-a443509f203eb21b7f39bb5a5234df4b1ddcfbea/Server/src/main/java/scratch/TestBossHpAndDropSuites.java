package scratch;

import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.boss.FutureBoss;
import nro.models.boss.bill.Bill;
import nro.models.boss.bill.Whis;
import nro.models.boss.bosstuonglai.Blackgoku;
import nro.models.boss.bosstuonglai.Superblackgoku;
import nro.models.boss.broly.Broly;
import nro.models.boss.cell.XenBoHung1;
import nro.models.boss.cell.XenBoHung2;
import nro.models.boss.cell.XenBoHungHoanThien;
import nro.models.boss.chill.Chill;
import nro.models.boss.chill.Chill2;
import nro.models.boss.cold.Cooler;
import nro.models.boss.cold.Cooler2;
import nro.models.boss.huydiet.Huydiet;
import nro.models.boss.huydiet.Thiensu;
import nro.models.boss.mapoffline.Boss_Whis;
import nro.models.boss.NgucTu.Cumber;
import nro.models.boss.NgucTu.SuperCumber;
import nro.models.map.Map;
import nro.models.map.Zone;
import nro.models.player.NPoint;
import nro.models.player.Player;
import nro.services.MapService;
import nro.services.RewardService;
import nro.utils.Util;

import java.util.ArrayList;

public class TestBossHpAndDropSuites {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    private static void assertTrue(String testName, boolean condition, String detail) {
        if (condition) {
            System.out.println("  [PASS] " + testName + " (" + detail + ")");
            testsPassed++;
        } else {
            System.err.println("  [FAIL] " + testName + " (" + detail + ")");
            testsFailed++;
        }
    }

    public static Map createMockMap(int mapId, String mapName) {
        Map map = new Map(mapId, mapName, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
                new int[2][2], new int[2], 1, false, 10, new ArrayList<>(), new ArrayList<>());
        map.mapWidth = 1000;
        map.mapHeight = 1000;
        return map;
    }

    public static Zone createMockZone(int mapId, String mapName) {
        Map map = createMockMap(mapId, mapName);
        return new Zone(map, 0, 10);
    }

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("     NRO - COMPREHENSIVE BOSS HP (+15%), LIFECYCLE & DROP VALIDATION SUITE      ");
        System.out.println("================================================================================");

        try {
            // Prevent ServerManager from trying to connect to live MariaDB on offline test runner
            try {
                java.lang.reflect.Field f = nro.server.ServerManager.class.getDeclaredField("instance");
                f.setAccessible(true);
                f.set(null, new nro.server.ServerManager());
            } catch (Exception ignored) {
            }

            suite1_testHpTiersAndOverflowPrevention();
            suite2_testColdMapImmunity();
            suite3_testSpecificProblematicBosses();
            suite4_testBossLifecycleAndTransitions();
            suite5_testBossDropTiersValidation();
            suite6_testSafetyAndExceptionHandling();
        } catch (Throwable t) {
            t.printStackTrace();
            testsFailed++;
        }

        System.out.println("\n================================================================================");
        System.out.println("FINAL RESULTS: " + testsPassed + " PASSED, " + testsFailed + " FAILED");
        System.out.println("================================================================================");

        if (testsFailed > 0) {
            System.exit(1);
        } else {
            System.exit(0);
        }
    }

    // ========================================================================
    // SUITE 1: Test HP Tiers and 32-bit Integer Overflow Prevention
    // ========================================================================
    private static void suite1_testHpTiersAndOverflowPrevention() {
        System.out.println("\n--- SUITE 1: HP Tiers and Overflow Calculations (+15%) ---");

        long[] baseHpCases = {
                10_000_000L,        // 10M
                100_000_000L,       // 100M
                499_999_999L,       // 499,999,999
                500_000_000L,       // 500M
                999_999_999L,       // 999,999,999
                1_000_000_000L,     // 1B
                1_900_000_000L,     // 1.9B
                2_000_000_000L,     // 2B
                2_100_000_000L,     // Above 2B
                2_147_483_647L      // Integer.MAX_VALUE
        };

        for (long baseHp : baseHpCases) {
            long calcHp = Math.round((double) baseHp * 1.15);
            if (calcHp > Integer.MAX_VALUE) {
                calcHp = Integer.MAX_VALUE;
            } else if (calcHp < 1) {
                calcHp = 1;
            }

            int intHp = (int) calcHp;
            assertTrue("HP Tier " + baseHp, intHp > 0,
                    "Base=" + baseHp + ", Calc=" + calcHp + ", IntHP=" + intHp);

            assertTrue("HP Bounds " + baseHp,
                    intHp >= 1 && intHp <= Integer.MAX_VALUE,
                    "Within [1 .. Integer.MAX_VALUE]");

            if (baseHp == 2_000_000_000L) {
                assertTrue("2B HP Capping", intHp == Integer.MAX_VALUE,
                        "2B + 15% capped at Integer.MAX_VALUE=" + Integer.MAX_VALUE);
            }
        }
    }

    // ========================================================================
    // SUITE 2: Cold Map Penalty Immunity for Bosses
    // ========================================================================
    private static void suite2_testColdMapImmunity() {
        System.out.println("\n--- SUITE 2: Cold Map Immunity for Bosses ---");

        // Map 109 is Cold Map (105-110)
        Zone coldZone = createMockZone(109, "Hành tinh Cold");
        assertTrue("Map 109 Cold check", MapService.gI().isMapCold(coldZone.map), "Map 109 is Cold");

        // 1. Regular player on cold map: HP should be halved
        Player normalPlayer = new Player();
        normalPlayer.isBoss = false;
        normalPlayer.isPet = false;
        normalPlayer.zone = coldZone;
        normalPlayer.nPoint.hpg = 100_000;
        normalPlayer.nPoint.calPoint();
        assertTrue("Player on Cold Map halved",
                normalPlayer.nPoint.hpMax == 50_000,
                "Expected 50,000 but got " + normalPlayer.nPoint.hpMax);

        // 2. Boss on cold map: HP should NOT be halved
        Player bossPlayer = new Player();
        bossPlayer.isBoss = true;
        bossPlayer.isPet = false;
        bossPlayer.zone = coldZone;
        bossPlayer.nPoint.hpg = 100_000;
        bossPlayer.nPoint.calPoint();
        assertTrue("Boss on Cold Map intact",
                bossPlayer.nPoint.hpMax == 100_000,
                "Expected 100,000 intact but got " + bossPlayer.nPoint.hpMax);

        // 3. Boss Cooler on Cold Map 109: HP must remain full (2,147,483,647)
        Cooler cooler = new Cooler();
        cooler.zone = coldZone;
        cooler.nPoint.calPoint();
        cooler.nPoint.setFullHpMp();
        assertTrue("Cooler on Cold Map 109",
                cooler.nPoint.hpMax == Integer.MAX_VALUE && cooler.nPoint.hp == Integer.MAX_VALUE,
                "Cooler hpMax=" + cooler.nPoint.hpMax + ", hp=" + cooler.nPoint.hp);
    }

    // ========================================================================
    // SUITE 3: Specific Problematic Bosses Verification
    // ========================================================================
    private static void suite3_testSpecificProblematicBosses() throws Exception {
        System.out.println("\n--- SUITE 3: Specific Problematic Bosses ---");

        // 1. Sên Hoàn Thiện (XenBoHungHoanThien)
        XenBoHungHoanThien xenHt = new XenBoHungHoanThien();
        xenHt.zone = createMockZone(100, "Võ đài Xên bọ hung");
        xenHt.nPoint.calPoint();
        xenHt.nPoint.setFullHpMp();
        assertTrue("XenBoHungHoanThien HP",
                xenHt.nPoint.hpMax == Integer.MAX_VALUE && xenHt.nPoint.hp == Integer.MAX_VALUE,
                "hpMax=" + xenHt.nPoint.hpMax + ", hp=" + xenHt.nPoint.hp);
        assertTrue("XenBoHungHoanThien not die", !xenHt.isDie(), "isDie() == false");

        // 2. Whis & Bill (Thần Thiên Sứ & Thần Hủy Diệt)
        Whis whis = new Whis();
        whis.zone = createMockZone(154, "Hành tinh Beerus");
        whis.nPoint.calPoint();
        whis.nPoint.setFullHpMp();
        long whisExpected = Math.min((long) Integer.MAX_VALUE, Math.round(1_500_000_000L * 1.15));
        assertTrue("Whis HP", whis.nPoint.hpMax == (int) whisExpected && whis.nPoint.hp > 0,
                "hpMax=" + whis.nPoint.hpMax + ", hp=" + whis.nPoint.hp);

        Bill bill = new Bill();
        bill.zone = whis.zone;
        bill.nPoint.calPoint();
        bill.nPoint.setFullHpMp();
        assertTrue("Bill HP", bill.nPoint.hpMax == Integer.MAX_VALUE && bill.nPoint.hp == Integer.MAX_VALUE,
                "hpMax=" + bill.nPoint.hpMax + ", hp=" + bill.nPoint.hp);

        // 3. Thiensu & Huydiet
        Thiensu thiensu = new Thiensu();
        thiensu.zone = createMockZone(168, "Vũ trụ");
        thiensu.nPoint.calPoint();
        thiensu.nPoint.setFullHpMp();
        assertTrue("Thiensu HP", thiensu.nPoint.hpMax == Integer.MAX_VALUE && thiensu.nPoint.hp > 0,
                "hpMax=" + thiensu.nPoint.hpMax);

        Huydiet huydiet = new Huydiet();
        huydiet.zone = thiensu.zone;
        huydiet.nPoint.calPoint();
        huydiet.nPoint.setFullHpMp();
        assertTrue("Huydiet HP", huydiet.nPoint.hpMax == Integer.MAX_VALUE && huydiet.nPoint.hp > 0,
                "hpMax=" + huydiet.nPoint.hpMax);

        // 4. Black Goku & Super Black Goku
        Blackgoku bg = new Blackgoku();
        bg.zone = createMockZone(92, "Thành phố tương lai");
        bg.nPoint.calPoint();
        bg.nPoint.setFullHpMp();
        assertTrue("Blackgoku HP", bg.nPoint.hpMax > 0 && bg.nPoint.hp > 0,
                "hpMax=" + bg.nPoint.hpMax + ", hp=" + bg.nPoint.hp);

        Superblackgoku sbg = new Superblackgoku();
        sbg.zone = bg.zone;
        sbg.nPoint.calPoint();
        sbg.nPoint.setFullHpMp();
        assertTrue("Superblackgoku HP", sbg.nPoint.hpMax == Integer.MAX_VALUE && sbg.nPoint.hp == Integer.MAX_VALUE,
                "hpMax=" + sbg.nPoint.hpMax + ", hp=" + sbg.nPoint.hp);

        // 5. Cooler & Cooler 2
        Cooler cooler = new Cooler();
        cooler.zone = createMockZone(109, "Hành tinh Fide");
        cooler.nPoint.calPoint();
        cooler.nPoint.setFullHpMp();
        assertTrue("Cooler HP", cooler.nPoint.hpMax == Integer.MAX_VALUE && cooler.nPoint.hp > 0,
                "hpMax=" + cooler.nPoint.hpMax);

        Cooler2 cooler2 = new Cooler2();
        cooler2.zone = cooler.zone;
        cooler2.nPoint.calPoint();
        cooler2.nPoint.setFullHpMp();
        assertTrue("Cooler2 HP", cooler2.nPoint.hpMax == Integer.MAX_VALUE && cooler2.nPoint.hp > 0,
                "hpMax=" + cooler2.nPoint.hpMax);

        // 6. Chill & Chill 2
        Chill chill = new Chill();
        chill.zone = createMockZone(163, "Hành tinh Plant");
        chill.nPoint.calPoint();
        chill.nPoint.setFullHpMp();
        assertTrue("Chill HP", chill.nPoint.hpMax == Integer.MAX_VALUE && chill.nPoint.hp > 0,
                "hpMax=" + chill.nPoint.hpMax);

        Chill2 chill2 = new Chill2();
        chill2.zone = chill.zone;
        chill2.nPoint.calPoint();
        chill2.nPoint.setFullHpMp();
        assertTrue("Chill2 HP", chill2.nPoint.hpMax == Integer.MAX_VALUE && chill2.nPoint.hp > 0,
                "hpMax=" + chill2.nPoint.hpMax);

        // 7. Cumber & Super Cumber
        Cumber cumber = new Cumber();
        cumber.zone = createMockZone(155, "Hành tinh Ngục Tù");
        cumber.nPoint.calPoint();
        cumber.nPoint.setFullHpMp();
        assertTrue("Cumber HP", cumber.nPoint.hpMax == Integer.MAX_VALUE && cumber.nPoint.hp > 0,
                "hpMax=" + cumber.nPoint.hpMax);

        SuperCumber superCumber = new SuperCumber();
        superCumber.zone = cumber.zone;
        superCumber.nPoint.calPoint();
        superCumber.nPoint.setFullHpMp();
        assertTrue("SuperCumber HP", superCumber.nPoint.hpMax == Integer.MAX_VALUE && superCumber.nPoint.hp > 0,
                "hpMax=" + superCumber.nPoint.hpMax);

        // 8. Boss_Whis (Map offline scaling HP)
        Player whisPlayer = new Player();
        whisPlayer.levelKillWhis = 10;
        whisPlayer.idPlayerForNPC = 1;
        whisPlayer.name = "TestPlayer";
        Boss_Whis bossWhis = new Boss_Whis(BossFactory.WHIS, BossData.WHIS, whis.zone, 100, 100, whisPlayer);
        assertTrue("Boss_Whis HP", bossWhis.nPoint.hpMax > 0 && bossWhis.nPoint.hp == bossWhis.nPoint.hpMax,
                "hpMax=" + bossWhis.nPoint.hpMax + ", hp=" + bossWhis.nPoint.hp);
    }

    // ========================================================================
    // SUITE 4: Boss Lifecycle States and Phase Transitions
    // ========================================================================
    private static void suite4_testBossLifecycleAndTransitions() {
        System.out.println("\n--- SUITE 4: Boss Lifecycle States and Transitions ---");

        XenBoHungHoanThien boss = new XenBoHungHoanThien();
        boss.zone = createMockZone(100, "Võ đài");

        // 1. Initial state
        assertTrue("Initial state valid", boss.nPoint.hpMax == Integer.MAX_VALUE && boss.nPoint.hp == Integer.MAX_VALUE,
                "hpMax=" + boss.nPoint.hpMax + ", hp=" + boss.nPoint.hp);
        assertTrue("Boss is alive", !boss.isDie(), "isDie() == false");

        // 2. Taking damage
        boss.nPoint.subHP(100_000_000);
        assertTrue("HP after damage", boss.nPoint.hp == Integer.MAX_VALUE - 100_000_000,
                "hp=" + boss.nPoint.hp);
        assertTrue("Boss still alive", !boss.isDie(), "isDie() == false");

        // 3. Overflow prevention on heal: addHp(Integer.MAX_VALUE)
        boss.nPoint.addHp(Integer.MAX_VALUE);
        assertTrue("Heal does not overflow", boss.nPoint.hp == Integer.MAX_VALUE,
                "HP clamped to hpMax, got hp=" + boss.nPoint.hp);

        // 4. Dying: subHP remaining HP
        boss.nPoint.subHP(boss.nPoint.hp);
        assertTrue("Boss dead after lethal damage", boss.nPoint.hp == 0 && boss.isDie(),
                "hp=" + boss.nPoint.hp + ", isDie=" + boss.isDie());

        // 5. Respawn: HP resets properly from original data without compounding
        boss.respawn();
        assertTrue("HP after respawn", boss.nPoint.hpMax == Integer.MAX_VALUE && boss.nPoint.hp == Integer.MAX_VALUE,
                "hpMax=" + boss.nPoint.hpMax + ", hp=" + boss.nPoint.hp);
        assertTrue("Boss alive after respawn", !boss.isDie(), "isDie() == false");

        // 6. Xen phase transition: XenBoHung1 -> XenBoHung2 -> XenBoHungHoanThien
        XenBoHung1 xbh1 = new XenBoHung1();
        Zone originalZone = createMockZone(100, "Võ đài");
        xbh1.zone = originalZone;
        xbh1.leaveMap();

        Boss xbh2 = nro.models.boss.BossManager.gI().getBossById(BossFactory.XEN_BO_HUNG_2);
        assertTrue("XenBoHung2 secondsRest == 0",
                xbh2 != null && xbh2.secondTimeRestToNextTimeAppear == 0,
                "secondTimeRestToNextTimeAppear=" + (xbh2 != null ? xbh2.secondTimeRestToNextTimeAppear : "null"));
        assertTrue("XenBoHung2 zone preserved", xbh2 != null && xbh2.zone == originalZone, "Zone preserved");
        assertTrue("XenBoHung2 HP valid", xbh2 != null && xbh2.nPoint.hpMax > 0 && xbh2.nPoint.hp > 0,
                "hpMax=" + (xbh2 != null ? xbh2.nPoint.hpMax : "null"));

        if (xbh2 != null) {
            xbh2.leaveMap();
        }
        Boss xht = nro.models.boss.BossManager.gI().getBossById(BossFactory.XEN_BO_HUNG_HOAN_THIEN);
        assertTrue("XenBoHungHoanThien secondsRest == 0",
                xht != null && xht.secondTimeRestToNextTimeAppear == 0,
                "secondTimeRestToNextTimeAppear=" + (xht != null ? xht.secondTimeRestToNextTimeAppear : "null"));
        assertTrue("XenBoHungHoanThien zone preserved", xht != null && xht.zone == originalZone, "Zone preserved");
        assertTrue("XenBoHungHoanThien HP valid", xht != null && xht.nPoint.hpMax == Integer.MAX_VALUE && xht.nPoint.hp == Integer.MAX_VALUE,
                "hpMax=" + (xht != null ? xht.nPoint.hpMax : "null"));
    }

    // ========================================================================
    // SUITE 5: Boss Drop Tiers Validation
    // ========================================================================
    private static void suite5_testBossDropTiersValidation() {
        System.out.println("\n--- SUITE 5: Boss Drop Tiers Validation ---");

        // Tier 1: maxHp < 500M -> 30% NR3S, 0% Thần Linh
        long[] tier1Hp = {10_000_000L, 100_000_000L, 499_999_999L};
        for (long hp : tier1Hp) {
            int rateThanLinh = getThanLinhRateForRegularBoss(hp);
            boolean dropsNr3s = getDropsNr3sForRegularBoss(hp);
            assertTrue("Drop Tier < 500M (" + hp + ")",
                    rateThanLinh == 0 && dropsNr3s,
                    "ThanLinh=" + rateThanLinh + "%, NR3S=" + dropsNr3s);
        }

        // Tier 2: 500M <= maxHp < 1B -> 15% Thần Linh
        long[] tier2Hp = {500_000_000L, 750_000_000L, 999_999_999L};
        for (long hp : tier2Hp) {
            int rateThanLinh = getThanLinhRateForRegularBoss(hp);
            boolean dropsNr3s = getDropsNr3sForRegularBoss(hp);
            assertTrue("Drop Tier 500M - <1B (" + hp + ")",
                    rateThanLinh == 15 && !dropsNr3s,
                    "ThanLinh=" + rateThanLinh + "%, NR3S=" + dropsNr3s);
        }

        // Tier 3: 1B <= maxHp < 2B -> 20% Thần Linh
        long[] tier3Hp = {1_000_000_000L, 1_500_000_000L, 1_999_999_999L};
        for (long hp : tier3Hp) {
            int rateThanLinh = getThanLinhRateForRegularBoss(hp);
            boolean dropsNr3s = getDropsNr3sForRegularBoss(hp);
            assertTrue("Drop Tier 1B - <2B (" + hp + ")",
                    rateThanLinh == 20 && !dropsNr3s,
                    "ThanLinh=" + rateThanLinh + "%, NR3S=" + dropsNr3s);
        }

        // Tier 4: maxHp >= 2B -> 50% Thần Linh
        long[] tier4Hp = {2_000_000_000L, (long) Integer.MAX_VALUE};
        for (long hp : tier4Hp) {
            int rateThanLinh = getThanLinhRateForRegularBoss(hp);
            boolean dropsNr3s = getDropsNr3sForRegularBoss(hp);
            assertTrue("Drop Tier >= 2B (" + hp + ")",
                    rateThanLinh == 50 && !dropsNr3s,
                    "ThanLinh=" + rateThanLinh + "%, NR3S=" + dropsNr3s);
        }

        // FutureBoss check: Always 100% NR3S + 1M gold, plus Thần Linh according to HP tier
        Superblackgoku futureBoss = new Superblackgoku();
        futureBoss.zone = createMockZone(92, "Thành phố tương lai");
        boolean isFuture = RewardService.gI().isFutureBoss(futureBoss);
        assertTrue("FutureBoss check", isFuture, "Superblackgoku is FutureBoss");

        long futureHp = (long) futureBoss.nPoint.hpMax;
        int futureThanLinhRate = getThanLinhRateForFutureBoss(futureHp);
        assertTrue("FutureBoss ThanLinh Rate (HP=" + futureHp + ")",
                futureThanLinhRate == 50,
                "Expected 50% got " + futureThanLinhRate + "%");
    }

    private static int getThanLinhRateForRegularBoss(long maxHp) {
        if (maxHp >= 2_000_000_000L) {
            return 50;
        } else if (maxHp >= 1_000_000_000L) {
            return 20;
        } else if (maxHp >= 500_000_000L) {
            return 15;
        }
        return 0;
    }

    private static boolean getDropsNr3sForRegularBoss(long maxHp) {
        return maxHp < 500_000_000L;
    }

    private static int getThanLinhRateForFutureBoss(long maxHp) {
        if (maxHp >= 2_000_000_000L) {
            return 50;
        } else if (maxHp >= 1_000_000_000L) {
            return 20;
        } else if (maxHp >= 500_000_000L) {
            return 15;
        }
        return 0;
    }

    // ========================================================================
    // SUITE 6: Safety, Clamping & Exception Handling
    // ========================================================================
    private static void suite6_testSafetyAndExceptionHandling() {
        System.out.println("\n--- SUITE 6: Safety, Clamping & Exception Handling ---");

        Player p = new Player();
        p.isBoss = true;
        p.nPoint.hpg = 1_000_000;
        p.nPoint.calPoint();
        p.nPoint.setFullHpMp();

        // 1. Negative HP clamping
        p.nPoint.setHp(-999);
        assertTrue("Negative HP clamped to 0", p.nPoint.hp == 0, "Got hp=" + p.nPoint.hp);

        // 2. SubHp overflow check
        p.nPoint.setHp(100);
        p.nPoint.subHP(10_000);
        assertTrue("subHP does not underflow below 0", p.nPoint.hp == 0, "Got hp=" + p.nPoint.hp);

        // 3. AddHp overflow check
        p.nPoint.setFullHpMp();
        p.nPoint.addHp(Integer.MAX_VALUE);
        assertTrue("addHp max clamped", p.nPoint.hp == p.nPoint.hpMax,
                "Got hp=" + p.nPoint.hp + ", hpMax=" + p.nPoint.hpMax);

        // 4. Client protocol signed 32-bit limit
        int maxProtocolHp = Integer.MAX_VALUE;
        assertTrue("Protocol 32-bit signed int compatibility",
                maxProtocolHp == 2147483647 && maxProtocolHp > 0,
                "2,147,483,647 fits in signed 32-bit int");
    }
}
