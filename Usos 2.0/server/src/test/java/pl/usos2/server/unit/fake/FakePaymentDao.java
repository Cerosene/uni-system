package pl.usos2.server.unit.fake;

import pl.usos2.server.dao.payment.PaymentDao;
import pl.usos2.server.model.finance.Payment;
import pl.usos2.server.model.user.Student;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FakePaymentDao implements PaymentDao {
    private final Map<Long, Payment> payments = new ConcurrentHashMap<>();

    @Override
    public Payment save(Long id, Long studentId, BigDecimal amount, String title, LocalDate dueDate) {
        Student student = createStudent(studentId);
        Payment payment = new Payment(id, student, amount, title, false, dueDate);
        payments.put(id, payment);
        return payment;
    }

    @Override
    public Payment markAsPaid(Long paymentId) {
        Payment payment = findExisting(paymentId);
        payment.markAsPaid();
        return payment;
    }

    @Override
    public Payment markAsUnpaid(Long paymentId) {
        Payment payment = findExisting(paymentId);
        if (payment.isPaid()) {
            // Fake domain uses setter only indirectly, so re-create object state
            Payment updated = new Payment(payment.getId(), payment.getStudent(), payment.getAmount(), payment.getTitle(), false, payment.getDueDate());
            payments.put(paymentId, updated);
            return updated;
        }
        return payment;
    }

    @Override
    public void deleteById(Long paymentId) {
        payments.remove(paymentId);
    }

    @Override
    public Optional<Payment> findById(Long paymentId) {
        return Optional.ofNullable(payments.get(paymentId));
    }

    @Override
    public List<Payment> findByStudentId(Long studentId) {
        return payments.values().stream()
                .filter(payment -> payment.getStudent() != null && studentId.equals(payment.getStudent().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Payment> findUnpaidByStudentId(Long studentId) {
        return payments.values().stream()
                .filter(payment -> payment.getStudent() != null
                        && studentId.equals(payment.getStudent().getId())
                        && !payment.isPaid())
                .collect(Collectors.toList());
    }

    @Override
    public List<Payment> findPaidByStudentId(Long studentId) {
        return payments.values().stream()
                .filter(payment -> payment.getStudent() != null
                        && studentId.equals(payment.getStudent().getId())
                        && payment.isPaid())
                .collect(Collectors.toList());
    }

    @Override
    public List<Payment> findOverdue(LocalDate currentDate) {
        return payments.values().stream()
                .filter(payment -> payment.getDueDate() != null
                        && payment.getDueDate().isBefore(currentDate)
                        && !payment.isPaid())
                .collect(Collectors.toList());
    }

    @Override
    public List<Payment> findOverdueByStudentId(Long studentId, LocalDate currentDate) {
        return payments.values().stream()
                .filter(payment -> payment.getStudent() != null
                        && studentId.equals(payment.getStudent().getId())
                        && payment.getDueDate() != null
                        && payment.getDueDate().isBefore(currentDate)
                        && !payment.isPaid())
                .collect(Collectors.toList());
    }

    @Override
    public List<Payment> findAll() {
        return new ArrayList<>(payments.values());
    }

    @Override
    public boolean existsById(Long paymentId) {
        return payments.containsKey(paymentId);
    }

    @Override
    public boolean existsDuplicateUnpaid(Long studentId, BigDecimal amount, String title, LocalDate dueDate) {
        return payments.values().stream()
                .anyMatch(payment -> payment.getStudent() != null
                        && studentId.equals(payment.getStudent().getId())
                        && !payment.isPaid()
                        && payment.getAmount().compareTo(amount) == 0
                        && payment.getTitle().equalsIgnoreCase(title)
                        && payment.getDueDate().equals(dueDate));
    }

    private Payment findExisting(Long paymentId) {
        Payment payment = payments.get(paymentId);
        if (payment == null) {
            throw new IllegalArgumentException("Payment not found.");
        }
        return payment;
    }

    private Student createStudent(Long studentId) {
        return new Student(studentId, "Student", "Student", "student" + studentId + "@example.com",
                "password", "S" + studentId, "Unknown", null);
    }
}
