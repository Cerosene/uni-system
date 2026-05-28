package pl.usos2.client.view.student;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.StringConverter;
import pl.usos2.client.util.DisplayTextFormatter;
import pl.usos2.client.util.MockDataProvider;
import pl.usos2.server.model.enumtype.RequestType;
import pl.usos2.server.model.request.Request;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.model.user.User;
import pl.usos2.server.service.request.RequestService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ApplicationsView extends VBox {

    private final Label titleLabel;
    private final Label formTitleLabel;
    private final ComboBox<RequestType> typeCombo;
    private final TextArea contentArea;
    private final Button submitBtn;
    private final Label historyTitleLabel;
    private final Student currentStudent;
    private final RequestService requestService;
    private final TableView<Request> historyTable;

    public ApplicationsView(User currentUser, RequestService requestService) {
        this.currentStudent = (Student) currentUser;
        this.requestService = requestService;
        setPadding(new Insets(30));
        setSpacing(20);
        setStyle("-fx-background-color: #f8fafc;");

        titleLabel = new Label();
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));

        VBox formContainer = new VBox(15);
        formContainer.setPadding(new Insets(20));
        formContainer.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 10, 0, 0, 2);");

        formTitleLabel = new Label();
        formTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        formTitleLabel.setStyle("-fx-text-fill: #1e293b;");

        typeCombo = new ComboBox<>();
        typeCombo.setItems(FXCollections.observableArrayList(RequestType.values()));
        typeCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(RequestType type) {
                return DisplayTextFormatter.formatRequestType(type);
            }

            @Override
            public RequestType fromString(String string) {
                return null;
            }
        });
        typeCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(RequestType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : DisplayTextFormatter.formatRequestType(item));
            }
        });
        typeCombo.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(RequestType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : DisplayTextFormatter.formatRequestType(item));
            }
        });
        typeCombo.setMaxWidth(350);
        typeCombo.setStyle("-fx-background-color: #f1f5f9; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-radius: 6;");

        contentArea = new TextArea();
        contentArea.setPrefHeight(120);
        contentArea.setStyle("-fx-control-inner-background: #f1f5f9; -fx-background-color: transparent; -fx-border-color: #cbd5e1; -fx-border-radius: 6;");

        submitBtn = new Button();
        submitBtn.setFont(Font.font("System", FontWeight.BOLD, 14));
        submitBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 10 20 10 20;");
        submitBtn.setOnMouseEntered(e -> submitBtn.setStyle("-fx-background-color: #1d4ed8; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 10 20 10 20;"));
        submitBtn.setOnMouseExited(e -> submitBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 10 20 10 20;"));
        submitBtn.setOnAction(e -> handleFormSubmit());

        formContainer.getChildren().addAll(formTitleLabel, typeCombo, contentArea, submitBtn);

        historyTitleLabel = new Label();
        historyTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        historyTitleLabel.setStyle("-fx-text-fill: #334155; -fx-padding: 10 0 0 0;");

        historyTable = new TableView<>();
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        historyTable.setPrefHeight(250);
        historyTable.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #cbd5e1;");

        TableColumn<Request, String> typeCol = new TableColumn<>("Typ");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(
                DisplayTextFormatter.formatRequestType(data.getValue().getType())
        ));

        TableColumn<Request, String> contentCol = new TableColumn<>("Uzasadnienie");
        contentCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getContent()));

        TableColumn<Request, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(
                DisplayTextFormatter.formatRequestStatus(data.getValue().getStatus())
        ));

        TableColumn<Request, String> dateCol = new TableColumn<>("Data złożenia");
        dateCol.setCellValueFactory(data -> {
            LocalDateTime dateTime = data.getValue().getCreatedAt();
            if (dateTime != null) {
                return new SimpleStringProperty(dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }
            return new SimpleStringProperty("");
        });

        historyTable.getColumns().addAll(typeCol, contentCol, statusCol, dateCol);
        historyTable.setItems(FXCollections.observableArrayList(requestService.getRequestsByStudent(currentStudent)));

        getChildren().addAll(titleLabel, formContainer, historyTitleLabel, historyTable);

        refreshLocalization();
        MockDataProvider.currentLocaleProperty().addListener((obs, oldLocale, newLocale) -> {
            refreshLocalization();
            refreshRequestsTable();
        });
    }

    private void handleFormSubmit() {
        RequestType selectedType = typeCombo.getValue();
        String contentText = contentArea.getText();

        if (selectedType == null || contentText == null || contentText.trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(MockDataProvider.i18n("alert_validation_title"));
            alert.setHeaderText(MockDataProvider.i18n("alert_validation_header"));
            alert.setContentText(MockDataProvider.i18n("alert_validation_content"));
            alert.showAndWait();
            return;
        }

        requestService.submitRequest(currentStudent, selectedType, contentText.trim());
        refreshRequestsTable();

        typeCombo.setValue(null);
        contentArea.clear();

        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle(MockDataProvider.i18n("alert_success_title"));
        successAlert.setHeaderText(MockDataProvider.i18n("alert_success_header"));
        successAlert.setContentText(MockDataProvider.i18n("alert_success_content"));
        successAlert.showAndWait();
    }

    private void refreshRequestsTable() {
        historyTable.setItems(FXCollections.observableArrayList(requestService.getRequestsByStudent(currentStudent)));
    }

    private void refreshLocalization() {
        titleLabel.setText(MockDataProvider.i18n("requests_title_screen"));
        formTitleLabel.setText(MockDataProvider.i18n("new_request_label"));
        typeCombo.setPromptText(MockDataProvider.i18n("select_request_type"));
        contentArea.setPromptText(MockDataProvider.i18n("request_content_prompt"));
        submitBtn.setText(MockDataProvider.i18n("submit_btn_txt"));
        historyTitleLabel.setText(MockDataProvider.i18n("history_title_label"));

        boolean isEn = MockDataProvider.getCurrentLocale().getLanguage().equals("en");
        historyTable.getColumns().get(0).setText(isEn ? "Type" : "Typ");
        historyTable.getColumns().get(1).setText(isEn ? "Justification" : "Uzasadnienie");
        historyTable.getColumns().get(2).setText(isEn ? "Status" : "Status");
        historyTable.getColumns().get(3).setText(isEn ? "Submission Date" : "Data złożenia");
    }
}
