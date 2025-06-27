package ui;

import db.DBConnection;
import model.User;
import model.Exam;
import util.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class ExamListPanel extends JPanel {
    private User studentUser;
    private StudentDashboardPanel parentPanel;
    private JPanel cardsPanel;
    private JScrollPane scrollPane;
    private JLabel progressLabel;
    private java.util.List<ExamData> exams = new ArrayList<>();

    public ExamListPanel(User user, StudentDashboardPanel parent) {
        this.studentUser = user;
        this.parentPanel = parent;
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(new Color(0x2C3E50)); // Ensure main panel uses dark gray

        JPanel cardPanel = new JPanel(new BorderLayout(16, 16));
        cardPanel.setBackground(new Color(255, 255, 255));
        cardPanel.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        JLabel titleLabel = new JLabel("Available Exams");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(new Color(44, 62, 80));
        cardPanel.add(titleLabel, BorderLayout.NORTH);

        cardsPanel = new JPanel();
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));
        cardsPanel.setBackground(Color.WHITE);

        scrollPane = new JScrollPane(cardsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        cardPanel.add(scrollPane, BorderLayout.CENTER);

        progressLabel = new JLabel();
        progressLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        progressLabel.setForeground(new Color(120, 120, 120));
        progressLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        cardPanel.add(progressLabel, BorderLayout.SOUTH);

        add(cardPanel, BorderLayout.CENTER);
        loadAssignedExams();
    }

    private void loadAssignedExams() {
        exams.clear();
        cardsPanel.removeAll();
        progressLabel.setText("Loading exams...");
        cardsPanel.revalidate();
        cardsPanel.repaint();

        SwingWorker<java.util.List<ExamData>, Void> worker = new SwingWorker<java.util.List<ExamData>, Void>() {
            @Override
            protected java.util.List<ExamData> doInBackground() throws Exception {
                java.util.List<ExamData> loadedExams = new ArrayList<>();
                try (Connection conn = DBConnection.getConnection()) {
                    String sql = "SELECT id, title, description, start_time, end_time, duration_minutes FROM exams";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        ExamData exam = new ExamData(
                                rs.getInt("id"),
                                rs.getString("title"),
                                rs.getString("description"),
                                rs.getString("start_time"),
                                rs.getString("end_time"),
                                rs.getInt("duration_minutes"));
                        loadedExams.add(exam);
                    }
                } catch (Exception ex) {
                    throw ex;
                }
                return loadedExams;
            }

            @Override
            protected void done() {
                try {
                    exams.clear();
                    cardsPanel.removeAll();
                    java.util.List<ExamData> loadedExams = get();
                    int count = 0;
                    for (ExamData exam : loadedExams) {
                        exams.add(exam);
                        cardsPanel.add(createExamCard(exam));
                        cardsPanel.add(Box.createVerticalStrut(18));
                        count++;
                    }
                    progressLabel.setText(count + " exam(s) available");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ExamListPanel.this, "Error loading exams: " + ex.getMessage());
                    progressLabel.setText("Failed to load exams");
                }
                cardsPanel.revalidate();
                cardsPanel.repaint();
            }
        };
        worker.execute();
    }

    private JPanel createExamCard(ExamData exam) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(16, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                BorderFactory.createEmptyBorder(18, 24, 18, 24)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel(exam.title);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(41, 128, 185));
        JLabel desc = new JLabel("<html><div style='width:350px;'>" + exam.description + "</div></html>");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        desc.setForeground(new Color(80, 80, 80));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.add(title);
        infoPanel.add(Box.createVerticalStrut(6));
        infoPanel.add(desc);

        JLabel details = new JLabel("Start: " + exam.start + "   End: " + exam.end + "   Duration: " + exam.duration + " min");
        details.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        details.setForeground(new Color(120, 120, 120));
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(details);

        card.add(infoPanel, BorderLayout.CENTER);

        JButton startBtn = new JButton("Start Exam");
        startBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        startBtn.setBackground(new Color(0, 180, 120));
        startBtn.setForeground(Color.WHITE);
        startBtn.setFocusPainted(false);
        startBtn.setBorder(BorderFactory.createEmptyBorder(10, 28, 10, 28));
        startBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        startBtn.addActionListener(e -> {
            if (parentPanel != null) {
                // Fix: Use the constructor instead of no-arg constructor
                Exam examObj = new Exam(
                    exam.id,
                    exam.title,
                    exam.description,
                    exam.start,
                    exam.end,
                    exam.duration
                );
                parentPanel.startExam(examObj);
            } else {
                JOptionPane.showMessageDialog(this, "Error: parentPanel is null. Cannot start exam.");
            }
        });

        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.add(startBtn);
        card.add(btnPanel, BorderLayout.EAST);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBackground(new Color(245, 250, 255));
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(41, 128, 185), 2, true),
                        BorderFactory.createEmptyBorder(18, 24, 18, 24)));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBackground(Color.WHITE);
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                        BorderFactory.createEmptyBorder(18, 24, 18, 24)));
            }
        });

        return card;
    }

    private static class ExamData {
        int id;
        String title, description, start, end;
        int duration;

        ExamData(int id, String title, String description, String start, String end, int duration) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.start = start;
            this.end = end;
            this.duration = duration;
        }
    }
}