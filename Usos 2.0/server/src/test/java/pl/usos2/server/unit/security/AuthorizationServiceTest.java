package pl.usos2.server.unit.security;

import org.junit.jupiter.api.Test;
import pl.usos2.server.model.enumtype.Semester;
import pl.usos2.server.model.enumtype.UserRole;
import pl.usos2.server.model.user.Administrator;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.security.AuthorizationService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthorizationServiceTest {
    @Test
    void requireRoleAllowsMatchingRole() {
        Administrator admin = new Administrator(3L, "Anna", "Zielinska", "admin@uni.pl", "password123", "ADM001");
        assertDoesNotThrow(() -> AuthorizationService.requireRole(admin, UserRole.ADMINISTRATOR));
    }

    @Test
    void requireRoleRejectsWrongRole() {
        Student student = new Student(1001L, "Mateusz", "Lewandowski", "mateusz@uni.pl", "password123", "320101", "Informatyka", Semester.THIRD);
        assertThrows(SecurityException.class, () -> AuthorizationService.requireRole(student, UserRole.ADMINISTRATOR));
    }

    @Test
    void requireSelfOrAnyRoleAllowsOwnerWithoutAdminRole() {
        Student student = new Student(1001L, "Mateusz", "Lewandowski", "mateusz@uni.pl", "password123", "320101", "Informatyka", Semester.THIRD);
        assertDoesNotThrow(() -> AuthorizationService.requireSelfOrAnyRole(student, 1001L, UserRole.ADMINISTRATOR));
    }
}
