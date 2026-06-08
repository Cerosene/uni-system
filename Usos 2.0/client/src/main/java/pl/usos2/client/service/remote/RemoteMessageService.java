package pl.usos2.client.service.remote;

import pl.usos2.client.network.ApiClient;
import pl.usos2.client.session.ClientSession;
import pl.usos2.server.model.request.Message;
import pl.usos2.server.model.user.User;
import pl.usos2.server.network.protocol.ApiAction;
import pl.usos2.server.service.message.MessageService;

import java.util.List;

public class RemoteMessageService extends MessageService {
    private final ApiClient apiClient;
    private final ClientSession session;

    public RemoteMessageService(ApiClient apiClient, ClientSession session) {
        super();
        this.apiClient = apiClient;
        this.session = session;
    }

    @Override
    public Message sendMessage(User sender, User recipient, String subject, String content) {
        return (Message) apiClient.send(ApiAction.MESSAGE_SEND, session.getToken(), apiClient.payload(
                "sender", sender, "recipient", recipient, "subject", subject, "content", content
        ));
    }

    @Override
    public Message markAsRead(Message message, User reader) {
        return (Message) apiClient.send(ApiAction.MESSAGE_MARK_READ, session.getToken(), apiClient.payload(
                "message", message, "reader", reader
        ));
    }

    @Override
    public Message findById(Long messageId) {
        return (Message) apiClient.send(ApiAction.MESSAGE_FIND_BY_ID, session.getToken(), apiClient.payload("messageId", messageId));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Message> getInbox(User recipient) {
        return (List<Message>) apiClient.send(ApiAction.MESSAGE_INBOX, session.getToken(), apiClient.payload("recipient", recipient));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Message> getUnreadInbox(User recipient) {
        return (List<Message>) apiClient.send(ApiAction.MESSAGE_UNREAD_INBOX, session.getToken(), apiClient.payload("recipient", recipient));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Message> getSentMessages(User sender) {
        return (List<Message>) apiClient.send(ApiAction.MESSAGE_SENT, session.getToken(), apiClient.payload("sender", sender));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Message> getAllMessages() {
        return (List<Message>) apiClient.send(ApiAction.MESSAGE_LIST_ALL, session.getToken());
    }
}
