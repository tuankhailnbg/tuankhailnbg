package nro.models.player;

import java.util.ArrayList;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class ListFriendEnemy<T> extends ArrayList<T> {

    public final Player player;

    public ListFriendEnemy(Player player) {
        this.player = player;
    }

}
