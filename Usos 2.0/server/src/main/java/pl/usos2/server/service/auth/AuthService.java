package pl.usos2.server.service.auth;

import pl.usos2.server.model.enumtype.UserRole;
import pl.usos2.server.model.user.User;
import pl.usos2.server.util.ValidationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class AuthService {
    private static final Logger logger = Logger.getLogger(AuthService.class.getName());

    private final List<User> users = new ArrayList<>();

    public User register(User user) {
        validateUser(user);

        if (findOptionalById(user.getId()).isPresent()) {
            logger.warning("Registration failed. User id already exists: " + user.getId());
            throw new IllegalArgumentException("User with this id already exists.");
        }

        String normalizedEmail = ValidationUtils.normalizeEmail(user.getEmail());

        boolean emailExists = users.stream()
                .anyMatch(existing -> existing.getEmail().equalsIgnoreCase(normalizedEmail));

        if (emailExists) {
            logger.warning("Registration failed. Email already exists: " + normalizedEmail);
            throw new IllegalArgumentException("User with this email already exists.");
        }

        user.setFirstName(user.getFirstName().trim());
        user.setLastName(user.getLastName().trim());
        user.setEmail(normalizedEmail);

        users.add(user);

        logger.info("Registered new user: " + user.getEmail());
        return user;
    }

    public User login(String email, String password) {
        String normalizedEmail = ValidationUtils.normalizeEmail(email);
        ValidationUtils.requireMinLength(password, 6, "Password must have at least 6 characters.");

        User user = findByEmail(normalizedEmail);

        if (!user.isActive()) {
            logger.warning("Login failed. Inactive account: " + normalizedEmail);
            throw new IllegalStateException("User account is inactive.");
        }

        if (!user.getPassword().equals(password)) {
            logger.warning("Login failed. Wrong password for: " + normalizedEmail);
            throw new IllegalArgumentException("Invalid email or password.");
        }

        logger.info("User logged in successfully: " + normalizedEmail);
        return user;
    }

    public void logout(Long userId) {
        User user = findById(userId);
        logger.info("User logged out: " + user.getEmail());
    }

    public User updateBasicData(Long userId, String firstName, String lastName) {
        User user = findById(userId);

        user.setFirstName(ValidationUtils.normalizeText(firstName, "First name cannot be empty."));
        user.setLastName(ValidationUtils.normalizeText(lastName, "Last name cannot be empty."));

        logger.info("Updated user basic data. id=" + userId);
        return user;
    }

    public User changeEmail(Long userId, String newEmail) {
        User user = findById(userId);
        String normalizedEmail = ValidationUtils.normalizeEmail(newEmail);

        boolean emailTaken = users.stream()
                .filter(existing -> !existing.getId().equals(userId))
                .anyMatch(existing -> existing.getEmail().equalsIgnoreCase(normalizedEmail));

        if (emailTaken) {
            logger.warning("Email change failed. Email already used: " + normalizedEmail);
            throw new IllegalArgumentException("User with this email already exists.");
        }

        user.setEmail(normalizedEmail);
        logger.info("Changed user email. id=" + userId + ", newEmail=" + normalizedEmail);
        return user;
    }

    public User changePassword(Long userId, String currentPassword, String newPassword) {
        User user = findById(userId);

        ValidationUtils.requireMinLength(currentPassword, 6, "Current password must have at least 6 characters.");
        ValidationUtils.requireMinLength(newPassword, 6, "New password must have at least 6 characters.");

        if (!user.getPassword().equals(currentPassword)) {
            logger.warning("Password change failed. Wrong current password for id=" + userId);
            throw new IllegalArgumentException("Current password is incorrect.");
        }

        if (currentPassword.equals(newPassword)) {
            logger.warning("Password change failed. New password matches old password for id=" + userId);
            throw new IllegalArgumentException("New password must be different from current password.");
        }

        user.setPassword(newPassword.trim());
        logger.info("Changed password for user id=" + userId);
        return user;
    }

    public User activateUser(Long userId) {
        User user = findById(userId);
        user.setActive(true);
        logger.info("Activated user id=" + userId);
        return user;
    }

    public User deactivateUser(Long userId) {
        User user = findById(userId);
        user.setActive(false);
        logger.info("Deactivated user id=" + userId);
        return user;
    }

    public User findById(Long userId) {
        ValidationUtils.requireNotNull(userId, "User id cannot be null.");

        return findOptionalById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    public User findByEmail(String email) {
        String normalizedEmail = ValidationUtils.normalizeEmail(email);

        return users.stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(normalizedEmail))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    public List<User> getUsersByRole(UserRole role) {
        ValidationUtils.requireNotNull(role, "User role cannot be null.");

        return users.stream()
                .filter(user -> user.getRole() == role)
                .toList();
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    private Optional<User> findOptionalById(Long userId) {
        return users.stream()
                .filter(user -> userId.equals(user.getId()))
                .findFirst();
    }

    private void validateUser(User user) {
        ValidationUtils.requireNotNull(user, "User cannot be null.");
        ValidationUtils.requireNotNull(user.getId(), "User id cannot be null.");
        ValidationUtils.requireNotBlank(user.getFirstName(), "First name cannot be empty.");
        ValidationUtils.requireNotBlank(user.getLastName(), "Last name cannot be empty.");
        ValidationUtils.requireValidEmail(user.getEmail(), "Email has invalid format.");
        ValidationUtils.requireMinLength(user.getPassword(), 6, "Password must have at least 6 characters.");
        ValidationUtils.requireNotNull(user.getRole(), "User role cannot be null.");
    }
}