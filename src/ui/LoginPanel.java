package ui;

import util.ThemeManager;
import util.PasswordUtils;
import db.DBConnection;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginPanel extends JPanel {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel messageLabel;
    private BufferedImage backgroundImage; // Store background image as a field

    public interface LoginListener {
        void onLoginSuccess(User user);
    }

    private LoginListener loginListener;

    public LoginPanel(LoginListener listener) {
        this.loginListener = listener;
        setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
        setMinimumSize(Toolkit.getDefaultToolkit().getScreenSize());
        setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());

        // Set background image for login panel
        try {
            backgroundImage = javax.imageio.ImageIO.read(getClass().getResource("/Images/Bg-2.jpg"));
            if (backgroundImage == null) {
                System.out.println("Background image not found! Check /Images/Bg-1.jpg");
            }
        } catch (Exception e) {
            System.out.println("Error loading background image: " + e.getMessage());
        }

        setLayout(new GridBagLayout());
        setOpaque(false);

        // Card panel for modern look
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }
        };
        card.setOpaque(false);
        card.setBorder(new LineBorder(ThemeManager.getAccent(), 2, true));
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(400, 420));
        card.setMaximumSize(new Dimension(400, 420));
        card.setMinimumSize(new Dimension(320, 350));
        card.setBorder(new EmptyBorder(32, 32, 32, 32));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;

        // Logo/Icon
        JLabel iconLabel = new JLabel();
        iconLabel.setHorizontalAlignment(JLabel.CENTER);
        java.net.URL iconUrl = getClass().getResource("/Icons/locked.png");
        if (iconUrl != null) {
            ImageIcon icon = new ImageIcon(iconUrl);
            Image img = icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
            iconLabel.setIcon(new ImageIcon(img));
        }
        card.add(iconLabel, c);

        c.gridy++;
        JLabel titleLabel = new JLabel("Offline Exam Proctor System", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(ThemeManager.getAccent());
        card.add(titleLabel, c);

        c.gridy++;
        c.gridwidth = 1;
        JLabel userLabel = new JLabel("Username:") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                // Three-color gradient: red -> yellow -> green
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp1 = new GradientPaint(0, 0, Color.RED, w / 2, h, Color.YELLOW);
                GradientPaint gp2 = new GradientPaint(w / 2, 0, Color.YELLOW, w, h, Color.GREEN);
                g2.setPaint(gp1);
                g2.setFont(getFont());
                g2.drawString(getText().substring(0, getText().length() / 2), 0, h - g2.getFontMetrics().getDescent());
                int mid = g2.getFontMetrics().stringWidth(getText().substring(0, getText().length() / 2));
                g2.setPaint(gp2);
                g2.drawString(getText().substring(getText().length() / 2), mid, h - g2.getFontMetrics().getDescent());
                g2.dispose();
            }
        };
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        userLabel.setForeground(new Color(0, 0, 0, 0)); // Make default text transparent
        card.add(userLabel, c);
        c.gridx = 1;
        usernameField = new JTextField(20) {
            @Override
            public boolean isOpaque() {
                return false;
            }
        };
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        usernameField.setBorder(new LineBorder(ThemeManager.getAccent(), 1, true));
        usernameField.setBackground(new Color(0, 0, 0, 0));
        usernameField.setForeground(Color.WHITE); // Set input text color to white
        usernameField.setCaretColor(Color.WHITE); // Set caret (pointer) color to white
        card.add(usernameField, c);

        c.gridx = 0;
        c.gridy++;
        JLabel passLabel = new JLabel("Password:") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp1 = new GradientPaint(0, 0, Color.RED, w / 2, h, Color.YELLOW);
                GradientPaint gp2 = new GradientPaint(w / 2, 0, Color.YELLOW, w, h, Color.GREEN);
                g2.setPaint(gp1);
                g2.setFont(getFont());
                g2.drawString(getText().substring(0, getText().length() / 2), 0, h - g2.getFontMetrics().getDescent());
                int mid = g2.getFontMetrics().stringWidth(getText().substring(0, getText().length() / 2));
                g2.setPaint(gp2);
                g2.drawString(getText().substring(getText().length() / 2), mid, h - g2.getFontMetrics().getDescent());
                g2.dispose();
            }
        };
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        passLabel.setForeground(new Color(0, 0, 0, 0)); // Make default text transparent
        card.add(passLabel, c);
        c.gridx = 1;
        passwordField = new JPasswordField(20) {
            @Override
            public boolean isOpaque() {
                return false;
            }
        };
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        passwordField.setBorder(new LineBorder(ThemeManager.getAccent(), 1, true));
        passwordField.setBackground(new Color(0, 0, 0, 0));
        passwordField.setForeground(Color.WHITE); // Set input text color to white
        passwordField.setCaretColor(Color.WHITE); // Set caret (pointer) color to white
        card.add(passwordField, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.NONE; // Allow button to use preferred size
        c.ipady = 12; // Add vertical padding if needed
        c.ipadx = 40;
        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 24));
        loginButton.setBackground(new Color(46, 204, 113)); // Green color
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder()); // No border
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setPreferredSize(new Dimension(180, 40)); // Reduced width, increased height
        card.add(loginButton, c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipady = 0;
        c.ipadx = 0;

        c.gridy++;
        messageLabel = new JLabel("", JLabel.CENTER);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageLabel.setForeground(Color.RED);
        card.add(messageLabel, c);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(card, gbc);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (backgroundImage != null) {
            // Draw the background image scaled to fill the panel
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            super.paintComponent(g);
        }
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter username and password.");
            return;
        }
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM users WHERE username = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String hash = rs.getString("password_hash");
                if (PasswordUtils.hashPassword(password).equals(hash)) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setRole(rs.getString("role"));
                    user.setFullName(rs.getString("full_name"));
                    user.setEmail(rs.getString("email"));
                    if (loginListener != null)
                        loginListener.onLoginSuccess(user);
                } else {
                    messageLabel.setText("Invalid password.");
                }
            } else {
                messageLabel.setText("User not found.");
            }
        } catch (Exception ex) {
            messageLabel.setText("Database error: " + ex.getMessage());
        }
    }
}
