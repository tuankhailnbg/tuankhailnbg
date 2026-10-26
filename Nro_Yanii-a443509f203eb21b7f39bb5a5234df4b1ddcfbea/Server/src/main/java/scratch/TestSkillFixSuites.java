package scratch;

import nro.consts.ConstPlayer;
import nro.models.player.Player;
import nro.models.skill.NClass;
import nro.models.skill.PlayerSkill;
import nro.models.skill.Skill;
import nro.models.skill.SkillNotFocus;
import nro.models.skill.SkillTemplate;
import nro.server.Manager;
import nro.server.io.Message;
import nro.utils.SkillUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

public class TestSkillFixSuites {

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

    private static void setupMockNClassData() {
        Manager.NCLASS.clear();

        // 0. Trái Đất
        NClass nClassTD = new NClass();
        nClassTD.classId = ConstPlayer.TRAI_DAT;
        nClassTD.name = "Trái Đất";
        addTemplateToClass(nClassTD, Skill.DRAGON, "Đấm Dragon", 7, 100);
        addTemplateToClass(nClassTD, Skill.KAMEJOKO, "Kamejoko", 7, 200);
        addTemplateToClass(nClassTD, Skill.THAI_DUONG_HA_SAN, "Thái Dương Hạ San", 7, 300);
        addTemplateToClass(nClassTD, Skill.KAIOKEN, "Kaio-ken", 7, 400);
        addTemplateToClass(nClassTD, Skill.QUA_CAU_KENH_KHI, "Quả Cầu Kênh Khi", 7, 500);
        addTemplateToClass(nClassTD, Skill.DICH_CHUYEN_TUC_THOI, "Dịch Chuyển Tức Thời", 7, 600);
        addTemplateToClass(nClassTD, Skill.THOI_MIEN, "Thôi Miên", 7, 700);
        addTemplateToClass(nClassTD, Skill.SUPER_KAME, "Super Kamejoko", 7, 800);
        Manager.NCLASS.add(nClassTD);

        // 1. Namếc
        NClass nClassNM = new NClass();
        nClassNM.classId = ConstPlayer.NAMEC;
        nClassNM.name = "Namếc";
        addTemplateToClass(nClassNM, Skill.DEMON, "Đấm Demon", 7, 1100);
        addTemplateToClass(nClassNM, Skill.MASENKO, "Masenko", 7, 1200);
        addTemplateToClass(nClassNM, Skill.TRI_THUONG, "Trị Thương", 7, 1300);
        addTemplateToClass(nClassNM, Skill.MAKANKOSAPPO, "Makankosappo", 7, 1400);
        addTemplateToClass(nClassNM, Skill.DE_TRUNG, "Đẻ Trứng", 7, 1500);
        addTemplateToClass(nClassNM, Skill.LIEN_HOAN, "Liên Hoàn", 7, 1600);
        addTemplateToClass(nClassNM, Skill.SOCOLA, "Biến Socola", 7, 1700);
        addTemplateToClass(nClassNM, Skill.MAFUBA, "Ma Fuba", 7, 1800);
        Manager.NCLASS.add(nClassNM);

        // 2. Xayda
        NClass nClassXD = new NClass();
        nClassXD.classId = ConstPlayer.XAYDA;
        nClassXD.name = "Xayda";
        addTemplateToClass(nClassXD, Skill.GALICK, "Đấm Galick", 7, 2100);
        addTemplateToClass(nClassXD, Skill.ANTOMIC, "Antomic", 7, 2200);
        addTemplateToClass(nClassXD, Skill.TAI_TAO_NANG_LUONG, "Tái Tạo Năng Lượng", 7, 2300);
        addTemplateToClass(nClassXD, Skill.BIEN_KHI, "Biến Khỉ", 7, 2400);
        addTemplateToClass(nClassXD, Skill.TU_SAT, "Tự Sát", 7, 2500);
        addTemplateToClass(nClassXD, Skill.HUYT_SAO, "Huýt Sáo", 7, 2600);
        addTemplateToClass(nClassXD, Skill.TROI, "Trói", 7, 2700);
        addTemplateToClass(nClassXD, Skill.SUPER_ANTOMIC, "Super Antomic", 7, 2800);
        Manager.NCLASS.add(nClassXD);
    }

    private static void addTemplateToClass(NClass nClass, int tempId, String name, int maxLevel, int baseSkillId) {
        SkillTemplate template = new SkillTemplate();
        template.id = (byte) tempId;
        template.classId = (byte) nClass.classId;
        template.name = name;
        template.maxPoint = (byte) maxLevel;
        template.skillss = new ArrayList<>();

        for (int lv = 1; lv <= maxLevel; lv++) {
            Skill skill = new Skill();
            skill.template = template;
            skill.skillId = (short) (baseSkillId + lv);
            skill.point = (byte) lv;
            skill.damage = (short) (100 + lv * 20);
            skill.coolDown = 1000 + lv * 100;
            skill.manaUse = 10 * lv;
            skill.powRequire = 1000L * lv;
            template.skillss.add(skill);
        }
        nClass.skillTemplatess.add(template);
    }

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("     NRO - COMPREHENSIVE SKILL UTIL FIX & PLANET SKILL VALIDATION SUITE        ");
        System.out.println("================================================================================");

        try {
            setupMockNClassData();

            suite1_testDynamicTemplateLookup();
            suite2_testEarthSkills();
            suite3_testNamecSkills();
            suite4_testSaiyanSkills();
            suite5_testPlayerSkillLoadingAndClientPacket();
            suite6_testSkillLearningAndProgression();
            suite7_testPetSkillUpgrade();
            suite8_testRestartAndDynamicReload();
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
    // SUITE 1: Dynamic Template Lookup across all 3 classes (No static snapshots, no exceptions)
    // ========================================================================
    private static void suite1_testDynamicTemplateLookup() {
        System.out.println("\n--- SUITE 1: Dynamic Template Lookup (TD -> NM -> XD) ---");

        // Earthling templates
        SkillTemplate st0 = SkillUtil.findSkillTemplate(Skill.DRAGON);
        assertTrue("findSkillTemplate(0 - DRAGON)", st0 != null && st0.name.equals("Đấm Dragon") && st0.classId == 0,
                "Found in Earthling class");

        SkillTemplate st1 = SkillUtil.findSkillTemplate(Skill.KAMEJOKO);
        assertTrue("findSkillTemplate(1 - KAMEJOKO)", st1 != null && st1.name.equals("Kamejoko") && st1.classId == 0,
                "Found in Earthling class");

        // Namek templates (previously failed because TD lookup returned null without exception)
        SkillTemplate st2 = SkillUtil.findSkillTemplate(Skill.DEMON);
        assertTrue("findSkillTemplate(2 - DEMON)", st2 != null && st2.name.equals("Đấm Demon") && st2.classId == 1,
                "Found in Namek class");

        SkillTemplate st3 = SkillUtil.findSkillTemplate(Skill.MASENKO);
        assertTrue("findSkillTemplate(3 - MASENKO)", st3 != null && st3.name.equals("Masenko") && st3.classId == 1,
                "Found in Namek class");

        SkillTemplate st7 = SkillUtil.findSkillTemplate(Skill.TRI_THUONG);
        assertTrue("findSkillTemplate(7 - TRI_THUONG)", st7 != null && st7.name.equals("Trị Thương") && st7.classId == 1,
                "Found in Namek class");

        // Saiyan templates (previously failed because TD lookup returned null without exception)
        SkillTemplate st4 = SkillUtil.findSkillTemplate(Skill.GALICK);
        assertTrue("findSkillTemplate(4 - GALICK)", st4 != null && st4.name.equals("Đấm Galick") && st4.classId == 2,
                "Found in Saiyan class");

        SkillTemplate st5 = SkillUtil.findSkillTemplate(Skill.ANTOMIC);
        assertTrue("findSkillTemplate(5 - ANTOMIC)", st5 != null && st5.name.equals("Antomic") && st5.classId == 2,
                "Found in Saiyan class");

        SkillTemplate st13 = SkillUtil.findSkillTemplate(Skill.BIEN_KHI);
        assertTrue("findSkillTemplate(13 - BIEN_KHI)", st13 != null && st13.name.equals("Biến Khỉ") && st13.classId == 2,
                "Found in Saiyan class");

        // Non-existent template
        SkillTemplate st999 = SkillUtil.findSkillTemplate(999);
        assertTrue("findSkillTemplate(999 - Non-existent)", st999 == null, "Returns null gracefully");
    }

    // ========================================================================
    // SUITE 2: Trái Đất (Earthling) Skills
    // ========================================================================
    private static void suite2_testEarthSkills() {
        System.out.println("\n--- SUITE 2: Trái Đất Skills ---");

        // Test DRAGON (0) level 1 to 7
        for (int lv = 1; lv <= 7; lv++) {
            Skill sk = SkillUtil.createSkill(Skill.DRAGON, lv);
            assertTrue("TD DRAGON lv " + lv,
                    sk != null && sk.skillId != -1 && sk.point == lv && sk.template.id == Skill.DRAGON,
                    "skillId=" + (sk != null ? sk.skillId : "null") + ", pt=" + (sk != null ? sk.point : "null"));
        }

        // Test KAMEJOKO (1)
        Skill kame7 = SkillUtil.createSkill(Skill.KAMEJOKO, 7);
        assertTrue("TD KAMEJOKO lv 7",
                kame7 != null && kame7.skillId == 207 && kame7.point == 7 && kame7.damage > 0,
                "skillId=" + kame7.skillId + ", dmg=" + kame7.damage);

        // Test THAI_DUONG_HA_SAN (6)
        Skill tdhs7 = SkillUtil.createSkill(Skill.THAI_DUONG_HA_SAN, 7);
        assertTrue("TD THAI_DUONG lv 7",
                tdhs7 != null && tdhs7.skillId == 307 && tdhs7.point == 7,
                "skillId=" + tdhs7.skillId);

        // Test KAIOKEN (9)
        Skill kaioken = SkillUtil.createSkill(Skill.KAIOKEN, 7);
        assertTrue("TD KAIOKEN lv 7",
                kaioken != null && kaioken.skillId == 407 && kaioken.point == 7,
                "skillId=" + kaioken.skillId);

        // Test QUA_CAU_KENH_KHI (10)
        Skill qckk = SkillUtil.createSkill(Skill.QUA_CAU_KENH_KHI, 7);
        assertTrue("TD QCKK lv 7",
                qckk != null && qckk.skillId == 507 && qckk.point == 7,
                "skillId=" + qckk.skillId);

        // Test SUPER_KAME (24) -> SkillNotFocus
        Skill superKame = SkillUtil.createSkill(Skill.SUPER_KAME, 1);
        assertTrue("TD SUPER_KAME is SkillNotFocus",
                superKame instanceof SkillNotFocus && superKame.skillId == 801,
                "instanceof SkillNotFocus: " + (superKame instanceof SkillNotFocus));
    }

    // ========================================================================
    // SUITE 3: Namec Skills (Must NOT return skillId = -1)
    // ========================================================================
    private static void suite3_testNamecSkills() {
        System.out.println("\n--- SUITE 3: Namec Skills (Fix Validation) ---");

        // Test DEMON (2) level 1 to 7
        for (int lv = 1; lv <= 7; lv++) {
            Skill sk = SkillUtil.createSkill(Skill.DEMON, lv);
            assertTrue("Namec DEMON lv " + lv,
                    sk != null && sk.skillId != -1 && sk.point == lv && sk.template.id == Skill.DEMON
                            && sk.template.name.equals("Đấm Demon"),
                    "skillId=" + (sk != null ? sk.skillId : "null") + ", pt=" + (sk != null ? sk.point : "null"));
        }

        // Test MASENKO (3)
        Skill masenko7 = SkillUtil.createSkill(Skill.MASENKO, 7);
        assertTrue("Namec MASENKO lv 7",
                masenko7 != null && masenko7.skillId == 1207 && masenko7.point == 7 && masenko7.damage > 0,
                "skillId=" + masenko7.skillId + ", dmg=" + masenko7.damage);

        // Test TRI_THUONG (7)
        Skill triThuong = SkillUtil.createSkill(Skill.TRI_THUONG, 7);
        assertTrue("Namec TRI_THUONG lv 7",
                triThuong != null && triThuong.skillId == 1307 && triThuong.point == 7,
                "skillId=" + triThuong.skillId);

        // Test MAKANKOSAPPO (11)
        Skill makankosappo = SkillUtil.createSkill(Skill.MAKANKOSAPPO, 7);
        assertTrue("Namec MAKANKOSAPPO lv 7",
                makankosappo != null && makankosappo.skillId == 1407 && makankosappo.point == 7,
                "skillId=" + makankosappo.skillId);

        // Test DE_TRUNG (12)
        Skill deTrung = SkillUtil.createSkill(Skill.DE_TRUNG, 7);
        assertTrue("Namec DE_TRUNG lv 7",
                deTrung != null && deTrung.skillId == 1507 && deTrung.point == 7,
                "skillId=" + deTrung.skillId);

        // Test LIEN_HOAN (17)
        Skill lienHoan = SkillUtil.createSkill(Skill.LIEN_HOAN, 7);
        assertTrue("Namec LIEN_HOAN lv 7",
                lienHoan != null && lienHoan.skillId == 1607 && lienHoan.point == 7,
                "skillId=" + lienHoan.skillId);

        // Test SOCOLA (18)
        Skill socola = SkillUtil.createSkill(Skill.SOCOLA, 7);
        assertTrue("Namec SOCOLA lv 7",
                socola != null && socola.skillId == 1707 && socola.point == 7,
                "skillId=" + socola.skillId);

        // Test MAFUBA (26) -> SkillNotFocus
        Skill mafuba = SkillUtil.createSkill(Skill.MAFUBA, 1);
        assertTrue("Namec MAFUBA is SkillNotFocus",
                mafuba instanceof SkillNotFocus && mafuba.skillId == 1801,
                "instanceof SkillNotFocus: " + (mafuba instanceof SkillNotFocus));
    }

    // ========================================================================
    // SUITE 4: Saiyan Skills (Must NOT return skillId = -1)
    // ========================================================================
    private static void suite4_testSaiyanSkills() {
        System.out.println("\n--- SUITE 4: Xayda Skills (Fix Validation) ---");

        // Test GALICK (4) level 1 to 7
        for (int lv = 1; lv <= 7; lv++) {
            Skill sk = SkillUtil.createSkill(Skill.GALICK, lv);
            assertTrue("Xayda GALICK lv " + lv,
                    sk != null && sk.skillId != -1 && sk.point == lv && sk.template.id == Skill.GALICK
                            && sk.template.name.equals("Đấm Galick"),
                    "skillId=" + (sk != null ? sk.skillId : "null") + ", pt=" + (sk != null ? sk.point : "null"));
        }

        // Test ANTOMIC (5)
        Skill antomic7 = SkillUtil.createSkill(Skill.ANTOMIC, 7);
        assertTrue("Xayda ANTOMIC lv 7",
                antomic7 != null && antomic7.skillId == 2207 && antomic7.point == 7 && antomic7.damage > 0,
                "skillId=" + antomic7.skillId + ", dmg=" + antomic7.damage);

        // Test TAI_TAO_NANG_LUONG (8)
        Skill taiTao = SkillUtil.createSkill(Skill.TAI_TAO_NANG_LUONG, 7);
        assertTrue("Xayda TAI_TAO_NANG_LUONG lv 7",
                taiTao != null && taiTao.skillId == 2307 && taiTao.point == 7,
                "skillId=" + taiTao.skillId);

        // Test BIEN_KHI (13)
        Skill bienKhi = SkillUtil.createSkill(Skill.BIEN_KHI, 7);
        assertTrue("Xayda BIEN_KHI lv 7",
                bienKhi != null && bienKhi.skillId == 2407 && bienKhi.point == 7,
                "skillId=" + bienKhi.skillId);

        // Test TU_SAT (14)
        Skill tuSat = SkillUtil.createSkill(Skill.TU_SAT, 7);
        assertTrue("Xayda TU_SAT lv 7",
                tuSat != null && tuSat.skillId == 2507 && tuSat.point == 7,
                "skillId=" + tuSat.skillId);

        // Test HUYT_SAO (21)
        Skill huytSao = SkillUtil.createSkill(Skill.HUYT_SAO, 7);
        assertTrue("Xayda HUYT_SAO lv 7",
                huytSao != null && huytSao.skillId == 2607 && huytSao.point == 7,
                "skillId=" + huytSao.skillId);

        // Test TROI (23)
        Skill troi = SkillUtil.createSkill(Skill.TROI, 7);
        assertTrue("Xayda TROI lv 7",
                troi != null && troi.skillId == 2707 && troi.point == 7,
                "skillId=" + troi.skillId);

        // Test SUPER_ANTOMIC (25) -> SkillNotFocus
        Skill superAntomic = SkillUtil.createSkill(Skill.SUPER_ANTOMIC, 1);
        assertTrue("Xayda SUPER_ANTOMIC is SkillNotFocus",
                superAntomic instanceof SkillNotFocus && superAntomic.skillId == 2801,
                "instanceof SkillNotFocus: " + (superAntomic instanceof SkillNotFocus));
    }

    // ========================================================================
    // SUITE 5: Player Skill Loading (GodGK simulation) & Client Packet Delivery
    // ========================================================================
    private static void suite5_testPlayerSkillLoadingAndClientPacket() {
        System.out.println("\n--- SUITE 5: Player Skill Loading & Client Packet Delivery ---");

        // Simulate DB loading for Trái Đất player
        Player plTD = new Player();
        plTD.gender = ConstPlayer.TRAI_DAT;
        plTD.playerSkill = new PlayerSkill(plTD);
        // DB row: DRAGON lv 7, KAMEJOKO lv 7, THAI_DUONG lv 1, KAIOKEN lv 0 (not learned)
        loadSimulatedSkills(plTD, new int[][]{
                {Skill.DRAGON, 0, 7},
                {Skill.KAMEJOKO, 0, 7},
                {Skill.THAI_DUONG_HA_SAN, 0, 1},
                {Skill.KAIOKEN, 0, 0}
        });

        assertTrue("TD getSizeSkill()", plTD.playerSkill.getSizeSkill() == 3,
                "Expected 3 learned skills, got: " + plTD.playerSkill.getSizeSkill());
        List<Short> packetTD = simulateServicePlayerPacket(plTD);
        assertTrue("TD Packet skill count", packetTD.size() == 3,
                "Sent " + packetTD.size() + " skills: " + packetTD);

        // Simulate DB loading for Namec player (THIS WAS 0 BEFORE FIX)
        Player plNM = new Player();
        plNM.gender = ConstPlayer.NAMEC;
        plNM.playerSkill = new PlayerSkill(plNM);
        // DB row: DEMON lv 7, MASENKO lv 7, TRI_THUONG lv 7, MAKANKOSAPPO lv 1, DE_TRUNG lv 0
        loadSimulatedSkills(plNM, new int[][]{
                {Skill.DEMON, 0, 7},
                {Skill.MASENKO, 0, 7},
                {Skill.TRI_THUONG, 0, 7},
                {Skill.MAKANKOSAPPO, 0, 1},
                {Skill.DE_TRUNG, 0, 0}
        });

        assertTrue("Namec getSizeSkill() NOT ZERO", plNM.playerSkill.getSizeSkill() == 4,
                "Expected 4 learned skills, got: " + plNM.playerSkill.getSizeSkill());
        List<Short> packetNM = simulateServicePlayerPacket(plNM);
        assertTrue("Namec Packet contains all 4 learned skills", packetNM.size() == 4,
                "Sent " + packetNM.size() + " skills: " + packetNM);
        assertTrue("Namec DEMON skillId != -1", packetNM.get(0) == 1107, "DEMON skillId = " + packetNM.get(0));

        // Simulate DB loading for Saiyan player (THIS WAS 0 BEFORE FIX)
        Player plXD = new Player();
        plXD.gender = ConstPlayer.XAYDA;
        plXD.playerSkill = new PlayerSkill(plXD);
        // DB row: GALICK lv 7, ANTOMIC lv 7, TAI_TAO lv 7, BIEN_KHI lv 7, TU_SAT lv 0
        loadSimulatedSkills(plXD, new int[][]{
                {Skill.GALICK, 0, 7},
                {Skill.ANTOMIC, 0, 7},
                {Skill.TAI_TAO_NANG_LUONG, 0, 7},
                {Skill.BIEN_KHI, 0, 7},
                {Skill.TU_SAT, 0, 0}
        });

        assertTrue("Xayda getSizeSkill() NOT ZERO", plXD.playerSkill.getSizeSkill() == 4,
                "Expected 4 learned skills, got: " + plXD.playerSkill.getSizeSkill());
        List<Short> packetXD = simulateServicePlayerPacket(plXD);
        assertTrue("Xayda Packet contains all 4 learned skills", packetXD.size() == 4,
                "Sent " + packetXD.size() + " skills: " + packetXD);
        assertTrue("Xayda GALICK skillId != -1", packetXD.get(0) == 2107, "GALICK skillId = " + packetXD.get(0));
    }

    private static void loadSimulatedSkills(Player player, int[][] dbSkills) {
        for (int[] row : dbSkills) {
            int tempId = row[0];
            int point = row[2];
            Skill skill;
            if (point != 0) {
                skill = SkillUtil.createSkill(tempId, point);
            } else {
                skill = SkillUtil.createSkillLevel0(tempId);
            }
            player.playerSkill.skills.add(skill);
        }
    }

    private static List<Short> simulateServicePlayerPacket(Player pl) {
        List<Short> sentSkills = new ArrayList<>();
        byte size = pl.playerSkill.getSizeSkill();
        for (Skill skill : pl.playerSkill.skills) {
            if (skill.skillId != -1) {
                sentSkills.add(skill.skillId);
            }
        }
        return sentSkills;
    }

    // ========================================================================
    // SUITE 6: Skill Learning & Max-level Progression (UseItem simulation)
    // ========================================================================
    private static void suite6_testSkillLearningAndProgression() {
        System.out.println("\n--- SUITE 6: Skill Learning & Max-Level Progression ---");

        Player player = new Player();
        player.gender = ConstPlayer.NAMEC;
        player.playerSkill = new PlayerSkill(player);

        // DEMON at level 7
        Skill demonMax = SkillUtil.createSkill(Skill.DEMON, 7);
        player.playerSkill.skills.add(demonMax);

        // MASENKO at level 0 (unlearned)
        Skill masenkoUnlearned = SkillUtil.createSkillLevel0(Skill.MASENKO);
        player.playerSkill.skills.add(masenkoUnlearned);

        // TRI_THUONG at level 1
        Skill triThuongLv1 = SkillUtil.createSkill(Skill.TRI_THUONG, 1);
        player.playerSkill.skills.add(triThuongLv1);

        // 1. Check max level skill: curSkill.point == 7
        Skill curDemon = SkillUtil.getSkillbyId(player, Skill.DEMON);
        assertTrue("Max level check on DEMON", curDemon != null && curDemon.point == 7 && curDemon.skillId != -1,
                "curSkill.point = " + (curDemon != null ? curDemon.point : 0));

        // 2. Learn level 1 for unlearned skill (MASENKO): point 0 -> 1
        Skill curMasenko = SkillUtil.getSkillbyId(player, Skill.MASENKO);
        assertTrue("MASENKO is unlearned before", curMasenko != null && curMasenko.point == 0 && curMasenko.skillId == -1,
                "curSkill.skillId = " + (curMasenko != null ? curMasenko.skillId : 0));

        Skill newMasenko = SkillUtil.createSkill(Skill.MASENKO, 1);
        SkillUtil.setSkill(player, newMasenko);
        Skill afterLearnMasenko = SkillUtil.getSkillbyId(player, Skill.MASENKO);
        assertTrue("MASENKO learned to level 1",
                afterLearnMasenko != null && afterLearnMasenko.point == 1 && afterLearnMasenko.skillId == 1201,
                "new skillId = " + (afterLearnMasenko != null ? afterLearnMasenko.skillId : 0));

        // 3. Upgrade level 1 to level 2 (TRI_THUONG): point 1 -> 2
        Skill curTriThuong = SkillUtil.getSkillbyId(player, Skill.TRI_THUONG);
        assertTrue("TRI_THUONG is level 1 before", curTriThuong != null && curTriThuong.point == 1,
                "curSkill.point = " + (curTriThuong != null ? curTriThuong.point : 0));

        Skill newTriThuong = SkillUtil.createSkill(Skill.TRI_THUONG, 2);
        SkillUtil.setSkill(player, newTriThuong);
        Skill afterUpgradeTriThuong = SkillUtil.getSkillbyId(player, Skill.TRI_THUONG);
        assertTrue("TRI_THUONG upgraded to level 2",
                afterUpgradeTriThuong != null && afterUpgradeTriThuong.point == 2 && afterUpgradeTriThuong.skillId == 1302,
                "new skillId = " + (afterUpgradeTriThuong != null ? afterUpgradeTriThuong.skillId : 0));
    }

    // ========================================================================
    // SUITE 7: Pet Skill Upgrade (upSkillPet)
    // ========================================================================
    private static void suite7_testPetSkillUpgrade() {
        System.out.println("\n--- SUITE 7: Pet Skill Upgrade ---");

        List<Skill> petSkills = new ArrayList<>();
        // Skill 0: DEMON (Namec) lv 1
        petSkills.add(SkillUtil.createSkill(Skill.DEMON, 1));
        // Skill 1: MASENKO (Namec) lv 1
        petSkills.add(SkillUtil.createSkill(Skill.MASENKO, 1));
        // Skill 2: GALICK (Xayda) lv 1
        petSkills.add(SkillUtil.createSkill(Skill.GALICK, 1));
        // Skill 3: DRAGON (TD) lv 1
        petSkills.add(SkillUtil.createSkill(Skill.DRAGON, 1));

        // Upgrade DEMON to lv 2
        boolean ok0 = SkillUtil.upSkillPet(petSkills, 0);
        assertTrue("upSkillPet Namec DEMON", ok0 && petSkills.get(0).point == 2 && petSkills.get(0).skillId == 1102,
                "lv=" + petSkills.get(0).point + ", id=" + petSkills.get(0).skillId);

        // Upgrade MASENKO to lv 2 (index 1 sets cooldown = 1000)
        boolean ok1 = SkillUtil.upSkillPet(petSkills, 1);
        assertTrue("upSkillPet Namec MASENKO (idx 1 cd=1000)",
                ok1 && petSkills.get(1).point == 2 && petSkills.get(1).coolDown == 1000,
                "lv=" + petSkills.get(1).point + ", cd=" + petSkills.get(1).coolDown);

        // Upgrade GALICK to lv 2
        boolean ok2 = SkillUtil.upSkillPet(petSkills, 2);
        assertTrue("upSkillPet Xayda GALICK", ok2 && petSkills.get(2).point == 2 && petSkills.get(2).skillId == 2102,
                "lv=" + petSkills.get(2).point + ", id=" + petSkills.get(2).skillId);

        // Upgrade DRAGON to lv 2
        boolean ok3 = SkillUtil.upSkillPet(petSkills, 3);
        assertTrue("upSkillPet TD DRAGON", ok3 && petSkills.get(3).point == 2 && petSkills.get(3).skillId == 102,
                "lv=" + petSkills.get(3).point + ", id=" + petSkills.get(3).skillId);
    }

    // ========================================================================
    // SUITE 8: Server Restart & Dynamic Reload Simulation (Zero static caching)
    // ========================================================================
    private static void suite8_testRestartAndDynamicReload() {
        System.out.println("\n--- SUITE 8: Server Restart & Dynamic Reload Simulation ---");

        // 1. Simulate server shutdown: Manager.NCLASS is cleared
        Manager.NCLASS.clear();
        Skill fallbackSkill = SkillUtil.createSkill(Skill.DEMON, 7);
        assertTrue("During shutdown (empty NCLASS), returns safe fallback without crash",
                fallbackSkill != null && fallbackSkill.skillId == -1 && fallbackSkill.point == 7,
                "fallback skillId = " + (fallbackSkill != null ? fallbackSkill.skillId : 0));

        // 2. Simulate server restart: Manager.NCLASS is re-populated from DB
        setupMockNClassData();

        // 3. Login Namec player after restart with existing DB data
        Player reloadedNM = new Player();
        reloadedNM.gender = ConstPlayer.NAMEC;
        reloadedNM.playerSkill = new PlayerSkill(reloadedNM);
        loadSimulatedSkills(reloadedNM, new int[][]{
                {Skill.DEMON, 0, 7},
                {Skill.MASENKO, 0, 7},
                {Skill.TRI_THUONG, 0, 7}
        });

        assertTrue("After restart: Namec player retains 3 skills", reloadedNM.playerSkill.getSizeSkill() == 3,
                "getSizeSkill() = " + reloadedNM.playerSkill.getSizeSkill());
        assertTrue("After restart: Namec DEMON skillId != -1",
                reloadedNM.playerSkill.skills.get(0).skillId == 1107,
                "DEMON skillId = " + reloadedNM.playerSkill.skills.get(0).skillId);
        assertTrue("After restart: Namec DEMON point == 7",
                reloadedNM.playerSkill.skills.get(0).point == 7,
                "DEMON point = " + reloadedNM.playerSkill.skills.get(0).point);

        // 4. Login Saiyan player after restart with existing DB data
        Player reloadedXD = new Player();
        reloadedXD.gender = ConstPlayer.XAYDA;
        reloadedXD.playerSkill = new PlayerSkill(reloadedXD);
        loadSimulatedSkills(reloadedXD, new int[][]{
                {Skill.GALICK, 0, 7},
                {Skill.ANTOMIC, 0, 7},
                {Skill.BIEN_KHI, 0, 7}
        });

        assertTrue("After restart: Saiyan player retains 3 skills", reloadedXD.playerSkill.getSizeSkill() == 3,
                "getSizeSkill() = " + reloadedXD.playerSkill.getSizeSkill());
        assertTrue("After restart: Saiyan GALICK skillId != -1",
                reloadedXD.playerSkill.skills.get(0).skillId == 2107,
                "GALICK skillId = " + reloadedXD.playerSkill.skills.get(0).skillId);
        assertTrue("After restart: Saiyan GALICK point == 7",
                reloadedXD.playerSkill.skills.get(0).point == 7,
                "GALICK point = " + reloadedXD.playerSkill.skills.get(0).point);

        // 5. Login Earthling player after restart with existing DB data
        Player reloadedTD = new Player();
        reloadedTD.gender = ConstPlayer.TRAI_DAT;
        reloadedTD.playerSkill = new PlayerSkill(reloadedTD);
        loadSimulatedSkills(reloadedTD, new int[][]{
                {Skill.DRAGON, 0, 7},
                {Skill.KAMEJOKO, 0, 7}
        });

        assertTrue("After restart: Earthling player retains 2 skills (no regression)",
                reloadedTD.playerSkill.getSizeSkill() == 2,
                "getSizeSkill() = " + reloadedTD.playerSkill.getSizeSkill());
        assertTrue("After restart: Earthling DRAGON skillId != -1",
                reloadedTD.playerSkill.skills.get(0).skillId == 107,
                "DRAGON skillId = " + reloadedTD.playerSkill.skills.get(0).skillId);
    }
}
