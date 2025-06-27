package ui;

import db.DBConnection;
import model.User;
import util.ThemeManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

public class StudentResultPanel extends JPanel {
    private User studentUser;
    private DefaultTableModel tableModel;
    private JTable resultTable;
    private JPanel resultsPanel;

    public StudentResultPanel(User user) {
        this.studentUser = user;
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(new Color(0x2C3E50)); // Ensure main panel uses dark gray

        // Create header panel with gradient background
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(41, 128, 185),
                        getWidth(), 0, new Color(39, 174, 96));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setPreferredSize(new Dimension(getWidth(), 100));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Title label with icon
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        titlePanel.setOpaque(false);

        JLabel iconLabel = new JLabel();
        // Fixed: Use classpath-relative path for the icon resource
        iconLabel.setIcon(new ImageIcon(getClass().getResource("/Icons/result.png")));

        JLabel titleLabel = new JLabel("My Exam Results");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);

        titlePanel.add(iconLabel);
        titlePanel.add(titleLabel);
        headerPanel.add(titlePanel, BorderLayout.WEST);

        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setOpaque(false);

        JButton refreshButton = createActionButton("Refresh", new Color(255, 255, 255, 120));
        JButton exportButton = createActionButton("Export CSV", new Color(255, 255, 255, 120));

        buttonPanel.add(refreshButton);
        buttonPanel.add(exportButton);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Main content area
        resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        resultsPanel.setBackground(Color.WHITE); // Ensure always visible

        JScrollPane scrollPane = new JScrollPane(resultsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Hidden table for export
        tableModel = new DefaultTableModel(new String[] { "Exam", "Score", "Percentage", "Date" }, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        resultTable = new JTable(tableModel);
        resultTable.setVisible(false);

        // Button actions
        refreshButton.addActionListener(e -> loadResultsAsync());
        exportButton.addActionListener(e -> exportToCSVAsync());

        loadResultsAsync();
    }

    private JButton createActionButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), 180));
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), 150));
                } else {
                    g2d.setColor(bgColor);
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2d.dispose();

                super.paintComponent(g);
            }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    private void loadResultsAsync() {
        // Use SwingWorker to keep UI responsive
        new SwingWorker<java.util.List<Vector<Object>>, Void>() {
            protected java.util.List<Vector<Object>> doInBackground() {
                java.util.List<Vector<Object>> allResults = new java.util.ArrayList<>();
                try (Connection conn = DBConnection.getConnection()) {
                    String sql = "SELECT e.title, r.score, r.taken_at, (SELECT COUNT(*) FROM questions WHERE exam_id=e.id) as total "
                            +
                            "FROM results r LEFT JOIN exams e ON r.exam_id=e.id " +
                            "WHERE r.student_id=? ORDER BY r.taken_at DESC";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setInt(1, studentUser.getId());
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        Vector<Object> row = new Vector<>();
                        row.add(rs.getString(1));
                        row.add(rs.getInt(2));
                        row.add(rs.getString(3));
                        row.add(rs.getInt(4));
                        allResults.add(row);
                    }
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(StudentResultPanel.this,
                            "Error loading results: " + ex.getMessage()));
                }
                return allResults;
            }

            protected void done() {
                try {
                    java.util.List<Vector<Object>> allResults = get();
                    updateResultsPanel(allResults);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(StudentResultPanel.this,
                            "Error updating results: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void updateResultsPanel(java.util.List<Vector<Object>> allResults) {
        resultsPanel.removeAll();
        tableModel.setRowCount(0);
        boolean first = true;
        for (Vector<Object> row : allResults) {
            String examTitle = (String) row.get(0);
            int score = (int) row.get(1);
            String takenAt = (String) row.get(2);
            int totalQuestions = (int) row.get(3);
            double percentage = totalQuestions > 0 ? (score * 100.0 / totalQuestions) : 0;
            Vector<Object> exportRow = new Vector<>();
            exportRow.add(examTitle);
            exportRow.add(score + "/" + totalQuestions);
            exportRow.add(String.format("%.1f%%", percentage));
            exportRow.add(takenAt);
            tableModel.addRow(exportRow);
            if (first) {
                addLatestResultCard(examTitle, score, totalQuestions, percentage, takenAt);
                first = false;
            } else {
                addResultCard(examTitle, score, totalQuestions, percentage, takenAt);
            }
        }
        if (allResults.isEmpty()) {
            JLabel noResultsLabel = new JLabel("No exam results found", JLabel.CENTER);
            noResultsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            noResultsLabel.setForeground(new Color(120, 120, 120));
            noResultsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            resultsPanel.add(noResultsLabel);
        }
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    private void addLatestResultCard(String examTitle, int score, int total, double percentage, String date) {
        JPanel card = new JPanel(new BorderLayout(20, 10));
        card.setBackground(new Color(245, 250, 255));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 230, 240)),
                BorderFactory.createEmptyBorder(25, 30, 25, 30)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Header with exam title
        JLabel titleLabel = new JLabel(examTitle);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(41, 128, 185));

        // Score panel
        JPanel scorePanel = new JPanel(new GridLayout(1, 3, 20, 0));
        scorePanel.setOpaque(false);

        // Score card
        scorePanel.add(createScoreCard("Score", score + "/" + total, new Color(52, 152, 219)));

        // Percentage card
        String percentageText = String.format("%.1f%%", percentage);
        Color percentageColor = percentage >= 70 ? new Color(46, 204, 113)
                : percentage >= 50 ? new Color(241, 196, 15) : new Color(231, 76, 60);
        scorePanel.add(createScoreCard("Percentage", percentageText, percentageColor));

        // Date card
        scorePanel.add(createScoreCard("Date Taken", date, new Color(155, 89, 182)));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(scorePanel, BorderLayout.CENTER);

        resultsPanel.add(card);
        resultsPanel.add(Box.createVerticalStrut(30));
    }

    private JPanel createScoreCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout(5, 10));
        card.setBackground(new Color(255, 255, 255, 200));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, color),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(120, 120, 120));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(new Color(60, 60, 60));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private void addResultCard(String examTitle, int score, int total, double percentage, String date) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Exam title
        JLabel titleLabel = new JLabel(examTitle);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(60, 60, 60));

        // Score info
        JPanel infoPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        infoPanel.setOpaque(false);

        String percentageText = String.format("%.1f%%", percentage);
        Color percentageColor = percentage >= 70 ? new Color(46, 204, 113)
                : percentage >= 50 ? new Color(241, 196, 15) : new Color(231, 76, 60);

        infoPanel.add(createInfoLabel("Score: " + score + "/" + total, new Color(52, 152, 219)));
        infoPanel.add(createInfoLabel("Percentage: " + percentageText, percentageColor));
        infoPanel.add(createInfoLabel("Date: " + date, new Color(155, 89, 182)));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(infoPanel, BorderLayout.CENTER);

        resultsPanel.add(card);
        resultsPanel.add(Box.createVerticalStrut(15));
    }

    private JLabel createInfoLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(color);
        return label;
    }

    private void exportToCSVAsync() {
        new SwingWorker<Void, Void>() {
            protected Void doInBackground() {
                try {
                    exportToCSV();
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(StudentResultPanel.this,
                            "Error exporting CSV: " + ex.getMessage(),
                            "Export Error",
                            JOptionPane.ERROR_MESSAGE));
                }
                return null;
            }
        }.execute();
    }

    private void exportToCSV() {
        // All UI code must be on EDT
        SwingUtilities.invokeLater(() -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Results as CSV");
            // Suggest a filename
            fileChooser.setSelectedFile(new java.io.File(studentUser.getUsername() + "_exam_results.csv"));
            // Set custom icon for dialog
            java.net.URL iconUrl = getClass().getResource("/Icons/appIcons.png");
            if (iconUrl != null) {
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window instanceof JFrame) {
                    ((JFrame) window).setIconImage(new ImageIcon(iconUrl).getImage());
                }
            }
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try (FileWriter writer = new FileWriter(fileChooser.getSelectedFile())) {
                    // Write header
                    writer.append("Exam,Score,Percentage,Date\n");
                    // Write data
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        writer.append(String.format("\"%s\",%s,%s,%s\n",
                                tableModel.getValueAt(i, 0),
                                tableModel.getValueAt(i, 1),
                                tableModel.getValueAt(i, 2),
                                tableModel.getValueAt(i, 3)));
                    }
                    JOptionPane.showMessageDialog(this,
                            "Results exported successfully to:\n" + fileChooser.getSelectedFile().getPath(),
                            "Export Successful",
                            JOptionPane.INFORMATION_MESSAGE,
                            iconUrl != null ? new ImageIcon(iconUrl) : null);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            "Error exporting CSV: " + ex.getMessage(),
                            "Export Error",
                            JOptionPane.ERROR_MESSAGE,
                            iconUrl != null ? new ImageIcon(iconUrl) : null);
                }
            }
        });
    }
}