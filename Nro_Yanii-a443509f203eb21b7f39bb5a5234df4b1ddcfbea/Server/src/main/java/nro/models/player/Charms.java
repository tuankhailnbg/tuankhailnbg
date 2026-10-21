package nro.models.player;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class Charms {

    private Player player;

    public Charms(Player player) {
        this.player = player;
    }

    public long tdTriTue;
    public long tdManhMe;
    public long tdDaTrau;
    public long tdOaiHung;
    public long tdBatTu;
    public long tdDeoDai;
    public long tdThuHut;
    public long tdDeTu;
    public long tdDeTuMabu;
    public long tdTriTue3;
    public long tdTriTue4;

    public long lastTimeSubMinTriTueX4;

    public void addTimeCharms(int itemId, int min) {
        switch (itemId) {
            case 213:
                if (tdTriTue < System.currentTimeMillis()) {
                    tdTriTue = System.currentTimeMillis();
                }
                tdTriTue += min * 60 * 1000L;
                break;
            case 214:
                if (tdManhMe < System.currentTimeMillis()) {
                    tdManhMe = System.currentTimeMillis();
                }
                tdManhMe += min * 60 * 1000L;
                break;
            case 215:
                if (tdDaTrau < System.currentTimeMillis()) {
                    tdDaTrau = System.currentTimeMillis();
                }
                tdDaTrau += min * 60 * 1000L;
                break;
            case 216:
                if (tdOaiHung < System.currentTimeMillis()) {
                    tdOaiHung = System.currentTimeMillis();
                }
                tdOaiHung += min * 60 * 1000L;
                break;
            case 217:
                if (tdBatTu < System.currentTimeMillis()) {
                    tdBatTu = System.currentTimeMillis();
                }
                tdBatTu += min * 60 * 1000L;
                break;
            case 218:
                if (tdDeoDai < System.currentTimeMillis()) {
                    tdDeoDai = System.currentTimeMillis();
                }
                tdDeoDai += min * 60 * 1000L;
                break;
            case 219:
                if (tdThuHut < System.currentTimeMillis()) {
                    tdThuHut = System.currentTimeMillis();
                }
                tdThuHut += min * 60 * 1000L;
                break;
            case 522:
                if (tdDeTu < System.currentTimeMillis()) {
                    tdDeTu = System.currentTimeMillis();
                }
                tdDeTu += min * 60 * 1000L;
                break;
            case 671:
                if (tdTriTue3 < System.currentTimeMillis()) {
                    tdTriTue3 = System.currentTimeMillis();
                }
                tdTriTue3 += min * 60 * 1000L;
                break;
            case 672:
                if (tdTriTue4 < System.currentTimeMillis()) {
                    tdTriTue4 = System.currentTimeMillis();
                }
                tdTriTue4 += min * 60 * 1000L;
                break;
            case 2025:
                if (tdDeTuMabu < System.currentTimeMillis()) {
                    tdDeTuMabu = System.currentTimeMillis();
                }
                tdDeTuMabu += min * 60 * 1000L;
                break;
        }
    }

    public void dispose() {
        this.player = null;
    }
}
