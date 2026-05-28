package pl.usos2.server.service.message;

import pl.usos2.server.dao.message.JdbcMessageDao;
import pl.usos2.server.dao.message.MessageDao;
import pl.usos2.server.model.enumtype.MessageStatus;
import pl.usos2.server.model.request.Message;
import pl.usos2.server.model.user.User;
import pl.usos2.server.util.ValidationUtils;

import java.util.List;
import java.util.logging.Logger;

public class MessageService {
    private static final Logger logger = Logger.getLogger(MessageService.class.getName());
    private final MessageDao messageDao;

    public MessageService() {
        this(new JdbcMessageDao());
    }

    public MessageService(MessageDao messageDao) {
        this.messageDao = messageDao;
    }

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

        Message message = messageDao.save(sender.getId(), recipient.getId(), subject.trim(), content.trim());
        logger.info("Message sent from " + sender.getEmail() + " to " + recipient.getEmail());
        logger.info("[DIAGNOSTIC] Message persisted in Oracle. messageId=" + message.getId());
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

        Message persistedMessage = messageDao.findById(message.getId())
                .orElseThrow(() -> new IllegalArgumentException("Message not found."));

        if (persistedMessage.getStatus() == MessageStatus.READ) {
            logger.warning("Message is already marked as read.");
            throw new IllegalStateException("Message is already marked as read.");
        }

        Message updated = messageDao.markAsRead(message.getId());

        // Preserve previous behavior for existing UI objects passed by reference.
        message.setStatus(MessageStatus.READ);
        logger.info("Message marked as read by: " + reader.getEmail());
        logger.info("[DIAGNOSTIC] Message marked as READ in Oracle. messageId=" + updated.getId());
        return message;
    }

    public Message findById(Long messageId) {
        ValidationUtils.requireNotNull(messageId, "Message id cannot be null.");

        Message message = messageDao.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found."));
        logger.info("[DIAGNOSTIC] Message loaded from Oracle. messageId=" + messageId);
        return message;
    }

    public List<Message> getInbox(User recipient) {
        ValidationUtils.requireNotNull(recipient, "Recipient cannot be null.");
        ValidationUtils.requireNotNull(recipient.getId(), "Recipient id cannot be null.");

        List<Message> inbox = messageDao.findInboxByRecipientId(recipient.getId());
        logger.info("[DIAGNOSTIC] Inbox loaded from Oracle. recipientId=" + recipient.getId()
                + ", count=" + inbox.size());
        return inbox;
    }

    public List<Message> getUnreadInbox(User recipient) {
        ValidationUtils.requireNotNull(recipient, "Recipient cannot be null.");
        ValidationUtils.requireNotNull(recipient.getId(), "Recipient id cannot be null.");

        List<Message> unreadInbox = messageDao.findUnreadInboxByRecipientId(recipient.getId());
        logger.info("[DIAGNOSTIC] Unread inbox loaded from Oracle. recipientId=" + recipient.getId()
                + ", count=" + unreadInbox.size());
        return unreadInbox;
    }

    public List<Message> getSentMessages(User sender) {
        ValidationUtils.requireNotNull(sender, "Sender cannot be null.");
        ValidationUtils.requireNotNull(sender.getId(), "Sender id cannot be null.");

        List<Message> sentMessages = messageDao.findSentBySenderId(sender.getId());
        logger.info("[DIAGNOSTIC] Sent messages loaded from Oracle. senderId=" + sender.getId()
                + ", count=" + sentMessages.size());
        return sentMessages;
    }

    public List<Message> getAllMessages() {
        List<Message> messages = messageDao.findAll();
        logger.info("[DIAGNOSTIC] All messages loaded from Oracle. count=" + messages.size());
        return messages;
    }
}
