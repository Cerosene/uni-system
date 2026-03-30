package pl.usos2.server.service.maintenance;

import pl.usos2.server.model.enumtype.ServiceTicketStatus;
import pl.usos2.server.model.service.ServiceTicket;
import pl.usos2.server.model.user.Administrator;
import pl.usos2.server.model.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceTicketService {
    private final List<ServiceTicket> tickets = new ArrayList<>();

    public ServiceTicket createTicket(User reporter, String title, String description) {
        if (reporter == null) {
            throw new IllegalArgumentException("Reporter cannot be null.");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be empty.");
        }

        ServiceTicket ticket = new ServiceTicket(
                null,
                reporter,
                title,
                description,
                ServiceTicketStatus.OPEN,
                LocalDateTime.now(),
                null
        );

        tickets.add(ticket);
        return ticket;
    }

    public void assignTicket(ServiceTicket ticket, Administrator administrator) {
        if (ticket == null || administrator == null) {
            throw new IllegalArgumentException("Ticket and administrator cannot be null.");
        }

        ticket.setAssignedTo(administrator);
        ticket.setStatus(ServiceTicketStatus.IN_PROGRESS);
    }

    public void closeTicket(ServiceTicket ticket) {
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket cannot be null.");
        }

        ticket.setStatus(ServiceTicketStatus.CLOSED);
    }

    public List<ServiceTicket> getAllTickets() {
        return new ArrayList<>(tickets);
    }
}