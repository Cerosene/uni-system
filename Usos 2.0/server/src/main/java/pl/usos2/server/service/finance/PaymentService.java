package pl.usos2.server.service.finance;

import pl.usos2.server.model.finance.Payment;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.util.ValidationUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class PaymentService {
    private static final Logger logger = Logger.getLogger(PaymentService.class.getName());

    private final List<Payment> payments = new ArrayList<>();

    public Payment createPayment(Long id, Student student, BigDecimal amount, String title, LocalDate dueDate) {
        ValidationUtils.requireNotNull(id, "Payment id cannot be null.");
        ValidationUtils.requireNotNull(student, "Student cannot be null.");
        ValidationUtils.requireNotNull(student.getId(), "Student id cannot be null.");
        ValidationUtils.requirePositive(amount, "Amount must be greater than zero.");
        ValidationUtils.requireNotBlank(title, "Payment title cannot be empty.");
        ValidationUtils.requireNotNull(dueDate, "Due date cannot be null.");

        String normalizedTitle = title.trim();

        boolean idExists = payments.stream().anyMatch(payment -> id.equals(payment.getId()));
        if (idExists) {
            logger.warning("Cannot create payment. Duplicate id: " + id);
            throw new IllegalArgumentException("Payment with this id already exists.");
        }

        boolean duplicatePayment = payments.stream()
                .filter(payment -> !payment.isPaid())
                .anyMatch(payment ->
                        payment.getStudent().getId().equals(student.getId())
                                && payment.getTitle().equalsIgnoreCase(normalizedTitle)
                                && payment.getDueDate().equals(dueDate)
                                && payment.getAmount().compareTo(amount) == 0
                );

        if (duplicatePayment) {
            logger.warning("Cannot create payment. Duplicate unpaid payment for student id=" + student.getId());
            throw new IllegalArgumentException("Similar unpaid payment already exists for this student.");
        }

        Payment payment = new Payment(id, student, amount, normalizedTitle, false, dueDate);
        payments.add(payment);

        logger.info("Payment created for student: " + student.getFullName() + ", title=" + normalizedTitle);
        return payment;
    }

    public Payment markAsPaid(Long paymentId) {
        Payment payment = findById(paymentId);

        if (payment.isPaid()) {
            logger.warning("Payment already paid. id=" + paymentId);
            throw new IllegalStateException("Payment is already marked as paid.");
        }

        payment.markAsPaid();
        logger.info("Payment marked as paid. id=" + paymentId);
        return payment;
    }

    public void removePayment(Long paymentId) {
        Payment payment = findById(paymentId);

        if (payment.isPaid()) {
            logger.warning("Cannot remove paid payment. id=" + paymentId);
            throw new IllegalStateException("Paid payment cannot be removed.");
        }

        payments.remove(payment);
        logger.info("Payment removed. id=" + paymentId);
    }

    public Payment findById(Long paymentId) {
        ValidationUtils.requireNotNull(paymentId, "Payment id cannot be null.");

        Optional<Payment> paymentOptional = payments.stream()
                .filter(payment -> paymentId.equals(payment.getId()))
                .findFirst();

        if (paymentOptional.isEmpty()) {
            throw new IllegalArgumentException("Payment not found.");
        }

        return paymentOptional.get();
    }

    public List<Payment> getPaymentsForStudent(Student student) {
        ValidationUtils.requireNotNull(student, "Student cannot be null.");
        ValidationUtils.requireNotNull(student.getId(), "Student id cannot be null.");

        return payments.stream()
                .filter(payment -> payment.getStudent().getId().equals(student.getId()))
                .toList();
    }

    public List<Payment> getUnpaidPaymentsForStudent(Student student) {
        ValidationUtils.requireNotNull(student, "Student cannot be null.");
        ValidationUtils.requireNotNull(student.getId(), "Student id cannot be null.");

        return payments.stream()
                .filter(payment -> payment.getStudent().getId().equals(student.getId()))
                .filter(payment -> !payment.isPaid())
                .toList();
    }

    public List<Payment> getPaidPaymentsForStudent(Student student) {
        ValidationUtils.requireNotNull(student, "Student cannot be null.");
        ValidationUtils.requireNotNull(student.getId(), "Student id cannot be null.");

        return payments.stream()
                .filter(payment -> payment.getStudent().getId().equals(student.getId()))
                .filter(Payment::isPaid)
                .toList();
    }

    public List<Payment> getOverduePayments(LocalDate currentDate) {
        ValidationUtils.requireNotNull(currentDate, "Current date cannot be null.");

        return payments.stream()
                .filter(payment -> !payment.isPaid())
                .filter(payment -> payment.getDueDate().isBefore(currentDate))
                .toList();
    }

    public List<Payment> getOverduePaymentsForStudent(Student student, LocalDate currentDate) {
        ValidationUtils.requireNotNull(student, "Student cannot be null.");
        ValidationUtils.requireNotNull(student.getId(), "Student id cannot be null.");
        ValidationUtils.requireNotNull(currentDate, "Current date cannot be null.");

        return payments.stream()
                .filter(payment -> payment.getStudent().getId().equals(student.getId()))
                .filter(payment -> !payment.isPaid())
                .filter(payment -> payment.getDueDate().isBefore(currentDate))
                .toList();
    }

    public List<Payment> getAllPayments() {
        return new ArrayList<>(payments);
    }
}