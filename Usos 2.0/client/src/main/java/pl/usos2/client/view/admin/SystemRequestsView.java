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

    private final Label titleLabel;
    private final TableColumn<Request, String> idCol;
    private final TableColumn<Request, String> typeCol;
    private final TableColumn<Request, String> userCol;
    private final TableColumn<Request, String> statusCol;
    private final TableColumn<Request, String> dateCol;
    private final TableColumn<Request, String> contentCol;
    private final Button processBtn;

    public SystemRequestsView(RequestService requestService) {
        this.requestService = requestService;

        setPadding(new Insets(30));
        setSpacing(20);
        setStyle("-fx-background-color: #f8fafc;");

        titleLabel = new Label();
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        idCol = new TableColumn<>();
        idCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));

        typeCol = new TableColumn<>();
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(
                DisplayTextFormatter.formatRequestType(data.getValue().getType())
        ));

        userCol = new TableColumn<>();
        userCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudent().getFullName()));

        statusCol = new TableColumn<>();
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(
                DisplayTextFormatter.formatRequestStatus(data.getValue().getStatus())
        ));

        dateCol = new TableColumn<>();
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCreatedAt() != null ? data.getValue().getCreatedAt().toString() : ""
        ));

        contentCol = new TableColumn<>();
        contentCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getContent()));

        table.getColumns().addAll(idCol, typeCol, userCol, statusCol, dateCol, contentCol);
        refreshTable();

        processBtn = new Button();
        processBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 6;");
        processBtn.setOnAction(e -> processSelectedRequest());

        getChildren().addAll(titleLabel, table, processBtn);

        refreshLocalization();
        MockDataProvider.currentLocaleProperty().addListener((obs, oldLocale, newLocale) -> refreshLocalization());
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

    private void refreshLocalization() {
        boolean isEn = isEnglish();

        titleLabel.setText(isEn ? "Student Applications" : "Wnioski studenckie");
        processBtn.setText(isEn ? "Process selected" : "Rozpatrz wniosek");

        idCol.setText("ID");
        typeCol.setText(isEn ? "Type" : "Typ");
        userCol.setText(isEn ? "From" : "Od");
        statusCol.setText(isEn ? "Status" : "Status");
        dateCol.setText(isEn ? "Date" : "Data");
        contentCol.setText(isEn ? "Content" : "Treść");

        table.refresh();
    }
}
