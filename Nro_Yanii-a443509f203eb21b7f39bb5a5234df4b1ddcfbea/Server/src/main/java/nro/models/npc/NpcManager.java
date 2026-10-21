package nro.models.npc;

import nro.consts.ConstNpc;
import nro.consts.ConstTask;
import nro.models.player.Player;
import nro.server.Manager;
import nro.services.TaskService;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class NpcManager {

    public static Npc getByIdAndMap(int id, int mapId) {
        for (Npc npc : Manager.NPCS) {
            if (npc.tempId == id && npc.mapId == mapId) {
                return npc;
            }
        }
        return null;
    }

    public static Npc getNpc(byte tempId) {
        for (Npc npc : Manager.NPCS) {
            if (npc.tempId == tempId) {
                return npc;
            }
        }
        return null;
    }

    public static Npc Autochatnpc() {
        for (Npc npc : Manager.NPCS) {
            switch (npc.tempId) { // id npc
                case ConstNpc.MR_POPO:

                    npc.npcChat("Chúng ta gặp rắc rối rồi...");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    npc.npcChat("Chỉ còn 70 giờ nữa...");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    npc.npcChat("Toàn bộ sinh vật sống sẽ bị hủy diệt...");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    npc.npcChat("Thượng Đế nhờ tôi báo tin cho các cậu biết...");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    npc.npcChat("Hãy nhanh chóng lên đường giải cứu trái đất.");
                    break;
            }

        }
        return null;
    }

    public static List<Npc> getNpcsByMapPlayer(Player player) {
        List<Npc> list = new ArrayList<>();
        if (player.zone != null) {
            for (Npc npc : player.zone.map.npcs) {
                if (npc.tempId == ConstNpc.NOI_BANH && Manager.EVENT_SEVER != 4) {
                    continue;
                }
                if (npc.tempId == ConstNpc.QUA_TRUNG && player.mabuEgg == null) {
                    continue;
                } else if (npc.tempId == ConstNpc.CALICK && TaskService.gI().getIdTask(player) < ConstTask.TASK_20_0) {
                    continue;
                }
                list.add(npc);
            }
        }
        return list;
    }
}
