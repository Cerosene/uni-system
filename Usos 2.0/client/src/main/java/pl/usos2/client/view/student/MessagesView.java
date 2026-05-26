package pl.usos2.client.view.student;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.StringConverter;
import pl.usos2.client.util.MockDataProvider;
import pl.usos2.server.model.enumtype.UserRole;
import pl.usos2.server.model.request.Message;
import pl.usos2.server.model.user.Lecturer;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.model.user.User;
import pl.usos2.server.service.auth.AuthService;
import pl.usos2.server.service.message.MessageService;

import java.time.LocalDateTime;

/**
 * Widok obsługi wiadomości studenckich dostosowany do spójnego wyglądu aplikacji.
 * Umożliwia przeglądanie skrzynki odbiorczej oraz wysyłanie nowych wiadomości z pełną walidacją.
 * Wykorzystuje centralny MockDataProvider i posiada wyłącznie polskie komentarze.
 */
public class MessagesView extends VBox {

    // Dane zalogowanego studenta
    private final Student currentStudent;
    private final MessageService messageService;
    private final AuthService authService;

    // Komponenty UI wymagające lokalizacji i18n
    private final Label titleLabel;
    private final Tab writeTab;
    private final Tab inboxTab;

    // Komponenty formularza nowej wiadomości
    private final Label selectLecturerLabel;
    private final ComboBox<Lecturer> lecturerComboBox;
    private final Label subjectLabel;
    private final TextField subjectField;
    private final Label messageContentLabel;
    private final TextArea messageArea;
    private final Button sendBtn;

    // Komponenty skrzynki odbiorczej
    private final Button refreshBtn;
    private final Label receivedMessagesLabel;
    private final ListView<Message> inboxListView;
    private final Label previewContentLabel;
    private final TextArea messageContentView;

    public MessagesView(User currentUser, MessageService messageService, AuthService authService) {
        setPadding(new Insets(30));
        setSpacing(20);
        setStyle("-fx-background-color: #f8fafc;");

        this.currentStudent = (Student) currentUser;
        this.messageService = messageService;
        this.authService = authService;

        // Główny nagłówek ekranu wiadomości
        titleLabel = new Label();
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #1e293b;");

        // TabPane stylizowany na spójny, nowoczesny komponent bez zbędnych obramowań
        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: transparent; -fx-tab-max-width: 200px;");

        // --- ZAKŁADKA 1: NAPISZ WIADOMOŚĆ ---
        writeTab = new Tab();
        writeTab.setClosable(false);

        VBox writeLayout = new VBox(15);
        writeLayout.setPadding(new Insets(20));
        writeLayout.setStyle("-fx-background-color: white; -fx-background-radius: 0 0 12 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 10, 0, 0, 2); -fx-border-color: #cbd5e1; -fx-border-width: 0 1 1 1; -fx-border-radius: 0 0 12 12;");

        selectLecturerLabel = new Label();
        selectLecturerLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        selectLecturerLabel.setStyle("-fx-text-fill: #475569;");

        lecturerComboBox = new ComboBox<>();
        lecturerComboBox.setMaxWidth(400);
        lecturerComboBox.setPrefHeight(38);
        lecturerComboBox.setStyle("-fx-background-color: #f1f5f9; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-radius: 6;");

        // Załadowanie unikalnych wykładowców z serwisu autoryzacji
        var lecturers = authService.getUsersByRole(UserRole.LECTURER).stream()
                .filter(user -> user instanceof Lecturer)
                .map(user -> (Lecturer) user)
                .toList();
        lecturerComboBox.setItems(FXCollections.observableArrayList(lecturers));

        // Konwerter do poprawnego wyświetlania tytułów naukowych i nazwisk wykładowców
        lecturerComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Lecturer l) {
                if (l == null) return "";
                String title = l.getAcademicTitle() != null ? l.getAcademicTitle() : "";
                return (title + " " + l.getFirstName() + " " + l.getLastName()).trim();
            }

            @Override
            public Lecturer fromString(String string) { return null; }
        });

        // Nowe pole walidacyjne: Temat wiadomości
        subjectLabel = new Label();
        subjectLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        subjectLabel.setStyle("-fx-text-fill: #475569;");

        subjectField = new TextField();
        subjectField.setPrefHeight(38);
        subjectField.setMaxWidth(600);
        subjectField.setStyle("-fx-background-color: #f1f5f9; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-radius: 6;");

        messageContentLabel = new Label();
        messageContentLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        messageContentLabel.setStyle("-fx-text-fill: #475569;");

        messageArea = new TextArea();
        messageArea.setPrefHeight(150);
        messageArea.setStyle("-fx-control-inner-background: #f1f5f9; -fx-background-color: transparent; -fx-border-color: #cbd5e1; -fx-border-radius: 6;");

        sendBtn = new Button();
        sendBtn.setFont(Font.font("System", FontWeight.BOLD, 14));
        sendBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 10 24;");

        // Efekty hover dla przycisku wysyłania
        sendBtn.setOnMouseEntered(e -> sendBtn.setStyle("-fx-background-color: #1d4ed8; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 10 24;"));
        sendBtn.setOnMouseExited(e -> sendBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 10 24;"));

        sendBtn.setOnAction(e -> handleSendMessage());

        writeLayout.getChildren().addAll(selectLecturerLabel, lecturerComboBox, subjectLabel, subjectField, messageContentLabel, messageArea, sendBtn);
        writeTab.setContent(writeLayout);

        // --- ZAKŁADKA 2: SKRZYNKA ODBIORCZA ---
        inboxTab = new Tab();
        inboxTab.setClosable(false);

        VBox inboxLayout = new VBox(15);
        inboxLayout.setPadding(new Insets(20));
        inboxLayout.setStyle("-fx-background-color: white; -fx-background-radius: 0 0 12 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 10, 0, 0, 2); -fx-border-color: #cbd5e1; -fx-border-width: 0 1 1 1; -fx-border-radius: 0 0 12 12;");

        refreshBtn = new Button();
        refreshBtn.setFont(Font.font("System", FontWeight.BOLD, 12));
        refreshBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8 16;");
        refreshBtn.setOnMouseEntered(e -> refreshBtn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8 16;"));
        refreshBtn.setOnMouseExited(e -> refreshBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8 16;"));
        refreshBtn.setOnAction(e -> refreshStudentInbox());

        receivedMessagesLabel = new Label();
        receivedMessagesLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        receivedMessagesLabel.setStyle("-fx-text-fill: #334155;");

        inboxListView = new ListView<>();
        inboxListView.setPrefHeight(180);
        inboxListView.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #cbd5e1;");

        // Mapowanie wyglądu pojedynczej komórki w liście wiadomości (Czytelny format nadawcy i tematu)
        inboxListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Message msg, boolean empty) {
                super.updateItem(msg, empty);
                if (empty || msg == null) {
                    setText(null);
                } else {
                    String senderName = msg.getSender() != null ? msg.getSender().getFirstName() + " " + msg.getSender().getLastName() : "System";
                    setText("Od: " + senderName + " | Temat: " + msg.getSubject());
                }
            }
        });

        previewContentLabel = new Label();
        previewContentLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        previewContentLabel.setStyle("-fx-text-fill: #334155;");

        messageContentView = new TextArea();
        messageContentView.setEditable(false);
        messageContentView.setPrefHeight(120);
        messageContentView.setStyle("-fx-control-inner-background: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 6;");

        // Podgląd wybranej wiadomości z listy
        inboxListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                messageContentView.setText(newVal.getContent());
            } else {
                messageContentView.clear();
            }
        });

        inboxLayout.getChildren().addAll(refreshBtn, receivedMessagesLabel, inboxListView, previewContentLabel, messageContentView);
        inboxTab.setContent(inboxLayout);

        tabPane.getTabs().addAll(writeTab, inboxTab);
        getChildren().addAll(titleLabel, tabPane);

        // Pierwsza inicjalizacja danych i napisów i18n
        refreshLocalization();
        refreshStudentInbox();

        // Rejestracja globalnego słuchacza zmian językowych
        MockDataProvider.currentLocaleProperty().addListener((obs, oldLocale, newLocale) -> refreshLocalization());
    }

    /**
     * Waliduje pola wejściowe (odbiorca, temat, treść) i zapisuje nową wiadomość w MockDataProvider.
     */
    private void handleSendMessage() {
        Lecturer selectedLecturer = lecturerComboBox.getValue();
        String subjectText = subjectField.getText();
        String bodyText = messageArea.getText();

        // Walidacja GUI: Żadne z pól (odbiorca, temat, treść) nie może być puste
        if (selectedLecturer == null || subjectText == null || subjectText.trim().isEmpty() || bodyText == null || bodyText.trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(MockDataProvider.i18n("alert_warn_title"));
            alert.setHeaderText(MockDataProvider.i18n("alert_validation_header"));
            alert.setContentText(MockDataProvider.i18n("fill_all_fields_error"));
            alert.showAndWait();
            return;
        }

        messageService.sendMessage(currentStudent, selectedLecturer, subjectText.trim(), bodyText.trim());
        refreshStudentInbox();

        lecturerComboBox.setValue(null);
        subjectField.clear();
        messageArea.clear();

        // Wyświetlenie komunikatu informacyjnego JavaFX o sukcesie
        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle(MockDataProvider.i18n("alert_info_title"));
        successAlert.setHeaderText(null);
        successAlert.setContentText(MockDataProvider.i18n("message_sent_success"));
        successAlert.showAndWait();
    }

    /**
     * Filtruje globalną listę wiadomości i ładuje do skrzynki odbiorczej tylko te, których adresatem jest bieżący student.
     */
    private void refreshStudentInbox() {
        inboxListView.getSelectionModel().clearSelection();
        messageContentView.clear();

        inboxListView.setItems(FXCollections.observableArrayList(messageService.getInbox(currentStudent)));
    }

    /**
     * Aktualizuje dynamicznie wszystkie etykiety oraz prompt-texty na podstawie wybranego języka aplikacji.
     */
    private void refreshLocalization() {
        boolean isEn = MockDataProvider.getCurrentLocale().getLanguage().equals("en");

        titleLabel.setText(MockDataProvider.i18n("messages_title_main"));
        writeTab.setText(MockDataProvider.i18n("tab_new_message"));
        inboxTab.setText(MockDataProvider.i18n("tab_inbox"));

        selectLecturerLabel.setText(MockDataProvider.i18n("select_lecturer_label"));
        lecturerComboBox.setPromptText(MockDataProvider.i18n("select_lecturer_prompt"));

        subjectLabel.setText(isEn ? "Message Subject:" : "Temat wiadomości:");
        subjectField.setPromptText(isEn ? "Enter message subject here..." : "Wpisz temat wiadomości tutaj...");

        messageContentLabel.setText(MockDataProvider.i18n("message_content_label"));
        messageArea.setPromptText(MockDataProvider.i18n("message_area_prompt"));
        sendBtn.setText(MockDataProvider.i18n("send_message_btn"));

        refreshBtn.setText(MockDataProvider.i18n("refresh_btn"));
        receivedMessagesLabel.setText(MockDataProvider.i18n("received_messages_label"));
        previewContentLabel.setText(MockDataProvider.i18n("preview_content_label"));
    }
}