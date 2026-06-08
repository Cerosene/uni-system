package pl.usos2.client.service.remote;

import pl.usos2.client.network.ApiClient;
import pl.usos2.client.session.ClientSession;
import pl.usos2.server.model.finance.Payment;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.network.protocol.ApiAction;
import pl.usos2.server.service.finance.PaymentService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class RemotePaymentService extends PaymentService {
    private final ApiClient apiClient;
    private final ClientSession session;

    public RemotePaymentService(ApiClient apiClient, ClientSession session) {
        super();
        this.apiClient = apiClient;
        this.session = session;
    }

    @Override
    public Payment createPayment(Long id, Student student, BigDecimal amount, String title, LocalDate dueDate) {
        return (Payment) apiClient.send(ApiAction.PAYMENT_CREATE, session.getToken(), apiClient.payload(
                "paymentId", id, "student", student, "amount", amount, "title", title, "dueDate", dueDate
        ));
    }

    @Override
    public Payment markAsPaid(Long paymentId) {
        return (Payment) apiClient.send(ApiAction.PAYMENT_MARK_PAID, session.getToken(), apiClient.payload("paymentId", paymentId));
    }

    @Override
    public Payment markAsUnpaid(Long paymentId) {
        return (Payment) apiClient.send(ApiAction.PAYMENT_MARK_UNPAID, session.getToken(), apiClient.payload("paymentId", paymentId));
    }

    @Override
    public void removePayment(Long paymentId) {
        apiClient.send(ApiAction.PAYMENT_REMOVE, session.getToken(), apiClient.payload("paymentId", paymentId));
    }

    @Override
    public Payment findById(Long paymentId) {
        return (Payment) apiClient.send(ApiAction.PAYMENT_FIND_BY_ID, session.getToken(), apiClient.payload("paymentId", paymentId));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Payment> getPaymentsForStudent(Student student) {
        return (List<Payment>) apiClient.send(ApiAction.PAYMENT_LIST_STUDENT, session.getToken(), apiClient.payload("student", student));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Payment> getUnpaidPaymentsForStudent(Student student) {
        return (List<Payment>) apiClient.send(ApiAction.PAYMENT_LIST_UNPAID_STUDENT, session.getToken(), apiClient.payload("student", student));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Payment> getPaidPaymentsForStudent(Student student) {
        return (List<Payment>) apiClient.send(ApiAction.PAYMENT_LIST_PAID_STUDENT, session.getToken(), apiClient.payload("student", student));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Payment> getOverduePayments(LocalDate currentDate) {
        return (List<Payment>) apiClient.send(ApiAction.PAYMENT_LIST_OVERDUE, session.getToken(), apiClient.payload("currentDate", currentDate));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Payment> getOverduePaymentsForStudent(Student student, LocalDate currentDate) {
        return (List<Payment>) apiClient.send(ApiAction.PAYMENT_LIST_OVERDUE_STUDENT, session.getToken(), apiClient.payload(
                "student", student, "currentDate", currentDate
        ));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Payment> getAllPayments() {
        return (List<Payment>) apiClient.send(ApiAction.PAYMENT_LIST_ALL, session.getToken());
    }
}
