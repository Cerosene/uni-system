package pl.usos2.server.service.maintenance;

import pl.usos2.server.model.enumtype.ServiceTicketStatus;
import pl.usos2.server.model.service.ServiceTicket;
import pl.usos2.server.model.user.Administrator;
import pl.usos2.server.model.user.User;
import pl.usos2.server.util.ValidationUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class ServiceTicketService {
    private static final Logger logger = Logger.getLogger(ServiceTicketService.class.getName());

    private final List<ServiceTicket> tickets = new ArrayList<>();
    private long nextTicketId = 1L;

    public ServiceTicket createTicket(User reporter, String title, String description) {
        ValidationUtils.requireNotNull(reporter, "Reporter cannot be null.");
        ValidationUtils.requireNotNull(reporter.getId(), "Reporter id cannot be null.");
        ValidationUtils.requireNotBlank(title, "Title cannot be empty.");
        ValidationUtils.requireNotBlank(description, "Description cannot be empty.");

        ServiceTicket ticket = new ServiceTicket(
                nextTicketId++,
                reporter,
                title.trim(),
                description.trim(),
                ServiceTicketStatus.OPEN,
                LocalDateTime.now(),
                null
        );

        tickets.add(ticket);
        logger.info("Created service ticket: " + ticket.getTitle());
        return ticket;
    }

    public void assignTicket(ServiceTicket ticket, Administrator administrator) {
        ValidationUtils.requireNotNull(ticket, "Ticket cannot be null.");
        ValidationUtils.requireNotNull(administrator, "Administrator cannot be null.");
        ValidationUtils.requireNotNull(administrator.getId(), "Administrator id cannot be null.");

        if (ticket.getStatus() == ServiceTicketStatus.CLOSED) {
            logger.warning("Cannot assign closed ticket: " + ticket.getTitle());
            throw new IllegalStateException("Closed ticket cannot be assigned.");
        }

        if (ticket.getAssignedTo() != null && !ticket.getAssignedTo().getId().equals(administrator.getId())) {
            logger.warning("Ticket already assigned to another administrator: " + ticket.getTitle());
            throw new IllegalStateException("Ticket is already assigned to another administrator.");
        }

        ticket.setAssignedTo(administrator);
        ticket.setStatus(ServiceTicketStatus.IN_PROGRESS);
        logger.info("Assigned ticket '" + ticket.getTitle() + "' to administrator: " + administrator.getFullName());
    }

    public void reassignTicket(ServiceTicket ticket, Administrator newAdministrator) {
        ValidationUtils.requireNotNull(ticket, "Ticket cannot be null.");
        ValidationUtils.requireNotNull(newAdministrator, "Administrator cannot be null.");
        ValidationUtils.requireNotNull(newAdministrator.getId(), "Administrator id cannot be null.");

        if (ticket.getStatus() == ServiceTicketStatus.CLOSED) {
            logger.warning("Cannot reassign closed ticket: " + ticket.getTitle());
            throw new IllegalStateException("Closed ticket cannot be reassigned.");
        }

        ticket.setAssignedTo(newAdministrator);
        ticket.setStatus(ServiceTicketStatus.IN_PROGRESS);
        logger.info("Reassigned ticket '" + ticket.getTitle() + "' to administrator: " + newAdministrator.getFullName());
    }

    public void closeTicket(ServiceTicket ticket) {
        ValidationUtils.requireNotNull(ticket, "Ticket cannot be null.");

        if (ticket.getStatus() == ServiceTicketStatus.CLOSED) {
            logger.warning("Cannot close ticket twice: " + ticket.getTitle());
            throw new IllegalStateException("Ticket is already closed.");
        }

        ticket.setStatus(ServiceTicketStatus.CLOSED);
        logger.info("Closed ticket: " + ticket.getTitle());
    }

    public ServiceTicket findById(Long ticketId) {
        ValidationUtils.requireNotNull(ticketId, "Ticket id cannot be null.");

        Optional<ServiceTicket> ticketOptional = tickets.stream()
                .filter(ticket -> ticketId.equals(ticket.getId()))
                .findFirst();

        if (ticketOptional.isEmpty()) {
            throw new IllegalArgumentException("Ticket not found.");
        }

        return ticketOptional.get();
    }

    public ServiceTicket findByTitle(String title) {
        ValidationUtils.requireNotBlank(title, "Title cannot be empty.");

        Optional<ServiceTicket> ticketOptional = tickets.stream()
                .filter(ticket -> ticket.getTitle().equalsIgnoreCase(title.trim()))
                .findFirst();

        if (ticketOptional.isEmpty()) {
            throw new IllegalArgumentException("Ticket not found.");
        }

        return ticketOptional.get();
    }

    public List<ServiceTicket> getTicketsByStatus(ServiceTicketStatus status) {
        ValidationUtils.requireNotNull(status, "Status cannot be null.");

        return tickets.stream()
                .filter(ticket -> ticket.getStatus() == status)
                .toList();
    }

    public List<ServiceTicket> getTicketsByReporter(User reporter) {
        ValidationUtils.requireNotNull(reporter, "Reporter cannot be null.");
        ValidationUtils.requireNotNull(reporter.getId(), "Reporter id cannot be null.");

        return tickets.stream()
                .filter(ticket -> ticket.getReporter().getId().equals(reporter.getId()))
                .toList();
    }

    public List<ServiceTicket> getTicketsAssignedTo(Administrator administrator) {
        ValidationUtils.requireNotNull(administrator, "Administrator cannot be null.");
        ValidationUtils.requireNotNull(administrator.getId(), "Administrator id cannot be null.");

        return tickets.stream()
                .filter(ticket -> ticket.getAssignedTo() != null)
                .filter(ticket -> ticket.getAssignedTo().getId().equals(administrator.getId()))
                .toList();
    }

    public List<ServiceTicket> getAllTickets() {
        return new ArrayList<>(tickets);
    }
}