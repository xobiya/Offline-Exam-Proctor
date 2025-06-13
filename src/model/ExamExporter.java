package model;

import org.json.JSONArray;
import org.json.JSONObject;
import java.sql.*;

public class ExamExporter {
    public static JSONObject getExamAsJson(int examId, Connection conn) throws Exception {
        JSONObject examJson = new JSONObject();

        // Get exam info
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM exams WHERE id=?");
        ps.setInt(1, examId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            examJson.put("id", rs.getInt("id"));
            examJson.put("title", rs.getString("title"));
            examJson.put("description", rs.getString("description"));
            examJson.put("start_time", rs.getString("start_time"));
            examJson.put("end_time", rs.getString("end_time"));
            examJson.put("duration_minutes", rs.getInt("duration_minutes"));
            examJson.put("entry_password", rs.getString("entry_password"));
            examJson.put("exit_password", rs.getString("exit_password"));
        }

        // Get questions
        JSONArray questionsArr = new JSONArray();
        ps = conn.prepareStatement("SELECT * FROM questions WHERE exam_id=?");
        ps.setInt(1, examId);
        rs = ps.executeQuery();
        while (rs.next()) {
            JSONObject q = new JSONObject();
            q.put("id", rs.getInt("id"));
            q.put("question_text", rs.getString("question_text"));
            q.put("option_a", rs.getString("option_a"));
            q.put("option_b", rs.getString("option_b"));
            q.put("option_c", rs.getString("option_c"));
            q.put("option_d", rs.getString("option_d"));
            q.put("correct_option", rs.getString("correct_option"));
            questionsArr.put(q);
        }
        examJson.put("questions", questionsArr);

        return examJson;
    }
}
