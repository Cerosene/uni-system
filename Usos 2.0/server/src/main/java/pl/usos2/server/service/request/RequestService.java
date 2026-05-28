package pl.usos2.server.service.request;

import pl.usos2.server.dao.request.JdbcRequestDao;
import pl.usos2.server.dao.request.RequestDao;
import pl.usos2.server.model.enumtype.RequestStatus;
import pl.usos2.server.model.enumtype.RequestType;
import pl.usos2.server.model.request.Request;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.service.audit.AuditLogService;
import pl.usos2.server.util.ValidationUtils;

import java.util.List;
import java.util.logging.Logger;

public class RequestService {
    private static final Logger logger = Logger.getLogger(RequestService.class.getName());
    private final RequestDao requestDao;
    private final AuditLogService auditLogService;

    public RequestService() {
        this(new JdbcRequestDao(), AuditLogService.getInstance());
    }

    public RequestService(RequestDao requestDao) {
        this(requestDao, AuditLogService.getInstance());
    }

    public RequestService(RequestDao requestDao, AuditLogService auditLogService) {
        this.requestDao = requestDao;
        this.auditLogService = auditLogService;
    }

    public Request submitRequest(Student student, RequestType type, String content) {
        ValidationUtils.requireNotNull(student, "Student cannot be null.");
        ValidationUtils.requireNotNull(student.getId(), "Student id cannot be null.");
        ValidationUtils.requireNotNull(type, "Request type cannot be null.");
        ValidationUtils.requireNotBlank(content, "Request content cannot be empty.");

        Request request = requestDao.save(student.getId(), type, content.trim());
        logger.info("Submitted request of type " + type + " for student: " + student.getFullName());
        logger.info("[DIAGNOSTIC] Request persisted in Oracle. requestId=" + request.getId());
        return request;
    }

    public void changeStatus(Request request, RequestStatus newStatus) {
        ValidationUtils.requireNotNull(request, "Request cannot be null.");
        ValidationUtils.requireNotNull(newStatus, "Status cannot be null.");

        Request persisted = requestDao.findById(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("Request not found."));

        RequestStatus currentStatus = persisted.getStatus();

        if (currentStatus == newStatus) {
            logger.warning("Request already has status: " + newStatus);
            throw new IllegalStateException("Request already has this status.");
        }

        if (!isTransitionAllowed(currentStatus, newStatus)) {
            logger.warning("Invalid request status transition: " + currentStatus + " -> " + newStatus);
            throw new IllegalStateException("Invalid request status transition.");
        }

        Request updated = requestDao.updateStatus(request.getId(), newStatus);

        // Preserve old behavior for objects already bound in JavaFX tables.
        request.setStatus(newStatus);
        logger.info("Changed request status from " + currentStatus + " to " + newStatus);
        logger.info("[DIAGNOSTIC] Request status updated in Oracle. requestId=" + updated.getId());
        auditSafely(
                updated.getStudent() == null ? null : updated.getStudent().getId(),
                "REQUEST_STATUS_CHANGED",
                "APPLICATIONS",
                updated.getId(),
                "Status changed from " + currentStatus + " to " + newStatus
        );
    }

    public Request findById(Long requestId) {
        ValidationUtils.requireNotNull(requestId, "Request id cannot be null.");

        Request request = requestDao.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found."));
        logger.info("[DIAGNOSTIC] Request loaded from Oracle. requestId=" + requestId);
        return request;
    }

    public List<Request> getRequestsByStudent(Student student) {
        ValidationUtils.requireNotNull(student, "Student cannot be null.");
        ValidationUtils.requireNotNull(student.getId(), "Student id cannot be null.");

        List<Request> requests = requestDao.findByStudentId(student.getId());
        logger.info("[DIAGNOSTIC] Student requests loaded from Oracle. studentId=" + student.getId()
                + ", count=" + requests.size());
        return requests;
    }

    public List<Request> getRequestsByStatus(RequestStatus status) {
        ValidationUtils.requireNotNull(status, "Status cannot be null.");

        List<Request> requests = requestDao.findByStatus(status);
        logger.info("[DIAGNOSTIC] Requests by status loaded from Oracle. status=" + status
                + ", count=" + requests.size());
        return requests;
    }

    public List<Request> getRequestsByType(RequestType type) {
        ValidationUtils.requireNotNull(type, "Request type cannot be null.");

        List<Request> requests = requestDao.findByType(type);
        logger.info("[DIAGNOSTIC] Requests by type loaded from Oracle. type=" + type
                + ", count=" + requests.size());
        return requests;
    }

    public List<Request> getPendingRequests() {
        List<Request> requests = requestDao.findPending();
        logger.info("[DIAGNOSTIC] Pending requests loaded from Oracle. count=" + requests.size());
        return requests;
    }

    public List<Request> getAllRequests() {
        List<Request> requests = requestDao.findAll();
        logger.info("[DIAGNOSTIC] All requests loaded from Oracle. count=" + requests.size());
        return requests;
    }

    private boolean isTransitionAllowed(RequestStatus currentStatus, RequestStatus newStatus) {
        return switch (currentStatus) {
            case SUBMITTED -> newStatus == RequestStatus.IN_REVIEW || newStatus == RequestStatus.REJECTED;
            case IN_REVIEW -> newStatus == RequestStatus.APPROVED || newStatus == RequestStatus.REJECTED;
            case APPROVED, REJECTED -> false;
        };
    }

    private void auditSafely(Long userId, String actionName, String entityName, Long entityId, String details) {
        try {
            auditLogService.logEvent(userId, actionName, entityName, entityId, details);
        } catch (RuntimeException exception) {
            logger.warning("Audit log write failed: " + exception.getMessage());
        }
    }
}
