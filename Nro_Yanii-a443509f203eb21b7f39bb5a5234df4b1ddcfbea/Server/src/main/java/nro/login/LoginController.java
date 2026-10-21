/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nro.login;

import nro.data.DataGame;
import nro.jdbc.DBService;
import nro.models.player.Player;
import nro.resources.Resources;
import nro.server.Client;
import nro.server.Manager;
import nro.server.io.Message;
import nro.server.io.Session;
import nro.services.Service;
import nro.utils.Log;
import nro.utils.Util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author 💖 Nro Yanii 💖
 */
public class LoginController {

    private LoginSession session;

    public LoginController(LoginSession session) {
        this.session = session;
    }

    public void process(Message m) {
        switch (m.command) {
            case Cmd.LOGIN:
                login(m);
                break;
            case Cmd.DISCONNECT:
                disconnect(m);
                break;
            case Cmd.SERVER_MESSAGE:
                serverMessage(m);
                break;
            case Cmd.UPDATE_TIME_LOGOUT:
                updateTimeLogout(m);
                break;
            default:
                System.out.println("cmd: " + m.command);
                break;
        }
    }

    public void updateTimeLogout(Message ms) {
        try {
            int userID = ms.reader().readInt();
            Player player = Client.gI().getPlayerByUser(userID);
            if (player != null) {
                Client.gI().kickSession(player.getSession());
            } else {
                updateTimeLogout(userID);
            }
        } catch (Exception ex) {
            Log.error(LoginController.class, ex);
        }
    }

    public void updateTimeLogout(int id) throws SQLException {
        PreparedStatement ps = null;
        try (Connection con = DBService.gI().getConnectionForGame();) {
            ps = con.prepareStatement("update account set last_time_logout = ? where id = ?");
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (Exception e) {
            Log.error(LoginController.class, e);
        } finally {
            ps.close();
        }
    }

    public void serverMessage(Message ms) {
        try {
            int clientID = ms.reader().readInt();
            String text = ms.reader().readUTF();
            Session session = Client.gI().getSession(clientID);
            if (session != null) {
                Service.getInstance().sendThongBaoOK(session, text);
            }
        } catch (IOException ex) {
            Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void disconnect(Message ms) {
        try {
            int userID = ms.reader().readInt();
            Player player = Client.gI().getPlayerByUser(userID);
            if (player != null) {
                Client.gI().kickSession(player.getSession());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void login(Message ms) {
        try {
            int clientID = ms.reader().readInt();
            Session session = Client.gI().getSession(clientID);
            if (session != null) {
                try {
                    byte status = ms.reader().readByte();
                    if (status == 0) {// thất bại
                        int userID = ms.reader().readInt();
                        boolean isAdmin = ms.reader().readBoolean();
                        boolean actived = ms.reader().readBoolean();
                        int goldBar = ms.reader().readInt();
                        long lastTimeLogin = ms.reader().readLong();
                        long lastTimeLogout = ms.reader().readLong();
                        String rewards = ms.reader().readUTF();
                        int ruby = ms.reader().readInt();
                        int diemTichNap = ms.reader().readInt();
                        int server = ms.reader().readInt();
                        int isUseMaBaoVe = ms.reader().readInt();
                        int MaBaoVe = ms.reader().readInt();
                        int TongNap = ms.reader().readInt();
                        int VND = ms.reader().readInt();
                        session.userId = userID;
                        Session se = Client.gI().getSession(session);
                        if (se != null) {
                            Client.gI().kickSession(se);
                            Client.gI().kickSession(session);
                            Service.getInstance().sendThongBaoOK(session, "Máy chủ tắt hoặc mất sóng");
                            return;
                        }
                        session.isAdmin = isAdmin;
                        // if (!session.isAdmin) {
                        // Client.gI().kickSession(session);
                        // Service.getInstance().sendThongBaoOK(session, "Hiện chỉ có admin mới có thể
                        // đăng nhập");
                        // return;
                        // }
                        session.actived = actived;
                        session.goldBar = goldBar;
                        session.lastTimeLogout = lastTimeLogin;
                        session.dataReward = rewards;
                        session.ruby = ruby;
                        session.diemTichNap = diemTichNap;
                        session.server = server;
                        session.isUseMaBaoVe = isUseMaBaoVe;
                        session.MaBaoVe = MaBaoVe;
                        session.tongnap = TongNap;
                        session.vndBar = VND;
                        System.out.println("login userID: " + userID + " Succeslly");
                        Resources.getInstance().sendSmallVersion(session);
                        Resources.getInstance().sendBGVersion(session);
                        session.timeWait = 0;
                        session.loginSuccess = true;
                        DataGame.sendVersionGame(session);
                    } else {

                        String text = ms.reader().readUTF();
                        Service.getInstance().sendThongBaoOK(session, text);
                    }
                } finally {

                    session.setLogging(false);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public void onConnectionFail() {
        System.out.println("Ket noi den may chu login that bai!");
        Util.setTimeout(() -> {
            session.reconnect();
        }, 10000, "knoi that bai");

    }

    public void onConnectOK() {
        System.out.println("----------Nro Yanii----------");
        session.getService().setServer(Manager.SERVER, Client.gI());
    }

    public void onDisconnected() {
        System.out.println("Mat ket noi may chu login");
        Util.setTimeout(() -> {
            session.reconnect();
        }, 10000, "----------Nro Yanii----------");
    }

}
