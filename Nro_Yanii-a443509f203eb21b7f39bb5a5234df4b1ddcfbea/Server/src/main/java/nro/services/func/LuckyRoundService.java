/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nro.services.func;

import nro.models.player.Player;
import nro.server.io.Message;
import nro.services.func.lr.LuckyRoundGem;
import nro.services.func.lr.LuckyRoundGold;

import java.io.IOException;

/**
 *
 * @author 💖 Nro Yanii 💖
 */
public class LuckyRoundService {

    private static LuckyRoundService i;

    public static LuckyRoundService gI() {
        if (i == null) {
            i = new LuckyRoundService();
        }
        return i;
    }

    public static final byte USING_GOLD = 0;
    public static final byte USING_GEM = 2;

    public void openCrackBallUI(Player player, byte type) {
        player.iDMark.setTypeLuckyRound(type);
        switch (type) {
            case USING_GOLD:
                LuckyRoundGold.gI().openUI(player, type);
                break;
            case USING_GEM:
                LuckyRoundGem.gI().openUI(player, type);
                break;
        }
    }

    public void readOpenBall(Player player, Message msg) {
        try {
            byte type = msg.reader().readByte();
            byte count = 0;
            if (msg.reader().available() > 0) {
                count = msg.reader().readByte();
            }
            if (count > 0) {
                switch (player.iDMark.getTypeLuckyRound()) {
                    case USING_GOLD:
                        LuckyRoundGold.gI().payAndGetStarted(player, count);
                        break;

                    case USING_GEM:
                        LuckyRoundGem.gI().payAndGetStarted(player, count);
                        break;
                }
            } else {
                openCrackBallUI(player, player.iDMark.getTypeLuckyRound());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
