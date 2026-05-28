package pl.usos2.client.view.admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import pl.usos2.server.model.rental.Rental;
import pl.usos2.server.service.rental.RentalService;

/**
 * Panel administratora z obsługą zwrotu wypożyczeń.
 */
public class AdminRentalsView extends VBox {

    private final RentalService rentalService = new RentalService();
    private final TableView<Rental> table = new TableView<>();

    public AdminRentalsView() {
        setPadding(new Insets(30));
        setSpacing(15);
        setStyle("-fx-background-color: #f8fafc;");

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Rental, String> borrowerCol = new TableColumn<>("Wypożyczający");
        borrowerCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getBorrower().getFirstName() + " " + data.getValue().getBorrower().getLastName()));

        TableColumn<Rental, String> itemCol = new TableColumn<>("Przedmiot");
        itemCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getResourceName()));

        TableColumn<Rental, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isReturned() ? "Zwrócono" : "Aktywne"));

        table.getColumns().addAll(borrowerCol, itemCol, statusCol);

        Button returnBtn = new Button("Oznacz jako zwrócone");
        returnBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-padding: 10 20;");
        returnBtn.setOnAction(e -> handleReturn());

        loadData();
        getChildren().addAll(new Label("Zarządzanie wypożyczeniami"), table, returnBtn);
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
                new Alert(Alert.AlertType.INFORMATION, "Pomyślnie zwrócono przedmiot.").showAndWait();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Błąd: " + e.getMessage()).showAndWait();
            }
        } else {
            new Alert(Alert.AlertType.WARNING, "Wybierz wypożyczenie z listy!").showAndWait();
        }
    }
}