package pl.usos2.client.view.admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pl.usos2.client.util.DisplayTextFormatter;
import pl.usos2.client.util.MockDataProvider;
import pl.usos2.server.model.enumtype.ServiceTicketStatus;
import pl.usos2.server.model.service.ServiceTicket;
import pl.usos2.server.service.maintenance.ServiceTicketService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AdminTicketsView extends VBox {

    private final ServiceTicketService serviceTicketService;
    private final TableView<ServiceTicket> table;

    public AdminTicketsView(ServiceTicketService serviceTicketService) {
        this.serviceTicketService = serviceTicketService;

        setPadding(new Insets(30));
        setSpacing(20);
        setStyle("-fx-background-color: #f8fafc;");

        boolean isEn = isEnglish();

        Label title = new Label(isEn ? "System Service Tickets" : "Zgłoszenia serwisowe");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ServiceTicket, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));

        TableColumn<ServiceTicket, String> fromCol = new TableColumn<>(isEn ? "From" : "Od");
        fromCol.setCellValueFactory(data -> {
            if (data.getValue().getReporter() == null) {
                return new SimpleStringProperty("");
            }
            return new SimpleStringProperty(data.getValue().getReporter().getFullName());
        });

        TableColumn<ServiceTicket, String> statusCol = new TableColumn<>(isEn ? "Status" : "Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(
                DisplayTextFormatter.formatServiceTicketStatus(data.getValue().getStatus())
        ));

        TableColumn<ServiceTicket, String> dateCol = new TableColumn<>(isEn ? "Created at" : "Data utworzenia");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCreatedAt() == null ? "" : data.getValue().getCreatedAt().toString()
        ));

        TableColumn<ServiceTicket, String> titleCol = new TableColumn<>(isEn ? "Title" : "Tytuł");
        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));

        TableColumn<ServiceTicket, String> descriptionCol = new TableColumn<>(isEn ? "Description" : "Opis");
        descriptionCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));

        table.getColumns().addAll(idCol, fromCol, statusCol, dateCol, titleCol, descriptionCol);
        refreshTable();

        Button processBtn = new Button(isEn ? "Process selected" : "Rozpatrz zgłoszenie");
        processBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 6;");
        processBtn.setOnAction(e -> processSelectedTicket());

        getChildren().addAll(title, table, processBtn);
    }

    private void processSelectedTicket() {
        ServiceTicket selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning(isEnglish() ? "Select a ticket first." : "Najpierw wybierz zgłoszenie.");
            return;
        }

        List<ServiceTicketStatus> availableStatuses = List.of(
                ServiceTicketStatus.OPEN,
                ServiceTicketStatus.IN_PROGRESS,
                ServiceTicketStatus.CLOSED
        );

        Map<String, ServiceTicketStatus> labelToStatus = new LinkedHashMap<>();
        for (ServiceTicketStatus status : availableStatuses) {
            labelToStatus.put(DisplayTextFormatter.formatServiceTicketStatus(status), status);
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(
                DisplayTextFormatter.formatServiceTicketStatus(selected.getStatus()),
                labelToStatus.keySet().stream().toList()
        );
        dialog.setTitle(isEnglish() ? "Process ticket" : "Rozpatrywanie zgłoszenia");
        dialog.setHeaderText(isEnglish() ? "Choose new status" : "Wybierz nowy status");
        dialog.setContentText(isEnglish() ? "Status:" : "Status:");

        dialog.showAndWait().ifPresent(selectedLabel -> {
            ServiceTicketStatus newStatus = labelToStatus.get(selectedLabel);
            if (newStatus == null || newStatus == selected.getStatus()) {
                return;
            }

            try {
                serviceTicketService.changeStatus(selected, newStatus);
                refreshTable();
                reselectById(selected.getId());
                table.refresh();
            } catch (IllegalArgumentException | IllegalStateException exception) {
                showWarning(exception.getMessage());
            }
        });
    }

    private void refreshTable() {
        ObservableList<ServiceTicket> items = FXCollections.observableArrayList(serviceTicketService.getAllTickets());
        table.setItems(items);
    }

    private void reselectById(Long ticketId) {
        if (ticketId == null) {
            return;
        }
        for (ServiceTicket ticket : table.getItems()) {
            if (ticketId.equals(ticket.getId())) {
                table.getSelectionModel().select(ticket);
                break;
            }
        }
    }

    private boolean isEnglish() {
        return "en".equalsIgnoreCase(MockDataProvider.getCurrentLocale().getLanguage());
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(MockDataProvider.i18n("alert_warn_title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
