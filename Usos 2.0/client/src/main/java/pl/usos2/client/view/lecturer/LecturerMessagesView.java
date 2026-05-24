package pl.usos2.client.view.lecturer;

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
import pl.usos2.client.util.MockDataProvider;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Widok zarządzania wiadomościami i komunikacji ze studentami dla wykładowcy.
 * Wspiera pełną reaktywność i18n oraz polskie komentarze deweloperskie.
 */
public class LecturerMessagesView extends VBox {

    private static final MessageService messageService = new MessageService();
    private final Lecturer currentLecturer;

    // Komponenty UI wymagające aktualizacji językowej
    private final Label titleLabel;
    private final Label inboxLabel;
    private final Label contentLabel;
    private final Button refreshBtn;
    private final Button replyBtn;
    private final Label newMsgLabel;
    private final Button sendBtn;

    private ListView<Message> inboxListView;
    private TextArea messageContentView;
    private TextArea replyArea;
    private ComboBox<Student> studentCombo;
    private TextField subjectField;
    private TextArea messageArea;

    public LecturerMessagesView() {
        setPadding(new Insets(30));
        setSpacing(20);
        setStyle("-fx-background-color: #f8fafc;");

        currentLecturer = new Lecturer(2L, "Tomasz", "Nowak", "lecturer@uni.pl", "password123", "EMP201", "Dr.");

        // Inicjalizacja etykiet i przycisków
        titleLabel = new Label();
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));

        inboxLabel = new Label();
        contentLabel = new Label();
        newMsgLabel = new Label();

        refreshBtn = new Button();
        replyBtn = new Button();
        sendBtn = new Button();

        // Podział ekranu na dwie główne sekcje
        HBox mainContent = new HBox(30);
        VBox.setVgrow(mainContent, Priority.ALWAYS);

        VBox inboxSection = createInboxSection();
        HBox.setHgrow(inboxSection, Priority.ALWAYS);

        VBox formSection = createComposeSection();
        HBox.setHgrow(formSection, Priority.ALWAYS);

        mainContent.getChildren().addAll(inboxSection, formSection);
        getChildren().addAll(titleLabel, mainContent);

        // Ładowanie wiadomości na start i konfiguracja i18n
        refreshMessages();
        refreshLocalization();

        // Reaktywne odświeżanie tekstów przy zmianie języka aplikacji
        MockDataProvider.currentLocaleProperty().addListener((obs, oldLocale, newLocale) -> refreshLocalization());
    }

    /**
     * Tworzy sekcję skrzybki odbiorczej wykładowcy.
     */
    private VBox createInboxSection() {
        VBox layout = new VBox(10);

        inboxListView = new ListView<>();
        inboxListView.setPrefHeight(200);

        messageContentView = new TextArea();
        messageContentView.setEditable(false);
        messageContentView.setPrefHeight(150);

        replyArea = new TextArea();
        replyArea.setPrefHeight(100);

        refreshBtn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #334155;");
        refreshBtn.setOnAction(e -> refreshMessages());

        replyBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold;");
        replyBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold;");
        replyBtn.setOnAction(e -> {
            Message selected = inboxListView.getSelectionModel().getSelectedItem();
            String text = replyArea.getText();

            if (selected != null && !text.trim().isEmpty()) {
                // Wywołanie metody sendMessage bezpośrednio z parametrami (Nadawca, Odbiorca, Temat, Treść).
                // Serwis sam zajmie się utworzeniem obiektu biznesowego oraz przypisaniem czasu i statusu.
                messageService.sendMessage(
                        currentLecturer,                  // Nadawca: Wykładowca (Lecturer)
                        selected.getSender(),             // Odbiorca: Student (User), który napisał pierwotną wiadomość
                        "[RE] " + selected.getSubject(),  // Temat wiadomości z przedrostkiem odpowiedzi
                        text                              // Treść wprowadzona w polu tekstowym szybkiej odpowiedzi
                );

                replyArea.clear();
                showAlert(Alert.AlertType.INFORMATION, "alert_info_title", "msg_sent_success_msg");
            }
        });

        // Reakcja na kliknięcie wiadomości na liście
        inboxListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                messageContentView.setText(newVal.getContent());
            } else {
                messageContentView.clear();
            }
        });

        layout.getChildren().addAll(refreshBtn, inboxLabel, inboxListView, contentLabel, messageContentView, replyArea, replyBtn);
        return layout;
    }

    /**
     * Tworzy sekcję formularza nowej wiadomości do studenta.
     */
    private VBox createComposeSection() {
        VBox form = new VBox(12);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e2e8f0;");

        studentCombo = new ComboBox<>();
        studentCombo.setMaxWidth(Double.MAX_VALUE);

        // Dodanie przykładowych studentów do wyboru
        studentCombo.getItems().addAll(
                new Student(1003L, "Dmytro", "Lytvyn", "dmytro@uni.pl", "pass123", "320103", "Informatyka", Semester.THIRD),
                new Student(1004L, "Anna", "Zielińska", "anna@uni.pl", "pass456", "320104", "Informatyka", Semester.THIRD)
        );

        studentCombo.setConverter(new StringConverter<Student>() {
            @Override
            public String toString(Student s) {
                return s == null ? "" : s.getFirstName() + " " + s.getLastName() + " (" + s.getStudentNumber() + ")";
            }
            @Override
            public Student fromString(String string) { return null; }
        });

        subjectField = new TextField();
        messageArea = new TextArea();
        messageArea.setPrefHeight(180);

        sendBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold;");
        sendBtn.setMaxWidth(Double.MAX_VALUE);

        sendBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold;");
        sendBtn.setMaxWidth(Double.MAX_VALUE);
        sendBtn.setOnAction(e -> {
            Student rec = studentCombo.getValue();
            String text = messageArea.getText();
            String subject = subjectField.getText();

            if (rec == null || text.trim().isEmpty() || subject.trim().isEmpty()) {
                // Dodatkowe zabezpieczenie: informacja o konieczności uzupełnienia pól (wykorzystujemy i18n)
                showAlert(Alert.AlertType.WARNING, "alert_warn_title", "fill_all_fields_error");
                return;
            }

            // Wywołanie analogiczne do panelu studenta, przekazujące 4 parametry do logiki biznesowej serwisu
            messageService.sendMessage(
                    currentLecturer,     // Nadawca: Zalogowany wykładowca
                    rec,                 // Odbiorca: Wybrany z listy rozwijanej student
                    subject,             // Temat: Wpisany w formularzu temat wiadomości
                    text                 // Treść: Główna zawartość listu
            );

            subjectField.clear();
            messageArea.clear();
            studentCombo.setValue(null);
            showAlert(Alert.AlertType.INFORMATION, "alert_info_title", "msg_sent_success_msg");
        });

        form.getChildren().addAll(newMsgLabel, studentCombo, subjectField, messageArea, sendBtn);
        return form;
    }

    /**
     * Synchronizuje teksty interfejsu z aktualnym językiem aplikacji.
     */
    private void refreshLocalization() {
        titleLabel.setText(MockDataProvider.i18n("lecturer_msg_title"));
        refreshBtn.setText(MockDataProvider.i18n("lecturer_msg_refresh_btn"));
        inboxLabel.setText(MockDataProvider.i18n("lecturer_msg_inbox_lbl"));
        contentLabel.setText(MockDataProvider.i18n("lecturer_msg_content_lbl"));
        replyBtn.setText(MockDataProvider.i18n("lecturer_msg_reply_btn"));
        newMsgLabel.setText(MockDataProvider.i18n("lecturer_msg_new_lbl"));
        sendBtn.setText(MockDataProvider.i18n("lecturer_msg_send_btn"));

        subjectField.setPromptText(MockDataProvider.i18n("lecturer_msg_subject_prompt"));
        messageArea.setPromptText(MockDataProvider.i18n("lecturer_msg_text_prompt"));
        replyArea.setPromptText(MockDataProvider.i18n("lecturer_msg_reply_prompt"));
        studentCombo.setPromptText(MockDataProvider.i18n("lecturer_msg_student_prompt"));
    }

    private void refreshMessages() {
        if (inboxListView == null) return;
        inboxListView.getItems().clear();
        messageContentView.clear();

        List<Message> allMessages = messageService.getAllMessages();
        List<Message> lecturerInbox = allMessages.stream()
                .filter(m -> m.getRecipient() != null && m.getRecipient().getId().equals(currentLecturer.getId()))
                .collect(Collectors.toList());

        inboxListView.getItems().addAll(lecturerInbox);
    }

    private void showAlert(Alert.AlertType type, String titleKey, String contentKey) {
        Alert alert = new Alert(type);
        alert.setTitle(MockDataProvider.i18n(titleKey));
        alert.setHeaderText(null);
        alert.setContentText(MockDataProvider.i18n(contentKey));
        alert.showAndWait();
    }

    public static MessageService getSharedMessageService() {
        return messageService;
    }
}