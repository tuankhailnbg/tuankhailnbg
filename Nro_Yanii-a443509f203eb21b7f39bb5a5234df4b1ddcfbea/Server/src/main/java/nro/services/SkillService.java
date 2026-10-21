package nro.services;

import nro.consts.ConstAchive;
import nro.consts.ConstPlayer;
import nro.models.intrinsic.Intrinsic;
import nro.models.mob.Mob;
import nro.models.mob.MobMe;
import nro.models.player.Pet;
import nro.models.player.Player;
import nro.models.pvp.PVP;
import nro.models.skill.Skill;
import nro.models.skill.SkillNotFocus;
import nro.server.io.Message;
import nro.services.func.PVPServcice;
import nro.utils.Log;
import nro.utils.SkillUtil;
import nro.utils.Util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import nro.models.boss.Boss;
import nro.services.func.RadaService;

public class SkillService {

    private static SkillService i;

    private SkillService() {

    }

    public static SkillService gI() {
        if (i == null) {
            i = new SkillService();
        }
        return i;
    }

    public boolean useSkill(Player player, Player plTarget, Mob mobTarget, Message message) {
        try {
            if (player.playerSkill.skillSelect == null && player.playerSkill == null
                    && player.playerSkill.skillSelect.template == null) {
                return false;
            }
            if (player.playerSkill.skillSelect.template.type == 2 && canUseSkillWithMana(player)
                    && canUseSkillWithCooldown(player)) {
                useSkillBuffToPlayer(player, plTarget);
                return true;
            }
            if ((player.effectSkill.isHaveEffectSkill()
                    && (player.playerSkill.skillSelect.template.id != Skill.TU_SAT
                            && player.playerSkill.skillSelect.template.id != Skill.QUA_CAU_KENH_KHI
                            && player.playerSkill.skillSelect.template.id != Skill.MAKANKOSAPPO))
                    || (plTarget != null && !canAttackPlayer(player, plTarget))
                    || (mobTarget != null && mobTarget.isDie())
                    || !canUseSkillWithMana(player) || !canUseSkillWithCooldown(player)) {
                return false;
            }
            if (player.effectSkill.useTroi) {
                EffectSkillService.gI().removeUseTroi(player);
            }
            if (player.effectSkill.isCharging) {
                EffectSkillService.gI().stopCharge(player);
            }
            if (player.isPet) {
            }
            byte st = -1;
            byte skillId = -1;
            Short dx = -1;
            Short dy = -1;
            byte dir = -1;
            Short x = -1;
            Short y = -1;

            try {
                st = message.reader().readByte();
                skillId = message.reader().readByte();
                dx = message.reader().readShort();
                dy = message.reader().readShort();
                dir = message.reader().readByte();
                x = message.reader().readShort();
                y = message.reader().readShort();
            } catch (Exception ex) {

            }
            if (st == 20 && skillId != player.playerSkill.skillSelect.template.id) {
                selectSkill(player, skillId);
                return false;
            }
            switch (player.playerSkill.skillSelect.template.type) {
                case 1:
                    useSkillAttack(player, plTarget, mobTarget);
                    break;
                case 3:
                    useSkillAlone(player);
                    break;
                default:
                    return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private void useSkillAttack(Player player, Player plTarget, Mob mobTarget) {
        if (!player.isBoss) {
            if (player.isPet) {
                if (player.nPoint.stamina > 0) {
                    player.nPoint.numAttack++;
                    boolean haveCharmPet = ((Pet) player).master.charms.tdDeTu > System.currentTimeMillis();
                    if (haveCharmPet ? player.nPoint.numAttack >= 5 : player.nPoint.numAttack >= 2) {
                        player.nPoint.numAttack = 0;
                        player.nPoint.stamina--;
                    }
                } else {
                    ((Pet) player).askPea();
                    return;
                }
            } else {
                if (player.nPoint.stamina > 0) {
                    if (player.charms.tdDeoDai < System.currentTimeMillis()) {
                        player.nPoint.numAttack++;
                        if (player.nPoint.numAttack == 5) {
                            player.nPoint.numAttack = 0;
                            player.nPoint.stamina--;
                            PlayerService.gI().sendCurrentStamina(player);
                        }
                    }
                } else {
                    Service.getInstance().sendThongBao(player, "Thể lực đã cạn kiệt, hãy nghỉ ngơi để lấy lại sức");
                    return;
                }
            }
        }

        boolean miss = false;
        switch (player.playerSkill.skillSelect.template.id) {
            case Skill.KAIOKEN: // kaioken
                int hpUse = player.nPoint.hpMax / 100 * 10;
                if (player.nPoint.hp <= hpUse) {
                    break;
                } else {
                    player.nPoint.setHp(player.nPoint.mp - hpUse);
                    PlayerService.gI().sendInfoHpMpMoney(player);
                    Service.getInstance().Send_Info_NV(player);
                }
            case Skill.DRAGON:
            case Skill.DEMON:
            case Skill.GALICK:
            case Skill.LIEN_HOAN:
                if (player.isPl()) {
                    player.playerSkill.skillSelect.coolDown = 0;
                }
                if (plTarget != null && Util.getDistance(player, plTarget) > Skill.RANGE_ATTACK_CHIEU_DAM) {
                    miss = true;
                }
                if (mobTarget != null && Util.getDistance(player, mobTarget) > Skill.RANGE_ATTACK_CHIEU_DAM) {
                    miss = true;
                }
            case Skill.KAMEJOKO:
            case Skill.MASENKO:
            case Skill.ANTOMIC:
                if (plTarget != null) {
                    playerAttackPlayer(player, plTarget, miss, -1);
                }
                if (mobTarget != null) {
                    playerAttackMob(player, mobTarget, miss, false);
                }
                if (player.mobMe != null) {
                    player.mobMe.attack(plTarget, mobTarget);
                }
                if (player.id >= 0) {
                    player.playerTask.achivements.get(ConstAchive.NOI_CONG_CAO_CUONG).count++;
                }
                affterUseSkill(player, player.playerSkill.skillSelect.template.id);
                break;
            // ******************************************************************
            case Skill.QUA_CAU_KENH_KHI:
                // ném cầu
                List<Mob> mobs = new ArrayList<>();
                if (plTarget != null) {
                    playerAttackPlayer(player, plTarget, false, -1);
                    for (Mob mob : player.zone.mobs) {
                        if (!mob.isDie()
                                && Util.getDistance(plTarget, mob) <= SkillUtil
                                        .getRangeQCKK(player.playerSkill.skillSelect.point)) {
                            mobs.add(mob);
                        }
                    }
                }
                if (mobTarget != null) {
                    playerAttackMob(player, mobTarget, false, true);
                    for (Mob mob : player.zone.mobs) {
                        if (!mob.equals(mobTarget) && !mob.isDie()
                                && Util.getDistance(mob, mobTarget) <= SkillUtil
                                        .getRangeQCKK(player.playerSkill.skillSelect.point)) {
                            mobs.add(mob);
                        }
                    }
                }
                for (Mob mob : mobs) {
                    mob.injured(player, player.nPoint.getDameAttack(true), true);
                }
                PlayerService.gI().sendInfoHpMpMoney(player);
                affterUseSkill(player, player.playerSkill.skillSelect.template.id);
                break;
            case Skill.MAKANKOSAPPO:
                // bắn laze
                if (plTarget != null) {
                    playerAttackPlayer(player, plTarget, false, -1);
                }
                if (mobTarget != null) {
                    playerAttackMob(player, mobTarget, false, true);
                }
                affterUseSkill(player, player.playerSkill.skillSelect.template.id);
                PlayerService.gI().sendInfoHpMpMoney(player);
                player.playerSkill.prepareLaze = false;
                break;
            case Skill.SOCOLA:
                EffectSkillService.gI().sendEffectUseSkill(player, Skill.SOCOLA);
                int timeSocola = SkillUtil.getTimeSocola();
                if (plTarget != null) {
                    EffectSkillService.gI().setSocola(plTarget, System.currentTimeMillis(), timeSocola);
                    Service.getInstance().Send_Caitrang(plTarget);
                    ItemTimeService.gI().sendItemTime(plTarget, 3780, timeSocola / 1000);
                }
                if (mobTarget != null) {
                    EffectSkillService.gI().sendMobAppearence(player, mobTarget, EffectSkillService.SOCOLA);
                    mobTarget.effectSkill.setSocola(System.currentTimeMillis(), timeSocola);
                }
                affterUseSkill(player, player.playerSkill.skillSelect.template.id);
                break;
            case Skill.DICH_CHUYEN_TUC_THOI:
                int timeChoangDCTT = SkillUtil.getTimeDCTT(player.playerSkill.skillSelect.point);
                if (plTarget != null) {
                    Service.getInstance().setPos(player, plTarget.location.x, plTarget.location.y);
                    playerAttackPlayer(player, plTarget, miss, -1);
                    EffectSkillService.gI().setBlindDCTT(plTarget, System.currentTimeMillis(), timeChoangDCTT);
                    EffectSkillService.gI().sendEffectPlayer(player, plTarget, EffectSkillService.TURN_ON_EFFECT,
                            EffectSkillService.BLIND_EFFECT);
                    PlayerService.gI().sendInfoHpMpMoney(plTarget);
                    ItemTimeService.gI().sendItemTime(plTarget, 3779, timeChoangDCTT / 1000);
                }
                if (mobTarget != null) {
                    Service.getInstance().setPos(player, mobTarget.location.x, mobTarget.location.y);
                    playerAttackMob(player, mobTarget, false, false);
                    mobTarget.effectSkill.setStartBlindDCTT(System.currentTimeMillis(), timeChoangDCTT);
                    EffectSkillService.gI().sendEffectMob(player, mobTarget, EffectSkillService.TURN_ON_EFFECT,
                            EffectSkillService.BLIND_EFFECT);
                }
                player.nPoint.isCrit100 = true;
                affterUseSkill(player, player.playerSkill.skillSelect.template.id);
                break;
            case Skill.THOI_MIEN:
                EffectSkillService.gI().sendEffectUseSkill(player, Skill.THOI_MIEN);
                int timeSleep = SkillUtil.getTimeThoiMien(player.playerSkill.skillSelect.point);
                if (plTarget != null) {
                    EffectSkillService.gI().setThoiMien(plTarget, System.currentTimeMillis(), timeSleep);
                    EffectSkillService.gI().sendEffectPlayer(player, plTarget, EffectSkillService.TURN_ON_EFFECT,
                            EffectSkillService.SLEEP_EFFECT);
                    ItemTimeService.gI().sendItemTime(plTarget, 3782, timeSleep / 1000);
                }
                if (mobTarget != null) {
                    mobTarget.effectSkill.setThoiMien(System.currentTimeMillis(), timeSleep);
                    EffectSkillService.gI().sendEffectMob(player, mobTarget, EffectSkillService.TURN_ON_EFFECT,
                            EffectSkillService.SLEEP_EFFECT);
                }
                affterUseSkill(player, player.playerSkill.skillSelect.template.id);
                break;
            case Skill.TROI:
                EffectSkillService.gI().sendEffectUseSkill(player, Skill.TROI);
                int timeHold = SkillUtil.getTimeTroi(player.playerSkill.skillSelect.point);
                EffectSkillService.gI().setUseTroi(player, System.currentTimeMillis(), timeHold);
                if (plTarget != null
                        && (!plTarget.playerSkill.prepareQCKK && !plTarget.playerSkill.prepareLaze
                                && !plTarget.playerSkill.prepareTuSat)) {
                    // MIEN TROI
                    if (plTarget.effectSkill.mienTroi) {
                        Service.gI().sendThongBao(player, "Chiêu thức này không có tác dụng với ta kakaka");
                    } else {
                        player.effectSkill.plAnTroi = plTarget;
                        EffectSkillService.gI().sendEffectPlayer(player, plTarget, EffectSkillService.TURN_ON_EFFECT,
                                EffectSkillService.HOLD_EFFECT);
                        EffectSkillService.gI().setAnTroi(plTarget, player, System.currentTimeMillis(), timeHold);
                    }
                }
                if (mobTarget != null) {
                    player.effectSkill.mobAnTroi = mobTarget;
                    EffectSkillService.gI().sendEffectMob(player, mobTarget, EffectSkillService.TURN_ON_EFFECT,
                            EffectSkillService.HOLD_EFFECT);
                    mobTarget.effectSkill.setTroi(System.currentTimeMillis(), timeHold);
                }
                affterUseSkill(player, player.playerSkill.skillSelect.template.id);
                break;
        }
        if (!player.isBoss) {
            player.effectSkin.lastTimeAttack = System.currentTimeMillis();
        }
    }

    public void useSkillThaiDuong(Player player) {
        if (!canUseSkillWithCooldown(player)) {
            return;
        }
        int timeStun = SkillUtil.getTimeStun(player.playerSkill.skillSelect.point);
        if (player.setClothes.thienXinHang == 5) {
            timeStun *= 2;
        }
        if (player == null || player.zone == null) {
            return;
        }
        List<Mob> mobs = new ArrayList<>();
        List<Player> players = new ArrayList<>();
        if (!player.zone.map.isMapOffline) {
            ReentrantLock lock = new ReentrantLock();
            lock.lock();
            List<Player> playersMap = player.zone.getHumanoids();
            for (Player pl : playersMap) {
                if (pl != null && !player.equals(pl)) {
                    int distance = Util.getDistance(player, pl);
                    int rangeStun = SkillUtil.getRangeStun(player.playerSkill.skillSelect.point);
                    if (distance <= rangeStun && canAttackPlayer(player, pl)) {// && (!pl.playerSkill.prepareQCKK &&
                                                                               // !pl.playerSkill.prepareLaze &&
                                                                               // !pl.playerSkill.prepareTuSat)
                        if (player.isPet && ((Pet) player).master.equals(pl)) {
                            continue;
                        }
                        if (player.isBoss && pl.isBoss) {
                            continue;
                        }
                        EffectSkillService.gI().startStun(pl, System.currentTimeMillis(), timeStun);
                        if (pl.typePk != ConstPlayer.NON_PK) {
                            players.add(pl);
                        }
                    }
                }
            }
        }
        for (Mob mob : player.zone.mobs) {
            if (Util.getDistance(player, mob) <= SkillUtil.getRangeStun(player.playerSkill.skillSelect.point)) {
                mob.effectSkill.startStun(System.currentTimeMillis(), timeStun);
                mobs.add(mob);
            }
        }
        EffectSkillService.gI().sendEffectBlindThaiDuongHaSan(player, players, mobs, timeStun);
        affterUseSkill(player, player.playerSkill.skillSelect.template.id);
    }

    public void useSkillTTNL(Player player) {
        EffectSkillService.gI().startCharge(player);
        affterUseSkill(player, player.playerSkill.skillSelect.template.id);
    }

    public void useSkillBienKhi(Player player) {
        EffectSkillService.gI().sendEffectMonkey(player);
        EffectSkillService.gI().setIsMonkey(player);
        affterUseSkill(player, player.playerSkill.skillSelect.template.id);
    }

    public void useSkillKhien(Player player) {
        if (player.effectSkill.anTroi) {
            Service.getInstance().sendThongBao(player, "đang bị tròi không thể sử dụng khiên năng lượng!");
            return;
        } else {
            EffectSkillService.gI().setStartShield(player);
            EffectSkillService.gI().sendEffectPlayer(player, player, EffectSkillService.TURN_ON_EFFECT,
                    EffectSkillService.SHIELD_EFFECT);
            ItemTimeService.gI().sendItemTime(player, 3784, player.effectSkill.timeShield / 1000);
            affterUseSkill(player, player.playerSkill.skillSelect.template.id);
        }
    }

    public void useSkillTuSat(Player player) {
        sendPlayerPrepareBom(player, 2000);
    }

    public void handleTuSat(Player player) {
        player.playerSkill.prepareTuSat = false;
        int rangeBom = SkillUtil.getRangeBom(player.playerSkill.skillSelect.point);
        int dame = player.nPoint.hpMax;
        if (player.effectSkill.isMonkey) {
            dame = (int) (dame / 2.4);
        } else {
            dame /= 2;
        }
        for (Mob mob : player.zone.mobs) {
            if (Util.getDistance(player, mob) <= rangeBom) {
                mob.injured(player, dame, true);
            }
        }
        for (int j = 0; j < player.zone.getHumanoids().size(); j++) {
            Player pl = player.zone.getHumanoids().get(j);
            if (pl != null && !player.equals(pl) && (canAttackPlayer(player, pl) || player.isBoss)) {
                // && Util.getDistance(player, pl) <= rangeBom) { // Xóa điều kiện khoảng cách
                // player
                pl.injured(player, pl.isBoss ? dame : dame, false, false);
                PlayerService.gI().sendInfoHpMpMoney(pl);
                Service.getInstance().Send_Info_NV(pl);
            }
        }
        affterUseSkill(player, player.playerSkill.skillSelect.template.id);
        player.injured(null, 2000000000, true, false);
    }

    public void useSkillDeTrung(Player player) {
        EffectSkillService.gI().sendEffectUseSkill(player, Skill.DE_TRUNG);
        if (player.mobMe != null) {
            player.mobMe.mobMeDie();
        }
        player.mobMe = new MobMe(player);
        affterUseSkill(player, player.playerSkill.skillSelect.template.id);
    }

    public void useSkillHuytSao(Player player) {
        int tileHP = SkillUtil.getPercentHPHuytSao(player.playerSkill.skillSelect.point);
        if (player.zone != null) {
            if (!player.zone.map.isMapOffline) {
                List<Player> playersMap = player.zone.getHumanoids();
                for (Player pl : playersMap) {
                    if (pl.effectSkill.useTroi && pl.effectSkill != null) {
                        EffectSkillService.gI().removeUseTroi(pl);
                    }
                    if (!pl.isBoss && pl.gender != ConstPlayer.NAMEC
                            && player.cFlag == pl.cFlag) {
                        EffectSkillService.gI().setStartHuytSao(pl, tileHP);
                        EffectSkillService.gI().sendEffectPlayer(pl, pl, EffectSkillService.TURN_ON_EFFECT,
                                EffectSkillService.HUYT_SAO_EFFECT);
                        pl.nPoint.calPoint();
                        pl.nPoint.setHp(pl.nPoint.hp + (int) ((long) pl.nPoint.hp * tileHP / 100));
                        Service.getInstance().point(pl);
                        Service.getInstance().Send_Info_NV(pl);
                        ItemTimeService.gI().sendItemTime(pl, 3781, 30);
                        PlayerService.gI().sendInfoHpMp(pl);
                    }

                }
            } else {
                EffectSkillService.gI().setStartHuytSao(player, tileHP);
                EffectSkillService.gI().sendEffectPlayer(player, player, EffectSkillService.TURN_ON_EFFECT,
                        EffectSkillService.HUYT_SAO_EFFECT);
                player.nPoint.calPoint();
                player.nPoint.setHp(player.nPoint.hp + (int) ((long) player.nPoint.hp * tileHP / 100));
                Service.getInstance().point(player);
                Service.getInstance().Send_Info_NV(player);
                ItemTimeService.gI().sendItemTime(player, 3781, 30);
                PlayerService.gI().sendInfoHpMp(player);
            }
        }
        affterUseSkill(player, player.playerSkill.skillSelect.template.id);
    }

    private void useSkillAlone(Player player) {
        List<Mob> mobs;
        List<Player> players;
        switch (player.playerSkill.skillSelect.template.id) {
            case Skill.BIEN_HINH: {
                EffectSkillService.gI().sendEffectbienhinh(player);
                if (player.effectSkill.levelBienHinh < player.playerSkill.skillSelect.point) {
                    EffectSkillService.gI().setBienHinh(player);
                    EffectSkillService.gI().sendEffectbienhinh(player);
                    Service.getInstance().Send_Caitrang(player);
                    Service.getInstance().point(player);
                    player.nPoint.setFullHpMp();
                    PlayerService.gI().sendInfoHpMp(player);
                    RadaService.getInstance().setIDAuraEff(player, player.getAura());
                    // ICON Ở GÓC TD / NM/XD
                    // ItemTimeService.gI().sendItemTime(player, player.gender == 0 ? 30011 :
                    // player.gender == 1 ? 30011 : 30011, player.effectSkill.timeBienHinh / 1000);
                    ItemTimeService.gI().sendItemTimeBienHinh(player, player.effectSkill.levelBienHinh);

                    affterUseSkill(player, player.playerSkill.skillSelect.template.id);
                }
                break;
            }
        }
        if (!player.playerTask.achivements.isEmpty()) {
            player.playerTask.achivements.get(ConstAchive.KY_NANG_THANH_THAO).count++;
        }
    }

    private void useSkillBuffToPlayer(Player player, Player plTarget) {
        switch (player.playerSkill.skillSelect.template.id) {
            case Skill.TRI_THUONG -> {
                List<Player> players = new ArrayList();
                int percentTriThuong = SkillUtil.getPercentTriThuong(player.playerSkill.skillSelect.point);
                if (canHsPlayer(player, plTarget)) {
                    players.add(plTarget);
                    List<Player> playersMap = player.zone.getNotBosses();
                    for (Player pl : playersMap) {
                        if (!pl.equals(plTarget)) {
                            if (canHsPlayer(player, plTarget) && Util.getDistance(player, pl) <= 300) {
                                players.add(pl);
                            }
                        }
                    }
                    // playerAttackPlayer(player, plTarget, false);
                    for (Player pl : players) {
                        boolean isDie = pl.isDie();
                        int hpHoi = pl.nPoint.hpMax * percentTriThuong / 100;
                        int mpHoi = pl.nPoint.mpMax * percentTriThuong / 100;
                        pl.nPoint.addHp(hpHoi);
                        pl.nPoint.addMp(mpHoi);
                        if (isDie) {
                            Service.getInstance().hsChar(pl, hpHoi, mpHoi);
                            PlayerService.gI().sendInfoHpMp(pl);
                        } else {
                            Service.getInstance().Send_Info_NV(pl);
                            PlayerService.gI().sendInfoHpMp(pl);
                        }
                    }
                    int hpHoiMe = player.nPoint.hp * percentTriThuong / 100;
                    player.nPoint.addHp(hpHoiMe);
                    PlayerService.gI().sendInfoHp(player);
                }
                affterUseSkill(player, player.playerSkill.skillSelect.template.id);

            }
        }
    }

    private void phanSatThuong(Player plAtt, Player plTarget, int dame) {
        int percentPST = plTarget.nPoint.tlPST;
        if (percentPST != 0) {
            int damePST = dame * percentPST / 100;
            Message msg;
            try {
                msg = new Message(56);
                msg.writer().writeInt((int) plAtt.id);
                if (damePST >= plAtt.nPoint.hp) {
                    damePST = plAtt.nPoint.hp - 1;
                }
                damePST = plAtt.injured(null, damePST, true, false);
                msg.writer().writeInt(plAtt.nPoint.hp);
                msg.writer().writeInt(damePST);
                msg.writer().writeBoolean(false);
                msg.writer().writeByte(36);
                Service.getInstance().sendMessAllPlayerInMap(plAtt, msg);
                msg.cleanup();
            } catch (IOException e) {
                Log.error(SkillService.class, e);
            }
        }
    }

    public void havePlayerAttack(Player plGetHit, boolean isCrit, int dame, int type) {
        try {
            Message msg = new Message(56);
            msg.writer().writeInt((int) plGetHit.id);
            msg.writer().writeInt(plGetHit.nPoint.hp);
            msg.writer().writeInt(dame);
            msg.writer().writeBoolean(isCrit);
            msg.writer().writeByte(type);
            Service.getInstance().sendMessAllPlayerInMap(plGetHit, msg);
            msg.cleanup();
        } catch (IOException e) {
            Log.error(SkillService.class, e);
        }
    }

    private void hutHPMP(Player player, int dame, boolean attackMob) {
        int tiLeHutHp = player.nPoint.getTileHutHp(attackMob);
        int tiLeHutMp = player.nPoint.getTiLeHutMp();
        int hpHoi = dame * tiLeHutHp / 100;
        int mpHoi = dame * tiLeHutMp / 100;
        if (hpHoi > 0 || mpHoi > 0) {
            PlayerService.gI().hoiPhuc(player, hpHoi, mpHoi);
        }
    }

    private void playerAttackPlayer(Player plAtt, Player plInjure, boolean miss, int damage) {
        if (plInjure.effectSkill.anTroi) {
            plAtt.nPoint.isCrit100 = true;
        }
        int dameHit = plInjure.injured(plAtt, miss ? 0 : damage != -1 ? damage : plAtt.nPoint.getDameAttack(false),
                false, false);
        phanSatThuong(plAtt, plInjure, dameHit);
        hutHPMP(plAtt, dameHit, false);
        Message msg;
        try {
            msg = new Message(-60);
            msg.writer().writeInt((int) plAtt.id); // id pem
            msg.writer().writeByte(plAtt.playerSkill.skillSelect.skillId); // skill pem
            msg.writer().writeByte(1); // số người pem
            msg.writer().writeInt((int) plInjure.id); // id ăn pem
            byte typeSkill = SkillUtil.getTyleSkillAttack(plAtt.playerSkill.skillSelect);
            msg.writer().writeByte(typeSkill == 2 ? 0 : 1); // read continue
            msg.writer().writeByte(typeSkill); // type skill
            msg.writer().writeInt(dameHit); // dame ăn
            msg.writer().writeBoolean(plInjure.isDie()); // is die
            msg.writer().writeBoolean(plAtt.nPoint.isCrit); // crit
            if (typeSkill != 1) {
                Service.getInstance().sendMessAllPlayerInMap(plAtt, msg);
                msg.cleanup();
            } else {
                plInjure.sendMessage(msg);
                msg.cleanup();
                msg = new Message(-60);
                msg.writer().writeInt((int) plAtt.id); // id pem
                msg.writer().writeByte(plAtt.playerSkill.skillSelect.skillId); // skill pem
                msg.writer().writeByte(1); // số người pem
                msg.writer().writeInt((int) plInjure.id); // id ăn pem
                msg.writer().writeByte(typeSkill == 2 ? 0 : 1); // read continue
                msg.writer().writeByte(0); // type skill
                msg.writer().writeInt(dameHit); // dame ăn
                msg.writer().writeBoolean(plInjure.isDie()); // is die
                msg.writer().writeBoolean(plAtt.nPoint.isCrit); // crit
                Service.getInstance().sendMessAnotherNotMeInMap(plInjure, msg);
                msg.cleanup();
            }
            Service.getInstance().addSMTN(plInjure, (byte) 2, 1, false);
        } catch (IOException e) {
            Log.error(SkillService.class, e);
        }
    }

    private void playerAttackMob(Player plAtt, Mob mob, boolean miss, boolean dieWhenHpFull) {
        if (!mob.isDie()) {
            if (plAtt.effectSkin.isVoHinh) {
                plAtt.effectSkin.isVoHinh = false;
            }
            int dameHit = plAtt.nPoint.getDameAttack(true);
            if (plAtt.charms.tdBatTu > System.currentTimeMillis() && plAtt.nPoint.hp == 1) {
                dameHit = 0;
            }
            if (plAtt.charms.tdManhMe > System.currentTimeMillis()) {
                dameHit += (dameHit * 150 / 100);
            }
            if (plAtt.isPet) {
                if (((Pet) plAtt).charms.tdDeTu > System.currentTimeMillis()) {
                    dameHit *= 2;
                }
            }
            if (miss) {
                dameHit = 0;
            }
            hutHPMP(plAtt, dameHit, true);
            mob.injured(plAtt, dameHit, dieWhenHpFull);
            sendPlayerAttackMob(plAtt, mob);
        }
    }

    public void sendPlayerPrepareSkill(Player player, int affterMiliseconds) {
        Message msg;
        try {
            msg = new Message(-45);
            msg.writer().writeByte(4);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(player.playerSkill.skillSelect.skillId);
            msg.writer().writeShort(affterMiliseconds);
            Service.getInstance().sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendPlayerPrepareBom(Player player, int affterMiliseconds) {
        Message msg;
        try {
            msg = new Message(-45);
            msg.writer().writeByte(7);
            msg.writer().writeInt((int) player.id);
            // msg.writer().writeShort(player.playerSkill.skillSelect.skillId);
            msg.writer().writeShort(104);
            msg.writer().writeShort(affterMiliseconds);
            Service.getInstance().sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public boolean canUseSkillWithMana(Player player) {
        if (player.playerSkill.skillSelect != null) {
            if (player.playerSkill.skillSelect.template.id == Skill.KAIOKEN) {
                int hpUse = player.nPoint.hpMax / 100 * 10;
                if (player.nPoint.hp <= hpUse) {
                    return false;
                }
            }
            switch (player.playerSkill.skillSelect.template.manaUseType) {
                case 0 -> {
                    return player.nPoint.mp >= player.playerSkill.skillSelect.manaUse;
                }
                case 1 -> {
                    int mpUse = (int) (player.nPoint.mpMax * player.playerSkill.skillSelect.manaUse / 100);
                    return player.nPoint.mp >= mpUse;
                }
                case 2 -> {
                    return player.nPoint.mp > 0;
                }
                default -> {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    public boolean canUseSkillWithCooldown(Player player) {
        return Util.canDoWithTime(player.playerSkill.skillSelect.lastTimeUseThisSkill,
                player.playerSkill.skillSelect.coolDown);
    }

    public void affterUseSkill(Player player, int skillId) {
        Intrinsic intrinsic = player.playerIntrinsic.intrinsic;
        switch (skillId) {
            case Skill.DICH_CHUYEN_TUC_THOI -> {
                if (intrinsic.id == 6) {
                    player.nPoint.dameAfter = intrinsic.param1;
                }
            }
            case Skill.THOI_MIEN -> {
                if (intrinsic.id == 7) {
                    player.nPoint.dameAfter = intrinsic.param1;
                }
            }
            case Skill.SOCOLA -> {
                if (intrinsic.id == 14) {
                    player.nPoint.dameAfter = intrinsic.param1;
                }
            }
            case Skill.TROI -> {
                if (intrinsic.id == 22) {
                    player.nPoint.dameAfter = intrinsic.param1;
                }
            }
        }
        setMpAffterUseSkill(player);
        setLastTimeUseSkill(player, skillId);
    }

    private void setMpAffterUseSkill(Player player) {
        if (player.playerSkill.skillSelect != null) {
            switch (player.playerSkill.skillSelect.template.manaUseType) {
                case 0 -> {
                    if (player.nPoint.mp >= player.playerSkill.skillSelect.manaUse) {
                        player.nPoint.setMp(player.nPoint.mp - player.playerSkill.skillSelect.manaUse);
                    }
                }
                case 1 -> {
                    int mpUse = (int) (player.nPoint.mpMax * player.playerSkill.skillSelect.manaUse / 100);
                    if (player.nPoint.mp >= mpUse) {
                        player.nPoint.setMp(player.nPoint.mp - mpUse);
                    }
                }
                case 2 ->
                    player.nPoint.setMp(0);
            }
            PlayerService.gI().sendInfoHpMpMoney(player);
        }
    }

    private void setLastTimeUseSkill(Player player, int skillId) {
        Intrinsic intrinsic = player.playerIntrinsic.intrinsic;
        int subTimeParam = 0;
        switch (skillId) {
            case Skill.TRI_THUONG -> {
                if (intrinsic.id == 10) {
                    subTimeParam = intrinsic.param1;
                }
            }
            case Skill.THAI_DUONG_HA_SAN -> {
                if (intrinsic.id == 3) {
                    subTimeParam = intrinsic.param1;
                }
            }
            case Skill.QUA_CAU_KENH_KHI -> {
                if (intrinsic.id == 4) {
                    subTimeParam = intrinsic.param1;
                }
            }
            case Skill.KHIEN_NANG_LUONG -> {
                if (intrinsic.id == 5 || intrinsic.id == 15 || intrinsic.id == 20) {
                    subTimeParam = intrinsic.param1;
                }
            }
            case Skill.MAKANKOSAPPO -> {
                if (intrinsic.id == 11) {
                    subTimeParam = intrinsic.param1;
                }
            }
            case Skill.DE_TRUNG -> {
                if (intrinsic.id == 12) {
                    subTimeParam = intrinsic.param1;
                }
            }
            case Skill.TU_SAT -> {
                if (intrinsic.id == 19) {
                    subTimeParam = intrinsic.param1;
                }
            }
            case Skill.HUYT_SAO -> {
                if (intrinsic.id == 21) {
                    subTimeParam = intrinsic.param1;
                }
            }
            case Skill.BIEN_HINH -> {
                subTimeParam = 1;
            }
        }
        int coolDown = player.playerSkill.skillSelect.coolDown;
        int time = coolDown * subTimeParam / 100;
        player.playerSkill.skillSelect.lastTimeUseThisSkill = System.currentTimeMillis() - time;
        if (subTimeParam != 0) {
            Service.getInstance().sendTimeSkill(player);
        }
    }

    private boolean canHsPlayer(Player player, Player plTarget) {
        if (plTarget == null) {
            return false;
        }
        if (plTarget.isBoss) {
            return false;
        }
        if (plTarget.typePk == ConstPlayer.PK_ALL) {
            return false;
        }
        if (plTarget.typePk == ConstPlayer.PK_PVP) {
            return false;
        }
        if (player.cFlag != 0) {
            if (plTarget.cFlag != 0 && plTarget.cFlag != player.cFlag) {
                return false;
            }
        } else if (plTarget.cFlag != 0) {
            return false;
        }
        return true;
    }

    private boolean canAttackPlayer(Player pl1, Player pl2) {
        if (pl2 != null && !pl1.isDie() && !pl2.isDie()) {
            if (pl1.typePk > 0 || pl2.typePk > 0) {
                return true;
            }
            if ((pl1.cFlag != 0 && pl2.cFlag != 0)
                    && (pl1.cFlag == 8 || pl2.cFlag == 8 || pl1.cFlag != pl2.cFlag)) {
                return true;
            }
            PVP pvp = PVPServcice.gI().findPvp(pl1);
            if (pvp != null) {
                if ((pvp.player1.equals(pl1) && pvp.player2.equals(pl2)
                        || (pvp.player1.equals(pl2) && pvp.player2.equals(pl1)))) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    private void sendPlayerAttackMob(Player plAtt, Mob mob) {
        Message msg;
        try {
            msg = new Message(54);
            msg.writer().writeInt((int) plAtt.id);
            msg.writer().writeByte(plAtt.playerSkill.skillSelect.skillId);
            msg.writer().writeByte(mob.id);
            Service.getInstance().sendMessAllPlayerInMap(plAtt, msg);
            msg.cleanup();

        } catch (IOException e) {

        }
    }

    // public void selectSkill(Player player, int skillId) {
    // Skill skillBefore = player.playerSkill.skillSelect;
    // for (Skill skill : player.playerSkill.skills) {
    // if (skill.skillId != -1 && skill.template.id == skillId) {
    // player.playerSkill.skillSelect = skill;
    // switch (skillBefore.template.id) {
    // case Skill.DRAGON, Skill.KAMEJOKO, Skill.DEMON, Skill.MASENKO,
    // Skill.LIEN_HOAN, Skill.GALICK, Skill.ANTOMIC -> {
    // switch (skill.template.id) {
    // case Skill.KAMEJOKO, Skill.DRAGON, Skill.DEMON, Skill.MASENKO,
    // Skill.LIEN_HOAN, Skill.GALICK, Skill.ANTOMIC -> {
    // }
    // }
    // }
    // }
    // break;
    // }
    // }
    // }
    public void selectSkill(Player player, int skillId) {
        for (Skill skill : player.playerSkill.skills) {
            if (skill.skillId != -1 && skill.template.id == skillId) {
                player.playerSkill.skillSelect = skill;
                break;
            }
        }
    }

    public void useSKillNotFocus(Player player, short skillID, short xPlayer, short yPlayer, byte dir, short x,
            short y) {
        try {
            SkillNotFocus skill = (SkillNotFocus) player.playerSkill.getSkillbyId(skillID);
            player.playerSkill.skillSelect = skill;
            if (canUseSkillWithMana(player) && canUseSkillWithCooldown(player)
                    && player.playerSkill.skillSelect != null) {
                if (player.location.x != xPlayer || player.location.y != yPlayer) {
                    return;
                }
                int skillRange = skill.getRange();
                int range = player.location.x + (dir == 1 ? skillRange : -skillRange);
                sendEffStartSkillNotFocus(player, skillID, dir, skill.getTimePre(), (byte) player.isFly);
                if (skillID != Skill.MAFUBA) {
                    Util.setTimeout(() -> {
                        long currentTime = System.currentTimeMillis();
                        skill.setTime(currentTime + skill.getTimeDame());
                        int period = skill.getTimeDame() / 15;
                        int count = 1;
                        sendEffEndUseSkillNotFocus(player, skillID, range, skill.getTimeDame(), 50, null);
                        while (!player.isDie() && currentTime < skill.getTime()) {
                            currentTime = System.currentTimeMillis();
                            int dameAttack = player.nPoint.getDameAttackSkillNotFocus() * (count == 15 ? 2 : 1);
                            count++;
                            for (Mob mob : player.zone.mobs) {
                                if (Math.abs(player.location.y - mob.location.y) <= 100) {
                                    if (dir == 1) {// phải
                                        if (mob.location.x >= xPlayer
                                                && Math.abs(player.location.x - mob.location.x) <= skillRange) {
                                            mob.injured(player, dameAttack, false);
                                        }
                                    } else {// trái
                                        if (mob.location.x <= xPlayer
                                                && Math.abs(player.location.x - mob.location.x) <= skillRange) {
                                            mob.injured(player, dameAttack, false);
                                        }
                                    }
                                }
                            }

                            for (Player p : player.zone.getHumanoids()) {
                                if (canAttackPlayer(player, p) && !p.equals(player)) {
                                    if (Math.abs(player.location.y - p.location.y) <= 100) {
                                        if (dir == 1) {// phải
                                            if (p.location.x >= xPlayer
                                                    && Math.abs(player.location.x - p.location.x) <= skillRange) {
                                                playerAttackPlayer(player, p, false, dameAttack);
                                                havePlayerAttack(p, false, dameAttack, 0);
                                            }
                                        } else {// trái
                                            if (p.location.x <= xPlayer
                                                    && Math.abs(player.location.x - p.location.x) <= skillRange) {
                                                playerAttackPlayer(player, p, false, dameAttack);
                                                havePlayerAttack(p, false, dameAttack, 0);
                                            }
                                        }
                                    }
                                }
                            }
                            affterUseSkill(player, skillID);
                            try {
                                TimeUnit.MILLISECONDS.sleep(period);
                            } catch (InterruptedException e) {
                            }
                        }
                    }, skill.getTimePre(), "Special Skill");
                } else {
                    Util.runThread(() -> {
                        try {
                            List<Object> list = new ArrayList<>();
                            TimeUnit.MILLISECONDS.sleep(skill.getTimePre());
                            int point = skill.point;
                            int distanceX = player.location.x + 50 * dir;
                            List<Mob> mobs = player.zone.mobs
                                    .stream()
                                    .filter(m -> !m.isDie() && Util.getDistance(distanceX, player.location.y,
                                            m.location.x, m.location.y) <= 100 + 15 * point)
                                    .sorted(Comparator.comparingInt(m -> Math.abs(distanceX - m.location.x)))
                                    .toList();
                            for (Mob mob : mobs) {
                                if (list.stream().filter(obj -> obj instanceof Mob).count() < skill.point) {
                                    list.add(mob);
                                }
                            }
                            List<Player> pls = player.zone.getHumanoids()
                                    .stream()
                                    .filter(m -> !m.equals(player) && !m.isDie()
                                            && Util.getDistance(distanceX, player.location.y, m.location.x,
                                                    m.location.y) <= 100 + 15 * point)
                                    .sorted(Comparator.comparingInt(m -> Math.abs(distanceX - m.location.x)))
                                    .toList();
                            for (Player pl : pls) {
                                if (list.stream().filter(obj -> obj instanceof Player).count() < skill.point) {
                                    list.add(pl);
                                }
                            }
                            sendEffEndUseSkillNotFocus(player, skillID, distanceX, skill.getTimeDame(),
                                    100 + 15 * skill.point, list);
                            affterUseSkill(player, skillID);
                            TimeUnit.MILLISECONDS.sleep(skill.getTimeDame());
                            for (Object obj : list) {
                                if (obj instanceof Mob mob) {
                                    mob.effectSkill.setMafuba(System.currentTimeMillis(), SkillUtil.getTimeMafuba(),
                                            skill.point, player);
                                    EffectSkillService.gI().sendMobAppearence(player, mob,
                                            point < 3 ? EffectSkillService.BINH_MAFUBA
                                                    : point <= 5 ? EffectSkillService.BINH_MAFUBA + 9
                                                            : EffectSkillService.BINH_MAFUBA - 9);
                                }
                                if (obj instanceof Player pl) {
                                    EffectSkillService.gI().setMafuba(pl, System.currentTimeMillis(),
                                            SkillUtil.getTimeMafuba(), player, point);
                                    Service.getInstance().Send_Caitrang(pl);
                                    ItemTimeService.gI().sendItemTime(pl,
                                            point < 3 ? 11236 : point <= 5 ? 11235 : 11237,
                                            SkillUtil.getTimeMafuba() / 1000);
                                }
                            }
                        } catch (InterruptedException e) {
                        }
                    }, "Thread mafuba");
                }
            }
        } catch (Exception e) {
            Log.error(SkillService.class, e);
        }
    }

    private void sendEffStartSkillNotFocus(Player player, short skillID, byte dir, int timePre, byte isFly) {
        byte gender = player.gender;
        try {
            // System.out.println("Send effect");
            Message m = new Message(-45);
            DataOutputStream ds = m.writer();
            ds.writeByte(20);
            ds.writeInt((int) player.id);// player id
            ds.writeShort(skillID);// skillId
            ds.writeByte(player.gender == 0 ? 1 : player.gender == 1 ? 3 : 2);// typeFrame
            ds.writeByte(dir);// huong nhin
            ds.writeShort(timePre);// time su dung
            ds.writeByte(isFly);// isfly
            // ds.writeByte(3);//typepaint
            if (player.inventory.itemsBody.get(7).isNotNullItem()) {
                for (int b = 1; b < player.inventory.itemsBody.get(7).itemOptions.size(); b++) {
                    if (player.inventory.itemsBody.get(7).itemOptions.get(b).optionTemplate.id == 231) {
                        player.inventory.itemsBody.get(7).itemOptions.get(b).param -= 1;
                        InventoryService.gI().sendItemBody(player);
                    }
                }
                if ((gender == 0 & player.inventory.itemsBody.get(7).template.id == 1285)
                        || (gender == 1 & player.inventory.itemsBody.get(7).template.id == 1287)
                        || (gender == 2 & player.inventory.itemsBody.get(7).template.id == 1289)) {
                    ds.writeByte(2);// typepaint
                } else {
                    ds.writeByte(3);// typepaint
                }
            } else {
                ds.writeByte(1);// typepaint
            }
            ds.writeByte(0);// typeItem
            ds.flush();
            Service.getInstance().sendMessAllPlayerInMap(player.zone, m);
            m.cleanup();
        } catch (IOException e) {
        }
    }

    private void sendEffEndUseSkillNotFocus(Player player, short skillID, int x, int time, int range,
            List<Object> hits) {
        Message m = new Message(-45);
        DataOutputStream ds = m.writer();
        byte gender = player.gender;
        try {
            ds.writeByte(21);
            ds.writeInt((int) player.id);
            ds.writeShort(skillID);
            ds.writeShort(x);
            ds.writeShort(player.location.y);
            ds.writeShort(time);
            ds.writeShort(range);
            if (player.inventory.itemsBody.get(7).isNotNullItem()) {
                for (int a = 1; a < player.inventory.itemsBody.get(7).itemOptions.size(); a++) {
                    if (player.inventory.itemsBody.get(7).itemOptions.get(a).optionTemplate.id == 231) {
                        player.inventory.itemsBody.get(7).itemOptions.get(a).param -= 1;
                        InventoryService.gI().sendItemBody(player);
                    }
                }
                if ((gender == 0 & player.inventory.itemsBody.get(7).template.id == 1285)
                        || (gender == 1 & player.inventory.itemsBody.get(7).template.id == 1287)
                        || (gender == 2 & player.inventory.itemsBody.get(7).template.id == 1289)) {
                    ds.writeByte(2);// typepaint
                } else {
                    ds.writeByte(3);// typepaint
                }
            } else {
                ds.writeByte(1);// typepaint
            }
            if (skillID == 26) {
                ds.writeByte(hits.size());
                for (Object obj : hits) {
                    if (obj instanceof Mob mob) {
                        ds.writeByte(0);
                        ds.writeByte(mob.id);
                    }
                    if (obj instanceof Player pl) {
                        ds.writeByte(1);
                        ds.writeInt((int) pl.id);
                    }
                }
            } else {
                ds.writeByte(0);
            }
            ds.writeByte(0);// type item
            ds.flush();
            Service.getInstance().sendMessAllPlayerInMap(player.zone, m);
            m.cleanup();
        } catch (IOException e) {
        }
    }

    public void useMakankosappo(Player player) {
        if (!canUseSkillWithCooldown(player)) {
            return;
        }
        player.playerSkill.prepareLaze = true;
        PlayerService.gI().hoiPhuc(player, 0, (int) (player.nPoint.mpMax / 2.5));
        sendPlayerPrepareSkill(player, 3000);
        Util.setTimeout(() -> {
            useSkill(player, ((Boss) player).plAttack, null, null);
        }, 3000, "Thread boss Use Skill Manakosappo");

    }

}
