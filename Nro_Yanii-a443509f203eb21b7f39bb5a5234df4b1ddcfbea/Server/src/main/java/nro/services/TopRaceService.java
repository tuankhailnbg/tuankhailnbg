package nro.services;

import java.io.IOException;
import java.util.List;
import nro.consts.Cmd;
import nro.manager.TopRaceManager;
import nro.manager.TopRaceManager.RankEntry;
import nro.models.Part;
import nro.models.PartManager;
import nro.models.player.Player;
import nro.server.Client;
import nro.server.io.Message;
import nro.utils.Util;

public class TopRaceService {

    private static final TopRaceService INSTANCE = new TopRaceService();

    public static TopRaceService gI() {
        return INSTANCE;
    }

    private TopRaceService() {
    }

    public void showRanking(Player viewer, String type) {
        List<RankEntry> list = TopRaceManager.gI().getRanking(type);
        Message msg = new Message(Cmd.TOP);
        try {
            msg.writer().writeByte(0);
            msg.writer().writeUTF(getTitle(type));
            msg.writer().writeByte(list.size());
            for (RankEntry entry : list) {
                Player pl = entry.player;
                msg.writer().writeInt(entry.rank);
                msg.writer().writeInt((int) pl.id);
                msg.writer().writeShort(pl.getHead());
                if (viewer.isVersionAbove(220)) {
                    Part part = PartManager.getInstance().find(pl.getHead());
                    msg.writer().writeShort(part != null ? part.getIcon(0) : 0);
                }
                msg.writer().writeShort(pl.getBody());
                msg.writer().writeShort(pl.getLeg());
                msg.writer().writeUTF(pl.name);
                msg.writer().writeUTF(Client.gI().getPlayer(pl.id) != null ? "Online" : "");
                msg.writer().writeUTF(formatDetail(type, entry));
            }
            viewer.sendMessage(msg);
        } catch (IOException e) {
            Service.gI().sendThongBao(viewer, "Không mở được bảng xếp hạng đua Top.");
        } finally {
            msg.cleanup();
        }
    }

    public void showStatus(Player player) {
        Service.gI().sendThongBaoOK(player, TopRaceManager.gI().getStatusText());
    }

    public void claim(Player player) {
        Service.gI().sendThongBaoOK(player, TopRaceManager.gI().claimRewards(player));
    }

    private String getTitle(String type) {
        switch (type) {
            case TopRaceManager.TASK:
                return "ĐUA TOP NHIỆM VỤ";
            case TopRaceManager.PET:
                return "ĐUA TOP ĐỆ TỬ";
            case TopRaceManager.NAP:
                return "ĐUA TOP NẠP";
            case TopRaceManager.POWER:
            default:
                return "ĐUA TOP SỨC MẠNH";
        }
    }

    private String formatDetail(String type, RankEntry entry) {
        if (TopRaceManager.NAP.equals(type)) {
            return Util.numberToMoney(entry.score) + " VNĐ";
        }
        if (TopRaceManager.TASK.equals(type)) {
            return entry.detail;
        }
        if (TopRaceManager.PET.equals(type)) {
            return "SM đệ tử: " + Util.numberToMoney(entry.score);
        }
        return "Sức mạnh: " + Util.numberToMoney(entry.score);
    }
}
