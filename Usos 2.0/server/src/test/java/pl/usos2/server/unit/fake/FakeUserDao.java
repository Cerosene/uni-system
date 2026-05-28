package pl.usos2.server.unit.fake;

import pl.usos2.server.dao.user.UserDao;
import pl.usos2.server.model.enumtype.UserRole;
import pl.usos2.server.model.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FakeUserDao implements UserDao {
    private final Map<Long, User> users = new ConcurrentHashMap<>();

    @Override
    public User save(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateBasicData(Long userId, String firstName, String lastName, String email) {
        User user = findExisting(userId);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        return user;
    }

    @Override
    public User updatePassword(Long userId, String newPassword) {
        User user = findExisting(userId);
        user.setPassword(newPassword);
        return user;
    }

    @Override
    public User updateActive(Long userId, boolean active) {
        User user = findExisting(userId);
        user.setActive(active);
        return user;
    }

    @Override
    public User updateRole(Long userId, UserRole role) {
        User user = findExisting(userId);
        user.setRole(role);
        return user;
    }

    @Override
    public void deleteById(Long userId) {
        users.remove(userId);
    }

    @Override
    public Optional<User> findById(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return users.values().stream()
                .filter(user -> user.getEmail() != null && user.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    @Override
    public List<User> findByRole(UserRole role) {
        return users.values().stream()
                .filter(user -> user.getRole() == role)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public boolean existsById(Long userId) {
        return users.containsKey(userId);
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    @Override
    public boolean existsByEmailExcludingId(String email, Long excludedUserId) {
        return users.values().stream()
                .anyMatch(user -> !user.getId().equals(excludedUserId)
                        && user.getEmail() != null
                        && user.getEmail().equalsIgnoreCase(email));
    }

    private User findExisting(Long userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }
        return user;
    }
}
