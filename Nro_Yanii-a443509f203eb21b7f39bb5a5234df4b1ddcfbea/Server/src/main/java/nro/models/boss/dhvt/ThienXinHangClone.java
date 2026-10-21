package nro.models.boss.dhvt;

import nro.consts.ConstPlayer;
import nro.models.boss.BossData;
import nro.models.map.DaiHoiVoThuat.DHVT23Service;
import nro.models.player.Player;
import nro.services.PlayerService;
import nro.utils.Log;
import nro.utils.Util;

/**
 * @author outcast c-cute hột me 😳
 */
public class ThienXinHangClone extends BossDHVT {
    private int timeLive;
    private long lastUpdate;


    public ThienXinHangClone(int id, Player player) {
        super(id, BossData.THIEN_XIN_HANG_CLONE);
        this.playerAtt = player;
        timeLive = 10;
        this.status = 1;
        DHVT23Service.gI().sendTypePK(player, this);
        PlayerService.gI().changeAndSendTypePK(this, ConstPlayer.PK_PVP);
    }

    @Override
    public void update() {
        try {
            if (!this.effectSkill.isHaveEffectSkill()
                    && !this.effectSkill.isCharging) {
                switch (this.status) {
                    case JUST_JOIN_MAP:
                        joinMap();
                        if (this.zone != null) {
                            changeStatus(ATTACK);
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
                }
            }
            if (Util.canDoWithTime(lastUpdate, 1000)) {
                lastUpdate = System.currentTimeMillis();
                if (timeLive > 0) {
                    timeLive--;
                } else {
                    super.leaveMap();
                }
            }
            } catch(Exception e){
                Log.error(ThienXinHangClone.class, e);
            }
        }
    }
