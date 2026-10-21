package nro.models.boss.td;

import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.boss.FutureBoss;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.services.Service;
import nro.services.func.ChangeMapService;
import nro.utils.Util;

public class Thodaica extends FutureBoss {

    public Thodaica() {
        super(BossFactory.THODAICA, BossData.THODAICA);
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
        if (Util.isTrue(1, 1)) {
            int numberOfGoldBars = Util.nextInt(10, 15);
            for (int i = 0; i < numberOfGoldBars; i++) {
                int goldItemId = 462;
                int xOffset = Util.nextInt(5, 8);
                int xPosition = pl.location.x + (i * xOffset);
                ItemMap itemMap = new ItemMap(
                        this.zone,
                        goldItemId,
                        1,
                        xPosition,
                        this.zone.map.yPhysicInTop(pl.location.x, pl.location.y - 24),
                        -1
                );
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
        ChangeMapService.gI().spaceShipArrive(this, (byte) 2, ChangeMapService.TENNIS_SPACE_SHIP);
        super.leaveMap();
    }
}
