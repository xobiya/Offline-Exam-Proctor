
package db;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;
import javax.swing.JOptionPane;

public class DBConnection {
    private static Connection connection;

    public static Connection getConnection() throws SQLException, IOException {
        if (connection == null || connection.isClosed()) {
            Properties props = new Properties();
            FileInputStream fis = new FileInputStream("db_config.properties");
            props.load(fis);
            String url = props.getProperty("DB_URL");
            String user = props.getProperty("DB_USER");
            String password = props.getProperty("DB_PASSWORD");
            connection = DriverManager.getConnection(url, user, password);
        }
        return connection;
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection()) {
            // Create admin user if not exists
           PreparedStatement stmt = conn.prepareStatement(
    "INSERT IGNORE INTO users (username, password_hash, full_name, role, email) VALUES (?, ?, ?, ?, ?)");
stmt.setString(1, "admin01");
stmt.setString(2, util.PasswordUtils.hashPassword("admin123")); // Hashes the plain text password
stmt.setString(3, "Feleke Eshetu");
stmt.setString(4, "admin");
stmt.setString(5, "admin01@example.com");
stmt.executeUpdate();
            
            // Create sample exam if none exists
            stmt = conn.prepareStatement(
                "INSERT IGNORE INTO exams (title, duration_minutes) VALUES (?, ?)");
            stmt.setString(1, "Java Fundamentals");
            stmt.setInt(2, 30);
            stmt.executeUpdate();
        } catch (SQLException | IOException e) {
            showDatabaseError(e);
        }
    }

    private static void showDatabaseError(Exception e) {
        JOptionPane.showMessageDialog(null,
            "Database Error: " + e.getMessage(),
            "Connection Failed",
            JOptionPane.ERROR_MESSAGE);
    }
}