package pl.usos2.server.service.request;

import pl.usos2.server.model.enumtype.RequestStatus;
import pl.usos2.server.model.enumtype.RequestType;
import pl.usos2.server.model.request.Request;
import pl.usos2.server.model.user.Student;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RequestService {
    private final List<Request> requests = new ArrayList<>();

    public Request submitRequest(Student student, RequestType type, String content) {
        if (student == null) {
            throw new IllegalArgumentException("Student cannot be null.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Request type cannot be null.");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Request content cannot be empty.");
        }

        Request request = new Request(null, student, type, content, RequestStatus.SUBMITTED, LocalDateTime.now());
        requests.add(request);
        return request;
    }

    public void changeStatus(Request request, RequestStatus newStatus) {
        if (request == null || newStatus == null) {
            throw new IllegalArgumentException("Request and status cannot be null.");
        }

        request.setStatus(newStatus);
    }

    public List<Request> getAllRequests() {
        return new ArrayList<>(requests);
    }
}