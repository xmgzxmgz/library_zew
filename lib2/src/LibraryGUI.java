import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LibraryGUI extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;

    public LibraryGUI() {
        setTitle("图书管理系统");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));

        JLabel usernameLabel = new JLabel("用户名:");
        usernameField = new JTextField();
        JLabel passwordLabel = new JLabel("密码:");
        passwordField = new JPasswordField();
        loginButton = new JButton("登录");
        registerButton = new JButton("注册");

        panel.add(usernameLabel);
        panel.add(usernameField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(loginButton);
        panel.add(registerButton);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 处理登录事件
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                // 调用登录方法
                loginUser(username, password);
            }
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 处理注册事件
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                // 调用注册方法
                registerUser(username, password);
            }
        });

        add(panel);
        setVisible(true);
    }

    private void loginUser(String username, String password) {
        // 调用登录方法
        System.out.println("用户登录：" + username);
        // 在这里调用SystemLibraryManagement中的登录方法
    }

    private void registerUser(String username, String password) {
        // 调用注册方法
        System.out.println("用户注册：" + username);
        // 在这里调用SystemLibraryManagement中的注册方法
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new LibraryGUI();
            }
        });
    }
}
