package pl.usos2.server.dao.serviceticket;

import pl.usos2.server.model.enumtype.ServiceTicketStatus;
import pl.usos2.server.model.service.ServiceTicket;

import java.util.List;
import java.util.Optional;

public interface ServiceTicketDao {
    ServiceTicket save(Long reporterUserId, String title, String description);

    ServiceTicket updateStatus(Long ticketId, ServiceTicketStatus status);

    ServiceTicket assignToAdmin(Long ticketId, Long adminId, ServiceTicketStatus status);

    Optional<ServiceTicket> findById(Long ticketId);

    Optional<ServiceTicket> findByTitle(String title);

    List<ServiceTicket> findByStatus(ServiceTicketStatus status);

    List<ServiceTicket> findByReporterId(Long reporterUserId);

    List<ServiceTicket> findByAssignedAdminId(Long adminId);

    List<ServiceTicket> findAll();
}

