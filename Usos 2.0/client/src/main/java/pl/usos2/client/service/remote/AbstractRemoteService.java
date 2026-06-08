package pl.usos2.client.service.remote;

import pl.usos2.client.network.ApiClient;
import pl.usos2.client.session.ClientSession;

import java.util.List;
import java.util.Map;

abstract class AbstractRemoteService {
    protected final ApiClient apiClient;
    protected final ClientSession session;

    AbstractRemoteService(ApiClient apiClient, ClientSession session) {
        this.apiClient = apiClient;
        this.session = session;
    }

    protected Object send(String action, Map<String, Object> payload) {
        return apiClient.send(action, session.getToken(), payload);
    }

    protected Object send(String action) {
        return apiClient.send(action, session.getToken());
    }

    @SuppressWarnings("unchecked")
    protected <T> List<T> list(Object value) {
        return (List<T>) value;
    }
}
