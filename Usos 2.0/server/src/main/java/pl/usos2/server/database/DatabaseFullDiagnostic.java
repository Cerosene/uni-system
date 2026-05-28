package pl.usos2.server.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DatabaseFullDiagnostic {

    private DatabaseFullDiagnostic() {
    }

    public static void main(String[] args) {
        Map<String, String> tableQueries = new LinkedHashMap<>();
        tableQueries.put("users", "SELECT COUNT(*) FROM users");
        tableQueries.put("students", "SELECT COUNT(*) FROM students");
        tableQueries.put("lecturers", "SELECT COUNT(*) FROM lecturers");
        tableQueries.put("employees", "SELECT COUNT(*) FROM employees");
        tableQueries.put("subjects", "SELECT COUNT(*) FROM subjects");
        tableQueries.put("course_groups", "SELECT COUNT(*) FROM course_groups");
        tableQueries.put("enrollments", "SELECT COUNT(*) FROM enrollments");
        tableQueries.put("grades", "SELECT COUNT(*) FROM grades");
        tableQueries.put("messages", "SELECT COUNT(*) FROM messages");
        tableQueries.put("applications", "SELECT COUNT(*) FROM applications");
        tableQueries.put("payments", "SELECT COUNT(*) FROM payments");
        tableQueries.put("service_tickets", "SELECT COUNT(*) FROM service_tickets");
        tableQueries.put("resources", "SELECT COUNT(*) FROM resources");
        tableQueries.put("loans", "SELECT COUNT(*) FROM loans");
        tableQueries.put("audit_logs", "SELECT COUNT(*) FROM audit_logs");

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            try (ResultSet ping = statement.executeQuery("SELECT 1 FROM dual")) {
                if (ping.next()) {
                    System.out.println("[DIAGNOSTIC] DB ping OK: " + ping.getInt(1));
                }
            }

            for (Map.Entry<String, String> entry : tableQueries.entrySet()) {
                try (ResultSet resultSet = statement.executeQuery(entry.getValue())) {
                    long count = resultSet.next() ? resultSet.getLong(1) : 0L;
                    System.out.println("[DIAGNOSTIC] table=" + entry.getKey() + " count=" + count);
                }
            }

            System.out.println("[DIAGNOSTIC] Full database diagnostic finished successfully.");
        } catch (Exception exception) {
            System.err.println("[DIAGNOSTIC] Full database diagnostic failed: " + exception.getMessage());
            exception.printStackTrace(System.err);
        }
    }
}
