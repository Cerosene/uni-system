package pl.usos2.server.unit.model;

import org.junit.jupiter.api.Test;
import pl.usos2.server.model.enumtype.Semester;
import pl.usos2.server.model.finance.Payment;
import pl.usos2.server.model.user.Student;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentTest {

    @Test
    void shouldMarkPaymentAsPaid() {
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