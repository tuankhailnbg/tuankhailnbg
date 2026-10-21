// Decompiled with: VMN
package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.function.BiConsumer;

public class Config {

    private short listen;
    private String driver;
    private String username;
    private String password;
    private String dbName;
    private String host;
    private short port;
    private short testmode;
    private int secondWaitLogin;

    public Config(String path) {
        try {
            System.out.println("load config");
            FileInputStream input = new FileInputStream(new File(path));
            Properties props = new Properties();
            props.load(new InputStreamReader((InputStream) input, StandardCharsets.UTF_8));
            props.forEach((BiConsumer<? super Object, ? super Object>) ((BiConsumer<Object, Object>) (t,
                    u) -> System.out.println(String.format("Config - %s: %s", t, u))));
            this.listen = Short.parseShort(props.getProperty("server.port"));
            this.driver = props.getProperty("db.driver");
            this.username = props.getProperty("db.user");
            this.password = props.getProperty("db.password");
            this.dbName = props.getProperty("db.name");
            this.host = props.getProperty("db.host");
            this.port = Short.parseShort(props.getProperty("db.port"));
            this.testmode = Short.parseShort(props.getProperty("admin.mode"));
            this.secondWaitLogin = props.containsKey("wait.login")
                    ? (int) Short.parseShort(props.getProperty("wait.login"))
                    : 5;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String getJdbcUrl() {
        return "jdbc:mysql://" + this.host + ":" + this.port + "/" + this.dbName;
    }

    public short getListen() {
        return this.listen;
    }

    public String getDriver() {
        return this.driver;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getDbName() {
        return this.dbName;
    }

    public String getHost() {
        return this.host;
    }

    public short getPort() {
        return this.port;
    }

    public short getTestmode() {
        return this.testmode;
    }

    public int getSecondWaitLogin() {
        return this.secondWaitLogin;
    }

    public void setListen(short listen) {
        this.listen = listen;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(short port) {
        this.port = port;
    }

    public void setTestmode(short testmode) {
        this.testmode = testmode;
    }

    public void setSecondWaitLogin(int secondWaitLogin) {
        this.secondWaitLogin = secondWaitLogin;
    }
}
