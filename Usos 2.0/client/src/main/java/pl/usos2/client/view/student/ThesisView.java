package pl.usos2.client.view.student;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pl.usos2.client.util.MockDataProvider;

/**
 * Widok wyboru promotora oraz tematów pracy dyplomowej (inżynierskiej/magisterskiej).
 * Wspiera wielojęzyczność (i18n) i odświeża interfejs na żywo.
 */
public class ThesisView extends VBox {

    private final Label titleLabel;
    private final TableView<ThesisTopic> table;
    private final Button applyBtn;

    // Kolumny tabeli podlegające lokalizacji nagłówków
    private final TableColumn<ThesisTopic, String> profCol;
    private final TableColumn<ThesisTopic, String> topicCol;
    private final TableColumn<ThesisTopic, String> slotsCol;

    public ThesisView() {
        setPadding(new Insets(30));
        setSpacing(20);
        setStyle("-fx-background-color: #f8fafc;");

        // Główny tytuł ekranu
        titleLabel = new Label();
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));

        // Inicjalizacja i konfiguracja tabeli tematów dyplomowych
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-background-radius: 8;");

        profCol = new TableColumn<>();
        profCol.setCellValueFactory(d -> d.getValue().lecturerProperty());

        topicCol = new TableColumn<>();
        // Używamy niestandardowej fabryki komórek, by nazwy tematów tłumaczyły się dynamicznie
        topicCol.setCellValueFactory(d -> d.getValue().topicKeyProperty());
        topicCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(MockDataProvider.i18n(item));
                }
            }
        });

        slotsCol = new TableColumn<>();
        slotsCol.setCellValueFactory(d -> d.getValue().slotsProperty());

        table.getColumns().addAll(profCol, topicCol, slotsCol);

        // Dodawanie testowych rekordów z kluczami lokalizacyjnymi tematów
        table.getItems().addAll(
                new ThesisTopic("dr inż. Jan Kowalski", "thesis_topic_ai", "1/3"),
                new ThesisTopic("dr Marek Wiśniewski", "thesis_topic_iot", "2/5")
        );

        // Przycisk zapisu na wybrany temat
        applyBtn = new Button();
        applyBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 6;");

        applyBtn.setOnAction(e -> {
            ThesisTopic selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(MockDataProvider.i18n("alert_info_title"));
                alert.setHeaderText(null);

                // Generowanie komunikatu sukcesu zapisu
                String successMsg = MockDataProvider.i18n("thesis_enroll_success") + " " + selected.getLecturer();
                alert.setContentText(successMsg);
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText(MockDataProvider.i18n("thesis_select_warning"));
                alert.showAndWait();
            }
        });

        getChildren().addAll(titleLabel, table, applyBtn);

        // Ustawienie tekstów językowych przy uruchomieniu panelu
        refreshLocalization();

        // Podpięcie słuchacza zdarzeń zmiany języka w aplikacji
        MockDataProvider.currentLocaleProperty().addListener((obs, oldLocale, newLocale) -> refreshLocalization());
    }

    /**
     * Odświeża napisy komponentów interfejsu oraz wymusza przerysowanie tabeli.
     */
    private void refreshLocalization() {
        titleLabel.setText(MockDataProvider.i18n("thesis_title_main"));
        applyBtn.setText(MockDataProvider.i18n("thesis_apply_btn"));

        // Tłumaczenie nagłówków kolumn
        profCol.setText(MockDataProvider.i18n("thesis_col_promotor"));
        topicCol.setText(MockDataProvider.i18n("thesis_col_topic"));
        slotsCol.setText(MockDataProvider.i18n("thesis_col_slots"));

        // Wymuszenie odświeżenia komórek tabeli w celu ponownego wywołania i18n
        table.refresh();
    }

    /**
     * Model danych reprezentujący temat pracy dyplomowej.
     */
    public static class ThesisTopic {
        private final javafx.beans.property.StringProperty lecturer;
        private final javafx.beans.property.StringProperty topicKey;
        private final javafx.beans.property.StringProperty slots;

        public ThesisTopic(String l, String tKey, String s) {
            this.lecturer = new javafx.beans.property.SimpleStringProperty(l);
            this.topicKey = new javafx.beans.property.SimpleStringProperty(tKey);
            this.slots = new javafx.beans.property.SimpleStringProperty(s);
        }

        public String getLecturer() { return lecturer.get(); }
        public javafx.beans.property.StringProperty lecturerProperty() { return lecturer; }

        public String getTopicKey() { return topicKey.get(); }
        public javafx.beans.property.StringProperty topicKeyProperty() { return topicKey; }

        public javafx.beans.property.StringProperty slotsProperty() { return slots; }
    }
}