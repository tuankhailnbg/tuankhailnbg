package nro.models.boss.robotsatthu;

import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.boss.BossManager;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.services.Service;
import nro.services.TaskService;
import nro.utils.Util;

/**
 *
 * @author ❤Girlkun75❤
 * @copyright ❤Nro Yanii❤
 */
public class Poc extends Boss {

    public Poc() {
        super(BossFactory.POC, BossData.POC);
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

    @Override
    public void leaveMap() {
        super.leaveMap();
        BossManager.gI().removeBoss(this);
        BossManager.gI().getBossById(BossFactory.PIC).changeToAttack();
    }

}
