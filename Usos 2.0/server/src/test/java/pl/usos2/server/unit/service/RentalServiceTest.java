package pl.usos2.server.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.usos2.server.model.enumtype.Semester;
import pl.usos2.server.model.rental.Rental;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.service.audit.AuditLogService;
import pl.usos2.server.service.rental.RentalService;
import pl.usos2.server.unit.fake.FakeAuditLogDao;
import pl.usos2.server.unit.fake.FakeRentalDao;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RentalServiceTest {

    private RentalService rentalService;
    private Student student;

    @BeforeEach
    void setUp() {
        rentalService = new RentalService(new FakeRentalDao(), new AuditLogService(new FakeAuditLogDao()));
        student = new Student(
                1L,
                "Kasia",
                "Nowak",
                "kasia@test.pl",
                "haslo123",
                "S200",
                "Informatyka",
                Semester.THIRD
        );
    }

    @Test
    @DisplayName("Powinno utworzyć wypożyczenie")
    void shouldCreateRental() {
        System.out.println("Test: tworzenie wypożyczenia");

        Rental rental = rentalService.createRental(
                2L,
                student,
                "Laptop Dell",
                LocalDate.now(),
                LocalDate.now().plusDays(14)
        );

        assertNotNull(rental);
        assertFalse(rental.isReturned());
        assertEquals(1, rentalService.getAllRentals().size());
    }

    @Test
    @DisplayName("Powinno zablokować drugie aktywne wypożyczenie tego samego zasobu")
    void shouldBlockSecondActiveRentalForSameResource() {
        System.out.println("Test: blokada drugiego aktywnego wypożyczenia tego samego zasobu");

        rentalService.createRental(
                2L,
                student,
                "Laptop Dell",
                LocalDate.now(),
                LocalDate.now().plusDays(14)
        );

        assertThrows(IllegalStateException.class, () -> rentalService.createRental(
                3L,
                student,
                "Laptop Dell",
                LocalDate.now(),
                LocalDate.now().plusDays(7)
        ));
    }

    @Test
    @DisplayName("Powinno oznaczyć wypożyczenie jako zwrócone")
    void shouldReturnRental() {
        System.out.println("Test: zwrot wypożyczenia");

        rentalService.createRental(
                2L,
                student,
                "Laptop Dell",
                LocalDate.now(),
                LocalDate.now().plusDays(14)
        );

        Rental rental = rentalService.returnRental(2L);

        assertTrue(rental.isReturned());
    }

    @Test
    @DisplayName("Powinno zablokować ponowny zwrot wypożyczenia")
    void shouldThrowExceptionWhenRentalAlreadyReturned() {
        System.out.println("Test: blokada ponownego zwrotu wypożyczenia");

        rentalService.createRental(
                2L,
                student,
                "Laptop Dell",
                LocalDate.now(),
                LocalDate.now().plusDays(14)
        );

        rentalService.returnRental(2L);

        assertThrows(IllegalStateException.class, () -> rentalService.returnRental(2L));
    }

    @Test
    @DisplayName("Powinno przedłużyć termin zwrotu")
    void shouldExtendReturnDate() {
        System.out.println("Test: przedłużenie terminu zwrotu");

        LocalDate rentalDate = LocalDate.of(2026, 4, 1);
        LocalDate originalReturnDate = LocalDate.of(2026, 5, 1);
        LocalDate extendedReturnDate = LocalDate.of(2026, 5, 10);

        rentalService.createRental(
                2L,
                student,
                "Laptop Dell",
                rentalDate,
                originalReturnDate
        );

        rentalService.extendReturnDate(2L, extendedReturnDate);

        assertEquals(extendedReturnDate, rentalService.findById(2L).getReturnDate());
    }

    @Test
    @DisplayName("Powinno zablokować przedłużenie na wcześniejszą lub tę samą datę")
    void shouldThrowExceptionWhenNewReturnDateIsNotLater() {
        System.out.println("Test: blokada niepoprawnego przedłużenia terminu");

        LocalDate rentalDate = LocalDate.of(2026, 4, 1);
        LocalDate returnDate = LocalDate.of(2026, 5, 10);

        rentalService.createRental(
                2L,
                student,
                "Laptop Dell",
                rentalDate,
                returnDate
        );

        assertThrows(IllegalArgumentException.class,
                () -> rentalService.extendReturnDate(2L, returnDate));
    }

    @Test
    @DisplayName("Powinno zablokować przedłużenie zwróconego wypożyczenia")
    void shouldBlockExtensionForReturnedRental() {
        System.out.println("Test: blokada przedłużenia zwróconego wypożyczenia");

        rentalService.createRental(
                2L,
                student,
                "Laptop Dell",
                LocalDate.now(),
                LocalDate.now().plusDays(14)
        );

        rentalService.returnRental(2L);

        assertThrows(IllegalStateException.class,
                () -> rentalService.extendReturnDate(2L, LocalDate.now().plusDays(20)));
    }

    @Test
    @DisplayName("Powinno znaleźć wypożyczenie po id")
    void shouldFindRentalById() {
        System.out.println("Test: wyszukiwanie wypożyczenia po id");

        rentalService.createRental(
                2L,
                student,
                "Laptop Dell",
                LocalDate.now(),
                LocalDate.now().plusDays(14)
        );

        assertEquals(2L, rentalService.findById(2L).getId());
    }

    @Test
    @DisplayName("Powinno rzucić wyjątek dla nieistniejącego wypożyczenia")
    void shouldThrowExceptionWhenRentalNotFound() {
        System.out.println("Test: wyszukiwanie nieistniejącego wypożyczenia");

        assertThrows(IllegalArgumentException.class, () -> rentalService.findById(99L));
    }

    @Test
    @DisplayName("Powinno zwrócić aktywne wypożyczenia")
    void shouldReturnActiveRentals() {
        System.out.println("Test: pobieranie aktywnych wypożyczeń");

        rentalService.createRental(
                2L,
                student,
                "Laptop Dell",
                LocalDate.now(),
                LocalDate.now().plusDays(14)
        );

        assertEquals(1, rentalService.getActiveRentals().size());
    }

    @Test
    @DisplayName("Powinno zwrócić listę zakończonych wypożyczeń")
    void shouldReturnReturnedRentals() {
        System.out.println("Test: pobieranie zwróconych wypożyczeń");

        rentalService.createRental(
                2L,
                student,
                "Laptop Dell",
                LocalDate.now(),
                LocalDate.now().plusDays(14)
        );

        rentalService.returnRental(2L);

        assertEquals(1, rentalService.getReturnedRentals().size());
    }

    @Test
    @DisplayName("Powinno zwrócić przeterminowane wypożyczenia")
    void shouldReturnOverdueRentals() {
        System.out.println("Test: pobieranie przeterminowanych wypożyczeń");

        rentalService.createRental(
                2L,
                student,
                "Laptop Dell",
                LocalDate.now().minusDays(10),
                LocalDate.now().minusDays(1)
        );

        assertEquals(1, rentalService.getOverdueRentals(LocalDate.now()).size());
    }

    @Test
    @DisplayName("Powinno zwrócić wypożyczenia użytkownika")
    void shouldReturnRentalsForBorrower() {
        System.out.println("Test: pobieranie wypożyczeń użytkownika");

        rentalService.createRental(
                2L,
                student,
                "Laptop Dell",
                LocalDate.now(),
                LocalDate.now().plusDays(14)
        );

        assertEquals(1, rentalService.getRentalsForBorrower(student).size());
    }

    @Test
    @DisplayName("Powinno zwrócić wypożyczenia po nazwie zasobu")
    void shouldReturnRentalsForResource() {
        System.out.println("Test: pobieranie wypożyczeń po zasobie");

        rentalService.createRental(
                2L,
                student,
                "Laptop Dell",
                LocalDate.now(),
                LocalDate.now().plusDays(14)
        );

        assertEquals(1, rentalService.getRentalsForResource("Laptop Dell").size());
    }
}