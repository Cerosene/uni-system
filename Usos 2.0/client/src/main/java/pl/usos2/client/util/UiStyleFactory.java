package pl.usos2.client.util;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Klasa pomocnicza zapewniająca spójny wygląd (Design System) komponentów UI w aplikacji.
 */
public class UiStyleFactory {

    public static Label createHeader(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD, 26));
        label.setStyle("-fx-text-fill: #1e293b;"); // Ciemnogranatowy kolor czcionki
        return label;
    }

    public static Label createSubHeader(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.SEMI_BOLD, 16));
        label.setStyle("-fx-text-fill: #334155;");
        return label;
    }

    public static Button createPrimaryButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        return btn;
    }

    public static Button createDangerButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #e11d48; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        return btn;
    }
}