package nro.models.boss.boss_doanh_trai;

import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.services.func.ChangeMapService;
import nro.models.map.phoban.DoanhTrai;
import nro.models.player.Player;
import nro.services.SkillService;
import nro.utils.SkillUtil;
import nro.utils.Util;

import java.util.List;
import nro.models.map.ItemMap;
import nro.services.Service;

/**
 *
 * @author 💖 Nro Yanii 💖
 *
 *
 */
public class TrungUyThep extends BossDoanhTrai {

    private boolean activeAttack;

    public TrungUyThep(DoanhTrai doanhTrai) {
        super(BossFactory.TRUNG_UD_THEP, BossData.TRUNG_UY_THEP, doanhTrai);
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
        /// Rơi BDKB
        if (Util.isTrue(100, 100)) {
            int vatpham = 611;
            ItemMap itemMap = new ItemMap(this.zone, vatpham, 1, x, y, pl.id);
            Service.getInstance().dropItemMap(this.zone, itemMap);
        }
    }

    @Override
    public void attack() {
        try {
            if (activeAttack) {
                Player pl = getPlayerAttack();
                this.playerSkill.skillSelect = this.getSkillAttack();
                if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                    SkillService.gI().useSkill(this, pl, null, null);
                    checkPlayerDie(pl);
                } else {
                    goToPlayer(pl, false);
                }
            } else {
                List<Player> notBosses = this.zone.getNotBosses();
                for (Player pl : notBosses) {
                    if (pl.location.x >= 650 && !pl.effectSkin.isVoHinh) {
                        this.activeAttack = true;
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            // ex.printStackTrace();
        }
    }

    @Override
    public void joinMap() {
        try {
            this.zone = this.doanhTrai.getMapById(mapJoin[Util.nextInt(0, mapJoin.length - 1)]);
            ChangeMapService.gI().changeMap(this, this.zone, 900, this.zone.map.yPhysicInTop(900, 100));
        } catch (Exception e) {

        }
    }

    @Override
    public int injured(Player plAtt, int damage, boolean piercing, boolean isMobAttack) {
        if (plAtt != null && !SkillUtil.isUseSkillDam(plAtt)) {
            return super.injured(plAtt, damage, piercing, isMobAttack);
        }
        damage = damage / 100;
        if (damage <= 0) {
            damage = 1;
        }
        return super.injured(plAtt, damage, piercing, isMobAttack);
    }

}
