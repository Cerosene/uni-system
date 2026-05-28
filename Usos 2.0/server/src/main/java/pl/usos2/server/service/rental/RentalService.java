package pl.usos2.server.service.rental;

import pl.usos2.server.dao.rental.JdbcRentalDao;
import pl.usos2.server.dao.rental.RentalDao;
import pl.usos2.server.model.rental.Rental;
import pl.usos2.server.model.user.User;
import pl.usos2.server.service.audit.AuditLogService;
import pl.usos2.server.util.ValidationUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

public class RentalService {
    private static final Logger logger = Logger.getLogger(RentalService.class.getName());
    private final RentalDao rentalDao;
    private final AuditLogService auditLogService;

    public RentalService() {
        this(new JdbcRentalDao(), AuditLogService.getInstance());
    }

    public RentalService(RentalDao rentalDao) {
        this(rentalDao, AuditLogService.getInstance());
    }

    public RentalService(RentalDao rentalDao, AuditLogService auditLogService) {
        this.rentalDao = rentalDao;
        this.auditLogService = auditLogService;
    }

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

        boolean idExists = rentalDao.existsById(id);
        if (idExists) {
            logger.warning("Cannot create rental. Duplicate id: " + id);
            throw new IllegalArgumentException("Rental with this id already exists.");
        }

        boolean resourceAlreadyRented = rentalDao.isResourceCurrentlyRented(normalizedResourceName);

        if (resourceAlreadyRented) {
            logger.warning("Cannot create rental. Resource already rented: " + normalizedResourceName);
            throw new IllegalStateException("Resource is already rented.");
        }

        Rental rental = rentalDao.save(id, borrower.getId(), normalizedResourceName, rentalDate, returnDate);

        logger.info("Rental created for resource: " + normalizedResourceName);
        logger.info("[DIAGNOSTIC] Rental persisted in Oracle. rentalId=" + rental.getId());
        auditSafely(borrower.getId(), "RESOURCE_RENTED", "LOANS", rental.getId(),
                "Resource rented: " + normalizedResourceName);
        return rental;
    }

    public Rental returnRental(Long rentalId) {
        Rental rental = findById(rentalId);

        if (rental.isReturned()) {
            logger.warning("Rental already returned. id=" + rentalId);
            throw new IllegalStateException("Rental has already been returned.");
        }

        Rental updated = rentalDao.markAsReturned(rentalId);

        // Keep old behavior for objects already bound by reference.
        rental.markAsReturned();
        logger.info("Rental returned. id=" + rentalId);
        logger.info("[DIAGNOSTIC] Rental marked as returned in Oracle. rentalId=" + updated.getId());
        auditSafely(
                updated.getBorrower() == null ? null : updated.getBorrower().getId(),
                "RESOURCE_RETURNED",
                "LOANS",
                updated.getId(),
                "Resource returned: " + updated.getResourceName()
        );
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

        Rental updated = rentalDao.extendDueDate(rentalId, newReturnDate);

        // Keep old behavior for objects already bound by reference.
        rental.setReturnDate(newReturnDate);
        logger.info("Extended rental return date. id=" + rentalId);
        logger.info("[DIAGNOSTIC] Rental due date updated in Oracle. rentalId=" + updated.getId());
        return rental;
    }

    public Rental findById(Long rentalId) {
        ValidationUtils.requireNotNull(rentalId, "Rental id cannot be null.");

        Rental rental = rentalDao.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("Rental not found."));
        logger.info("[DIAGNOSTIC] Rental loaded from Oracle. rentalId=" + rentalId);
        return rental;
    }

    public List<Rental> getRentalsForBorrower(User borrower) {
        ValidationUtils.requireNotNull(borrower, "Borrower cannot be null.");
        ValidationUtils.requireNotNull(borrower.getId(), "Borrower id cannot be null.");

        List<Rental> rentals = rentalDao.findByBorrowerId(borrower.getId());
        logger.info("[DIAGNOSTIC] Borrower rentals loaded from Oracle. borrowerId=" + borrower.getId()
                + ", count=" + rentals.size());
        return rentals;
    }

    public List<Rental> getRentalsForResource(String resourceName) {
        String normalizedResourceName = ValidationUtils.normalizeText(resourceName, "Resource name cannot be empty.");

        List<Rental> rentals = rentalDao.findByResourceName(normalizedResourceName);
        logger.info("[DIAGNOSTIC] Resource rentals loaded from Oracle. resource=" + normalizedResourceName
                + ", count=" + rentals.size());
        return rentals;
    }

    public List<Rental> getActiveRentals() {
        List<Rental> rentals = rentalDao.findActive();
        logger.info("[DIAGNOSTIC] Active rentals loaded from Oracle. count=" + rentals.size());
        return rentals;
    }

    public List<Rental> getReturnedRentals() {
        List<Rental> rentals = rentalDao.findReturned();
        logger.info("[DIAGNOSTIC] Returned rentals loaded from Oracle. count=" + rentals.size());
        return rentals;
    }

    public List<Rental> getOverdueRentals(LocalDate currentDate) {
        ValidationUtils.requireNotNull(currentDate, "Current date cannot be null.");

        List<Rental> rentals = rentalDao.findOverdue(currentDate);
        logger.info("[DIAGNOSTIC] Overdue rentals loaded from Oracle. date=" + currentDate
                + ", count=" + rentals.size());
        return rentals;
    }

    public List<Rental> getAllRentals() {
        List<Rental> rentals = rentalDao.findAll();
        logger.info("[DIAGNOSTIC] All rentals loaded from Oracle. count=" + rentals.size());
        return rentals;
    }

    public List<String> getAvailableResources() {
        List<String> resources = rentalDao.findAvailableResourceNames();
        logger.info("[DIAGNOSTIC] Available resources loaded from Oracle. count=" + resources.size());
        return resources;
    }

    private void auditSafely(Long userId, String actionName, String entityName, Long entityId, String details) {
        try {
            auditLogService.logEvent(userId, actionName, entityName, entityId, details);
        } catch (RuntimeException exception) {
            logger.warning("Audit log write failed: " + exception.getMessage());
        }
    }
}
