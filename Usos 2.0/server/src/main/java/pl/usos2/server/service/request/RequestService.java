package pl.usos2.server.service.request;

import pl.usos2.server.model.enumtype.RequestStatus;
import pl.usos2.server.model.enumtype.RequestType;
import pl.usos2.server.model.request.Request;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.util.ValidationUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class RequestService {
    private static final Logger logger = Logger.getLogger(RequestService.class.getName());

    private final List<Request> requests = new ArrayList<>();
    private long nextRequestId = 1L;

    public Request submitRequest(Student student, RequestType type, String content) {
        ValidationUtils.requireNotNull(student, "Student cannot be null.");
        ValidationUtils.requireNotNull(student.getId(), "Student id cannot be null.");
        ValidationUtils.requireNotNull(type, "Request type cannot be null.");
        ValidationUtils.requireNotBlank(content, "Request content cannot be empty.");

        Request request = new Request(
                nextRequestId++,
                student,
                type,
                content.trim(),
                RequestStatus.SUBMITTED,
                LocalDateTime.now()
        );

        requests.add(request);
        logger.info("Submitted request of type " + type + " for student: " + student.getFullName());
        return request;
    }

    public void changeStatus(Request request, RequestStatus newStatus) {
        ValidationUtils.requireNotNull(request, "Request cannot be null.");
        ValidationUtils.requireNotNull(newStatus, "Status cannot be null.");

        RequestStatus currentStatus = request.getStatus();

        if (currentStatus == newStatus) {
            logger.warning("Request already has status: " + newStatus);
            throw new IllegalStateException("Request already has this status.");
        }

        if (!isTransitionAllowed(currentStatus, newStatus)) {
            logger.warning("Invalid request status transition: " + currentStatus + " -> " + newStatus);
            throw new IllegalStateException("Invalid request status transition.");
        }

        request.setStatus(newStatus);
        logger.info("Changed request status from " + currentStatus + " to " + newStatus);
    }

    public Request findById(Long requestId) {
        ValidationUtils.requireNotNull(requestId, "Request id cannot be null.");

        return requests.stream()
                .filter(request -> requestId.equals(request.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Request not found."));
    }

    public List<Request> getRequestsByStudent(Student student) {
        ValidationUtils.requireNotNull(student, "Student cannot be null.");
        ValidationUtils.requireNotNull(student.getId(), "Student id cannot be null.");

        return requests.stream()
                .filter(request -> request.getStudent().getId().equals(student.getId()))
                .toList();
    }

    public List<Request> getRequestsByStatus(RequestStatus status) {
        ValidationUtils.requireNotNull(status, "Status cannot be null.");

        return requests.stream()
                .filter(request -> request.getStatus() == status)
                .toList();
    }

    public List<Request> getRequestsByType(RequestType type) {
        ValidationUtils.requireNotNull(type, "Request type cannot be null.");

        return requests.stream()
                .filter(request -> request.getType() == type)
                .toList();
    }

    public List<Request> getPendingRequests() {
        return requests.stream()
                .filter(request ->
                        request.getStatus() == RequestStatus.SUBMITTED
                                || request.getStatus() == RequestStatus.IN_REVIEW)
                .toList();
    }

    public List<Request> getAllRequests() {
        return new ArrayList<>(requests);
    }

    private boolean isTransitionAllowed(RequestStatus currentStatus, RequestStatus newStatus) {
        return switch (currentStatus) {
            case SUBMITTED -> newStatus == RequestStatus.IN_REVIEW || newStatus == RequestStatus.REJECTED;
            case IN_REVIEW -> newStatus == RequestStatus.APPROVED || newStatus == RequestStatus.REJECTED;
            case APPROVED, REJECTED -> false;
        };
    }
}