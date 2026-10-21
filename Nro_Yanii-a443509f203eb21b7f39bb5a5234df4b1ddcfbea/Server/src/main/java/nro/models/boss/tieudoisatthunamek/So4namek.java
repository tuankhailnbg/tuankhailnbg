package nro.models.boss.tieudoisatthunamek;

import nro.models.boss.*;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.services.Service;
import nro.utils.Util;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class So4namek extends FutureBoss {

    public So4namek() {
        super(BossFactory.SO4NAMEK, BossData.SO4NAMEK);
    }

    @Override
    public int injured(Player plAtt, int damage, boolean piercing, boolean isMobAttack) {
        if (plAtt == null) {
            return 0;
        }
        if (!this.isDie()) {
            damage = 1;
            this.nPoint.subHP(damage);
            if (isDie()) {
                rewards(plAtt);
                notifyPlayeKill(plAtt);
                die();
            }
            return damage;
        } else {
            return 0;
        }
    }

    @Override
    public void rewards(Player pl) {
        // Cộng điểm vào SQL
        // TopBoss.update(pl);
        /// Rơi vật phẩm
        if (Util.isTrue(50, 100)) {
            ItemMap itemMap = new ItemMap(this.zone, 861, Util.nextInt(1000, 10000),
                    pl.location.x, this.zone.map.yPhysicInTop(pl.location.x, pl.location.y - 24), pl.id);
            Service.getInstance().dropItemMap(this.zone, itemMap);
        } else {
            ItemMap itemMap = new ItemMap(this.zone, Util.nextInt(1066, 1070), Util.nextInt(10, 20),
                    pl.location.x, this.zone.map.yPhysicInTop(pl.location.x, pl.location.y - 24), pl.id);
            Service.getInstance().dropItemMap(this.zone, itemMap);
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
        this.textTalkBefore = new String[] { "" };
        this.textTalkMidle = new String[] { "Oải rồi hả?", "Ê cố lên nhóc",
                "Chán", "Đại ca Fide có nhầm không nhỉ" };
    }

    @Override
    public void leaveMap() {
        BossManager.gI().getBossById(BossFactory.SO3NAMEK).changeToAttack();
        super.leaveMap();
        BossManager.gI().removeBoss(this);
    }
}
