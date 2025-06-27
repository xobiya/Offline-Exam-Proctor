package ui;

import db.DBConnection;
import util.ThemeManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

public class LogManagementPanel extends JPanel {
    private JTable logTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> studentComboBox, examComboBox;
    private Vector<Integer> studentIds = new Vector<>();
    private Vector<Integer> examIds = new Vector<>();
    private JButton refreshButton;

    public LogManagementPanel() {
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
        JButton clearButton = new JButton("Clear");
        JButton exportButton = new JButton("Export CSV");
        filterPanel.add(refreshButton);
        filterPanel.add(clearButton);
        filterPanel.add(exportButton);
        add(filterPanel, BorderLayout.NORTH);
        // Table setup
        tableModel = new DefaultTableModel(new String[]{"ID", "Student", "Exam", "Type", "Detail", "Timestamp"}, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        logTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(logTable);
        add(scrollPane, BorderLayout.CENTER);
        // Actions
        refreshButton.addActionListener(e -> loadLogs());
        studentComboBox.addActionListener(e -> loadLogs());
        examComboBox.addActionListener(e -> loadLogs());
        clearButton.addActionListener(e -> {
            studentComboBox.setSelectedIndex(0);
            examComboBox.setSelectedIndex(0);
            loadLogs();
        });
        exportButton.addActionListener(e -> exportLogsToCSV());
        loadStudents();
        loadExams();
        loadLogs();
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

    private void loadLogs() {
        tableModel.setRowCount(0);
        Integer studentId = null, examId = null;
        if (studentComboBox.getSelectedIndex() > 0) studentId = studentIds.get(studentComboBox.getSelectedIndex() - 1);
        if (examComboBox.getSelectedIndex() > 0) examId = examIds.get(examComboBox.getSelectedIndex() - 1);
        try (Connection conn = DBConnection.getConnection()) {
            StringBuilder sql = new StringBuilder("SELECT a.id, u.full_name, e.title, a.event_type, a.description, a.event_time FROM activity_log a LEFT JOIN users u ON a.student_id=u.id LEFT JOIN exams e ON a.exam_id=e.id WHERE 1=1");
            if (studentId != null) sql.append(" AND a.student_id=" + studentId);
            if (examId != null) sql.append(" AND a.exam_id=" + examId);
            sql.append(" ORDER BY a.event_time DESC");
            PreparedStatement ps = conn.prepareStatement(sql.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt(1));
                row.add(rs.getString(2));
                row.add(rs.getString(3));
                row.add(rs.getString(4));
                row.add(rs.getString(5));
                row.add(rs.getString(6));
                tableModel.addRow(row);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading logs: " + ex.getMessage());
        }
    }

    private void exportLogsToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Logs to CSV");
        fileChooser.setSelectedFile(new java.io.File("logs_export.csv"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (java.io.FileWriter writer = new java.io.FileWriter(fileChooser.getSelectedFile())) {
                // Write header
                writer.append("ID,Student,Exam,Type,Detail,Timestamp\n");
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    writer.append(String.format("%s,%s,%s,%s,%s,%s\n",
                        tableModel.getValueAt(i, 0),
                        tableModel.getValueAt(i, 1),
                        tableModel.getValueAt(i, 2),
                        tableModel.getValueAt(i, 3),
                        tableModel.getValueAt(i, 4),
                        tableModel.getValueAt(i, 5)));
                }
                JOptionPane.showMessageDialog(this, "Logs exported successfully.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error exporting logs: " + ex.getMessage());
            }
        }
    }
}
