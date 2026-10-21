package nro.models.boss.td;

import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.boss.BossManager;
import nro.models.boss.FutureBoss;
import nro.models.boss.TopBoss;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.services.Service;
import nro.utils.Util;

public class Kogu extends FutureBoss {

    public Kogu() {
        super(BossFactory.KOGU, BossData.KOGU);
        super.isBoss = true;
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
        TopBoss.updatediemnoel(pl);
        if (Util.isTrue(10, 100)) {
            int soluong = Util.nextInt(1, 3);
            for (int i = 0; i < soluong; i++) {
                int vatpham = 649;
                int randomvitrix = Util.nextInt(5, 8);
                int xPosition = pl.location.x + randomvitrix;
                int yPosition = this.zone.map.yPhysicInTop(pl.location.x, pl.location.y - 24);
                ItemMap itemMap = new ItemMap(this.zone, vatpham, 1, xPosition, yPosition, pl.id);

                Service.getInstance().dropItemMap(this.zone, itemMap);
            }
        }
         if (Util.isTrue(1, 40)) {
            int soluong = 1;
            for (int i = 0; i < soluong; i++) {
                int vatpham = 926;
                int randomvitrix = Util.nextInt(5, 8);
                int xPosition = pl.location.x + randomvitrix;
                int yPosition = this.zone.map.yPhysicInTop(pl.location.x, pl.location.y - 24);
                ItemMap itemMap = new ItemMap(this.zone, vatpham, 1, xPosition, yPosition, pl.id);

                Service.getInstance().dropItemMap(this.zone, itemMap);
            }
        }
          if (Util.isTrue(1, 40)) {
            int soluong = 1;
            for (int i = 0; i < soluong; i++) {
                int vatpham = 925;
                int randomvitrix = Util.nextInt(5, 8);
                int xPosition = pl.location.x + randomvitrix;
                int yPosition = this.zone.map.yPhysicInTop(pl.location.x, pl.location.y - 24);
                ItemMap itemMap = new ItemMap(this.zone, vatpham, 1, xPosition, yPosition, pl.id);

                Service.getInstance().dropItemMap(this.zone, itemMap);
            }
        }
    }

    @Override
    public void checkPlayerDie(Player pl) {

    }

    @Override
    public void initTalk() {
        this.textTalkAfter = new String[]{"Các ngươi chờ đấy, ta sẽ quay lại sau"};
    }
    
    @Override
    public void leaveMap() {
        super.leaveMap();
        BossManager.gI().removeBoss(this);
    }
}
