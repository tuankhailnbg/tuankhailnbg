package nro.models.dragon;

import lombok.Getter;
import lombok.Setter;
import nro.consts.Cmd;
import nro.models.player.Player;
import nro.server.io.Message;
import nro.services.Service;
import nro.utils.Util;

import java.io.IOException;

/**
 * @author outcast c-cute hột me 😳
 */

@Setter
@Getter
public abstract class AbsDragon implements Runnable {

    private String content;
    private String[] wishes;
    private String tutorial;
    private Player summoner;
    private long summonerID;
    private String name;
    private boolean appear;
    private long lastTimeAppear;

    public AbsDragon(Player player) {
        this.summoner = player;
        this.summonerID = player.id;
    }

    public abstract void openMenu();

    public abstract void summon();

    public abstract void reSummon();

    public abstract void showWishes();

    public void sendNotify() {
        Message m = new Message(Cmd.SERVER_MESSAGE);
        try {
            m.writer().writeUTF(summoner.name + " vừa gọi " + name + " tại "
                    + summoner.zone.map.mapName + " khu vực " + summoner.zone.zoneId);
            Service.getInstance().sendMessAllPlayerIgnoreMe(summoner, m);
        } catch (IOException e) {
        }
    }

    public abstract void callDragon();

    public abstract void leave();

    @Override
    public void run() {
        while (isAppear()) {
            if (Util.canDoWithTime(lastTimeAppear, 60000 * 5)) {
                leave();
            }
        }
    }
}