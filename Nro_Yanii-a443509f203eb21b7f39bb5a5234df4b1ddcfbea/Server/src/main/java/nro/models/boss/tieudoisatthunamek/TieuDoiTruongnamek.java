package nro.models.boss.tieudoisatthunamek;

import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.boss.FutureBoss;
import nro.models.boss.TopBoss;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.server.ServerNotify;
import nro.services.Service;
import nro.services.func.ChangeMapService;
import nro.utils.Util;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class TieuDoiTruongnamek extends FutureBoss {

    public TieuDoiTruongnamek() {
        super(BossFactory.TIEU_DOI_TRUONGNAMEK, BossData.TIEU_DOI_TRUONGNAMEK);
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
        this.textTalkMidle = new String[] { "Oải rồi hả?", "Ê cố lên nhóc",
                "Chán", "Đại ca Fide có nhầm không nhỉ" };

    }

    @Override
    public void leaveMap() {
        super.leaveMap();
        this.changeToIdle();
    }

    @Override
    public void joinMap() {
        if (this.zone == null) {
            this.zone = getMapCanJoin(mapJoin[Util.nextInt(0, mapJoin.length - 1)]);
        }
        if (this.zone != null) {
            BossFactory.createBoss(BossFactory.SO4NAMEK).zone = this.zone;
            BossFactory.createBoss(BossFactory.SO3NAMEK).zone = this.zone;
            BossFactory.createBoss(BossFactory.SO2NAMEK).zone = this.zone;
            BossFactory.createBoss(BossFactory.SO1NAMEK).zone = this.zone;
            ChangeMapService.gI().changeMapBySpaceShip(this, this.zone, ChangeMapService.TENNIS_SPACE_SHIP);
            ServerNotify.gI().notify("Boss " + this.name + " vừa xuất hiện tại " + this.zone.map.mapName);
        }
    }

}
