package ui;

import java.awt.*;
import javax.swing.*;
import model.User;
import util.ThemeManager;

public class MainFrame extends JFrame implements LoginPanel.LoginListener {
    private JPanel mainPanel;
    private User currentUser;

    public MainFrame() {
        setTitle("Offline Exam Proctor System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        setAppIcon();

        getContentPane().setBackground(ThemeManager.getPrimaryBackground());

        mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);

        showLoginPanel();
    }

    /** Attempts to load the application icon (PNG format). */
    private void setAppIcon() {
        java.net.URL icoIcon = getClass().getResource("/Icons/appIcons.png");
        try {
            if (icoIcon != null) {
                setIconImage(new ImageIcon(icoIcon).getImage());
            }
        } catch (Exception e) {
            System.out.println("Error loading PNG icon: " + e.getMessage());
        }
    }

    private void showLoginPanel() {
        mainPanel.removeAll();
        LoginPanel loginPanel = new LoginPanel(this);
        mainPanel.add(loginPanel, BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    @Override
    public void onLoginSuccess(User user) {
        this.currentUser = user;
        if ("admin".equalsIgnoreCase(user.getRole())) {
            showAdminDashboard(user);
        } else {
            showStudentDashboard(user);
        }
    }

    public void showAdminDashboard(User user) {
        mainPanel.removeAll();
        AdminDashboardPanel adminPanel = new AdminDashboardPanel(user, this);
        mainPanel.add(adminPanel, BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void showStudentDashboard(User user) {
        mainPanel.removeAll();
        StudentDashboardPanel studentPanel = new StudentDashboardPanel(user, this);
        mainPanel.add(studentPanel, BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void logout() {
        this.currentUser = null;
        showLoginPanel();
    }
}
