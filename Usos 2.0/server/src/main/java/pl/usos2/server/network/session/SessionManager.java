package pl.usos2.server.network.session;

import pl.usos2.server.model.user.User;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SessionManager {
    private final ConcurrentMap<String, User> sessions = new ConcurrentHashMap<>();

    public AuthSession createSession(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("Cannot create session for empty user.");
        }
        String token = UUID.randomUUID().toString();
        sessions.put(token, user);
        return new AuthSession(token, user);
    }

    public Optional<User> findUser(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(sessions.get(token));
    }

    public User requireUser(String token) {
        return findUser(token).orElseThrow(() -> new SecurityException("User session is missing or expired."));
    }

    public void invalidate(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }

    public int activeSessionCount() {
        return sessions.size();
    }
}
