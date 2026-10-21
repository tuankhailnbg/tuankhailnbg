/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nro.models.boss.cdrd;

import nro.models.boss.BossData;
import nro.models.map.dungeon.SnakeRoad;
import nro.models.player.Player;

/**
 *
 * @author 💖 Nro Yanii 💖
 */
public class Nadic extends CBoss {

    public Nadic(long id, short x, short y, SnakeRoad dungeon, BossData data) {
        super(id, x, y, dungeon, data);
    }

    @Override
    protected boolean useSpecialSkill() {
        return false;
    }

    @Override
    public void rewards(Player pl) {

    }

    @Override
    public void idle() {

    }

    @Override
    public void checkPlayerDie(Player pl) {

    }

    @Override
    public void changeToAttack() {
        chat("Ha ha ha");
        super.changeToAttack();
    }

    @Override
    public void initTalk() {
        this.textTalkBefore = new String[] {};
        this.textTalkMidle = new String[] { "Ốp la...Xay da da!" };
        this.textTalkAfter = new String[] { "Sếp hãy giết nó, trả thù cho em!" };
    }

}
