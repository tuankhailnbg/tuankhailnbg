package nro.models.player;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class Gift {

    private Player player;

    public Gift(Player player) {
        this.player = player;
    }

    public boolean goldTanThu;
    public boolean gemTanThu;

    public void dispose() {
        this.player = null;
    }

}
