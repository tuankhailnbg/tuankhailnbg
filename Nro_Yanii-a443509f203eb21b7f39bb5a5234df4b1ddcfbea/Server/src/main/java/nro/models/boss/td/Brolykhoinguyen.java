package nro.models.boss.td;

import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.boss.FutureBoss;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.services.RewardService;
import nro.services.Service;
import nro.services.func.ChangeMapService;
import nro.utils.Util;

public class Brolykhoinguyen extends FutureBoss {

    public Brolykhoinguyen() {
        super(BossFactory.BROLYKHOINGUYEN, BossData.BROLYKHOINGUYEN);
        super.isBoss = true;
    }

    @Override
    public int injured(Player plAtt, int damage, boolean piercing, boolean isMobAttack) {
        if (plAtt == null) {
            return 0;
        }
        if (!this.isDie()) {
          
            if (damage > 20_000_000) {
                damage = 20_000_000;
            }
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
        ItemMap itemMap = null;
        int x = this.location.x;
        int y = this.zone.map.yPhysicInTop(x, this.location.y - 24);
        if (Util.isTrue(1, 100)) {
            int[] set1 = {565, 560, 562, 564, 566, 561};
            itemMap = new ItemMap(this.zone, set1[Util.nextInt(0, set1.length - 1)], 1, x, y, pl.id);
            RewardService.gI().initBaseOptionClothes(itemMap.itemTemplate.id, itemMap.itemTemplate.type,
                    itemMap.options);
        } else if (Util.isTrue(1, 30)) {
            int[] set2 = {555, 556, 563, 557, 558, 559, 567};
            itemMap = new ItemMap(this.zone, set2[Util.nextInt(0, set2.length - 1)], 1, x, y, pl.id);
            RewardService.gI().initBaseOptionClothes(itemMap.itemTemplate.id, itemMap.itemTemplate.type,
                    itemMap.options);
        } else if (Util.isTrue(1, 50)) {
            itemMap = new ItemMap(this.zone, 400, 1, x, y, pl.id);
        } else if (Util.isTrue(1, 30)) {
            itemMap = new ItemMap(this.zone, 2144, 1, x, y, pl.id);
        } else if (Util.isTrue(1, 50)) {
            itemMap = new ItemMap(this.zone, 2084, 1, x, y, pl.id);
        } else if (Util.isTrue(1, 50)) {
            itemMap = new ItemMap(this.zone, 2085, 1, x, y, pl.id);
        } else if (Util.isTrue(1, 100)) {
            itemMap = new ItemMap(this.zone, 2086, 1, x, y, pl.id);
        }
        if (itemMap != null) {
            Service.getInstance().dropItemMap(zone, itemMap);
        }
    }

    @Override
    public void checkPlayerDie(Player pl) {

    }

    @Override
    public void initTalk() {
        this.textTalkAfter = new String[]{""};
    }

    @Override
    public void leaveMap() {
        ChangeMapService.gI().spaceShipArrive(this, (byte) 2, ChangeMapService.TENNIS_SPACE_SHIP);
        super.leaveMap();
    }
}
