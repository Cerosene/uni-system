package pl.usos2.client.service.remote;

import pl.usos2.client.network.ApiClient;
import pl.usos2.client.session.ClientSession;
import pl.usos2.server.model.enumtype.RequestStatus;
import pl.usos2.server.model.enumtype.RequestType;
import pl.usos2.server.model.request.Request;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.network.protocol.ApiAction;
import pl.usos2.server.service.request.RequestService;

import java.util.List;

public class RemoteRequestService extends RequestService {
    private final ApiClient apiClient;
    private final ClientSession session;

    public RemoteRequestService(ApiClient apiClient, ClientSession session) {
        super();
        this.apiClient = apiClient;
        this.session = session;
    }

    @Override
    public Request submitRequest(Student student, RequestType type, String content) {
        return (Request) apiClient.send(ApiAction.REQUEST_SUBMIT, session.getToken(), apiClient.payload(
                "student", student, "type", type, "content", content
        ));
    }

    @Override
    public void changeStatus(Request request, RequestStatus newStatus) {
        apiClient.send(ApiAction.REQUEST_CHANGE_STATUS, session.getToken(), apiClient.payload("request", request, "status", newStatus));
        request.setStatus(newStatus);
    }

    @Override
    public Request findById(Long requestId) {
        return (Request) apiClient.send(ApiAction.REQUEST_FIND_BY_ID, session.getToken(), apiClient.payload("requestId", requestId));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Request> getRequestsByStudent(Student student) {
        return (List<Request>) apiClient.send(ApiAction.REQUEST_LIST_STUDENT, session.getToken(), apiClient.payload("student", student));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Request> getRequestsByStatus(RequestStatus status) {
        return (List<Request>) apiClient.send(ApiAction.REQUEST_LIST_STATUS, session.getToken(), apiClient.payload("status", status));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Request> getRequestsByType(RequestType type) {
        return (List<Request>) apiClient.send(ApiAction.REQUEST_LIST_TYPE, session.getToken(), apiClient.payload("type", type));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Request> getPendingRequests() {
        return (List<Request>) apiClient.send(ApiAction.REQUEST_LIST_PENDING, session.getToken());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Request> getAllRequests() {
        return (List<Request>) apiClient.send(ApiAction.REQUEST_LIST_ALL, session.getToken());
    }
}
