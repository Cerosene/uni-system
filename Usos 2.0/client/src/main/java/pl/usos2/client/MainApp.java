package pl.usos2.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pl.usos2.client.view.auth.LoginView;
import pl.usos2.client.view.layout.MainLayout;

public class MainApp extends Application {
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        showLogin();
        primaryStage.setTitle("University System USOS 2.0");
        primaryStage.show();
    }

    public void showLogin() {
        LoginView loginView = new LoginView(this);
        Scene scene = new Scene(loginView, 1280, 800);
        primaryStage.setScene(scene);
    }


    public void showMainLayout(UserRole role) {
        MainLayout mainLayout = new MainLayout(role, this);
        primaryStage.getScene().setRoot(mainLayout);
    }



    public static void main(String[] args) {
        launch(args);
    }
}