package nro.models.boss.broly;

import java.util.ArrayList;
import nro.consts.ConstRatio;
import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.map.Zone;
import nro.models.player.Player;
import nro.models.skill.Skill;
import nro.services.EffectSkillService;
import nro.services.Service;
import nro.services.SkillService;
import nro.services.func.ChangeMapService;
import nro.utils.SkillUtil;
import nro.utils.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import nro.models.player.Pet;
import nro.services.PetService;
import nro.services.PlayerService;
import nro.utils.Log;
import org.apache.commons.lang3.time.StopWatch;

public class Broly extends Boss {

    static final int MAX_HP = 50000000;

    private final Map angryPlayers;
    private final List<Player> playersAttack;
    private boolean angry;
    protected StopWatch stopWatch;

    public Broly() {
        super(BossData.BROLY);
        this.angryPlayers = new HashMap();
        this.playersAttack = new ArrayList<>();
        stopWatch = new StopWatch();
    }

    @Override
    public void initTalk() {
        this.textTalkAfter = new String[]{"Các ngươi chờ đấy, ta sẽ quay lại sau"};
    }

    @Override
    public void attack() {
        if (!canAttackWithTime(400)) {
            return;
        }
        brolyAngry();
        Player pl = getPlayerAttack();
        if (pl != null && pl.location != null) {
            if (useSpecialSkill()) {
                return;
            }
            byte dir = (byte) (this.location.x - pl.location.x < 0 ? 1 : -1);
            if (Util.getDistance(this, pl) <= Skill.RANGE_ATTACK_CHIEU_DAM) {
                this.playerSkill.skillSelect = this.getSkillAttack();
                if (Util.isTrue(15, ConstRatio.PER100)) {
                    int x = Util.isTrue(70, 100) ? pl.location.x + Util.nextInt(30, 40) * dir : pl.location.x + Util.nextInt(60, 70) * dir;
                    goToXY(x, Util.nextInt(6) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(20, 40), false);
                }
                SkillService.gI().useSkill(this, pl, null, null);
                checkPlayerDie(pl);
            } else if (Util.getDistance(this, pl) <= Skill.RANGE_ATTACK_CHIEU_CHUONG) {
                this.playerSkill.skillSelect = SkillUtil.getRangeSkill(this.playerSkill.skills);
                if (Util.isTrue(10, ConstRatio.PER100)) {
                    int x = Util.isTrue(70, 100) ? pl.location.x + Util.nextInt(60, 70) * dir : pl.location.x + Util.nextInt(30, 40) * -dir;
                    goToXY(x, Util.nextInt(5) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(30, 40), false);
                }
                SkillService.gI().useSkill(this, pl, null, null);
                checkPlayerDie(pl);
            } else {
                goToPlayer(pl, false);
            }
        } else {
            if (isNotFullHp()) {
                useSpecialSkill();
            }
        }
    }

    private void brolyAngry() {
        for (Player player : this.zone.getPlayers()) {
            if (!stopWatch.isStarted()) {
                stopWatch.start();
            }
            if (!playersAttack.contains(player)) {
                if (!angry) {
                    handleNotAngryCase(player);
                } else {
                    handleAngryCase(player);
                }
            }
        }
        if (playersAttack.isEmpty() && angry) {
            resetAngryState();
        }
    }

    private void handleNotAngryCase(Player player) {
        if (Util.getDistance(this, player) <= 100 && !player.isDie()) {
            this.chat("Tránh xa ta ra đừng để ta nóng!");
            EffectSkillService.gI().startCharge(this);
            if (stopWatch.getTime(TimeUnit.SECONDS) >= 5) {
                this.angry = true;
                this.chat("Ngươi làm ta tức giận rồi đó");
                playersAttack.add(player);
            }
        } else {
            stopWatch.reset();
        }
    }

    private void handleAngryCase(Player player) {
        if (Util.getDistance(this, player) <= 100 && !player.isDie()) {
            this.chat("Dám lại gần ta, ta giận rồi đấy");
            playersAttack.add(player);
        }
    }

    private void resetAngryState() {
        angry = false;
        stopWatch.reset();
    }

    @Override
    protected void changeBossAppear() {
        try {
            sendEffectCharge(this);
            int currentIndex = this.typeBosses.indexOf((int) this.bossType);
            Integer nextType = this.typeBosses.get(currentIndex + 1);
            this.bossType = nextType;
            this.data = nextLevel.get(nextType);
            TimeUnit.MILLISECONDS.sleep(30000);
            init();
            PlayerService.gI().hoiPhuc(this, this.nPoint.hpMax, 0);
            if (this.bossType == BossFactory.SUPER_BROLY) {
                PetService.gI().createNewPet(this, false, false, false, false, false, false, (byte) Util.nextInt(0, 2));
            }
            Service.getInstance().sendPlayerInfo(this);
            EffectSkillService.gI().sendEffectEndCharge(this);
            this.changeToAttack();
        } catch (InterruptedException e) {
        
        }
    }

    @Override
    protected int nextLevel(int damage, Player plAtt) {
        if (damage >= this.nPoint.hp && isNotLastTypeBoss() && this.nPoint.hpMax >= 1500000) {
            canUpLevel = true;
            playersAttack.clear();
            rewards(plAtt);
            notifyPlayeKill(plAtt);
            this.changeToIdle();
            return this.nPoint.hp - 1;
        }
        return damage;
    }

    @Override
    protected int handleDamageForSpecialBoss(int damage, Player plAtt) {
        if (plAtt != null) {
            int skill = plAtt.playerSkill.skillSelect.template.id;
            switch (skill) {
                case Skill.KAMEJOKO, Skill.ANTOMIC, Skill.MASENKO, Skill.LIEN_HOAN -> {
                    damage = 1;
                    Service.getInstance().chat(plAtt, "Trời ơi, chưởng hoàn toàn vô hiệu lực với hắn..");
                }
                case Skill.TU_SAT, Skill.QUA_CAU_KENH_KHI, Skill.MAKANKOSAPPO -> {

                }
                default -> {
                    if (damage > this.nPoint.hpMax / 100) {
                        damage = this.nPoint.hpMax / 100;
                    }
                }
            }
            addPlayerAttack(plAtt);
        }
        resetPoint();
        return damage;
    }

    private int maxCountResetPoint = Util.nextInt(30, 35);
    private int countResetPoint;

    private void resetPoint() {
        if (this.countResetPoint++ >= maxCountResetPoint && this.nPoint.hpMax < MAX_HP && this.nPoint.getCurrPercentHP() < 80) {
         //   System.out.println("a");
            double min = Util.nextDouble(this.nPoint.hpMax * 0.14, this.nPoint.hpMax * 0.7);
            double max = Util.nextDouble(this.nPoint.hpMax * 0.2, this.nPoint.hpMax * 0.3);
            this.nPoint.hpMax += this.nPoint.getCurrPercentHP() < 50 ? max : min;
            if (this.nPoint.hpMax > MAX_HP) {
                this.nPoint.hpMax = MAX_HP;
            }
            if (this.bossType == BossFactory.BROLY) {
                this.nPoint.dame = this.nPoint.hpMax / 25;
            }
            Service.getInstance().Send_Info_NV(this);
            maxCountResetPoint = Util.nextInt(30, 35);
            countResetPoint = 0;
        }
    }
    int countChangePlayer;

    @Override
    public Player getPlayerAttack() {
        if (!playersAttack.isEmpty() && this.bossType == BossFactory.BROLY) {
            Player pl = playersAttack.get(0);
            if (pl == null) {
                playersAttack.remove(pl);
                return null;
            }
            if (pl.isDie() || pl.zone != this.zone) {
             //   System.out.println("Remove2: " + pl.name);
                playersAttack.remove(pl);
                return null;
            }
            return pl;
        } else {
            try {
                super.getPlayerAttack();
            } catch (Exception e) {
               
            }
        }
        return plAttack;
    }

    private void addPlayerAttack(Player plAtt) {
        boolean haveInList = false;
        for (Player pl : playersAttack) {
            if (pl.equals(plAtt)) {
                haveInList = true;
                break;
            }
        }
        if (!haveInList) {
            if (plAtt.isPet) {
                Player master = ((Pet) plAtt).master;
                playersAttack.add(master);
            } else {
                playersAttack.add(plAtt);
            }
            Service.getInstance().chat(this, "Mi làm ta nổi giận rồi "
                    + plAtt.name.replaceAll("$", "").replaceAll("#", ""));
        }
    }

    @Override
    protected boolean handleUseSpecialSkill(boolean allow) {
        if (Util.isTrue(70, 100)) {
            Skill skill = this.skillsSpecial.get(Util.nextInt(0, this.skillsSpecial.size() - 1));
            this.playerSkill.skillSelect = skill;
            if (SkillService.gI().canUseSkillWithCooldown(this)) {
                Service.getInstance().bossUseSkillNotFocus(this, skill);
                return true;
            }
        }
        return false;
    }

    @Override
    public void checkPlayerDie(Player pl) {
        if (pl != null && pl.isDie()) {
            Service.getInstance().chat(this, "Chừa nha " + pl.name + " động vào ta chỉ có chết.");
            this.angryPlayers.put(pl, 0);
            this.playersAttack.remove(pl);
            this.plAttack = null;
        }
    }

    @Override
    public void joinMap() {
        this.zone = getMapCanJoin(mapJoin[Util.nextInt(0, mapJoin.length - 1)]);
        int x = Util.nextInt(50, this.zone.map.mapWidth - 50);
        ChangeMapService.gI().changeMap(this, this.zone, x, this.zone.map.yPhysicInTop(x, 0));
    }

    @Override
    public void respawn() {
        super.respawn();
        this.plAttack = null;
        if (this.playersAttack != null) {
            this.playersAttack.clear();
        }
        if (this.angryPlayers != null) {
            this.angryPlayers.clear();
        }
    }

    @Override
    public Zone getMapCanJoin(int mapId) {
        return super.getMapCanJoin(mapId);
    }

    @Override
    protected void notifyPlayeKill(Player player) {
    }

    @Override
    public void rewards(Player pl) {
        if (this.bossType == BossFactory.SUPER_BROLY && pl != null && pl.isPl() && pl.pet == null) {
            PetService.gI().createNewPet(pl, false, false, false, false, false, false, (byte) Util.nextInt(0, 2));
        }
    }

}
