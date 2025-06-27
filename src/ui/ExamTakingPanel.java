package ui;

import db.DBConnection;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import model.Exam;
import model.User;
import util.PasswordUtils;


public class ExamTakingPanel extends JPanel {
    private static final String FONT_NAME = "Segoe UI";
    private static final Color COLOR_ANSWERED   = new Color(46, 204, 113);
    private static final Color COLOR_CURRENT    = new Color(52, 152, 219);
    private static final Color COLOR_UNANSWERED = new Color(231, 76, 60);

    private final User studentUser;
    private final Exam exam;
    private final JPanel parentPanel;

    private final List<QuestionData> questions = new ArrayList<>();
    private final Map<Integer, String> answers = new HashMap<>();
    private int currentIndex = 0;

    private JLabel questionLabel;
    private JLabel timerLabel;
    private JLabel progressLabel;
    private RoundedOptionButton[] optionButtons = new RoundedOptionButton[4];
    private ButtonGroup optionGroup = new ButtonGroup();
    private JButton nextButton;
    private JButton prevButton;
    private JButton submitButton;
    private JProgressBar progressBar;
    private JPanel questionNavigationInnerPanel;
    private JScrollPane questionNavScrollPane;

    private javax.swing.Timer examTimer;
    private int timeLeftSeconds;
    private long lastActivityTimestamp;

    private volatile boolean examSubmitted = false; // Prevent double submission
    private volatile boolean exitPromptActive = false; // Prevent multiple exit prompts
    
    public ExamTakingPanel(User user, Exam exam, JPanel parentPanel) {
        this.studentUser = user;
        this.exam = exam;
        this.parentPanel = parentPanel;

        // Prompt for entry password on EDT
        SwingUtilities.invokeLater(() -> {
            boolean passwordOk = promptExamEntryPassword();
            if (!passwordOk) {
                if (parentPanel instanceof StudentDashboardPanel) {
                    ((StudentDashboardPanel) parentPanel).showExamList();
                }
                return;
            }
            // After authentication, initialize UI and load questions
            initUI();
            loadQuestionsFromExamInBackground();
            openExamFrame();
        });
    }

    /** Prompt user for the exam entry password. */
    private boolean promptExamEntryPassword() {
        String entryPasswordHash = getExamPasswordHashFromDB("entry_password");
        if (entryPasswordHash == null || entryPasswordHash.isEmpty()) {
            JOptionPane.showMessageDialog(
                null,
                "Exam entry password is not set.",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel label = new JLabel("Enter exam password:");
        label.setFont(new Font(FONT_NAME, Font.BOLD, 14));
        panel.add(label, BorderLayout.NORTH);
        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setFont(new Font(FONT_NAME, Font.PLAIN, 14));
        panel.add(passwordField, BorderLayout.CENTER);

        int option = JOptionPane.showConfirmDialog(
            null,
            panel,
            "Exam Authentication",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );
        if (option == JOptionPane.OK_OPTION) {
            String entered = new String(passwordField.getPassword());
            if (PasswordUtils.verifyPassword(entered, entryPasswordHash)) {
                return true;
            } else {
                JOptionPane.showMessageDialog(
                    null,
                    "Incorrect password.",
                    "Authentication Failed",
                    JOptionPane.ERROR_MESSAGE
                );
                return false;
            }
        }
        return false;
    }

    /** Prompt user for the exam exit password when attempting to close/minimize. */
    private boolean promptExamExitPassword(JFrame parentFrame) {
        String exitPasswordHash = getExamPasswordHashFromDB("exit_password");
        if (exitPasswordHash == null || exitPasswordHash.isEmpty()) {
            JOptionPane.showMessageDialog(
                parentFrame,
                "Exam exit password is not set.",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel label = new JLabel("Enter exam exit password:");
        label.setFont(new Font(FONT_NAME, Font.BOLD, 14));
        panel.add(label, BorderLayout.NORTH);
        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setFont(new Font(FONT_NAME, Font.PLAIN, 14));
        panel.add(passwordField, BorderLayout.CENTER);

        int option = JOptionPane.showConfirmDialog(
            parentFrame,
            panel,
            "Confirm Exit",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (option == JOptionPane.OK_OPTION) {
            String entered = new String(passwordField.getPassword());
            if (PasswordUtils.verifyPassword(entered, exitPasswordHash)) {
                return true;
            } else {
                JOptionPane.showMessageDialog(
                    parentFrame,
                    "Incorrect exit password. Exam window will remain open.",
                    "Incorrect Password",
                    JOptionPane.ERROR_MESSAGE
                );
                return false;
            }
        }
        return false;
    }

    /** Retrieves the hashed password from the database (entry_password or exit_password). */
    private String getExamPasswordHashFromDB(String column) {
        String hash = null;
        String sql = "SELECT " + column + " FROM exams WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, exam.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    hash = rs.getString(1);
                }
            }
        } catch (SQLException | IOException e) {
            JOptionPane.showMessageDialog(
                this,
                "Error retrieving password: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
        return hash;
    }

    /** Sets up the Swing UI components (without loading questions). */
    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UIManager.getColor("Panel.background"));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Header Panel: exam title + timer
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Center Panel: question text + options + progress
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);

        // Navigation Panel: Previous, Next, Submit
        JPanel navPanel = createNavigationPanel();
        add(navPanel, BorderLayout.SOUTH);

        // Question Navigation: scrollable grid of question buttons
        questionNavigationInnerPanel = new JPanel(new GridLayout(0, 5, 5, 5));
        questionNavigationInnerPanel.setBackground(UIManager.getColor("Panel.background"));
        questionNavScrollPane = new JScrollPane(questionNavigationInnerPanel);
        questionNavScrollPane.setPreferredSize(new Dimension(120, 0));
        add(questionNavScrollPane, BorderLayout.EAST);

        // Keyboard shortcuts: Left/Right arrows
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "next");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "prev");
        actionMap.put("next", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (nextButton.isEnabled()) {
                    showQuestion(currentIndex + 1);
                }
            }
        });
        actionMap.put("prev", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (prevButton.isEnabled()) {
                    showQuestion(currentIndex - 1);
                }
            }
        });

        // Initialize lastActivityTimestamp
        lastActivityTimestamp = System.currentTimeMillis();
        setupActivityMonitoring();
    }

    /** Creates the header panel (title + timer). */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        headerPanel.setBackground(UIManager.getColor("Panel.background"));

        JLabel titleLabel = new JLabel("Exam: " + (exam != null ? exam.getTitle() : ""));
        titleLabel.setFont(new Font(FONT_NAME, Font.BOLD, 20));
        titleLabel.setForeground(UIManager.getColor("Label.foreground"));

        timerLabel = new JLabel();
        timerLabel.setFont(new Font(FONT_NAME, Font.BOLD, 20));
        timerLabel.setForeground(UIManager.getColor("Label.foreground"));

        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        timePanel.setBackground(UIManager.getColor("Panel.background"));
        timePanel.add(timerLabel);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(timePanel, BorderLayout.EAST);
        return headerPanel;
    }

    /** Creates the center panel containing question text, options, and progress bar. */
    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(UIManager.getColor("Panel.background"));
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 220, 220)),
            new EmptyBorder(15, 15, 15, 15)
        ));

        questionLabel = new JLabel();
        questionLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 18));
        questionLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        questionLabel.setVerticalAlignment(SwingConstants.TOP);

        // Options panel (4 radio buttons)
        JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        optionsPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        optionsPanel.setBackground(UIManager.getColor("Panel.background"));
        for (int i = 0; i < 4; i++) {
            optionButtons[i] = new RoundedOptionButton(i);
            optionButtons[i].setFont(new Font(FONT_NAME, Font.PLAIN, 16));
            optionButtons[i].setOpaque(false);
            optionButtons[i].setBorderPainted(false);
            optionButtons[i].setContentAreaFilled(false);
            optionButtons[i].setFocusPainted(false);
            optionButtons[i].setMargin(new Insets(5, 15, 5, 15));
            optionButtons[i].setPreferredSize(new Dimension(200, 40));

            final int idx = i;
            optionButtons[i].addActionListener(e -> {
                // When a button is selected, store the corresponding letter
                String letter = String.valueOf((char) ('A' + idx));
                QuestionData q = questions.get(currentIndex);
                answers.put(q.id, letter);
                updateQuestionNavigationPanel();
                updateProgressDisplay();
            });
            optionGroup.add(optionButtons[i]);
            optionsPanel.add(optionButtons[i]);
        }

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setForeground(COLOR_ANSWERED);
        progressBar.setBackground(new Color(230, 230, 230));
        progressBar.setBorder(new EmptyBorder(5, 0, 5, 0));

        progressLabel = new JLabel();
        progressLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 14));
        progressLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setBackground(UIManager.getColor("Panel.background"));
        progressPanel.add(progressBar, BorderLayout.NORTH);
        progressPanel.add(progressLabel, BorderLayout.SOUTH);

        centerPanel.add(questionLabel, BorderLayout.NORTH);
        centerPanel.add(optionsPanel, BorderLayout.CENTER);
        centerPanel.add(progressPanel, BorderLayout.SOUTH);

        return centerPanel;
    }

    /** Creates the bottom navigation panel (Previous, Next, Submit, Exit). */
    private JPanel createNavigationPanel() {
        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        navPanel.setBackground(UIManager.getColor("Panel.background"));

        prevButton = new JButton("Previous");
        nextButton = new JButton("Next");
        submitButton = new JButton("Submit Exam");
        JButton exitButton = new JButton("Exit Exam");

        styleNavigationButton(prevButton, COLOR_CURRENT);
        styleNavigationButton(nextButton, COLOR_CURRENT);
        styleNavigationButton(submitButton, COLOR_UNANSWERED);
        styleNavigationButton(exitButton, new Color(120, 120, 120));

        prevButton.addActionListener(e -> showQuestion(currentIndex - 1));
        nextButton.addActionListener(e -> showQuestion(currentIndex + 1));
        submitButton.addActionListener(e -> confirmSubmission());
        exitButton.addActionListener(e -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (promptExamExitPassword(frame)) {
                disposeExamFrame(frame);
            } else {
                enforceFullScreen(frame);
            }
        });

        prevButton.setMnemonic(KeyEvent.VK_P);
        nextButton.setMnemonic(KeyEvent.VK_N);
        submitButton.setMnemonic(KeyEvent.VK_S);
        exitButton.setMnemonic(KeyEvent.VK_E);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(UIManager.getColor("Panel.background"));
        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);
        buttonPanel.add(submitButton);
        buttonPanel.add(exitButton);

        navPanel.add(buttonPanel, BorderLayout.CENTER);
        return navPanel;
    }

    /** Applies consistent styling to navigation buttons. */
    private void styleNavigationButton(JButton button, Color baseColor) {
        button.setFont(new Font(FONT_NAME, Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(baseColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(baseColor.darker(), 1),
            new EmptyBorder(8, 20, 8, 20)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(baseColor.brighter());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(baseColor);
            }
        });
    }

    /** Opens the exam in a new JFrame, attaches listeners for exit/minimize, and starts activity monitoring. */
    private void openExamFrame() {
        JFrame examFrame = new JFrame("Exam: " + (exam != null ? exam.getTitle() : ""));
        java.net.URL iconUrl = getClass().getResource("/Icons/appIcons.png");
        if (iconUrl != null) {
            examFrame.setIconImage(new ImageIcon(iconUrl).getImage());
        }
        examFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        examFrame.setUndecorated(true); // Make undecorated for true full-screen
        examFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        examFrame.setLocationRelativeTo(null);
        examFrame.setContentPane(this);
        examFrame.setAlwaysOnTop(true); // Try to keep on top
        examFrame.setVisible(true);

        // Window closing (attempt to close) -> prompt exit password
        examFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExitPasswordPrompt(examFrame);
            }
        });

        // Minimize/Restore -> prompt exit password
        examFrame.addWindowStateListener(e -> {
            int newState = e.getNewState();
            if ((newState & Frame.ICONIFIED) == Frame.ICONIFIED || newState == Frame.NORMAL) {
                handleExitPasswordPrompt(examFrame);
            }
        });

        // Window focus listener for activity monitoring
        examFrame.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(WindowEvent e) {
                logActivity("WINDOW_SWITCH", "Window lost focus (possible alt-tab or application switch).");
                SwingUtilities.invokeLater(() -> {
                    handleExitPasswordPrompt(examFrame);
                });
            }
        });
    }

    /** Handles the exit password prompt and enforces full-screen if incorrect. */
    private void handleExitPasswordPrompt(JFrame examFrame) {
        if (exitPromptActive) return;
        exitPromptActive = true;
        boolean ok = promptExamExitPassword(examFrame);
        if (ok) {
            disposeExamFrame(examFrame);
        } else {
            enforceFullScreen(examFrame);
        }
        exitPromptActive = false;
    }

    /** Forces the exam frame back to maximized and always-on-top for 3 seconds. */
    private void enforceFullScreen(JFrame frame) {
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setAlwaysOnTop(true);
        frame.toFront();
        frame.requestFocus();
        javax.swing.Timer t = new javax.swing.Timer(100, null);
        long start = System.currentTimeMillis();
        t.addActionListener(evt -> {
            if (System.currentTimeMillis() - start < 3000) {
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                frame.setAlwaysOnTop(true);
                frame.toFront();
                frame.requestFocus();
            } else {
                t.stop();
                frame.setAlwaysOnTop(false);
            }
        });
        t.setRepeats(true);
        t.start();
    }

    /** Submits the exam results using a background thread; wraps DELETE + INSERT in a transaction. */
    private void submitExamInBackground() {
        if (examSubmitted) return;
        examSubmitted = true;
        SwingUtilities.invokeLater(() -> {
            prevButton.setEnabled(false);
            nextButton.setEnabled(false);
            submitButton.setEnabled(false);
        });
        new Thread(() -> {
            final int[] correctCount = {0};
            for (QuestionData q : questions) {
                String ansLetter = answers.getOrDefault(q.id, "");
                if (!ansLetter.isEmpty() && ansLetter.equalsIgnoreCase(q.answer)) {
                    correctCount[0]++;
                }
            }
            String deleteSql = "DELETE FROM results WHERE exam_id = ? AND student_id = ?";
            String insertSql = "INSERT INTO results (student_id, exam_id, score, total_questions) VALUES (?, ?, ?, ?)";
            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false);
                try (PreparedStatement psDelete = conn.prepareStatement(deleteSql)) {
                    psDelete.setInt(1, exam.getId());
                    psDelete.setInt(2, studentUser.getId());
                    psDelete.executeUpdate();
                }
                try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                    psInsert.setInt(1, studentUser.getId());
                    psInsert.setInt(2, exam.getId());
                    psInsert.setInt(3, correctCount[0]);
                    psInsert.setInt(4, questions.size());
                    psInsert.executeUpdate();
                }
                conn.commit();
                SwingUtilities.invokeLater(() -> showResultDialog(correctCount[0], questions.size()));
            } catch (SQLException | IOException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                        ExamTakingPanel.this,
                        "An error occurred while submitting your exam: " + e.getMessage(),
                        "Submission Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                });
            }
        }).start();
    }

    /** Displays the question at the specified index, saving the previous answer. */
    private void showQuestion(int index) {
        if (index < 0 || index >= questions.size()) return;
        currentIndex = index;
        QuestionData q = questions.get(currentIndex);
        // Set question text (wrap in HTML for long text)
        String text = q.question;
        if (text.length() > 80) {
            questionLabel.setText("<html>" + text + "</html>");
        } else {
            questionLabel.setText(text);
        }
        // Set option texts
        optionButtons[0].setText(q.optionA);
        optionButtons[1].setText(q.optionB);
        optionButtons[2].setText(q.optionC);
        optionButtons[3].setText(q.optionD);
        // Clear selection, then re-select if previously answered
        optionGroup.clearSelection();
        String savedLetter = answers.get(q.id);
        if (savedLetter != null) {
            int letterIndex = savedLetter.charAt(0) - 'A';
            if (letterIndex >= 0 && letterIndex < 4) {
                optionButtons[letterIndex].setSelected(true);
            }
        }
        // Update progress bar and navigation buttons
        updateProgressDisplay();
        prevButton.setEnabled(currentIndex > 0);
        nextButton.setEnabled(currentIndex < questions.size() - 1);
        updateQuestionNavigationPanel();
    }

    /** Updates progress bar and label based on current index and answered count. */
    private void updateProgressDisplay() {
        int total = questions.size();
        int answeredCount = answers.size();
        progressBar.setMaximum(total);
        progressBar.setValue(currentIndex + 1);
        int percent = (int) (((currentIndex + 1) * 100.0) / total);
        progressBar.setString(percent + "% Complete");
        progressLabel.setText(String.format(
            "Question %d of %d (Answered: %d)",
            currentIndex + 1, total, answeredCount
        ));
    }

    /** Rebuilds the question navigation panel (GridLayout inside JScrollPane). */
    private void updateQuestionNavigationPanel() {
        questionNavigationInnerPanel.removeAll();
        for (int i = 0; i < questions.size(); i++) {
            JButton btn = new JButton(String.valueOf(i + 1));
            QuestionData q = questions.get(i);
            if (i == currentIndex) {
                btn.setBackground(COLOR_CURRENT);
            } else if (answers.containsKey(q.id)) {
                btn.setBackground(COLOR_ANSWERED);
            } else {
                btn.setBackground(COLOR_UNANSWERED);
            }
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font(FONT_NAME, Font.BOLD, 12));
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            btn.setPreferredSize(new Dimension(40, 30));
            final int idx = i;
            btn.addActionListener(e -> showQuestion(idx));
            questionNavigationInnerPanel.add(btn);
        }
        questionNavigationInnerPanel.revalidate();
        questionNavigationInnerPanel.repaint();
    }

    /** Displays the results dialog after submission. */
    private void showResultDialog(int correctCount, int totalQuestions) {
        JDialog resultDialog = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            "Exam Results",
            true
        );
        resultDialog.setSize(400, 250);
        resultDialog.setLocationRelativeTo(this);
        resultDialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(UIManager.getColor("Panel.background"));

        JLabel titleLabel = new JLabel("Exam Submitted Successfully!", SwingConstants.CENTER);
        titleLabel.setFont(new Font(FONT_NAME, Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        double percentage = (double) correctCount / totalQuestions * 100;
        String performance;
        Color performanceColor;

        if (percentage >= 80) {
            performance = "Excellent!";
            performanceColor = COLOR_ANSWERED;
        } else if (percentage >= 60) {
            performance = "Good!";
            performanceColor = COLOR_CURRENT;
        } else if (percentage >= 40) {
            performance = "Fair";
            performanceColor = new Color(241, 196, 15);
        } else {
            performance = "Needs Improvement";
            performanceColor = COLOR_UNANSWERED;
        }

        JLabel scoreLabel = new JLabel(
            String.format("Score: %d/%d (%.1f%%)", correctCount, totalQuestions, percentage),
            SwingConstants.CENTER
        );
        scoreLabel.setFont(new Font(FONT_NAME, Font.BOLD, 16));
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel performanceLabel = new JLabel(performance, SwingConstants.CENTER);
        performanceLabel.setFont(new Font(FONT_NAME, Font.BOLD, 16));
        performanceLabel.setForeground(performanceColor);
        performanceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton closeButton = new JButton("Close");
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeButton.addActionListener(e -> {
            resultDialog.dispose();
            if (parentPanel instanceof StudentDashboardPanel) {
                ((StudentDashboardPanel) parentPanel).showExamList();
            }
        });

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(scoreLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(performanceLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        contentPanel.add(closeButton);

        resultDialog.add(contentPanel, BorderLayout.CENTER);
        resultDialog.setVisible(true);
    }

    /** Starts the countdown timer for the exam. */
    private void startTimer() {
        if (examTimer != null && examTimer.isRunning()) return; // Prevent multiple timers
        timeLeftSeconds = exam.getDurationMinutes() * 60;
        updateTimerLabel();

        examTimer = new javax.swing.Timer(1000, e -> {
            timeLeftSeconds--;
            updateTimerLabel();

            if (timeLeftSeconds == 300) {
                logActivity("TIMER_WARNING", "5 minutes remaining.");
                JOptionPane.showMessageDialog(
                    this,
                    "Only 5 minutes remaining!",
                    "Time Warning",
                    JOptionPane.WARNING_MESSAGE
                );
            } else if (timeLeftSeconds == 60) {
                logActivity("TIMER_WARNING", "1 minute remaining.");
                JOptionPane.showMessageDialog(
                    this,
                    "Only 1 minute remaining!",
                    "Time Warning",
                    JOptionPane.WARNING_MESSAGE
                );
            }

            if (timeLeftSeconds <= 0) {
                examTimer.stop();
                logActivity("TIME_UP", "Exam time expired. Auto-submitting.");
                SwingUtilities.invokeLater(() -> {
                    Window window = SwingUtilities.getWindowAncestor(this);
                    if (window instanceof JFrame) {
                        ((JFrame) window).dispose();
                    }
                    if (parentPanel instanceof StudentDashboardPanel) {
                        ((StudentDashboardPanel) parentPanel).showExamList();
                    }
                });
                submitExamInBackground();
            }
        });
        examTimer.setRepeats(true);
        examTimer.start();
    }

    /** Updates the timer label (mm:ss) and changes color in last 5 minutes. */
    private void updateTimerLabel() {
        int minutes = timeLeftSeconds / 60;
        int seconds = timeLeftSeconds % 60;
        timerLabel.setText(String.format("Time left: %02d:%02d", minutes, seconds));

        if (timeLeftSeconds <= 300) {
            timerLabel.setForeground(COLOR_UNANSWERED);
        } else {
            timerLabel.setForeground(UIManager.getColor("Label.foreground"));
        }
    }

    /** Sets up global activity monitoring: mouse/keyboard inactivity and window focus. */
    private void setupActivityMonitoring() {
        // Track any mouse or key events to update lastActivityTimestamp
        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            if (event instanceof MouseEvent || event instanceof KeyEvent) {
                lastActivityTimestamp = System.currentTimeMillis();
            }
        }, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);

        // Timer to check for inactivity (2 minutes)
        new javax.swing.Timer(30_000, e -> {  // check every 30 seconds
            long idleMillis = System.currentTimeMillis() - lastActivityTimestamp;
            if (idleMillis > (2 * 60 * 1000L)) {
                logActivity("INACTIVITY", "No mouse/keyboard activity for over 2 minutes.");
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                        this,
                        "No activity detected for over 2 minutes. Please remain active in the exam window.",
                        "Inactivity Warning",
                        JOptionPane.WARNING_MESSAGE
                    );
                });
                lastActivityTimestamp = System.currentTimeMillis(); // reset to avoid repeated warnings
            }
        }).start();
    }

    /** Logs an activity event to the database (activity_log table). */
    private void logActivity(String eventType, String description) {
        String sql = "INSERT INTO activity_log (student_id, exam_id, event_time, event_type, description) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentUser.getId());
            ps.setInt(2, exam.getId());
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            ps.setString(4, eventType);
            ps.setString(5, description);
            ps.executeUpdate();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
    }

    /** Data class representing a question. */
    public static class QuestionData implements java.io.Serializable {
        public final int id;
        public final String question;
        public final String optionA;
        public final String optionB;
        public final String optionC;
        public final String optionD;
        public final String answer; // "A", "B", "C", or "D"

        public QuestionData(int id, String question, String optionA, String optionB,
                            String optionC, String optionD, String answer) {
            this.id = id;
            this.question = question;
            this.optionA = optionA;
            this.optionB = optionB;
            this.optionC = optionC;
            this.optionD = optionD;
            this.answer = answer;
        }
    }

    /** Custom JRadioButton with rounded rectangle and hover/selection rendering. */
    private static class RoundedOptionButton extends JRadioButton {
        private final int index;

        public RoundedOptionButton(int index) {
            this.index = index;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );

            if (getModel().isSelected() || getModel().isRollover()) {
                Color fill = getModel().isSelected() ? COLOR_CURRENT : new Color(230, 230, 230);
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(getModel().isSelected() ? Color.WHITE : Color.BLACK);
            } else {
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.BLACK);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            }

            String prefix = (char) ('A' + index) + ". ";
            FontMetrics fm = g2.getFontMetrics();
            int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(prefix + getText(), 15, textY);
            g2.dispose();
        }
    }

    /** Loads questions from a provided list (e.g., from LAN transfer) and updates the UI. */
    public void loadQuestionsFromTransfer(List<QuestionData> transferredQuestions) {
        if (transferredQuestions == null || transferredQuestions.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "No questions received from transfer.",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        questions.clear();
        questions.addAll(transferredQuestions);
        answers.clear();
        showQuestion(0);
        updateQuestionNavigationPanel();
        updateProgressDisplay();
        // Optionally, start timer if not already started
        if (examTimer == null) {
            startTimer();
        }
    }

    /** Loads questions from the database in a background SwingWorker thread. */
    private void loadQuestionsFromExamInBackground() {
        new SwingWorker<List<QuestionData>, Void>() {
            @Override
            protected List<QuestionData> doInBackground() {
                List<QuestionData> loaded = new ArrayList<>();
                String sql = "SELECT id, question_text, option_a, option_b, option_c, option_d, correct_option FROM questions WHERE exam_id = ?";
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, exam.getId());
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            loaded.add(new QuestionData(
                                rs.getInt("id"),
                                rs.getString("question_text"),
                                rs.getString("option_a"),
                                rs.getString("option_b"),
                                rs.getString("option_c"),
                                rs.getString("option_d"),
                                rs.getString("correct_option")
                            ));
                        }
                    }
                } catch (SQLException | IOException e) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(
                            ExamTakingPanel.this,
                            "Error loading questions: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                        );
                    });
                    e.printStackTrace();
                }
                return loaded;
            }
            @Override
            protected void done() {
                try {
                    List<QuestionData> loadedQuestions = get();
                    questions.clear();
                    questions.addAll(loadedQuestions);
                    if (questions.isEmpty()) {
                        JOptionPane.showMessageDialog(
                            ExamTakingPanel.this,
                            "No questions found for this exam.",
                            "No Questions",
                            JOptionPane.WARNING_MESSAGE
                        );
                        if (parentPanel instanceof StudentDashboardPanel) {
                            ((StudentDashboardPanel) parentPanel).showExamList();
                        }
                    } else {
                        showQuestion(0);
                        startTimer();
                        updateQuestionNavigationPanel();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    /** Prompts the student to confirm submission, warns about unanswered questions. */
    private void confirmSubmission() {
        int unanswered = questions.size() - answers.size();
        if (unanswered > 0) {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "You have " + unanswered + " unanswered questions. Are you sure you want to submit?",
                "Unanswered Questions",
                JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }
        submitExamInBackground();
    }

    /** Disposes the exam frame and returns to the dashboard. */
    private void disposeExamFrame(JFrame frame) {
        if (examTimer != null) {
            examTimer.stop();
        }
        frame.dispose();
        if (parentPanel instanceof StudentDashboardPanel) {
            ((StudentDashboardPanel) parentPanel).showExamList();
        }
    }
}
