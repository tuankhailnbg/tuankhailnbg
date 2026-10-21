package nro.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author ❤Girlkun75❤
 * @copyright ❤Nro Yanii❤
 */
public class ConnPool {

    public String url;
    public String user;
    public String password;
    public static ConnPool i;

    public static ConnPool gI() {
        if (i == null) {
            i = new ConnPool("jdbc:mysql://" + DBService.DB_HOST + ":" + DBService.DB_PORT + "/" + DBService.DB_NAME
                    + "?autoReconnect=True", DBService.DB_USER, DBService.DB_PASSWORD, DBService.MAX_CONN);
        }
        return i;
    }

    private ConnPool(String url, String user, String password, int max) {
        this.conns = new Vector<Connection>();
        this.getTime = new Vector<Long>();
        this.timeout = 30000;
        this.url = url;
        this.user = user;
        this.password = password;
        this.maxConn = max;
    }

    List<Connection> conns;

    List<Long> getTime;

    int timeout;

    int maxConn;

    public Connection newConnection() throws SQLException {
        try {
            Class.forName(DBService.DRIVER);
            Connection conn = DriverManager.getConnection(this.url, this.user, this.password);
            return conn;
        } catch (ClassNotFoundException cnfe) {
            throw new SQLException("Can't find class for driver: " + DBService.DRIVER);
        }
    }

    public synchronized Connection getConnection() throws SQLException {
        for (int i = 0; i < this.conns.size(); i++) {
            if (((Long) this.getTime.get(i)).longValue() != 0L
                    && System.currentTimeMillis() - ((Long) this.getTime.get(i)).longValue() > this.timeout) {
                close(this.conns.get(i));
                this.conns.set(i, newConnection());
                this.getTime.set(i, Long.valueOf(0L));
            }
            if (((Long) this.getTime.get(i)).longValue() == 0L) {
                this.getTime.set(i, Long.valueOf(System.currentTimeMillis()));
                if (((Connection) this.conns.get(i)).isClosed()) {
                    this.conns.set(i, newConnection());
                }
                return this.conns.get(i);
            }
        }
        if (this.conns.size() >= this.maxConn) {
            throw new SQLException("Limited Connection for " + this.url);
        }
        Connection conn = newConnection();
        this.conns.add(conn);
        this.getTime.add(Long.valueOf(System.currentTimeMillis()));
        return conn;
    }

    public void close(Connection conn) {
        try {
            conn.close();
        } catch (SQLException sQLException) {
        }
    }

    public void free(Connection conn) {
        int i = this.conns.indexOf(conn);
        if (i > -1) {
            this.getTime.set(i, Long.valueOf(0L));
        }
    }
}
