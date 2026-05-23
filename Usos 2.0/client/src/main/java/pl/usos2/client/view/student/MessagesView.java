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

import java.util.List;
import java.util.stream.Collectors;

public class MessagesView extends VBox {

    // Obecny student z uprawnieniami
    private final Student currentStudent;

    private ListView<Message> inboxListView;
    private TextArea messageContentView;

    public MessagesView() {
        setPadding(new Insets(30));
        setSpacing(20);
        setStyle("-fx-background-color: #f8fafc;");

        currentStudent = new Student(1003L, "Dmytro", "Lytvyn", "dmytro@uni.pl", "pass123", "320103", "Informatyka", Semester.THIRD);

        Label title = new Label("Wiadomości");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));

        // Główny kontener zakładek
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        // --- ZAKŁADKA 1: WYSYŁANIE WIADOMOŚCI ---
        Tab sendTab = new Tab("Wyślij wiadomość");
        VBox sendForm = createSendForm();
        sendTab.setContent(sendForm);

        // --- ZAKŁADKA 2: WIADOMOŚCI PRZYCHODZĄCE ---
        Tab inboxTab = new Tab("Skrzynka odbiorcza");
        VBox inboxLayout = createInboxLayout();
        inboxTab.setContent(inboxLayout);

        // Odświeżamy listę wiadomości przychodzących po przejściu do zakładki „Wiadomości przychodzące”
        inboxTab.setOnSelectionChanged(e -> {
            if (inboxTab.isSelected()) {
                refreshStudentInbox();
            }
        });

        tabPane.getTabs().addAll(inboxTab, sendTab);

        getChildren().addAll(title, tabPane);
    }

    // Tworzenie formularza wysyłania wiadomości (zakładka 1)
    private VBox createSendForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 8; -fx-background-radius: 8;");

        ComboBox<Lecturer> lecturerCombo = new ComboBox<>();
        Lecturer drNowak = new Lecturer(2L, "Tomasz", "Nowak", "lecturer@uni.pl", "password123", "EMP201", "Dr.");
        Lecturer profKowalski = new Lecturer(3L, "Jan", "Kowalski", "j.kowalski@uni.pl", "password123", "EMP202", "Prof.");

        lecturerCombo.getItems().addAll(drNowak, profKowalski);
        lecturerCombo.setPromptText("Wybierz adresata (Prowadzącego)");
        lecturerCombo.setMaxWidth(Double.MAX_VALUE);

        lecturerCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Lecturer lecturer) {
                if (lecturer == null) return "";
                return lecturer.getAcademicTitle() + " " + lecturer.getLastName() + " (" + lecturer.getEmail() + ")";
            }
            @Override
            public Lecturer fromString(String string) { return null; }
        });

        TextField subjectField = new TextField();
        subjectField.setPromptText("Temat wiadomości");

        TextArea messageArea = new TextArea();
        messageArea.setPromptText("Treść wiadomości...");
        messageArea.setPrefHeight(200);

        Button sendBtn = new Button("Wyślij wiadomość");
        sendBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        sendBtn.setMaxWidth(Double.MAX_VALUE);

        sendBtn.setOnAction(e -> {
            Lecturer selectedLecturer = lecturerCombo.getValue();
            String subject = subjectField.getText().trim();
            String content = messageArea.getText().trim();

            if (selectedLecturer == null || subject.isEmpty() || content.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Uwaga", "Wszystkie pola muszą być wypełnione!");
                return;
            }

            LecturerMessagesView.getSharedMessageService().sendMessage(
                    currentStudent,
                    selectedLecturer,
                    subject,
                    content
            );

            subjectField.clear();
            messageArea.clear();
            lecturerCombo.setValue(null);

            showAlert(Alert.AlertType.INFORMATION, "Sukces", "Wiadomość została wysłana do prowadzącego!");
        });

        form.getChildren().addAll(new Label("Nowa wiadomość:"), lecturerCombo, subjectField, messageArea, sendBtn);
        return form;
    }

    // Tworzenie szablonu dla wiadomości przychodzących (zakładka 2)
    private VBox createInboxLayout() {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 8; -fx-background-radius: 8;");

        Button refreshBtn = new Button("🔄 Odśwież");
        refreshBtn.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> refreshStudentInbox());

        inboxListView = new ListView<>();
        inboxListView.setPrefHeight(180);

        // Sformatowanie komórek tak, aby było widoczne, od kogo pochodzi wiadomość (od wykładowcy)
        inboxListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Message msg, boolean empty) {
                super.updateItem(msg, empty);
                if (empty || msg == null) {
                    setText(null);
                } else {
                    String senderName = msg.getSender().getFirstName() + " " + msg.getSender().getLastName();

                    if (msg.getSender() instanceof Lecturer) {
                        senderName = ((Lecturer) msg.getSender()).getAcademicTitle() + " " + msg.getSender().getLastName();
                    }
                    setText("Od: " + senderName + " | Temat: " + msg.getSubject());
                }
            }
        });

        messageContentView = new TextArea();
        messageContentView.setEditable(false);
        messageContentView.setPromptText("Treść wiadomości pojawi się tutaj po wybraniu jej z listy...");
        messageContentView.setPrefHeight(150);

        // Słuchacz wyboru wiadomości z listy
        inboxListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                messageContentView.setText(newVal.getContent());
            } else {
                messageContentView.clear();
            }
        });

        layout.getChildren().addAll(
                refreshBtn,
                new Label("Otrzymane wiadomości:"), inboxListView,
                new Label("Treść wiadomości:"), messageContentView
        );

        return layout;
    }

    // Metoda filtrowania wiadomości, w której odbiorcą (Recipient) jest obecny student
    private void refreshStudentInbox() {
        inboxListView.getItems().clear();
        messageContentView.clear();

        MessageService service = LecturerMessagesView.getSharedMessageService();
        List<Message> allMessages = service.getAllMessages();

        // Zostawiamy tylko te wiadomości, które zostały napisane do TEGO studenta
        List<Message> studentInbox = allMessages.stream()
                .filter(m -> m.getRecipient() != null && m.getRecipient().getId().equals(currentStudent.getId()))
                .collect(Collectors.toList());

        inboxListView.getItems().addAll(studentInbox);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}