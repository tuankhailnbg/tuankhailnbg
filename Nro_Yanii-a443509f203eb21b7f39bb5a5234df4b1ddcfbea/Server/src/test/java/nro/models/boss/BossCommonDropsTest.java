package nro.models.boss;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import nro.consts.ConstItem;
import nro.models.boss.boss_doanh_trai.NinjaAoTim;
import nro.models.boss.tieudoisatthunamek.So1namek;
import nro.models.item.ItemOptionTemplate;
import nro.models.item.ItemTemplate;
import nro.models.map.ItemMap;
import nro.models.map.Zone;
import nro.models.player.Player;
import nro.server.Maintenance;
import nro.server.Manager;
import nro.services.RewardService;
import nro.utils.Util;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/** Executes production loot and death handlers without a database or game clients. */
public class BossCommonDropsTest {
    private Player killer;

    @BeforeClass
    public static void templates() {
        // Prevent the notification worker from running during offline tests.
        Maintenance.isRuning = true;
        for (int i = 0; i <= 1400; i++) {
            ItemTemplate template = new ItemTemplate();
            template.id = (short) i;
            template.name = "Test item " + i;
            template.type = 5;
            Manager.ITEM_TEMPLATES.add(template);
        }
        for (int i = 0; i <= 300; i++) {
            ItemOptionTemplate option = new ItemOptionTemplate();
            option.id = i;
            option.name = "Test option " + i;
            Manager.ITEM_OPTION_TEMPLATES.add(option);
        }
    }

    @Before
    public void reset() throws Exception {
        killer = new Player();
        killer.id = 42;
        killer.name = "Loot tester";
        Field field = Util.class.getDeclaredField("rand");
        field.setAccessible(true);
        ((Random) field.get(null)).setSeed(20261030L);
        BossManager.BOSSES_IN_GAME.clear();
    }

    @Test
    public void commonLootHasIndependentFifteenPercentRollsAndInclusiveQuantities() {
        TestBoss boss = bossAt(1, 2_000_000_000);
        int[] counts = new int[3];
        boolean[] combinations = new boolean[8];
        boolean[][] quantities = new boolean[2][11];
        for (int i = 0; i < 10000; i++) {
            RewardService.gI().rewardBossDrop(boss, killer);
            int mask = 0;
            for (ItemMap item : drain(boss)) {
                int index = commonIndex(item.itemTemplate.id);
                assertTrue("Unexpected loot outside Future/Cold", index >= 0);
                assertEquals(killer.id, item.playerId);
                assertEquals("A type must not drop twice", 0, mask & (1 << index));
                mask |= 1 << index;
                counts[index]++;
                if (index == 0) {
                    assertEquals(1, item.quantity);
                } else {
                    assertTrue(item.quantity >= 1 && item.quantity <= 10);
                    quantities[index - 1][item.quantity] = true;
                }
            }
            combinations[mask] = true;
        }
        for (int count : counts) {
            assertTrue("Expected about 1500/10000; got " + count, count > 1300 && count < 1700);
        }
        for (boolean combination : combinations) assertTrue("All independent outcomes must occur", combination);
        for (boolean[] seen : quantities) {
            for (int quantity = 1; quantity <= 10; quantity++) assertTrue(seen[quantity]);
        }
    }

    @Test
    public void commonLootAlsoWorksOnMabuNamekAndOtherNonFutureMaps() {
        for (int mapId : new int[] {31, 104, 114, 155, 163, 168}) {
            TestBoss boss = bossAt(mapId, 2_000_000_000);
            int count = 0;
            for (int i = 0; i < 100; i++) {
                RewardService.gI().rewardBossDrop(boss, killer);
                for (ItemMap item : drain(boss)) {
                    assertTrue("No divine loot on map " + mapId, commonIndex(item.itemTemplate.id) >= 0);
                    count++;
                }
            }
            assertTrue("No common loot on map " + mapId, count > 0);
        }
    }

    @Test
    public void divineLootStillRequiresFutureOrColdAndTheExistingHpThreshold() {
        assertEquals(0, divineCount(109, 1_000_000_000));
        int tenPercent = divineCount(109, 1_000_000_001);
        int twentyPercent = divineCount(99, 2_000_000_000);
        assertTrue("10% divine tier: " + tenPercent, tenPercent > 60 && tenPercent < 140);
        assertTrue("20% divine tier: " + twentyPercent, twentyPercent > 140 && twentyPercent < 260);
        assertEquals(0, divineCount(114, 2_000_000_000));
    }

    @Test
    public void rewardsOccurOncePerLifeAndNeverForALivingDespawn() {
        TestBoss boss = bossAt(1, 100);
        assertFalse(boss.rewardOnDefeat(killer));
        assertEquals(0, boss.specificRewards.get());
        boss.nPoint.hp = 0;
        assertTrue(boss.rewardOnDefeat(killer));
        drain(boss);
        assertFalse(boss.rewardOnDefeat(killer));
        assertTrue(drain(boss).isEmpty());
        assertEquals(1, boss.specificRewards.get());
        boss.respawn();
        boss.nPoint.hp = 0;
        assertTrue(boss.rewardOnDefeat(killer));
        assertEquals(2, boss.specificRewards.get());
    }

    @Test
    public void ordinaryLethalDamageAndFollowupDeathHandlerOnlyRewardOnce() {
        TestBoss boss = bossAt(1, 100);
        boss.injured(killer, 2000, true, false);
        assertTrue(boss.isDie());
        assertEquals(1, boss.specificRewards.get());
        drain(boss);
        assertFalse(boss.rewardOnDefeat(killer));
        assertTrue(drain(boss).isEmpty());
        boss.injured(killer, 2000, true, false);
        assertEquals(1, boss.specificRewards.get());
    }

    @Test
    public void concurrentDeathHandlersCannotDuplicateRewards() throws Exception {
        TestBoss boss = bossAt(1, 100);
        boss.nPoint.hp = 0;
        ExecutorService executor = Executors.newFixedThreadPool(8);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger rewarded = new AtomicInteger();
        for (int i = 0; i < 32; i++) {
            executor.submit(() -> {
                try {
                    start.await();
                    if (boss.rewardOnDefeat(killer)) rewarded.incrementAndGet();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        start.countDown();
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        assertEquals(1, rewarded.get());
        assertEquals(1, boss.specificRewards.get());
    }

    @Test
    public void actualNamekCustomDeathPathUsesCommonLootWithoutOldLargeRubyDrops() {
        NamekBoss boss = new NamekBoss();
        boss.zone = zone(31);
        boss.location.x = 100;
        boss.location.y = 150;
        int[] counts = new int[3];
        for (int i = 0; i < 10000; i++) {
            boss.respawn();
            boss.nPoint.hp = 1;
            boss.injured(killer, 100, true, false);
            for (ItemMap item : drain(boss)) {
                int index = commonIndex(item.itemTemplate.id);
                if (index >= 0) {
                    counts[index]++;
                    assertTrue(item.quantity >= 1 && item.quantity <= (index == 0 ? 1 : 10));
                }
            }
        }
        for (int count : counts) assertTrue("Namek common count " + count, count > 1300 && count < 1700);
    }

    @Test
    public void doanhTraiHasOneThreeStarRollAndPreservesFourAndFiveStarRates() {
        DoanhTraiBoss boss = new DoanhTraiBoss();
        boss.zone = zone(59);
        boss.location.x = 100;
        boss.location.y = 150;
        int[] counts = new int[3];
        for (int i = 0; i < 10000; i++) {
            boss.respawn();
            boss.nPoint.hp = 0;
            assertTrue(boss.rewardOnDefeat(killer));
            for (ItemMap item : drain(boss)) {
                int id = item.itemTemplate.id;
                if (id >= 16 && id <= 18) counts[id - 16]++;
            }
        }
        assertTrue("3-star count " + counts[0], counts[0] > 1300 && counts[0] < 1700);
        for (int i = 1; i < 3; i++) {
            assertTrue("4/5-star count " + counts[i], counts[i] > 3100 && counts[i] < 3600);
        }
    }

    private int divineCount(int mapId, int hp) {
        TestBoss boss = bossAt(mapId, hp);
        int count = 0;
        for (int i = 0; i < 1000; i++) {
            RewardService.gI().rewardBossDrop(boss, killer);
            for (ItemMap item : drain(boss)) {
                if (item.itemTemplate.id >= 555 && item.itemTemplate.id <= 567) count++;
            }
        }
        return count;
    }

    private static int commonIndex(int id) {
        if (id == ConstItem.NGOC_RONG_3_SAO) return 0;
        if (id == ConstItem.HONG_NGOC) return 1;
        if (id == ConstItem.THOI_VANG) return 2;
        return -1;
    }

    private List<ItemMap> drain(Boss boss) {
        List<ItemMap> items = boss.zone.getItemMapsForPlayer(killer);
        for (ItemMap item : items) boss.zone.removeItemMap(item);
        return items;
    }

    private static TestBoss bossAt(int mapId, int hp) {
        TestBoss boss = new TestBoss();
        boss.zone = zone(mapId);
        boss.location.x = 100;
        boss.location.y = 150;
        boss.nPoint.hpMax = hp;
        return boss;
    }

    private static Zone zone(int id) {
        nro.models.map.Map map = new nro.models.map.Map(id, "Offline test", (byte) 0,
                (byte) 0, (byte) 0, (byte) 0, (byte) 0, new int[2][2], new int[2],
                0, false, 10, new ArrayList<>(), new ArrayList<>()) {
            @Override public int yPhysicInTop(int x, int y) { return 150; }
        };
        return new Zone(map, 0, 10);
    }

    private static class TestBoss extends Boss {
        final AtomicInteger specificRewards = new AtomicInteger();
        TestBoss() { super(-900000, BossData.KUKU); }
        @Override public void init() { nPoint.hpMax = 100; nPoint.hp = 100; }
        @Override public void rewards(Player player) { specificRewards.incrementAndGet(); }
        @Override public void initTalk() { }
        @Override public void idle() { }
        @Override public void checkPlayerDie(Player player) { }
    }

    private static class NamekBoss extends So1namek {
        @Override public void init() { nPoint.hpMax = 100; nPoint.hp = 100; }
        @Override protected void notifyPlayeKill(Player player) { }
    }

    private static class DoanhTraiBoss extends NinjaAoTim {
        DoanhTraiBoss() { super(new nro.models.map.phoban.DoanhTrai(-1)); }
        @Override public void init() { nPoint.hpMax = 100; nPoint.hp = 100; }
    }
}
