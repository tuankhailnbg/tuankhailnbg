package nro.models.pvp;

import nro.consts.ConstAchive;
import nro.consts.ConstPlayer;
import nro.models.player.Player;
import nro.services.PlayerService;
import nro.services.func.PVPServcice;
import nro.utils.Util;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public abstract class PVP {

    public static final byte TYPE_LEAVE_MAP = 7;
    public static final byte TYPE_DIE = 5;
    public static final byte TYPE_LOWER_HP = 2;

    protected static final byte TYPE_PVP_CHALLENGE = 7;
    protected static final byte TYPE_PVP_REVENGE = 5;
    protected static final byte TYPE_PVP_MARTIAL_CONGRESS = 2;

    public byte typePVP;
    public Player player1;
    public Player player2;

    public boolean start;
    public long lastTimeStart;
    public int timeLeftPVP;

    public void start() {
        this.lastTimeStart = System.currentTimeMillis();
        this.timeLeftPVP = -1;
        this.start = true;
    }

    public void update() {
        if (this.timeLeftPVP != -1 && this.start && Util.canDoWithTime(lastTimeStart, timeLeftPVP)) {
            if (player1.nPoint.getCurrPercentHP() < player2.nPoint.getCurrPercentHP()) {
                finishPVP(player1, TYPE_LOWER_HP);
            }
        }
    }

    public void finishPVP(Player plLose, byte typeWin) {
        if (plLose.typePk != ConstPlayer.NON_PK) {
            Player plWin = player1.equals(plLose) ? player2 : player1;

            reward(plWin);
            PlayerService.gI().changeAndSendTypePK(player1, ConstPlayer.NON_PK);
            PlayerService.gI().changeAndSendTypePK(player2, ConstPlayer.NON_PK);
            sendResultMatch(plWin, plLose, typeWin);
            PVPServcice.gI().removePVP(this);
        }
    }

    public abstract void sendResultMatch(Player winer, Player loser, byte typeWin);

    public abstract void reward(Player plWin);

    public void dispose() {

    }
}
