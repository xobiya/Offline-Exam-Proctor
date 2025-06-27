package ui;

import db.DBConnection;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import model.User;
import util.PasswordUtils;

public class StudentManagementPanel extends JPanel {
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private JButton addButton, editButton, deleteButton, resetPasswordButton, refreshButton;
    private JTextField searchField;
    private JLabel searchIcon;
    private ModernTableCellRenderer cellRenderer;
    private Image backgroundImg;

    public StudentManagementPanel() {
        // Load background image from /Images/stdd.jpg
        java.net.URL bgUrl = getClass().getResource("/Images/stdd.jpg");
        if (bgUrl != null) {
            backgroundImg = new ImageIcon(bgUrl).getImage();
        }
        setLayout(new BorderLayout());
        setOpaque(false); // Make panel transparent so paintComponent shows image
        setBackground(new Color(245, 247, 250)); // Soft background

        // --- Header with Search Bar ---
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(new Color(245, 247, 250));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

        JLabel titleLabel = new JLabel("Student Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(33, 150, 243));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Search bar panel
        JPanel searchPanel = new JPanel(null);
        searchPanel.setPreferredSize(new Dimension(300, 35));
        searchPanel.setBackground(new Color(245, 247, 250));
        searchField = new JTextField();
        searchField.setBounds(0, 0, 300, 35);
        searchField.setBackground(new Color(245, 245, 250));
        searchField.setForeground(Color.DARK_GRAY);
        searchField.setCaretColor(Color.DARK_GRAY);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 200), 1, true),
            BorderFactory.createEmptyBorder(5, 10, 5, 35)
        ));
        searchField.putClientProperty("JTextField.placeholderText", "Search students...");
        searchIcon = new JLabel();
        java.net.URL iconUrl = getClass().getResource("/Icons/Search-1.png");
        if (iconUrl != null) {
            ImageIcon icon = new ImageIcon(iconUrl);
            Image img = icon.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH);
            searchIcon.setIcon(new ImageIcon(img));
        } else {
            searchIcon.setIcon(UIManager.getIcon("FileView.fileIcon"));
        }
        searchIcon.setBounds(265, 8, 20, 20);
        searchIcon.setOpaque(true);
        searchIcon.setBackground(Color.WHITE);
        searchIcon.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        searchIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchIcon.setToolTipText("Search");
        searchPanel.add(searchField);
        searchPanel.add(searchIcon);
        headerPanel.add(searchPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Table setup
        tableModel = new DefaultTableModel(new String[] { "ID", "Username", "Full Name", "Email" }, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        studentTable = new JTable(tableModel);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentTable.setRowHeight(32);
        studentTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        studentTable.setShowGrid(false);
        studentTable.setIntercellSpacing(new Dimension(0, 0));
        studentTable.setBackground(new Color(250, 250, 255));
        studentTable.setForeground(Color.DARK_GRAY);
        studentTable.setSelectionBackground(new Color(232, 240, 254));
        studentTable.setSelectionForeground(Color.BLACK);
        JTableHeader tableHeader = studentTable.getTableHeader();
        tableHeader.setDefaultRenderer(new ModernTableHeaderRenderer());
        tableHeader.setPreferredSize(new Dimension(100, 40));
        // Custom cell renderer with hover
        cellRenderer = new ModernTableCellRenderer();
        for (int i = 0; i < studentTable.getColumnCount(); i++) {
            studentTable.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }
        studentTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int row = studentTable.rowAtPoint(e.getPoint());
                cellRenderer.setHoveredRow(row);
                studentTable.repaint();
            }
        });
        studentTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent e) {
                cellRenderer.setHoveredRow(-1);
                studentTable.repaint();
            }
        });
        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(180, 180, 200)));
        refreshButton = new RoundedButton("Refresh", new Color(33, 150, 243), Color.WHITE);
        addButton = new RoundedButton("Add Student", new Color(76, 175, 80), Color.WHITE);
        editButton = new RoundedButton("Edit Student", new Color(255, 193, 7), Color.WHITE);
        deleteButton = new RoundedButton("Delete Student", new Color(244, 67, 54), Color.WHITE);
        resetPasswordButton = new RoundedButton("Reset Password", new Color(3, 169, 244), Color.WHITE);
        buttonPanel.add(refreshButton);
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(resetPasswordButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Button actions
        addButton.addActionListener(e -> showStudentForm(null));
        editButton.addActionListener(e -> editSelectedStudent());
        deleteButton.addActionListener(e -> deleteSelectedStudent());
        resetPasswordButton.addActionListener(e -> resetSelectedStudentPassword());
        refreshButton.addActionListener(e -> loadStudents());

        // --- Search functionality ---
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                filterTable(searchField.getText());
            }
        });
        searchIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                filterTable(searchField.getText());
            }
        });
        loadStudents();
    }

    // --- RoundedButton class for modern rounded buttons ---
    class RoundedButton extends JButton {
        private final Color baseColor;
        public RoundedButton(String text, Color bgColor, Color fgColor) {
            super(text);
            this.baseColor = bgColor;
            setFocusPainted(false);
            setBackground(bgColor);
            setForeground(fgColor);
            setFont(new Font("Segoe UI", Font.PLAIN, 15));
            setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isEnabled() ? getBackground() : Color.LIGHT_GRAY);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
            super.paintComponent(g2);
            g2.dispose();
        }
        @Override
        public void setBackground(Color bg) {
            super.setBackground(bg);
            repaint();
        }
    }

    // --- Custom table header renderer ---
    class ModernTableHeaderRenderer extends DefaultTableCellRenderer {
        public ModernTableHeaderRenderer() {
            setOpaque(true);
            setBackground(new Color(33, 150, 243));
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 15));
            setHorizontalAlignment(SwingConstants.CENTER);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            return this;
        }
    }

    // --- Custom table cell renderer for hover and alternate row colors ---
    class ModernTableCellRenderer extends DefaultTableCellRenderer {
        private int hoveredRow = -1;
        public void setHoveredRow(int row) { this.hoveredRow = row; }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            if (isSelected) {
                setBackground(new Color(232, 240, 254));
            } else if (row == hoveredRow) {
                setBackground(new Color(220, 230, 245));
            } else if (row % 2 == 0) {
                setBackground(new Color(245, 247, 250));
            } else {
                setBackground(Color.WHITE);
            }
            setForeground(Color.DARK_GRAY);
            return this;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (backgroundImg != null) {
            // Draw the image scaled to fill the panel
            g.drawImage(backgroundImg, 0, 0, getWidth(), getHeight(), this);
        }
        super.paintComponent(g);
    }

    private void loadStudents() {
        tableModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT id, username, full_name, email FROM users WHERE role='student'";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("username"));
                row.add(rs.getString("full_name"));
                row.add(rs.getString("email"));
                tableModel.addRow(row);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading students: " + ex.getMessage());
        }
    }

    private void showStudentForm(User student) {
        JTextField usernameField = new JTextField();
        JTextField fullNameField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        if (student != null) {
            usernameField.setText(student.getUsername());
            usernameField.setEditable(false);
            fullNameField.setText(student.getFullName());
            emailField.setText(student.getEmail());
        }
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Full Name:"));
        panel.add(fullNameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        if (student == null) {
            panel.add(new JLabel("Password:"));
            panel.add(passwordField);
        }
        int result = JOptionPane.showConfirmDialog(this, panel, student == null ? "Add Student" : "Edit Student",
                JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            if (student == null) {
                addStudent(usernameField.getText(), fullNameField.getText(), emailField.getText(),
                        new String(passwordField.getPassword()));
            } else {
                updateStudent(student.getId(), fullNameField.getText(), emailField.getText());
            }
        }
    }

    private void addStudent(String username, String fullName, String email, String password) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO users (username, password_hash, role, full_name, email) VALUES (?, ?, 'student', ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, PasswordUtils.hashPassword(password));
            ps.setString(3, fullName);
            ps.setString(4, email);
            ps.executeUpdate();
            loadStudents();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error adding student: " + ex.getMessage());
        }
    }

    private void updateStudent(int id, String fullName, String email) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE users SET full_name=?, email=? WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setInt(3, id);
            ps.executeUpdate();
            loadStudents();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating student: " + ex.getMessage());
        }
    }

    private void editSelectedStudent() {
        int row = studentTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a student to edit.");
            return;
        }
        User student = new User();
        student.setId((int) tableModel.getValueAt(row, 0));
        student.setUsername((String) tableModel.getValueAt(row, 1));
        student.setFullName((String) tableModel.getValueAt(row, 2));
        student.setEmail((String) tableModel.getValueAt(row, 3));
        showStudentForm(student);
    }

    private void deleteSelectedStudent() {
        int row = studentTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a student to delete.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this student?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "DELETE FROM users WHERE id=? AND role='student'";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, id);
                ps.executeUpdate();
                loadStudents();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting student: " + ex.getMessage());
            }
        }
    }

    private void resetSelectedStudentPassword() {
        int row = studentTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a student to reset password.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        JPasswordField passwordField = new JPasswordField();
        int result = JOptionPane.showConfirmDialog(this, passwordField, "Enter new password",
                JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String newPassword = new String(passwordField.getPassword());
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "UPDATE users SET password_hash=? WHERE id=? AND role='student'";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, PasswordUtils.hashPassword(newPassword));
                ps.setInt(2, id);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Password reset successful.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error resetting password: " + ex.getMessage());
            }
        }
    }

    private void filterTable(String query) {
        javax.swing.table.TableRowSorter<DefaultTableModel> sorter = new javax.swing.table.TableRowSorter<>(tableModel);
        studentTable.setRowSorter(sorter);
        if (query.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + query));
        }
    }
}
