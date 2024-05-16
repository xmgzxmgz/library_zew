import java.sql.*;
import java.util.HashMap;
import java.util.Scanner;
import java.util.ArrayList;

public class SystemLibraryManagement {
    static final String DRIVER_JDBC = "com.mysql.cj.jdbc.Driver";
    static final String URL_DB = "jdbc:mysql://localhost/libdb";
    static final String PASS = "xmgz5656";
    static final String USER = "root";

    private ArrayList<User> users; // 用户列表
    private User loggedInUser; // 当前登录用户
    private HashMap<String, ArrayList<Book>> categories; // 图书分类
    private ArrayList<Book> books; // 图书列表

    public SystemLibraryManagement() {
        this.users = new ArrayList<>();
        this.loggedInUser = null;
        this.categories = new HashMap<>();
        this.books = new ArrayList<>();
        initializeDatabase(); // 初始化数据库
    }

    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(URL_DB, USER, PASS);
             Statement stmt = conn.createStatement()) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS book (id INT AUTO_INCREMENT PRIMARY KEY, title VARCHAR(255), author VARCHAR(255), borrowed BOOLEAN DEFAULT FALSE)";
            stmt.executeUpdate(createTableSQL); // 创建图书表
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL_DB, USER, PASS);
    }

    private void borrowBook(String titleBorrow) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM book WHERE title = ? AND borrowed = true")) {
            pstmt.setString(1, titleBorrow);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                System.out.println("该书已被借阅！");
            } else {
                try (PreparedStatement updateStmt = conn.prepareStatement("UPDATE book SET borrowed = true WHERE title = ?")) {
                    updateStmt.setString(1, titleBorrow);
                    int updatedRows = updateStmt.executeUpdate();
                    if (updatedRows > 0) {
                        System.out.println("成功借阅书籍：" + titleBorrow);
                    } else {
                        System.out.println("找不到指定的书籍：" + titleBorrow);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("借阅书籍失败：" + e.getMessage());
        }
    }

    private void returnBook(String titleReturn) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE book SET borrowed = false WHERE title = ?")) {
            pstmt.setString(1, titleReturn);
            int updatedRows = pstmt.executeUpdate();
            if (updatedRows > 0) {
                System.out.println("成功归还书籍：" + titleReturn);
            } else {
                System.out.println("找不到指定的书籍：" + titleReturn);
            }
        } catch (SQLException e) {
            System.out.println("归还书籍失败：" + e.getMessage());
        }
    }

    public static class User {
        private String password;
        private String username;

        public User(String username, String password) {
            this.password = password;
            this.username = username;
        }

        public boolean authenticate(String username, String password) {
            return this.username.equals(username) && this.password.equals(password);
        }
    }

    public class Book {
        private String title;
        private String author;

        public Book(String title, String author) {
            this.title = title;
            this.author = author;
        }

        public String getAuthor() {
            return author;
        }

        public String getTitle() {
            return title;
        }
    }

    private void addUserToDatabase(String username, String password) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO user (username, password) VALUES (?, ?)")) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("添加用户到数据库失败：" + e.getMessage());
        }
    }

    public void addUser(String username, String password) {
        User newUser = new User(username, password);
        users.add(newUser);
        addUserToDatabase(username, password);
        System.out.println("用户注册成功！");
    }

    public User loginUser(String username, String password) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM user WHERE username = ? AND password = ?")) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                System.out.println("登录成功！");
                return new User(username, password);
            } else {
                System.out.println("用户名或密码错误！");
                return null;
            }
        } catch (SQLException e) {
            System.out.println("登录失败：" + e.getMessage());
            return null;
        }
    }

    public void addBook(String title, String author, String category) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO book (title, author) VALUES (?, ?)")) {
            pstmt.setString(1, title);
            pstmt.setString(2, author);
            pstmt.executeUpdate();

            if (!categories.containsKey(category)) {
                categories.put(category, new ArrayList<>());
            }
            categories.get(category).add(new Book(title, author));

            System.out.println("成功添加书籍！");
        } catch (SQLException e) {
            System.out.println("添加书籍失败：" + e.getMessage());
        }
    }

    public void deleteBook(String title) {
        for (Book book : books) {
            if (book.getTitle().equalsIgnoreCase(title)) {
                books.remove(book);
                System.out.println("成功删除书籍！");
                return;
            }
        }
        System.out.println("找不到指定的书籍！");
    }

    public void updateBook(String title, String newTitle, String newAuthor) {
        for (Book book : books) {
            if (book.getTitle().equalsIgnoreCase(title)) {
                book = new Book(newTitle, newAuthor);
                System.out.println("成功修改书籍信息！");
                return;
            }
        }
        System.out.println("找不到指定的书籍！");
    }

    public void searchBook(String title) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM book WHERE title LIKE ?")) {
            pstmt.setString(1, "%" + title + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String bookTitle = rs.getString("title");
                String author = rs.getString("author");
                System.out.println("书名：" + bookTitle + "，作者：" + author);
            }
        } catch (SQLException e) {
            System.out.println("查找书籍失败：" + e.getMessage());
        }
    }

    public void adminMenu() {
        if (loggedInUser != null) {
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("管理员菜单");
                System.out.println("1. 添加书籍");
                System.out.println("2. 删除书籍");
                System.out.println("3. 修改书籍信息");
                System.out.println("4. 查找书籍");
                System.out.println("5. 返回主菜单");
                System.out.print("请选择操作：");
                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        System.out.print("请输入书名：");
                        String newTitle = scanner.nextLine();
                        System.out.print("请输入作者：");
                        String newAuthor = scanner.nextLine();
                        System.out.print("请输入分类：");
                        String category = scanner.nextLine();
                        addBook(newTitle, newAuthor, category);
                        break;
                    case 2:
                        System.out.print("请输入要删除的书籍标题：");
                        String deleteTitle = scanner.nextLine();
                        deleteBook(deleteTitle);
                        break;
                    case 3:
                        System.out.print("请输入要修改的书籍标题：");
                        String oldTitle = scanner.nextLine();
                        System.out.print("请输入新标题：");
                        String updateTitle = scanner.nextLine();
                        System.out.print("请输入新作者：");
                        String updateAuthor = scanner.nextLine();
                        updateBook(oldTitle, updateTitle, updateAuthor);
                        break;
                    case 4:
                        System.out.print("请输入要查找的书籍标题：");
                        String searchTitle = scanner.nextLine();
                        searchBook(searchTitle);
                        break;
                    case 5:
                        return;
                    default:
                        System.out.println("无效的选项，请重试！");
                }
            }
        } else {
            System.out.println("请先登录！");
        }
    }

    public void mainMenu() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("欢迎使用图书管理系统！");
            System.out.println("1. 用户注册");
            System.out.println("2. 用户登录");
            System.out.println("3. 查找书籍");
            System.out.println("4. 借阅书籍");
            System.out.println("5. 归还书籍");
            System.out.println("6. 管理员菜单");
            System.out.println("7. 退出");

            System.out.print("请选择操作：");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("请输入用户名：");
                    String regUsername = scanner.nextLine();
                    System.out.print("请输入密码：");
                    String regPassword = scanner.nextLine();
                    addUser(regUsername, regPassword);
                    break;
                case 2:
                    System.out.print("请输入用户名：");
                    String loginUsername = scanner.nextLine();
                    System.out.print("请输入密码：");
                    String loginPassword = scanner.nextLine();
                    loggedInUser = loginUser(loginUsername, loginPassword);
                    break;
                case 3:
                    System.out.print("请输入搜索关键词：");
                    String keyword = scanner.nextLine();
                    searchBook(keyword);
                    break;
                case 4:
                    System.out.print("请输入要借阅的书籍标题：");
                    String borrowTitle = scanner.nextLine();
                    borrowBook(borrowTitle);
                    break;
                case 5:
                    System.out.print("请输入要归还的书籍标题：");
                    String returnTitle = scanner.nextLine();
                    returnBook(returnTitle);
                    break;
                case 6:
                    adminMenu();
                    break;
                case 7:
                    System.out.println("感谢使用系统，再见！");
                    System.exit(0);
                    break;
                default:
                    System.out.println("无效的选项，请重试！");
            }
        }
    }

    public static void main(String[] args) {
        SystemLibraryManagement library = new SystemLibraryManagement();
        library.mainMenu();
    }
}