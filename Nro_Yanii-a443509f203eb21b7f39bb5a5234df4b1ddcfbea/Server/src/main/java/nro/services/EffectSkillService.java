package nro.services;

import java.io.IOException;
import nro.models.mob.Mob;
import nro.models.player.Player;
import nro.models.skill.Skill;
import nro.server.io.Message;
import nro.utils.SkillUtil;
import nro.utils.Log;

import java.util.List;

////Luồng
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import nro.server.Manager;
import nro.services.func.RadaService;
import nro.utils.Util;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class EffectSkillService {

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static final byte TURN_ON_EFFECT = 1;
    public static final byte TURN_OFF_EFFECT = 0;
    public static final byte TURN_OFF_ALL_EFFECT = 2;

    public static final byte HOLD_EFFECT = 32;
    public static final byte SHIELD_EFFECT = 33;
    public static final byte HUYT_SAO_EFFECT = 39;
    public static final byte BLIND_EFFECT = 40;
    public static final byte SLEEP_EFFECT = 41;
    public static final byte STONE_EFFECT = 42;

    public static final short SOCOLA = 4133;
    public static final short BINH_MAFUBA = 11183;

    private static EffectSkillService i;

    private EffectSkillService() {

    }

    public static EffectSkillService gI() {
        if (i == null) {
            i = new EffectSkillService();
        }
        return i;
    }

    public void sendEffectbienhinh(Player player) {
        Skill skill = SkillUtil.getSkillbyId(player, Skill.BIEN_HINH);
        if (skill == null) {
            Service.getInstance().sendThongBao(player, "Errorrr");
            return;
        }
        Message msg;
        try {
            msg = new Message(-45);
            msg.writer().writeByte(6);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(97);
            Service.getInstance().sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void downBienHinh(Player player) {
        player.effectSkill.isBienHinh = false;
        player.effectSkill.levelBienHinh = 0;

        EffectSkillService.gI().sendEffectMonkey(player);
        RadaService.getInstance().RadarSetAura(player);

        Service.getInstance().Send_Caitrang(player);
        Service.getInstance().point(player);
    }

    public void setBienHinh(Player player) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {

        }

        Skill template = Manager.NCLASS.get(player.gender)
                .getSkillTemplate(player.playerSkill.skillSelect.template.id).skillss.stream()
                .filter(s -> s.point == player.playerSkill.skillSelect.point)
                .findFirst().orElse(null);

        if (template == null) {
            return;
        }

        int skillLevel = player.playerSkill.skillSelect.point;
        boolean lastLevel = player.effectSkill.levelBienHinh >= skillLevel - 1;

        player.effectSkill.isBienHinh = true;
        player.effectSkill.levelBienHinh = Math.min(skillLevel, player.effectSkill.levelBienHinh + 1);
        player.effectSkill.timeBienHinh = SkillUtil.getTimeBienHinh(lastLevel, template.coolDown);
        player.effectSkill.lastTimeBienHinh = System.currentTimeMillis();

        if (!lastLevel && template.coolDown > 0) {
            player.playerSkill.skillSelect.coolDown = template.coolDown * 5 / 100;
        } else {
            player.playerSkill.skillSelect.coolDown = template.coolDown;
        }
    }

    // hiệu ứng player dùng skill
    public void sendEffectUseSkill(Player player, byte skillId) {
        Skill skill = SkillUtil.getSkillbyId(player, skillId);
        Message msg;
        try {
            msg = new Message(-45);
            msg.writer().writeByte(8);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(skill.skillId);
            Service.getInstance().sendMessAnotherNotMeInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            Log.error(EffectSkillService.class, e);
        }
    }

    public void sendEffectPlayer(Player plUseSkill, Player plTarget, byte toggle, byte effect) {
        Message msg;
        try {
            msg = new Message(-124);
            msg.writer().writeByte(toggle); // 0: hủy hiệu ứng, 1: bắt đầu hiệu ứng
            msg.writer().writeByte(0); // 0: vào phần phayer, 1: vào phần mob
            if (toggle == TURN_OFF_ALL_EFFECT) {
                msg.writer().writeInt((int) plTarget.id);
            } else {
                msg.writer().writeByte(effect); // loại hiệu ứng
                msg.writer().writeInt((int) plTarget.id); // id player dính effect
                msg.writer().writeInt((int) plUseSkill.id); // id player dùng skill
            }
            Service.getInstance().sendMessAllPlayerInMap(plUseSkill, msg);
            msg.cleanup();
        } catch (Exception e) {
            Log.error(EffectSkillService.class, e);
        }
    }

    public void sendEffectMob(Player plUseSkill, Mob mobTarget, byte toggle, byte effect) {
        Message msg;
        try {
            msg = new Message(-124);
            msg.writer().writeByte(toggle); // 0: hủy hiệu ứng, 1: bắt đầu hiệu ứng
            msg.writer().writeByte(1); // 0: vào phần phayer, 1: vào phần mob
            msg.writer().writeByte(effect); // loại hiệu ứng
            msg.writer().writeByte(mobTarget.id); // id mob dính effect
            msg.writer().writeInt((int) plUseSkill.id); // id player dùng skill
            Service.getInstance().sendMessAllPlayerInMap(mobTarget.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
            Log.error(EffectSkillService.class, e);
        }
    }

    // Trói *********************************************************************
    // dừng sử dụng trói
    public void removeUseTroi(Player player) {
        if (player.effectSkill.mobAnTroi != null) {
            player.effectSkill.mobAnTroi.effectSkill.removeAnTroi();
        }
        if (player.effectSkill.plAnTroi != null) {
            removeAnTroi(player.effectSkill.plAnTroi);
        }
        player.effectSkill.useTroi = false;
        player.effectSkill.mobAnTroi = null;
        player.effectSkill.plAnTroi = null;
        sendEffectPlayer(player, player, TURN_OFF_EFFECT, HOLD_EFFECT);
    }

    // hết thời gian bị trói
    private void removeAnTroi(Player player) {
        if (player != null && player.effectSkill != null) {
            player.effectSkill.anTroi = false;
            player.effectSkill.plTroi = null;
            sendEffectPlayer(player, player, TURN_OFF_EFFECT, HOLD_EFFECT);
        }
    }

    public void setAnTroi(Player player, Player plTroi, long lastTimeAnTroi, int timeAnTroi) {
        player.effectSkill.anTroi = true;
        // player.effectSkill.lastTimeAnTroi = lastTimeAnTroi;
        // player.effectSkill.timeAnTroi = timeAnTroi;
        player.effectSkill.plTroi = plTroi;
    }

    public void setUseTroi(Player player, long lastTimeTroi, int timeTroi) {
        player.effectSkill.useTroi = true;
        player.effectSkill.lastTimeTroi = lastTimeTroi;
        player.effectSkill.timeTroi = timeTroi;
    }
    // **************************************************************************

    // Thôi miên ****************************************************************
    // thiết lập thời gian bắt đầu bị thôi miên
    public void setThoiMien(Player player, long lastTimeThoiMien, int timeThoiMien) {
        player.effectSkill.isThoiMien = true;
        player.effectSkill.lastTimeThoiMien = lastTimeThoiMien;
        player.effectSkill.timeThoiMien = timeThoiMien;
    }

    // hết hiệu ứng thôi miên
    public void removeThoiMien(Player player) {
        player.effectSkill.isThoiMien = false;
        sendEffectPlayer(player, player, TURN_OFF_EFFECT, SLEEP_EFFECT);
    }

    // **************************************************************************
    // Thái dương hạ san &&&&****************************************************
    // player ăn choáng thái dương hạ san
    public void startStun(Player player, long lastTimeStartBlind, int timeBlind) {
        player.effectSkill.lastTimeStartStun = lastTimeStartBlind;
        player.effectSkill.timeStun = timeBlind;
        player.effectSkill.isStun = true;
        sendEffectPlayer(player, player, TURN_ON_EFFECT, BLIND_EFFECT);
    }

    // kết thúc choáng thái dương hạ san
    public void removeStun(Player player) {
        player.effectSkill.isStun = false;
        sendEffectPlayer(player, player, TURN_OFF_EFFECT, BLIND_EFFECT);
    }
    // **************************************************************************

    // Socola *******************************************************************
    // player biến thành socola
    public void setSocola(Player player, long lastTimeSocola, int timeSocola) {
        player.effectSkill.lastTimeSocola = lastTimeSocola;
        player.effectSkill.timeSocola = timeSocola;
        player.effectSkill.isSocola = true;
        player.effectSkill.countPem1hp = 0;
    }

    public void setMaPhongBa(Player player, long lastTimeMaPhongBa, int timeMaPhongBa, int dameMaFuBa) {
        player.effectSkill.lastTimeMaPhongBa = lastTimeMaPhongBa;
        player.effectSkill.timeMaPhongBa = timeMaPhongBa;
        player.dameMaFuBa = dameMaFuBa;
        player.effectSkill.isMaPhongBa = true;
    }

    // player trở lại thành người
    public void removeSocola(Player player) {
        player.effectSkill.isSocola = false;
        Service.getInstance().Send_Caitrang(player);
    }
    // quái biến thành socola

    public void sendMobAppearence(Player player, Mob mob, int icon) {
        Message msg;
        try {
            msg = new Message(-112);
            msg.writer().writeByte(1);
            msg.writer().writeByte(mob.id); // mob id
            msg.writer().writeShort(icon); // icon socola
            Service.getInstance().sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (IOException e) {
            Log.error(EffectSkillService.class, e);
        }
    }

    public void removeMaPhongBa(Player player) {
        player.effectSkill.lastTimeMaPhongBa = 0;
        player.effectSkill.timeMaPhongBa = 0;
        player.effectSkill.isMaPhongBa = false;
        player.dameMaFuBa = 0;
        Service.getInstance().Send_Caitrang(player);
    }

    // quái biến thành socola
    public void sendMobToSocola(Player player, Mob mob, int timeSocola) {
        Message msg;
        try {
            msg = new Message(-112);
            msg.writer().writeByte(1);
            msg.writer().writeByte(mob.id); // mob id
            msg.writer().writeShort(4133); // icon socola
            Service.getInstance().sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
            mob.effectSkill.setSocola(System.currentTimeMillis(), timeSocola);
        } catch (Exception e) {
            Log.error(EffectSkillService.class, e);
        }
    }
    // **************************************************************************

    // **************************************************************************
    // Dịch chuyển tức thời *****************************************************
    public void setBlindDCTT(Player player, long lastTimeDCTT, int timeBlindDCTT) {
        player.effectSkill.isBlindDCTT = true;
        player.effectSkill.lastTimeBlindDCTT = lastTimeDCTT;
        player.effectSkill.timeBlindDCTT = timeBlindDCTT;
    }

    public void removeBlindDCTT(Player player) {
        player.effectSkill.isBlindDCTT = false;
        sendEffectPlayer(player, player, TURN_OFF_EFFECT, BLIND_EFFECT);
    }
    // **************************************************************************

    // Huýt sáo *****************************************************************
    // Hưởng huýt sáo
    public void setStartHuytSao(Player player, int tiLeHP) {
        player.effectSkill.tiLeHPHuytSao = tiLeHP;
        player.effectSkill.lastTimeHuytSao = System.currentTimeMillis();
    }

    // Hết hiệu ứng huýt sáo
    public void removeHuytSao(Player player) {
        player.effectSkill.tiLeHPHuytSao = 0;
        sendEffectPlayer(player, player, TURN_OFF_EFFECT, HUYT_SAO_EFFECT);
        Service.getInstance().point(player);
        Service.getInstance().Send_Info_NV(player);
    }

    // **************************************************************************
    // Biến khỉ *****************************************************************
    // Bắt đầu biến khỉ
    public void setIsMonkey(Player player) {
        Util.setTimeout(() -> {
            int timeMonkey = SkillUtil.getTimeMonkey(player.playerSkill.skillSelect.point);
            if (player.setClothes.cadic == 5) {
                timeMonkey *= 5;
            }
            player.effectSkill.isMonkey = true;
            player.effectSkill.timeMonkey = timeMonkey;
            player.effectSkill.lastTimeUpMonkey = System.currentTimeMillis();
            player.effectSkill.levelMonkey = (byte) player.playerSkill.skillSelect.point;
            Service.getInstance().sendSpeedPlayer(player, 6);
            Service.getInstance().Send_Caitrang(player);
            Service.getInstance().point(player);
        }, 2000, "Thread set bien khi");
    }

    public void monkeyDown(Player player) {
        player.effectSkill.isMonkey = false;
        player.effectSkill.levelMonkey = 0;
        if (player.nPoint.hp > player.nPoint.hpMax) {
            player.nPoint.setHp(player.nPoint.hpMax);
        }
        sendEffectEndCharge(player);
        sendEffectMonkey(player);
        Service.getInstance().setNotMonkey(player);
        Service.getInstance().Send_Caitrang(player);
        Service.getInstance().point(player);
        PlayerService.gI().sendInfoHpMp(player);
        Service.getInstance().Send_Info_NV(player);
    }
    // **************************************************************************
    // Tái tạo năng lượng *******************************************************

    public void startCharge(Player player) {
        if (!player.effectSkill.isCharging) {
            player.effectSkill.isCharging = true;
            sendEffectCharge(player);
        }
    }

    public void stopCharge(Player player) {
        player.effectSkill.countCharging = 0;
        player.effectSkill.isCharging = false;
        ;
        sendEffectStopCharge(player);

    }

    // **************************************************************************
    // Khiên năng lượng *********************************************************
    public void setStartShield(Player player) {
        player.effectSkill.isShielding = true;
        player.effectSkill.lastTimeShieldUp = System.currentTimeMillis();
        player.effectSkill.timeShield = SkillUtil.getTimeShield(player.playerSkill.skillSelect.point);
    }

    public void removeShield(Player player) {
        player.effectSkill.isShielding = false;
        sendEffectPlayer(player, player, TURN_OFF_EFFECT, SHIELD_EFFECT);
    }

    public void breakShield(Player player) {
        removeShield(player);
        Service.getInstance().sendThongBao(player, "Khiên năng lượng đã bị vỡ!");
        ItemTimeService.gI().removeItemTime(player, 3784);
    }

    // **************************************************************************
    public void sendEffectBlindThaiDuongHaSan(Player plUseSkill, List<Player> players, List<Mob> mobs, int timeStun) {
        Message msg;
        try {
            msg = new Message(-45);
            msg.writer().writeByte(0);
            msg.writer().writeInt((int) plUseSkill.id);
            msg.writer().writeShort(plUseSkill.playerSkill.skillSelect.skillId);
            msg.writer().writeByte(mobs.size());
            for (Mob mob : mobs) {
                msg.writer().writeByte(mob.id);
                msg.writer().writeByte(timeStun / 1000);
            }
            msg.writer().writeByte(players.size());
            for (Player pl : players) {
                msg.writer().writeInt((int) pl.id);
                msg.writer().writeByte(timeStun / 1000);
            }
            Service.getInstance().sendMessAllPlayerInMap(plUseSkill, msg);
            msg.cleanup();
        } catch (Exception e) {
            Log.error(EffectSkillService.class, e);
        }
    }

    // hiệu ứng bắt đầu gồng
    public void sendEffectStartCharge(Player player) {
        Skill skill = SkillUtil.getSkillbyId(player, Skill.TAI_TAO_NANG_LUONG);
        Message msg;
        try {
            msg = new Message(-45);
            msg.writer().writeByte(6);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(skill.skillId);
            Service.getInstance().sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            Log.error(EffectSkillService.class, e);
        }
    }

    // hiệu ứng đang gồng
    public void sendEffectCharge(Player player) {
        Skill skill = SkillUtil.getSkillbyId(player, Skill.TAI_TAO_NANG_LUONG);
        Message msg;
        try {
            msg = new Message(-45);
            msg.writer().writeByte(1);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(skill.skillId);
            Service.getInstance().sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            Log.error(EffectSkillService.class, e);
        }
    }

    // dừng gồng
    public void sendEffectStopCharge(Player player) {
        try {
            Message msg = new Message(-45);
            msg.writer().writeByte(3);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(-1);
            Service.getInstance().sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            Log.error(EffectSkillService.class, e);
        }
    }

    // hiệu ứng nổ kết thúc gồng
    public void sendEffectEndCharge(Player player) {
        Message msg;
        try {
            msg = new Message(-45);
            msg.writer().writeByte(5);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(player.playerSkill.skillSelect.skillId);
            Service.getInstance().sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            Log.error(EffectSkillService.class, e);
        }
    }

    // hiệu ứng biến khỉ
    public void sendEffectMonkey(Player player) {
        try {
            Skill skill = SkillUtil.getSkillbyId(player, Skill.BIEN_KHI);
            Message msg;

            msg = new Message(-45);
            msg.writer().writeByte(6);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(skill.skillId);
            Service.getInstance().sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {

        }
    }

    public void sendEffectBienHinh(Player player) {
        Skill skill = SkillUtil.getSkillbyId(player, Skill.BIEN_HINH);
        Message msg;
        try {
            msg = new Message(-45);
            msg.writer().writeByte(6);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(skill.skillId);
            Service.getInstance().sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            Log.error(EffectSkillService.class, e);
        }
    }

    public void setMafuba(Player pl, long currentTimeMillis, int timeMafuba, Player plUseSkill, int point) {
        pl.effectSkill.lastTimeMafuba = currentTimeMillis;
        pl.effectSkill.timeMafuba = timeMafuba;
        pl.effectSkill.isMafuba = true;
        pl.effectSkill.lastTimeBurn = System.currentTimeMillis();
        pl.effectSkill.plUseSkillMafuba = plUseSkill;
        pl.effectSkill.levelMafuba = point;
        pl.effectSkill.countPem1hp = 0;
    }

    public void removeMafuba(Player player) {
        player.effectSkill.isMafuba = false;
        player.effectSkill.plUseSkillMafuba = null;
        Service.getInstance().Send_Caitrang(player);
    }
}
