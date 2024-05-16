import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BookDAO {
    private static final String URL_DB = "jdbc:mysql://localhost/libdb";
    private static final String USER = "root";
    private static final String PASS = "xmgz5656";
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL_DB, USER, PASS);
    }

    public void addBookToDatabase(String title, String author) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO book (title, author) VALUES (?, ?)")) {
            pstmt.setString(1, title);
            pstmt.setString(2, author);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("添加书籍到数据库失败：" + e.getMessage());
        }
    }

    public void deleteBookFromDatabase(String title) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM book WHERE title = ?")) {
            pstmt.setString(1, title);
            int deletedRows = pstmt.executeUpdate();
            if (deletedRows > 0) {
                System.out.println("成功删除书籍：" + title);
            } else {
                System.out.println("找不到指定的书籍：" + title);
            }
        } catch (SQLException e) {
            System.out.println("删除书籍失败：" + e.getMessage());
        }
    }

    public void updateBookInDatabase(String oldTitle, String newTitle, String newAuthor) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE book SET title = ?, author = ? WHERE title = ?")) {
            pstmt.setString(1, newTitle);
            pstmt.setString(2, newAuthor);
            pstmt.setString(3, oldTitle);
            int updatedRows = pstmt.executeUpdate();
            if (updatedRows > 0) {
                System.out.println("成功修改书籍信息！");
            } else {
                System.out.println("找不到指定的书籍：" + oldTitle);
            }
        } catch (SQLException e) {
            System.out.println("修改书籍信息失败：" + e.getMessage());
        }
    }
}

