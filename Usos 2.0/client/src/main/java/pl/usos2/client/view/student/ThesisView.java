package pl.usos2.client.view.student;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ThesisView extends VBox {
    public ThesisView() {
        setPadding(new Insets(30));
        setSpacing(20);

        Label title = new Label("Wybór Promotora i Tematu Pracy");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));

        // Tabela dostępnych tematów i promotorów
        TableView<ThesisTopic> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ThesisTopic, String> profCol = new TableColumn<>("Promotor");
        profCol.setCellValueFactory(d -> d.getValue().lecturerProperty());

        TableColumn<ThesisTopic, String> topicCol = new TableColumn<>("Temat");
        topicCol.setCellValueFactory(d -> d.getValue().topicProperty());

        TableColumn<ThesisTopic, String> slotsCol = new TableColumn<>("Miejsca");
        slotsCol.setCellValueFactory(d -> d.getValue().slotsProperty());

        table.getColumns().addAll(profCol, topicCol, slotsCol);

        // Dane testowe
        table.getItems().addAll(
                new ThesisTopic("dr inż. Jan Kowalski", "Analiza algorytmów AI", "1/2"),
                new ThesisTopic("prof. Anna Nowak", "Systemy rozproszone", "0/3"),
                new ThesisTopic("dr Marek Wisniewski", "Bezpieczeństwo IoT", "2/5")
        );

        Button applyBtn = new Button("Zapisz się na temat");
        applyBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold;");

        applyBtn.setOnAction(e -> {
            ThesisTopic selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                System.out.println("Zapisano do: " + selected.getLecturer());
            }
        });

        getChildren().addAll(title, table, applyBtn);
    }

    // Klasa pomocnicza dla modelu danych
    public static class ThesisTopic {
        private final javafx.beans.property.StringProperty lecturer;
        private final javafx.beans.property.StringProperty topic;
        private final javafx.beans.property.StringProperty slots;

        public ThesisTopic(String l, String t, String s) {
            this.lecturer = new javafx.beans.property.SimpleStringProperty(l);
            this.topic = new javafx.beans.property.SimpleStringProperty(t);
            this.slots = new javafx.beans.property.SimpleStringProperty(s);
        }
        public javafx.beans.property.StringProperty lecturerProperty() { return lecturer; }
        public javafx.beans.property.StringProperty topicProperty() { return topic; }
        public javafx.beans.property.StringProperty slotsProperty() { return slots; }
        public String getLecturer() { return lecturer.get(); }
    }
}