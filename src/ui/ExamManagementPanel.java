package ui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import model.ExamExporter;
import model.ExamTransfer;
import util.CryptoUtils;
import util.PasswordUtils;
import db.DBConnection;
import ui.QuestionManagementPanel;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;


public class ExamManagementPanel extends JPanel {
    // UI Components
    private JTable examTable;
    private DefaultTableModel tableModel;
    private JButton addButton, editButton, deleteButton, refreshButton, exportButton, lanServerButton;
    private JTextField searchField;
    
    // Color Scheme
    private final Color primaryColor = new Color(0, 120, 215);
    private final Color successColor = new Color(46, 204, 113);
    private final Color dangerColor = new Color(231, 76, 60);
    private final Color warningColor = new Color(241, 196, 15);
    private final Color darkBackground = new Color(45, 45, 48);
    private final Color tableBackground = new Color(37, 37, 38);
    private final Color tableHeaderBackground = new Color(62, 62, 64);
    private final Color tableSelectionColor = new Color(62, 92, 118);
    private final Color borderColor = new Color(90, 90, 90);

    // Sample data (replace with database connection in real implementation)
    private final List<model.Exam> exams = new ArrayList<>();

    private ServerSocket examServerSocket;
    private Thread examServerThread;

    public ExamManagementPanel() {
        initUI();
        loadSampleData();
        setupListeners();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(darkBackground);
        
        createHeaderPanel();
        createTablePanel();
        createButtonPanel();
    }

    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setBackground(darkBackground);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        // Title with icon
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setBackground(darkBackground);
        
        JLabel titleLabel = new JLabel("Exam Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        
        titlePanel.add(createIconLabel());
        titlePanel.add(titleLabel);
        headerPanel.add(titlePanel, BorderLayout.WEST);
        
        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout(0, 0));
        searchPanel.setBackground(darkBackground);

        // Container for text field and icon
        JPanel searchFieldPanel = new JPanel(null); // null layout for overlay
        searchFieldPanel.setPreferredSize(new Dimension(300, 35));
        searchFieldPanel.setBackground(darkBackground);

        searchField = new JTextField();
        searchField.setBounds(0, 0, 300, 35);
        searchField.setBackground(tableBackground);
        searchField.setForeground(Color.WHITE);
        searchField.setCaretColor(Color.WHITE);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(1, 1, 1, 1, borderColor),
            new EmptyBorder(5, 10, 5, 35)
        ));
        searchField.putClientProperty("JTextField.placeholderText", "Search exams...");

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
        searchIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                filterTable(searchField.getText());
            }
        });
        // If the icon is still not visible, check the icon file and path, and try a colored PNG for testing.

        searchFieldPanel.add(searchField);
        searchFieldPanel.add(searchIcon);
        searchPanel.add(searchFieldPanel, BorderLayout.CENTER);

        headerPanel.add(searchPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);
    }

    private void createTablePanel() {
        tableModel = new DefaultTableModel(new Object[] { 
            "ID", "Title", "Description", "Start Date", "End Date", "Duration", "Status" 
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                switch (column) {
                    case 0: return Integer.class;
                    case 5: return Integer.class;
                    case 6: return String.class;
                    default: return String.class;
                }
            }
        };
        
        examTable = new JTable(tableModel);
        customizeTableAppearance();
        
        JScrollPane scrollPane = new JScrollPane(examTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(tableBackground);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void customizeTableAppearance() {
        examTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        examTable.setRowHeight(35);
        examTable.setIntercellSpacing(new Dimension(0, 0));
        examTable.setShowGrid(false);
        examTable.setBackground(tableBackground);
        examTable.setForeground(Color.WHITE);
        examTable.setSelectionBackground(tableSelectionColor);
        examTable.setSelectionForeground(Color.WHITE);
        examTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        examTable.setAutoCreateRowSorter(true);
        
        examTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, 
                        isSelected, hasFocus, row, column);
                
                if (column == 6) {
                    String status = (String) value;
                    c.setForeground(Color.WHITE);
                    
                    if ("Active".equals(status)) {
                        c.setBackground(successColor);
                    } else if ("Upcoming".equals(status)) {
                        c.setBackground(warningColor);
                    } else if ("Expired".equals(status)) {
                        c.setBackground(dangerColor);
                    } else {
                        c.setBackground(tableBackground);
                    }
                } else {
                    c.setBackground(isSelected ? tableSelectionColor : tableBackground);
                }
                
                return c;
            }
        });
        
        JTableHeader header = examTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(tableHeaderBackground);
        header.setForeground(Color.WHITE);
        header.setBorder(new MatteBorder(0, 0, 1, 0, borderColor));
        
        TableColumnModel columnModel = examTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);
        columnModel.getColumn(1).setPreferredWidth(200);
        columnModel.getColumn(2).setPreferredWidth(300);
        columnModel.getColumn(3).setPreferredWidth(150);
        columnModel.getColumn(4).setPreferredWidth(150);
        columnModel.getColumn(5).setPreferredWidth(80);
        columnModel.getColumn(6).setPreferredWidth(100);
    }

    private void createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(darkBackground);
        buttonPanel.setBorder(new MatteBorder(1, 0, 0, 0, borderColor));

        refreshButton = createModernButton("Refresh", primaryColor);
        addButton = createModernButton("Add Exam", successColor);
        editButton = createModernButton("Edit Exam", warningColor);
        deleteButton = createModernButton("Delete Exam", dangerColor);
        exportButton = createModernButton("Export Exam", new Color(52, 152, 219));
        lanServerButton = createModernButton("Start LAN Exam Server", new Color(52, 152, 219));

        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        exportButton.setEnabled(false);

        buttonPanel.add(refreshButton);
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(lanServerButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JButton createModernButton(String text, Color color) {
        JButton button = new JButton(text);
        
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorder(new CompoundBorder(
            new MatteBorder(1, 1, 1, 1, color.darker()),
            new EmptyBorder(8, 15, 8, 15)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.brighter());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                button.setBackground(color.darker());
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                button.setBackground(color.brighter());
            }
        });
        
        return button;
    }

    private void setupListeners() {
        examTable.getSelectionModel().addListSelectionListener(e -> {
            boolean rowSelected = examTable.getSelectedRow() != -1;
            editButton.setEnabled(rowSelected);
            deleteButton.setEnabled(rowSelected);
            exportButton.setEnabled(rowSelected);
        });
        
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterTable(searchField.getText());
            }
        });
        
        addButton.addActionListener(e -> showExamForm(null));
        editButton.addActionListener(e -> editSelectedExam());
        deleteButton.addActionListener(e -> deleteSelectedExam());
        refreshButton.addActionListener(e -> refreshData());
        exportButton.addActionListener(e -> exportSelectedExam());
        lanServerButton.addActionListener(e -> startLanExamServer());
    }

    private void filterTable(String query) {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        examTable.setRowSorter(sorter);
        
        if (query.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query));
        }
    }

    private void loadSampleData() {
        // Sample data - replace with database connection in real implementation
        exams.clear();
        exams.add(new model.Exam(1, "Midterm Exam", "Covering chapters 1-5", 
                LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                90));
        exams.add(new model.Exam(2, "Final Exam", "Comprehensive exam", 
                LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                120));
        exams.add(new model.Exam(3, "Quiz 1", "Basic concepts", 
                LocalDateTime.now().minusDays(3).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                LocalDateTime.now().minusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                45));
        
        refreshData();
    }

    private void refreshData() {
        tableModel.setRowCount(0);
        try (java.sql.Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT id, title, description, start_time, end_time, duration_minutes FROM exams";
            java.sql.PreparedStatement ps = conn.prepareStatement(sql);
            java.sql.ResultSet rs = ps.executeQuery();
            java.time.format.DateTimeFormatter dbFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            java.time.format.DateTimeFormatter displayFormatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String desc = rs.getString("description");
                String start = rs.getString("start_time");
                String end = rs.getString("end_time");
                int duration = rs.getInt("duration_minutes");
                String formattedStart = start != null ? java.time.LocalDateTime.parse(start, dbFormatter).format(displayFormatter) : "N/A";
                String formattedEnd = end != null ? java.time.LocalDateTime.parse(end, dbFormatter).format(displayFormatter) : "N/A";
                String status = determineExamStatus(start, end, dbFormatter);
                tableModel.addRow(new Object[]{id, title, desc, formattedStart, formattedEnd, duration, status});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading exams: " + ex.getMessage());
        }
    }

    private String formatDateTime(String dateTime, DateTimeFormatter dbFormatter, DateTimeFormatter displayFormatter) {
        if (dateTime == null) return "N/A";
        
        try {
            LocalDateTime dt = LocalDateTime.parse(dateTime, dbFormatter);
            return dt.format(displayFormatter);
        } catch (Exception e) {
            return "N/A";
        }
    }

    private String determineExamStatus(String startTime, String endTime, DateTimeFormatter formatter) {
        if (startTime == null || endTime == null) return "N/A";
        
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = LocalDateTime.parse(startTime, formatter);
            LocalDateTime end = LocalDateTime.parse(endTime, formatter);
            
            if (now.isBefore(start)) {
                return "Upcoming";
            } else if (now.isAfter(start) && now.isBefore(end)) {
                return "Active";
            } else {
                return "Expired";
            }
        } catch (Exception e) {
            return "N/A";
        }
    }

    private void showExamForm(model.Exam exam) {
        JDialog dialog = new JDialog();
        dialog.setTitle(exam == null ? "Add New Exam" : "Edit Exam");
        dialog.setModal(true);
        dialog.setSize(550, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel();
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        
        JTextField titleField = new JTextField(exam != null ? exam.getTitle() : "");
        JTextArea descArea = new JTextArea(exam != null ? exam.getDescription() : "", 4, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        
        JPanel datePanel = new JPanel(new GridLayout(2, 2, 10, 10));
        JTextField startDateField = new JTextField(exam != null ? exam.getStartTime() : "");
        JTextField endDateField = new JTextField(exam != null ? exam.getEndTime() : "");
        
        datePanel.add(createFormField("Start Date:", startDateField));
        datePanel.add(createDatePickerButton(startDateField));
        datePanel.add(createFormField("End Date:", endDateField));
        datePanel.add(createDatePickerButton(endDateField));
        
        JTextField durationField = new JTextField(exam != null ? String.valueOf(exam.getDurationMinutes()) : "");
        
        // Password fields
        JPasswordField entryPasswordField = new JPasswordField();
        JPasswordField exitPasswordField = new JPasswordField();
        if (exam != null) {
            // Show placeholder for security, do not prefill
            entryPasswordField.setText("");
            exitPasswordField.setText("");
        }
        formPanel.add(createFormField("Title:", titleField));
        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(createFormField("Description:", new JScrollPane(descArea)));
        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(datePanel);
        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(createFormField("Duration (minutes):", durationField));
        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(createFormField("Entry Password:", entryPasswordField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormField("Exit Password:", exitPasswordField));
        formPanel.add(Box.createVerticalGlue());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JButton cancelButton = createModernButton("Cancel", new Color(120, 120, 120));
        cancelButton.addActionListener(e -> dialog.dispose());
        
        JButton saveButton = createModernButton("Save", successColor);
        saveButton.addActionListener(e -> {
            if (validateExamForm(titleField, durationField)) {
                String entryPassword = new String(entryPasswordField.getPassword());
                String exitPassword = new String(exitPasswordField.getPassword());
                String entryPasswordHash = entryPassword.isEmpty() ? (exam != null ? exam.getEntryPassword() : null) : PasswordUtils.hashPassword(entryPassword);
                String exitPasswordHash = exitPassword.isEmpty() ? (exam != null ? exam.getExitPassword() : null) : PasswordUtils.hashPassword(exitPassword);
                saveExam(
                    exam != null ? exam.getId() : -1,
                    titleField.getText(),
                    descArea.getText(),
                    startDateField.getText(),
                    endDateField.getText(),
                    durationField.getText(),
                    entryPasswordHash,
                    exitPasswordHash
                );
                dialog.dispose();
            }
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JPanel createFormField(String label, Component field) {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        jLabel.setForeground(Color.BLACK); // Set label text color to black
        panel.add(jLabel, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private JButton createDatePickerButton(JTextField targetField) {
        JButton button = new JButton("Select Date");
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setPreferredSize(new Dimension(120, 30));
        
        button.addActionListener(e -> {
            JPanel panel = new JPanel(new BorderLayout());
            
            JSpinner timeSpinner = new JSpinner(new SpinnerDateModel());
            JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm:ss");
            timeSpinner.setEditor(timeEditor);
            
            JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
            JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
            dateSpinner.setEditor(dateEditor);
            
            JPanel spinnerPanel = new JPanel(new GridLayout(2, 1));
            spinnerPanel.add(dateSpinner);
            spinnerPanel.add(timeSpinner);
            
            panel.add(spinnerPanel, BorderLayout.CENTER);
            
            int option = JOptionPane.showConfirmDialog(
                this, 
                panel, 
                "Select Date/Time", 
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
            
            if (option == JOptionPane.OK_OPTION) {
                Date date = (Date) dateSpinner.getValue();
                Date time = (Date) timeSpinner.getValue();
                
                Calendar dateCal = Calendar.getInstance();
                dateCal.setTime(date);
                
                Calendar timeCal = Calendar.getInstance();
                timeCal.setTime(time);
                
                dateCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
                dateCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
                dateCal.set(Calendar.SECOND, timeCal.get(Calendar.SECOND));
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                targetField.setText(sdf.format(dateCal.getTime()));
            }
        });
        
        return button;
    }

    private boolean validateExamForm(JTextField titleField, JTextField durationField) {
        if (titleField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title is required", "Error", JOptionPane.ERROR_MESSAGE);
            titleField.requestFocus();
            return false;
        }
        
        try {
            int duration = Integer.parseInt(durationField.getText());
            if (duration <= 0) {
                JOptionPane.showMessageDialog(this, "Duration must be positive", "Error", JOptionPane.ERROR_MESSAGE);
                durationField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid duration value", "Error", JOptionPane.ERROR_MESSAGE);
            durationField.requestFocus();
            return false;
        }
        
        return true;
    }

    private void saveExam(int id, String title, String desc, String start, String end, String duration, String entryPasswordHash, String exitPasswordHash) {
        try (java.sql.Connection conn = DBConnection.getConnection()) {
            if (id == -1) {
                // Insert new exam into DB
                String sql = "INSERT INTO exams (title, description, start_time, end_time, duration_minutes, entry_password, exit_password) VALUES (?, ?, ?, ?, ?, ?, ?)";
                java.sql.PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, title);
                ps.setString(2, desc);
                ps.setString(3, start);
                ps.setString(4, end);
                ps.setInt(5, Integer.parseInt(duration));
                ps.setString(6, entryPasswordHash);
                ps.setString(7, exitPasswordHash);
                ps.executeUpdate();
            } else {
                // Update existing exam in DB
                String sql = "UPDATE exams SET title=?, description=?, start_time=?, end_time=?, duration_minutes=?, entry_password=?, exit_password=? WHERE id=?";
                java.sql.PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, title);
                ps.setString(2, desc);
                ps.setString(3, start);
                ps.setString(4, end);
                ps.setInt(5, Integer.parseInt(duration));
                ps.setString(6, entryPasswordHash);
                ps.setString(7, exitPasswordHash);
                ps.setInt(8, id);
                ps.executeUpdate();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving exam: " + ex.getMessage());
        }
        // Refresh QuestionManagementPanel exam list
        QuestionManagementPanel qmp = QuestionManagementPanel.getInstance();
        if (qmp != null) {
            qmp.loadExams();
        }
        refreshData();
    }

    private void editSelectedExam() {
        int viewRow = examTable.getSelectedRow();
        if (viewRow == -1) return;
        
        int modelRow = examTable.convertRowIndexToModel(viewRow);
        int id = (int) tableModel.getValueAt(modelRow, 0);
        
        for (model.Exam exam : exams) {
            if (exam.getId() == id) {
                showExamForm(exam);
                break;
            }
        }
    }

    private void deleteSelectedExam() {
        int viewRow = examTable.getSelectedRow();
        if (viewRow == -1) return;
        
        int modelRow = examTable.convertRowIndexToModel(viewRow);
        int id = (int) tableModel.getValueAt(modelRow, 0);
        String title = (String) tableModel.getValueAt(modelRow, 1);
        
        int option = JOptionPane.showConfirmDialog(
            this, 
            createDeleteConfirmationPanel(title), 
            "Confirm Deletion", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (option == JOptionPane.YES_OPTION) {
            exams.removeIf(exam -> exam.getId() == id);
            refreshData();
        }
    }

    private JPanel createDeleteConfirmationPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(new JLabel("<html><b>Are you sure you want to delete this exam?</b></html>"), BorderLayout.NORTH);
        
        JLabel examLabel = new JLabel("<html><i>" + title + "</i></html>");
        examLabel.setBorder(new EmptyBorder(10, 20, 10, 20));
        panel.add(examLabel, BorderLayout.CENTER);
        
        return panel;
    }

    private JLabel createIconLabel() {
        JLabel iconLabel = new JLabel();
        // Use a default icon or replace with your own icon path
        iconLabel.setIcon(UIManager.getIcon("FileView.directoryIcon"));
        return iconLabel;
    }

    private JLabel createSearchIcon() {
        JLabel searchIcon = new JLabel();
        // Use a default icon or replace with your own icon path
        searchIcon.setIcon(UIManager.getIcon("FileView.fileIcon"));
        searchIcon.setBorder(new EmptyBorder(0, 0, 0, 10));
        return searchIcon;
    }

    private void exportSelectedExam() {
        int viewRow = examTable.getSelectedRow();
        if (viewRow == -1) return;
        int modelRow = examTable.convertRowIndexToModel(viewRow);
        int examId = (int) tableModel.getValueAt(modelRow, 0);
        try (java.sql.Connection conn = db.DBConnection.getConnection()) {
            // Export exam as JSON
          
org.json.JSONObject examJson = model.ExamExporter.getExamAsJson(examId, conn);
// Ensure entry_password and exit_password are present for import compatibility
if (!examJson.has("entry_password") || examJson.isNull("entry_password")) {
    examJson.put("entry_password", "");
}
if (!examJson.has("exit_password") || examJson.isNull("exit_password")) {
    examJson.put("exit_password", "");
}
            String jsonString = examJson.toString(2); // Pretty print
            String encrypted = util.CryptoUtils.encrypt(jsonString);
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Export Exam to USB or File");
            fileChooser.setSelectedFile(new java.io.File("exam_" + examId + ".exam"));
            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                java.io.File file = fileChooser.getSelectedFile();
                try (java.io.FileWriter fw = new java.io.FileWriter(file)) {
                    fw.write(encrypted);
                    fw.flush();
                    JOptionPane.showMessageDialog(this, "Exam exported successfully!\nFile: " + file.getAbsolutePath());
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Call this method to start the LAN exam server
    public void startExamServer(model.Exam exam, ArrayList<ui.ExamTakingPanel.QuestionData> questions, int port) {
        ExamTransfer transfer = new ExamTransfer(exam, questions);
        examServerThread = new Thread(() -> {
            try {
                examServerSocket = new ServerSocket(port);
                while (!Thread.currentThread().isInterrupted()) {
                    Socket client = examServerSocket.accept();
                    ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                    out.writeObject(transfer);
                    out.flush();
                    client.close();
                }
            } catch (IOException e) {
                // Optionally show error in GUI
            }
        });
        examServerThread.start();
    }

    public void stopExamServer() {
        try {
            if (examServerSocket != null) examServerSocket.close();
            if (examServerThread != null) examServerThread.interrupt();
        } catch (IOException e) {}
    }

    private void startLanExamServer() {
        int row = examTable.getSelectedRow();
        if (row != -1) {
            int examId = (int) tableModel.getValueAt(row, 0);
            model.Exam selectedExam = null;
            ArrayList<ui.ExamTakingPanel.QuestionData> questions = new ArrayList<>();
            // Find the selected exam and its questions
            for (model.Exam exam : exams) {
                if (exam.getId() == examId) {
                    selectedExam = exam;
                    break;
                }
            }
            if (selectedExam != null) {
                // TODO: Load questions for the selected exam
                // Example: questions = loadQuestionsForExam(selectedExam.getId());
                
                startExamServer(selectedExam, questions, 5000); // Port 5000
                JOptionPane.showMessageDialog(this, "LAN Exam Server started on port 5000");
            } else {
                JOptionPane.showMessageDialog(this, "Selected exam not found", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}