package nro.models.player;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class EffectFlagBag {

    private Player player;

    public EffectFlagBag(Player player) {
        this.player = player;
    }

    public boolean useVoOc;
    public boolean useCayKem;
    public boolean useCaHeo;
    public boolean useConDieu;
    public boolean useDieuRong;
    public boolean useMeoMun;
    public boolean useXienCa;
    public boolean usePhongHeo;
    public boolean useKiemz;
    public boolean useHoaHong;
    public boolean useHoaVang;
    public boolean useGayTre;

    public void reset() {
        this.useVoOc = false;
        this.useCayKem = false;
        this.useCaHeo = false;
        this.useConDieu = false;
        this.useDieuRong = false;
        this.useMeoMun = false;
        this.useXienCa = false;
        this.usePhongHeo = false;
        this.useKiemz = false;
        this.useHoaHong = false;
        this.useHoaVang = false;
        this.useGayTre = false;
    }

    public void dispose() {
        this.player = null;
    }
}
