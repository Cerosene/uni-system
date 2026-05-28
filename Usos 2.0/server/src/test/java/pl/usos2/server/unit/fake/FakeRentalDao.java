package pl.usos2.server.unit.fake;

import pl.usos2.server.dao.rental.RentalDao;
import pl.usos2.server.model.rental.Rental;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.model.user.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class FakeRentalDao implements RentalDao {
    private final Map<Long, Rental> rentals = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public Rental save(Long id, Long borrowerId, String resourceName, LocalDate rentalDate, LocalDate dueDate) {
        Rental rental = new Rental(id, createUser(borrowerId), resourceName, rentalDate, dueDate, false);
        rentals.put(id, rental);
        return rental;
    }

    @Override
    public Rental markAsReturned(Long rentalId) {
        Rental rental = findExisting(rentalId);
        rental.markAsReturned();
        return rental;
    }

    @Override
    public Rental extendDueDate(Long rentalId, LocalDate newDueDate) {
        Rental rental = findExisting(rentalId);
        rental.setReturnDate(newDueDate);
        return rental;
    }

    @Override
    public Optional<Rental> findById(Long rentalId) {
        return Optional.ofNullable(rentals.get(rentalId));
    }

    @Override
    public List<Rental> findByBorrowerId(Long borrowerId) {
        return rentals.values().stream()
                .filter(rental -> rental.getBorrower() != null
                        && borrowerId.equals(rental.getBorrower().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Rental> findByResourceName(String resourceName) {
        return rentals.values().stream()
                .filter(rental -> rental.getResourceName() != null
                        && rental.getResourceName().equalsIgnoreCase(resourceName))
                .collect(Collectors.toList());
    }

    @Override
    public List<Rental> findActive() {
        return rentals.values().stream()
                .filter(rental -> !rental.isReturned())
                .collect(Collectors.toList());
    }

    @Override
    public List<Rental> findReturned() {
        return rentals.values().stream()
                .filter(Rental::isReturned)
                .collect(Collectors.toList());
    }

    @Override
    public List<Rental> findOverdue(LocalDate currentDate) {
        return rentals.values().stream()
                .filter(rental -> rental.getReturnDate() != null
                        && rental.getReturnDate().isBefore(currentDate)
                        && !rental.isReturned())
                .collect(Collectors.toList());
    }

    @Override
    public List<Rental> findAll() {
        return new ArrayList<>(rentals.values());
    }

    @Override
    public List<String> findAvailableResourceNames() {
        return rentals.values().stream()
                .filter(Rental::isReturned)
                .map(Rental::getResourceName)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(Long rentalId) {
        return rentals.containsKey(rentalId);
    }

    @Override
    public boolean isResourceCurrentlyRented(String resourceName) {
        return rentals.values().stream()
                .anyMatch(rental -> rental.getResourceName() != null
                        && rental.getResourceName().equalsIgnoreCase(resourceName)
                        && !rental.isReturned());
    }

    private Rental findExisting(Long rentalId) {
        Rental rental = rentals.get(rentalId);
        if (rental == null) {
            throw new IllegalArgumentException("Rental not found.");
        }
        return rental;
    }

    private User createUser(Long borrowerId) {
        return new Student(borrowerId, "Student", "Student", "student" + borrowerId + "@example.com",
                "password", "S" + borrowerId, "Unknown", null);
    }
}
