package pl.usos2.client.view.student;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pl.usos2.client.util.MockDataProvider;
import pl.usos2.server.model.enumtype.RequestStatus;
import pl.usos2.server.model.enumtype.RequestType;
import pl.usos2.server.model.request.Request;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Widok składania i przeglądania wniosków studenckich.
 * Obsługuje dynamiczną zmianę języka (i18n), pełną walidację pól formularza
 * oraz wyświetlanie historii w tabeli zintegrowanej z MockDataProvider.
 */
public class ApplicationsView extends VBox {

    // Komponenty UI wymagające aktualizacji po zmianie języka
    private final Label titleLabel;
    private final Label formTitleLabel;
    private final ComboBox<RequestType> typeCombo;
    private final TextArea contentArea;
    private final Button submitBtn;
    private final Label historyTitleLabel;

    // Tabela historyczna wyświetlająca złożone wnioski
    private final TableView<Request> historyTable;

    public ApplicationsView() {
        setPadding(new Insets(30));
        setSpacing(20);
        setStyle("-fx-background-color: #f8fafc;");

        // Główny nagłówek ekranu
        titleLabel = new Label();
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));

        // --- FORMULARZ ZGŁOSZENIOWY (Stylizacja Tailwind CSS style) ---
        VBox formContainer = new VBox(15);
        formContainer.setPadding(new Insets(20));
        formContainer.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 10, 0, 0, 2);");

        formTitleLabel = new Label();
        formTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        formTitleLabel.setStyle("-fx-text-fill: #1e293b;");

        // Wybór typu wniosku z enuma RequestType
        typeCombo = new ComboBox<>();
        typeCombo.setItems(FXCollections.observableArrayList(RequestType.values()));
        typeCombo.setMaxWidth(350);
        typeCombo.setStyle("-fx-background-color: #f1f5f9; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-radius: 6;");

        // Pole tekstowe na uzasadnienie wniosku
        contentArea = new TextArea();
        contentArea.setPrefHeight(120);
        contentArea.setStyle("-fx-control-inner-background: #f1f5f9; -fx-background-color: transparent; -fx-border-color: #cbd5e1; -fx-border-radius: 6;");

        // Przycisk wysyłania wniosku z obsługą zdarzenia i walidacją
        submitBtn = new Button();
        submitBtn.setFont(Font.font("System", FontWeight.BOLD, 14));
        submitBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 10 20 10 20;");

        // Efekty najechania myszką na przycisk akcji
        submitBtn.setOnMouseEntered(e -> submitBtn.setStyle("-fx-background-color: #1d4ed8; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 10 20 10 20;"));
        submitBtn.setOnMouseExited(e -> submitBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 10 20 10 20;"));

        submitBtn.setOnAction(e -> handleFormSubmit());

        formContainer.getChildren().addAll(formTitleLabel, typeCombo, contentArea, submitBtn);

        // --- LISTA ZGŁOSZONYCH WNIOSKÓW (HISTORIA W TABELI) ---
        historyTitleLabel = new Label();
        historyTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        historyTitleLabel.setStyle("-fx-text-fill: #334155; -fx-padding: 10 0 0 0;");

        // Definicja i konfiguracja tabeli historycznej
        historyTable = new TableView<>();
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        historyTable.setPrefHeight(250);
        historyTable.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #cbd5e1;");

        // Kolumna: Typ wniosku
        TableColumn<Request, String> typeCol = new TableColumn<>("Typ");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType().toString()));

        // Kolumna: Uzasadnienie (Treść)
        TableColumn<Request, String> contentCol = new TableColumn<>("Uzasadnienie");
        contentCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getContent()));

        // Kolumna: Status rozpatrzenia
        TableColumn<Request, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().toString()));

        // Kolumna: Data utworzenia wpisu
        TableColumn<Request, String> dateCol = new TableColumn<>("Data złożenia");
        dateCol.setCellValueFactory(data -> {
            LocalDateTime dateTime = data.getValue().getCreatedAt();
            if (dateTime != null) {
                return new SimpleStringProperty(dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }
            return new SimpleStringProperty("");
        });

        historyTable.getColumns().addAll(typeCol, contentCol, statusCol, dateCol);

        // Podpięcie reaktywnej listy wniosków z globalnego MockDataProvider
        historyTable.setItems(MockDataProvider.requests);

        getChildren().addAll(titleLabel, formContainer, historyTitleLabel, historyTable);

        // Pierwsze tłumaczenie interfejsu przy inicjalizacji
        refreshLocalization();

        // Nasłuchiwanie globalnej zmiany języka w aplikacji
        MockDataProvider.currentLocaleProperty().addListener((obs, oldLocale, newLocale) -> refreshLocalization());
    }

    /**
     * Obsługuje proces wysyłania i walidacji formularza nowego wniosku.
     */
    private void handleFormSubmit() {
        RequestType selectedType = typeCombo.getValue();
        String contentText = contentArea.getText();

        // Walidacja pól: sprawdzenie czy typ został wybrany oraz czy opis nie jest pusty
        if (selectedType == null || contentText == null || contentText.trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(MockDataProvider.i18n("alert_validation_title"));
            alert.setHeaderText(MockDataProvider.i18n("alert_validation_header"));
            alert.setContentText(MockDataProvider.i18n("alert_validation_content"));
            alert.showAndWait();
            return;
        }

        // Pobranie aktualnego studenta zalogowanego w systemie za pomocą MockDataProvider
        pl.usos2.server.model.user.Student currentStudent = MockDataProvider.students.stream()
                .filter(s -> s.getLastName().equals("Lytvyn"))
                .findFirst()
                .orElse(MockDataProvider.students.get(0));

        // Wyznaczenie nowego unikalnego ID dla wniosku bezpośrednio z publicznej listy requests
        long newId = MockDataProvider.requests.size() + 1L;

        // Tworzenie nowej instancji obiektu klasy modelowej Request
        Request newRequest = new Request(
                newId,
                currentStudent,
                selectedType,
                contentText.trim(),
                RequestStatus.SUBMITTED,
                LocalDateTime.now()
        );

        // Dodanie wniosku do globalnej listy demonstracyjnej (tabela odświeży się automatycznie)
        MockDataProvider.requests.add(newRequest);

        // Wyczyszczenie formularza po pomyślnym przesłaniu danych
        typeCombo.setValue(null);
        contentArea.clear();

        // Wyświetlenie komunikatu o sukcesie operacji
        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle(MockDataProvider.i18n("alert_success_title"));
        successAlert.setHeaderText(MockDataProvider.i18n("alert_success_header"));
        successAlert.setContentText(MockDataProvider.i18n("alert_success_content"));
        successAlert.showAndWait();
    }

    /**
     * Metoda aktualizująca wszystkie etykiety tekstowe na podstawie aktualnego języka.
     */
    private void refreshLocalization() {
        titleLabel.setText(MockDataProvider.i18n("requests_title_screen"));
        formTitleLabel.setText(MockDataProvider.i18n("new_request_label"));
        typeCombo.setPromptText(MockDataProvider.i18n("select_request_type"));
        contentArea.setPromptText(MockDataProvider.i18n("request_content_prompt"));
        submitBtn.setText(MockDataProvider.i18n("submit_btn_txt"));
        historyTitleLabel.setText(MockDataProvider.i18n("history_title_label"));

        // Dynamiczne tłumaczenie nagłówków kolumn w tabeli JavaFX
        boolean isEn = MockDataProvider.getCurrentLocale().getLanguage().equals("en");
        historyTable.getColumns().get(0).setText(isEn ? "Type" : "Typ");
        historyTable.getColumns().get(1).setText(isEn ? "Justification" : "Uzasadnienie");
        historyTable.getColumns().get(2).setText(isEn ? "Status" : "Status");
        historyTable.getColumns().get(3).setText(isEn ? "Submission Date" : "Data złożenia");
    }
}