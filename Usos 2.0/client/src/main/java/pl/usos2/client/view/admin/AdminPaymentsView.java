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
import pl.usos2.client.util.MockDataProvider;
import pl.usos2.server.model.finance.Payment;
import pl.usos2.server.service.finance.PaymentService;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class AdminPaymentsView extends VBox {

    private final PaymentService paymentService;
    private final TableView<Payment> table;

    public AdminPaymentsView(PaymentService paymentService) {
        this.paymentService = paymentService;

        setPadding(new Insets(30));
        setSpacing(20);
        setStyle("-fx-background-color: #f8fafc;");

        boolean isEn = isEnglish();

        Label title = new Label(isEn ? "Users Payments" : "Płatności użytkowników");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Payment, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));

        TableColumn<Payment, String> studentCol = new TableColumn<>(isEn ? "Student" : "Student");
        studentCol.setCellValueFactory(data -> {
            if (data.getValue().getStudent() == null) {
                return new SimpleStringProperty("");
            }
            return new SimpleStringProperty(data.getValue().getStudent().getFullName());
        });

        TableColumn<Payment, String> titleCol = new TableColumn<>(isEn ? "Title" : "Tytuł");
        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));

        TableColumn<Payment, String> amountCol = new TableColumn<>(isEn ? "Amount" : "Kwota");
        amountCol.setCellValueFactory(data -> new SimpleStringProperty(formatAmount(data.getValue())));

        TableColumn<Payment, String> dueDateCol = new TableColumn<>(isEn ? "Due date" : "Termin");
        dueDateCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDueDate() == null ? "" : data.getValue().getDueDate().toString()
        ));

        TableColumn<Payment, String> statusCol = new TableColumn<>(isEn ? "Status" : "Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(formatPaymentStatus(data.getValue().isPaid())));

        table.getColumns().addAll(idCol, studentCol, titleCol, amountCol, dueDateCol, statusCol);
        refreshTable();

        Button updateStatusBtn = new Button(isEn ? "Change status" : "Zmień status");
        updateStatusBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 6;");
        updateStatusBtn.setOnAction(e -> changeSelectedStatus());

        getChildren().addAll(title, table, updateStatusBtn);
    }

    private void changeSelectedStatus() {
        Payment selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning(isEnglish() ? "Select a payment first." : "Najpierw wybierz płatność.");
            return;
        }

        Map<String, Boolean> statusOptions = new LinkedHashMap<>();
        statusOptions.put(isEnglish() ? "Paid" : "Opłacona", true);
        statusOptions.put(isEnglish() ? "Unpaid" : "Nieopłacona", false);

        String currentLabel = selected.isPaid()
                ? (isEnglish() ? "Paid" : "Opłacona")
                : (isEnglish() ? "Unpaid" : "Nieopłacona");

        ChoiceDialog<String> dialog = new ChoiceDialog<>(currentLabel, statusOptions.keySet().stream().toList());
        dialog.setTitle(isEnglish() ? "Payment status" : "Status płatności");
        dialog.setHeaderText(isEnglish() ? "Choose new status" : "Wybierz nowy status");
        dialog.setContentText(isEnglish() ? "Status:" : "Status:");

        dialog.showAndWait().ifPresent(chosenLabel -> {
            Boolean shouldBePaid = statusOptions.get(chosenLabel);
            if (shouldBePaid == null || shouldBePaid == selected.isPaid()) {
                return;
            }

            try {
                if (shouldBePaid) {
                    paymentService.markAsPaid(selected.getId());
                } else {
                    paymentService.markAsUnpaid(selected.getId());
                }
                refreshTable();
                reselectById(selected.getId());
                table.refresh();
            } catch (IllegalArgumentException | IllegalStateException exception) {
                showWarning(exception.getMessage());
            }
        });
    }

    private void refreshTable() {
        ObservableList<Payment> items = FXCollections.observableArrayList(paymentService.getAllPayments());
        table.setItems(items);
    }

    private void reselectById(Long paymentId) {
        if (paymentId == null) {
            return;
        }
        for (Payment payment : table.getItems()) {
            if (paymentId.equals(payment.getId())) {
                table.getSelectionModel().select(payment);
                break;
            }
        }
    }

    private String formatAmount(Payment payment) {
        if (payment.getAmount() == null) {
            return "";
        }

        Locale locale = isEnglish() ? Locale.ENGLISH : new Locale("pl", "PL");
        DecimalFormat decimalFormat = new DecimalFormat("0.00", new DecimalFormatSymbols(locale));
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
        return decimalFormat.format(payment.getAmount()) + " PLN";
    }

    private String formatPaymentStatus(boolean paid) {
        if (isEnglish()) {
            return paid ? "Paid" : "Unpaid";
        }
        return paid ? "Opłacona" : "Nieopłacona";
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
