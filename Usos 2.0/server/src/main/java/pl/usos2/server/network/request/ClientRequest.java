package pl.usos2.server.network.request;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ClientRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String action;
    private final String sessionToken;
    private final Map<String, Object> payload;

    public ClientRequest(String action, String sessionToken, Map<String, Object> payload) {
        this.action = action;
        this.sessionToken = sessionToken;
        this.payload = payload == null ? Collections.emptyMap() : new HashMap<>(payload);
    }

    public static ClientRequest of(String action, String sessionToken, Map<String, Object> payload) {
        return new ClientRequest(action, sessionToken, payload);
    }

    public String getAction() {
        return action;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public Map<String, Object> getPayload() {
        return Collections.unmodifiableMap(payload);
    }

    public Object get(String key) {
        return payload.get(key);
    }
}
