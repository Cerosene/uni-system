package pl.usos2.client.view.student;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pl.usos2.client.util.MockDataProvider;
import pl.usos2.server.model.finance.Payment;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.model.user.User;
import pl.usos2.server.service.finance.PaymentService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PaymentsView extends VBox {

    private final Student currentStudent;
    private final PaymentService paymentService;

    private final Label titleLabel;
    private final Label accountStatusTitle;
    private final Label balanceValue;
    private final Label balanceSubLabel;
    private final Label historyLabel;

    private final TableView<Payment> table;
    private final TableColumn<Payment, String> titleCol;
    private final TableColumn<Payment, BigDecimal> amountCol;
    private final TableColumn<Payment, LocalDate> dateCol;
    private final TableColumn<Payment, Boolean> statusCol;

    public PaymentsView(User currentUser, PaymentService paymentService) {
        setPadding(new Insets(30));
        setSpacing(25);
        setStyle("-fx-background-color: #f8fafc;");

        this.currentStudent = (Student) currentUser;
        this.paymentService = paymentService;

        titleLabel = new Label();
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 26));

        HBox balanceCard = new HBox(40);
        balanceCard.setPadding(new Insets(25, 30, 25, 30));
        balanceCard.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 10, 0, 0, 4); " +
                "-fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 12;");
        balanceCard.setAlignment(Pos.CENTER_LEFT);

        VBox balanceLeft = new VBox(5);
        accountStatusTitle = new Label();
        accountStatusTitle.setTextFill(Color.web("#64748b"));
        accountStatusTitle.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));

        balanceValue = new Label("0.00 PLN");
        balanceValue.setFont(Font.font("System", FontWeight.BOLD, 28));
        balanceValue.setTextFill(Color.web("#10b981"));

        balanceSubLabel = new Label();
        balanceSubLabel.setTextFill(Color.web("#94a3b8"));
        balanceSubLabel.setFont(Font.font("System", 12));

        balanceLeft.getChildren().addAll(accountStatusTitle, balanceValue, balanceSubLabel);
        balanceCard.getChildren().add(balanceLeft);

        historyLabel = new Label();
        historyLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 15));
        historyLabel.setTextFill(Color.web("#334155"));

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-background-radius: 8; -fx-overflow-x: hidden;");

        titleCol = new TableColumn<>();
        titleCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getTitle()));

        amountCol = new TableColumn<>();
        amountCol.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getAmount()));
        amountCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f PLN", item));
                    setStyle("-fx-font-weight: bold;");
                }
            }
        });

        dateCol = new TableColumn<>();
        dateCol.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getDueDate()));
        dateCol.setCellFactory(col -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(formatter));
                }
            }
        });

        statusCol = new TableColumn<>();
        statusCol.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().isPaid()));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label statusTag = new Label(item
                            ? MockDataProvider.i18n("payment_status_paid")
                            : MockDataProvider.i18n("payment_status_unpaid"));
                    statusTag.setPadding(new Insets(3, 8, 3, 8));
                    if (item) {
                        statusTag.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #15803d; -fx-background-radius: 4; -fx-font-weight: bold; -fx-font-size: 11;");
                    } else {
                        statusTag.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-background-radius: 4; -fx-font-weight: bold; -fx-font-size: 11;");
                    }
                    setGraphic(statusTag);
                }
            }
        });

        table.getColumns().addAll(titleCol, amountCol, dateCol, statusCol);

        refreshPaymentData();
        getChildren().addAll(titleLabel, balanceCard, historyLabel, table);

        refreshLocalization();
        MockDataProvider.currentLocaleProperty().addListener((obs, oldLocale, newLocale) -> {
            refreshLocalization();
            refreshPaymentData();
        });
    }

    private void refreshPaymentData() {
        var payments = paymentService.getPaymentsForStudent(currentStudent);
        table.setItems(FXCollections.observableArrayList(payments));

        var unpaidSum = payments.stream()
                .filter(payment -> !payment.isPaid())
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        boolean isEn = "en".equalsIgnoreCase(MockDataProvider.getCurrentLocale().getLanguage());
        String paymentsWord = isEn ? "payments" : "opłat";

        balanceValue.setText(String.format("%.2f PLN", unpaidSum));
        balanceSubLabel.setText(MockDataProvider.i18n("balance_sub_info") + " • " + payments.size() + " " + paymentsWord);
    }

    private void refreshLocalization() {
        titleLabel.setText(MockDataProvider.i18n("payments_title_main"));
        accountStatusTitle.setText(MockDataProvider.i18n("account_balance_status"));
        historyLabel.setText(MockDataProvider.i18n("payments_history_label"));

        titleCol.setText(MockDataProvider.i18n("col_payment_title"));
        amountCol.setText(MockDataProvider.i18n("col_payment_amount"));
        dateCol.setText(MockDataProvider.i18n("col_payment_date"));
        statusCol.setText(MockDataProvider.i18n("col_payment_status"));

        table.refresh();
    }
}