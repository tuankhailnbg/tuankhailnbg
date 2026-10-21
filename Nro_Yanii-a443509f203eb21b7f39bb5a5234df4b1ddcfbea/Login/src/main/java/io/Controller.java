// Decompiled with VMN
package io;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import model.User;
import model.UserManager;

public class Controller {

    private Session session;
    private Lock lock = new ReentrantLock();

    public Controller(Session session) {
        this.session = session;
    }

    public void process(Message ms) {
        switch (ms.command) {
            case 1: {
                this.login(ms);
                break;
            }
            case 2: {
                this.logout(ms);
                break;
            }
            case 5: {
                this.session.setServer(ms);
                break;
            }
            default: {
                System.out.println("cmd: " + ms.command);
            }
        }
    }

    public void logout(Message ms) {
        try {
            int userID = ms.reader().readInt();
            User user = UserManager.getInstance().find(userID);
            if (user != null) {
                System.out.println("logout user: " + user.getUsername());
                UserManager.getInstance().remove(user);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void login(Message ms) {
        try {
            byte serverID = ms.reader().readByte();
            int clientID = ms.reader().readInt();
            String username = ms.reader().readUTF();
            String password = ms.reader().readUTF();
            this.lock.lock();
            try {
                User user = new User(username, password, serverID, clientID, this.session);
                boolean result = user.login();
                if (result) {
                    UserManager.getInstance().add(user);
                }
            } finally {
                this.lock.unlock();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void onDisconnected() {
        UserManager.getInstance().removeAllUserWithServerID(this.session.getServerID());
        System.out.println("client " + this.session.sessionName + " ket noi thanh cong!");
    }
}
