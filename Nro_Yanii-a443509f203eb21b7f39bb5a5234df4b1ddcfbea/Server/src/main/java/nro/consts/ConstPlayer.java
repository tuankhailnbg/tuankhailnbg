package nro.consts;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class ConstPlayer {

        public static final byte[][] AURABIENHINH = {
                        { 7, 7, 13, 6, 31 },
                        { 7, 7, 13, 6, 31 },
                        { 7, 7, 13, 6, 31 }
        };
        public static final short[][] HEADBIENHINH = {
                        { 1400, 1401, 1402, 1403, 1404 }, // 5 head TD
                        { 1405, 1406, 1407, 1408, 1409 }, // 5 haed NM
                        { 1410, 1411, 1412, 1413, 1414 }, // 5 head XD
        };
        public static final short[] BODYBIENHINH = { 1394, 1396, 1398 }; // TD NM XD
        public static final short[] LEGBIENHINH = { 1395, 1397, 1399 }; // TD NM XD

        public static final int[] HEADMONKEY = { 192, 195, 196, 199, 197, 200, 198 };

        public static final byte TRAI_DAT = 0;
        public static final byte NAMEC = 1;
        public static final byte XAYDA = 2;

        // type pk
        public static final byte NON_PK = 0;
        public static final byte PK_PVP = 3;
        public static final byte PK_ALL = 5;

        // type fushion
        public static final byte NON_FUSION = 0;
        public static final byte LUONG_LONG_NHAT_THE = 4;
        public static final byte HOP_THE_PORATA = 6;
        public static final byte HOP_THE_PORATA2 = 8;
}
