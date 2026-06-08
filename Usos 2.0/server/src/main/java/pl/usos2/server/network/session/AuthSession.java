package pl.usos2.server.network.session;

import pl.usos2.server.model.user.User;

import java.io.Serializable;

public class AuthSession implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String token;
    private final User user;

    public AuthSession(String token, User user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }
}
