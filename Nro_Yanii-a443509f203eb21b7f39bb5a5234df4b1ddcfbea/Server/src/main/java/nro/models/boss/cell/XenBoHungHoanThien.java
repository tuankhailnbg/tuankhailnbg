package nro.models.boss.cell;

import nro.consts.ConstItem;
import nro.consts.ConstRatio;
import nro.models.boss.*;
import nro.models.item.ItemOption;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.server.Manager;
import nro.services.RewardService;
import nro.services.Service;
import nro.services.SkillService;
import nro.services.TaskService;
import nro.utils.Log;
import nro.utils.SkillUtil;
import nro.utils.Util;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class XenBoHungHoanThien extends FutureBoss {

    public XenBoHungHoanThien() {
        super(BossFactory.XEN_BO_HUNG_HOAN_THIEN, BossData.XEN_BO_HUNG_HOAN_THIEN);
    }

    @Override
    protected boolean useSpecialSkill() {
        return false;
    }

    @Override
    public void rewards(Player pl) {
        // Kiểm tra và hoàn thành nhiệm vụ khi giết boss
        TaskService.gI().checkDoneTaskKillBoss(pl, this);

        generalRewards(pl);
    }

    @Override
    public void attack() {
        try {
            Player pl = getPlayerAttack();
            if (pl != null) {
                this.playerSkill.skillSelect = this.getSkillAttack();
                if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                    if (Util.isTrue(15, ConstRatio.PER100) && SkillUtil.isUseSkillChuong(this)) {
                        goToXY(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 80)),
                                Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 50), false);
                    }
                    SkillService.gI().useSkill(this, pl, null, null);
                    checkPlayerDie(pl);
                } else {
                    goToPlayer(pl, false);
                }
            }
        } catch (Exception ex) {
            Log.error(XenBoHungHoanThien.class, ex);
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
        this.textTalkBefore = new String[] {};
        this.textTalkMidle = new String[] { "Tất cả nhào vô", "Mình ta cũng đủ để hủy diệt các ngươi" };
        this.textTalkAfter = new String[] {};
    }

    @Override
    public void joinMap() {
        super.joinMap();
    }

    @Override
    public void leaveMap() {
        BossManager.gI().getBossById(BossFactory.XEN_BO_HUNG_1).setJustRest();
        super.leaveMap();
        BossManager.gI().removeBoss(this);
    }

}
