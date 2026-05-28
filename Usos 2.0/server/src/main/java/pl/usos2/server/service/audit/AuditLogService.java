package pl.usos2.server.service.audit;

import pl.usos2.server.dao.audit.AuditLogDao;
import pl.usos2.server.dao.audit.JdbcAuditLogDao;
import pl.usos2.server.model.audit.AuditLog;
import pl.usos2.server.util.ValidationUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

public class AuditLogService {
    private static final Logger logger = Logger.getLogger(AuditLogService.class.getName());
    private static final AuditLogService INSTANCE = new AuditLogService(new JdbcAuditLogDao());

    private final AuditLogDao auditLogDao;

    public static AuditLogService getInstance() {
        return INSTANCE;
    }

    public AuditLogService() {
        this(new JdbcAuditLogDao());
    }

    public AuditLogService(AuditLogDao auditLogDao) {
        this.auditLogDao = auditLogDao;
    }

    public AuditLog logEvent(Long userId, String actionName, String entityName, Long entityId, String actionDetails) {
        ValidationUtils.requireNotBlank(actionName, "Action name cannot be empty.");

        String normalizedActionName = actionName.trim().toUpperCase();
        String normalizedEntityName = entityName == null || entityName.isBlank() ? null : entityName.trim().toUpperCase();
        String normalizedDetails = actionDetails == null ? null : actionDetails.trim();

        AuditLog log = auditLogDao.save(userId, normalizedActionName, normalizedEntityName, entityId, normalizedDetails);
        logger.info("[DIAGNOSTIC] Audit event saved to Oracle. action=" + normalizedActionName + ", entityId=" + entityId);
        return log;
    }

    public List<AuditLog> getHistory() {
        List<AuditLog> history = auditLogDao.findAll();
        logger.info("[DIAGNOSTIC] Audit history loaded from Oracle. count=" + history.size());
        return history;
    }

    public List<AuditLog> getHistoryByUser(Long userId) {
        ValidationUtils.requireNotNull(userId, "User id cannot be null.");

        List<AuditLog> history = auditLogDao.findByUserId(userId);
        logger.info("[DIAGNOSTIC] Audit history by user loaded from Oracle. userId=" + userId + ", count=" + history.size());
        return history;
    }

    public List<AuditLog> getHistoryByAction(String actionName) {
        ValidationUtils.requireNotBlank(actionName, "Action name cannot be empty.");

        List<AuditLog> history = auditLogDao.findByActionName(actionName.trim().toUpperCase());
        logger.info("[DIAGNOSTIC] Audit history by action loaded from Oracle. action=" + actionName + ", count=" + history.size());
        return history;
    }

    public List<AuditLog> getHistoryByDate(LocalDateTime fromInclusive, LocalDateTime toInclusive) {
        ValidationUtils.requireNotNull(fromInclusive, "From date cannot be null.");
        ValidationUtils.requireNotNull(toInclusive, "To date cannot be null.");

        if (toInclusive.isBefore(fromInclusive)) {
            throw new IllegalArgumentException("To date cannot be earlier than from date.");
        }

        List<AuditLog> history = auditLogDao.findByDateRange(fromInclusive, toInclusive);
        logger.info("[DIAGNOSTIC] Audit history by date loaded from Oracle. from=" + fromInclusive
                + ", to=" + toInclusive + ", count=" + history.size());
        return history;
    }
}
