package pl.usos2.server.model.finance;

import pl.usos2.server.model.base.BaseEntity;
import pl.usos2.server.model.user.Student;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Payment extends BaseEntity {
    private Student student;
    private BigDecimal amount;
    private String title;
    private boolean paid;
    private LocalDate dueDate;

    public Payment() {
    }

    public Payment(Long id, Student student, BigDecimal amount, String title, boolean paid, LocalDate dueDate) {
        super(id);
        this.student = student;
        this.amount = amount;
        this.title = title;
        this.paid = paid;
        this.dueDate = dueDate;
    }

    public Student getStudent() {
        return student;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getTitle() {
        return title;
    }

    public boolean isPaid() {
        return paid;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void markAsPaid() {
        this.paid = true;
    }
}