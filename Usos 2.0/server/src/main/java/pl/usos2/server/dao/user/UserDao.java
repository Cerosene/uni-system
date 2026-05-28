package pl.usos2.server.dao.user;

import pl.usos2.server.model.enumtype.UserRole;
import pl.usos2.server.model.user.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {
    User save(User user);

    User updateBasicData(Long userId, String firstName, String lastName, String email);

    User updatePassword(Long userId, String newPassword);

    User updateActive(Long userId, boolean active);

    User updateRole(Long userId, UserRole role);

    void deleteById(Long userId);

    Optional<User> findById(Long userId);

    Optional<User> findByEmail(String email);

    List<User> findByRole(UserRole role);

    List<User> findAll();

    boolean existsById(Long userId);

    boolean existsByEmail(String email);

    boolean existsByEmailExcludingId(String email, Long excludedUserId);
}
