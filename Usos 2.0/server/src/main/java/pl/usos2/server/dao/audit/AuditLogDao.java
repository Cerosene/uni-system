package pl.usos2.server.dao.audit;

import pl.usos2.server.model.audit.AuditLog;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogDao {
    AuditLog save(Long userId, String actionName, String entityName, Long entityId, String actionDetails);

    List<AuditLog> findAll();

    List<AuditLog> findByUserId(Long userId);

    List<AuditLog> findByActionName(String actionName);

    List<AuditLog> findByDateRange(LocalDateTime fromInclusive, LocalDateTime toInclusive);
}
