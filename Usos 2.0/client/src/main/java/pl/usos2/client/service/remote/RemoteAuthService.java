package pl.usos2.client.service.remote;

import pl.usos2.client.network.ApiClient;
import pl.usos2.client.session.ClientSession;
import pl.usos2.server.model.enumtype.UserRole;
import pl.usos2.server.model.user.User;
import pl.usos2.server.network.protocol.ApiAction;
import pl.usos2.server.network.session.AuthSession;
import pl.usos2.server.service.auth.AuthService;

import java.util.List;

public class RemoteAuthService extends AuthService {
    private final ApiClient apiClient;
    private final ClientSession session;

    public RemoteAuthService(ApiClient apiClient, ClientSession session) {
        super();
        this.apiClient = apiClient;
        this.session = session;
    }

    @Override
    public User login(String email, String password) {
        AuthSession authSession = (AuthSession) apiClient.send(
                ApiAction.AUTH_LOGIN,
                null,
                apiClient.payload("email", email, "password", password)
        );
        session.update(authSession.getToken(), authSession.getUser());
        return authSession.getUser();
    }

    @Override
    public void logout(Long userId) {
        apiClient.send(ApiAction.AUTH_LOGOUT, session.getToken());
        session.clear();
    }

    @Override
    public User register(User user) {
        return (User) apiClient.send(ApiAction.AUTH_REGISTER, session.getToken(), apiClient.payload("user", user));
    }

    @Override
    public User updateBasicData(Long userId, String firstName, String lastName) {
        return (User) apiClient.send(ApiAction.USER_UPDATE_BASIC, session.getToken(), apiClient.payload(
                "userId", userId, "firstName", firstName, "lastName", lastName
        ));
    }

    @Override
    public User changeEmail(Long userId, String newEmail) {
        return (User) apiClient.send(ApiAction.USER_CHANGE_EMAIL, session.getToken(), apiClient.payload(
                "userId", userId, "newEmail", newEmail
        ));
    }

    @Override
    public User changePassword(Long userId, String currentPassword, String newPassword) {
        return (User) apiClient.send(ApiAction.USER_CHANGE_PASSWORD, session.getToken(), apiClient.payload(
                "userId", userId, "currentPassword", currentPassword, "newPassword", newPassword
        ));
    }

    @Override
    public User activateUser(Long userId) {
        return (User) apiClient.send(ApiAction.USER_ACTIVATE, session.getToken(), apiClient.payload("userId", userId));
    }

    @Override
    public User deactivateUser(Long userId) {
        return (User) apiClient.send(ApiAction.USER_DEACTIVATE, session.getToken(), apiClient.payload("userId", userId));
    }

    @Override
    public void deleteUser(Long userId) {
        apiClient.send(ApiAction.USER_DELETE, session.getToken(), apiClient.payload("userId", userId));
    }

    @Override
    public User findById(Long userId) {
        return (User) apiClient.send(ApiAction.USER_FIND_BY_ID, session.getToken(), apiClient.payload("userId", userId));
    }

    @Override
    public User findByEmail(String email) {
        return (User) apiClient.send(ApiAction.USER_FIND_BY_EMAIL, session.getToken(), apiClient.payload("email", email));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<User> getUsersByRole(UserRole role) {
        return (List<User>) apiClient.send(ApiAction.USER_LIST_BY_ROLE, session.getToken(), apiClient.payload("role", role));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<User> getAllUsers() {
        return (List<User>) apiClient.send(ApiAction.USER_LIST_ALL, session.getToken());
    }

    @Override
    public User changeRole(Long userId, UserRole role) {
        return (User) apiClient.send(ApiAction.USER_CHANGE_ROLE, session.getToken(), apiClient.payload(
                "userId", userId, "role", role
        ));
    }
}
