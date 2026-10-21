package nro.noti;

import nro.consts.Cmd;
import nro.jdbc.DBService;
import nro.models.player.Player;
import nro.server.io.Message;
import nro.services.Service;

import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author outcast c-cute hột me 😳
 */
public class NotiManager {

    private static final NotiManager INSTANCE = new NotiManager();

    public static NotiManager getInstance() {
        return INSTANCE;
    }

    private static List<Notification> notifications = new ArrayList<Notification>();
    private static Alert alert;

    public void load() {
        loadNoti();
        loadAlert();
    }

    public void loadNoti() {
        try {
            notifications.clear();
            PreparedStatement ps = DBService.gI().getConnectionForGame().prepareStatement("SELECT * FROM `notifications`");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Notification notification = new Notification();
                notification.setId(rs.getInt("id"));
                notification.setContent(rs.getString("content"));
                notification.setTitle(rs.getString("title"));
                addNoti(notification);
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void loadAlert() {
        try {
            PreparedStatement ps = DBService.gI().getConnectionForGame().prepareStatement("SELECT * FROM `alert`");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Alert a = new Alert();
                a.content = rs.getString("content");
                this.alert = a;
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void addNoti(Notification noti) {
        notifications.add(noti);
    }

    public void sendAlert(Player player) {
        Service.getInstance().sendThongBaoFromAdmin(player, alert.content);
    }

    public void sendDanhQuaiNhanNgoc(Player player) {
        if (player.event.luotNhanNgocMienPhi == 1) {
            Service.getInstance().sendThongBao(player, "Hôm nay bạn sẽ nhận được từ 1 đến 2 viên ngọc khi tiêu diệt 1 con quái");
        }
    }

    public void sendNoti(Player player) {
        Message m = new Message(Cmd.GAME_INFO);
        try {
            DataOutputStream ds = m.writer();
            ds.writeByte(notifications.size());
            for (Notification notification : notifications) {
                ds.writeShort(notification.getId());
                ds.writeUTF(notification.getTitle());
                ds.writeUTF(notification.getContent());
            }
            ds.flush();
            player.sendMessage(m);
            m.cleanup();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
