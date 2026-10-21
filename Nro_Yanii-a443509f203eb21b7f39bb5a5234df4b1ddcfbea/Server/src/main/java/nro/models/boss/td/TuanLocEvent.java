package nro.models.boss.td;

import nro.consts.ConstRatio;
import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.player.Player;
import nro.services.SkillService;
import nro.utils.SkillUtil;
import nro.utils.Util;

public class TuanLocEvent extends Boss {

    public TuanLocEvent() {
        super(BossFactory.TUAN_LOC_EVENT, BossData.TUAN_LOC_EVENT);

    }
    private boolean checkNhatChuong = false;
    private long lastTimeNhatChuong = 0;
    private long lastTimRestPawn;

    @Override
    protected boolean useSpecialSkill() {
        return false;
    }

    @Override
    public void rewards(Player pl) {

    }

    @Override
    public int injured(Player plAtt, int damage, boolean piercing, boolean isMobAttack) {
        int dame = 0;
        if (this.isDie()) {
            return dame;
        } else {
            if (damage > 1) {
                damage = 0;
            }
            if (checkNhatChuong) {
                return 10;
            }
            dame = super.injured(plAtt, damage, piercing, isMobAttack);
            if (this.isDie()) {
                die();
            }
            return dame;
        }
    }

    public void NhatChuong() {
        checkNhatChuong = true;
        lastTimeNhatChuong = System.currentTimeMillis();
    }

    public boolean checkNhatChuong() {
        return checkNhatChuong;
    }

    @Override
    public void attack() {
        if (!canAttackWithTime(400)) {
            return;
        }
        if (isDie()) {
            super.leaveMap();
        }
        if (lastTimeNhatChuong > 0) {
            if (Util.canDoWithTime(lastTimeNhatChuong, 5000)) {
                lastTimeNhatChuong = 0;
                checkNhatChuong = false;
                super.leaveMap();
                setJustRest();
                changeStatus(DIE);
            }
        }
        if (Util.canDoWithTime(lastTimRestPawn, 180000)) {

            lastTimRestPawn = System.currentTimeMillis();
            super.leaveMap();
            setJustRest();
            changeStatus(DIE);
        }
        try {
            Player pl = getPlayerAttack();
            if (pl != null) {
                if (!useSpecialSkill()) {
                    this.playerSkill.skillSelect = this.getSkillAttack();
                    if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                        if (Util.isTrue(15, ConstRatio.PER100) && SkillUtil.isUseSkillChuong(this)) {
                            goToXY(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 80)),
                                    Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 50),
                                    false);
                        }
                        SkillService.gI().useSkill(this, pl, null, null);
                        checkPlayerDie(pl);
                    } else {
                        goToPlayer(pl, false);
                    }
                }
            }
        } catch (Exception ex) {
        }
    }

    @Override
    public void checkPlayerDie(Player pl) {
    }

    @Override
    public void initTalk() {
        this.textTalkBefore = new String[]{};
        this.textTalkMidle = new String[]{"|-1|Khà khà", "|-1|He he", "|-1|Chết nè"};
        this.textTalkAfter = new String[]{};
    }

    @Override
    public void leaveMap() {
        super.leaveMap();
    }

    @Override
    public void joinMap() {

        super.joinMap();
        this.lastTimRestPawn = System.currentTimeMillis();
        this.lastTimeNhatChuong = 0;
        this.checkNhatChuong = false;
    }
}
