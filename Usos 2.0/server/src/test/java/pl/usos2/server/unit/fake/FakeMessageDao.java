package pl.usos2.server.unit.fake;

import pl.usos2.server.dao.message.MessageDao;
import pl.usos2.server.model.enumtype.MessageStatus;
import pl.usos2.server.model.request.Message;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.model.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class FakeMessageDao implements MessageDao {
    private final Map<Long, Message> messages = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public Message save(Long senderId, Long recipientId, String subject, String content) {
        Long id = nextId.getAndIncrement();
        Message message = new Message(
                id,
                createUser(senderId),
                createUser(recipientId),
                subject,
                content,
                LocalDateTime.now(),
                MessageStatus.SENT
        );
        messages.put(id, message);
        return message;
    }

    @Override
    public Message markAsRead(Long messageId) {
        Message message = findExisting(messageId);
        message.setStatus(MessageStatus.READ);
        return message;
    }

    @Override
    public Optional<Message> findById(Long messageId) {
        return Optional.ofNullable(messages.get(messageId));
    }

    @Override
    public List<Message> findInboxByRecipientId(Long recipientId) {
        return messages.values().stream()
                .filter(message -> message.getRecipient() != null && recipientId.equals(message.getRecipient().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Message> findUnreadInboxByRecipientId(Long recipientId) {
        return messages.values().stream()
                .filter(message -> message.getRecipient() != null
                        && recipientId.equals(message.getRecipient().getId())
                        && message.getStatus() == MessageStatus.SENT)
                .collect(Collectors.toList());
    }

    @Override
    public List<Message> findSentBySenderId(Long senderId) {
        return messages.values().stream()
                .filter(message -> message.getSender() != null && senderId.equals(message.getSender().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Message> findAll() {
        return new ArrayList<>(messages.values());
    }

    private Message findExisting(Long messageId) {
        Message message = messages.get(messageId);
        if (message == null) {
            throw new IllegalArgumentException("Message not found.");
        }
        return message;
    }

    private User createUser(Long userId) {
        return new Student(userId, "User", "User", "user" + userId + "@example.com", "password", "S" + userId,
                "Unknown", null);
    }
}
