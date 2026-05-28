package pl.usos2.server.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.usos2.server.model.enumtype.Semester;
import pl.usos2.server.model.enumtype.ServiceTicketStatus;
import pl.usos2.server.model.service.ServiceTicket;
import pl.usos2.server.model.user.Administrator;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.service.audit.AuditLogService;
import pl.usos2.server.service.maintenance.ServiceTicketService;
import pl.usos2.server.unit.fake.FakeAuditLogDao;
import pl.usos2.server.unit.fake.FakeServiceTicketDao;

import static org.junit.jupiter.api.Assertions.*;

class ServiceTicketServiceTest {

    private ServiceTicketService serviceTicketService;
    private Student student;
    private Administrator administrator;
    private Administrator secondAdministrator;

    @BeforeEach
    void setUp() {
        serviceTicketService = new ServiceTicketService(new FakeServiceTicketDao(), new AuditLogService(new FakeAuditLogDao()));
        student = new Student(
                1L, "Kasia", "Nowak", "kasia@test.pl", "haslo123", "s22222", "Informatyka", Semester.FOURTH
        );
        administrator = new Administrator(
                2L, "Maria", "Admin", "admin@test.pl", "admin123", "A001"
        );
        secondAdministrator = new Administrator(
                3L, "Jan", "Admin", "admin2@test.pl", "admin456", "A002"
        );
    }

    @Test
    @DisplayName("Powinno utworzyć zgłoszenie z wygenerowanym id")
    void shouldCreateTicketWithGeneratedId() {
        System.out.println("Test: tworzenie zgłoszenia z generowanym id");

        ServiceTicket ticket = serviceTicketService.createTicket(student, "Problem z kontem", "Nie mogę się zalogować.");

        assertNotNull(ticket.getId());
        assertEquals(ServiceTicketStatus.OPEN, ticket.getStatus());
    }

    @Test
    @DisplayName("Powinno przypisać zgłoszenie administratorowi")
    void shouldAssignTicket() {
        System.out.println("Test: przypisanie zgłoszenia administratorowi");

        ServiceTicket ticket = serviceTicketService.createTicket(student, "Problem", "Opis problemu.");
        serviceTicketService.assignTicket(ticket, administrator);

        assertEquals(ServiceTicketStatus.IN_PROGRESS, ticket.getStatus());
        assertEquals(administrator, ticket.getAssignedTo());
    }

    @Test
    @DisplayName("Powinno zablokować przypisanie zamkniętego zgłoszenia")
    void shouldThrowExceptionWhenAssigningClosedTicket() {
        System.out.println("Test: blokada przypisania zamkniętego zgłoszenia");

        ServiceTicket ticket = serviceTicketService.createTicket(student, "Problem", "Opis problemu.");
        serviceTicketService.closeTicket(ticket);

        assertThrows(IllegalStateException.class,
                () -> serviceTicketService.assignTicket(ticket, administrator));
    }

    @Test
    @DisplayName("Powinno zablokować przypisanie zgłoszenia do innego administratora gdy już jest przypisane")
    void shouldThrowExceptionWhenTicketAlreadyAssignedToAnotherAdministrator() {
        System.out.println("Test: blokada przypisania do innego administratora");

        ServiceTicket ticket = serviceTicketService.createTicket(student, "Problem", "Opis problemu.");
        serviceTicketService.assignTicket(ticket, administrator);

        assertThrows(IllegalStateException.class,
                () -> serviceTicketService.assignTicket(ticket, secondAdministrator));
    }

    @Test
    @DisplayName("Powinno przepisać zgłoszenie do innego administratora")
    void shouldReassignTicket() {
        System.out.println("Test: przepisanie zgłoszenia do innego administratora");

        ServiceTicket ticket = serviceTicketService.createTicket(student, "Problem", "Opis problemu.");
        serviceTicketService.assignTicket(ticket, administrator);
        serviceTicketService.reassignTicket(ticket, secondAdministrator);

        assertEquals(secondAdministrator, ticket.getAssignedTo());
    }

    @Test
    @DisplayName("Powinno zamknąć zgłoszenie")
    void shouldCloseTicket() {
        System.out.println("Test: zamknięcie zgłoszenia");

        ServiceTicket ticket = serviceTicketService.createTicket(student, "Problem", "Opis problemu.");
        serviceTicketService.closeTicket(ticket);

        assertEquals(ServiceTicketStatus.CLOSED, ticket.getStatus());
    }

    @Test
    @DisplayName("Powinno zablokować ponowne zamknięcie zgłoszenia")
    void shouldThrowExceptionWhenClosingClosedTicket() {
        System.out.println("Test: blokada ponownego zamknięcia zgłoszenia");

        ServiceTicket ticket = serviceTicketService.createTicket(student, "Problem", "Opis problemu.");
        serviceTicketService.closeTicket(ticket);

        assertThrows(IllegalStateException.class,
                () -> serviceTicketService.closeTicket(ticket));
    }

    @Test
    @DisplayName("Powinno znaleźć zgłoszenie po id")
    void shouldFindTicketById() {
        System.out.println("Test: wyszukiwanie zgłoszenia po id");

        ServiceTicket ticket = serviceTicketService.createTicket(student, "Problem", "Opis problemu.");

        assertEquals(ticket.getId(), serviceTicketService.findById(ticket.getId()).getId());
    }

    @Test
    @DisplayName("Powinno znaleźć zgłoszenie po tytule")
    void shouldFindTicketByTitle() {
        System.out.println("Test: wyszukiwanie zgłoszenia po tytule");

        serviceTicketService.createTicket(student, "Problem z kontem", "Nie mogę się zalogować.");

        assertEquals("Problem z kontem", serviceTicketService.findByTitle("Problem z kontem").getTitle());
    }

    @Test
    @DisplayName("Powinno zwrócić zgłoszenia po statusie")
    void shouldReturnTicketsByStatus() {
        System.out.println("Test: pobieranie zgłoszeń po statusie");

        ServiceTicket ticket = serviceTicketService.createTicket(student, "Problem", "Opis");
        serviceTicketService.assignTicket(ticket, administrator);

        assertEquals(1, serviceTicketService.getTicketsByStatus(ServiceTicketStatus.IN_PROGRESS).size());
    }

    @Test
    @DisplayName("Powinno zwrócić zgłoszenia zgłoszone przez użytkownika")
    void shouldReturnTicketsByReporter() {
        System.out.println("Test: pobieranie zgłoszeń użytkownika");

        serviceTicketService.createTicket(student, "Problem 1", "Opis 1");
        serviceTicketService.createTicket(student, "Problem 2", "Opis 2");

        assertEquals(2, serviceTicketService.getTicketsByReporter(student).size());
    }

    @Test
    @DisplayName("Powinno zwrócić zgłoszenia przypisane administratorowi")
    void shouldReturnTicketsAssignedToAdministrator() {
        System.out.println("Test: pobieranie zgłoszeń przypisanych administratorowi");

        ServiceTicket ticket = serviceTicketService.createTicket(student, "Problem", "Opis");
        serviceTicketService.assignTicket(ticket, administrator);

        assertEquals(1, serviceTicketService.getTicketsAssignedTo(administrator).size());
    }
}