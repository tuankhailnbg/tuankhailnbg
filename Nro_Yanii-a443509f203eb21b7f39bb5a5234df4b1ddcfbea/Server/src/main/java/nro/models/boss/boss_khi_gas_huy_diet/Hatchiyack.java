/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nro.models.boss.boss_khi_gas_huy_diet;

import nro.models.boss.boss_ban_do_kho_bau.*;
import nro.consts.ConstRatio;
import nro.consts.MapName;
import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.map.phoban.BanDoKhoBau;
import nro.models.player.Player;
import nro.services.SkillService;
import nro.services.func.ChangeMapService;
import nro.utils.Util;

import java.util.List;
import nro.consts.ConstPlayer;
import nro.models.boss.Boss;
import nro.models.boss.mabu_war.BossMabuWar;
import nro.models.boss.mapoffline.Boss_ThanMeo;
import nro.models.boss.mapoffline.Boss_Yanjiro;
import nro.models.map.phoban.KhiGas;
import nro.server.ServerNotify;
import nro.services.PlayerService;
import nro.utils.Log;

/**
 *
 * @author 💖 Nro Yanii 💖
 */
public class Hatchiyack extends BossKhiGasHuyDiet {

    private boolean activeAttack;

    public Hatchiyack(KhiGas khiGas) {
        super(BossFactory.TRUNG_UY_XANH_LO, BossData.TRUNG_UY_XANH_LO_2, khiGas);
    }

    @Override
    public void attack() {
        try {
            if (!useSpecialSkill()) {
                Player pl = getPlayerAttack();
                this.playerSkill.skillSelect = this.getSkillAttack();
                if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                    if (Util.isTrue(10, ConstRatio.PER100)) {
                        goToXY(pl.location.x + Util.nextInt(-20, 20),
                                Util.nextInt(pl.location.y - 80, this.zone.map.yPhysicInTop(pl.location.x, 0)), false);
                    }
                    SkillService.gI().useSkill(this, pl, null, null);
                    checkPlayerDie(pl);
                } else {
                    goToPlayer(pl, false);
                }
            }
        } catch (Exception ex) {
        }
    }

    @Override
    public void update() {
        try {
            if (!this.effectSkill.isHaveEffectSkill()
                    && !this.effectSkill.isCharging) {
                this.immortalMp();
                switch (this.status) {
                    case RESPAWN:
                        respawn();
                        break;
                    case JUST_RESPAWN:
                        this.changeStatus(REST);
                        break;
                    case REST:
                        if (Util.canDoWithTime(lastTimeRest, secondTimeRestToNextTimeAppear * 1000)) {
                            this.changeStatus(JUST_JOIN_MAP);
                        }
                        break;
                    case JUST_JOIN_MAP:
                        joinMap();
                        if (this.zone != null) {
                            changeStatus(TALK_BEFORE);
                        }
                        break;
                    case TALK_BEFORE:
                        if (talk()) {
                            if (activeAttack) {
                                if (!this.joinMapIdle) {
                                    changeToAttack();
                                } else {
                                    this.changeStatus(IDLE);
                                }
                            } else {
                                List<Player> notBosses = this.zone.getNotBosses();
                                for (Player pl : notBosses) {
                                    if ((pl.location.x <= 358 && pl.location.y == 456) && !pl.effectSkin.isVoHinh) {
                                        this.activeAttack = true;
                                        break;
                                    }
                                }
                            }
                        }
                        break;
                    case ATTACK:
                        this.talk();
                        if (this.playerSkill.prepareTuSat || this.playerSkill.prepareLaze
                                || this.playerSkill.prepareQCKK) {
                            break;
                        } else {
                            this.attack();
                        }
                        break;
                    case IDLE:
                        this.idle();
                        break;
                    case DIE:
                        if (this.joinMapIdle) {
                            this.changeToIdle();
                        }
                        changeStatus(TALK_AFTER);
                        break;
                    case TALK_AFTER:
                        if (talk()) {
                            changeStatus(LEAVE_MAP);
                            this.lastTimeStartLeaveMap = System.currentTimeMillis();
                        }
                        break;
                    case LEAVE_MAP:
                        if (Util.canDoWithTime(lastTimeStartLeaveMap, timeDelayLeaveMap)) {
                            this.leaveMap();
                            this.changeStatus(RESPAWN);
                        }
                        break;
                    case DO_NOTHING:
                        break;
                }
            }
        } catch (Exception e) {
            Log.error(Boss.class, e);
        }
    }

    private void immortalMp() {
        this.nPoint.mp = this.nPoint.mpg;
    }

    @Override
    protected boolean useSpecialSkill() {
        // boss này chỉ có chiêu thái dương hạ san
        this.playerSkill.skillSelect = this.getSkillSpecial();
        if (SkillService.gI().canUseSkillWithCooldown(this)) {
            SkillService.gI().useSkill(this, null, null, null);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void rewards(Player pl) {

    }
}
