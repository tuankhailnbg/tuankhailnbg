package nro.models.boss.td;

import nro.consts.ConstPlayer;
import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.boss.BossManager;
import nro.models.boss.FutureBoss;
import nro.models.boss.TopBoss;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.services.PlayerService;
import nro.services.Service;
import nro.services.func.ChangeMapService;
import nro.utils.Log;
import nro.utils.Util;

public class SuperBojack extends FutureBoss {

    public SuperBojack() {
        super(BossFactory.SUPERBOJACK, BossData.SUPERBOJACK);
        super.isBoss = true;
    }

    @Override
    public void changeToAttack() {
        Boss bojack = BossManager.gI().getBossById(BossFactory.BOJACK);
        Boss kogu = BossManager.gI().getBossById(BossFactory.KOGU);
        Boss bujin = BossManager.gI().getBossById(BossFactory.BUJIN);
        Boss zangya = BossManager.gI().getBossById(BossFactory.ZANGYA);
        Boss bido = BossManager.gI().getBossById(BossFactory.BIDO);
        if (bojack != null || kogu != null || bujin != null || zangya != null || bido != null) {
            return;
        }
        PlayerService.gI().changeAndSendTypePK(this, ConstPlayer.PK_ALL);
        changeStatus(ATTACK);
    }

    public void joinMap() {
        super.joinMap();
        BossFactory.createBoss(BossFactory.BIDO).zone = this.zone;
        BossFactory.createBoss(BossFactory.BOJACK).zone = this.zone;
        BossFactory.createBoss(BossFactory.BUJIN).zone = this.zone;
        BossFactory.createBoss(BossFactory.KOGU).zone = this.zone;
        BossFactory.createBoss(BossFactory.ZANGYA).zone = this.zone;
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
        textTalkMidle = new String[]{"Ta chính là đệ nhất vũ trụ cao thủ"};
        textTalkAfter = new String[]{"Ác quỷ biến hình aaa..."};
    }

    public void leaveMap() {
        ChangeMapService.gI().spaceShipArrive(this, (byte) 2, ChangeMapService.TENNIS_SPACE_SHIP);
        super.leaveMap();
    }
}
