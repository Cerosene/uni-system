package pl.usos2.server.unit.model;

import org.junit.jupiter.api.Test;
import pl.usos2.server.model.enumtype.Semester;
import pl.usos2.server.model.rental.Rental;
import pl.usos2.server.model.user.Student;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RentalTest {

    @Test
    void shouldMarkRentalAsReturned() {
        Student student = new Student(
                1L, "Kasia", "Nowak", "kasia@test.pl", "haslo123", "S200", "Informatyka", Semester.THIRD
        );

        Rental rental = new Rental(
                2L,
                student,
                "Laptop Dell",
                LocalDate.now(),
                LocalDate.now().plusDays(14),
                false
        );

        assertFalse(rental.isReturned());

        rental.markAsReturned();

        assertTrue(rental.isReturned());
    }
}