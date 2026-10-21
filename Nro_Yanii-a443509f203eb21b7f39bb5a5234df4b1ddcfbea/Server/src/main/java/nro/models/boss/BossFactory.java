package nro.models.boss;

import nro.models.boss.td.Thodaica;
import nro.models.boss.td.Zangya;
import nro.models.boss.td.SuperBojack;
import nro.models.boss.td.Kogu;
import nro.models.boss.td.Bujin;
import nro.models.boss.td.Bojack;
import nro.models.boss.td.Bido;
import nro.consts.ConstMap;
import nro.models.boss.bill.*;
import nro.models.boss.bosstuonglai.*;
import nro.models.boss.broly.*;
import nro.models.boss.cell.*;
import nro.models.boss.chill.*;
import nro.models.boss.cold.*;
import nro.models.boss.fide.*;
import nro.models.boss.mabu_war.*;
import nro.models.boss.nappa.*;
import nro.models.boss.robotsatthu.*;
import nro.models.boss.tieudoisatthu.*;
import nro.models.boss.NguHanhSon.*;
import nro.models.boss.NgucTu.*;
import nro.models.boss.huydiet.Huydiet;
import nro.models.boss.huydiet.Thiensu;
import nro.models.boss.td.Berushalloween;
import nro.models.boss.td.Brolykhoinguyen;
import nro.models.boss.td.Fu;
import nro.models.boss.td.Onggianoel;
import nro.models.boss.td.TuanLocEvent;
import nro.models.boss.tieudoisatthunamek.So1namek;
import nro.models.boss.tieudoisatthunamek.So2namek;
import nro.models.boss.tieudoisatthunamek.So3namek;
import nro.models.boss.tieudoisatthunamek.So4namek;
import nro.models.boss.tieudoisatthunamek.TieuDoiTruongnamek;
//import nro.models.boss.tap_luyen.CallBossTapLuyen;
import nro.models.boss.traidat.*;
import nro.models.map.Map;
import nro.models.map.Zone;
import nro.models.map.mabu.MabuWar;
import nro.services.MapService;
import org.apache.log4j.Logger;

/**
 * @author 💖 Nro Yanii 💖
 * @copyright 💖 NROLOVE 💖
 */
public class BossFactory {

    // id boss
    public static final int ONG_GIA_NOEL = -1007227;
    public static final int TUAN_LOC_EVENT = -1007226;
    public static final int BROLYKHOINGUYEN = -1007225;
    public static final int FU = -1007224;
    public static final int BERUSHALLOWEEN = -1007223;
    public static final int THIENSU = -1007220;
    public static final int HUYDIET = -1007221;

    public static final int KOGU = -100718;
    public static final int ZANGYA = -100717;
    public static final int BIDO = -100716;
    public static final int BUJIN = -100715;
    public static final int BOJACK = -100714;
    public static final int SUPERBOJACK = -100713;
    public static final int THODAICA = -100712;
    public static final int BROLY = -10001;
    public static final int SUPER_BROLY = -10002;
    public static final int TRUNG_UY_TRANG = -10003;
    public static final int TRUNG_UY_XANH_LO = -10004;
    public static final int TRUNG_UD_THEP = -10005;
    public static final int NINJA_AO_TIM = -10006;
    public static final int NINJA_AO_TIM_FAKE_1 = -10007;
    public static final int NINJA_AO_TIM_FAKE_2 = -10008;
    public static final int NINJA_AO_TIM_FAKE_3 = -10009;
    public static final int NINJA_AO_TIM_FAKE_4 = -100010;
    public static final int NINJA_AO_TIM_FAKE_5 = -100011;
    public static final int NINJA_AO_TIM_FAKE_6 = -100012;
    public static final int ROBOT_VE_SI_1 = -100013;
    public static final int ROBOT_VE_SI_2 = -100014;
    public static final int ROBOT_VE_SI_3 = -100015;
    public static final int ROBOT_VE_SI_4 = -100016;
    public static final int XEN_BO_HUNG_1 = -100017;
    public static final int XEN_BO_HUNG_2 = -100018;
    public static final int XEN_BO_HUNG_HOAN_THIEN = -100019;
    public static final int XEN_BO_HUNG = -100020;
    public static final int XEN_CON = -122;
    public static final int XEN_CON_1 = -123;
    public static final int XEN_CON_2 = -124;
    public static final int XEN_CON_3 = -125;
    public static final int XEN_CON_4 = -126;
    public static final int XEN_CON_5 = -127;
    public static final int XEN_CON_6 = -128;
    public static final int SIEU_BO_HUNG = -100022;
    public static final int KUKU = -100023;
    public static final int MAP_DAU_DINH = -100024;
    public static final int RAMBO = -100025;
    public static final int COOLER = -100026;
    public static final int COOLER2 = -100027;
    public static final int SO4 = -100028;
    public static final int SO3 = -100029;
    public static final int SO2 = -100030;
    public static final int SO1 = -100031;
    public static final int TIEU_DOI_TRUONG = -100032;
    public static final int SO4NAMEK = -1000728;
    public static final int SO3NAMEK = -1000729;
    public static final int SO2NAMEK = -1000730;
    public static final int SO1NAMEK = -1000731;
    public static final int TIEU_DOI_TRUONGNAMEK = -1000732;
    public static final int FIDE_DAI_CA_1 = -100033;
    public static final int FIDE_DAI_CA_2 = -100034;
    public static final int FIDE_DAI_CA_3 = -100035;
    public static final int ANDROID_19 = -100036;
    public static final int ANDROID_20 = -100037;
    public static final int ANDROID_13 = -100038;
    public static final int ANDROID_14 = -100039;
    public static final int ANDROID_15 = -100040;
    public static final int PIC = -100041;
    public static final int POC = -100042;
    public static final int KINGKONG = -100043;
    public static final int SUPER_BROLY_RED = -100044;
    public static final int LUFFY = -100045;
    public static final int ZORO = -100046;
    public static final int SANJI = -100047;
    public static final int USOPP = -100048;
    public static final int FRANKY = -100049;
    public static final int BROOK = -100050;
    public static final int NAMI = -100051;
    public static final int CHOPPER = -100052;
    public static final int ROBIN = -100053;
    public static final int WHIS = -100054;
    public static final int BILL = -100055;
    public static final int CHILL = -100056;
    public static final int CHILL2 = -100057;
    public static final int BULMA = -100058;
    public static final int POCTHO = -100059;
    public static final int CHICHITHO = -100060;
    public static final int BLACKGOKU = -100061;
    public static final int SUPERBLACKGOKU = -100062;
    public static final int SANTA_CLAUS = -100063;
    public static final int MABU_MAP = -100064;
    public static final int SUPER_BU = -100065;
    public static final int BU_TENK = -100066;
    public static final int DRABULA_TANG1 = -100067;
    public static final int BUIBUI_TANG2 = -100068;
    public static final int BUIBUI_TANG3 = -100069;
    public static final int YACON_TANG4 = -100070;
    public static final int DRABULA_TANG5 = -100071;
    public static final int GOKU_TANG5 = -100072;
    public static final int CADIC_TANG5 = -100073;
    public static final int DRABULA_TANG6 = -100074;
    public static final int XEN_MAX = -100075;
    public static final int HOA_HONG = -100076;
    public static final int SOI_HEC_QUYN = -100077;
    public static final int O_DO = -100078;
    public static final int XINBATO = -100079;
    public static final int CHA_PA = -100080;
    public static final int PON_PUT = -100081;
    public static final int CHAN_XU = -100082;
    public static final int TAU_PAY_PAY = -100083;
    public static final int YAMCHA = -100084;
    public static final int JACKY_CHUN = -100085;
    public static final int THIEN_XIN_HANG = -100086;
    public static final int LIU_LIU = -100087;
    public static final int THIEN_XIN_HANG_CLONE = -100088;
    public static final int THIEN_XIN_HANG_CLONE1 = -100089;
    public static final int THIEN_XIN_HANG_CLONE2 = -100090;
    public static final int THIEN_XIN_HANG_CLONE3 = -100091;
    public static final int QILIN = -100092;
    public static final int NGO_KHONG = -100093;
    public static final int BAT_GIOI = -100094;
    public static final int FIDEGOLD = -100095;
    public static final int CUMBER = -100096;
    public static final int CUMBER2 = -100097;

    public static final int THAN_MEO = -100098;

    public static final int DRACULA = -100099;
    public static final int VO_HINH = -1000100;
    public static final int BONG_BANG = -1000101;
    public static final int VUA_QUY_SATAN = -1000102;
    public static final int THO_DAU_BAC = -1000103;

    public static final int DR_LYCHEE = -1000104;

    public static final int HATCHIJACK = -1000105;

    public static final byte TAP_SU_1 = -104;
    public static final byte TAP_SU_2 = -105;
    public static final byte TAP_SU_3 = -106;
    public static final byte TAP_SU_4 = -107;
    public static final byte TAP_SU_5 = -108;

    public static final byte TAN_BINH_1 = -109;
    public static final byte TAN_BINH_2 = -110;
    public static final byte TAN_BINH_3 = -111;
    public static final byte TAN_BINH_4 = -112;
    public static final byte TAN_BINH_5 = -113;
    public static final byte TAN_BINH_6 = -114;

    public static final byte DOI_TRUONG_1 = -115;

    public static final byte CHIEN_BINH_1 = -98;
    public static final byte CHIEN_BINH_2 = -99;
    public static final byte CHIEN_BINH_3 = -100;
    public static final byte CHIEN_BINH_4 = -101;
    public static final byte CHIEN_BINH_5 = -102;
    public static final byte CHIEN_BINH_6 = -103;

    private static final Logger logger = Logger.getLogger(BossFactory.class);

    public static final int[] MAP_APPEARED_QILIN = { ConstMap.VACH_NUI_ARU_42, ConstMap.VACH_NUI_MOORI_43,
            ConstMap.VACH_NUI_KAKAROT,
            ConstMap.LANG_ARU, ConstMap.LANG_MORI, ConstMap.LANG_KAKAROT, ConstMap.DOI_HOA_CUC, ConstMap.DOI_NAM_TIM,
            ConstMap.DOI_HOANG,
            ConstMap.TRAM_TAU_VU_TRU, ConstMap.TRAM_TAU_VU_TRU_25, ConstMap.TRAM_TAU_VU_TRU_26, ConstMap.LANG_PLANT,
            ConstMap.RUNG_NGUYEN_SINH,
            ConstMap.RUNG_CO, ConstMap.RUNG_THONG_XAYDA, ConstMap.RUNG_DA, ConstMap.THUNG_LUNG_DEN, ConstMap.BO_VUC_DEN,
            ConstMap.THANH_PHO_VEGETA,
            ConstMap.THUNG_LUNG_TRE, ConstMap.RUNG_NAM, ConstMap.RUNG_BAMBOO, ConstMap.RUNG_XUONG,
            ConstMap.RUNG_DUONG_XI, ConstMap.NAM_KAME,
            ConstMap.DAO_BULONG, ConstMap.DONG_KARIN, ConstMap.THI_TRAN_MOORI, ConstMap.THUNG_LUNG_MAIMA,
            ConstMap.NUI_HOA_TIM, ConstMap.NUI_HOA_VANG,
            ConstMap.NAM_GURU, ConstMap.DONG_NAM_GURU, ConstMap.THUNG_LUNG_NAMEC
    };

    private BossFactory() {

    }

    public static boolean isYar(byte id) {
        return (id == TAP_SU_1 || id == TAP_SU_2 || id == TAP_SU_3 || id == TAP_SU_4 || id == TAP_SU_5
                || id == TAN_BINH_1 || id == TAN_BINH_2
                || id == TAN_BINH_3 || id == TAN_BINH_4 || id == TAN_BINH_5 || id == TAN_BINH_6 || id == DOI_TRUONG_1
                || id == CHIEN_BINH_1 || id == CHIEN_BINH_2
                || id == CHIEN_BINH_3 || id == CHIEN_BINH_4 || id == CHIEN_BINH_5 || id == CHIEN_BINH_6);
    }

    public static void initBoss() {
        new Thread(() -> {
            try {
                // createBoss(TUAN_LOC_EVENT);
                // createBoss(ONG_GIA_NOEL);
                // createBoss(BROLYKHOINGUYEN);
                // createBoss(FU);
                createBoss(SIEU_BO_HUNG);
                // createBoss(BERUSHALLOWEEN);
                createBoss(THIENSU);
                // createBoss(SUPERBOJACK);
                // createBoss(THODAICA);
                createBoss(XEN_BO_HUNG_1);
                createBoss(XEN_BO_HUNG);
                createBoss(XEN_CON_1);
                createBoss(XEN_CON_2);
                createBoss(XEN_CON_3);
                createBoss(XEN_CON_4);
                createBoss(XEN_CON_5);
                createBoss(BLACKGOKU);
                createBoss(CHILL);
                createBoss(COOLER);
                createBoss(KUKU);
                createBoss(MAP_DAU_DINH);
                createBoss(RAMBO);
                createBoss(TIEU_DOI_TRUONG);
                createBoss(TIEU_DOI_TRUONGNAMEK);
                createBoss(FIDE_DAI_CA_1);
                createBoss(ANDROID_20);
                createBoss(ANDROID_13);
                createBoss(KINGKONG);
                createBoss(CUMBER);
                createBoss(SUPER_BROLY_RED);

                // for (int i = 0; i < 15; i++) {
                // createBoss(BROLY);
                // }
            } catch (Exception e) {
                logger.error("Err initboss", e);
            }
        }).start();
    }

    public static void initBossMabuWar() {
        new Thread(() -> {
            for (short mapid : BossData.DRABULA_TANG1.mapJoin) {
                Map map = MapService.gI().getMapById(mapid);
                for (Zone zone : map.zones) {
                    Boss boss = new Drabula_Tang1(mapid, zone.zoneId);
                    MabuWar.gI().bosses.add(boss);
                }
            }
            for (short mapid : BossData.DRABULA_TANG6.mapJoin) {
                Map map = MapService.gI().getMapById(mapid);
                for (Zone zone : map.zones) {
                    Boss boss = new Drabula_Tang6(mapid, zone.zoneId);
                    MabuWar.gI().bosses.add(boss);
                }
            }
            for (short mapid : BossData.GOKU_TANG5.mapJoin) {
                Map map = MapService.gI().getMapById(mapid);
                for (Zone zone : map.zones) {
                    Boss boss = new Goku_Tang5(mapid, zone.zoneId);
                    MabuWar.gI().bosses.add(boss);
                }
            }
            for (short mapid : BossData.CALICH_TANG5.mapJoin) {
                Map map = MapService.gI().getMapById(mapid);
                for (Zone zone : map.zones) {
                    Boss boss = new Calich_Tang5(mapid, zone.zoneId);
                    MabuWar.gI().bosses.add(boss);
                }
            }
            for (short mapid : BossData.BUIBUI_TANG2.mapJoin) {
                Map map = MapService.gI().getMapById(mapid);
                for (Zone zone : map.zones) {
                    Boss boss = new BuiBui_Tang2(mapid, zone.zoneId);
                    MabuWar.gI().bosses.add(boss);
                }
            }
            for (short mapid : BossData.BUIBUI_TANG3.mapJoin) {
                Map map = MapService.gI().getMapById(mapid);
                for (Zone zone : map.zones) {
                    Boss boss = new BuiBui_Tang3(mapid, zone.zoneId);
                    MabuWar.gI().bosses.add(boss);
                }
            }
            for (short mapid : BossData.YACON_TANG4.mapJoin) {
                Map map = MapService.gI().getMapById(mapid);
                for (Zone zone : map.zones) {
                    Boss boss = new Yacon_Tang4(mapid, zone.zoneId);
                    MabuWar.gI().bosses.add(boss);
                }
            }
        }).start();
    }

    public static Boss createBoss(int bossId) {
        Boss boss = null;
        switch (bossId) {
            case HUYDIET:
                boss = new Huydiet();
                break;
            case THIENSU:
                boss = new Thiensu();
                break;
            case BROLY:
                boss = new Broly();
                break;
            case XEN_BO_HUNG_1:
                boss = new XenBoHung1();
                break;
            case XEN_BO_HUNG_2:
                boss = new XenBoHung2();
                break;
            case XEN_BO_HUNG_HOAN_THIEN:
                boss = new XenBoHungHoanThien();
                break;
            case XEN_BO_HUNG:
                boss = new XenBoHung();
                break;
            case SIEU_BO_HUNG:
                boss = new SieuBoHung();
                break;
            case XEN_CON:
                boss = new XenCon();
                break;
            case XEN_CON_1:
                boss = new XenCon1();
                break;
            case XEN_CON_2:
                boss = new XenCon2();
                break;
            case XEN_CON_3:
                boss = new XenCon3();
                break;
            case XEN_CON_4:
                boss = new XenCon4();
                break;
            case XEN_CON_5:
                boss = new XenCon5();
                break;
            case XEN_CON_6:
                boss = new XenCon6();
                break;
            case KUKU:
                boss = new Kuku();
                break;
            case MAP_DAU_DINH:
                boss = new MapDauDinh();
                break;
            case RAMBO:
                boss = new Rambo();
                break;
            case COOLER:
                boss = new Cooler();
                break;
            case COOLER2:
                boss = new Cooler2();
                break;
            case SO4:
                boss = new So4();
                break;
            case SO3:
                boss = new So3();
                break;
            case SO2:
                boss = new So2();
                break;
            case SO1:
                boss = new So1();
                break;
            case TIEU_DOI_TRUONG:
                boss = new TieuDoiTruong();
                break;
            case SO4NAMEK:
                boss = new So4namek();
                break;
            case SO3NAMEK:
                boss = new So3namek();
                break;
            case SO2NAMEK:
                boss = new So2namek();
                break;
            case SO1NAMEK:
                boss = new So1namek();
                break;
            case TIEU_DOI_TRUONGNAMEK:
                boss = new TieuDoiTruongnamek();
                break;
            case FIDE_DAI_CA_1:
                boss = new FideDaiCa1();
                break;
            case FIDE_DAI_CA_2:
                boss = new FideDaiCa2();
                break;
            case FIDE_DAI_CA_3:
                boss = new FideDaiCa3();
                break;
            case ANDROID_19:
                boss = new Android19();
                break;
            case ANDROID_20:
                boss = new Android20();
                break;
            case ANDROID_13:
                boss = new Android13();
                break;
            case ANDROID_14:
                boss = new Android14();
                break;
            case ANDROID_15:
                boss = new Android15();
                break;
            case POC:
                boss = new Poc();
                break;
            case PIC:
                boss = new Pic();
                break;
            case KINGKONG:
                boss = new KingKong();
                break;
            case WHIS:
                boss = new Whis();
                break;
            case BILL:
                boss = new Bill();
                break;
            case CHILL:
                boss = new Chill();
                break;
            case CHILL2:
                boss = new Chill2();
                break;
            case BULMA:
                boss = new BULMA();
                break;
            case POCTHO:
                boss = new POCTHO();
                break;
            case CHICHITHO:
                boss = new CHICHITHO();
                break;
            case BLACKGOKU:
                boss = new Blackgoku();
                break;
            case SUPERBLACKGOKU:
                boss = new Superblackgoku();
                break;
            case MABU_MAP:
                boss = new Mabu_Tang6();
                break;
            case XEN_MAX:
                boss = new XenMax();
                break;
            case SUPER_BROLY_RED:
                boss = new SuperBroly();
                break;
            // case NGO_KHONG:
            // boss = new NgoKhong();
            // break;
            // case BAT_GIOI:
            // boss = new BatGioi();
            // break;
            // case FIDEGOLD:
            // boss = new FideGold();
            // break;
            // case CUMBER:
            // boss = new Cumber();
            // break;
            // case CUMBER2:
            // boss = new SuperCumber();
            // break;
        }
        return boss;
    }

}
