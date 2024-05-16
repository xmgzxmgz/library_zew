import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class LibraryManagementSystem {
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost/libdb";
    static final String USER = "root";
    static final String PASS = "xmgz5656";

    private ArrayList<User> users;
    private ArrayList<Book> books;
    private User loggedInUser;
    private HashMap<String, ArrayList<Book>> categories;

    public LibraryManagementSystem() {
        this.users = new ArrayList<>();
        this.books = new ArrayList<>();
        this.loggedInUser = null;
        this.categories = new HashMap<>();
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement stmt = conn.createStatement()) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS book (id INT AUTO_INCREMENT PRIMARY KEY, title VARCHAR(255), author VARCHAR(255), borrowed BOOLEAN DEFAULT FALSE)";
            stmt.executeUpdate(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    private void borrowBook(String borrowTitle) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE book SET borrowed = true WHERE title = ?")) {
            pstmt.setString(1, borrowTitle);
            int updatedRows = pstmt.executeUpdate();
            if (updatedRows > 0) {
                System.out.println("成功借阅图书：" + borrowTitle);
            } else {
                System.out.println("找不到指定的图书：" + borrowTitle);
            }
        } catch (SQLException e) {
            System.out.println("借阅图书失败：" + e.getMessage());
        }
    }

    private void returnBook(String returnTitle) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE book SET borrowed = false WHERE title = ?")) {
            pstmt.setString(1, returnTitle);
            int updatedRows = pstmt.executeUpdate();
            if (updatedRows > 0) {
                System.out.println("成功归还图书：" + returnTitle);
            } else {
                System.out.println("找不到指定的图书：" + returnTitle);
            }
        } catch (SQLException e) {
            System.out.println("归还图书失败：" + e.getMessage());
        }
    }

    public class User {
        private String username;
        private String password;

        public User(String username, String password) {
            this.username = username;
            this.password = password;
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

        public String getTitle() {
            return title;
        }

        public String getAuthor() {
            return author;
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

            System.out.println("图书添加成功！");
        } catch (SQLException e) {
            System.out.println("添加图书失败：" + e.getMessage());
        }
    }

    public void deleteBook(String title) {
        for (Book book : books) {
            if (book.getTitle().equalsIgnoreCase(title)) {
                books.remove(book);
                System.out.println("图书删除成功！");
                return;
            }
        }
        System.out.println("找不到指定的图书！");
    }

    public void updateBook(String title, String newTitle, String newAuthor) {
        for (Book book : books) {
            if (book.getTitle().equalsIgnoreCase(title)) {
                book = new Book(newTitle, newAuthor);
                System.out.println("图书信息修改成功！");
                return;
            }
        }
        System.out.println("找不到指定的图书！");
    }

    public void searchBook(String title) {
        for (Book book : books) {
            if (book.getTitle().equalsIgnoreCase(title)) {
                System.out.println("书名：" + book.getTitle() + ", 作者：" + book.getAuthor());
                return;
            }
        }
        System.out.println("找不到指定的图书！");
    }

    public void adminMenu() {
        if (loggedInUser != null) {
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("后台管理菜单");
                System.out.println("1. 添加图书");
                System.out.println("2. 删除图书");
                System.out.println("3. 修改图书信息");
                System.out.println("4. 查询图书");
                System.out.println("5. 返回主菜单");

                System.out.print("请选择操作：");
                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        System.out.print("请输入书名: ");
                        String newTitle = scanner.nextLine();
                        System.out.print("请输入作者: ");
                        String newAuthor = scanner.nextLine();
                        System.out.print("请输入类别: ");
                        String category = scanner.nextLine();
                        addBook(newTitle, newAuthor, category);
                        break;
                    case 2:
                        System.out.print("请输入要删除的书名: ");
                        String deleteTitle = scanner.nextLine();
                        deleteBook(deleteTitle);
                        break;
                    case 3:
                        System.out.print("请输入要修改信息的书名: ");
                        String oldTitle = scanner.nextLine();
                        System.out.print("请输入新的书名: ");
                        String updateTitle = scanner.nextLine();
                        System.out.print("请输入新的作者: ");
                        String updateAuthor = scanner.nextLine();
                        updateBook(oldTitle, updateTitle, updateAuthor);
                        break;
                    case 4:
                        System.out.print("请输入要查询的书名: ");
                        String searchTitle = scanner.nextLine();
                        searchBook(searchTitle);
                        break;
                    case 5:
                        return;
                    default:
                        System.out.println("无效的选择，请重新输入！");
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
            System.out.println("3. 搜索图书");
            System.out.println("4. 借书");
            System.out.println("5. 还书");
            System.out.println("6. 后台管理");
            System.out.println("7. 退出");

            System.out.print("请选择操作：");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("请输入用户名: ");
                    String regUsername = scanner.nextLine();
                    System.out.print("请输入密码: ");
                    String regPassword = scanner.nextLine();
                    addUser(regUsername, regPassword);
                    break;
                case 2:
                    System.out.print("请输入用户名: ");
                    String loginUsername = scanner.nextLine();
                    System.out.print("请输入密码: ");
                    String loginPassword = scanner.nextLine();
                    loggedInUser = loginUser(loginUsername, loginPassword);
                    break;
                case 3:
                    System.out.print("请输入搜索关键词: ");
                    String keyword = scanner.nextLine();
                    searchBook(keyword);
                    break;
                case 4:
                    System.out.print("请输入要借阅的书名: ");
                    String borrowTitle = scanner.nextLine();
                    borrowBook(borrowTitle);
                    break;
                case 5:
                    System.out.print("请输入要归还的书名: ");
                    String returnTitle = scanner.nextLine();
                    returnBook(returnTitle);
                    break;
                case 6:
                    adminMenu();
                    break;
                case 7:
                    System.out.println("谢谢使用，再见！");
                    System.exit(0);
                    break;
                default:
                    System.out.println("无效的选择，请重新输入！");
            }
        }
    }

    public static void main(String[] args) {
        LibraryManagementSystem library = new LibraryManagementSystem();
        library.mainMenu();
    }
}
