package pl.usos2.server.service.finance;

import pl.usos2.server.dao.payment.JdbcPaymentDao;
import pl.usos2.server.dao.payment.PaymentDao;
import pl.usos2.server.model.finance.Payment;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.service.audit.AuditLogService;
import pl.usos2.server.util.ValidationUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

public class PaymentService {
    private static final Logger logger = Logger.getLogger(PaymentService.class.getName());
    private final PaymentDao paymentDao;
    private final AuditLogService auditLogService;

    public PaymentService() {
        this(new JdbcPaymentDao(), AuditLogService.getInstance());
    }

    public PaymentService(PaymentDao paymentDao) {
        this(paymentDao, AuditLogService.getInstance());
    }

    public PaymentService(PaymentDao paymentDao, AuditLogService auditLogService) {
        this.paymentDao = paymentDao;
        this.auditLogService = auditLogService;
    }

    public Payment createPayment(Long id, Student student, BigDecimal amount, String title, LocalDate dueDate) {
        ValidationUtils.requireNotNull(id, "Payment id cannot be null.");
        ValidationUtils.requireNotNull(student, "Student cannot be null.");
        ValidationUtils.requireNotNull(student.getId(), "Student id cannot be null.");
        ValidationUtils.requirePositive(amount, "Amount must be greater than zero.");
        ValidationUtils.requireNotBlank(title, "Payment title cannot be empty.");
        ValidationUtils.requireNotNull(dueDate, "Due date cannot be null.");

        String normalizedTitle = title.trim();

        boolean idExists = paymentDao.existsById(id);
        if (idExists) {
            logger.warning("Cannot create payment. Duplicate id: " + id);
            throw new IllegalArgumentException("Payment with this id already exists.");
        }

        boolean duplicatePayment = paymentDao.existsDuplicateUnpaid(student.getId(), amount, normalizedTitle, dueDate);

        if (duplicatePayment) {
            logger.warning("Cannot create payment. Duplicate unpaid payment for student id=" + student.getId());
            throw new IllegalArgumentException("Similar unpaid payment already exists for this student.");
        }

        Payment payment = paymentDao.save(id, student.getId(), amount, normalizedTitle, dueDate);

        logger.info("Payment created for student: " + student.getFullName() + ", title=" + normalizedTitle);
        logger.info("[DIAGNOSTIC] Payment persisted in Oracle. paymentId=" + payment.getId());
        return payment;
    }

    public Payment markAsPaid(Long paymentId) {
        Payment payment = findById(paymentId);

        if (payment.isPaid()) {
            logger.warning("Payment already paid. id=" + paymentId);
            throw new IllegalStateException("Payment is already marked as paid.");
        }

        Payment updated = paymentDao.markAsPaid(paymentId);
        payment.markAsPaid();

        logger.info("Payment marked as paid. id=" + paymentId);
        logger.info("[DIAGNOSTIC] Payment marked as paid in Oracle. paymentId=" + updated.getId());
        auditSafely(
                updated.getStudent() == null ? null : updated.getStudent().getId(),
                "PAYMENT_MARKED_PAID",
                "PAYMENTS",
                updated.getId(),
                "Payment marked as paid. title=" + updated.getTitle()
        );
        return payment;
    }

    public Payment markAsUnpaid(Long paymentId) {
        Payment payment = findById(paymentId);

        if (!payment.isPaid()) {
            logger.warning("Payment already unpaid. id=" + paymentId);
            throw new IllegalStateException("Payment is already marked as unpaid.");
        }

        Payment updated = paymentDao.markAsUnpaid(paymentId);
        logger.info("Payment marked as unpaid. id=" + paymentId);
        logger.info("[DIAGNOSTIC] Payment marked as unpaid in Oracle. paymentId=" + updated.getId());
        return updated;
    }

    public void removePayment(Long paymentId) {
        Payment payment = findById(paymentId);

        if (payment.isPaid()) {
            logger.warning("Cannot remove paid payment. id=" + paymentId);
            throw new IllegalStateException("Paid payment cannot be removed.");
        }

        paymentDao.deleteById(paymentId);
        logger.info("Payment removed. id=" + paymentId);
        logger.info("[DIAGNOSTIC] Payment removed from Oracle. paymentId=" + paymentId);
    }

    public Payment findById(Long paymentId) {
        ValidationUtils.requireNotNull(paymentId, "Payment id cannot be null.");

        Payment payment = paymentDao.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found."));
        logger.info("[DIAGNOSTIC] Payment loaded from Oracle. paymentId=" + paymentId);
        return payment;
    }

    public List<Payment> getPaymentsForStudent(Student student) {
        ValidationUtils.requireNotNull(student, "Student cannot be null.");
        ValidationUtils.requireNotNull(student.getId(), "Student id cannot be null.");

        List<Payment> payments = paymentDao.findByStudentId(student.getId());
        logger.info("[DIAGNOSTIC] Student payments loaded from Oracle. studentId=" + student.getId()
                + ", count=" + payments.size());
        return payments;
    }

    public List<Payment> getUnpaidPaymentsForStudent(Student student) {
        ValidationUtils.requireNotNull(student, "Student cannot be null.");
        ValidationUtils.requireNotNull(student.getId(), "Student id cannot be null.");

        List<Payment> payments = paymentDao.findUnpaidByStudentId(student.getId());
        logger.info("[DIAGNOSTIC] Unpaid student payments loaded from Oracle. studentId=" + student.getId()
                + ", count=" + payments.size());
        return payments;
    }

    public List<Payment> getPaidPaymentsForStudent(Student student) {
        ValidationUtils.requireNotNull(student, "Student cannot be null.");
        ValidationUtils.requireNotNull(student.getId(), "Student id cannot be null.");

        List<Payment> payments = paymentDao.findPaidByStudentId(student.getId());
        logger.info("[DIAGNOSTIC] Paid student payments loaded from Oracle. studentId=" + student.getId()
                + ", count=" + payments.size());
        return payments;
    }

    public List<Payment> getOverduePayments(LocalDate currentDate) {
        ValidationUtils.requireNotNull(currentDate, "Current date cannot be null.");

        List<Payment> payments = paymentDao.findOverdue(currentDate);
        logger.info("[DIAGNOSTIC] Overdue payments loaded from Oracle. date=" + currentDate
                + ", count=" + payments.size());
        return payments;
    }

    public List<Payment> getOverduePaymentsForStudent(Student student, LocalDate currentDate) {
        ValidationUtils.requireNotNull(student, "Student cannot be null.");
        ValidationUtils.requireNotNull(student.getId(), "Student id cannot be null.");
        ValidationUtils.requireNotNull(currentDate, "Current date cannot be null.");

        List<Payment> payments = paymentDao.findOverdueByStudentId(student.getId(), currentDate);
        logger.info("[DIAGNOSTIC] Overdue student payments loaded from Oracle. studentId=" + student.getId()
                + ", date=" + currentDate + ", count=" + payments.size());
        return payments;
    }

    public List<Payment> getAllPayments() {
        List<Payment> payments = paymentDao.findAll();
        logger.info("[DIAGNOSTIC] All payments loaded from Oracle. count=" + payments.size());
        return payments;
    }

    private void auditSafely(Long userId, String actionName, String entityName, Long entityId, String details) {
        try {
            auditLogService.logEvent(userId, actionName, entityName, entityId, details);
        } catch (RuntimeException exception) {
            logger.warning("Audit log write failed: " + exception.getMessage());
        }
    }
}
