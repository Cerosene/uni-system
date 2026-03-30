package pl.usos2.server.service.auth;

import pl.usos2.server.model.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AuthService {
    private final List<User> users = new ArrayList<>();

    public User register(User user) {
        validateUser(user);

        boolean emailExists = users.stream()
                .anyMatch(existing -> existing.getEmail().equalsIgnoreCase(user.getEmail()));

        if (emailExists) {
            throw new IllegalArgumentException("User with this email already exists.");
        }

        users.add(user);
        return user;
    }

    public User login(String email, String password) {
        Optional<User> userOptional = users.stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst();

        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        User user = userOptional.get();

        if (!user.isActive()) {
            throw new IllegalStateException("User account is inactive.");
        }

        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        return user;
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty.");
        }
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must have at least 6 characters.");
        }
    }
}