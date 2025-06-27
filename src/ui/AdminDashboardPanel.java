package ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.User;
import util.ThemeManager;

public class AdminDashboardPanel extends JPanel {
    private final User adminUser;
    private MainFrame mainFrame;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private Map<String, JButton> navButtons = new HashMap<>();
    private String[] navItems = { "Dashboard", "Exams", "Questions", "Students", "Logs", "Results", "Logout" };
    private String[] navIcons = {
        "dashboard.png", "exam.png", "question.png", "people.png", "log.png", "result.png", "logout.png"
    };
    private Color sidebarColor = new Color(30, 34, 45);
    private Color hoverColor = new Color(40, 44, 60);
    private Color activeColor = new Color(0, 120, 215);
    private Color pressedColor = new Color(0, 90, 180);
    private BufferedImage backgroundImage;
    private int totalStudents = 0;
    private int activeExams = 0;
    private int totalQuestions = 0;
    private int recentResults = 0;
    private int systemLogs = 0;
    private int activeSessions = 0;

    public AdminDashboardPanel(User user, MainFrame frame) {
        this.adminUser = user;
        this.mainFrame = frame;
        loadStatsFromDatabase();
        loadBackgroundImage();
        initUI();
    }

    private void loadBackgroundImage() {
        try {
            // Try loading as resource (classpath)
            java.net.URL imgUrl = getClass().getResource("/Images/bg-7.jpg");
            if (imgUrl != null) {
                backgroundImage = ImageIO.read(imgUrl);
            } else {
                // Fallback: try loading from file system (for dev/testing)
                java.io.File imgFile = new java.io.File("bin/Images/bg-7.jpg");
                if (imgFile.exists()) {
                    backgroundImage = ImageIO.read(imgFile);
                } else {
                    System.out.println("Background image not found in resources or file system: " + imgFile.getAbsolutePath());
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Error loading background image: " + e.getMessage());
        }
    }

    private void loadStatsFromDatabase() {
        try {
            Connection conn = db.DBConnection.getConnection();
            // Total Students (count users with role 'student')
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE role = 'student'")) {
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) totalStudents = rs.getInt(1);
                }
            }
            // Active Exams
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM exams WHERE start_time <= NOW() AND end_time >= NOW()")) {
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) activeExams = rs.getInt(1);
                }
            }
            // Total Questions
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM questions")) {
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) totalQuestions = rs.getInt(1);
                }
            }
            // Recent Results (last 7 days)
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM results WHERE taken_at >= NOW() - INTERVAL 7 DAY")) {
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) recentResults = rs.getInt(1);
                }
            }
            // System Logs (last 7 days)
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM activity_log WHERE event_time >= NOW() - INTERVAL 7 DAY")) {
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) systemLogs = rs.getInt(1);
                }
            }
            // Active Sessions (students with a session in the last 10 minutes)
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(DISTINCT student_id) FROM activity_log WHERE event_time >= NOW() - INTERVAL 10 MINUTE")) {
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) activeSessions = rs.getInt(1);
                }
            }
            conn.close();
        } catch (SQLException | IOException e) {
            // Handle both SQL and IO exceptions
            e.printStackTrace();
        }
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(ThemeManager.getPrimaryBackground());

        createSidebar();
        createCardPanel();

        setActiveNav("Dashboard");
    }

    private void createSidebar() {
        // Sidebar with icons and modern design
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(sidebarColor);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(new EmptyBorder(30, 10, 30, 10));

        // Add header with user info
        addUserHeader(sidebar);

        // Add navigation buttons
        for (int i = 0; i < navItems.length; i++) {
            String item = navItems[i];
            JButton btn = createNavButton(item, navIcons[i]);
            navButtons.put(item, btn);
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(8)); // Reduced spacing
        }

        // Add flexible space at the bottom
        sidebar.add(Box.createVerticalGlue());
        add(sidebar, BorderLayout.WEST);
    }

    private void addUserHeader(JPanel sidebar) {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(sidebarColor);
        headerPanel.setBorder(new EmptyBorder(0, 0, 30, 0));
        headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // User avatar (placeholder)
        JLabel avatar = new JLabel();
        avatar.setIcon(new ImageIcon(createRoundedIcon(60, 60, activeColor)));
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        avatar.setBorder(new EmptyBorder(0, 0, 10, 0));

        // User name
        JLabel nameLabel = new JLabel(adminUser.getFullName());
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // User role
        JLabel roleLabel = new JLabel("Administrator");
        roleLabel.setForeground(new Color(180, 180, 180));
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(avatar);
        headerPanel.add(nameLabel);
        headerPanel.add(roleLabel);
        sidebar.add(headerPanel);
    }

    private Image createRoundedIcon(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.fillOval(0, 0, width, height);
        g2.dispose();
        return image;
    }

    private JButton createNavButton(String item, String iconPath) {
        // Load and scale icon for button
        java.net.URL iconUrl = getClass().getResource("/Icons/" + iconPath);
        ImageIcon icon = null;
        if (iconUrl != null) {
            Image img = new ImageIcon(iconUrl).getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
            icon = new ImageIcon(img);
        } else {
            icon = createPlaceholderIcon(24, 24);
        }
        JButton btn = new JButton(item, icon);
        styleNavButton(btn);
        btn.addActionListener(e -> handleNav(item));
        addButtonHoverEffects(btn);

        return btn;
    }

    private ImageIcon createPlaceholderIcon(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        return new ImageIcon(img);
    }

    private void styleNavButton(JButton btn) {
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(200, 45));
        btn.setMinimumSize(new Dimension(200, 45));
        btn.setPreferredSize(new Dimension(200, 45));
        btn.setBackground(sidebarColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Slightly smaller font
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setIconTextGap(15);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 0, 0, 0), 2),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)));
    }

    private void addButtonHoverEffects(JButton btn) {
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!btn.getBackground().equals(activeColor)) {
                    btn.setBackground(hoverColor);
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!btn.getBackground().equals(activeColor)) {
                    btn.setBackground(sidebarColor);
                }
            }

            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (!btn.getBackground().equals(activeColor)) {
                    btn.setBackground(pressedColor);
                }
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (!btn.getBackground().equals(activeColor)) {
                    btn.setBackground(hoverColor);
                }
            }
        });
    }

    private void createCardPanel() {
        // Card panel for modules
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false); // Make card panel transparent so background shows
        cardPanel.setBackground(new Color(0,0,0,0));
        cardPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Add cards for each module
        cardPanel.add(createDashboardPanel(), "Dashboard");
        cardPanel.add(new ExamManagementPanel(), "Exams");
        cardPanel.add(new QuestionManagementPanel(), "Questions");
        cardPanel.add(new StudentManagementPanel(), "Students");
        cardPanel.add(new LogManagementPanel(), "Logs");
        cardPanel.add(new ResultManagementPanel(), "Results");

        // Use BackgroundPanel to show background image in main content area
        JPanel backgroundPanel = new BackgroundPanel();
        backgroundPanel.setLayout(new BorderLayout());
        backgroundPanel.add(cardPanel, BorderLayout.CENTER);
        // Only add to EAST to avoid covering sidebar
        add(backgroundPanel, BorderLayout.CENTER);
    }

    // Custom panel for painting background image in main content area (excluding sidebar)
    private class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                // Draw image to fill this panel only (not the whole AdminDashboardPanel)
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false); // Make transparent so background image shows
        // panel.setBackground(ThemeManager.getPrimaryBackground()); // Remove or comment out
        
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false); // Transparent
        // headerPanel.setBackground(ThemeManager.getPrimaryBackground()); // Remove or comment out
        headerPanel.setBorder(new EmptyBorder(0, 0, 30, 0));
        
        JLabel welcome = new JLabel("Welcome, " + adminUser.getFullName(), JLabel.LEFT);
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcome.setForeground(Color.BLACK); // Set to black
        
        JLabel role = new JLabel("Administrator Dashboard", JLabel.LEFT);
        role.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        role.setForeground(Color.BLACK); // Set to black
        
        headerPanel.add(welcome, BorderLayout.NORTH);
        headerPanel.add(role, BorderLayout.SOUTH);
        
        // Stats panel
        JPanel statsPanel = createStatsPanel();
        statsPanel.setOpaque(false); // Transparent
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(statsPanel, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 20, 20));
        panel.setOpaque(false); // Transparent
        panel.setBorder(new EmptyBorder(20, 0, 0, 0));
        panel.add(createStatCard("Total Students", String.format("%,d", totalStudents), new Color(70, 130, 180)));
        panel.add(createStatCard("Active Exams", String.format("%d", activeExams), new Color(60, 179, 113)));
        panel.add(createStatCard("Questions", String.format("%,d", totalQuestions), new Color(255, 165, 0)));
        panel.add(createStatCard("Recent Results", String.format("%d", recentResults), new Color(147, 112, 219)));
        panel.add(createStatCard("System Logs", String.format("%,d", systemLogs), new Color(220, 20, 60)));
        panel.add(createStatCard("Active Sessions", String.format("%d", activeSessions), new Color(46, 204, 113)));
        return panel;
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false); // Transparent
        // card.setBackground(Color.WHITE); // Remove or comment out
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(Color.BLACK); // Set to black
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        valueLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        card.add(titleLabel);
        card.add(valueLabel);
        card.add(Box.createVerticalGlue());
        
        return card;
    }

    private void handleNav(String item) {
        if (item.equals("Logout")) {
            mainFrame.logout();
        } else {
            cardLayout.show(cardPanel, item);
            setActiveNav(item);
        }
    }

    private void setActiveNav(String active) {
        for (String item : navItems) {
            JButton btn = navButtons.get(item);
            if (btn == null) continue;
            
            if (item.equals(active)) {
                btn.setBackground(activeColor);
                btn.setFont(btn.getFont().deriveFont(Font.BOLD));
            } else {
                btn.setBackground(sidebarColor);
                btn.setFont(btn.getFont().deriveFont(Font.PLAIN));
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Only paint background for the whole panel if you want, or leave empty to avoid painting over sidebar
        super.paintComponent(g);
    }
}