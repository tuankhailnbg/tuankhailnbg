package nro.models.mob;

import nro.utils.Util;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class MobPoint {

    public final Mob mob;
    public int hp;
    public int maxHp;
    public int dame;

    public int clanMemHighestDame; // dame lớn nhất trong clan
    public int clanMemHighestHp; // hp lớn nhất trong clan

    public int xHpForDame = 50; // dame gốc = highesHp / xHpForDame;
    public int xDameForHp = 10; // hp gốc = xDameForHp * highestDame;

    public MobPoint(Mob mob) {
        this.mob = mob;
    }

    public int getHpFull() {
        return maxHp;
    }

    public void setHpFull(int hp) {
        maxHp = hp;
    }

    public int getHP() {
        return hp;
    }

    public void setHP(int hp) {
        if (this.hp < 0) {
            this.hp = 0;
        } else {
            this.hp = hp;
        }
    }

    public int getDameAttack() {
        return this.dame != 0 ? this.dame + Util.nextInt(-(this.dame / 100), (this.dame / 100))
                : this.getHpFull() * Util.nextInt(mob.pDame - 1, mob.pDame + 1) / 100
                        + Util.nextInt(-(mob.level * 10), mob.level * 10);
    }
}
