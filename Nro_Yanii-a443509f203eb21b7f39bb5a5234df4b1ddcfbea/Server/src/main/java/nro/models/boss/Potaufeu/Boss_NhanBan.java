package nro.models.boss.Potaufeu;

import nro.models.boss.mapoffline.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Setter;
import nro.consts.ConstItem;
import nro.consts.ConstMap;
import nro.consts.ConstPlayer;
import nro.consts.ConstRatio;
import nro.consts.ConstTask;
import nro.event.Event;
import nro.lib.RandomCollection;
import nro.models.boss.Boss;
import nro.models.boss.mabu_war.*;
import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.boss.BossManager;
import nro.models.boss.FutureBoss;
import nro.models.boss.cdrd.CBoss;
import nro.models.boss.iboss.BossInterface;
import nro.models.boss.nappa.Kuku;
import nro.models.boss.nappa.MapDauDinh;
import nro.models.boss.nappa.Rambo;
import nro.models.item.ItemOption;
import nro.models.map.ItemMap;
import nro.models.map.Zone;
import nro.models.npc.NpcFactory;
import nro.models.player.Player;
import nro.models.skill.Skill;
import nro.server.Manager;
import nro.server.ServerNotify;
import nro.services.EffectSkillService;
import nro.services.ItemTimeService;
import nro.services.MapService;
import nro.services.PlayerService;
import nro.services.RewardService;
import nro.services.Service;
import nro.services.SkillService;
import nro.services.TaskService;
import nro.services.func.ChangeMapService;
import nro.utils.Log;
import nro.utils.SkillUtil;
import nro.utils.Util;

/**
 * @author outcast c-cute hột me 😳
 */
/**
 * 
 */
public class Boss_NhanBan extends FutureBoss {

    public Boss_NhanBan(int bossID, BossData bossData, Zone zone, int x, int y, int id_player) throws Exception {
        super(bossID - 200000, bossData);
        this.zone = zone;
        this.location.x = x;
        this.location.y = y;
        this.idPlayerForNPC = id_player;
    }

    @Override
    protected boolean useSpecialSkill() {
        return false;
    }

    @Override
    public void rewards(Player pl) {
        pl.itemTime.doneDanhNhanBan = true;
        pl.itemTime.isDanhNhanBan = false;
        ItemMap it = new ItemMap(this.zone, 638, 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x,
                this.location.y - 24), pl.id);
        pl.itemTime.isDanhNhanBan = true;
        ItemTimeService.gI().sendItemTime(pl, 2295, 1);
        Service.gI().dropItemMap(this.zone, it);
    }

    @Override
    public void idle() {

    }

    @Override
    public void checkPlayerDie(Player pl) {
    }

    long lastTimeBatKhien;

    @Override
    public void attack() {
        try {
            if (this.zone == null) {
                leaveMap();
            } else {
                if (Util.canDoWithTime(lastTimeBatKhien, 10000)) {
                    batKhien();
                    lastTimeBatKhien = System.currentTimeMillis();
                }
                Player pl = super.zone.findPlayerByID(super.idPlayerForNPC);
                if (pl != null && !pl.isMiniPet) {
                    PlayerService.gI().changeAndSendTypePK(this, ConstPlayer.PK_PVP);
                    PlayerService.gI().changeAndSendTypePK(pl, ConstPlayer.PK_PVP);
                    this.playerSkill.skillSelect = this.getSkillAttack();

                    // Kiểm tra null cho playerSkill và playerSkill.skillSelect
                    if (this.playerSkill != null && this.playerSkill.skillSelect != null) {

                        double distance = Util.getDistance(this, pl);
                        double range = this.getRangeCanAttackWithSkillSelect();

                        // Kiểm tra distance và range
                        if (distance <= range) {
                            if (Util.isTrue(15, ConstRatio.PER100)) {
                                if (SkillUtil.isUseSkillChuong(this)) {
                                    goToXY(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 80)),
                                            Util.nextInt(10) % 2 == 0 ? pl.location.y
                                                    : pl.location.y - Util.nextInt(0, 50),
                                            false);
                                } else {
                                    goToXY(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(10, 30)),
                                            Util.nextInt(10) % 2 == 0 ? pl.location.y
                                                    : pl.location.y - Util.nextInt(0, 50),
                                            false);
                                }
                            }
                            SkillService skillService = SkillService.gI();

                            // Kiểm tra null cho skillService
                            if (skillService != null) {
                                skillService.useSkill(this, pl, null, null);
                                checkPlayerDie(pl);
                            } else {
                                // Xử lý khi skillService là null
                                Log.error("SkillService is null");
                            }
                        } else {
                            goToPlayer(pl, false);
                        }
                    } else {
                        // Xử lý khi playerSkill hoặc playerSkill.skillSelect là null
                        Log.error("playerSkill or playerSkill.skillSelect is null");
                    }
                } else {
                    leaveMap();
                }
            }
        } catch (Exception ex) {
            Log.error(Boss.class, ex);
        }
    }

    private void batKhien() {
        this.effectSkill.isShielding = true;
        this.effectSkill.lastTimeShieldUp = System.currentTimeMillis();
        this.effectSkill.timeShield = 5000;
        EffectSkillService.gI().sendEffectPlayer(this, this, EffectSkillService.TURN_ON_EFFECT,
                EffectSkillService.SHIELD_EFFECT);
        ItemTimeService.gI().sendItemTime(this, 3784, 5000 / 1000);
    }

    @Override
    public int injured(Player plAtt, int damage, boolean piercing, boolean isMobAttack) {
        try {
            if (plAtt.id == super.idPlayerForNPC) {
                int dame = 0;
                if (this.isDie()) {
                    return dame;
                } else {
                    if (Util.isTrue(1, 5) && plAtt != null) {
                        switch (plAtt.playerSkill.skillSelect.template.id) {
                            case Skill.TU_SAT:
                            case Skill.QUA_CAU_KENH_KHI:
                            case Skill.MAKANKOSAPPO:
                                break;
                            default:
                                return 0;
                        }
                    }

                    dame = super.injured(plAtt, damage, piercing, isMobAttack);
                    if (this.isDie()) {
                        PlayerService.gI().changeAndSendTypePK(plAtt, ConstPlayer.NON_PK);
                        die();
                    }
                    return dame;
                }
            }
        } catch (Exception e) {

        }
        return 0;
    }

    public void joinMap() {
        if (this.zone != null) {
            ChangeMapService.gI().changeMap(this, this.zone.map.mapId, this.zone.zoneId, this.location.x,
                    this.location.y);
        }
    }

    protected void notifyPlayeKill(Player player) {
        if (player != null) {
        }
    }

    @Override
    public void initTalk() {
    }

    @Override
    public void leaveMap() {
        super.leaveMap();
    }
}
