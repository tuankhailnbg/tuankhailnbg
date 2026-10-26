package nro.models.boss.huydiet;

import nro.models.boss.*;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.services.Service;
import nro.services.func.ChangeMapService;
import nro.utils.Util;

/**
 *
 * @author 💖 Nro Yanii 💖
 *
 *
 */
public class Thiensu extends FutureBoss {

    public Thiensu() {
        super(BossFactory.THIENSU, BossData.THIENSU);
    }

    @Override
    public void joinMap() {
        super.joinMap();
        BossFactory.createBoss(BossFactory.HUYDIET).zone = this.zone;
    }

   @Override
    public void rewards(Player pl) {
        if (Util.isTrue(50, 100)) {
            int soluong = Util.nextInt(1, 5);
            int[] vatpham = new int[]{1311};
            int khoangCachX = Util.nextInt(5, 8);
            for (int i = 0; i < soluong; i++) {
                int idVang = vatpham[i % vatpham.length];
                int viTriX = pl.location.x + (i * khoangCachX);
                ItemMap itemMap = new ItemMap(
                        this.zone,
                        idVang,
                        1,
                        viTriX,
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
        textTalkMidle = new String[]{"Ta chính là đệ nhất vũ trụ cao thủ"};
        textTalkAfter = new String[]{"Ác quỷ biến hình aaa..."};
    }

    public void leaveMap() {
        ChangeMapService.gI().spaceShipArrive(this, (byte) 2, ChangeMapService.TENNIS_SPACE_SHIP);
        super.leaveMap();
    }
}
