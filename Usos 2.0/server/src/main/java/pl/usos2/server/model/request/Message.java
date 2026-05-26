package pl.usos2.server.model.request;

import pl.usos2.server.model.base.BaseEntity;
import pl.usos2.server.model.enumtype.MessageStatus;
import pl.usos2.server.model.user.User;

import java.time.LocalDateTime;

public class Message extends BaseEntity {
    private User sender;
    private User recipient;
    private String subject;
    private String content;
    private LocalDateTime sentAt;
    private MessageStatus status;

    public Message() {
    }

    public Message(Long id, User sender, User recipient, String subject, String content,
                   LocalDateTime sentAt, MessageStatus status) {
        super(id);
        this.sender = sender;
        this.recipient = recipient;
        this.subject = subject;
        this.content = content;
        this.sentAt = sentAt;
        this.status = status;
    }

    public User getSender() {
        return sender;
    }

    public User getRecipient() {
        return recipient;
    }

    public String getSubject() {
        return subject;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        String senderName = sender != null ? sender.getFullName() : "System";
        String subjectText = subject != null && !subject.isBlank() ? subject : "Brak tematu";
        
        return "Od: " + senderName + " | Temat: " + subjectText;
    }
}