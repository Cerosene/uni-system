package pl.usos2.server.service.auth;

import pl.usos2.server.dao.user.JdbcUserDao;
import pl.usos2.server.dao.user.UserDao;
import pl.usos2.server.model.enumtype.UserRole;
import pl.usos2.server.model.user.User;
import pl.usos2.server.service.audit.AuditLogService;
import pl.usos2.server.util.ValidationUtils;

import java.util.List;
import java.util.logging.Logger;

public class AuthService {
    private static final Logger logger = Logger.getLogger(AuthService.class.getName());

    private final UserDao userDao;
    private final AuditLogService auditLogService;

    public AuthService() {
        this(new JdbcUserDao(), AuditLogService.getInstance());
    }

    public AuthService(UserDao userDao) {
        this(userDao, AuditLogService.getInstance());
    }

    public AuthService(UserDao userDao, AuditLogService auditLogService) {
        this.userDao = userDao;
        this.auditLogService = auditLogService;
    }

    public User register(User user) {
        validateUser(user);

        if (userDao.existsById(user.getId())) {
            logger.warning("Registration failed. User id already exists: " + user.getId());
            throw new IllegalArgumentException("User with this id already exists.");
        }

        String normalizedEmail = ValidationUtils.normalizeEmail(user.getEmail());
        if (userDao.existsByEmail(normalizedEmail)) {
            logger.warning("Registration failed. Email already exists: " + normalizedEmail);
            throw new IllegalArgumentException("User with this email already exists.");
        }

        user.setFirstName(user.getFirstName().trim());
        user.setLastName(user.getLastName().trim());
        user.setEmail(normalizedEmail);

        User saved = userDao.save(user);
        logger.info("Registered new user: " + saved.getEmail());
        logger.info("[DIAGNOSTIC] User persisted in Oracle during register. userId=" + saved.getId());
        auditSafely(saved.getId(), "USER_CREATED", "USERS", saved.getId(),
                "User registered with email=" + saved.getEmail());
        return saved;
    }

    public User login(String email, String password) {
        String normalizedEmail = ValidationUtils.normalizeEmail(email);
        ValidationUtils.requireMinLength(password, 6, "Password must have at least 6 characters.");

        User user = userDao.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));
        logger.info("[DIAGNOSTIC] Login lookup loaded user from Oracle. email=" + normalizedEmail);

        if (!user.isActive()) {
            logger.warning("Login failed. Inactive account: " + normalizedEmail);
            throw new IllegalStateException("User account is inactive.");
        }

        if (!user.getPassword().equals(password)) {
            logger.warning("Login failed. Wrong password for: " + normalizedEmail);
            throw new IllegalArgumentException("Invalid email or password.");
        }

        logger.info("User logged in successfully: " + normalizedEmail);
        auditSafely(user.getId(), "LOGIN", "USERS", user.getId(), "Successful login for email=" + normalizedEmail);
        return user;
    }

    public void logout(Long userId) {
        User user = findById(userId);
        logger.info("User logged out: " + user.getEmail());
        auditSafely(user.getId(), "LOGOUT", "USERS", user.getId(), "User logged out. email=" + user.getEmail());
    }

    public User updateBasicData(Long userId, String firstName, String lastName) {
        User existing = findById(userId);

        User updated = userDao.updateBasicData(
                userId,
                ValidationUtils.normalizeText(firstName, "First name cannot be empty."),
                ValidationUtils.normalizeText(lastName, "Last name cannot be empty."),
                existing.getEmail()
        );
        logger.info("Updated user basic data. id=" + userId);
        logger.info("[DIAGNOSTIC] User basic data updated in Oracle. userId=" + userId);
        auditSafely(userId, "USER_UPDATED", "USERS", userId,
                "Updated firstName/lastName for user id=" + userId);
        return updated;
    }

    public User changeEmail(Long userId, String newEmail) {
        User existing = findById(userId);
        String normalizedEmail = ValidationUtils.normalizeEmail(newEmail);

        boolean emailTaken = userDao.existsByEmailExcludingId(normalizedEmail, userId);
        if (emailTaken) {
            logger.warning("Email change failed. Email already used: " + normalizedEmail);
            throw new IllegalArgumentException("User with this email already exists.");
        }

        User updated = userDao.updateBasicData(
                userId,
                existing.getFirstName(),
                existing.getLastName(),
                normalizedEmail
        );
        logger.info("Changed user email. id=" + userId + ", newEmail=" + normalizedEmail);
        logger.info("[DIAGNOSTIC] User email updated in Oracle. userId=" + userId);
        auditSafely(userId, "USER_UPDATED", "USERS", userId,
                "Updated email to " + normalizedEmail);
        return updated;
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

        User updated = userDao.updatePassword(userId, newPassword.trim());
        logger.info("Changed password for user id=" + userId);
        logger.info("[DIAGNOSTIC] User password updated in Oracle. userId=" + userId);
        return updated;
    }

    public User activateUser(Long userId) {
        User updated = userDao.updateActive(userId, true);
        logger.info("Activated user id=" + userId);
        logger.info("[DIAGNOSTIC] User activation saved in Oracle. userId=" + userId);
        return updated;
    }

    public User deactivateUser(Long userId) {
        User updated = userDao.updateActive(userId, false);
        logger.info("Deactivated user id=" + userId);
        logger.info("[DIAGNOSTIC] User deactivation saved in Oracle. userId=" + userId);
        return updated;
    }

    public void deleteUser(Long userId) {
        ValidationUtils.requireNotNull(userId, "User id cannot be null.");
        User existing = findById(userId);
        userDao.deleteById(userId);
        logger.info("Deleted user id=" + userId);
        logger.info("[DIAGNOSTIC] User deleted in Oracle. userId=" + userId);
        auditSafely(userId, "USER_DELETED", "USERS", userId,
                "User deleted. email=" + existing.getEmail());
    }

    public User findById(Long userId) {
        ValidationUtils.requireNotNull(userId, "User id cannot be null.");

        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        logger.info("[DIAGNOSTIC] User loaded by id from Oracle. userId=" + userId);
        return user;
    }

    public User findByEmail(String email) {
        String normalizedEmail = ValidationUtils.normalizeEmail(email);

        User user = userDao.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        logger.info("[DIAGNOSTIC] User loaded by email from Oracle. email=" + normalizedEmail);
        return user;
    }

    public List<User> getUsersByRole(UserRole role) {
        ValidationUtils.requireNotNull(role, "User role cannot be null.");

        List<User> users = userDao.findByRole(role);
        logger.info("[DIAGNOSTIC] Users loaded by role from Oracle. role=" + role + ", count=" + users.size());
        return users;
    }

    public List<User> getAllUsers() {
        List<User> users = userDao.findAll();
        logger.info("[DIAGNOSTIC] All users loaded from Oracle. count=" + users.size());
        return users;
    }

    public User changeRole(Long userId, UserRole role) {
        ValidationUtils.requireNotNull(userId, "User id cannot be null.");
        ValidationUtils.requireNotNull(role, "User role cannot be null.");

        User updated = userDao.updateRole(userId, role);
        logger.info("Changed role for user id=" + userId + " to " + role);
        logger.info("[DIAGNOSTIC] User role updated in Oracle. userId=" + userId);
        auditSafely(userId, "USER_UPDATED", "USERS", userId, "Changed role to " + role);
        return updated;
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

    private void auditSafely(Long userId, String actionName, String entityName, Long entityId, String details) {
        try {
            auditLogService.logEvent(userId, actionName, entityName, entityId, details);
        } catch (RuntimeException exception) {
            logger.warning("Audit log write failed: " + exception.getMessage());
        }
    }
}
