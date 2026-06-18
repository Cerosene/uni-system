package pl.usos2.client.view.admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pl.usos2.client.util.MockDataProvider;
import pl.usos2.server.model.rental.Rental;
import pl.usos2.server.service.rental.RentalService;
import pl.usos2.client.util.ErrorDialogUtil;

/**
 * Panel administratora z obsługą zwrotu wypożyczeń.
 */
public class AdminRentalsView extends VBox {

    private final RentalService rentalService; // Zmieniono na final bez inicjalizacji 'new'
    private final TableView<Rental> table = new TableView<>();

    private final Label titleLabel;
    private final TableColumn<Rental, String> borrowerCol;
    private final TableColumn<Rental, String> itemCol;
    private final TableColumn<Rental, String> statusCol;
    private final Button returnBtn;

    // Konstruktor przyjmujący serwis z ApplicationContext
    public AdminRentalsView(RentalService rentalService) {
        this.rentalService = rentalService;

        setPadding(new Insets(30));
        setSpacing(15);
        setStyle("-fx-background-color: #f8fafc;");

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        borrowerCol = new TableColumn<>();
        borrowerCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getBorrower().getFirstName() + " " + data.getValue().getBorrower().getLastName()));

        itemCol = new TableColumn<>();
        itemCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getResourceName()));

        statusCol = new TableColumn<>();
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(formatStatus(data.getValue().isReturned())));

        table.getColumns().addAll(borrowerCol, itemCol, statusCol);

        returnBtn = new Button();
        returnBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 6;");
        returnBtn.setOnAction(e -> handleReturn());

        titleLabel = new Label();
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));

        loadData();
        getChildren().addAll(titleLabel, table, returnBtn);
        refreshLocalization();
        MockDataProvider.currentLocaleProperty().addListener((obs, oldLocale, newLocale) -> refreshLocalization());
    }

    private void loadData() {
        table.getItems().setAll(rentalService.getAllRentals());
    }

    private void handleReturn() {
        Rental selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                rentalService.returnRental(selected.getId());
                loadData();
                String successMsg = isEnglish() ? "Resource successfully marked as returned." : "Pomyślnie zwrócono przedmiot.";
                new Alert(Alert.AlertType.INFORMATION, successMsg).showAndWait();
            } catch (Exception e) {
                ErrorDialogUtil.showError("ERROR", "Błąd: " + e.getMessage());
            }
        } else {
            String warnMsg = isEnglish() ? "Please select a rental from the list!" : "Wybierz wypożyczenie z listy!";
            ErrorDialogUtil.showWarning("WARNING", warnMsg);
        }
    }

    private String formatStatus(boolean returned) {
        if (isEnglish()) {
            return returned ? "Returned" : "Active";
        }
        return returned ? "Zwrócono" : "Aktywne";
    }

    private boolean isEnglish() {
        return "en".equalsIgnoreCase(MockDataProvider.getCurrentLocale().getLanguage());
    }

    private void refreshLocalization() {
        boolean isEn = isEnglish();
        titleLabel.setText(MockDataProvider.i18n("admin_rentals"));
        returnBtn.setText(isEn ? "Mark as returned" : "Oznacz jako zwrócone");

        borrowerCol.setText(isEn ? "Borrower" : "Wypożyczający");
        itemCol.setText(MockDataProvider.i18n("rental_col_item"));
        statusCol.setText(MockDataProvider.i18n("rental_col_status"));

        table.refresh();
    }
}