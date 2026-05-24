package pl.usos2.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pl.usos2.client.view.auth.LoginView;
import pl.usos2.client.view.layout.MainLayout;
import pl.usos2.server.model.enumtype.UserRole;

public class MainApp extends Application {
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        showLogin();
        primaryStage.setTitle("University System USOS 2.0");
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    /**
     * Wyświetla widok logowania i wymusza zachowanie pełnego ekranu systemowego.
     */
    public void showLogin() {
        LoginView loginView = new LoginView(this);

        // Tworzymy obiekt Scene, przekazując nasz widok oraz domyślne wymiary bazowe
        Scene scene = new Scene(loginView, 1280, 800);
        primaryStage.setScene(scene);

        // Wymuszenie otwarcia okna logowania w trybie zmaksymalizowanym (pełne okno systemowe)
        primaryStage.setMaximized(true);

        // Zapobiega to ucięciu zawartości na niektórych systemach operacyjnych
        primaryStage.show();
    }

    public void showMainLayout(UserRole role) {
        MainLayout mainLayout = new MainLayout(role, this);
        primaryStage.getScene().setRoot(mainLayout);
    }

    public static void main(String[] args) {
        launch(args);
    }
}