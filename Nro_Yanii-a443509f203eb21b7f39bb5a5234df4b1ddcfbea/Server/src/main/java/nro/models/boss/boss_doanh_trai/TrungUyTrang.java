package nro.models.boss.boss_doanh_trai;

import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.services.func.ChangeMapService;
import nro.consts.ConstMob;
import nro.consts.ConstRatio;
import nro.models.mob.Mob;
import nro.models.map.phoban.DoanhTrai;
import nro.models.player.Player;
import nro.services.SkillService;
import nro.utils.Util;

import java.util.ArrayList;
import java.util.List;
import nro.models.map.ItemMap;
import nro.services.Service;

/**
 *
 * @author 💖 Nro Yanii 💖
 *
 *
 */
public class TrungUyTrang extends BossDoanhTrai {

    public TrungUyTrang(DoanhTrai doanhTrai) {
        super(BossFactory.TRUNG_UY_TRANG, BossData.TRUNG_UY_TRANG, doanhTrai);
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
    public Player getPlayerAttack() throws Exception {
        List<Player> list = new ArrayList<>();

        List<Player> notBosses = this.zone.getNotBosses();
        for (Player pl : notBosses) {
            if (!pl.isDie() && pl.location.x >= 755 && pl.location.x <= 1069 && !pl.effectSkin.isVoHinh) {
                list.add(pl);
            }
        }
        if (!list.isEmpty()) {
            return list.get(Util.nextInt(0, list.size() - 1));
        } else {
            throw new Exception();
        }
    }

    @Override
    public void attack() {
        try {
            Player pl = getPlayerAttack();
            this.playerSkill.skillSelect = this.getSkillAttack();
            if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                if (Util.isTrue(20, ConstRatio.PER100)) {
                    goToXY(pl.location.x + Util.nextInt(-20, 20), pl.location.y, false);
                }
                SkillService.gI().useSkill(this, pl, null, null);
                checkPlayerDie(pl);
            } else {
                goToPlayer(pl, false);
            }
        } catch (Exception ex) {
            // ex.printStackTrace();
        }
    }

    @Override
    public int injured(Player plAtt, int damage, boolean piercing, boolean isMobAttack) {
        if (!piercing) {
            boolean haveBulon = false;
            for (Mob mob : this.zone.mobs) {
                if (mob.tempId == ConstMob.BULON && !mob.isDie()) {
                    haveBulon = true;
                    break;
                }
            }
            if (haveBulon) {
                damage = 1;
            }
        }
        return super.injured(plAtt, damage, piercing, isMobAttack);
    }

    @Override
    public void joinMap() {
        try {
            this.zone = this.doanhTrai.getMapById(mapJoin[Util.nextInt(0, mapJoin.length - 1)]);
            int x = Util.nextInt(755, 1069);
            ChangeMapService.gI().changeMap(this, this.zone, x, this.zone.map.yPhysicInTop(x, 0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected boolean useSpecialSkill() {
        return false;
    }

}
