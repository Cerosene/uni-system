package pl.usos2.server.unit.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.usos2.server.model.enumtype.Semester;
import pl.usos2.server.model.rental.Rental;
import pl.usos2.server.model.user.Student;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RentalTest {

    @Test
    @DisplayName("Powinno utworzyć wypożyczenie z poprawnymi danymi")
    void shouldCreateRentalWithCorrectData() {
        System.out.println("Test: tworzenie wypożyczenia z poprawnymi danymi");

        Student student = new Student(
                1L, "Kasia", "Nowak", "kasia@test.pl", "haslo123", "S200", "Informatyka", Semester.THIRD
        );

        LocalDate rentalDate = LocalDate.now();
        LocalDate returnDate = LocalDate.now().plusDays(14);

        Rental rental = new Rental(
                2L,
                student,
                "Laptop Dell",
                rentalDate,
                returnDate,
                false
        );

        assertEquals(2L, rental.getId());
        assertEquals(student, rental.getBorrower());
        assertEquals("Laptop Dell", rental.getResourceName());
        assertEquals(rentalDate, rental.getRentalDate());
        assertEquals(returnDate, rental.getReturnDate());
        assertFalse(rental.isReturned());
    }

    @Test
    @DisplayName("Powinno oznaczyć wypożyczenie jako zwrócone")
    void shouldMarkRentalAsReturned() {
        System.out.println("Test: oznaczanie wypożyczenia jako zwróconego");

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