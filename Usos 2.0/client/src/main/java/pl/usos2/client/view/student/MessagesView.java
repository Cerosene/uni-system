package pl.usos2.client.view.student;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.StringConverter;
import pl.usos2.server.model.user.Lecturer;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.model.enumtype.Semester;
import pl.usos2.server.model.request.Message;
import pl.usos2.server.service.message.MessageService;
import pl.usos2.client.view.lecturer.LecturerMessagesView;
import pl.usos2.client.util.MockDataProvider;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Widok obsługi wiadomości studenckich.
 * Umożliwia wysyłanie wiadomości do wykładowców oraz przeglądanie skrzynki odbiorczej.
 * Obsługuje dynamiczną zmianę języka (i18n) i posiada komentarze po polsku.
 */
public class MessagesView extends VBox {

    private final Student currentStudent;

    // Komponenty UI wymagające lokalizacji
    private final Label titleLabel;
    private final Label tabNewMessageLabel;
    private final Label selectLecturerLabel;
    private final ComboBox<Lecturer> lecturerComboBox;
    private final Label messageContentLabel;
    private final TextArea messageArea;
    private final Button sendBtn;

    private final Label tabInboxLabel;
    private final Button refreshBtn;
    private final Label receivedMessagesLabel;
    private final ListView<Message> inboxListView;
    private final Label previewContentLabel;
    private final TextArea messageContentView;

    public MessagesView() {
        setPadding(new Insets(30));
        setSpacing(20);
        setStyle("-fx-background-color: #f8fafc;");

        // Dane demonstracyjne bieżącego studenta
        currentStudent = new Student(1003L, "Dmytro", "Lytvyn", "dmytro@uni.pl", "pass123", "320103", "Informatyka", Semester.THIRD);

        // Główny nagłówek ekranu
        titleLabel = new Label();
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));

        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: transparent;");

        // --- ZAKŁADKA 1: NOWA WIADOMOŚĆ ---
        Tab writeTab = new Tab();
        writeTab.setClosable(false);
        tabNewMessageLabel = new Label();
        writeTab.setGraphic(tabNewMessageLabel);

        VBox writeLayout = new VBox(15);
        writeLayout.setPadding(new Insets(20));
        writeLayout.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8;");

        selectLecturerLabel = new Label();
        selectLecturerLabel.setStyle("-fx-font-weight: bold;");

        lecturerComboBox = new ComboBox<>();
        lecturerComboBox.setMaxWidth(Double.MAX_VALUE);

        // Dane testowe wykładowców
        lecturerComboBox.getItems().addAll(
                new Lecturer(2L, "Tomasz", "Nowak", "t.nowak@uni.pl", "", "EMP201", "Dr."),
                new Lecturer(5L, "Maria", "Kowalska", "m.kowalska@uni.pl", "", "EMP202", "Prof.")
        );

        // --- POPRAWKA DLA STRINGCONVERTER Z UŻYCIEM GETACADEMICTITLE() ---
        lecturerComboBox.setConverter(new StringConverter<Lecturer>() {
            @Override
            public String toString(Lecturer l) {
                // Jeśli obiekt jest nullem, zwracamy pusty ciąg znaków
                if (l == null) {
                    return "";
                }

                // Pobieramy tytuł naukowy z poprawnej metody getAcademicTitle()
                String title = l.getAcademicTitle() != null ? l.getAcademicTitle() : "";

                // Łączymy tytuł naukowy, imię oraz nazwisko wykładowcy w jeden czytelny tekst
                return (title + " " + l.getFirstName() + " " + l.getLastName()).trim();
            }

            @Override
            public Lecturer fromString(String string) {
                // Mapowanie odwrotne ze String na obiekt nie jest używane w tym widoku
                return null;
            }
        });

        messageContentLabel = new Label();
        messageContentLabel.setStyle("-fx-font-weight: bold;");

        messageArea = new TextArea();
        messageArea.setPrefHeight(150);

        sendBtn = new Button();
        sendBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 6;");

        // --- POPRAWKA METODY WYSYŁANIA WIADOMOŚCI ---
        sendBtn.setOnAction(e -> {
            Lecturer selected = lecturerComboBox.getValue();
            String text = messageArea.getText();

            if (selected == null || text.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, MockDataProvider.i18n("alert_warn_title"), MockDataProvider.i18n("fill_all_fields_error"));
                return;
            }

            MessageService service = LecturerMessagesView.getSharedMessageService();

            // Definiujemy temat wiadomości (wymagany czwarty parametr w modelu serwerowym)
            String subject = "Wiadomość od: " + currentStudent.getFirstName() + " " + currentStudent.getLastName();

            // Przekazujemy pełne 4 parametry: nadawca, odbiorca, temat, treść
            service.sendMessage(currentStudent, selected, subject, text);

            showAlert(Alert.AlertType.INFORMATION, MockDataProvider.i18n("alert_info_title"), MockDataProvider.i18n("message_sent_success"));
            messageArea.clear();
            lecturerComboBox.setValue(null);
        });

        writeLayout.getChildren().addAll(selectLecturerLabel, lecturerComboBox, messageContentLabel, messageArea, sendBtn);
        writeTab.setContent(writeLayout);

        // --- ZAKŁADKA 2: SKRZYNKA ODBIORCZA ---
        Tab inboxTab = new Tab();
        inboxTab.setClosable(false);
        tabInboxLabel = new Label();
        inboxTab.setGraphic(tabInboxLabel);

        VBox inboxLayout = new VBox(15);
        inboxLayout.setPadding(new Insets(20));
        inboxLayout.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8;");

        refreshBtn = new Button();
        refreshBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold;");
        refreshBtn.setOnAction(e -> refreshStudentInbox());

        receivedMessagesLabel = new Label();
        receivedMessagesLabel.setStyle("-fx-font-weight: bold;");

        inboxListView = new ListView<>();
        inboxListView.setPrefHeight(150);

        previewContentLabel = new Label();
        previewContentLabel.setStyle("-fx-font-weight: bold;");

        messageContentView = new TextArea();
        messageContentView.setEditable(false);
        messageContentView.setPrefHeight(120);

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

        // Pierwsza inicjalizacja tekstów i ładowanie danych
        refreshLocalization();
        refreshStudentInbox();

        // Rejestracja słuchacza zmian lokalizacji (i18n)
        MockDataProvider.currentLocaleProperty().addListener((obs, oldLocale, newLocale) -> refreshLocalization());
    }

    /**
     * Filtruje i odświeża listę wiadomości odebranych przez aktualnego studenta.
     */
    private void refreshStudentInbox() {
        inboxListView.getItems().clear();
        messageContentView.clear();

        MessageService service = LecturerMessagesView.getSharedMessageService();
        List<Message> allMessages = service.getAllMessages();

        List<Message> studentInbox = allMessages.stream()
                .filter(m -> m.getRecipient() != null && m.getRecipient().getId().equals(currentStudent.getId()))
                .collect(Collectors.toList());

        inboxListView.getItems().addAll(studentInbox);
    }

    /**
     * Pomocnicza metoda wyświetlająca okna komunikatów.
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Aktualizuje teksty na ekranie po zmianie języka aplikacji.
     */
    private void refreshLocalization() {
        titleLabel.setText(MockDataProvider.i18n("messages_title_main"));
        tabNewMessageLabel.setText(MockDataProvider.i18n("tab_new_message"));
        selectLecturerLabel.setText(MockDataProvider.i18n("select_lecturer_label"));
        lecturerComboBox.setPromptText(MockDataProvider.i18n("select_lecturer_prompt"));
        messageContentLabel.setText(MockDataProvider.i18n("message_content_label"));
        messageArea.setPromptText(MockDataProvider.i18n("message_area_prompt"));
        sendBtn.setText(MockDataProvider.i18n("send_message_btn"));

        tabInboxLabel.setText(MockDataProvider.i18n("tab_inbox"));
        refreshBtn.setText(MockDataProvider.i18n("refresh_btn"));
        receivedMessagesLabel.setText(MockDataProvider.i18n("received_messages_label"));
        previewContentLabel.setText(MockDataProvider.i18n("preview_content_label"));
    }
}