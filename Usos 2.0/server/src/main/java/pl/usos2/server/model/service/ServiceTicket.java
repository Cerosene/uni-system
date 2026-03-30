package pl.usos2.server.model.service;

import pl.usos2.server.model.base.BaseEntity;
import pl.usos2.server.model.enumtype.ServiceTicketStatus;
import pl.usos2.server.model.user.Administrator;
import pl.usos2.server.model.user.User;

import java.time.LocalDateTime;

public class ServiceTicket extends BaseEntity {
    private User reporter;
    private String title;
    private String description;
    private ServiceTicketStatus status;
    private LocalDateTime createdAt;
    private Administrator assignedTo;

    public ServiceTicket() {
    }

    public ServiceTicket(Long id, User reporter, String title, String description,
                         ServiceTicketStatus status, LocalDateTime createdAt, Administrator assignedTo) {
        super(id);
        this.reporter = reporter;
        this.title = title;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.assignedTo = assignedTo;
    }

    public User getReporter() {
        return reporter;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public ServiceTicketStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Administrator getAssignedTo() {
        return assignedTo;
    }

    public void setStatus(ServiceTicketStatus status) {
        this.status = status;
    }

    public void setAssignedTo(Administrator assignedTo) {
        this.assignedTo = assignedTo;
    }
}