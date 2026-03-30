package pl.usos2.server.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.usos2.server.model.enumtype.Semester;
import pl.usos2.server.model.enumtype.ServiceTicketStatus;
import pl.usos2.server.model.service.ServiceTicket;
import pl.usos2.server.model.user.Administrator;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.service.maintenance.ServiceTicketService;

import static org.junit.jupiter.api.Assertions.*;

class ServiceTicketServiceTest {

    private ServiceTicketService serviceTicketService;
    private Student student;
    private Administrator administrator;

    @BeforeEach
    void setUp() {
        serviceTicketService = new ServiceTicketService();
        student = new Student(
                1L, "Kasia", "Nowak", "kasia@test.pl", "haslo123", "s22222", "Informatyka", Semester.FOURTH
        );
        administrator = new Administrator(
                2L, "Maria", "Admin", "admin@test.pl", "admin123", "a001"
        );
    }

    @Test
    void shouldCreateOpenTicket() {
        ServiceTicket ticket = serviceTicketService.createTicket(student, "Problem z kontem", "Nie mogę się zalogować.");

        assertNotNull(ticket);
        assertEquals(ServiceTicketStatus.OPEN, ticket.getStatus());
    }

    @Test
    void shouldAssignTicketAndChangeStatusToInProgress() {
        ServiceTicket ticket = serviceTicketService.createTicket(student, "Problem", "Opis problemu.");
        serviceTicketService.assignTicket(ticket, administrator);

        assertEquals(ServiceTicketStatus.IN_PROGRESS, ticket.getStatus());
        assertEquals(administrator, ticket.getAssignedTo());
    }

    @Test
    void shouldCloseTicket() {
        ServiceTicket ticket = serviceTicketService.createTicket(student, "Problem", "Opis problemu.");
        serviceTicketService.closeTicket(ticket);

        assertEquals(ServiceTicketStatus.CLOSED, ticket.getStatus());
    }
}