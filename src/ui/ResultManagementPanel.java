package ui;

import db.DBConnection;
import util.ThemeManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

public class ResultManagementPanel extends JPanel {
    private JTable resultTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> studentComboBox, examComboBox;
    private Vector<Integer> studentIds = new Vector<>();
    private Vector<Integer> examIds = new Vector<>();
    private JButton refreshButton, exportButton;

    public ResultManagementPanel() {
        setLayout(new BorderLayout());
        setBackground(ThemeManager.getPrimaryBackground());
        // Top filter panel
        JPanel filterPanel = new JPanel();
        filterPanel.setBackground(ThemeManager.getPrimaryBackground());
        filterPanel.add(new JLabel("Student:"));
        studentComboBox = new JComboBox<>();
        filterPanel.add(studentComboBox);
        filterPanel.add(new JLabel("Exam:"));
        examComboBox = new JComboBox<>();
        filterPanel.add(examComboBox);
        refreshButton = new JButton("Refresh");
        filterPanel.add(refreshButton);
        exportButton = new JButton("Export CSV");
        filterPanel.add(exportButton);
        add(filterPanel, BorderLayout.NORTH);
        // Table setup
        tableModel = new DefaultTableModel(new String[]{"ID", "Student", "Exam", "Score", "Taken At"}, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        resultTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(resultTable);
        add(scrollPane, BorderLayout.CENTER);
        // Actions
        refreshButton.addActionListener(e -> loadResults());
        studentComboBox.addActionListener(e -> loadResults());
        examComboBox.addActionListener(e -> loadResults());
        exportButton.addActionListener(e -> exportToCSV());
        loadStudents();
        loadExams();
        loadResults();
    }

    private void loadStudents() {
        studentComboBox.removeAllItems();
        studentIds.clear();
        studentComboBox.addItem("All");
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT id, full_name FROM users WHERE role='student'";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                studentIds.add(rs.getInt("id"));
                studentComboBox.addItem(rs.getString("full_name"));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading students: " + ex.getMessage());
        }
    }

    private void loadExams() {
        examComboBox.removeAllItems();
        examIds.clear();
        examComboBox.addItem("All");
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT id, title FROM exams";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                examIds.add(rs.getInt("id"));
                examComboBox.addItem(rs.getString("title"));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading exams: " + ex.getMessage());
        }
    }

    private void loadResults() {
        tableModel.setRowCount(0);
        Integer studentId = null, examId = null;
        if (studentComboBox.getSelectedIndex() > 0) studentId = studentIds.get(studentComboBox.getSelectedIndex() - 1);
        if (examComboBox.getSelectedIndex() > 0) examId = examIds.get(examComboBox.getSelectedIndex() - 1);
        try (Connection conn = DBConnection.getConnection()) {
            StringBuilder sql = new StringBuilder("SELECT results.id, u.full_name, e.title, results.score, results.taken_at FROM results LEFT JOIN users u ON results.student_id=u.id LEFT JOIN exams e ON results.exam_id=e.id WHERE 1=1");
            if (studentId != null) sql.append(" AND results.student_id=" + studentId);
            if (examId != null) sql.append(" AND results.exam_id=" + examId);
            sql.append(" ORDER BY results.taken_at DESC");
            PreparedStatement ps = conn.prepareStatement(sql.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt(1));
                row.add(rs.getString(2));
                row.add(rs.getString(3));
                row.add(rs.getInt(4));
                row.add(rs.getString(5));
                tableModel.addRow(row);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading results: " + ex.getMessage());
        }
    }

    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Results as CSV");
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try (FileWriter csvWriter = new FileWriter(fileChooser.getSelectedFile())) {
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    csvWriter.append(tableModel.getColumnName(i));
                    if (i < tableModel.getColumnCount() - 1) csvWriter.append(",");
                }
                csvWriter.append("\n");
                for (int row = 0; row < tableModel.getRowCount(); row++) {
                    for (int col = 0; col < tableModel.getColumnCount(); col++) {
                        csvWriter.append(String.valueOf(tableModel.getValueAt(row, col)));
                        if (col < tableModel.getColumnCount() - 1) csvWriter.append(",");
                    }
                    csvWriter.append("\n");
                }
                JOptionPane.showMessageDialog(this, "Results exported successfully.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error exporting CSV: " + ex.getMessage());
            }
        }
    }
}
