package pl.usos2.server.dao.request;

import pl.usos2.server.model.enumtype.RequestStatus;
import pl.usos2.server.model.enumtype.RequestType;
import pl.usos2.server.model.request.Request;

import java.util.List;
import java.util.Optional;

public interface RequestDao {
    Request save(Long studentId, RequestType type, String content);

    Request updateStatus(Long requestId, RequestStatus newStatus);

    Optional<Request> findById(Long requestId);

    List<Request> findByStudentId(Long studentId);

    List<Request> findByStatus(RequestStatus status);

    List<Request> findByType(RequestType type);

    List<Request> findPending();

    List<Request> findAll();
}

