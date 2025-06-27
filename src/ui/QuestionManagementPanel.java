package ui;

import model.Question;
import ui.QuestionFormDialog;
import db.DBConnection;
import util.ThemeManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

public class QuestionManagementPanel extends JPanel {
    private static QuestionManagementPanel instance;
    private JComboBox<String> examComboBox;
    private JTable questionTable;
    private DefaultTableModel tableModel;
    private JButton addButton, editButton, deleteButton, refreshButton;
    private Vector<Integer> examIds = new Vector<>();

    public QuestionManagementPanel() {
        instance = this;
        setLayout(new BorderLayout(16, 16));
        setBackground(ThemeManager.getPrimaryBackground());

        // Top Panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 16));
        topPanel.setBackground(ThemeManager.getPrimaryBackground());
        JLabel selectExamLabel = new JLabel("Select Exam:");
        selectExamLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        examComboBox = new JComboBox<>();
        examComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        examComboBox.setPreferredSize(new Dimension(220, 32));
        topPanel.add(selectExamLabel);
        topPanel.add(examComboBox);
        add(topPanel, BorderLayout.NORTH);

        // Search bar panel
        JPanel searchPanel = new JPanel(null);
        searchPanel.setPreferredSize(new Dimension(300, 35));
        searchPanel.setBackground(ThemeManager.getPrimaryBackground());
        JTextField searchField = new JTextField();
        searchField.setBounds(0, 0, 300, 35);
        searchField.setBackground(Color.WHITE);
        searchField.setForeground(Color.DARK_GRAY);
        searchField.setCaretColor(Color.DARK_GRAY);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 200), 1, true),
            BorderFactory.createEmptyBorder(5, 10, 5, 35)
        ));
        searchField.putClientProperty("JTextField.placeholderText", "Search questions...");
        JLabel searchIcon = new JLabel();
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
        add(searchPanel, BorderLayout.BEFORE_FIRST_LINE);

        // Table
        tableModel = new DefaultTableModel(new String[]{"ID", "Question", "A", "B", "C", "D", "Correct"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        questionTable = new JTable(tableModel);
        questionTable.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        questionTable.setRowHeight(32);
        questionTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        questionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        questionTable.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(questionTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 16));
        buttonPanel.setBackground(ThemeManager.getPrimaryBackground());
        addButton = createModernButton("Add Question", new Color(46, 204, 113));
        editButton = createModernButton("Edit Question", new Color(241, 196, 15));
        deleteButton = createModernButton("Delete Question", new Color(231, 76, 60));
        refreshButton = createModernButton("Refresh", new Color(52, 152, 219));
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Event Handlers
        addButton.addActionListener(e -> showQuestionForm(null));
        editButton.addActionListener(e -> editSelectedQuestion());
        deleteButton.addActionListener(e -> deleteSelectedQuestion());
        refreshButton.addActionListener(e -> loadQuestions());
        examComboBox.addActionListener(e -> loadQuestions());

        // Add search filter logic
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                String query = searchField.getText();
                javax.swing.table.TableRowSorter<DefaultTableModel> sorter = new javax.swing.table.TableRowSorter<>(tableModel);
                questionTable.setRowSorter(sorter);
                if (query.trim().isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + query));
                }
            }
        });

        loadExams();
    }

    private JButton createModernButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static QuestionManagementPanel getInstance() {
        return instance;
    }

    public void loadExams() {
        examComboBox.removeAllItems();
        examIds.clear();
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
        if (examComboBox.getItemCount() > 0) {
            examComboBox.setSelectedIndex(0);
            loadQuestions();
        }
    }

    private void loadQuestions() {
        tableModel.setRowCount(0);
        int examIdx = examComboBox.getSelectedIndex();
        if (examIdx == -1 || examIdx >= examIds.size()) return;
        int examId = examIds.get(examIdx);
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM questions WHERE exam_id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, examId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("question_text"));
                row.add(rs.getString("option_a"));
                row.add(rs.getString("option_b"));
                row.add(rs.getString("option_c"));
                row.add(rs.getString("option_d"));
                row.add(rs.getString("correct_option"));
                tableModel.addRow(row);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading questions: " + ex.getMessage());
        }
    }

private void showQuestionForm(Question question) {
    Question result = QuestionFormDialog.showDialog(this, question);
    if (result != null) {
        if (question == null) {
            addQuestion(result.getQuestionText(), result.getOptionA(), result.getOptionB(),
                    result.getOptionC(), result.getOptionD(), result.getCorrectOption());
        } else {
            updateQuestion(result.getId(), result.getQuestionText(), result.getOptionA(),
                    result.getOptionB(), result.getOptionC(), result.getOptionD(), result.getCorrectOption());
        }
    }
}

    private void addQuestion(String q, String a, String b, String c, String d, String correct) {
        int examIdx = examComboBox.getSelectedIndex();
        if (examIdx == -1 || examIdx >= examIds.size()) return;
        int examId = examIds.get(examIdx);
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO questions (exam_id, question_text, option_a, option_b, option_c, option_d, correct_option) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, examId);
            ps.setString(2, q);
            ps.setString(3, a);
            ps.setString(4, b);
            ps.setString(5, c);
            ps.setString(6, d);
            ps.setString(7, correct);
            ps.executeUpdate();
            loadQuestions();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error adding question: " + ex.getMessage());
        }
    }

    private void updateQuestion(int id, String q, String a, String b, String c, String d, String correct) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE questions SET question_text=?, option_a=?, option_b=?, option_c=?, option_d=?, correct_option=? WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, q);
            ps.setString(2, a);
            ps.setString(3, b);
            ps.setString(4, c);
            ps.setString(5, d);
            ps.setString(6, correct);
            ps.setInt(7, id);
            ps.executeUpdate();
            loadQuestions();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating question: " + ex.getMessage());
        }
    }

    private void editSelectedQuestion() {
        int row = questionTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a question to edit.");
            return;
        }
        Question q = new Question();
        q.setId((int) tableModel.getValueAt(row, 0));
        q.setQuestionText((String) tableModel.getValueAt(row, 1));
        q.setOptionA((String) tableModel.getValueAt(row, 2));
        q.setOptionB((String) tableModel.getValueAt(row, 3));
        q.setOptionC((String) tableModel.getValueAt(row, 4));
        q.setOptionD((String) tableModel.getValueAt(row, 5));
        q.setCorrectOption((String) tableModel.getValueAt(row, 6));
        showQuestionForm(q);
    }

    private void deleteSelectedQuestion() {
        int row = questionTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a question to delete.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this question?", "Confirm",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "DELETE FROM questions WHERE id=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, id);
                ps.executeUpdate();
                loadQuestions();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting question: " + ex.getMessage());
            }
        }
    }
}
