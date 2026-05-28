package pl.usos2.server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {

    private static final String DEFAULT_URL = "jdbc:oracle:thin:@//localhost:1521/FREEPDB1";
    private static final String DEFAULT_USER = "USOS";
    private static final String DEFAULT_PASSWORD = "usos123";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        String url = resolve("USOS_DB_URL", "usos.db.url", DEFAULT_URL);
        String user = resolve("USOS_DB_USER", "usos.db.user", DEFAULT_USER);
        String password = resolve("USOS_DB_PASSWORD", "usos.db.password", DEFAULT_PASSWORD);

        return DriverManager.getConnection(url, user, password);
    }

    private static String resolve(String envName, String propertyName, String defaultValue) {
        String envValue = System.getenv(envName);
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }

        String propertyValue = System.getProperty(propertyName);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue.trim();
        }

        return defaultValue;
    }
}

