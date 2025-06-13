package model;

import java.io.Serializable;
import java.util.ArrayList;

public class ExamTransfer implements Serializable {
    private static final long serialVersionUID = 1L;
    public Exam exam;
    public ArrayList<ui.ExamTakingPanel.QuestionData> questions;

    public ExamTransfer(Exam exam, ArrayList<ui.ExamTakingPanel.QuestionData> questions) {
        this.exam = exam;
        this.questions = questions;
    }
}
