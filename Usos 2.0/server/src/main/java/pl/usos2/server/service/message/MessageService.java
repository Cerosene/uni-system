package pl.usos2.server.service.message;

import pl.usos2.server.model.enumtype.MessageStatus;
import pl.usos2.server.model.request.Message;
import pl.usos2.server.model.user.User;
import pl.usos2.server.util.ValidationUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MessageService {
    private static final Logger logger = Logger.getLogger(MessageService.class.getName());

    private final List<Message> messages = new ArrayList<>();
    private long nextMessageId = 1L;

    public Message sendMessage(User sender, User recipient, String subject, String content) {
        ValidationUtils.requireNotNull(sender, "Sender cannot be null.");
        ValidationUtils.requireNotNull(sender.getId(), "Sender id cannot be null.");
        ValidationUtils.requireNotNull(recipient, "Recipient cannot be null.");
        ValidationUtils.requireNotNull(recipient.getId(), "Recipient id cannot be null.");
        ValidationUtils.requireNotBlank(subject, "Subject cannot be empty.");
        ValidationUtils.requireNotBlank(content, "Content cannot be empty.");

        if (sender.getId().equals(recipient.getId())) {
            logger.warning("Cannot send message to the same user.");
            throw new IllegalArgumentException("Sender and recipient cannot be the same user.");
        }

        Message message = new Message(
                nextMessageId++,
                sender,
                recipient,
                subject.trim(),
                content.trim(),
                LocalDateTime.now(),
                MessageStatus.SENT
        );

        messages.add(message);
        logger.info("Message sent from " + sender.getEmail() + " to " + recipient.getEmail());
        return message;
    }

    public Message markAsRead(Message message, User reader) {
        ValidationUtils.requireNotNull(message, "Message cannot be null.");
        ValidationUtils.requireNotNull(reader, "Reader cannot be null.");
        ValidationUtils.requireNotNull(reader.getId(), "Reader id cannot be null.");

        if (!message.getRecipient().getId().equals(reader.getId())) {
            logger.warning("Only recipient can mark message as read.");
            throw new IllegalArgumentException("Only recipient can mark message as read.");
        }

        if (message.getStatus() == MessageStatus.READ) {
            logger.warning("Message is already marked as read.");
            throw new IllegalStateException("Message is already marked as read.");
        }

        message.setStatus(MessageStatus.READ);
        logger.info("Message marked as read by: " + reader.getEmail());
        return message;
    }

    public Message findById(Long messageId) {
        ValidationUtils.requireNotNull(messageId, "Message id cannot be null.");

        return messages.stream()
                .filter(message -> messageId.equals(message.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Message not found."));
    }

    public List<Message> getInbox(User recipient) {
        ValidationUtils.requireNotNull(recipient, "Recipient cannot be null.");
        ValidationUtils.requireNotNull(recipient.getId(), "Recipient id cannot be null.");

        return messages.stream()
                .filter(message -> message.getRecipient().getId().equals(recipient.getId()))
                .toList();
    }

    public List<Message> getUnreadInbox(User recipient) {
        ValidationUtils.requireNotNull(recipient, "Recipient cannot be null.");
        ValidationUtils.requireNotNull(recipient.getId(), "Recipient id cannot be null.");

        return messages.stream()
                .filter(message -> message.getRecipient().getId().equals(recipient.getId()))
                .filter(message -> message.getStatus() == MessageStatus.SENT)
                .toList();
    }

    public List<Message> getSentMessages(User sender) {
        ValidationUtils.requireNotNull(sender, "Sender cannot be null.");
        ValidationUtils.requireNotNull(sender.getId(), "Sender id cannot be null.");

        return messages.stream()
                .filter(message -> message.getSender().getId().equals(sender.getId()))
                .toList();
    }

    public List<Message> getAllMessages() {
        return new ArrayList<>(messages);
    }
}