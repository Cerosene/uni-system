package pl.usos2.client.service.remote;

import pl.usos2.client.network.ApiClient;
import pl.usos2.client.session.ClientSession;
import pl.usos2.server.model.enumtype.ServiceTicketStatus;
import pl.usos2.server.model.service.ServiceTicket;
import pl.usos2.server.model.user.Administrator;
import pl.usos2.server.model.user.User;
import pl.usos2.server.network.protocol.ApiAction;
import pl.usos2.server.service.maintenance.ServiceTicketService;

import java.util.List;

public class RemoteServiceTicketService extends ServiceTicketService {
    private final ApiClient apiClient;
    private final ClientSession session;

    public RemoteServiceTicketService(ApiClient apiClient, ClientSession session) {
        super();
        this.apiClient = apiClient;
        this.session = session;
    }

    @Override
    public ServiceTicket createTicket(User reporter, String title, String description) {
        return (ServiceTicket) apiClient.send(ApiAction.TICKET_CREATE, session.getToken(), apiClient.payload(
                "reporter", reporter, "title", title, "description", description
        ));
    }

    @Override
    public void assignTicket(ServiceTicket ticket, Administrator administrator) {
        apiClient.send(ApiAction.TICKET_ASSIGN, session.getToken(), apiClient.payload("ticket", ticket, "administrator", administrator));
        ticket.setAssignedTo(administrator);
        ticket.setStatus(ServiceTicketStatus.IN_PROGRESS);
    }

    @Override
    public void reassignTicket(ServiceTicket ticket, Administrator newAdministrator) {
        apiClient.send(ApiAction.TICKET_REASSIGN, session.getToken(), apiClient.payload("ticket", ticket, "administrator", newAdministrator));
        ticket.setAssignedTo(newAdministrator);
        ticket.setStatus(ServiceTicketStatus.IN_PROGRESS);
    }

    @Override
    public void closeTicket(ServiceTicket ticket) {
        apiClient.send(ApiAction.TICKET_CLOSE, session.getToken(), apiClient.payload("ticket", ticket));
        ticket.setStatus(ServiceTicketStatus.CLOSED);
    }

    @Override
    public void changeStatus(ServiceTicket ticket, ServiceTicketStatus newStatus) {
        apiClient.send(ApiAction.TICKET_CHANGE_STATUS, session.getToken(), apiClient.payload("ticket", ticket, "status", newStatus));
        ticket.setStatus(newStatus);
    }

    @Override
    public ServiceTicket findById(Long ticketId) {
        return (ServiceTicket) apiClient.send(ApiAction.TICKET_FIND_BY_ID, session.getToken(), apiClient.payload("ticketId", ticketId));
    }

    @Override
    public ServiceTicket findByTitle(String title) {
        return (ServiceTicket) apiClient.send(ApiAction.TICKET_FIND_BY_TITLE, session.getToken(), apiClient.payload("title", title));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ServiceTicket> getTicketsByStatus(ServiceTicketStatus status) {
        return (List<ServiceTicket>) apiClient.send(ApiAction.TICKET_LIST_STATUS, session.getToken(), apiClient.payload("status", status));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ServiceTicket> getTicketsByReporter(User reporter) {
        return (List<ServiceTicket>) apiClient.send(ApiAction.TICKET_LIST_REPORTER, session.getToken(), apiClient.payload("reporter", reporter));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ServiceTicket> getTicketsAssignedTo(Administrator administrator) {
        return (List<ServiceTicket>) apiClient.send(ApiAction.TICKET_LIST_ASSIGNED, session.getToken(), apiClient.payload("administrator", administrator));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ServiceTicket> getAllTickets() {
        return (List<ServiceTicket>) apiClient.send(ApiAction.TICKET_LIST_ALL, session.getToken());
    }
}
