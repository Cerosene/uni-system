package pl.usos2.client.view.student;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pl.usos2.client.util.DisplayTextFormatter;
import pl.usos2.client.util.MockDataProvider;
import pl.usos2.server.model.service.ServiceTicket;
import pl.usos2.server.model.user.User;
import pl.usos2.server.service.maintenance.ServiceTicketService;

public class TicketsView extends VBox {

    private final User currentUser;
    private final ServiceTicketService serviceTicketService;

    private final Label titleLabel;
    private final Label formTitleLabel;
    private final TextField titleField;
    private final ComboBox<String> categoryCombo;
    private final TextArea descriptionArea;
    private final Button submitBtn;
    private final Label historyTitleLabel;
    private final TableView<ServiceTicket> ticketsTable;

    public TicketsView(User currentUser, ServiceTicketService serviceTicketService) {
        setPadding(new Insets(30));
        setSpacing(20);
        setStyle("-fx-background-color: #f8fafc;");

        this.currentUser = currentUser;
        this.serviceTicketService = serviceTicketService;

        titleLabel = new Label();
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #1e293b;");

        VBox formContainer = new VBox(15);
        formContainer.setPadding(new Insets(20));
        formContainer.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 10, 0, 0, 2);");

        formTitleLabel = new Label();
        formTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        formTitleLabel.setStyle("-fx-text-fill: #334155;");

        titleField = new TextField();
        titleField.setPrefHeight(38);
        titleField.setStyle("-fx-background-color: #f1f5f9; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-radius: 6;");

        categoryCombo = new ComboBox<>();
        categoryCombo.setItems(FXCollections.observableArrayList("Dydaktyka", "Sprawy studenckie", "Pomoc techniczna / IT", "Inne"));
        categoryCombo.setMaxWidth(350);
        categoryCombo.setPrefHeight(38);
        categoryCombo.setStyle("-fx-background-color: #f1f5f9; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-radius: 6;");

        descriptionArea = new TextArea();
        descriptionArea.setPrefHeight(100);
        descriptionArea.setStyle("-fx-control-inner-background: #f1f5f9; -fx-background-color: transparent; -fx-border-color: #cbd5e1; -fx-border-radius: 6;");

        submitBtn = new Button();
        submitBtn.setFont(Font.font("System", FontWeight.BOLD, 14));
        submitBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 10 20 10 20;");
        submitBtn.setOnMouseEntered(e -> submitBtn.setStyle("-fx-background-color: #1d4ed8; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 10 20 10 20;"));
        submitBtn.setOnMouseExited(e -> submitBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 10 20 10 20;"));
        submitBtn.setOnAction(e -> handleTicketSubmit());

        formContainer.getChildren().addAll(formTitleLabel, titleField, categoryCombo, descriptionArea, submitBtn);

        historyTitleLabel = new Label();
        historyTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        historyTitleLabel.setStyle("-fx-text-fill: #334155; -fx-padding: 10 0 0 0;");

        ticketsTable = new TableView<>();
        ticketsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        ticketsTable.setPrefHeight(220);
        ticketsTable.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #cbd5e1;");

        TableColumn<ServiceTicket, String> titleCol = new TableColumn<>();
        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));

        TableColumn<ServiceTicket, String> categoryCol = new TableColumn<>();
        categoryCol.setCellValueFactory(data -> new SimpleStringProperty(extractCategory(data.getValue().getDescription())));

        TableColumn<ServiceTicket, String> descCol = new TableColumn<>();
        descCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));

        TableColumn<ServiceTicket, String> statusCol = new TableColumn<>();
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(
                DisplayTextFormatter.formatServiceTicketStatus(data.getValue().getStatus())
        ));

        ticketsTable.getColumns().addAll(titleCol, categoryCol, descCol, statusCol);
        ticketsTable.setItems(FXCollections.observableArrayList(serviceTicketService.getTicketsByReporter(currentUser)));

        getChildren().addAll(titleLabel, formContainer, historyTitleLabel, ticketsTable);

        refreshLocalization();
        MockDataProvider.currentLocaleProperty().addListener((obs, oldLocale, newLocale) -> refreshLocalization());
    }

    private void handleTicketSubmit() {
        String titleText = titleField.getText();
        String category = categoryCombo.getValue();
        String descriptionText = descriptionArea.getText();

        if (titleText == null || titleText.trim().isEmpty() || descriptionText == null || descriptionText.trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(MockDataProvider.i18n("alert_warn_title"));
            alert.setHeaderText(MockDataProvider.i18n("alert_validation_header"));
            alert.setContentText(MockDataProvider.i18n("fill_all_fields_error"));
            alert.showAndWait();
            return;
        }

        String finalCategory = category != null ? category : "Inne";
        String fullDescription = "[Kategoria: " + finalCategory + "] " + descriptionText.trim();

        serviceTicketService.createTicket(currentUser, titleText.trim(), fullDescription);
        ticketsTable.setItems(FXCollections.observableArrayList(serviceTicketService.getTicketsByReporter(currentUser)));

        titleField.clear();
        categoryCombo.setValue(null);
        descriptionArea.clear();

        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle(MockDataProvider.i18n("alert_info_title"));
        successAlert.setHeaderText(null);
        successAlert.setContentText(MockDataProvider.i18n("request_success_msg"));
        successAlert.showAndWait();
    }

    private void refreshLocalization() {
        boolean isEn = MockDataProvider.getCurrentLocale().getLanguage().equals("en");

        titleLabel.setText(MockDataProvider.i18n("tickets"));
        formTitleLabel.setText(isEn ? "Open a New Support Ticket:" : "Otwórz nowe zgłoszenie serwisowe:");
        titleField.setPromptText(isEn ? "Enter issue title..." : "Wpisz tytuł zgłoszenia...");
        categoryCombo.setPromptText(isEn ? "Select category..." : "Wybierz kategorię...");
        descriptionArea.setPromptText(isEn ? "Describe your problem in detail..." : "Opisz szczegółowo swój problem...");
        submitBtn.setText(isEn ? "Submit Ticket" : "Wyślij zgłoszenie");
        historyTitleLabel.setText(isEn ? "My Support Tickets History:" : "Historia Twoich zgłoszeń serwisowych:");

        ticketsTable.getColumns().get(0).setText(isEn ? "Title" : "Tytuł zgłoszenia");
        ticketsTable.getColumns().get(1).setText(isEn ? "Category" : "Kategoria");
        ticketsTable.getColumns().get(2).setText(isEn ? "Description" : "Opis problemu");
        ticketsTable.getColumns().get(3).setText(isEn ? "Status" : "Status");
    }

    private String extractCategory(String description) {
        if (description == null) {
            return "";
        }

        int start = description.indexOf("[Kategoria:");
        int end = description.indexOf("]");
        if (start == 0 && end > start) {
            return description.substring("[Kategoria:".length(), end).trim();
        }
        return "";
    }
}
