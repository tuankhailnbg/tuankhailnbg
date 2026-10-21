package nro.models.boss.boss_khi_gas_huy_diet;

import nro.models.boss.Boss;
import static nro.models.boss.Boss.DAME_PERCENT_HP_HUND;
import static nro.models.boss.Boss.DAME_PERCENT_HP_THOU;
import nro.models.boss.BossData;
import nro.models.boss.BossManager;
import nro.models.item.ItemOption;
import nro.models.map.ItemMap;
import nro.models.map.phoban.KhiGas;
import nro.models.mob.Mob;
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
public abstract class BossKhiGasHuyDiet extends Boss {

    protected KhiGas khiGas;

    public BossKhiGasHuyDiet(int id, BossData data, KhiGas KhiGas) {
        super(id, data);
        this.khiGas = KhiGas;
        this.spawn(KhiGas.level);
    }

    private void spawn(byte level) {
        this.nPoint.hpg = (int) (level * this.data.hp[0][0]);
        switch (this.data.typeDame) {
            case DAME_PERCENT_HP_THOU:
                this.nPoint.dameg = (int) (this.nPoint.hpg / 1000 * this.data.dame);
                break;
            case DAME_PERCENT_HP_HUND:
                this.nPoint.dameg = (int) (this.nPoint.hpg / 100 * this.data.dame);
                break;
        }
        this.nPoint.calPoint();
        this.nPoint.setFullHpMp();
    }

    @Override
    public void attack() {
        if (!canAttackWithTime(400)) {
            return;
        }
        super.attack();
    }

    @Override
    public void idle() {
        boolean allMobDie = true;
        for (Mob mob : this.zone.mobs) {
            if (!mob.isDie()) {
                allMobDie = false;
                break;
            }
        }
        if (allMobDie) {
            this.changeToAttack();
        }
    }

    @Override
    public void checkPlayerDie(Player pl) {
        if (pl.isDie()) {
            Service.getInstance().chat(this, "Chừa chưa ranh con, nên nhớ ta là " + this.name);
        }
    }

    @Override
    public void initTalk() {

    }

    @Override
    public void leaveMap() {
        super.leaveMap();
        BossManager.gI().removeBoss(this);
    }

    @Override
    public void rewards(Player pl) {
        pl.clan.banDoKhoBau.trungUyIsDie = true;
        int[] nro = { 17, 18, 19, 20, 539 };
        ItemMap itemMap = new ItemMap(this.zone, nro[Util.nextInt(0, nro.length - 1)], 1,
                this.location.x, this.zone.map.yPhysicInTop(this.location.x, 100), -1);
        itemMap.options.add(new ItemOption(73, 0));
        Service.getInstance().dropItemMap(this.zone, itemMap);
    }

    @Override
    protected boolean useSpecialSkill() {
        return false;
    }

    @Override
    protected void notifyPlayeKill(Player player) {
    }

    @Override
    public void joinMap() {
        try {
            this.zone = this.khiGas.getMapById(148);
            ChangeMapService.gI().changeMap(this, this.zone, 513, 480);
        } catch (Exception e) {

        }
    }
}
