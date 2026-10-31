package nro.services.func.webadmin;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import nro.models.item.Item;
import nro.models.item.ItemOption;
import nro.models.item.ItemOptionTemplate;
import nro.models.item.ItemTemplate;
import nro.server.Manager;
import nro.services.ItemService;
import nro.services.RewardService;
import nro.services.func.AdminBalanceService;
import nro.services.func.AdminBuffItemService;
import nro.utils.Log;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * Web Admin HTTP Server nhẹ, hiệu năng cao, tích hợp trực tiếp trong Java JVM
 * Sử dụng com.sun.net.httpserver.HttpServer tiêu chuẩn của Java 21,
 * hoàn toàn không phụ thuộc XAMPP, Apache, Node.js hay thư viện ngoài.
 *
 * @author Antigravity
 */
public class AdminWebServer {

    private static AdminWebServer instance;
    private HttpServer server;
    private boolean isRunning = false;
    private int port = 8888;
    // Admin authentication/API must not be exposed to the game player network.
    // Access from the VPS desktop or a private VPN; override only intentionally.
    private String host = System.getProperty("nro.admin.bindAddress", "127.0.0.1");

    private AdminWebServer() {
    }

    public static AdminWebServer gI() {
        if (instance == null) {
            instance = new AdminWebServer();
        }
        return instance;
    }

    public synchronized void start() {
        if (isRunning) return;
        try {
            // Đọc cấu hình từ server.properties nếu có
            try {
                if (Manager.apiPort > 0 && Manager.apiPort != 8080 && Manager.apiPort != 14445) {
                    this.port = Manager.apiPort;
                }
            } catch (Throwable ignored) {
            }

            server = HttpServer.create(new InetSocketAddress(host, port), 0);
            server.setExecutor(Executors.newFixedThreadPool(8));

            // Đăng ký các endpoints
            server.createContext("/", new RootHandler());
            server.createContext("/login", new LoginViewHandler());
            server.createContext("/auth-token", new OneTimeAuthHandler());
            server.createContext("/api/login", new ApiLoginHandler());
            server.createContext("/api/logout", new ApiLogoutHandler());
            server.createContext("/api/options", new ApiOptionsHandler());
            server.createContext("/api/item-info", new ApiItemInfoHandler());
            server.createContext("/api/player-info", new ApiPlayerInfoHandler());
            server.createContext("/api/buff", new ApiBuffHandler());
            server.createContext("/api/balance/credit", new ApiBalanceCreditHandler());
            server.createContext("/api/history/balance", new ApiHistoryBalanceHandler());

            server.start();
            isRunning = true;
            Log.success("[ADMIN_WEB_SERVER] Bảng Quản Trị Trực Quan đã khởi động tại: http://localhost:" + port);
            System.out.println("================================================================================");
            System.out.println(">>> NRO WEB ADMIN DASHBOARD RUNNING AT: http://localhost:" + port + " <<<");
            System.out.println("================================================================================");
        } catch (Throwable e) {
            Log.error(AdminWebServer.class.getName() + ": Lỗi khởi động Web Admin Server: " + e.getMessage());
        }
    }

    public synchronized void stop() {
        if (!isRunning || server == null) return;
        try {
            server.stop(1);
            isRunning = false;
            Log.success("[ADMIN_WEB_SERVER] Đã dừng Web Admin Server.");
        } catch (Throwable e) {
            Log.error(AdminWebServer.class.getName() + ": Lỗi dừng Web Admin Server: " + e.getMessage());
        }
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    /**
     * Handler trang chủ Dashboard
     */
    private static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            String path = ex.getRequestURI().getPath();
            if (!path.equals("/")) {
                sendResponse(ex, 404, "Not Found", "text/plain; charset=UTF-8");
                return;
            }

            AdminWebAuth.AdminSession session = getSessionFromExchange(ex);
            if (session == null) {
                // Chưa đăng nhập -> Chuyển hướng sang trang đăng nhập
                redirect(ex, "/login");
                return;
            }

            String html = AdminWebPage.getHtml(session.username, session.csrfToken);
            sendResponse(ex, 200, html, "text/html; charset=UTF-8");
        }
    }

    /**
     * Handler hiển thị giao diện đăng nhập
     */
    private static class LoginViewHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            AdminWebAuth.AdminSession session = getSessionFromExchange(ex);
            if (session != null) {
                redirect(ex, "/");
                return;
            }
            String query = ex.getRequestURI().getQuery();
            String error = null;
            if (query != null && query.contains("error=invalid_token")) {
                error = "Liên kết đăng nhập nhanh đã hết hạn hoặc không hợp lệ!";
            }
            String html = AdminWebPage.getLoginPage(error);
            sendResponse(ex, 200, html, "text/html; charset=UTF-8");
        }
    }

    /**
     * Handler đăng nhập nhanh bằng One-time Token từ trong game
     */
    private static class OneTimeAuthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            Map<String, String> params = parseQueryParams(ex.getRequestURI().getQuery());
            String token = params.get("t");
            AdminWebAuth.AdminSession session = AdminWebAuth.gI().consumeOneTimeToken(token);
            if (session != null) {
                ex.getResponseHeaders().add("Set-Cookie", "ADMIN_SESSION=" + session.sessionToken + "; Path=/; HttpOnly; SameSite=Lax");
                redirect(ex, "/");
            } else {
                redirect(ex, "/login?error=invalid_token");
            }
        }
    }

    /**
     * Handler API Login
     */
    private static class ApiLoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!ex.getRequestMethod().equalsIgnoreCase("POST")) {
                sendResponse(ex, 405, "Method Not Allowed", "text/plain");
                return;
            }

            String body = readRequestBody(ex);
            String username = null;
            String password = null;
            boolean isJson = isJsonRequest(ex);

            if (isJson) {
                try {
                    JSONObject obj = (JSONObject) new JSONParser().parse(body);
                    username = (String) obj.get("username");
                    password = (String) obj.get("password");
                } catch (Exception ignored) {
                }
            } else {
                Map<String, String> form = parseFormData(body);
                username = form.get("username");
                password = form.get("password");
            }

            AdminWebAuth.AdminSession session = AdminWebAuth.gI().login(username, password);
            if (session != null) {
                if (isJson) {
                    JSONObject res = new JSONObject();
                    res.put("success", true);
                    res.put("token", session.sessionToken);
                    res.put("csrfToken", session.csrfToken);
                    res.put("username", session.username);
                    ex.getResponseHeaders().add("Set-Cookie", "ADMIN_SESSION=" + session.sessionToken + "; Path=/; HttpOnly; SameSite=Lax");
                    sendResponse(ex, 200, res.toJSONString(), "application/json; charset=UTF-8");
                } else {
                    ex.getResponseHeaders().add("Set-Cookie", "ADMIN_SESSION=" + session.sessionToken + "; Path=/; HttpOnly; SameSite=Lax");
                    redirect(ex, "/");
                }
            } else {
                if (isJson) {
                    JSONObject res = new JSONObject();
                    res.put("success", false);
                    res.put("message", "Sai tài khoản, mật khẩu hoặc tài khoản không có quyền Admin!");
                    sendResponse(ex, 401, res.toJSONString(), "application/json; charset=UTF-8");
                } else {
                    String html = AdminWebPage.getLoginPage("Sai tài khoản, mật khẩu hoặc không có quyền Admin!");
                    sendResponse(ex, 401, html, "text/html; charset=UTF-8");
                }
            }
        }
    }

    /**
     * Handler API Logout
     */
    private static class ApiLogoutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            AdminWebAuth.AdminSession session = getSessionFromExchange(ex);
            if (session != null) {
                AdminWebAuth.gI().logout(session.sessionToken);
            }
            ex.getResponseHeaders().add("Set-Cookie", "ADMIN_SESSION=; Path=/; Expires=Thu, 01 Jan 1970 00:00:00 GMT");
            JSONObject res = new JSONObject();
            res.put("success", true);
            sendResponse(ex, 200, res.toJSONString(), "application/json; charset=UTF-8");
        }
    }

    /**
     * Handler lấy toàn bộ Option từ database / Manager.ITEM_OPTION_TEMPLATES
     */
    private static class ApiOptionsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            AdminWebAuth.AdminSession session = getSessionFromExchange(ex);
            if (session == null) {
                sendResponse(ex, 401, "{\"error\":\"Unauthorized\"}", "application/json");
                return;
            }

            JSONArray arr = new JSONArray();
            for (ItemOptionTemplate iot : Manager.ITEM_OPTION_TEMPLATES) {
                if (iot == null || iot.name == null) continue;
                JSONObject obj = new JSONObject();
                obj.put("id", iot.id);
                obj.put("name", iot.name);
                obj.put("type", iot.type);
                obj.put("category", getOptionCategory(iot.id, iot.name));
                boolean hasParam = iot.name.contains("#");
                obj.put("hasParam", hasParam);

                // Gợi ý param mặc định hợp lý
                int defParam = 0;
                if (hasParam) {
                    if (iot.id == 50 || iot.id == 77 || iot.id == 103) defParam = 100;
                    else if (iot.id == 14) defParam = 20;
                    else if (iot.id == 6 || iot.id == 7) defParam = 100000;
                    else if (iot.id == 0) defParam = 50000;
                    else if (iot.id == 47) defParam = 5000;
                    else if (iot.id == 95 || iot.id == 96) defParam = 15;
                    else if (iot.id == 97) defParam = 20;
                    else defParam = 10;
                }
                obj.put("defaultParam", defParam);
                arr.add(obj);
            }

            sendResponse(ex, 200, arr.toJSONString(), "application/json; charset=UTF-8");
        }
    }

    /**
     * Handler tra cứu thông tin vật phẩm
     */
    private static class ApiItemInfoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            AdminWebAuth.AdminSession session = getSessionFromExchange(ex);
            if (session == null) {
                sendResponse(ex, 401, "{\"error\":\"Unauthorized\"}", "application/json");
                return;
            }

            Map<String, String> query = parseQueryParams(ex.getRequestURI().getQuery());
            String idStr = query.get("id");
            if (idStr == null || idStr.trim().isEmpty()) {
                sendResponse(ex, 400, "{\"error\":\"Thiếu ID vật phẩm\"}", "application/json; charset=UTF-8");
                return;
            }

            try {
                int id = Integer.parseInt(idStr.trim());
                if (id < 0 || id >= Manager.ITEM_TEMPLATES.size()) {
                    sendResponse(ex, 404, "{\"error\":\"Vật phẩm ID " + id + " không tồn tại!\"}", "application/json; charset=UTF-8");
                    return;
                }

                ItemTemplate it = Manager.ITEM_TEMPLATES.get(id);
                if (it == null || it.name == null || it.name.contains("[Vô hiệu hóa]")) {
                    sendResponse(ex, 404, "{\"error\":\"Vật phẩm ID " + id + " đã bị vô hiệu hóa hoặc không tồn tại!\"}", "application/json; charset=UTF-8");
                    return;
                }

                JSONObject res = new JSONObject();
                res.put("id", it.id);
                res.put("name", it.name);
                res.put("type", it.type);
                res.put("gender", it.gender);
                res.put("iconID", it.iconID);
                res.put("description", it.description);

                // Lấy option gốc mẫu
                JSONArray baseOpts = new JSONArray();
                try {
                    Item sample = ItemService.gI().createNewItem(it.id);
                    RewardService.gI().initBaseOptionClothes(it.id, it.type, sample.itemOptions);
                    for (ItemOption io : sample.itemOptions) {
                        if (io != null && io.optionTemplate != null) {
                            JSONObject bo = new JSONObject();
                            bo.put("id", io.optionTemplate.id);
                            bo.put("name", io.getOptionString());
                            bo.put("param", io.param);
                            baseOpts.add(bo);
                        }
                    }
                } catch (Throwable ignored) {
                }
                res.put("baseOptions", baseOpts);

                sendResponse(ex, 200, res.toJSONString(), "application/json; charset=UTF-8");
            } catch (Exception e) {
                sendResponse(ex, 400, "{\"error\":\"ID vật phẩm không hợp lệ: " + e.getMessage() + "\"}", "application/json; charset=UTF-8");
            }
        }
    }

    /**
     * Handler tra cứu thông tin người chơi / tài khoản
     */
    private static class ApiPlayerInfoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            AdminWebAuth.AdminSession session = getSessionFromExchange(ex);
            if (session == null) {
                sendResponse(ex, 401, "{\"error\":\"Unauthorized\"}", "application/json");
                return;
            }

            Map<String, String> query = parseQueryParams(ex.getRequestURI().getQuery());
            String q = query.get("query");
            if (q == null || q.trim().isEmpty()) {
                sendResponse(ex, 400, "{\"error\":\"Vui lòng nhập tên hoặc ID\"}", "application/json; charset=UTF-8");
                return;
            }

            AdminBalanceService.AccountInfo info = AdminBalanceService.gI().lookupAccount(q.trim());
            if (info == null) {
                sendResponse(ex, 404, "{\"error\":\"Không tìm thấy người chơi hoặc tài khoản phù hợp!\"}", "application/json; charset=UTF-8");
                return;
            }

            JSONObject res = new JSONObject();
            res.put("accountId", info.accountId);
            res.put("username", info.username);
            res.put("targetPlayerId", info.playerId != null ? info.playerId : 0);
            res.put("targetPlayerName", info.playerName != null ? info.playerName : info.username);
            res.put("isOnline", info.isOnline);
            res.put("gender", info.gender);
            res.put("power", info.power);
            res.put("currentVnd", info.currentVnd);
            res.put("currentTongnap", info.currentTongnap);

            sendResponse(ex, 200, res.toJSONString(), "application/json; charset=UTF-8");
        }
    }

    /**
     * Handler API Buff Vật Phẩm (POST)
     */
    private static class ApiBuffHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!ex.getRequestMethod().equalsIgnoreCase("POST")) {
                sendResponse(ex, 405, "Method Not Allowed", "text/plain");
                return;
            }

            AdminWebAuth.AdminSession session = getSessionFromExchange(ex);
            if (session == null) {
                sendResponse(ex, 401, "{\"success\":false,\"message\":\"Chưa đăng nhập!\"}", "application/json; charset=UTF-8");
                return;
            }

            String csrf = ex.getRequestHeaders().getFirst("X-CSRF-Token");
            if (!AdminWebAuth.gI().validateCsrf(session, csrf)) {
                sendResponse(ex, 403, "{\"success\":false,\"message\":\"CSRF token không hợp lệ!\"}", "application/json; charset=UTF-8");
                return;
            }

            String body = readRequestBody(ex);
            try {
                JSONObject payload = (JSONObject) new JSONParser().parse(body);
                String recQuery = (String) payload.get("recipientQuery");
                long itemTemplateId = ((Number) payload.get("itemTemplateId")).longValue();
                long quantity = ((Number) payload.get("quantity")).longValue();
                boolean keepDefault = payload.get("keepDefaultOptions") != null && (Boolean) payload.get("keepDefaultOptions");
                JSONArray optsArr = (JSONArray) payload.get("options");

                Map<Integer, Integer> customOptions = new LinkedHashMap<>();
                if (optsArr != null) {
                    for (Object o : optsArr) {
                        JSONObject opt = (JSONObject) o;
                        int id = ((Number) opt.get("id")).intValue();
                        int param = ((Number) opt.get("param")).intValue();
                        customOptions.put(id, param);
                    }
                }

                // Thực thi buff thông qua AdminBuffItemService
                JSONObject result = AdminBuffItemService.gI().buffItemDirect(
                        session.username,
                        recQuery,
                        (int) itemTemplateId,
                        (int) quantity,
                        customOptions,
                        keepDefault
                );

                int code = Boolean.TRUE.equals(result.get("success")) ? 200 : 400;
                sendResponse(ex, code, result.toJSONString(), "application/json; charset=UTF-8");
            } catch (Exception e) {
                Log.error(AdminWebServer.class, e, "Lỗi thực thi API Buff");
                JSONObject err = new JSONObject();
                err.put("success", false);
                err.put("message", "Lỗi xử lý buff: " + e.getMessage());
                sendResponse(ex, 500, err.toJSONString(), "application/json; charset=UTF-8");
            }
        }
    }

    /**
     * Handler API Cộng Số Dư Nạp Tiền (POST)
     */
    private static class ApiBalanceCreditHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!ex.getRequestMethod().equalsIgnoreCase("POST")) {
                sendResponse(ex, 405, "Method Not Allowed", "text/plain");
                return;
            }

            AdminWebAuth.AdminSession session = getSessionFromExchange(ex);
            if (session == null) {
                sendResponse(ex, 401, "{\"success\":false,\"message\":\"Chưa đăng nhập!\"}", "application/json; charset=UTF-8");
                return;
            }

            String csrf = ex.getRequestHeaders().getFirst("X-CSRF-Token");
            if (!AdminWebAuth.gI().validateCsrf(session, csrf)) {
                sendResponse(ex, 403, "{\"success\":false,\"message\":\"CSRF token không hợp lệ!\"}", "application/json; charset=UTF-8");
                return;
            }

            String body = readRequestBody(ex);
            try {
                JSONObject payload = (JSONObject) new JSONParser().parse(body);
                String recQuery = (String) payload.get("recipientQuery");
                long amount = ((Number) payload.get("amount")).longValue();
                String transCode = (String) payload.get("transCode");
                String note = (String) payload.get("note");
                String mode = (String) payload.get("mode");

                AdminBalanceService.CreditResult cr = AdminBalanceService.gI().creditBalance(
                        session.username,
                        recQuery,
                        (int) amount,
                        transCode,
                        note,
                        mode
                );

                JSONObject res = new JSONObject();
                res.put("success", cr.success);
                res.put("message", cr.message);
                res.put("transCode", cr.transCode);
                res.put("amount", cr.amount);
                res.put("balanceBefore", cr.balanceBefore);
                res.put("balanceAfter", cr.balanceAfter);
                res.put("tongnapBefore", cr.tongnapBefore);
                res.put("tongnapAfter", cr.tongnapAfter);
                res.put("mode", cr.mode);
                res.put("isDuplicate", cr.isDuplicate);

                int code = cr.success ? 200 : 400;
                sendResponse(ex, code, res.toJSONString(), "application/json; charset=UTF-8");
            } catch (Exception e) {
                Log.error(AdminWebServer.class, e, "Lỗi thực thi API Cộng số dư");
                JSONObject err = new JSONObject();
                err.put("success", false);
                err.put("message", "Lỗi xử lý cộng tiền: " + e.getMessage());
                sendResponse(ex, 500, err.toJSONString(), "application/json; charset=UTF-8");
            }
        }
    }

    /**
     * Handler xem lịch sử cộng tiền
     */
    private static class ApiHistoryBalanceHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            AdminWebAuth.AdminSession session = getSessionFromExchange(ex);
            if (session == null) {
                sendResponse(ex, 401, "{\"error\":\"Unauthorized\"}", "application/json");
                return;
            }

            List<AdminBalanceService.CreditHistoryEntry> list = AdminBalanceService.gI().getTransactionHistory(50, null);
            JSONArray arr = new JSONArray();
            for (AdminBalanceService.CreditHistoryEntry e : list) {
                JSONObject obj = new JSONObject();
                obj.put("id", e.id);
                obj.put("transCode", e.transCode);
                obj.put("accountId", e.accountId);
                obj.put("playerId", e.playerId != null ? e.playerId : 0);
                obj.put("playerName", e.playerName != null ? e.playerName : "N/A");
                obj.put("adminUsername", e.adminUsername);
                obj.put("balanceBefore", e.balanceBefore);
                obj.put("amount", e.amount);
                obj.put("balanceAfter", e.balanceAfter);
                obj.put("tongnapBefore", e.tongnapBefore);
                obj.put("tongnapAfter", e.tongnapAfter);
                obj.put("mode", e.mode);
                obj.put("note", e.note);
                obj.put("createdAt", e.createdAt);
                obj.put("status", e.status);
                arr.add(obj);
            }

            sendResponse(ex, 200, arr.toJSONString(), "application/json; charset=UTF-8");
        }
    }

    // ==========================================
    // UTILITY METHODS
    // ==========================================

    private static AdminWebAuth.AdminSession getSessionFromExchange(HttpExchange ex) {
        String token = null;
        // 1. Thử đọc từ header Cookie
        String cookieHeader = ex.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader != null) {
            for (String cookie : cookieHeader.split(";")) {
                String[] pair = cookie.trim().split("=");
                if (pair.length == 2 && pair[0].equalsIgnoreCase("ADMIN_SESSION")) {
                    token = pair[1];
                    break;
                }
            }
        }
        // 2. Thử đọc từ header Authorization
        if (token == null) {
            String authHeader = ex.getRequestHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7).trim();
            }
        }
        return AdminWebAuth.gI().getValidSession(token);
    }

    private static void sendResponse(HttpExchange ex, int statusCode, String responseText, String contentType) throws IOException {
        byte[] bytes = responseText.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", contentType);
        ex.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
        ex.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void redirect(HttpExchange ex, String location) throws IOException {
        ex.getResponseHeaders().set("Location", location);
        ex.sendResponseHeaders(302, -1);
        ex.close();
    }

    private static String readRequestBody(HttpExchange ex) throws IOException {
        InputStream is = ex.getRequestBody();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int read;
        while ((read = is.read(buffer)) != -1) {
            if (baos.size() + read > 256 * 1024) {
                throw new IOException("Admin request too large");
            }
            baos.write(buffer, 0, read);
        }
        return baos.toString(StandardCharsets.UTF_8);
    }

    private static boolean isJsonRequest(HttpExchange ex) {
        String ct = ex.getRequestHeaders().getFirst("Content-Type");
        return ct != null && ct.toLowerCase().contains("application/json");
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                map.put(URLDecoder.decode(entry[0], StandardCharsets.UTF_8), URLDecoder.decode(entry[1], StandardCharsets.UTF_8));
            } else if (entry.length == 1) {
                map.put(URLDecoder.decode(entry[0], StandardCharsets.UTF_8), "");
            }
        }
        return map;
    }

    private static Map<String, String> parseFormData(String body) {
        return parseQueryParams(body);
    }

    /**
     * Phân loại Option theo 11 danh mục yêu cầu
     */
    public static String getOptionCategory(int id, String name) {
        if (name == null) return "Thuộc tính khác";
        String lower = name.toLowerCase();
        String unaccented = stripDiacritics(lower);

        // 1. Set Kích Hoạt (IDs 127-144, hoặc chứa 'Set ', 'Kích hoạt')
        if ((id >= 127 && id <= 144) || lower.contains("set ") || unaccented.contains("kich hoat")) {
            return "Set Kích Hoạt";
        }
        // 2. Sao pha lê
        if (id == 107 || lower.contains("pha lê") || unaccented.contains("pha le") || lower.contains("sao")) {
            return "Sao pha lê";
        }
        // 3. Hút HP / Hút KI / Hồi phục
        if (id == 8 || id == 95 || id == 96 || id == 104 || id == 27 || id == 28 || id == 100 || id == 101 ||
                unaccented.contains("hut") || unaccented.contains("hoi") || (unaccented.contains("bien") && unaccented.contains("thanh"))) {
            return "Hút HP / Hút KI / Hồi phục";
        }
        // 4. Né đòn / Phản sát thương / Xuyên giáp
        if (id == 97 || id == 98 || id == 99 || (id >= 108 && id <= 110) || id == 159 ||
                unaccented.contains("ne") || unaccented.contains("phan") || unaccented.contains("xuyen")) {
            return "Né đòn / Phản sát thương / Xuyên giáp";
        }
        // 5. Chí mạng
        if (id == 14 || id == 5 || unaccented.contains("chi mang") || lower.contains("crit")) {
            return "Chí mạng";
        }
        // 6. Sức đánh
        if (id == 0 || id == 50 || id == 49 || id == 147 ||
                unaccented.contains("suc danh") || unaccented.contains("tan cong") || lower.contains("dame")) {
            return "Sức đánh";
        }
        // 7. HP
        if (id == 6 || id == 77 || id == 22 || id == 162 || id == 173 ||
                lower.contains("hp") || unaccented.contains("mau")) {
            return "HP";
        }
        // 8. KI
        if (id == 7 || id == 103 || id == 23 || id == 163 ||
                lower.contains("ki") || lower.contains("mp") || unaccented.contains("nang luong")) {
            return "KI";
        }
        // 9. Giáp
        if (id == 47 || id == 94 || unaccented.contains("giap") || lower.contains("def")) {
            return "Giáp";
        }
        // 10. Thuộc tính khác
        return "Thuộc tính khác";
    }

    public static String removeAccents(String str) {
        return stripDiacritics(str).toLowerCase();
    }

    public static String stripDiacritics(String str) {
        if (str == null) return "";
        String nfd = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfd).replaceAll("").replaceAll("đ", "d").replaceAll("Đ", "D");
    }
}
