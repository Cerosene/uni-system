package pl.usos2.server.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.usos2.server.model.enumtype.RequestStatus;
import pl.usos2.server.model.enumtype.RequestType;
import pl.usos2.server.model.enumtype.Semester;
import pl.usos2.server.model.request.Request;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.service.audit.AuditLogService;
import pl.usos2.server.service.request.RequestService;
import pl.usos2.server.unit.fake.FakeAuditLogDao;
import pl.usos2.server.unit.fake.FakeRequestDao;

import static org.junit.jupiter.api.Assertions.*;

class RequestServiceTest {

    private RequestService requestService;
    private Student student;

    @BeforeEach
    void setUp() {
        requestService = new RequestService(new FakeRequestDao(), new AuditLogService(new FakeAuditLogDao()));
        student = new Student(
                1L,
                "Jan",
                "Kowalski",
                "jan@test.pl",
                "haslo123",
                "s10001",
                "Informatyka",
                Semester.SECOND
        );
    }

    @Test
    @DisplayName("Powinno złożyć wniosek z wygenerowanym id")
    void shouldSubmitRequestWithGeneratedId() {
        System.out.println("Test: składanie wniosku z generowanym id");

        Request request = requestService.submitRequest(student, RequestType.SCHOLARSHIP, "Proszę o stypendium.");

        assertNotNull(request.getId());
        assertEquals(RequestStatus.SUBMITTED, request.getStatus());
    }

    @Test
    @DisplayName("Powinno zablokować złożenie pustego wniosku")
    void shouldThrowExceptionWhenRequestContentIsBlank() {
        System.out.println("Test: blokada pustej treści wniosku");

        assertThrows(IllegalArgumentException.class,
                () -> requestService.submitRequest(student, RequestType.OTHER, " "));
    }

    @Test
    @DisplayName("Powinno zmienić status z SUBMITTED na IN_REVIEW")
    void shouldChangeStatusFromSubmittedToInReview() {
        System.out.println("Test: zmiana statusu na IN_REVIEW");

        Request request = requestService.submitRequest(student, RequestType.CERTIFICATE, "Potrzebuję zaświadczenia.");
        requestService.changeStatus(request, RequestStatus.IN_REVIEW);

        assertEquals(RequestStatus.IN_REVIEW, request.getStatus());
    }

    @Test
    @DisplayName("Powinno zmienić status z IN_REVIEW na APPROVED")
    void shouldChangeStatusFromInReviewToApproved() {
        System.out.println("Test: zmiana statusu na APPROVED");

        Request request = requestService.submitRequest(student, RequestType.CERTIFICATE, "Potrzebuję zaświadczenia.");
        requestService.changeStatus(request, RequestStatus.IN_REVIEW);
        requestService.changeStatus(request, RequestStatus.APPROVED);

        assertEquals(RequestStatus.APPROVED, request.getStatus());
    }

    @Test
    @DisplayName("Powinno zablokować niedozwolone przejście statusu")
    void shouldThrowExceptionForInvalidStatusTransition() {
        System.out.println("Test: blokada niedozwolonego przejścia statusu");

        Request request = requestService.submitRequest(student, RequestType.CERTIFICATE, "Potrzebuję zaświadczenia.");

        assertThrows(IllegalStateException.class,
                () -> requestService.changeStatus(request, RequestStatus.APPROVED));
    }

    @Test
    @DisplayName("Powinno zablokować ustawienie tego samego statusu drugi raz")
    void shouldThrowExceptionWhenSettingTheSameStatusAgain() {
        System.out.println("Test: blokada ustawienia tego samego statusu drugi raz");

        Request request = requestService.submitRequest(student, RequestType.CERTIFICATE, "Potrzebuję zaświadczenia.");

        assertThrows(IllegalStateException.class,
                () -> requestService.changeStatus(request, RequestStatus.SUBMITTED));
    }

    @Test
    @DisplayName("Powinno znaleźć wniosek po id")
    void shouldFindRequestById() {
        System.out.println("Test: wyszukiwanie wniosku po id");

        Request request = requestService.submitRequest(student, RequestType.OTHER, "Treść wniosku");

        assertEquals(request.getId(), requestService.findById(request.getId()).getId());
    }

    @Test
    @DisplayName("Powinno zwrócić wnioski studenta")
    void shouldReturnRequestsByStudent() {
        System.out.println("Test: pobieranie wniosków studenta");

        requestService.submitRequest(student, RequestType.CERTIFICATE, "Zaświadczenie");
        requestService.submitRequest(student, RequestType.SCHOLARSHIP, "Stypendium");

        assertEquals(2, requestService.getRequestsByStudent(student).size());
    }

    @Test
    @DisplayName("Powinno zwrócić wnioski po statusie")
    void shouldReturnRequestsByStatus() {
        System.out.println("Test: pobieranie wniosków po statusie");

        Request request = requestService.submitRequest(student, RequestType.CERTIFICATE, "Zaświadczenie");
        requestService.changeStatus(request, RequestStatus.IN_REVIEW);

        assertEquals(1, requestService.getRequestsByStatus(RequestStatus.IN_REVIEW).size());
    }

    @Test
    @DisplayName("Powinno zwrócić wnioski po typie")
    void shouldReturnRequestsByType() {
        System.out.println("Test: pobieranie wniosków po typie");

        requestService.submitRequest(student, RequestType.CERTIFICATE, "Zaświadczenie");
        requestService.submitRequest(student, RequestType.SCHOLARSHIP, "Stypendium");

        assertEquals(1, requestService.getRequestsByType(RequestType.CERTIFICATE).size());
    }

    @Test
    @DisplayName("Powinno zwrócić oczekujące wnioski")
    void shouldReturnPendingRequests() {
        System.out.println("Test: pobieranie oczekujących wniosków");

        Request request = requestService.submitRequest(student, RequestType.CERTIFICATE, "Potrzebuję zaświadczenia.");
        requestService.changeStatus(request, RequestStatus.IN_REVIEW);

        assertEquals(1, requestService.getPendingRequests().size());
    }
}