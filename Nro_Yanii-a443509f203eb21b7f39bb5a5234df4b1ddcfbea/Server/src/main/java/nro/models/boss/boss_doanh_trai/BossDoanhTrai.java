package nro.models.boss.boss_doanh_trai;

import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossManager;
import nro.models.clan.Clan;
import nro.models.clan.ClanMember;
import nro.models.map.ItemMap;
import nro.models.map.phoban.DoanhTrai;
import nro.models.player.Player;
import nro.services.Service;
import nro.services.TaskService;
import nro.utils.Util;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public abstract class BossDoanhTrai extends Boss {

    private long highestDame; // dame lớn nhất trong clan
    private long highestHp; // hp lớn nhất trong clan

    private long xHpForDame = 50; // dame gốc = highesHp / xHpForDame;
    private long xDameForHp = 50; // hp gốc = xDameForHp * highestDame;

    protected DoanhTrai doanhTrai;

    public BossDoanhTrai(int id, BossData data, DoanhTrai doanhTrai) {
        super(id, data);
        this.xHpForDame = data.dame;

        int[] arrHp = data.hp[Util.nextInt(0, data.hp.length - 1)];
        this.xDameForHp = arrHp[Util.nextInt(0, arrHp.length - 1)];
        this.doanhTrai = doanhTrai;

        this.spawn(doanhTrai.clan);
    }

    private void spawn(Clan clan) {
        switch (this.typeDame) {
            case DAME_TIME_PLAYER_WITH_HIGHEST_HP_IN_CLAN:
                for (ClanMember cm : clan.getMembers()) {
                    for (Player pl : clan.membersInGame) {
                        if (pl.id == cm.id && pl.nPoint.hpMax >= highestHp) {
                            this.highestHp = pl.nPoint.hpMax;
                        }
                    }
                }
                this.nPoint.dameg = (int) (this.highestHp / this.xHpForDame);
                break;
        }
        switch (this.typeHp) {
            case HP_TIME_PLAYER_WITH_HIGHEST_DAME_IN_CLAN:
                for (ClanMember cm : clan.getMembers()) {
                    for (Player pl : clan.membersInGame) {
                        if (pl.id == cm.id && pl.nPoint.dame >= highestDame) {
                            this.highestDame = pl.nPoint.dame;
                        }
                    }
                }
                this.nPoint.hpg = (int) (this.highestDame * this.xDameForHp);
                this.nPoint.calPoint();
                this.nPoint.setFullHpMp();
                break;
        }
    }

    @Override
    public void idle() {

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
        int[] nro = { 16, 17, 18 };
        ItemMap itemMap = new ItemMap(this.zone, nro[Util.nextInt(0, nro.length - 1)], 1,
                this.location.x, this.zone.map.yPhysicInTop(this.location.x, 100), -1);
        Service.getInstance().dropItemMap(this.zone, itemMap);
        TaskService.gI().checkDoneTaskKillBoss(pl, this);
    }

    @Override
    protected boolean useSpecialSkill() {
        return false;
    }

    @Override
    protected void notifyPlayeKill(Player player) {
    }
}
