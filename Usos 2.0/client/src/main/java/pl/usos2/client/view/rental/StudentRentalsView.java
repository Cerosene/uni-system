package pl.usos2.client.view.rental;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pl.usos2.client.util.MockDataProvider;
import pl.usos2.server.model.rental.Rental;
import pl.usos2.server.model.user.User;
import pl.usos2.server.service.rental.RentalService;

/**
 * Widok wyświetlający historię wypożyczeń dla zalogowanego studenta.
 */
public class StudentRentalsView extends VBox {
    private final RentalService rentalService;
    private final TableView<Rental> rentalTable = new TableView<>();
    private final TableColumn<Rental, String> itemCol = new TableColumn<>();
    private final TableColumn<Rental, String> dateCol = new TableColumn<>();
    private final TableColumn<Rental, String> statusCol = new TableColumn<>();
    private final Label titleLabel = new Label();
    private final User currentStudent;

    public StudentRentalsView(User student, RentalService rentalService) {
        this.currentStudent = student;
        this.rentalService = rentalService;
        setPadding(new Insets(30));
        setSpacing(20);
        setStyle("-fx-background-color: #f8fafc;");

        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));

        rentalTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        itemCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getResourceName()));
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRentalDate().toString()));
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isReturned() ? "Zwrócono" : "Wypożyczono"));

        rentalTable.getColumns().addAll(itemCol, dateCol, statusCol);

        refreshData();
        getChildren().addAll(titleLabel, rentalTable);
        refreshLocalization();
        MockDataProvider.currentLocaleProperty().addListener((obs, old, newL) -> refreshLocalization());
    }

    private void refreshData() {
        rentalTable.getItems().setAll(rentalService.getRentalsForBorrower(currentStudent));
    }

    private void refreshLocalization() {
        titleLabel.setText(MockDataProvider.i18n("my_rentals_title"));
        itemCol.setText(MockDataProvider.i18n("rental_col_item"));
        dateCol.setText(MockDataProvider.i18n("rental_col_date"));
        statusCol.setText(MockDataProvider.i18n("rental_col_status"));

    }
}