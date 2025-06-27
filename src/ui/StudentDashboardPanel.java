package ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import model.Exam;
import model.ExamTransfer;
import model.User;

public class StudentDashboardPanel extends JPanel {
    private JPanel content;
    private User studentUser;
    private MainFrame mainFrame;
    private BufferedImage backgroundImage;
    private Color sidebarColor = new Color(0x2C3E50);
    private Color primaryColor = new Color(0x3498DB);
    private Color hoverColor = new Color(0x2980B9);
    private Color activeColor = new Color(0x1ABC9C);

    // Add a Theme inner class for consistent UI/UX
    public static class Theme {
        public static final Color PRIMARY = new Color(0x3498DB);
        public static final Color SIDEBAR_BG = new Color(0x2C3E50);
        public static final Color ACCENT = new Color(0x1ABC9C);
        public static final Color TEXT = Color.WHITE;
        public static final Color CARD1 = new Color(0x3498DB);
        public static final Color CARD2 = new Color(0x2ECC71);
        public static final Color CARD3 = new Color(0xE74C3C);
        public static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 28);
        public static final Font SUBHEADER_FONT = new Font("Segoe UI", Font.PLAIN, 18);
        public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);
        public static final Font CARD_FONT = new Font("Segoe UI", Font.PLAIN, 14);
        public static final Font CARD_TITLE_FONT = new Font("Segoe UI", Font.BOLD, 16);
    }

    // Static background image for all content panels
    private static BufferedImage globalBackgroundImage = null;
    public static void setGlobalBackgroundImage(BufferedImage img) {
        globalBackgroundImage = img;
    }
    public static BufferedImage getGlobalBackgroundImage() {
        return globalBackgroundImage;
    }

    public StudentDashboardPanel(User user, MainFrame frame) {
        this.studentUser = user;
        this.mainFrame = frame;
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(Theme.SIDEBAR_BG);
        removeAll();
        // Load global background image if not set
        if (getGlobalBackgroundImage() == null) {
            try {
                BufferedImage img = ImageIO.read(getClass().getResource("/Images/stdd.jpg"));
                setGlobalBackgroundImage(img);
            } catch (IOException | IllegalArgumentException e) {
                System.out.println("Error loading background image: " + e.getMessage());
            }
        }

        // Create sidebar with improved design
        JPanel sidebar = createSidebar();

        // Create main content area with global background
        content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                BufferedImage bg = getGlobalBackgroundImage();
                if (bg != null) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
                    g2d.setColor(new Color(0, 0, 0, 30));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    g2d.dispose();
                } else {
                    g.setColor(Theme.SIDEBAR_BG);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
                super.paintComponent(g);
            }
        };
        content.setBackground(new Color(0,0,0,0));
        content.setLayout(new BorderLayout());

        // Create welcome panel with better styling
        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setOpaque(false);
        welcomePanel.setBorder(new EmptyBorder(30, 30, 20, 30));

        JLabel welcome = new JLabel("Welcome, " + user.getFullName(), JLabel.LEFT);
        welcome.setFont(Theme.HEADER_FONT);
        welcome.setForeground(Theme.TEXT);

        JLabel role = new JLabel("Student Dashboard", JLabel.LEFT);
        role.setFont(Theme.SUBHEADER_FONT);
        role.setForeground(new Color(220, 220, 220));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(welcome);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(role);

        welcomePanel.add(textPanel, BorderLayout.NORTH);

        // Add quick stats or dashboard cards
        JPanel cardsPanel = createDashboardCards();
        welcomePanel.add(cardsPanel, BorderLayout.CENTER);

        content.add(welcomePanel, BorderLayout.NORTH);

        // Add sidebar first to ensure correct z-order and layout
        add(sidebar, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Theme.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setBorder(new EmptyBorder(20, 0, 20, 0));
        add(sidebar, BorderLayout.WEST);

        // Profile section with improved design
        JPanel profilePanel = new JPanel();
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
        profilePanel.setOpaque(false);
        profilePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        profilePanel.setBorder(new EmptyBorder(0, 20, 30, 20));

        // Circular profile picture
        JPanel profilePicContainer = new JPanel(new BorderLayout());
        profilePicContainer.setOpaque(false);
        profilePicContainer.setMaximumSize(new Dimension(80, 80));
        profilePicContainer.setAlignmentX(Component.CENTER_ALIGNMENT);
        profilePicContainer.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel profileIconLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(primaryColor);
                g2.fillOval(0, 0, getWidth(), getHeight());
                super.paintComponent(g2);
            }
        };
        profileIconLabel.setPreferredSize(new Dimension(80, 80));
        profileIconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        profileIconLabel.setVerticalAlignment(SwingConstants.CENTER);
        // Use the custom profile icon
        java.net.URL iconUrl = getClass().getResource("/Icons/user.png");
        if (iconUrl != null) {
            ImageIcon icon = new ImageIcon(iconUrl);
            Image img = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            profileIconLabel.setIcon(new ImageIcon(img));
        } else {
            profileIconLabel.setText("\uD83D\uDC64");
            profileIconLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 30));
            profileIconLabel.setForeground(Color.WHITE);
        }
        // Add click listener to show student info panel instead of dialog
        profileIconLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        profileIconLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                StudentDashboardPanel.this.showStudentProfilePanel();
            }
        });
        profilePicContainer.add(profileIconLabel);
        profilePanel.add(profilePicContainer);

        JLabel nameLabel = new JLabel(studentUser.getUsername());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        profilePanel.add(nameLabel);

        JLabel emailLabel = new JLabel(studentUser.getEmail());
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        emailLabel.setForeground(new Color(180, 180, 180));
        emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        profilePanel.add(emailLabel);

        sidebar.add(profilePanel);

        // Navigation menu with improved design
        String[][] navItems = new String[][] {
                { "\uD83D\uDCD6", "Exams", "View and take available exams" },
                { "\uD83D\uDCCA", "Results", "Check your exam results" },
                { "\uD83D\uDEAA", "Logout", "Sign out of your account" },
                { "\uD83D\uDDC2", "Import Exam", "Import exam from USB" }
        };

        for (String[] nav : navItems) {
            JPanel menuItem = new JPanel();
            menuItem.setLayout(new BorderLayout());
            menuItem.setBackground(new Color(0, 0, 0, 0));
            menuItem.setMaximumSize(new Dimension(250, 60));
            menuItem.setBorder(new EmptyBorder(5, 20, 5, 10));
            menuItem.setCursor(new Cursor(Cursor.HAND_CURSOR));

            JPanel contentPanel = new JPanel(new BorderLayout());
            contentPanel.setOpaque(false);
            contentPanel.setBorder(new EmptyBorder(8, 10, 8, 10));

            JLabel iconLabel = new JLabel(nav[0]);
            iconLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 20));
            iconLabel.setForeground(Color.WHITE);
            iconLabel.setPreferredSize(new Dimension(30, 30));

            JLabel textLabel = new JLabel(nav[1]);
            textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            textLabel.setForeground(Color.WHITE);

            JLabel descLabel = new JLabel(nav[2]);
            descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            descLabel.setForeground(new Color(180, 180, 180));

            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setOpaque(false);
            textPanel.add(textLabel);
            textPanel.add(descLabel);

            contentPanel.add(iconLabel, BorderLayout.WEST);
            contentPanel.add(textPanel, BorderLayout.CENTER);

            // Add active indicator (initially invisible)
            JPanel activeIndicator = new JPanel();
            activeIndicator.setOpaque(false);
            activeIndicator.setPreferredSize(new Dimension(5, 0));
            activeIndicator.setBackground(activeColor);

            menuItem.add(contentPanel, BorderLayout.CENTER);
            menuItem.add(activeIndicator, BorderLayout.WEST);

            // Remove hover effects for menuItem
            menuItem.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    // Reset all menu items
                    for (Component comp : sidebar.getComponents()) {
                        if (comp instanceof JPanel) {
                            for (Component innerComp : ((JPanel) comp).getComponents()) {
                                if (innerComp instanceof JPanel && ((JPanel) innerComp).getComponentCount() > 0) {
                                    Component maybeIndicator = ((JPanel) innerComp).getComponent(0);
                                    if (maybeIndicator instanceof JPanel) {
                                        maybeIndicator.setBackground(sidebarColor);
                                    }
                                }
                            }
                        }
                    }
                    // Set current as active
                    activeIndicator.setBackground(activeColor);
                    handleNav(nav[1]);
                }
            });

            sidebar.add(menuItem);
            sidebar.add(Box.createVerticalStrut(5));
        }

        // Add some space at the bottom
        sidebar.add(Box.createVerticalGlue());

        // Add 'Fetch Exam from LAN' button at the bottom
        JButton fetchExamButton = new JButton("Fetch Exam from LAN");
        fetchExamButton.setFont(Theme.BUTTON_FONT);
        fetchExamButton.setBackground(primaryColor);
        fetchExamButton.setForeground(Color.WHITE);
        fetchExamButton.setFocusPainted(false);
        fetchExamButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        fetchExamButton.setMaximumSize(new Dimension(200, 40));
        fetchExamButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        fetchExamButton.addActionListener(e -> {
            JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
            JTextField ipField = new JTextField();
            JTextField portField = new JTextField("5000");
            panel.add(new JLabel("Server IP Address:"));
            panel.add(ipField);
            panel.add(new JLabel("Port:"));
            panel.add(portField);
            int result = JOptionPane.showConfirmDialog(this, panel, "Connect to Exam Server", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String ip = ipField.getText().trim();
                int port = 5000;
                try { port = Integer.parseInt(portField.getText().trim()); } catch (Exception ex) {}
                if (!ip.isEmpty()) {
                    fetchExamFromServer(ip, port);
                } else {
                    JOptionPane.showMessageDialog(this, "Please enter a valid IP address.");
                }
            }
        });
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(fetchExamButton);

        // Add version or copyright info
        JLabel versionLabel = new JLabel("v1.0.0 © 2017", JLabel.CENTER);
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        versionLabel.setForeground(new Color(150, 150, 150));
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        versionLabel.setBorder(new EmptyBorder(20, 0, 0, 0));
        sidebar.add(versionLabel);

        return sidebar;
    }

    private JPanel createDashboardCards() {
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setBorder(new EmptyBorder(20, 30, 30, 30));

        // Card 1: Upcoming Exams
        JPanel card1 = createCard("\uD83D\uDCC5", "Upcoming Exams", "3 exams scheduled", primaryColor);

        // Card 2: Recent Results
        JPanel card2 = createCard("\uD83D\uDCC8", "Recent Results", "Average score: 85%", new Color(0x2ECC71));

        // Card 3: Notifications
        JPanel card3 = createCard("\uD83D\uDD14", "Notifications", "2 new messages", new Color(0xE74C3C));

        cardsPanel.add(card1);
        cardsPanel.add(card2);
        cardsPanel.add(card3);

        return cardsPanel;
    }

    private JPanel createCard(String icon, String title, String value, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 180));
        card.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 1, 1, 1, new Color(255, 255, 255, 30)),
                new EmptyBorder(20, 20, 20, 20)));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add hover effect
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 220));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 180));
            }
        });

        JLabel iconLabel = new JLabel(icon, JLabel.CENTER);
        iconLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 30));
        iconLabel.setForeground(Color.WHITE);
        iconLabel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);

        JLabel valueLabel = new JLabel(value, JLabel.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        valueLabel.setForeground(new Color(240, 240, 240));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.add(iconLabel);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(valueLabel);

        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }

    private void handleNav(String item) {
        if (item.equals("Logout")) {
            mainFrame.logout();
        } else if (item.equals("Exams")) {
            showExamList();
        } else if (item.equals("Results")) {
            showResults();
        } else if (item.equals("Import Exam")) {
            showImportExamPanel();
        } else {
            JOptionPane.showMessageDialog(this, item + " section coming soon.");
        }
    }

    public void showExamList() {
        content.removeAll();
        content.add(new ExamListPanel(studentUser, this), BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
        this.revalidate();
        this.repaint();
    }

    public void startExam(Exam exam) {
        content.removeAll();
        content.add(new ExamTakingPanel(studentUser, exam, this), BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
        this.revalidate();
        this.repaint();
    }

    public void showResults() {
        content.removeAll();
        content.add(new StudentResultPanel(studentUser), BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
        this.revalidate();
        this.repaint();
    }

    public void showImportExamPanel() {
        content.removeAll();
        content.add(new ExamImportPanel(), BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
        this.revalidate();
        this.repaint();
    }

    // Call this method to fetch the exam from the LAN server
    public void fetchExamFromServer(String serverIp, int port) {
        new Thread(() -> {
            try {
                Socket socket = new Socket(serverIp, port);
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                ExamTransfer transfer = (ExamTransfer) in.readObject();
                socket.close();

                SwingUtilities.invokeLater(() -> {
                    ExamTakingPanel examPanel = new ExamTakingPanel(studentUser, transfer.exam, this);
                    examPanel.loadQuestionsFromTransfer(transfer.questions);
                    content.removeAll();
                    content.add(examPanel, BorderLayout.CENTER);
                    content.revalidate();
                    content.repaint();
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Failed to fetch exam: " + e.getMessage());
                });
            }
        }).start();
    }

    public void showStudentProfilePanel() {
        JPanel profilePanel = new JPanel();
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
        profilePanel.setBackground(Color.WHITE);
        profilePanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel title = new JLabel("Student Profile");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(new Color(44, 62, 80));
        profilePanel.add(title);
        profilePanel.add(Box.createVerticalStrut(20));

        JLabel name = new JLabel("Name: " + studentUser.getFullName());
        name.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        profilePanel.add(name);
        profilePanel.add(Box.createVerticalStrut(10));

        JLabel username = new JLabel("Username: " + studentUser.getUsername());
        username.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        username.setAlignmentX(Component.CENTER_ALIGNMENT);
        profilePanel.add(username);
        profilePanel.add(Box.createVerticalStrut(10));

        JLabel email = new JLabel("Email: " + studentUser.getEmail());
        email.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        email.setAlignmentX(Component.CENTER_ALIGNMENT);
        profilePanel.add(email);
        profilePanel.add(Box.createVerticalStrut(20));

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeBtn.setBackground(new Color(52, 152, 219));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeBtn.addActionListener(e -> {
            // Restore dashboard
            content.removeAll();
            JPanel welcomePanel = new JPanel(new BorderLayout());
            welcomePanel.setOpaque(false);
            welcomePanel.setBorder(new EmptyBorder(30, 30, 20, 30));
            JLabel welcome = new JLabel("Welcome, " + studentUser.getFullName(), JLabel.LEFT);
            welcome.setFont(Theme.HEADER_FONT);
            welcome.setForeground(Theme.TEXT);
            JLabel role = new JLabel("Student Dashboard", JLabel.LEFT);
            role.setFont(Theme.SUBHEADER_FONT);
            role.setForeground(new Color(220, 220, 220));
            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setOpaque(false);
            textPanel.add(welcome);
            textPanel.add(Box.createVerticalStrut(5));
            textPanel.add(role);
            welcomePanel.add(textPanel, BorderLayout.NORTH);
            JPanel cardsPanel = createDashboardCards();
            welcomePanel.add(cardsPanel, BorderLayout.CENTER);
            content.add(welcomePanel, BorderLayout.NORTH);
            content.revalidate();
            content.repaint();
        });
        profilePanel.add(closeBtn);

        content.removeAll();
        content.add(profilePanel, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }
}