package pl.usos2.server.unit.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.usos2.server.model.enumtype.Semester;
import pl.usos2.server.model.finance.Payment;
import pl.usos2.server.model.user.Student;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentTest {

    @Test
    @DisplayName("Powinno utworzyć płatność z poprawnymi danymi")
    void shouldCreatePaymentWithCorrectData() {
        System.out.println("Test: tworzenie płatności z poprawnymi danymi");

        Student student = new Student(
                1L, "Jan", "Nowak", "jan@test.pl", "haslo123", "S100", "Informatyka", Semester.FIRST
        );

        LocalDate dueDate = LocalDate.now().plusDays(7);

        Payment payment = new Payment(
                2L,
                student,
                new BigDecimal("200.00"),
                "Tuition fee",
                false,
                dueDate
        );

        assertEquals(2L, payment.getId());
        assertEquals(student, payment.getStudent());
        assertEquals(new BigDecimal("200.00"), payment.getAmount());
        assertEquals("Tuition fee", payment.getTitle());
        assertFalse(payment.isPaid());
        assertEquals(dueDate, payment.getDueDate());
    }

    @Test
    @DisplayName("Powinno oznaczyć płatność jako opłaconą")
    void shouldMarkPaymentAsPaid() {
        System.out.println("Test: oznaczanie płatności jako opłaconej");

        Student student = new Student(
                1L, "Jan", "Nowak", "jan@test.pl", "haslo123", "S100", "Informatyka", Semester.FIRST
        );

        Payment payment = new Payment(
                2L,
                student,
                new BigDecimal("200.00"),
                "Tuition fee",
                false,
                LocalDate.now().plusDays(7)
        );

        assertFalse(payment.isPaid());

        payment.markAsPaid();

        assertTrue(payment.isPaid());
    }
}