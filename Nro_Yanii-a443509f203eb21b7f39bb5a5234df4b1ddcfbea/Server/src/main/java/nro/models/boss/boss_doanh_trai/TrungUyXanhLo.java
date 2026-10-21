package nro.models.boss.boss_doanh_trai;

import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.map.ItemMap;
import nro.services.func.ChangeMapService;
import nro.models.map.phoban.DoanhTrai;
import nro.models.player.Player;
import nro.services.Service;
import nro.utils.Util;

/**
 *
 * Arriety
 *
 */
public class TrungUyXanhLo extends BossDoanhTrai {

    public TrungUyXanhLo(DoanhTrai doanhTrai) {
        super(BossFactory.TRUNG_UY_XANH_LO, BossData.TRUNG_UY_XANH_LO, doanhTrai);
        //super.isBoss = true;
    }

    @Override
    public void rewards(Player pl) {
        int x = this.location.x;
        int y = this.zone.map.yPhysicInTop(x, this.location.y - 24);
        /// Rơi nro
        if (Util.isTrue(100, 100)) {
            int[] vatpham = {16, 17, 18};
            int vatphamChon = vatpham[Util.nextInt(0, vatpham.length - 1)];
            ItemMap itemMap = new ItemMap(this.zone, vatphamChon, 1, x, y, pl.id);
            Service.getInstance().dropItemMap(this.zone, itemMap);
        }
        /// Rơi capsule Pt
        if (Util.isTrue(50, 100)) {
            int soluong = Util.nextInt(1, 3);
            int vatpham = 1237;
            ItemMap itemMap = new ItemMap(this.zone, vatpham, soluong, x, y, pl.id);
            Service.getInstance().dropItemMap(this.zone, itemMap);
        }
    }

    @Override
    public void joinMap() {
        try {
            this.zone = this.doanhTrai.getMapById(mapJoin[Util.nextInt(0, mapJoin.length - 1)]);
            ChangeMapService.gI().changeMap(this, this.zone, 1065, this.zone.map.yPhysicInTop(1065, 0));
        } catch (Exception e) {

        }
    }

}
