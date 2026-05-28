package pl.usos2.server.unit.fake;

import pl.usos2.server.dao.request.RequestDao;
import pl.usos2.server.model.enumtype.RequestStatus;
import pl.usos2.server.model.enumtype.RequestType;
import pl.usos2.server.model.request.Request;
import pl.usos2.server.model.user.Student;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class FakeRequestDao implements RequestDao {
    private final Map<Long, Request> requests = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public Request save(Long studentId, RequestType type, String content) {
        Long id = nextId.getAndIncrement();
        Request request = new Request(
                id,
                createStudent(studentId),
                type,
                content,
                RequestStatus.SUBMITTED,
                LocalDateTime.now()
        );
        requests.put(id, request);
        return request;
    }

    @Override
    public Request updateStatus(Long requestId, RequestStatus newStatus) {
        Request request = findExisting(requestId);
        request.setStatus(newStatus);
        return request;
    }

    @Override
    public Optional<Request> findById(Long requestId) {
        return Optional.ofNullable(requests.get(requestId));
    }

    @Override
    public List<Request> findByStudentId(Long studentId) {
        return requests.values().stream()
                .filter(request -> request.getStudent() != null && studentId.equals(request.getStudent().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Request> findByStatus(RequestStatus status) {
        return requests.values().stream()
                .filter(request -> request.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public List<Request> findByType(RequestType type) {
        return requests.values().stream()
                .filter(request -> request.getType() == type)
                .collect(Collectors.toList());
    }

    @Override
    public List<Request> findPending() {
        return requests.values().stream()
                .filter(request -> request.getStatus() == RequestStatus.SUBMITTED
                        || request.getStatus() == RequestStatus.IN_REVIEW)
                .collect(Collectors.toList());
    }

    @Override
    public List<Request> findAll() {
        return new ArrayList<>(requests.values());
    }

    private Request findExisting(Long requestId) {
        Request request = requests.get(requestId);
        if (request == null) {
            throw new IllegalArgumentException("Request not found.");
        }
        return request;
    }

    private Student createStudent(Long studentId) {
        return new Student(studentId, "Student", "Student", "student" + studentId + "@example.com",
                "password", "S" + studentId, "Unknown", null);
    }
}
