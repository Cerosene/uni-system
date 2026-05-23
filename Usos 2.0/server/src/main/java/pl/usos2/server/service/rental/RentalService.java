package pl.usos2.server.service.rental;

import pl.usos2.server.model.rental.Rental;
import pl.usos2.server.model.user.User;
import pl.usos2.server.util.ValidationUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class RentalService {
    private static final Logger logger = Logger.getLogger(RentalService.class.getName());

    private final List<Rental> rentals = new ArrayList<>();

    public Rental createRental(Long id, User borrower, String resourceName, LocalDate rentalDate, LocalDate returnDate) {
        ValidationUtils.requireNotNull(id, "Rental id cannot be null.");
        ValidationUtils.requireNotNull(borrower, "Borrower cannot be null.");
        ValidationUtils.requireNotNull(borrower.getId(), "Borrower id cannot be null.");
        ValidationUtils.requireNotBlank(resourceName, "Resource name cannot be empty.");
        ValidationUtils.requireNotNull(rentalDate, "Rental date cannot be null.");
        ValidationUtils.requireNotNull(returnDate, "Return date cannot be null.");

        String normalizedResourceName = resourceName.trim();

        if (returnDate.isBefore(rentalDate)) {
            throw new IllegalArgumentException("Return date cannot be before rental date.");
        }

        boolean idExists = rentals.stream().anyMatch(rental -> id.equals(rental.getId()));
        if (idExists) {
            logger.warning("Cannot create rental. Duplicate id: " + id);
            throw new IllegalArgumentException("Rental with this id already exists.");
        }

        boolean resourceAlreadyRented = rentals.stream()
                .filter(rental -> !rental.isReturned())
                .anyMatch(rental -> rental.getResourceName().equalsIgnoreCase(normalizedResourceName));

        if (resourceAlreadyRented) {
            logger.warning("Cannot create rental. Resource already rented: " + normalizedResourceName);
            throw new IllegalStateException("Resource is already rented.");
        }

        Rental rental = new Rental(id, borrower, normalizedResourceName, rentalDate, returnDate, false);
        rentals.add(rental);

        logger.info("Rental created for resource: " + normalizedResourceName);
        return rental;
    }

    public Rental returnRental(Long rentalId) {
        Rental rental = findById(rentalId);

        if (rental.isReturned()) {
            logger.warning("Rental already returned. id=" + rentalId);
            throw new IllegalStateException("Rental has already been returned.");
        }

        rental.markAsReturned();
        logger.info("Rental returned. id=" + rentalId);
        return rental;
    }

    public Rental extendReturnDate(Long rentalId, LocalDate newReturnDate) {
        Rental rental = findById(rentalId);
        ValidationUtils.requireNotNull(newReturnDate, "New return date cannot be null.");

        if (rental.isReturned()) {
            logger.warning("Cannot extend returned rental. id=" + rentalId);
            throw new IllegalStateException("Returned rental cannot be extended.");
        }

        if (!newReturnDate.isAfter(rental.getReturnDate())) {
            logger.warning("New return date must be after current return date. id=" + rentalId);
            throw new IllegalArgumentException("New return date must be after current return date.");
        }

        rental.setReturnDate(newReturnDate);
        logger.info("Extended rental return date. id=" + rentalId);
        return rental;
    }

    public Rental findById(Long rentalId) {
        ValidationUtils.requireNotNull(rentalId, "Rental id cannot be null.");

        Optional<Rental> rentalOptional = rentals.stream()
                .filter(rental -> rentalId.equals(rental.getId()))
                .findFirst();

        if (rentalOptional.isEmpty()) {
            throw new IllegalArgumentException("Rental not found.");
        }

        return rentalOptional.get();
    }

    public List<Rental> getRentalsForBorrower(User borrower) {
        ValidationUtils.requireNotNull(borrower, "Borrower cannot be null.");
        ValidationUtils.requireNotNull(borrower.getId(), "Borrower id cannot be null.");

        return rentals.stream()
                .filter(rental -> rental.getBorrower().getId().equals(borrower.getId()))
                .toList();
    }

    public List<Rental> getRentalsForResource(String resourceName) {
        String normalizedResourceName = ValidationUtils.normalizeText(resourceName, "Resource name cannot be empty.");

        return rentals.stream()
                .filter(rental -> rental.getResourceName().equalsIgnoreCase(normalizedResourceName))
                .toList();
    }

    public List<Rental> getActiveRentals() {
        return rentals.stream()
                .filter(rental -> !rental.isReturned())
                .toList();
    }

    public List<Rental> getReturnedRentals() {
        return rentals.stream()
                .filter(Rental::isReturned)
                .toList();
    }

    public List<Rental> getOverdueRentals(LocalDate currentDate) {
        ValidationUtils.requireNotNull(currentDate, "Current date cannot be null.");

        return rentals.stream()
                .filter(rental -> !rental.isReturned())
                .filter(rental -> rental.getReturnDate().isBefore(currentDate))
                .toList();
    }

    public List<Rental> getAllRentals() {
        return new ArrayList<>(rentals);
    }
}