package nro.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TruyVanSQL {

    // Thông tin kết nối đến cơ sở dữ liệu
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost/nronotbuff_sql";

    // Thông tin đăng nhập vào cơ sở dữ liệu
    static final String USER = "root";
    static final String PASS = "";

    public static String getPlayerNameById(int playerId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        String playerName = null;

        try {
            // Bước 1: Đăng ký JDBC Driver
            Class.forName(JDBC_DRIVER);

            // Bước 2: Mở kết nối
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Bước 3: Tạo truy vấn SQL
            String sql = "SELECT name FROM player WHERE id = ?";
            
            stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, playerId);

            // Bước 4: Thực hiện truy vấn
            ResultSet rs = stmt.executeQuery();

            // Bước 5: Xử lý kết quả truy vấn
            if (rs.next()) {
                playerName = rs.getString("name");
            }
            // Bước 6: Đóng tất cả các tài nguyên
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Bước 7: Đảm bảo đóng tất cả các tài nguyên khi kết thúc
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException se2) {
            }
            try {
                if (conn != null) conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

        return playerName;
    }

    public static void main(String[] args) {
        // Gọi hàm để lấy tên của người chơi với ID cụ thể
        int playerId = 1; // Điền ID cần tìm kiếm
        String playerName = getPlayerNameById(playerId);

        // In ra tên của người chơi (hoặc xử lý nó theo nhu cầu của bạn)
        System.out.println("Player Name: " + playerName);
    }
}
