package pl.usos2.server.unit.fake;

import pl.usos2.server.dao.audit.AuditLogDao;
import pl.usos2.server.model.audit.AuditLog;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class FakeAuditLogDao implements AuditLogDao {
    private final Map<Long, AuditLog> logs = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public AuditLog save(Long userId, String actionName, String entityName, Long entityId, String actionDetails) {
        Long id = nextId.getAndIncrement();
        AuditLog log = new AuditLog(id, userId, actionName, entityName, entityId, actionDetails, LocalDateTime.now());
        logs.put(id, log);
        return log;
    }

    @Override
    public List<AuditLog> findAll() {
        return new ArrayList<>(logs.values());
    }

    @Override
    public List<AuditLog> findByUserId(Long userId) {
        return logs.values().stream()
                .filter(log -> log.getUserId() != null && log.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<AuditLog> findByActionName(String actionName) {
        return logs.values().stream()
                .filter(log -> log.getActionName() != null && log.getActionName().equalsIgnoreCase(actionName))
                .collect(Collectors.toList());
    }

    @Override
    public List<AuditLog> findByDateRange(LocalDateTime fromInclusive, LocalDateTime toInclusive) {
        return logs.values().stream()
                .filter(log -> !log.getCreatedAt().isBefore(fromInclusive)
                        && !log.getCreatedAt().isAfter(toInclusive))
                .collect(Collectors.toList());
    }
}
