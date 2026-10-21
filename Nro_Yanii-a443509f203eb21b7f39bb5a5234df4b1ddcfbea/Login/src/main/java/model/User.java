// Decompiled with: VMN
package model;

import db.DbManager;
import io.Session;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import server.Server;

public class User {

    private Session session;
    private int userID;
    private String username;
    private String password;
    private int serverID;
    private int clientID;
    private boolean admin;
    private boolean actived;
    private int goldBar;
    private long lastTimeLogin;
    private long lastTimeLogout;
    private String rewards;
    private int ruby;
    private int mocNap;
    private int server;
    private int isUseMaBaoVe;
    private int MaBaoVe;
    private int tongnap;
    private int vndBar;

    public User(String username, String password, int serverID, int clientID, Session session) {
        this.username = username;
        this.password = password;
        this.serverID = serverID;
        this.clientID = clientID;
        this.session = session;
    }

    public boolean login() {
        System.out.println("Login username: " + this.username + " serverID: " + this.serverID);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection conn = DbManager.getInstance().getConnectionForLogin();
            String query = "select * from account where username = ? and password = ? limit 1";
            ps = conn.prepareStatement(query);
            ps.setString(1, this.username);
            ps.setString(2, this.password);
            rs = ps.executeQuery();
            if (rs.next()) {
                int waitLogin;
                this.userID = rs.getInt("account.id");
                int serverLogin = rs.getInt("server_login");
                if (serverLogin != this.serverID) {
                    this.session.getService().loginFailed(this.clientID, "Account nay thuoc may chu SV" + serverLogin);
                    boolean bl = false;
                    return bl;
                }
                User us = UserManager.getInstance().find(this.userID);
                if (us != null) {
                    us.disconnect();
                    this.session.getService().loginFailed(this.clientID, "Đăng nhập thất bại, vui lòng đăng nhập lại!");
                    boolean bl = false;
                    return bl;
                }
                this.lastTimeLogin = rs.getTimestamp("last_time_login").getTime();
                this.lastTimeLogout = rs.getTimestamp("last_time_logout").getTime();
                this.admin = rs.getBoolean("is_admin");
                int secondsPass = (int) ((System.currentTimeMillis() - this.lastTimeLogout) / 1000L);
                if (secondsPass < (waitLogin = Server.getInstance().getConfig().getSecondWaitLogin())) {
                    this.session.getService().loginFailed(this.clientID,
                            "Vui lòng chờ " + (waitLogin - secondsPass) + " giây để đăng nhập lại.");
                    boolean bl = false;
                    return bl;
                }
                this.actived = rs.getBoolean("active");
                this.goldBar = rs.getInt("account.thoi_vang");
                this.rewards = rs.getString("reward");
                // this.ruby = rs.getInt("ruby");
                // this.mocNap = rs.getInt("count_card");
                this.server = rs.getInt("server_login");
                // this.isUseMaBaoVe = rs.getInt("isUseMaBaoVe");
                // this.MaBaoVe = rs.getInt("MaBaoVe");
                this.tongnap = rs.getInt("account.tongnap");
                this.vndBar = rs.getInt("account.vnd");
                boolean ban = rs.getBoolean("ban");
                if (!this.admin && Server.getInstance().getConfig().getTestmode() == 1) {
                    this.session.getService().loginFailed(this.clientID,
                            "Server đang được admin xử lý và kiểm tra lại,vui lòng quay lại sau");
                    boolean bl = false;
                    return bl;
                }
                if (ban) {
                    this.session.getService().loginFailed(this.clientID, "Tài khoản đã bị khóa do vi phạm điều khoản!");
                    boolean bl = false;
                    return bl;
                }
                this.session.getService().loginSuccessful(this);
                boolean bl = true;
                return bl;
            }
            this.session.getService().loginFailed(this.clientID, "Thông tin tài khoản hoặc mật khẩu không chính xác");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sQLException) {
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException sQLException) {
                }
            }
        }
    }

    public void disconnect() {
        this.session.getService().disconnect(this.userID);
        UserManager.getInstance().remove(this);
    }

    public Session getSession() {
        return this.session;
    }

    public int getUserID() {
        return this.userID;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public int getServerID() {
        return this.serverID;
    }

    public int getClientID() {
        return this.clientID;
    }

    public boolean isAdmin() {
        return this.admin;
    }

    public boolean isActived() {
        return this.actived;
    }

    public int getisUseMaBaoVe() {
        return this.isUseMaBaoVe;
    }

    public int getMaBaoVe() {
        return this.MaBaoVe;
    }

    public int getGoldBar() {
        return this.goldBar;
    }

    public int getVndBar() {
        return this.vndBar;
    }

    public int getTongNap() {
        return this.tongnap;
    }

    public long getLastTimeLogin() {
        return this.lastTimeLogin;
    }

    public long getLastTimeLogout() {
        return this.lastTimeLogout;
    }

    public String getRewards() {
        return this.rewards;
    }

    public int getRuby() {
        return this.ruby;
    }

    public int getMocNap() {
        return this.mocNap;
    }

    public int getServer() {
        return this.server;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setServerID(int serverID) {
        this.serverID = serverID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public void setActived(boolean actived) {
        this.actived = actived;
    }

    public void setGoldBar(int goldBar) {
        this.goldBar = goldBar;
    }

    public void setVndBar(int vndBar) {
        this.vndBar = vndBar;
    }

    public void setTongNap(int tongnap) {
        this.tongnap = tongnap;
    }

    public void setisUseMaBaoVe(int isUseMaBaoVe) {
        this.isUseMaBaoVe = isUseMaBaoVe;
    }

    public void setMaBaoVe(int MaBaoVe) {
        this.MaBaoVe = MaBaoVe;
    }

    public void setLastTimeLogin(long lastTimeLogin) {
        this.lastTimeLogin = lastTimeLogin;
    }

    public void setLastTimeLogout(long lastTimeLogout) {
        this.lastTimeLogout = lastTimeLogout;
    }

    public void setRewards(String rewards) {
        this.rewards = rewards;
    }

    public void setRuby(int ruby) {
        this.ruby = ruby;
    }

    public void setMocNap(int mocNap) {
        this.mocNap = mocNap;
    }

    public void setServer(int server) {
        this.server = server;
    }
}
