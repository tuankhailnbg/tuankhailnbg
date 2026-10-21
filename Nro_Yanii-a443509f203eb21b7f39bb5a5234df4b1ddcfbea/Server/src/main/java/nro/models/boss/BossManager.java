package nro.models.boss;

import nro.utils.Log;
import java.util.ArrayList;
import java.util.List;
import nro.models.player.Player;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class BossManager {

    public static final List<Boss> BOSSES_IN_GAME;
    private static BossManager intance;

    static {
        BOSSES_IN_GAME = new ArrayList<>();
    }
    public int bossId = -1;

    public void updateAllBoss() {
        for (int i = BOSSES_IN_GAME.size() - 1; i >= 0; i--) {
            Boss boss = BOSSES_IN_GAME.get(i);
            if (boss != null) {
                try {
                    boss.update();
                } catch (Exception e) {
                    System.out.println("Lỗi khi update boss: \n" + e);
                }
            }
        }
    }

    private BossManager() {

    }

    public static BossManager gI() {
        if (intance == null) {
            intance = new BossManager();
        }
        return intance;
    }

    public Boss getBossTau77ByPlayer(Player player) {
        for (int i = BOSSES_IN_GAME.size() - 1; i >= 0; i--) {
            if (BOSSES_IN_GAME.get(i).id == (-251003 - player.id - 2000)) {
                return BOSSES_IN_GAME.get(i);
            }
        }
        return null;
    }

    public Boss getBossById(int bossId) {
        for (int i = BOSSES_IN_GAME.size() - 1; i >= 0; i--) {
            if (BOSSES_IN_GAME.get(i).id == bossId) {
                return BOSSES_IN_GAME.get(i);
            }
        }
        return null;
    }

    public void addBoss(Boss boss) {
        boolean have = false;
        for (Boss b : BOSSES_IN_GAME) {
            if (boss.equals(b)) {
                have = true;
                break;
            }
        }
        if (!have) {
            BOSSES_IN_GAME.add(boss);

        }
    }

    public void removeBoss(Boss boss) {
        BOSSES_IN_GAME.remove(boss);
        boss.dispose();
    }
}
