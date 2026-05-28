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
import pl.usos2.server.model.enumtype.RequestStatus;
import pl.usos2.server.model.request.Request;
import pl.usos2.server.service.request.RequestService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SystemRequestsView extends VBox {

    private final RequestService requestService;
    private final TableView<Request> table;

    public SystemRequestsView(RequestService requestService) {
        this.requestService = requestService;

        setPadding(new Insets(30));
        setSpacing(20);
        setStyle("-fx-background-color: #f8fafc;");

        boolean isEn = isEnglish();

        Label title = new Label(isEn ? "Student Applications" : "Wnioski studenckie");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Request, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));

        TableColumn<Request, String> typeCol = new TableColumn<>(isEn ? "Type" : "Typ");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(
                DisplayTextFormatter.formatRequestType(data.getValue().getType())
        ));

        TableColumn<Request, String> userCol = new TableColumn<>(isEn ? "From" : "Od");
        userCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudent().getFullName()));

        TableColumn<Request, String> statusCol = new TableColumn<>(isEn ? "Status" : "Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(
                DisplayTextFormatter.formatRequestStatus(data.getValue().getStatus())
        ));

        TableColumn<Request, String> dateCol = new TableColumn<>(isEn ? "Date" : "Data");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCreatedAt() != null ? data.getValue().getCreatedAt().toString() : ""
        ));

        TableColumn<Request, String> contentCol = new TableColumn<>(isEn ? "Content" : "Treść");
        contentCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getContent()));

        table.getColumns().addAll(idCol, typeCol, userCol, statusCol, dateCol, contentCol);
        refreshTable();

        Button processBtn = new Button(isEn ? "Process selected" : "Rozpatrz wniosek");
        processBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 6;");
        processBtn.setOnAction(e -> processSelectedRequest());

        getChildren().addAll(title, table, processBtn);
    }

    private void processSelectedRequest() {
        Request selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning(isEnglish() ? "Select a request first." : "Najpierw wybierz wniosek.");
            return;
        }

        List<RequestStatus> availableStatuses = List.of(
                RequestStatus.SUBMITTED,
                RequestStatus.IN_REVIEW,
                RequestStatus.APPROVED,
                RequestStatus.REJECTED
        );

        Map<String, RequestStatus> labelToStatus = new LinkedHashMap<>();
        for (RequestStatus status : availableStatuses) {
            labelToStatus.put(DisplayTextFormatter.formatRequestStatus(status), status);
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(
                DisplayTextFormatter.formatRequestStatus(selected.getStatus()),
                labelToStatus.keySet().stream().toList()
        );
        dialog.setTitle(isEnglish() ? "Process request" : "Rozpatrywanie wniosku");
        dialog.setHeaderText(isEnglish() ? "Choose new status" : "Wybierz nowy status");
        dialog.setContentText(isEnglish() ? "Status:" : "Status:");

        dialog.showAndWait().ifPresent(selectedLabel -> {
            RequestStatus newStatus = labelToStatus.get(selectedLabel);
            if (newStatus == null || newStatus == selected.getStatus()) {
                return;
            }

            try {
                applyStatusChange(selected, newStatus);
                refreshTable();
                reselectById(selected.getId());
                table.refresh();
            } catch (IllegalStateException | IllegalArgumentException exception) {
                showWarning(exception.getMessage());
            }
        });
    }

    private void applyStatusChange(Request selected, RequestStatus newStatus) {
        RequestStatus current = selected.getStatus();

        if (current == RequestStatus.SUBMITTED && newStatus == RequestStatus.APPROVED) {
            requestService.changeStatus(selected, RequestStatus.IN_REVIEW);
            requestService.changeStatus(selected, RequestStatus.APPROVED);
            return;
        }

        requestService.changeStatus(selected, newStatus);
    }

    private void refreshTable() {
        ObservableList<Request> items = FXCollections.observableArrayList(requestService.getAllRequests());
        table.setItems(items);
    }

    private void reselectById(Long requestId) {
        if (requestId == null) {
            return;
        }
        for (Request request : table.getItems()) {
            if (requestId.equals(request.getId())) {
                table.getSelectionModel().select(request);
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
