package nro.models.boss.tieudoisatthu;

import nro.models.boss.*;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.services.Service;
import nro.services.TaskService;
import nro.utils.Util;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class So3 extends FutureBoss {

    public So3() {
        super(BossFactory.SO3, BossData.SO3);
    }

    @Override
    protected boolean useSpecialSkill() {
        return false;
    }

    @Override
    public void rewards(Player pl) {
        /// Rơi vật phẩm
        if (Util.isTrue(1, 100)) {
            int[] vatpham = { 18, 19, 20 };
            int vatphamChon = vatpham[Util.nextInt(0, vatpham.length - 1)];
            int randomvitrix = Util.nextInt(5, 8);
            int xPosition = pl.location.x + randomvitrix;
            int yPosition = this.zone.map.yPhysicInTop(pl.location.x, pl.location.y - 24);
            ItemMap itemMap = new ItemMap(this.zone, vatphamChon, 1, xPosition, yPosition, -1);
            Service.getInstance().dropItemMap(this.zone, itemMap);
            /// Roi vật phẩm noel
        }
        TaskService.gI().checkDoneTaskKillBoss(pl, this);
    }

    @Override
    public void idle() {

    }

    @Override
    public void checkPlayerDie(Player pl) {

    }

    @Override
    public void initTalk() {
        this.textTalkMidle = new String[] { "Oải rồi hả?", "Ê cố lên nhóc",
                "Chán", "Đại ca Fide có nhầm không nhỉ" };

    }

    @Override
    public void leaveMap() {
        BossManager.gI().getBossById(BossFactory.SO2).changeToAttack();
        BossManager.gI().getBossById(BossFactory.SO1).changeToAttack();
        super.leaveMap();
        BossManager.gI().removeBoss(this);
    }

}
