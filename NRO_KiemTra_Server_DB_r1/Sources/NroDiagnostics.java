import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.security.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.jar.*;

public class NroDiagnostics {
    private static int failures;
    private static int warnings;
    private static Path root;
    private static Path tools;
    private static NroSupport.Settings settings;

    private static void line(String value) { System.out.println(value); }
    private static void ok(String value) { line("OK   " + value); }
    private static void fail(String value) { failures++; line("FAIL " + value); }
    private static void warn(String value) { warnings++; line("WARN " + value); }

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.err.println("Usage: NroDiagnostics PROJECT_ROOT TOOL_ROOT REPORT_PATH");
            System.exit(2);
        }
        root = Path.of(args[0]).toAbsolutePath().normalize();
        tools = Path.of(args[1]).toAbsolutePath().normalize();
        settings = new NroSupport.Settings(root);
        NroSupport.capture(Path.of(args[2]), settings.secrets);
        line("NRO DATABASE AND SERVER DIAGNOSTICS r1");
        line("Time: " + LocalDateTime.now());
        line("Java: " + System.getProperty("java.version"));
        line("Project: " + root);
        line("READ ONLY: no INSERT, UPDATE, DELETE, CREATE, GRANT or import is executed.");
        try {
            config();
            jar(root.resolve("Login/target/ServerLogin-1.0-RELEASE-jar-with-dependencies.jar"), "server.Server");
            jar(root.resolve("Server/target/TeaMobi-1.0-RELEASE-jar-with-dependencies.jar"), "nro.server.ServerManager");
            resources();
            handshake("LOGIN", settings.game.getProperty("login.host", "127.0.0.1"), settings.loginPort());
            handshake("GAME", "127.0.0.1", settings.gamePort());
            database("LOGIN", settings.login.getProperty("db.host", "localhost"), settings.login.getProperty("db.port", "3306"), settings.login.getProperty("db.name", ""), settings.login.getProperty("db.user", ""), settings.login.getProperty("db.password", ""), false);
            database("GAME", settings.game.getProperty("server.db.ip", "localhost"), settings.game.getProperty("server.db.port", "3306"), settings.game.getProperty("server.db.name", ""), settings.game.getProperty("server.db.us", "root"), settings.game.getProperty("server.db.pw", "123456"), true);
        } catch (Throwable e) {
            fail("Unexpected check failure: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        line("");
        line("RESULT: " + failures + " FAIL, " + warnings + " WARN");
        line("A successful socket/DB check does not prove client login or gameplay.");
        line("Established Login/Game links and fresh startup logs must also be reviewed.");
        System.out.flush();
        System.err.flush();
        if (failures > 0) System.exit(1);
    }

    private static void config() {
        line("\n[CONFIGURATION]");
        for (String message : settings.errors) fail(message);
        for (String key : List.of("server.port","db.host","db.port","db.name","db.user","db.password","db.driver","admin.mode")) {
            if (!settings.login.containsKey(key)) fail("Missing Login setting=" + key);
        }
        line("Login port=" + settings.loginPort() + ", game port=" + settings.gamePort());
        line("Game connects to Login at " + settings.game.getProperty("login.host", "127.0.0.1") + ":3105 (port fixed in source).");
        line("Server ID=" + settings.game.getProperty("server.sv", "missing") + ", admin.mode=" + settings.login.getProperty("admin.mode", "missing"));
        if (settings.loginPort() != 3105) fail("Login port differs from the game's fixed loginPort=3105.");
        if (!settings.login.getProperty("db.name", "").equals(settings.game.getProperty("server.db.name", ""))) fail("Login and Game use different database names.");
        else ok("Login/Game database names match.");
        if (!settings.login.getProperty("db.password", "").equals(settings.game.getProperty("server.db.pw", "123456"))) warn("Login/Game database passwords differ. Both connections will be tested separately.");
        else ok("Login/Game database passwords match (values hidden).");
        if (!settings.login.getProperty("db.user", "").equals(settings.game.getProperty("server.db.us", ""))) warn("Login/Game database users differ. Both connections will be tested separately.");
        if ("1".equals(settings.login.getProperty("admin.mode", ""))) warn("admin.mode=1 rejects non-admin game accounts.");
        boolean hasList = false;
        for (int i=1; i<=10; i++) hasList |= settings.game.containsKey("server.sv" + i);
        if (!hasList) fail("No server.sv1..server.sv10 entry; Manager.loadProperties cannot build the server list.");
        if (settings.gamePort() != 14445) warn("Game port differs from 14445 used by the client package.");
        for (String module : List.of("Login", "Server")) {
            Path old = root.resolve(module + "/run.bat");
            try {
                if (Files.exists(old) && Files.readString(old).toLowerCase(Locale.ROOT).contains("-jar " + module.toLowerCase(Locale.ROOT) + ".jar"))
                    warn(module + "/run.bat starts the old root JAR instead of the Maven target JAR.");
            } catch (IOException ignored) { }
        }
    }

    private static void jar(Path path, String mainClass) {
        line("\n[JAR " + path.getFileName() + "]");
        if (!Files.isRegularFile(path)) { fail("Missing built JAR: " + path); return; }
        try (JarFile jar = new JarFile(path.toFile())) {
            String entry = jar.getManifest().getMainAttributes().getValue("Main-Class");
            if (!mainClass.equals(entry)) fail("Unexpected Main-Class=" + entry); else ok("Main-Class=" + entry);
            for (String required : List.of(mainClass.replace('.', '/') + ".class", "com/mysql/cj/jdbc/Driver.class", "com/zaxxer/hikari/HikariDataSource.class")) {
                if (jar.getJarEntry(required) == null) fail("Missing dependency/class: " + required);
            }
            ok("JAR readable, size=" + Files.size(path) + ", modified=" + Files.getLastModifiedTime(path));
        } catch (Exception e) { fail("JAR error: " + e.getMessage()); }
    }

    private static void resources() throws Exception {
        line("\n[SERVER RESOURCES]");
        Path gameConfig = root.resolve("Server/resources/config/game.properties");
        if (!Files.isRegularFile(gameConfig)) fail("Missing Server/resources/config/game.properties");
        int checked=0, missing=0, changed=0;
        for (String row : Files.readAllLines(tools.resolve("expected-resources.tsv"), StandardCharsets.UTF_8)) {
            if (row.isBlank()) continue;
            String[] cells = row.split("\t");
            Path path = root.resolve(cells[0]);
            checked++;
            if (!Files.isRegularFile(path)) {
                if (missing++ < 12) fail("Missing resource: " + cells[0]);
                continue;
            }
            byte[] data = Files.readAllBytes(path);
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            sha.update(("blob " + data.length + "\0").getBytes(StandardCharsets.UTF_8));
            String hash = HexFormat.of().formatHex(sha.digest(data));
            if (data.length != Long.parseLong(cells[1]) || !hash.equals(cells[2])) {
                if (changed++ < 12) warn("Resource differs from supplied source: " + cells[0]);
            }
        }
        if (missing>12) fail("Additional missing resources: " + (missing-12));
        line("Checked=" + checked + ", missing=" + missing + ", changed=" + changed);
        if (missing==0 && changed==0) ok("Required map/update resources match the pinned source.");
    }

    static boolean handshake(String label, String host, int port) {
        line("\n[NRO HANDSHAKE " + label + " " + host + ":" + port + "]");
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 1500);
            socket.setSoTimeout(2500);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());
            out.writeByte(-27); out.writeShort(0); out.flush();
            int cmd = in.readByte();
            int length = in.readUnsignedShort();
            if (cmd != -27 || length < 2 || length > 4096) { fail("Port is open but NRO key reply is invalid."); return false; }
            byte[] packet = new byte[length];
            in.readFully(packet);
            int keyLength = packet[0] & 255;
            if (keyLength<1 || keyLength>127 || length<keyLength+1) { fail("Invalid NRO key packet size."); return false; }
            ok("NRO key handshake received; no account login sent.");
            return true;
        } catch (Exception e) { fail("Handshake failed: " + e.getClass().getSimpleName() + ": " + e.getMessage()); return false; }
    }

    private static void database(String label, String host, String port, String db, String user, String password, boolean checkData) {
        line("\n[DATABASE " + label + "]");
        line("host=" + host + ", port=" + port + ", schema=" + db + ", user=" + user + ", password=[hidden]");
        if (!db.matches("[A-Za-z0-9_]+")) { fail("Invalid/missing database name."); return; }
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Properties auth = new Properties();
            auth.setProperty("user", user); auth.setProperty("password", password);
            auth.setProperty("connectTimeout", "3000"); auth.setProperty("socketTimeout", "5000");
            DriverManager.setLoginTimeout(5);
            try (Connection con = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + db, auth)) {
                con.setReadOnly(true);
                con.setAutoCommit(false);
                try {
                    try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT DATABASE(), VERSION()")) {
                        rs.next(); ok("Connected; DATABASE=" + rs.getString(1) + ", VERSION=" + rs.getString(2));
                    }
                    if (checkData) schema(con, db);
                    else {
                        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM account")) { rs.next(); ok("Login account table readable; rows=" + rs.getLong(1)); }
                    }
                } finally { con.rollback(); }
            }
        } catch (SQLException e) {
            fail("SQLState=" + e.getSQLState() + ", code=" + e.getErrorCode() + ", " + e.getMessage());
            if ("28000".equals(e.getSQLState())) line("HINT: check db.user/db.password and server.db.us/server.db.pw permissions.");
            if (e.getErrorCode()==1049) line("HINT: configured database does not exist.");
        } catch (Exception e) { fail(e.getClass().getSimpleName() + ": " + e.getMessage()); }
    }

    private static void schema(Connection con, String db) throws Exception {
        Map<String,Set<String>> expected = new TreeMap<>();
        for (String row : Files.readAllLines(tools.resolve("expected-schema.tsv"), StandardCharsets.UTF_8)) {
            if (row.isBlank()) continue;
            String[] cells = row.split("\t");
            expected.computeIfAbsent(cells[0], k->new TreeSet<>()).add(cells[1]);
        }
        Map<String,Set<String>> actual = new TreeMap<>();
        try (PreparedStatement ps = con.prepareStatement("SELECT TABLE_NAME,COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=?")) {
            ps.setString(1, db);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) actual.computeIfAbsent(rs.getString(1), k->new TreeSet<>()).add(rs.getString(2)); }
        }
        line("Expected tables=" + expected.size() + ", visible tables/views=" + actual.size());
        Set<String> core = Set.of("account","player","map_template","skill_template","item_template","item_option_template","mob_template","npc_template","caption","power_limit","attribute_server","attribute_template","clan_sv"+settings.game.getProperty("server.sv","1"));
        for (Map.Entry<String,Set<String>> entry : expected.entrySet()) {
            String table = entry.getKey();
            if (!actual.containsKey(table)) {
                if (core.contains(table)) fail("Missing core table=" + table); else warn("Missing source table=" + table);
            } else {
                Set<String> absent = new TreeSet<>(entry.getValue()); absent.removeAll(actual.get(table));
                if (!absent.isEmpty()) {
                    if (core.contains(table)) fail("Missing columns " + table + ": " + absent); else warn("Different columns " + table + ": " + absent);
                }
            }
        }
        for (String table : List.of("map_template","skill_template","item_template","item_option_template","mob_template","npc_template","head_avatar","flag_bag","cai_trang","caption","power_limit","attribute_template","attribute_server","task_main_template","task_sub_template","account","player")) {
            if (!actual.containsKey(table)) continue;
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM `" + table + "`")) {
                rs.next(); long count = rs.getLong(1); line("ROWS " + table + "=" + count);
                if (count==0 && Set.of("map_template","skill_template","item_template","item_option_template","mob_template","npc_template").contains(table)) fail("Empty core game template table=" + table);
            }
        }
        Set<String> account = actual.getOrDefault("account", Set.of());
        if (account.containsAll(Set.of("server_login","ban","last_time_login","last_time_logout"))) {
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM account WHERE server_login=? AND ban=0")) {
                ps.setInt(1, Integer.parseInt(settings.game.getProperty("server.sv","1")));
                try (ResultSet rs = ps.executeQuery()) { rs.next(); long n=rs.getLong(1); line("Eligible non-banned accounts for server="+n); if(n==0) warn("No non-banned account matches server.sv. No account is created by this tool."); }
            }
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM account WHERE last_time_login IS NULL OR last_time_logout IS NULL OR CAST(last_time_login AS CHAR) LIKE '0000-%' OR CAST(last_time_logout AS CHAR) LIKE '0000-%'")) {
                rs.next(); long n=rs.getLong(1); if(n>0) warn("Accounts with NULL/zero login timestamps="+n+"; the original Login code calls getTimestamp(...).getTime()."); else ok("Account login timestamps are non-null/non-zero.");
            }
        }
    }
}

class NroSupport {
    static final class Settings {
        final Properties login = new Properties();
        final Properties game = new Properties();
        final List<String> errors = new ArrayList<>();
        final List<String> secrets = new ArrayList<>();
        Settings(Path root) {
            load(login,root.resolve("Login/server.ini"),StandardCharsets.UTF_8);
            load(game,root.resolve("Server/resources/config/server.properties"),StandardCharsets.ISO_8859_1);
            for(Properties p:List.of(login,game)) for(String key:p.stringPropertyNames()) if(key.toLowerCase(Locale.ROOT).matches(".*(password|\\.pw|\\.key\\d*)$")) {
                String value=p.getProperty(key); if(value!=null && !value.isEmpty()) secrets.add(value);
            }
            if(!game.containsKey("server.db.pw")) secrets.add("123456");
            secrets.sort(Comparator.comparingInt(String::length).reversed());
        }
        void load(Properties p,Path path,Charset charset) {
            try(Reader in=Files.newBufferedReader(path,charset)) { p.load(in); }
            catch(Exception e) { errors.add("Cannot read " + path + ": " + e.getMessage()); }
        }
        int loginPort() { return number(login.getProperty("server.port"),3105); }
        int gamePort() { return number(game.getProperty("server.port"),14445); }
        int number(String s,int fallback) { try { return Integer.parseInt(s.trim()); } catch(Exception e) { return fallback; } }
    }
    static String redact(String text,List<String> secrets) {
        text=text.replaceAll("\\u001B\\[[;\\d]*m", "");
        for(String value:secrets) text=text.replace(value,"[hidden]");
        return text.replaceAll("(?i)((?:db\\.password|server\\.db\\.pw|server\\.key\\d*|api\\.key)\\s*[:=]\\s*).*", "$1[hidden]");
    }
    static void capture(Path log,List<String> secrets) throws IOException {
        Files.createDirectories(log.toAbsolutePath().getParent());
        PrintStream console=System.out;
        OutputStream file=Files.newOutputStream(log,StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
        OutputStream tee=new OutputStream() {
            final ByteArrayOutputStream buffer=new ByteArrayOutputStream();
            public synchronized void write(int b) throws IOException { if(b=='\n') { emit(); } else { buffer.write(b); if(buffer.size()>65536) emit(); } }
            private void emit() throws IOException {
                String clean=redact(buffer.toString(StandardCharsets.UTF_8),secrets);
                byte[] bytes=(clean+System.lineSeparator()).getBytes(StandardCharsets.UTF_8);
                file.write(bytes); file.flush(); console.print(clean+System.lineSeparator()); console.flush(); buffer.reset();
            }
            public synchronized void flush() throws IOException { file.flush(); console.flush(); }
        };
        PrintStream stream=new PrintStream(tee,true,StandardCharsets.UTF_8);
        System.setOut(stream); System.setErr(stream);
    }
}
