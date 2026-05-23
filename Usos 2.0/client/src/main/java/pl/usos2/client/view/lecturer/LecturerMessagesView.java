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

import java.util.List;
import java.util.stream.Collectors;

public class LecturerMessagesView extends VBox {

    // Статический singleton обслуживания сообщений
    private static final MessageService messageService = new MessageService();

    // Текущий преподаватель (dr Nowak, ID = 2)
    private final Lecturer currentLecturer;

    private ListView<Message> inboxListView;
    private TextArea messageContentView;
    private TextArea replyArea;

    public LecturerMessagesView() {
        setPadding(new Insets(30));
        setSpacing(20);
        setStyle("-fx-background-color: #f8fafc;");

        currentLecturer = new Lecturer(2L, "Tomasz", "Nowak", "lecturer@uni.pl", "password123", "EMP201", "Dr.");

        Label title = new Label("Panel Wiadomości Wykładowcy");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));

        // Główny kontener zakładek, podobnie jak w przypadku studenta
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        // --- ZAKŁADKA 1: PYTANIA I ODPOWIEDZI ---
        Tab inboxTab = new Tab("Skrzynka odbiorcza");
        VBox inboxLayout = createInboxLayout();
        inboxTab.setContent(inboxLayout);

        // --- ZAKŁADKA 2: NAPISZ NOWĄ WIADOMOŚĆ ---
        Tab sendTab = new Tab("Napisz wiadomość");
        VBox sendForm = createSendForm();
        sendTab.setContent(sendForm);

        // Automatyczna aktualizacja wiadomości przychodzących po powrocie do zakładki „Skrzynka odbiorcza”
        inboxTab.setOnSelectionChanged(e -> {
            if (inboxTab.isSelected()) {
                refreshMessages();
            }
        });

        tabPane.getTabs().addAll(inboxTab, sendTab);

        getChildren().addAll(title, tabPane);
    }

    // Układ zakładki wiadomości przychodzących (Zakładka 1)
    private VBox createInboxLayout() {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 8; -fx-background-radius: 8;");

        Button refreshBtn = new Button("🔄 Odśwież skrzynkę");
        refreshBtn.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> refreshMessages());

        inboxListView = new ListView<>();
        inboxListView.setPrefHeight(180);

        // Estetyczne wyświetlanie nazwy nadawcy (studenta) na liście
        inboxListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Message msg, boolean empty) {
                super.updateItem(msg, empty);
                if (empty || msg == null) {
                    setText(null);
                } else {
                    String senderInfo = msg.getSender().getFirstName() + " " + msg.getSender().getLastName();
                    if (msg.getSender() instanceof Student) {
                        senderInfo += " (Student, " + ((Student) msg.getSender()).getStudentNumber() + ")";
                    }
                    setText("Od: " + senderInfo + " | Temat: " + msg.getSubject());
                }
            }
        });

        messageContentView = new TextArea();
        messageContentView.setEditable(false);
        messageContentView.setPromptText("Treść wybranej wiadomości pojawi się tutaj...");
        messageContentView.setPrefHeight(120);

        // Wyświetlanie zawartości po dokonaniu wyboru
        inboxListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                messageContentView.setText(newVal.getContent());
            } else {
                messageContentView.clear();
            }
        });

        replyArea = new TextArea();
        replyArea.setPromptText("Wpisz treść odpowiedzi...");
        replyArea.setPrefHeight(100);

        Button sendReplyBtn = new Button("Wyślij odpowiedź");
        sendReplyBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        sendReplyBtn.setMaxWidth(Double.MAX_VALUE);

        sendReplyBtn.setOnAction(e -> {
            Message selectedMsg = inboxListView.getSelectionModel().getSelectedItem();
            String replyContent = replyArea.getText().trim();

            if (selectedMsg == null) {
                showAlert(Alert.AlertType.WARNING, "Uwaga", "Wybierz wiadomość, na którą chcesz odpowiedzieć!");
                return;
            }
            if (replyContent.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Uwaga", "Treść odpowiedzi nie może быть pusta!");
                return;
            }

            // Wysyłamy odpowiedź z powrotem
            messageService.sendMessage(
                    currentLecturer,
                    selectedMsg.getSender(),
                    "Re: " + selectedMsg.getSubject(),
                    replyContent
            );

            replyArea.clear();
            showAlert(Alert.AlertType.INFORMATION, "Sukces", "Odpowiedź została wysłana do " + selectedMsg.getSender().getFirstName() + "!");
        });

        // Wstępna inicjalizacja listy wiadomości
        refreshMessages();

        layout.getChildren().addAll(
                refreshBtn,
                new Label("Wiadomości przychodzące:"), inboxListView,
                new Label("Treść wiadomości:"), messageContentView,
                new Label("Szybka odpowiedź:"), replyArea,
                sendReplyBtn
        );

        return layout;
    }

    // Układ zakładki służącej do tworzenia nowej wiadomości dla studenta (Zakładka 2)
    private VBox createSendForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 8; -fx-background-radius: 8;");

        ComboBox<Student> studentCombo = new ComboBox<>();

        // Tworzymy studentów z Moko
        Student student1 = new Student(1001L, "Mateusz", "Lewandowski", "m.lewandowski@uni.pl", "pass", "320101", "Informatyka", Semester.THIRD);
        Student student2 = new Student(1002L, "Igor", "Sikora", "i.sikora@uni.pl", "pass", "320102", "Informatyka", Semester.THIRD);
        Student student3 = new Student(1003L, "Dmytro", "Lytvyn", "dmytro@uni.pl", "pass123", "320103", "Informatyka", Semester.THIRD);

        studentCombo.getItems().addAll(student1, student2, student3);
        studentCombo.setPromptText("Wybierz studenta (Adresata)");
        studentCombo.setMaxWidth(Double.MAX_VALUE);

        // Konwerter do wyświetlania imienia i nazwiska oraz numeru indeksu studenta w polu ComboBox
        studentCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Student student) {
                if (student == null) return "";
                return student.getFirstName() + " " + student.getLastName() + " (" + student.getStudentNumber() + ")";
            }
            @Override
            public Student fromString(String string) { return null; }
        });

        TextField subjectField = new TextField();
        subjectField.setPromptText("Temat wiadomości");

        TextArea messageArea = new TextArea();
        messageArea.setPromptText("Treść wiadomości do studenta...");
        messageArea.setPrefHeight(200);

        Button sendBtn = new Button("Wyślij wiadomość do studenta");
        sendBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        sendBtn.setMaxWidth(Double.MAX_VALUE);

        sendBtn.setOnAction(e -> {
            Student selectedStudent = studentCombo.getValue();
            String subject = subjectField.getText().trim();
            String content = messageArea.getText().trim();

            if (selectedStudent == null || subject.isEmpty() || content.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Uwaga", "Wszystkie pola muszą być wypełnione!");
                return;
            }

            // Wysyłamy za pośrednictwem serwisu globalnego
            messageService.sendMessage(
                    currentLecturer,
                    selectedStudent,
                    subject,
                    content
            );

            subjectField.clear();
            messageArea.clear();
            studentCombo.setValue(null);

            showAlert(Alert.AlertType.INFORMATION, "Sukces", "Wiadomość została pomyślnie wysłana do studenta!");
        });

        form.getChildren().addAll(new Label("Nowa wiadomość do studenta:"), studentCombo, subjectField, messageArea, sendBtn);
        return form;
    }

    // Metoda aktualizacji listy wiadomości przychodzących dla wykładowcy
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

    // Metoda pobierająca statycznego singletona służącego do komunikacji z ekranem studenckim
    public static MessageService getSharedMessageService() {
        return messageService;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}