package nro.models.boss.robotsatthu;

import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.services.RewardService;
import nro.services.Service;
import nro.services.TaskService;
import nro.utils.Util;

/**
 *
 * @author ❤Girlkun75❤
 * @copyright ❤Nro Yanii❤
 */
public class Android13 extends Boss {

    public Android13() {
        super(BossFactory.ANDROID_13, BossData.ANDROID_13);
    }

    @Override
    public void joinMap() {
        super.joinMap();
        BossFactory.createBoss(BossFactory.ANDROID_14).zone = this.zone;
        BossFactory.createBoss(BossFactory.ANDROID_15).zone = this.zone;
    }

    @Override
    protected boolean useSpecialSkill() {
        return false;
    }

     @Override
    public void rewards(Player pl) {
        /// Rơi vật phẩm
        if (Util.isTrue(1, 50)) {
            int[] vatpham = {17, 18, 19, 20};
            int vatphamChon = vatpham[Util.nextInt(0, vatpham.length - 1)];
            int randomvitrix = Util.nextInt(5, 8);
            int xPosition = pl.location.x + randomvitrix;
            int yPosition = this.zone.map.yPhysicInTop(pl.location.x, pl.location.y - 24);
            ItemMap itemMap = new ItemMap(this.zone, vatphamChon, 1, xPosition, yPosition, -1);
            Service.getInstance().dropItemMap(this.zone, itemMap);
            /// Roi vật phẩm noel
        } else if (Util.isTrue(100, 100)) {
             
            /// Nv
            TaskService.gI().checkDoneTaskKillBoss(pl, this);
        }
    }

    @Override
    public void idle() {
    }

    @Override
    public void checkPlayerDie(Player pl) {
    }

    @Override
    public void initTalk() {
    }

}
