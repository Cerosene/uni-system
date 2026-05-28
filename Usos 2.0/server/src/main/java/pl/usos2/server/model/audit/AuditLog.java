package pl.usos2.server.model.audit;

import pl.usos2.server.model.base.BaseEntity;

import java.time.LocalDateTime;

public class AuditLog extends BaseEntity {
    private Long userId;
    private String actionName;
    private String entityName;
    private Long entityId;
    private String actionDetails;
    private LocalDateTime createdAt;

    public AuditLog(Long id,
                    Long userId,
                    String actionName,
                    String entityName,
                    Long entityId,
                    String actionDetails,
                    LocalDateTime createdAt) {
        super(id);
        this.userId = userId;
        this.actionName = actionName;
        this.entityName = entityName;
        this.entityId = entityId;
        this.actionDetails = actionDetails;
        this.createdAt = createdAt;
    }

    public Long getUserId() {
        return userId;
    }

    public String getActionName() {
        return actionName;
    }

    public String getEntityName() {
        return entityName;
    }

    public Long getEntityId() {
        return entityId;
    }

    public String getActionDetails() {
        return actionDetails;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
