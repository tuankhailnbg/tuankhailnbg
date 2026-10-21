package nro.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author 💖 Nro Yanii 💖
 * 
 *
 */
public class DBHika {

    private static HikariConfig config = new HikariConfig();

    private static HikariDataSource ds;

    static {
        config.setDriverClassName(DBService.DRIVER);
        config.setJdbcUrl(DBService.URL
                .replaceAll("#0", "mysql")
                .replaceAll("#1", DBService.DB_HOST)
                .replaceAll("#2", DBService.DB_PORT + "")
                .replaceAll("#3", DBService.DB_NAME));
        config.setUsername(DBService.DB_USER);
        config.setPassword(DBService.DB_PASSWORD);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.setMinimumIdle(20);
        config.setMaximumPoolSize(100);

        config.setMaxLifetime(120000L);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "true");
        config.addDataSourceProperty("characterEncoding", "utf8");
        config.addDataSourceProperty("useUnicode", "true");
        ds = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static void close() {
        DBHika.ds.close();
    }

}
