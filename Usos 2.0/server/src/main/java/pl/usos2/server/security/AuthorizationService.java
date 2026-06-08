package pl.usos2.server.security;

import pl.usos2.server.model.enumtype.UserRole;
import pl.usos2.server.model.user.User;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public final class AuthorizationService {
    private AuthorizationService() {
    }

    public static void requireLoggedIn(User user) {
        if (user == null || user.getId() == null) {
            throw new SecurityException("User must be logged in.");
        }
    }

    public static void requireRole(User user, UserRole requiredRole) {
        requireLoggedIn(user);
        if (user.getRole() != requiredRole) {
            throw new SecurityException("Operation requires role: " + requiredRole + ".");
        }
    }

    public static void requireAnyRole(User user, UserRole... allowedRoles) {
        requireLoggedIn(user);
        Set<UserRole> allowed = Arrays.stream(allowedRoles).collect(Collectors.toSet());
        if (!allowed.contains(user.getRole())) {
            throw new SecurityException("Operation requires one of roles: " + allowed + ".");
        }
    }

    public static void requireSelfOrAnyRole(User user, Long targetUserId, UserRole... allowedRoles) {
        requireLoggedIn(user);
        if (targetUserId != null && targetUserId.equals(user.getId())) {
            return;
        }
        requireAnyRole(user, allowedRoles);
    }
}
