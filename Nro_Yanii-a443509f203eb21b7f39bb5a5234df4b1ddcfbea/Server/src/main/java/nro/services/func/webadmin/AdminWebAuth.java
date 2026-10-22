package nro.services.func.webadmin;

import nro.jdbc.DBService;
import nro.models.player.Player;
import nro.utils.Log;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Quản lý xác thực, phân quyền và phiên làm việc (Session/CSRF) cho Web Admin Dashboard
 *
 * @author Antigravity
 */
public class AdminWebAuth {

    private static AdminWebAuth instance;

    // Thời gian hết hạn phiên làm việc: 2 giờ không hoạt động
    private static final long SESSION_EXPIRY_MS = 2 * 60 * 60 * 1000L;

    // Thời gian hết hạn của token đăng nhập 1 lần từ game: 5 phút
    private static final long ONE_TIME_TOKEN_EXPIRY_MS = 5 * 60 * 1000L;

    private final Map<String, AdminSession> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, OneTimeToken> oneTimeTokens = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public static class AdminSession {
        public String sessionToken;
        public String csrfToken;
        public String username;
        public int accountId;
        public long loginTime;
        public long lastAccessTime;

        public boolean isExpired() {
            return System.currentTimeMillis() - lastAccessTime > SESSION_EXPIRY_MS;
        }

        public void touch() {
            this.lastAccessTime = System.currentTimeMillis();
        }
    }

    public static class OneTimeToken {
        public String token;
        public String username;
        public int accountId;
        public long createdAt;

        public boolean isExpired() {
            return System.currentTimeMillis() - createdAt > ONE_TIME_TOKEN_EXPIRY_MS;
        }
    }

    private AdminWebAuth() {
    }

    public static AdminWebAuth gI() {
        if (instance == null) {
            instance = new AdminWebAuth();
        }
        return instance;
    }

    /**
     * Xác thực thông tin đăng nhập với database
     */
    public AdminSession login(String username, String password) {
        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            return null;
        }
        username = username.trim();
        password = password.trim();

        try (Connection con = DBService.gI().getConnectionForGame();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id, username, is_admin, role, admin FROM account WHERE username = ? AND password = ? LIMIT 1")) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean isAdmin = rs.getBoolean("is_admin");
                    int role = rs.getInt("role");
                    int adminCol = rs.getInt("admin");

                    if (isAdmin || role == 1 || adminCol == 1) {
                        int accountId = rs.getInt("id");
                        return createSession(username, accountId);
                    } else {
                        Log.warning("[WEB_ADMIN_AUTH] Tài khoản " + username + " đăng nhập đúng mật khẩu nhưng không có quyền ADMIN!");
                    }
                }
            }
        } catch (Exception e) {
            Log.error(AdminWebAuth.class, e, "Lỗi kiểm tra đăng nhập Web Admin: " + username);
        }
        return null;
    }

    /**
     * Tạo token 1 lần từ trong game cho Admin khi chat hoặc bấm menu
     */
    public String createOneTimeLoginToken(Player admin) {
        if (admin == null || !admin.isAdmin()) return null;

        String token = generateRandomToken(32);
        OneTimeToken ott = new OneTimeToken();
        ott.token = token;
        ott.username = admin.name;
        ott.accountId = admin.getSession() != null ? admin.getSession().userId : (int) admin.id;
        ott.createdAt = System.currentTimeMillis();

        oneTimeTokens.put(token, ott);
        return token;
    }

    /**
     * Tiêu thụ token 1 lần để tạo Session chính thức
     */
    public AdminSession consumeOneTimeToken(String token) {
        if (token == null || token.trim().isEmpty()) return null;
        OneTimeToken ott = oneTimeTokens.remove(token.trim());
        if (ott == null || ott.isExpired()) {
            return null;
        }
        return createSession(ott.username, ott.accountId);
    }

    private AdminSession createSession(String username, int accountId) {
        AdminSession session = new AdminSession();
        session.sessionToken = generateRandomToken(48);
        session.csrfToken = generateRandomToken(32);
        session.username = username;
        session.accountId = accountId;
        session.loginTime = System.currentTimeMillis();
        session.lastAccessTime = session.loginTime;

        activeSessions.put(session.sessionToken, session);
        return session;
    }

    public AdminSession getValidSession(String token) {
        if (token == null) return null;
        AdminSession s = activeSessions.get(token);
        if (s == null) return null;
        if (s.isExpired()) {
            activeSessions.remove(token);
            return null;
        }
        s.touch();
        return s;
    }

    public void logout(String token) {
        if (token != null) {
            activeSessions.remove(token);
        }
    }

    public boolean validateCsrf(AdminSession session, String csrfToken) {
        if (session == null || csrfToken == null) return false;
        return session.csrfToken.equals(csrfToken.trim());
    }

    private String generateRandomToken(int bytesLength) {
        byte[] bytes = new byte[bytesLength];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
