package pl.usos2.client.session;

import pl.usos2.server.model.user.User;

public class ClientSession {
    private String token;
    private User user;

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }

    public boolean isLoggedIn() {
        return token != null && !token.isBlank() && user != null;
    }

    public void update(String token, User user) {
        this.token = token;
        this.user = user;
    }

    public void clear() {
        this.token = null;
        this.user = null;
    }
}
