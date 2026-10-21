package nro.models.intrinsic;

import nro.models.player.Player;
import nro.services.IntrinsicService;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class IntrinsicPlayer {

    private Player player;

    public byte countOpen;

    public Intrinsic intrinsic;

    public IntrinsicPlayer(Player player) {
        this.player = player;
        this.intrinsic = IntrinsicService.gI().getIntrinsicById(0);
    }

    public void dispose() {
        this.player = null;
        this.intrinsic = null;
    }
}
