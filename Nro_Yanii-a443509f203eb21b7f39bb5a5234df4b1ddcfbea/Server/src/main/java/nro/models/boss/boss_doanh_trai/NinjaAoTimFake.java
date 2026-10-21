package nro.models.boss.boss_doanh_trai;

import nro.consts.ConstPlayer;
import nro.consts.ConstRatio;
import nro.models.boss.BossData;
import nro.models.map.ItemMap;
import nro.models.map.phoban.DoanhTrai;
import nro.models.player.Player;
import nro.services.Service;
import nro.utils.Util;
import nro.services.SkillService;

/**
 *
 * @author 💖 Nro Yanii 💖
 *
 *
 */
public class NinjaAoTimFake extends NinjaAoTim {

    public NinjaAoTimFake(int id, DoanhTrai doanhTrai) {
        super(id, BossData.NINJA_AO_TIM_FAKE, doanhTrai);
        this.typePk = ConstPlayer.PK_ALL;
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
    public void attack() {
        try {
            if (!useSpecialSkill()) {
                if (Util.isTrue(30, ConstRatio.PER100)) {
                    this.talk();
                }
                Player pl = getPlayerAttack();
                this.playerSkill.skillSelect = this.getSkillAttack();
                if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                    if (Util.isTrue(50, ConstRatio.PER100)) {
                        goToXY(pl.location.x + Util.nextInt(-20, 20), Util.nextInt(pl.location.y - 80,
                                this.zone.map.yPhysicInTop(pl.location.x, 0)), false);
                    }
                    SkillService.gI().useSkill(this, pl, null, null);
                    checkPlayerDie(pl);
                } else {
                    goToPlayer(pl, false);
                }
            }
        } catch (Exception ex) {
            // ex.printStackTrace();
        }
    }

   

}
