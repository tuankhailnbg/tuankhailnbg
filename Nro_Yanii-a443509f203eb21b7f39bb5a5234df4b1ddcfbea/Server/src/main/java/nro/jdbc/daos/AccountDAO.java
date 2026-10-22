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

    public static final int CREATE_ACCOUNT_ERROR = -1;
    public static final int CREATE_ACCOUNT_EXISTS = -2;

    public static synchronized int createAccount(String user, String password) {
        if (user == null || password == null) {
            return CREATE_ACCOUNT_ERROR;
        }

        final String checkSql = "SELECT id FROM account WHERE username = ? LIMIT 1";
        final String insertSql = "INSERT INTO account (username, password) VALUES (?, ?)";

        try (Connection con = DBService.gI().getConnectionForGame();
                PreparedStatement checkPs = con.prepareStatement(checkSql)) {

            checkPs.setString(1, user);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next()) {
                    return CREATE_ACCOUNT_EXISTS;
                }
            }

            try (PreparedStatement insertPs = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                insertPs.setString(1, user);
                insertPs.setString(2, password);

                int affectedRows = insertPs.executeUpdate();
                if (affectedRows <= 0) {
                    return CREATE_ACCOUNT_ERROR;
                }

                try (ResultSet generatedKeys = insertPs.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }

                // INSERT thành công nhưng driver không trả generated key.
                // Trả 1 để Service vẫn nhận diện là thành công.
                return 1;
            }
        } catch (SQLException e) {
            // MariaDB/MySQL duplicate key. Hữu ích nếu hai request đăng ký cùng lúc.
            if (e.getErrorCode() == 1062) {
                return CREATE_ACCOUNT_EXISTS;
            }
            Log.error(AccountDAO.class, e);
            return CREATE_ACCOUNT_ERROR;
        } catch (Exception e) {
            Log.error(AccountDAO.class, e);
            return CREATE_ACCOUNT_ERROR;
        }
    }

}
