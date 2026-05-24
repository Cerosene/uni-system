package pl.usos2.client.view.student;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pl.usos2.client.util.MockDataProvider;

/**
 * Widok obsługujący zgłaszanie problemów technicznych i serwisowych przez studenta.
 * Zawiera uproszczony formularz zgłoszeniowy, pełną walidację pól z alertami JavaFX
 * oraz listę dotychczasowych zgłoszeń powiązaną z MockDataProvider.
 */
public class TicketsView extends VBox {

    // Komponenty UI wymagające dynamicznej aktualizacji języka (i18n)
    private final Label titleLabel;
    private final Label formTitleLabel;
    private final TextField titleField;
    private final ComboBox<String> categoryCombo;
    private final TextArea descriptionArea;
    private final Button submitBtn;
    private final Label historyTitleLabel;

    // Tabela wyświetlająca historię zgłoszeń serwisowych
    private final TableView<MockDataProvider.ServiceTicket> ticketsTable;

    public TicketsView() {
        setPadding(new Insets(30));
        setSpacing(20);
        setStyle("-fx-background-color: #f8fafc;");

        // Główny nagłówek ekranu zgłoszeń
        titleLabel = new Label();
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #1e293b;");

        // --- FORMULARZ ZGŁOSZENIA SERWISOWEGO (Stylizacja Tailwind) ---
        VBox formContainer = new VBox(15);
        formContainer.setPadding(new Insets(20));
        formContainer.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 10, 0, 0, 2);");

        formTitleLabel = new Label();
        formTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        formTitleLabel.setStyle("-fx-text-fill: #334155;");

        // Pole: Tytuł zgłoszenia
        titleField = new TextField();
        titleField.setPrefHeight(38);
        titleField.setStyle("-fx-background-color: #f1f5f9; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-radius: 6;");

        // Pole: Kategoria problemu
        categoryCombo = new ComboBox<>();
        categoryCombo.setItems(FXCollections.observableArrayList("Dydaktyka", "Sprawy studenckie", "Pomoc techniczna / IT", "Inne"));
        categoryCombo.setMaxWidth(350);
        categoryCombo.setPrefHeight(38);
        categoryCombo.setStyle("-fx-background-color: #f1f5f9; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-radius: 6;");

        // Pole: Opis problemu
        descriptionArea = new TextArea();
        descriptionArea.setPrefHeight(100);
        descriptionArea.setStyle("-fx-control-inner-background: #f1f5f9; -fx-background-color: transparent; -fx-border-color: #cbd5e1; -fx-border-radius: 6;");

        // Przycisk wysyłania formularza
        submitBtn = new Button();
        submitBtn.setFont(Font.font("System", FontWeight.BOLD, 14));
        submitBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 10 20 10 20;");

        // Efekty wizualne hover dla przycisku
        submitBtn.setOnMouseEntered(e -> submitBtn.setStyle("-fx-background-color: #1d4ed8; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 10 20 10 20;"));
        submitBtn.setOnMouseExited(e -> submitBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 10 20 10 20;"));

        submitBtn.setOnAction(e -> handleTicketSubmit());

        formContainer.getChildren().addAll(formTitleLabel, titleField, categoryCombo, descriptionArea, submitBtn);

        // --- HISTORIA ZGŁOSZEŃ (TABELA) ---
        historyTitleLabel = new Label();
        historyTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        historyTitleLabel.setStyle("-fx-text-fill: #334155; -fx-padding: 10 0 0 0;");

        ticketsTable = new TableView<>();
        ticketsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        ticketsTable.setPrefHeight(220);
        ticketsTable.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #cbd5e1;");

        // Konfiguracja kolumn tabeli na podstawie podklasy ServiceTicket z MockDataProvider
        TableColumn<MockDataProvider.ServiceTicket, String> titleCol = new TableColumn<>();
        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));

        TableColumn<MockDataProvider.ServiceTicket, String> catCol = new TableColumn<>();
        catCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));

        TableColumn<MockDataProvider.ServiceTicket, String> descCol = new TableColumn<>();
        descCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));

        TableColumn<MockDataProvider.ServiceTicket, String> statusCol = new TableColumn<>();
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

        // Dodajemy tylko potrzebne kolumny (bez priorytetu)
        ticketsTable.getColumns().addAll(titleCol, catCol, descCol, statusCol);

        // Podpięcie reaktywnej listy zgłoszeń z centralnego dostawcy danych
        ticketsTable.setItems(MockDataProvider.tickets);

        getChildren().addAll(titleLabel, formContainer, historyTitleLabel, ticketsTable);

        // Uruchomienie lokalizacji językowej
        refreshLocalization();
        MockDataProvider.currentLocaleProperty().addListener((obs, oldLocale, newLocale) -> refreshLocalization());
    }

    /**
     * Waliduje pola wejściowe i zapisuje nowe zgłoszenie serwisowe w systemie.
     */
    private void handleTicketSubmit() {
        String titleText = titleField.getText();
        String category = categoryCombo.getValue();
        String descriptionText = descriptionArea.getText();

        // Walidacja GUI: Tytuł i opis nie mogą być puste
        if (titleText == null || titleText.trim().isEmpty() || descriptionText == null || descriptionText.trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(MockDataProvider.i18n("alert_warn_title"));
            alert.setHeaderText(MockDataProvider.i18n("alert_validation_header"));
            alert.setContentText(MockDataProvider.i18n("fill_all_fields_error"));
            alert.showAndWait();
            return;
        }

        // Domyślna wartość dla ComboBoxa kategorii, jeśli użytkownik jej nie wybrał
        String finalCategory = (category != null) ? category : "Inne";

        // Generowanie nowego unikalnego ID zgłoszenia
        long newId = MockDataProvider.tickets.size() + 1L;

        // Utworzenie nowej instancji zgłoszenia serwisowego (przekazujemy "Średni" na sztywno do modelu, jeśli to wymagane przez konstruktor)
        MockDataProvider.ServiceTicket newTicket = new MockDataProvider.ServiceTicket(
                newId,
                titleText.trim(),
                finalCategory,
                descriptionText.trim(),
                "Średni",
                "Nowy"
        );

        // Dodanie do globalnej listy – tabela automatycznie odświeży swój widok
        MockDataProvider.tickets.add(newTicket);

        // Czyszczenie pól formularza po udanej operacji
        titleField.clear();
        categoryCombo.setValue(null);
        descriptionArea.clear();

        // Wyświetlenie okna informacyjnego JavaFX o sukcesie
        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle(MockDataProvider.i18n("alert_info_title"));
        successAlert.setHeaderText(null);
        successAlert.setContentText(MockDataProvider.i18n("request_success_msg"));
        successAlert.showAndWait();
    }

    /**
     * Tłumaczy wszystkie napisy i nagłówki kolumn w widoku na podstawie wybranego języka.
     */
    private void refreshLocalization() {
        boolean isEn = MockDataProvider.getCurrentLocale().getLanguage().equals("en");

        titleLabel.setText(MockDataProvider.i18n("tickets"));
        formTitleLabel.setText(isEn ? "Open a New Support Ticket:" : "Otwórz nowe zgłoszenie serwisowe:");
        titleField.setPromptText(isEn ? "Enter issue title..." : "Wpisz tytuł zgłoszenia...");
        categoryCombo.setPromptText(isEn ? "Select category..." : "Wybierz kategorię...");
        descriptionArea.setPromptText(isEn ? "Describe your problem in detail..." : "Opisz szczegółowo swój problem...");
        submitBtn.setText(isEn ? "Submit Ticket" : "Wyślij zgłoszenie");
        historyTitleLabel.setText(isEn ? "My Support Tickets History:" : "Historia Twoich zgłoszeń serwisowych:");

        // Tłumaczenie nagłówków kolumn tabeli
        ticketsTable.getColumns().get(0).setText(isEn ? "Title" : "Tytuł zgłoszenia");
        ticketsTable.getColumns().get(1).setText(isEn ? "Category" : "Kategoria");
        ticketsTable.getColumns().get(2).setText(isEn ? "Description" : "Opis problemu");
        ticketsTable.getColumns().get(3).setText(isEn ? "Status" : "Status");
    }
}