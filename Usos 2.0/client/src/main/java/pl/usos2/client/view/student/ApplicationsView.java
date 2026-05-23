package pl.usos2.client.view.student;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ApplicationsView extends VBox {
    public ApplicationsView() {
        setPadding(new Insets(30));
        setSpacing(20);

        Label title = new Label("Składanie Wniosków");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));

        // --- FORMULARZ ZGŁOSZENIOWY ---
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e2e8f0;");

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("O stypendium", "O powtarzanie przedmiotu", "O urlop dziekański", "Inne");
        typeCombo.setPromptText("Wybierz rodzaj wniosku");
        typeCombo.setMaxWidth(Double.MAX_VALUE);

        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Uzasadnienie wniosku...");
        contentArea.setPrefHeight(150);

        Button submitBtn = new Button("Złóż wniosek");
        submitBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold;");
        submitBtn.setMaxWidth(Double.MAX_VALUE);

        // Obsługa zdarzeń kliknięcia (Logika Builder)
        submitBtn.setOnAction(e -> {
            Wniosek nowyWniosek = new Wniosek.WniosekBuilder()
                    .setTyp(typeCombo.getValue())
                    .setTresc(contentArea.getText())
                    .setData("2026-05-03")
                    .build();
            System.out.println("Wniosek wysłany!");
        });

        form.getChildren().addAll(new Label("Nowy wniosek:"), typeCombo, contentArea, submitBtn);

        // --- LISTA ZGŁOSZONYCH ---
        Label historyTitle = new Label("Moje wnioski:");
        historyTitle.setFont(Font.font("System", FontWeight.BOLD, 16));

        VBox historyList = new VBox(10);
        historyList.getChildren().add(new Label("Brak aktywnych wniosków."));

        getChildren().addAll(title, form, historyTitle, historyList);
    }
}