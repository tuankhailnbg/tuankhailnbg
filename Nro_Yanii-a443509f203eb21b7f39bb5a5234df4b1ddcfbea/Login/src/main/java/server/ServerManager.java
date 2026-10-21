// Decompiled with: VMN
package server;

import io.Session;
import java.util.ArrayList;
import java.util.List;

public class ServerManager {

    private List<Session> sessions = new ArrayList<Session>();

    public void add(Session session) {
        List<Session> list = this.sessions;
        synchronized (list) {
            this.sessions.add(session);
        }
    }

    public void remove(Session session) {
        List<Session> list = this.sessions;
        synchronized (list) {
            this.sessions.remove(session);
        }
    }

    public Session find(int id) {
        List<Session> list = this.sessions;
        synchronized (list) {
            for (Session session : this.sessions) {
                if (session.id != id) {
                    continue;
                }
                return session;
            }
        }
        return null;
    }

    public Session findWithServerID(int serverID) {
        List<Session> list = this.sessions;
        synchronized (list) {
            for (Session session : this.sessions) {
                if (session.getServerID() != serverID) {
                    continue;
                }
                return session;
            }
        }
        return null;
    }

    public List<Session> getSessions() {
        return this.sessions;
    }
}
