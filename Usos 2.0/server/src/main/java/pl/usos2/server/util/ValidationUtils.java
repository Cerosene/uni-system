package pl.usos2.server.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public final class ValidationUtils {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private ValidationUtils() {
    }

    public static void requireNotNull(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void requireMinLength(String value, int minLength, String message) {
        requireNotBlank(value, message);
        if (value.trim().length() < minLength) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void requireValidEmail(String email, String message) {
        requireNotBlank(email, message);
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void requirePositive(BigDecimal value, String message) {
        requireNotNull(value, message);
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    public static String normalizeEmail(String email) {
        requireValidEmail(email, "Email has invalid format.");
        return email.trim().toLowerCase();
    }

    public static String normalizeText(String value, String message) {
        requireNotBlank(value, message);
        return value.trim();
    }
}