package pl.usos2.server.service.maintenance;

import pl.usos2.server.dao.serviceticket.JdbcServiceTicketDao;
import pl.usos2.server.dao.serviceticket.ServiceTicketDao;
import pl.usos2.server.model.enumtype.ServiceTicketStatus;
import pl.usos2.server.model.service.ServiceTicket;
import pl.usos2.server.model.user.Administrator;
import pl.usos2.server.model.user.User;
import pl.usos2.server.service.audit.AuditLogService;
import pl.usos2.server.util.ValidationUtils;

import java.util.List;
import java.util.logging.Logger;

public class ServiceTicketService {
    private static final Logger logger = Logger.getLogger(ServiceTicketService.class.getName());
    private final ServiceTicketDao serviceTicketDao;
    private final AuditLogService auditLogService;

    public ServiceTicketService() {
        this(new JdbcServiceTicketDao(), AuditLogService.getInstance());
    }

    public ServiceTicketService(ServiceTicketDao serviceTicketDao) {
        this(serviceTicketDao, AuditLogService.getInstance());
    }

    public ServiceTicketService(ServiceTicketDao serviceTicketDao, AuditLogService auditLogService) {
        this.serviceTicketDao = serviceTicketDao;
        this.auditLogService = auditLogService;
    }

    public ServiceTicket createTicket(User reporter, String title, String description) {
        ValidationUtils.requireNotNull(reporter, "Reporter cannot be null.");
        ValidationUtils.requireNotNull(reporter.getId(), "Reporter id cannot be null.");
        ValidationUtils.requireNotBlank(title, "Title cannot be empty.");
        ValidationUtils.requireNotBlank(description, "Description cannot be empty.");

        ServiceTicket ticket = serviceTicketDao.save(reporter.getId(), title.trim(), description.trim());
        logger.info("Created service ticket: " + ticket.getTitle());
        logger.info("[DIAGNOSTIC] Service ticket persisted in Oracle. ticketId=" + ticket.getId());
        auditSafely(reporter.getId(), "SERVICE_TICKET_CREATED", "SERVICE_TICKETS", ticket.getId(),
                "Service ticket created. title=" + ticket.getTitle());
        return ticket;
    }

    public void assignTicket(ServiceTicket ticket, Administrator administrator) {
        ValidationUtils.requireNotNull(ticket, "Ticket cannot be null.");
        ValidationUtils.requireNotNull(administrator, "Administrator cannot be null.");
        ValidationUtils.requireNotNull(administrator.getId(), "Administrator id cannot be null.");

        ServiceTicket persistedTicket = findById(ticket.getId());

        if (persistedTicket.getStatus() == ServiceTicketStatus.CLOSED) {
            logger.warning("Cannot assign closed ticket: " + ticket.getTitle());
            throw new IllegalStateException("Closed ticket cannot be assigned.");
        }

        if (persistedTicket.getAssignedTo() != null
                && !persistedTicket.getAssignedTo().getId().equals(administrator.getId())) {
            logger.warning("Ticket already assigned to another administrator: " + ticket.getTitle());
            throw new IllegalStateException("Ticket is already assigned to another administrator.");
        }

        ServiceTicket updated = serviceTicketDao.assignToAdmin(ticket.getId(), administrator.getId(), ServiceTicketStatus.IN_PROGRESS);

        // Keep old behavior for JavaFX-bound object references.
        ticket.setAssignedTo(administrator);
        ticket.setStatus(ServiceTicketStatus.IN_PROGRESS);
        logger.info("Assigned ticket '" + ticket.getTitle() + "' to administrator: " + administrator.getFullName());
        logger.info("[DIAGNOSTIC] Service ticket assignment persisted in Oracle. ticketId=" + updated.getId());
    }

    public void reassignTicket(ServiceTicket ticket, Administrator newAdministrator) {
        ValidationUtils.requireNotNull(ticket, "Ticket cannot be null.");
        ValidationUtils.requireNotNull(newAdministrator, "Administrator cannot be null.");
        ValidationUtils.requireNotNull(newAdministrator.getId(), "Administrator id cannot be null.");

        ServiceTicket persistedTicket = findById(ticket.getId());

        if (persistedTicket.getStatus() == ServiceTicketStatus.CLOSED) {
            logger.warning("Cannot reassign closed ticket: " + ticket.getTitle());
            throw new IllegalStateException("Closed ticket cannot be reassigned.");
        }

        ServiceTicket updated = serviceTicketDao.assignToAdmin(ticket.getId(), newAdministrator.getId(), ServiceTicketStatus.IN_PROGRESS);

        // Keep old behavior for JavaFX-bound object references.
        ticket.setAssignedTo(newAdministrator);
        ticket.setStatus(ServiceTicketStatus.IN_PROGRESS);
        logger.info("Reassigned ticket '" + ticket.getTitle() + "' to administrator: " + newAdministrator.getFullName());
        logger.info("[DIAGNOSTIC] Service ticket reassignment persisted in Oracle. ticketId=" + updated.getId());
    }

    public void closeTicket(ServiceTicket ticket) {
        ValidationUtils.requireNotNull(ticket, "Ticket cannot be null.");

        ServiceTicket persistedTicket = findById(ticket.getId());

        if (persistedTicket.getStatus() == ServiceTicketStatus.CLOSED) {
            logger.warning("Cannot close ticket twice: " + ticket.getTitle());
            throw new IllegalStateException("Ticket is already closed.");
        }

        ServiceTicket updated = serviceTicketDao.updateStatus(ticket.getId(), ServiceTicketStatus.CLOSED);

        // Keep old behavior for JavaFX-bound object references.
        ticket.setStatus(ServiceTicketStatus.CLOSED);
        logger.info("Closed ticket: " + ticket.getTitle());
        logger.info("[DIAGNOSTIC] Service ticket status persisted in Oracle. ticketId=" + updated.getId()
                + ", status=" + updated.getStatus());
    }

    public void changeStatus(ServiceTicket ticket, ServiceTicketStatus newStatus) {
        ValidationUtils.requireNotNull(ticket, "Ticket cannot be null.");
        ValidationUtils.requireNotNull(newStatus, "Status cannot be null.");

        ServiceTicket persistedTicket = findById(ticket.getId());
        ServiceTicketStatus currentStatus = persistedTicket.getStatus();
        if (currentStatus == newStatus) {
            logger.warning("Ticket already has status: " + newStatus + ". ticketId=" + ticket.getId());
            throw new IllegalStateException("Ticket already has this status.");
        }

        ServiceTicket updated = serviceTicketDao.updateStatus(ticket.getId(), newStatus);
        ticket.setStatus(newStatus);

        logger.info("Changed ticket status from " + currentStatus + " to " + newStatus + ". ticketId=" + ticket.getId());
        logger.info("[DIAGNOSTIC] Service ticket status changed in Oracle. ticketId=" + updated.getId()
                + ", status=" + updated.getStatus());
    }

    public ServiceTicket findById(Long ticketId) {
        ValidationUtils.requireNotNull(ticketId, "Ticket id cannot be null.");

        ServiceTicket ticket = serviceTicketDao.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found."));
        logger.info("[DIAGNOSTIC] Service ticket loaded from Oracle. ticketId=" + ticketId);
        return ticket;
    }

    public ServiceTicket findByTitle(String title) {
        ValidationUtils.requireNotBlank(title, "Title cannot be empty.");

        ServiceTicket ticket = serviceTicketDao.findByTitle(title.trim())
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found."));
        logger.info("[DIAGNOSTIC] Service ticket loaded by title from Oracle. title=" + title.trim());
        return ticket;
    }

    public List<ServiceTicket> getTicketsByStatus(ServiceTicketStatus status) {
        ValidationUtils.requireNotNull(status, "Status cannot be null.");

        List<ServiceTicket> tickets = serviceTicketDao.findByStatus(status);
        logger.info("[DIAGNOSTIC] Service tickets by status loaded from Oracle. status=" + status
                + ", count=" + tickets.size());
        return tickets;
    }

    public List<ServiceTicket> getTicketsByReporter(User reporter) {
        ValidationUtils.requireNotNull(reporter, "Reporter cannot be null.");
        ValidationUtils.requireNotNull(reporter.getId(), "Reporter id cannot be null.");

        List<ServiceTicket> tickets = serviceTicketDao.findByReporterId(reporter.getId());
        logger.info("[DIAGNOSTIC] Service tickets by reporter loaded from Oracle. reporterId=" + reporter.getId()
                + ", count=" + tickets.size());
        return tickets;
    }

    public List<ServiceTicket> getTicketsAssignedTo(Administrator administrator) {
        ValidationUtils.requireNotNull(administrator, "Administrator cannot be null.");
        ValidationUtils.requireNotNull(administrator.getId(), "Administrator id cannot be null.");

        List<ServiceTicket> tickets = serviceTicketDao.findByAssignedAdminId(administrator.getId());
        logger.info("[DIAGNOSTIC] Service tickets assigned to admin loaded from Oracle. adminId="
                + administrator.getId() + ", count=" + tickets.size());
        return tickets;
    }

    public List<ServiceTicket> getAllTickets() {
        List<ServiceTicket> tickets = serviceTicketDao.findAll();
        logger.info("[DIAGNOSTIC] All service tickets loaded from Oracle. count=" + tickets.size());
        return tickets;
    }

    private void auditSafely(Long userId, String actionName, String entityName, Long entityId, String details) {
        try {
            auditLogService.logEvent(userId, actionName, entityName, entityId, details);
        } catch (RuntimeException exception) {
            logger.warning("Audit log write failed: " + exception.getMessage());
        }
    }
}
