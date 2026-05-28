package pl.usos2.server.dao.rental;

import pl.usos2.server.model.rental.Rental;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RentalDao {
    Rental save(Long id, Long borrowerId, String resourceName, LocalDate rentalDate, LocalDate dueDate);

    Rental markAsReturned(Long rentalId);

    Rental extendDueDate(Long rentalId, LocalDate newDueDate);

    Optional<Rental> findById(Long rentalId);

    List<Rental> findByBorrowerId(Long borrowerId);

    List<Rental> findByResourceName(String resourceName);

    List<Rental> findActive();

    List<Rental> findReturned();

    List<Rental> findOverdue(LocalDate currentDate);

    List<Rental> findAll();

    List<String> findAvailableResourceNames();

    boolean existsById(Long rentalId);

    boolean isResourceCurrentlyRented(String resourceName);
}

