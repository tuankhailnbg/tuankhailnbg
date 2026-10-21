package nro.models.task;

import nro.models.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class TaskPlayer {

    public TaskMain taskMain;

    public SideTask sideTask;
    public List<Achivement> achivements;

    private Player player;

    public TaskPlayer(Player player) {
        this.player = player;
        this.sideTask = new SideTask();
        this.achivements = new ArrayList<>();
    }

    public void dispose() {
        this.taskMain = null;
        this.sideTask = null;
        this.player = null;
    }

}
