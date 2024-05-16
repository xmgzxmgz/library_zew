import java.sql.*;

public class UserDAO {
    private static final String URL_DB = "jdbc:mysql://localhost/libdb";
    private static final String USER = "root";
    private static final String PASS = "xmgz5656";
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL_DB, USER, PASS);
    }

    public void addUserToDatabase(String username, String password) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO user (username, password) VALUES (?, ?)")) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("添加用户到数据库失败：" + e.getMessage());
        }
    }

    public SystemLibraryManagement.User loginUser(String username, String password) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM user WHERE username = ? AND password = ?")) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                System.out.println("登录成功！");
                return new SystemLibraryManagement.User(username, password);
            } else {
                System.out.println("用户名或密码错误！");
                return null;
            }
        } catch (SQLException e) {
            System.out.println("登录失败：" + e.getMessage());
            return null;
        }
    }
}
