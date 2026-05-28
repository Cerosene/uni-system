package pl.usos2.server.dao.message;

import pl.usos2.server.model.request.Message;

import java.util.List;
import java.util.Optional;

public interface MessageDao {
    Message save(Long senderId, Long recipientId, String subject, String content);

    Message markAsRead(Long messageId);

    Optional<Message> findById(Long messageId);

    List<Message> findInboxByRecipientId(Long recipientId);

    List<Message> findUnreadInboxByRecipientId(Long recipientId);

    List<Message> findSentBySenderId(Long senderId);

    List<Message> findAll();
}

