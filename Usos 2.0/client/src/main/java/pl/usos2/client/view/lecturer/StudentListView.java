package pl.usos2.client.view.lecturer;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pl.usos2.client.util.MockDataProvider;

/**
 * Widok wyświetlający listę studentów przypisanych do konkretnej grupy zajęciowej.
 * Obsługuje wielojęzyczność (i18n) dla tabel i nagłówków.
 */
public class StudentListView extends VBox {

    private final String groupName;
    private final Label titleLabel;

    // Kolumny tabeli podlegające lokalizacji
    private final TableColumn<String[], String> idCol;
    private final TableColumn<String[], String> nameCol;
    private final TableColumn<String[], String> emailCol;

    public StudentListView(String groupName) {
        this.groupName = groupName;
        setPadding(new Insets(30));
        setSpacing(20);
        setStyle("-fx-background-color: #f8fafc;");

        titleLabel = new Label();
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));

        TableView<String[]> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-background-radius: 8;");

        idCol = new TableColumn<>();
        idCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[0]));

        nameCol = new TableColumn<>();
        nameCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[1]));

        emailCol = new TableColumn<>();
        emailCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[2]));

        table.getColumns().addAll(idCol, nameCol, emailCol);

        // Dodanie demonstracyjnych danych studentów
        table.getItems().addAll(
                new String[]{"320101", "Mateusz Lewandowski", "m.lewandowski@student.pl"},
                new String[]{"320102", "Igor Sikora", "i.sikora@student.pl"},
                new String[]{"320103", "Dmytro Lytvyn", "dmytro@uni.pl"}
        );

        getChildren().addAll(titleLabel, table);

        // Inicjalne ustawienie języka interfejsu
        refreshLocalization();

        // Nasłuchiwanie zmian języka globalnego
        MockDataProvider.currentLocaleProperty().addListener((obs, oldLocale, newLocale) -> refreshLocalization());
    }

    /**
     * Tłumaczy nagłówki tabeli oraz tytuł sekcji listy studentów.
     */
    private void refreshLocalization() {
        titleLabel.setText(MockDataProvider.i18n("lecturer_student_list_title") + ": " + groupName);

        idCol.setText(MockDataProvider.i18n("student_list_col_id"));
        nameCol.setText(MockDataProvider.i18n("student_list_col_name"));
        emailCol.setText(MockDataProvider.i18n("student_list_col_email"));
    }
}