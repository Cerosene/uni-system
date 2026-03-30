package pl.usos2.server.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.usos2.server.model.enumtype.RequestStatus;
import pl.usos2.server.model.enumtype.RequestType;
import pl.usos2.server.model.enumtype.Semester;
import pl.usos2.server.model.request.Request;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.service.request.RequestService;

import static org.junit.jupiter.api.Assertions.*;

class RequestServiceTest {

    private RequestService requestService;
    private Student student;

    @BeforeEach
    void setUp() {
        requestService = new RequestService();
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
    void shouldSubmitRequestWithSubmittedStatus() {
        Request request = requestService.submitRequest(student, RequestType.SCHOLARSHIP, "Proszę o stypendium.");

        assertNotNull(request);
        assertEquals(RequestStatus.SUBMITTED, request.getStatus());
        assertEquals(student, request.getStudent());
    }

    @Test
    void shouldThrowExceptionWhenContentIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> requestService.submitRequest(student, RequestType.OTHER, " "));
    }

    @Test
    void shouldChangeRequestStatus() {
        Request request = requestService.submitRequest(student, RequestType.CERTIFICATE, "Potrzebuję zaświadczenia.");
        requestService.changeStatus(request, RequestStatus.APPROVED);

        assertEquals(RequestStatus.APPROVED, request.getStatus());
    }
}