/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nro.login;

import nro.server.Client;
import nro.server.io.Message;
import nro.server.io.Session;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author 💖 Nro Yanii 💖
 */
public class LoginService {

    private LoginSession session;

    public LoginService(LoginSession session) {
        this.session = session;
    }

    public void login(byte server, int clientID, String username, String password) {
        try {
            Message ms = new Message(Cmd.LOGIN);
            DataOutputStream ds = ms.writer();
            ds.writeByte(server);
            ds.writeInt(clientID);
            ds.writeUTF(username);
            ds.writeUTF(password);
            ds.flush();
            sendMessage(ms);
            ms.cleanup();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void logout(int userID) {
        try {
            Message ms = new Message(Cmd.LOGOUT);
            DataOutputStream ds = ms.writer();
            ds.writeInt(userID);
            ds.flush();
            sendMessage(ms);
            ms.cleanup();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void setServer(int serverID, Client client) {
        try {
            // System.out.println("add all users to the login server");
            List<Session> sessions = client.getSessions();
            synchronized (sessions) {
                List<Session> list = sessions.stream().filter((t) -> t.loginSuccess).collect(Collectors.toList());
                Message ms = new Message(Cmd.SERVER);
                DataOutputStream ds = ms.writer();
                ds.writeInt(serverID);
                ds.writeInt(list.size());
                for (Session session : list) {
                    ds.writeInt(session.id);
                    ds.writeInt(session.userId);
                    ds.writeUTF(session.uu);
                    ds.writeUTF(session.pp);
                }
                ds.flush();
                sendMessage(ms);
                ms.cleanup();
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void sendMessage(Message ms) {
        session.sendMessage(ms);
    }
}
