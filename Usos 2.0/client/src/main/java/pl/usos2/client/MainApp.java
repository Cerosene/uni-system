package pl.usos2.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pl.usos2.client.view.auth.LoginView;
import pl.usos2.client.view.layout.MainLayout;
import pl.usos2.server.config.ApplicationContext;
import pl.usos2.server.config.DemoDataInitializer;
import pl.usos2.server.model.user.User;

public class MainApp extends Application {

    private Stage primaryStage;
    private ApplicationContext context;
    private User currentUser;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        this.context = new ApplicationContext();
        DemoDataInitializer.initialize(context);

        primaryStage.setTitle("University System USOS 2.0");
        primaryStage.setMaximized(true);

        showLogin();

        primaryStage.show();
    }

    public void showLogin() {
        LoginView loginView = new LoginView(this, context.getAuthService());

        Scene scene = new Scene(loginView, 1280, 800);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
    }

    public void onLoginSuccess(User user) {
        this.currentUser = user;
        showMainLayout(user);
    }

    public void showMainLayout(User user) {
        MainLayout mainLayout = new MainLayout(user, context, this);
        primaryStage.getScene().setRoot(mainLayout);
    }

    public void logout() {
        if (currentUser != null) {
            context.getAuthService().logout(currentUser.getId());
            currentUser = null;
        }

        showLogin();
    }

    public ApplicationContext getContext() {
        return context;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
