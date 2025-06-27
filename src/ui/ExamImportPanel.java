package ui;

import db.DBConnection;
import org.json.JSONArray;
import org.json.JSONObject;
import util.CryptoUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class ExamImportPanel extends JPanel {
    public ExamImportPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JLabel title = new JLabel("Import Exam from USB", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        JButton importBtn = new JButton("Import .exam File");
        importBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        importBtn.addActionListener(e -> importExamFile());

        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.add(importBtn);
        add(centerPanel, BorderLayout.CENTER);
    }

    private void importExamFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Exam File (.exam)");
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                String encrypted = new String(Files.readAllBytes(file.toPath()));
                String jsonString = CryptoUtils.decrypt(encrypted);

                JSONObject examJson = new JSONObject(jsonString);

                if (!examJson.has("id") || !examJson.has("questions")) {
                    throw new Exception("Invalid exam file format.");
                }

                try (Connection conn = DBConnection.getConnection()) {
                    // Insert exam
                    PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO exams (id, title, description, start_time, end_time, duration_minutes, entry_password, exit_password) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE title=VALUES(title), description=VALUES(description), start_time=VALUES(start_time), end_time=VALUES(end_time), duration_minutes=VALUES(duration_minutes), entry_password=VALUES(entry_password), exit_password=VALUES(exit_password)");
                    ps.setInt(1, examJson.getInt("id"));
                    ps.setString(2, examJson.getString("title"));
                    ps.setString(3, examJson.optString("description", ""));
                    ps.setString(4, examJson.optString("start_time", null));
                    ps.setString(5, examJson.optString("end_time", null));
                    ps.setInt(6, examJson.optInt("duration_minutes", 0));
                    ps.setString(7, examJson.optString("entry_password", ""));
                    ps.setString(8, examJson.optString("exit_password", ""));
                    ps.executeUpdate();

                    // Insert questions
                    JSONArray questions = examJson.getJSONArray("questions");
                    for (int i = 0; i < questions.length(); i++) {
                        JSONObject q = questions.getJSONObject(i);
                        ps = conn.prepareStatement(
                            "INSERT INTO questions (id, exam_id, question_text, option_a, option_b, option_c, option_d, correct_option) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE question_text=VALUES(question_text)");
                        ps.setInt(1, q.getInt("id"));
                        ps.setInt(2, examJson.getInt("id"));
                        ps.setString(3, q.getString("question_text"));
                        ps.setString(4, q.getString("option_a"));
                        ps.setString(5, q.getString("option_b"));
                        ps.setString(6, q.getString("option_c"));
                        ps.setString(7, q.getString("option_d"));
                        ps.setString(8, q.getString("correct_option"));
                        ps.executeUpdate();
                    }
                }

                JOptionPane.showMessageDialog(this, "Exam imported successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Import failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
