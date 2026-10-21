// Decompiled with: VMN
package model;

import java.util.ArrayList;
import java.util.List;

public class UserManager {

    private static final UserManager instance = new UserManager();
    private List<User> users = new ArrayList<User>();

    public static UserManager getInstance() {
        return instance;
    }

    public void add(User user) {
        List<User> list = this.users;
        synchronized (list) {
            this.users.add(user);
        }
    }

    public void remove(User user) {
        List<User> list = this.users;
        synchronized (list) {
            this.users.remove(user);
        }
    }

    public User find(int userID) {
        List<User> list = this.users;
        synchronized (list) {
            for (User user : this.users) {
                if (user.getUserID() != userID) {
                    continue;
                }
                return user;
            }
        }
        return null;
    }

    public void removeAllUserWithServerID(int serverID) {
        List<User> list = this.users;
        synchronized (list) {
            this.users.removeIf(t -> t.getServerID() == serverID);
        }
    }
}
