package ui;

import model.Question;

import javax.swing.*;
import java.awt.*;

public class QuestionFormDialog {
    public static Question showDialog(Component parent, Question existingQuestion) {
        JTextArea questionArea = new JTextArea(4, 30);
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        JScrollPane questionScroll = new JScrollPane(questionArea);

        JTextField aField = new JTextField(25);
        JTextField bField = new JTextField(25);
        JTextField cField = new JTextField(25);
        JTextField dField = new JTextField(25);

        JComboBox<String> correctCombo = new JComboBox<>(new String[] { "A", "B", "C", "D" });
        correctCombo.setPreferredSize(new Dimension(60, 30));

        if (existingQuestion != null) {
            questionArea.setText(existingQuestion.getQuestionText());
            aField.setText(existingQuestion.getOptionA());
            bField.setText(existingQuestion.getOptionB());
            cField.setText(existingQuestion.getOptionC());
            dField.setText(existingQuestion.getOptionD());
            correctCombo.setSelectedItem(existingQuestion.getCorrectOption());
        }

        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;
        contentPanel.add(new JLabel("Question:"), gbc);
        gbc.gridx = 1;
        contentPanel.add(questionScroll, gbc);

        gbc.gridx = 0; gbc.gridy++;
        contentPanel.add(new JLabel("Option A:"), gbc);
        gbc.gridx = 1;
        contentPanel.add(aField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        contentPanel.add(new JLabel("Option B:"), gbc);
        gbc.gridx = 1;
        contentPanel.add(bField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        contentPanel.add(new JLabel("Option C:"), gbc);
        gbc.gridx = 1;
        contentPanel.add(cField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        contentPanel.add(new JLabel("Option D:"), gbc);
        gbc.gridx = 1;
        contentPanel.add(dField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        contentPanel.add(new JLabel("Correct Option:"), gbc);
        gbc.gridx = 1;
        contentPanel.add(correctCombo, gbc);

        int result = JOptionPane.showConfirmDialog(parent, contentPanel, existingQuestion == null ? "Add Question" : "Edit Question",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            Question q = existingQuestion == null ? new Question() : existingQuestion;
            q.setQuestionText(questionArea.getText().trim());
            q.setOptionA(aField.getText().trim());
            q.setOptionB(bField.getText().trim());
            q.setOptionC(cField.getText().trim());
            q.setOptionD(dField.getText().trim());
            q.setCorrectOption((String) correctCombo.getSelectedItem());
            return q;
        }
        return null;
    }
}
