package nro.jdbc.daos;

import nro.jdbc.DBService;
import nro.server.Manager;
import nro.server.io.Session;
import nro.utils.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class AccountDAO {

    public static void updateAccount(Session session) {
        PreparedStatement ps = null;
        try (Connection con = DBService.gI().getConnectionForGetPlayer();) {
            ps = con.prepareStatement("update account set password = ? where id = ? and username = ?");
            ps.setString(1, session.pp);
            ps.setInt(2, session.userId);
            ps.setString(3, session.uu);
            ps.executeUpdate();
        } catch (Exception e) {
            Log.error(AccountDAO.class, e);
        } finally {
            try {
                ps.close();
            } catch (SQLException ex) {
            }
        }
    }

    public static void updateLastTimeLoginAllAccount() {
        PreparedStatement ps = null;
        try (Connection con = DBService.gI().getConnectionForGame();) {
            ps = con.prepareStatement("update account set last_time_login = '2000-01-01', "
                    + "last_time_logout = '2001-01-01' where server_login = " + Manager.SERVER);
            ps.executeUpdate();
        } catch (Exception e) {
            Log.error(AccountDAO.class, e);
        } finally {
            try {
                ps.close();
            } catch (SQLException ex) {
                // java.util.logging.Logger.getLogger(AccountDAO.class.getName()).log(Level.SEVERE,
                // null, ex);
            }
        }
    }

    public static void updateAccoutLogout(Session session) {
        if (session.uu != null && session.pp != null) {
            PreparedStatement ps = null;
            try (Connection con = DBService.gI().getConnectionForGame();) {
                ps = con.prepareStatement("update account set last_time_logout = ? where id = ?");
                ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                ps.setInt(2, session.userId);
                ps.executeUpdate();
            } catch (Exception e) {
                Log.error(AccountDAO.class, e);
            } finally {
                try {
                    ps.close();
                } catch (SQLException ex) {
                }
            }
        }
    }

    public static void banAccount(Session session) {
        PreparedStatement ps = null;
        try (Connection con = DBService.gI().getConnectionForGetPlayer();) {
            ps = con.prepareStatement("update account set ban = 1 where id = ? and username = ?");
            ps.setInt(1, session.userId);
            ps.setString(2, session.uu);
            ps.executeUpdate();
        } catch (Exception e) {
            Log.error(AccountDAO.class, e);
        } finally {
            try {
                ps.close();
            } catch (SQLException ex) {
            }
        }
    }

    public static int createAccount(String user, String password) {
        int key = -1;
        PreparedStatement ps = null;
        try (Connection con = DBService.gI().getConnectionForGetPlayer();) {
            ps = con.prepareStatement("select * from account where username = ?");
            ps.setString(1, user);
            if (ps.executeQuery().next()) {
                System.out.println("Tạo thất bại do tài khoản đã tồn tại");
            } else {
                ps = con.prepareStatement("insert into account(username,password) values (?,?)",
                        Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, user);
                ps.setString(2, password);
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                rs.next();
                key = rs.getInt(1);
                System.out.println("Tạo tài khoản thành công!");
            }
        } catch (Exception e) {
            Log.error(AccountDAO.class, e);
        } finally {
            try {
                ps.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return key;
    }

}
