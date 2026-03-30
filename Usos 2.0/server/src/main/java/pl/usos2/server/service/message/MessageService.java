package pl.usos2.server.service.message;

import pl.usos2.server.model.enumtype.MessageStatus;
import pl.usos2.server.model.request.Message;
import pl.usos2.server.model.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageService {
    private final List<Message> messages = new ArrayList<>();

    public Message sendMessage(User sender, User recipient, String subject, String content) {
        if (sender == null || recipient == null) {
            throw new IllegalArgumentException("Sender and recipient cannot be null.");
        }
        if (sender == recipient) {
            throw new IllegalArgumentException("Sender and recipient cannot be the same user.");
        }
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Subject cannot be empty.");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content cannot be empty.");
        }

        Message message = new Message(null, sender, recipient, subject, content, LocalDateTime.now(), MessageStatus.SENT);
        messages.add(message);
        return message;
    }

    public List<Message> getAllMessages() {
        return new ArrayList<>(messages);
    }
}