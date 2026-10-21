// Decompiled with: VMN
package server;

import io.Message;
import io.Session;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class ServerService {

    private ServerManager manager;

    public ServerService(ServerManager manager) {
        this.manager = manager;
    }

    public void disconnect(int userID, Session except) {
        try {
            List<Session> sessions;
            Message ms = new Message(3);
            DataOutputStream ds = ms.writer();
            ds.writeInt(userID);
            ds.flush();
            List<Session> list = sessions = this.manager.getSessions();
            synchronized (list) {
                for (Session session : sessions) {
                    if (session == except) {
                        continue;
                    }
                    session.sendMessage(ms);
                }
            }
            ms.cleanup();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void sendMessage(Message ms) {
        List<Session> sessions;
        List<Session> list = sessions = this.manager.getSessions();
        synchronized (list) {
            for (Session session : sessions) {
                session.sendMessage(ms);
            }
        }
    }
}
