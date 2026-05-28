package pl.usos2.server.dao.payment;

import pl.usos2.server.model.finance.Payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentDao {
    Payment save(Long id, Long studentId, BigDecimal amount, String title, LocalDate dueDate);

    Payment markAsPaid(Long paymentId);

    Payment markAsUnpaid(Long paymentId);

    void deleteById(Long paymentId);

    Optional<Payment> findById(Long paymentId);

    List<Payment> findByStudentId(Long studentId);

    List<Payment> findUnpaidByStudentId(Long studentId);

    List<Payment> findPaidByStudentId(Long studentId);

    List<Payment> findOverdue(LocalDate currentDate);

    List<Payment> findOverdueByStudentId(Long studentId, LocalDate currentDate);

    List<Payment> findAll();

    boolean existsById(Long paymentId);

    boolean existsDuplicateUnpaid(Long studentId, BigDecimal amount, String title, LocalDate dueDate);
}
