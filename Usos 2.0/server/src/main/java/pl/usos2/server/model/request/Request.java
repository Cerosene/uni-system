package pl.usos2.server.model.request;

import pl.usos2.server.model.base.BaseEntity;
import pl.usos2.server.model.enumtype.RequestStatus;
import pl.usos2.server.model.enumtype.RequestType;
import pl.usos2.server.model.user.Student;

import java.time.LocalDateTime;

public class Request extends BaseEntity {
    private Student student;
    private RequestType type;
    private String content;
    private RequestStatus status;
    private LocalDateTime createdAt;

    public Request() {
    }

    public Request(Long id, Student student, RequestType type, String content, RequestStatus status, LocalDateTime createdAt) {
        super(id);
        this.student = student;
        this.type = type;
        this.content = content;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Student getStudent() {
        return student;
    }

    public RequestType getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }
}