package pl.usos2.server.model.rental;

import pl.usos2.server.model.base.BaseEntity;
import pl.usos2.server.model.user.User;

import java.time.LocalDate;

public class Rental extends BaseEntity {
    private User borrower;
    private String resourceName;
    private LocalDate rentalDate;
    private LocalDate returnDate;
    private boolean returned;

    public Rental() {
    }

    public Rental(Long id, User borrower, String resourceName, LocalDate rentalDate, LocalDate returnDate, boolean returned) {
        super(id);
        this.borrower = borrower;
        this.resourceName = resourceName;
        this.rentalDate = rentalDate;
        this.returnDate = returnDate;
        this.returned = returned;
    }

    public User getBorrower() {
        return borrower;
    }

    public String getResourceName() {
        return resourceName;
    }

    public LocalDate getRentalDate() {
        return rentalDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public boolean isReturned() {
        return returned;
    }

    public void markAsReturned() {
        this.returned = true;
    }
}