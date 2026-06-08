package pl.usos2.server.unit.network;

import org.junit.jupiter.api.Test;
import pl.usos2.server.model.enumtype.Semester;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.network.session.AuthSession;
import pl.usos2.server.network.session.SessionManager;

import static org.junit.jupiter.api.Assertions.*;

class SessionManagerTest {
    @Test
    void createSessionStoresUserAndInvalidateRemovesIt() {
        SessionManager manager = new SessionManager();
        Student student = new Student(1001L, "Mateusz", "Lewandowski", "mateusz@uni.pl", "password123", "320101", "Informatyka", Semester.THIRD);

        AuthSession session = manager.createSession(student);

        assertNotNull(session.getToken());
        assertEquals(student.getId(), manager.requireUser(session.getToken()).getId());
        assertEquals(1, manager.activeSessionCount());

        manager.invalidate(session.getToken());

        assertTrue(manager.findUser(session.getToken()).isEmpty());
        assertEquals(0, manager.activeSessionCount());
    }
}
