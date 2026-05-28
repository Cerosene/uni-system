package pl.usos2.server.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.usos2.server.model.enumtype.Semester;
import pl.usos2.server.model.finance.Payment;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.service.audit.AuditLogService;
import pl.usos2.server.service.finance.PaymentService;
import pl.usos2.server.unit.fake.FakeAuditLogDao;
import pl.usos2.server.unit.fake.FakePaymentDao;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceTest {

    private PaymentService paymentService;
    private Student student;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(new FakePaymentDao(), new AuditLogService(new FakeAuditLogDao()));
        student = new Student(
                1L,
                "Jan",
                "Nowak",
                "jan@test.pl",
                "haslo123",
                "S100",
                "Informatyka",
                Semester.FIRST
        );
    }

    @Test
    @DisplayName("Powinno utworzyć płatność")
    void shouldCreatePayment() {
        System.out.println("Test: tworzenie płatności");

        Payment payment = paymentService.createPayment(
                10L,
                student,
                new BigDecimal("200.00"),
                "Czesne",
                LocalDate.now().plusDays(7)
        );

        assertNotNull(payment);
        assertFalse(payment.isPaid());
        assertEquals(1, paymentService.getAllPayments().size());
    }

    @Test
    @DisplayName("Powinno oznaczyć płatność jako opłaconą")
    void shouldMarkPaymentAsPaid() {
        System.out.println("Test: oznaczenie płatności jako opłaconej");

        paymentService.createPayment(
                10L,
                student,
                new BigDecimal("200.00"),
                "Czesne",
                LocalDate.now().plusDays(7)
        );

        Payment payment = paymentService.markAsPaid(10L);

        assertTrue(payment.isPaid());
    }

    @Test
    @DisplayName("Powinno zablokować ponowne opłacenie tej samej płatności")
    void shouldThrowExceptionWhenPaymentAlreadyPaid() {
        System.out.println("Test: blokada ponownego opłacenia płatności");

        paymentService.createPayment(
                10L,
                student,
                new BigDecimal("200.00"),
                "Czesne",
                LocalDate.now().plusDays(7)
        );

        paymentService.markAsPaid(10L);

        assertThrows(IllegalStateException.class, () -> paymentService.markAsPaid(10L));
    }

    @Test
    @DisplayName("Powinno zablokować zduplikowaną nieopłaconą płatność")
    void shouldBlockDuplicateUnpaidPayment() {
        System.out.println("Test: blokada duplikatu nieopłaconej płatności");

        paymentService.createPayment(
                10L,
                student,
                new BigDecimal("200.00"),
                "Czesne",
                LocalDate.of(2026, 5, 10)
        );

        assertThrows(IllegalArgumentException.class, () -> paymentService.createPayment(
                11L,
                student,
                new BigDecimal("200.00"),
                "Czesne",
                LocalDate.of(2026, 5, 10)
        ));
    }

    @Test
    @DisplayName("Powinno znaleźć płatność po id")
    void shouldFindPaymentById() {
        System.out.println("Test: wyszukiwanie płatności po id");

        paymentService.createPayment(
                10L,
                student,
                new BigDecimal("200.00"),
                "Czesne",
                LocalDate.now().plusDays(7)
        );

        assertEquals(10L, paymentService.findById(10L).getId());
    }

    @Test
    @DisplayName("Powinno rzucić wyjątek dla nieistniejącej płatności")
    void shouldThrowExceptionWhenPaymentNotFound() {
        System.out.println("Test: wyszukiwanie nieistniejącej płatności");

        assertThrows(IllegalArgumentException.class, () -> paymentService.findById(99L));
    }

    @Test
    @DisplayName("Powinno zwrócić wszystkie płatności studenta")
    void shouldReturnPaymentsForStudent() {
        System.out.println("Test: pobieranie wszystkich płatności studenta");

        paymentService.createPayment(
                10L,
                student,
                new BigDecimal("200.00"),
                "Czesne",
                LocalDate.now().plusDays(7)
        );

        paymentService.createPayment(
                11L,
                student,
                new BigDecimal("50.00"),
                "Biblioteka",
                LocalDate.now().plusDays(3)
        );

        assertEquals(2, paymentService.getPaymentsForStudent(student).size());
    }

    @Test
    @DisplayName("Powinno zwrócić opłacone i nieopłacone płatności studenta")
    void shouldReturnPaidAndUnpaidPaymentsForStudent() {
        System.out.println("Test: pobieranie opłaconych i nieopłaconych płatności studenta");

        paymentService.createPayment(
                10L,
                student,
                new BigDecimal("200.00"),
                "Czesne",
                LocalDate.now().plusDays(7)
        );

        paymentService.createPayment(
                11L,
                student,
                new BigDecimal("50.00"),
                "Biblioteka",
                LocalDate.now().plusDays(3)
        );

        paymentService.markAsPaid(11L);

        assertEquals(1, paymentService.getUnpaidPaymentsForStudent(student).size());
        assertEquals(1, paymentService.getPaidPaymentsForStudent(student).size());
    }

    @Test
    @DisplayName("Powinno zwrócić zaległe płatności")
    void shouldReturnOverduePayments() {
        System.out.println("Test: pobieranie zaległych płatności");

        paymentService.createPayment(
                10L,
                student,
                new BigDecimal("300.00"),
                "Opłata biblioteczna",
                LocalDate.now().minusDays(2)
        );

        assertEquals(1, paymentService.getOverduePayments(LocalDate.now()).size());
    }

    @Test
    @DisplayName("Powinno zwrócić zaległe płatności konkretnego studenta")
    void shouldReturnOverduePaymentsForStudent() {
        System.out.println("Test: pobieranie zaległych płatności studenta");

        paymentService.createPayment(
                10L,
                student,
                new BigDecimal("300.00"),
                "Opłata biblioteczna",
                LocalDate.now().minusDays(2)
        );

        assertEquals(1, paymentService.getOverduePaymentsForStudent(student, LocalDate.now()).size());
    }

    @Test
    @DisplayName("Powinno usunąć nieopłaconą płatność")
    void shouldRemoveUnpaidPayment() {
        System.out.println("Test: usuwanie nieopłaconej płatności");

        paymentService.createPayment(
                10L,
                student,
                new BigDecimal("200.00"),
                "Czesne",
                LocalDate.now().plusDays(7)
        );

        paymentService.removePayment(10L);

        assertEquals(0, paymentService.getAllPayments().size());
    }

    @Test
    @DisplayName("Powinno zablokować usunięcie opłaconej płatności")
    void shouldThrowExceptionWhenRemovingPaidPayment() {
        System.out.println("Test: blokada usunięcia opłaconej płatności");

        paymentService.createPayment(
                10L,
                student,
                new BigDecimal("200.00"),
                "Czesne",
                LocalDate.now().plusDays(7)
        );

        paymentService.markAsPaid(10L);

        assertThrows(IllegalStateException.class, () -> paymentService.removePayment(10L));
    }
}