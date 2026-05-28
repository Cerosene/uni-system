package pl.usos2.server.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public final class DatabaseConnectionDiagnostic {

    private DatabaseConnectionDiagnostic() {
    }

    public static void main(String[] args) {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            try (ResultSet ping = statement.executeQuery("SELECT 1 FROM dual")) {
                if (ping.next()) {
                    System.out.println("DB ping OK: " + ping.getInt(1));
                }
            }

            try (ResultSet users = statement.executeQuery("SELECT COUNT(*) FROM users")) {
                if (users.next()) {
                    System.out.println("Users count: " + users.getLong(1));
                }
            }

            System.out.println("Database diagnostic finished successfully.");
        } catch (Exception exception) {
            System.err.println("Database diagnostic failed: " + exception.getMessage());
            exception.printStackTrace(System.err);
        }
    }
}

