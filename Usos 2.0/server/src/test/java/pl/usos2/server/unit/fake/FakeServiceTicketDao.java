package pl.usos2.server.unit.fake;

import pl.usos2.server.dao.serviceticket.ServiceTicketDao;
import pl.usos2.server.model.enumtype.ServiceTicketStatus;
import pl.usos2.server.model.service.ServiceTicket;
import pl.usos2.server.model.user.Administrator;
import pl.usos2.server.model.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class FakeServiceTicketDao implements ServiceTicketDao {
    private final Map<Long, ServiceTicket> tickets = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public ServiceTicket save(Long reporterUserId, String title, String description) {
        Long id = nextId.getAndIncrement();
        ServiceTicket ticket = new ServiceTicket(
                id,
                createUser(reporterUserId),
                title,
                description,
                ServiceTicketStatus.OPEN,
                LocalDateTime.now(),
                null
        );
        tickets.put(id, ticket);
        return ticket;
    }

    @Override
    public ServiceTicket updateStatus(Long ticketId, ServiceTicketStatus status) {
        ServiceTicket ticket = findExisting(ticketId);
        ticket.setStatus(status);
        return ticket;
    }

    @Override
    public ServiceTicket assignToAdmin(Long ticketId, Long adminId, ServiceTicketStatus status) {
        ServiceTicket ticket = findExisting(ticketId);
        ticket.setAssignedTo(createAdministrator(adminId));
        ticket.setStatus(status);
        return ticket;
    }

    @Override
    public Optional<ServiceTicket> findById(Long ticketId) {
        return Optional.ofNullable(tickets.get(ticketId));
    }

    @Override
    public Optional<ServiceTicket> findByTitle(String title) {
        return tickets.values().stream()
                .filter(ticket -> ticket.getTitle() != null && ticket.getTitle().equalsIgnoreCase(title))
                .findFirst();
    }

    @Override
    public List<ServiceTicket> findByStatus(ServiceTicketStatus status) {
        return tickets.values().stream()
                .filter(ticket -> ticket.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceTicket> findByReporterId(Long reporterUserId) {
        return tickets.values().stream()
                .filter(ticket -> ticket.getReporter() != null && reporterUserId.equals(ticket.getReporter().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceTicket> findByAssignedAdminId(Long adminId) {
        return tickets.values().stream()
                .filter(ticket -> ticket.getAssignedTo() != null && adminId.equals(ticket.getAssignedTo().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceTicket> findAll() {
        return new ArrayList<>(tickets.values());
    }

    private ServiceTicket findExisting(Long ticketId) {
        ServiceTicket ticket = tickets.get(ticketId);
        if (ticket == null) {
            throw new IllegalArgumentException("Service ticket not found.");
        }
        return ticket;
    }

    private User createUser(Long userId) {
        return new Administrator(userId, "Admin", "Admin", "admin" + userId + "@example.com", "password", "ADM" + userId);
    }

    private Administrator createAdministrator(Long adminId) {
        return new Administrator(adminId, "Admin", "Admin", "admin" + adminId + "@example.com", "password", "ADM" + adminId);
    }
}
