package nro.models.boss.td;

import java.time.LocalDateTime;
import java.time.ZoneId;
import nro.consts.ConstPlayer;
import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.boss.FutureBoss;
import nro.models.item.ItemOption;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.server.ServerNotify;
import nro.services.MapService;
import nro.services.PlayerService;
import nro.services.Service;
import nro.services.func.ChangeMapService;
import nro.utils.Util;

public class Fu extends FutureBoss {

    public Fu() {
        super(BossFactory.FU, BossData.FU);
        super.isBoss = true;
        // MIEN TROI
        this.effectSkill.mienTroi = true;
    }

    @Override
    public void changeToAttack() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        if (now.getHour() == 23) {
            if (this.zone != null) {
                MapService.gI().exitMap(this);
            }
            this.zone = getMapCanJoin(mapJoin[Util.nextInt(0, mapJoin.length - 1)]);
            int x = Util.nextInt(50, this.zone.map.mapWidth - 50);
            ChangeMapService.gI().changeMap(this, this.zone, x, this.zone.map.yPhysicInTop(x, 0));
            ServerNotify.gI().notify("Boss " + this.name + " vừa xuất hiện tại " + this.zone.map.mapName + "");
            PlayerService.gI().changeAndSendTypePK(this, ConstPlayer.PK_ALL);
            changeStatus(ATTACK);
        }
    }

    @Override
    public int injured(Player plAtt, int damage, boolean piercing, boolean isMobAttack) {
        if (plAtt == null) {
            return 0;
        }
        if (!this.isDie()) {
            // Giới hạn damage nếu lớn hơn 200k
            if (damage > 200_000) {
                damage = 200_000;
            }
            // Áp dụng damage
            this.nPoint.subHP(damage);
            // Kiểm tra nếu chết
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
            int vatpham = 2129;
            int randomvitrix = Util.nextInt(5, 8);
            int xPosition = pl.location.x + randomvitrix;
            int yPosition = this.zone.map.yPhysicInTop(pl.location.x, pl.location.y - 24);
            ItemMap itemMap = new ItemMap(this.zone, vatpham, 1, xPosition, yPosition, pl.id);

            itemMap.options.add(new ItemOption(50, 35));
            itemMap.options.add(new ItemOption(77, 15));
            itemMap.options.add(new ItemOption(103, 35));
            itemMap.options.add(new ItemOption(5, 30));
            itemMap.options.add(new ItemOption(94, 30));
            if (Util.isTrue(1, 100)) {
            } else {
                itemMap.options.add(new ItemOption(93, Util.nextInt(1, 3)));
            }
            Service.getInstance().dropItemMap(this.zone, itemMap);
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
