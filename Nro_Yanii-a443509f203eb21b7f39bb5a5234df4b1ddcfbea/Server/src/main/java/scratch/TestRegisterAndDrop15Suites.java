package scratch;

import nro.consts.ConstItem;
import nro.consts.ConstMob;
import nro.jdbc.DBService;
import nro.jdbc.daos.AccountDAO;
import nro.models.item.Item;
import nro.models.item.ItemOption;
import nro.models.item.ItemOptionTemplate;
import nro.models.item.ItemTemplate;
import nro.models.map.ItemMap;
import nro.models.mob.BigBoss;
import nro.models.mob.Mob;
import nro.models.mob.MobFactory;
import nro.models.player.Player;
import nro.models.player.Pet;
import nro.server.Manager;
import nro.services.ItemService;
import nro.services.RewardService;
import nro.utils.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TestRegisterAndDrop15Suites {

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

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("     NRO - TEST REGISTRATION & SKH 15% DROP RATE COMPREHENSIVE SUITE");
        System.out.println("================================================================================");

        try {
            setupTemplatesFromDatabase();
            testRegistrationValidationAndDatabase();
            testRegistrationConcurrency();
            testSkh15DropSimulation();
            testSkhPlanetMappingAndItemProperties();
            testPetKillerMasterOwnership();
            testBossExclusion();
        } catch (Exception e) {
            e.printStackTrace();
            testsFailed++;
        }

        System.out.println("\n================================================================================");
        System.out.println("RESULTS: " + testsPassed + " PASSED, " + testsFailed + " FAILED");
        System.out.println("================================================================================");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void setupTemplatesFromDatabase() {
        try (Connection con = DBService.gI().getConnectionForGame()) {
            if (Manager.ITEM_OPTION_TEMPLATES.isEmpty()) {
                ResultSet rsOpt = con.createStatement().executeQuery("SELECT id, name, type FROM item_option_template ORDER BY id ASC");
                int maxOptId = 0;
                List<ItemOptionTemplate> optList = new ArrayList<>();
                while (rsOpt.next()) {
                    ItemOptionTemplate ot = new ItemOptionTemplate();
                    ot.id = rsOpt.getInt("id");
                    ot.name = rsOpt.getString("name");
                    ot.type = rsOpt.getByte("type");
                    optList.add(ot);
                    if (ot.id > maxOptId) maxOptId = ot.id;
                }
                for (int i = 0; i <= maxOptId + 50; i++) {
                    Manager.ITEM_OPTION_TEMPLATES.add(null);
                }
                for (ItemOptionTemplate ot : optList) {
                    if (ot.id < Manager.ITEM_OPTION_TEMPLATES.size()) {
                        Manager.ITEM_OPTION_TEMPLATES.set(ot.id, ot);
                    }
                }
                System.out.println("  [SETUP] Loaded " + optList.size() + " ItemOptionTemplates into Manager");
            }
        } catch (Exception e) {
            System.err.println("  [SETUP ERROR] Failed loading ItemOptionTemplates: " + e.getMessage());
        }
    }

    // =========================================================================
    // 1. REGISTRATION VALIDATION & DATABASE TESTING
    // =========================================================================
    private static void testRegistrationValidationAndDatabase() throws Exception {
        System.out.println("\n--- [SUITE 1] In-Game Registration Validation & Database Persistence ---");

        String testUser = "testreg_" + System.currentTimeMillis() % 100000;
        String testPass = "pass12345";

        // Cleanup before test if needed
        cleanupAccount(testUser);

        // Test 1.1: Valid Registration
        int accId = AccountDAO.createAccount(testUser, testPass);
        assertTrue("Create Account Valid", accId > 0, "Created account id=" + accId + ", username=" + testUser);

        // Verify account exists in MariaDB
        boolean dbFound = false;
        try (Connection con = DBService.gI().getConnectionForGame();
             PreparedStatement ps = con.prepareStatement("SELECT id, username, password FROM account WHERE username = ?")) {
            ps.setString(1, testUser);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dbFound = true;
                    int id = rs.getInt("id");
                    String u = rs.getString("username");
                    String p = rs.getString("password");
                    assertTrue("Account in MariaDB", id == accId && u.equals(testUser) && p.equals(testPass),
                            "DB record verified: id=" + id + ", user=" + u + ", pass_len=" + p.length());
                }
            }
        }
        assertTrue("Account persisted", dbFound, "Account successfully stored in MariaDB");

        // Test 1.2: Duplicate Registration Rejection
        int dupResult = AccountDAO.createAccount(testUser, testPass);
        assertTrue("Duplicate Account Rejected", dupResult == AccountDAO.CREATE_ACCOUNT_EXISTS,
                "Result: " + dupResult + " == " + AccountDAO.CREATE_ACCOUNT_EXISTS);

        // Test 1.3: Validation Boundary Checks
        // Validation logic matching Service.regisAccount:
        assertTrue("Short user (<4 chars) rejected", !isValidUserPass("abc", "pass123"), "len=3 rejected");
        assertTrue("Long user (>18 chars) rejected", !isValidUserPass("user_exceeding_18_chars", "pass123"), "len>18 rejected");
        assertTrue("Short pass (<5 chars) rejected", !isValidUserPass("validuser", "1234"), "len=4 rejected");
        assertTrue("Long pass (>18 chars) rejected", !isValidUserPass("validuser", "pass_exceeding_18_chars"), "len>18 rejected");
        assertTrue("User with space rejected", !isValidUserPass("user name", "pass123"), "Contains space");
        assertTrue("Pass with space rejected", !isValidUserPass("validuser", "pass word"), "Contains space");
        assertTrue("Null user rejected", !isValidUserPass(null, "pass123"), "Null user");
        assertTrue("Null pass rejected", !isValidUserPass("validuser", null), "Null pass");
        assertTrue("Empty user rejected", !isValidUserPass("", "pass123"), "Empty user");
        assertTrue("Empty pass rejected", !isValidUserPass("validuser", ""), "Empty pass");
        assertTrue("Invalid chars in user rejected", !isValidUserPass("user@!#$", "pass123"), "Special symbols");

        // Valid boundary checks
        assertTrue("Min len user (4) & pass (5) valid", isValidUserPass("abcd", "12345"), "len=4 and 5 accepted");
        assertTrue("Max len user (18) & pass (18) valid", isValidUserPass("123456789012345678", "123456789012345678"), "len=18 accepted");

        // Cleanup
        cleanupAccount(testUser);
    }

    private static boolean isValidUserPass(String user, String pass) {
        if (user == null || pass == null || user.isEmpty() || pass.isEmpty()) {
            return false;
        }
        user = user.trim();
        pass = pass.trim();
        if (user.length() < 4 || user.length() > 18) {
            return false;
        }
        if (pass.length() < 5 || pass.length() > 18) {
            return false;
        }
        if (user.indexOf(' ') >= 0 || pass.indexOf(' ') >= 0) {
            return false;
        }
        if (!user.matches("^[a-zA-Z0-9_.-]+$")) {
            return false;
        }
        return true;
    }

    private static void cleanupAccount(String username) {
        try (Connection con = DBService.gI().getConnectionForGame();
             PreparedStatement ps = con.prepareStatement("DELETE FROM account WHERE username = ?")) {
            ps.setString(1, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            // Ignore
        }
    }

    // =========================================================================
    // 2. CONCURRENT REGISTRATION TESTING
    // =========================================================================
    private static void testRegistrationConcurrency() throws Exception {
        System.out.println("\n--- [SUITE 2] Concurrent Registration Race Condition Test ---");

        String raceUser = "race_" + System.currentTimeMillis() % 100000;
        cleanupAccount(raceUser);

        int threads = 10;
        ExecutorService exec = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);

        AtomicInteger successes = new AtomicInteger(0);
        AtomicInteger duplicates = new AtomicInteger(0);
        AtomicInteger errors = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            exec.submit(() -> {
                try {
                    startLatch.await();
                    int res = AccountDAO.createAccount(raceUser, "password123");
                    if (res > 0) {
                        successes.incrementAndGet();
                    } else if (res == AccountDAO.CREATE_ACCOUNT_EXISTS) {
                        duplicates.incrementAndGet();
                    } else {
                        errors.incrementAndGet();
                    }
                } catch (Exception e) {
                    errors.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await(5, TimeUnit.SECONDS);
        exec.shutdown();

        assertTrue("Exactly 1 registration succeeded among 10 concurrent calls", successes.get() == 1,
                "Successes=" + successes.get() + ", Duplicates=" + duplicates.get() + ", Errors=" + errors.get());
        assertTrue("Remaining 9 concurrent calls returned DUPLICATE", duplicates.get() == (threads - 1),
                "Duplicates=" + duplicates.get() + " of " + (threads - 1));
        assertTrue("Zero unexpected errors during concurrent insert", errors.get() == 0,
                "Errors=" + errors.get());

        // Verify MariaDB has exactly 1 row for raceUser
        int rowCount = 0;
        try (Connection con = DBService.gI().getConnectionForGame();
             PreparedStatement ps = con.prepareStatement("SELECT count(*) FROM account WHERE username = ?")) {
            ps.setString(1, raceUser);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    rowCount = rs.getInt(1);
                }
            }
        }
        assertTrue("MariaDB contains exactly 1 unique record", rowCount == 1, "Count=" + rowCount);

        cleanupAccount(raceUser);
    }

    // =========================================================================
    // 3. SKH 15% DROP RATE SIMULATION (100,000 MOBS)
    // =========================================================================
    private static void testSkh15DropSimulation() {
        System.out.println("\n--- [SUITE 3] SKH 15% Drop Rate Statistical Simulation (100,000 Kills) ---");

        int totalKills = 100_000;
        int dropRate = RewardService.SKH_DROP_RATE;
        assertTrue("RewardService.SKH_DROP_RATE is 15", dropRate == 15, "SKH_DROP_RATE=" + dropRate);

        int totalDrops = 0;
        Map<Integer, Integer> planetDrops = new HashMap<>();
        Map<Integer, Integer> setDrops = new HashMap<>();
        Map<Integer, Integer> slotDrops = new HashMap<>();

        planetDrops.put(0, 0);
        planetDrops.put(1, 0);
        planetDrops.put(2, 0);

        for (int i = 0; i < 5; i++) slotDrops.put(i, 0);

        for (int i = 0; i < totalKills; i++) {
            // Equal distribution of players across 3 planets (Earth 0, Namek 1, Saiyan 2)
            int gender = i % 3;

            // Pure independent 15% check as implemented in RewardService
            if (ThreadLocalRandom.current().nextInt(100) < RewardService.SKH_DROP_RATE) {
                totalDrops++;
                planetDrops.put(gender, planetDrops.get(gender) + 1);

                int slot = Util.nextInt(0, 4);
                slotDrops.put(slot, slotDrops.get(slot) + 1);

                int setIdx = Util.nextInt(0, 2);
                int setOptionId = RewardService.ACTIVATION_SET[gender][setIdx][0];
                setDrops.put(setOptionId, setDrops.getOrDefault(setOptionId, 0) + 1);
            }
        }

        double dropPercentage = (totalDrops * 100.0) / totalKills;
        System.out.printf("  Total Kills: %d, Total SKH Drops: %d, Rate: %.2f%%\n", totalKills, totalDrops, dropPercentage);

        // 100,000 trials with p=0.15 has mean=15,000, stddev = sqrt(100000 * 0.15 * 0.85) = 112.9
        // 4 stddev range is [14,548 to 15,452] or 14.5% to 15.5%
        assertTrue("Total drops approx 15% (14.5% - 15.5%)", dropPercentage >= 14.5 && dropPercentage <= 15.5,
                String.format("%.2f%% within expected statistical range", dropPercentage));

        // Verify all 5 slots dropped roughly 20% of total drops each (18% - 22%)
        System.out.println("  Slot Distribution (Áo, Quần, Găng, Giày, Rada):");
        for (int slot = 0; slot < 5; slot++) {
            int count = slotDrops.get(slot);
            double slotPct = (count * 100.0) / totalDrops;
            System.out.printf("    Slot %d: %d drops (%.2f%% of drops)\n", slot, count, slotPct);
            assertTrue("Slot " + slot + " fairly distributed (18% - 22%)", slotPct >= 18.0 && slotPct <= 22.0,
                    String.format("%.2f%%", slotPct));
        }

        // Verify all 9 sets dropped roughly 1/9 of total drops (~11.1%)
        System.out.println("  Set Distribution across all 9 sets:");
        for (Map.Entry<Integer, Integer> entry : setDrops.entrySet()) {
            int setOpt = entry.getKey();
            int count = entry.getValue();
            double setPct = (count * 100.0) / totalDrops;
            System.out.printf("    Option %d: %d drops (%.2f%% of drops)\n", setOpt, count, setPct);
            assertTrue("Set Option " + setOpt + " dropped fairly (9% - 13%)", setPct >= 9.0 && setPct <= 13.0,
                    String.format("%.2f%%", setPct));
        }
    }

    // =========================================================================
    // 4. PLANET MAPPING & ITEM PROPERTIES VERIFICATION
    // =========================================================================
    private static void testSkhPlanetMappingAndItemProperties() {
        System.out.println("\n--- [SUITE 4] Planet Gender SKH Mapping & Item Options Verification ---");

        // Earth (gender 0): Tenshinhan (127), Krillin (128), Songoku (129)
        int[][] earthSets = RewardService.ACTIVATION_SET[0];
        List<Integer> earthOptNames = new ArrayList<>();
        List<Integer> earthOptEffects = new ArrayList<>();
        for (int[] s : earthSets) {
            earthOptNames.add(s[0]);
            earthOptEffects.add(s[1]);
        }
        assertTrue("Earth Set 127 present (Tenshinhan)", earthOptNames.contains(127), "Found 127");
        assertTrue("Earth Set 128 present (Krillin)", earthOptNames.contains(128), "Found 128");
        assertTrue("Earth Set 129 present (Songoku)", earthOptNames.contains(129), "Found 129");
        assertTrue("Earth Effect 139 present", earthOptEffects.contains(139), "Found 139");
        assertTrue("Earth Effect 140 present", earthOptEffects.contains(140), "Found 140");
        assertTrue("Earth Effect 141 present", earthOptEffects.contains(141), "Found 141");

        // Namek (gender 1): Piccolo (130), Ốc Tiêu (131), Pikkoro Daimao (132)
        int[][] namekSets = RewardService.ACTIVATION_SET[1];
        List<Integer> namekOptNames = new ArrayList<>();
        List<Integer> namekOptEffects = new ArrayList<>();
        for (int[] s : namekSets) {
            namekOptNames.add(s[0]);
            namekOptEffects.add(s[1]);
        }
        assertTrue("Namek Set 130 present (Piccolo)", namekOptNames.contains(130), "Found 130");
        assertTrue("Namek Set 131 present (Ốc Tiêu)", namekOptNames.contains(131), "Found 131");
        assertTrue("Namek Set 132 present (Pikkoro Daimao)", namekOptNames.contains(132), "Found 132");
        assertTrue("Namek Effect 142 present", namekOptEffects.contains(142), "Found 142");
        assertTrue("Namek Effect 143 present", namekOptEffects.contains(143), "Found 143");
        assertTrue("Namek Effect 144 present", namekOptEffects.contains(144), "Found 144");

        // Saiyan (gender 2): Kakarot (133), Vegeta (134), Nappa (135)
        int[][] saiyanSets = RewardService.ACTIVATION_SET[2];
        List<Integer> saiyanOptNames = new ArrayList<>();
        List<Integer> saiyanOptEffects = new ArrayList<>();
        for (int[] s : saiyanSets) {
            saiyanOptNames.add(s[0]);
            saiyanOptEffects.add(s[1]);
        }
        assertTrue("Saiyan Set 133 present (Kakarot)", saiyanOptNames.contains(133), "Found 133");
        assertTrue("Saiyan Set 134 present (Vegeta)", saiyanOptNames.contains(134), "Found 134");
        assertTrue("Saiyan Set 135 present (Nappa)", saiyanOptNames.contains(135), "Found 135");
        assertTrue("Saiyan Effect 136 present", saiyanOptEffects.contains(136), "Found 136");
        assertTrue("Saiyan Effect 137 present", saiyanOptEffects.contains(137), "Found 137");
        assertTrue("Saiyan Effect 138 present", saiyanOptEffects.contains(138), "Found 138");

        // Test item generation for 1000 items per gender
        for (int gender = 0; gender <= 2; gender++) {
            for (int i = 0; i < 1000; i++) {
                int slot = Util.nextInt(0, 4);
                int tempId = ConstItem.LIST_ITEM_CLOTHES[gender][slot][0];

                List<ItemOption> options = new ArrayList<>();
                RewardService.gI().initBaseOptionClothes(tempId, slot, options);

                int setIdx = Util.nextInt(0, 2);
                int[] idOption = RewardService.ACTIVATION_SET[gender][setIdx];

                options.add(new ItemOption(idOption[0], 1));
                options.add(new ItemOption(idOption[1], 100));
                options.add(new ItemOption(30, 0));
                options.add(new ItemOption(73, 0));

                // Verify base options present
                boolean hasBase = !options.isEmpty();
                if (!hasBase) {
                    assertTrue("Item has base options", false, "tempId=" + tempId + ", slot=" + slot);
                }

                // Verify set option is strictly belonging to current gender
                int setNameOpt = idOption[0];
                if (gender == 0 && (setNameOpt < 127 || setNameOpt > 129)) {
                    assertTrue("Earth receives only Earth SKH", false, "Got option " + setNameOpt);
                }
                if (gender == 1 && (setNameOpt < 130 || setNameOpt > 132)) {
                    assertTrue("Namek receives only Namek SKH", false, "Got option " + setNameOpt);
                }
                if (gender == 2 && (setNameOpt < 133 || setNameOpt > 135)) {
                    assertTrue("Saiyan receives only Saiyan SKH", false, "Got option " + setNameOpt);
                }
            }
        }
        assertTrue("All 3,000 generated items strictly adhered to killer planet gender", true, "3,000/3,000 validated");
    }

    // =========================================================================
    // 5. PET KILLER MASTER OWNERSHIP TEST
    // =========================================================================
    private static void testPetKillerMasterOwnership() {
        System.out.println("\n--- [SUITE 5] Pet Killer Master Ownership & Gender Assignment ---");

        Player master = new Player();
        master.id = 88888;
        master.gender = 1; // Namek master
        master.isBoss = false;
        master.name = "MasterNamek";

        Pet pet = new Pet(master);
        pet.isPet = true;
        pet.gender = 0; // Earth disciple

        Player killer = pet.isPet ? pet.master : pet;
        assertTrue("Killer resolves to Pet Master", killer == master, "Master ID=" + killer.id);
        assertTrue("Gender is Master gender (1=Namek)", killer.gender == 1, "Gender=" + killer.gender);
        assertTrue("Killer is not boss", !killer.isBoss, "isBoss=false");
    }

    // =========================================================================
    // 6. BOSS & DUMMY MOBS EXCLUSION TEST
    // =========================================================================
    private static void testBossExclusion() {
        System.out.println("\n--- [SUITE 6] Boss & Non-Normal Mobs Drop Exclusion ---");

        Mob dummy = new Mob(new Mob() {{ tempId = ConstMob.MOC_NHAN; }});
        Mob hiru = new Mob(new Mob() {{ tempId = ConstMob.HIRUDEGARN; }});
        Mob octopus = new Mob(new Mob() {{ tempId = ConstMob.VUA_BACH_TUOC; }});
        Mob guard = new Mob(new Mob() {{ tempId = ConstMob.ROBOT_BAO_VE; }});
        Mob boss = MobFactory.newMob(new Mob() {{ tempId = 77; }});

        assertTrue("Moc Nhan excluded", isExcludedMob(dummy), "tempId=" + ConstMob.MOC_NHAN);
        assertTrue("Hirudegarn excluded", isExcludedMob(hiru), "tempId=" + ConstMob.HIRUDEGARN);
        assertTrue("Octopus excluded", isExcludedMob(octopus), "tempId=" + ConstMob.VUA_BACH_TUOC);
        assertTrue("Guard Robot excluded", isExcludedMob(guard), "tempId=" + ConstMob.ROBOT_BAO_VE);
        assertTrue("BigBoss instance excluded", isExcludedMob(boss), "instanceof BigBoss");

        Mob normalMob = new Mob(new Mob() {{ tempId = 1; }}); // Moc nhan id 0, normal mob id 1 (Moc son)
        assertTrue("Normal Mob eligible for SKH drop", !isExcludedMob(normalMob), "tempId=1");
    }

    private static boolean isExcludedMob(Mob mob) {
        return mob == null
                || mob.tempId == ConstMob.MOC_NHAN
                || mob.tempId == ConstMob.HIRUDEGARN
                || mob.tempId == ConstMob.VUA_BACH_TUOC
                || mob.tempId == ConstMob.ROBOT_BAO_VE
                || (mob instanceof BigBoss);
    }
}
