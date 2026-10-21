package nro.models.player;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import nro.models.boss.Boss;
import nro.models.mob.Mob;
import nro.services.EffectSkillService;
import nro.services.ItemTimeService;
import nro.services.PlayerService;
import nro.services.Service;
import nro.utils.Util;

/**
 *
 * @author 💖 Nro Yanii 💖
 *
 *
 */
public class EffectSkill {

    private Player player;

    private Boss boss;

    // thái dương hạ san
    public boolean isStun;
    public long lastTimeStartStun;
    public int timeStun;

    // khiên năng lượng
    public boolean isShielding;
    public long lastTimeShieldUp;
    public int timeShield;

    // biến khỉ
    public boolean isMonkey;
    public byte levelMonkey;
    public long lastTimeUpMonkey;
    public int timeMonkey;

    // tái tạo năng lượng
    public boolean isCharging;
    public int countCharging;

    // huýt sáo
    public int tiLeHPHuytSao;
    public long lastTimeHuytSao;

    // thôi miên
    public boolean isThoiMien;
    public long lastTimeThoiMien;
    public int timeThoiMien;

    // trói
    public boolean useTroi;
    public boolean anTroi;
    public long lastTimeTroi;
    // public long lastTimeAnTroi;
    public int timeTroi;
    // public int timeAnTroi;
    public Player plTroi;
    public Player plAnTroi;
    public Mob mobAnTroi;

      // MIEN TROI
    public boolean mienTroi;
    
    // dịch chuyển tức thời
    public boolean isBlindDCTT;
    public long lastTimeBlindDCTT;
    public int timeBlindDCTT;

    // socola
    public boolean isSocola;
    public long lastTimeSocola;
    public int timeSocola;
    public int countPem1hp;
    public long lastTimeMafuba;
    public int timeMafuba;
    public boolean isMafuba;
    public Player plUseSkillMafuba;
    public int levelMafuba;

    // ma phong ba
    public boolean isMaPhongBa = false;
    public long lastTimeMaPhongBa;
    public int timeMaPhongBa;

    public long lastTimeSubHP;

    // Bien hinh
    public boolean isBienHinh;
    public long lastTimeBienHinh;
    public int timeBienHinh;
    public int levelBienHinh = 0;

    // BiNgo
    public boolean isBiNgo;
    public long lastBiNgo;
    // BiNgo

    public EffectSkill(Player player) {
        this.player = player;
    }

    public void removeSkillEffectWhenDie() {
        if (isMonkey) {
            EffectSkillService.gI().monkeyDown(player);
        }
        if (isShielding) {
            EffectSkillService.gI().removeShield(player);
            ItemTimeService.gI().removeItemTime(player, 3784);
        }
        if (useTroi) {
            EffectSkillService.gI().removeUseTroi(this.player);
        }
        if (isStun) {
            EffectSkillService.gI().removeStun(this.player);
        }
        if (isThoiMien) {
            EffectSkillService.gI().removeThoiMien(this.player);
        }
        if (isBlindDCTT) {
            EffectSkillService.gI().removeBlindDCTT(this.player);
        }
        if (isMafuba) {
            EffectSkillService.gI().removeMafuba(this.player);
        }
    }

    public void update() {
        if (isBienHinh && (Util.canDoWithTime(lastTimeBienHinh, timeBienHinh))) {
            EffectSkillService.gI().downBienHinh(player);
        }
        if (isMonkey && (Util.canDoWithTime(lastTimeUpMonkey, timeMonkey))) {
            EffectSkillService.gI().monkeyDown(player);
        }
        if (isShielding && (Util.canDoWithTime(lastTimeShieldUp, timeShield))) {
            EffectSkillService.gI().removeShield(player);
        }
        if (useTroi && Util.canDoWithTime(lastTimeTroi, timeTroi)
                || plAnTroi != null && plAnTroi.isDie()
                || useTroi && isHaveEffectSkill()) {
            EffectSkillService.gI().removeUseTroi(this.player);
        }
        // if (anTroi && (Util.canDoWithTime(lastTimeAnTroi, timeAnTroi) ||
        // player.isDie())) {
        // EffectSkillService.gI().removeAnTroi(this.player);
        // }
        if (isStun && Util.canDoWithTime(lastTimeStartStun, timeStun)) {
            EffectSkillService.gI().removeStun(this.player);
        }
        if (isThoiMien && (Util.canDoWithTime(lastTimeThoiMien, timeThoiMien))) {
            EffectSkillService.gI().removeThoiMien(this.player);
        }
        if (isBlindDCTT && (Util.canDoWithTime(lastTimeBlindDCTT, timeBlindDCTT))) {
            EffectSkillService.gI().removeBlindDCTT(this.player);
        }
        if (isSocola && (Util.canDoWithTime(lastTimeSocola, timeSocola))) {
            EffectSkillService.gI().removeSocola(this.player);
        }
        if (isMaPhongBa) {
            if (Util.canDoWithTime(lastTimeMaPhongBa, timeMaPhongBa)) {
                EffectSkillService.gI().removeMaPhongBa(this.player);
            }
        }
        if (tiLeHPHuytSao != 0 && Util.canDoWithTime(lastTimeHuytSao, 30000)) {
            EffectSkillService.gI().removeHuytSao(this.player);
        }
        // BiNgo
        if (isBiNgo && Util.canDoWithTime(lastBiNgo, 30_000)) {
            isBiNgo = false;
            this.player.injured(player, player.nPoint.hpMax, true, false);
            Service.gI().Send_Caitrang(player);
            ItemTimeService.gI().sendAllItemTime(player);
        }
        // BiNgo
        handleDmgByEffect();
    }

    public long lastTimeBurn;

    private void handleDmgByEffect() {
        if (isMafuba && Util.canDoWithTime(lastTimeBurn, 1000)) {
            lastTimeBurn = System.currentTimeMillis();
            Player plUseSkill = this.player.effectSkill.plUseSkillMafuba;
            this.player.injured(plUseSkill, plUseSkill.nPoint.hpMax * 10 / 100, true, false);
            PlayerService.gI().sendInfoHp(player);
            Service.getInstance().Send_Info_NV(player);
            // SkillService.gI().havePlayerAttack(this.player, false,
            // plUseSkill.nPoint.hpMax * 10 / 100, 0);
        }
    }

    public boolean isHaveEffectSkill() {
        return isStun || isBlindDCTT || anTroi || isThoiMien;
    }

    public void dispose() {
        this.player = null;
    }
}
