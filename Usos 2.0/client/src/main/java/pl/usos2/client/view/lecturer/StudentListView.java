package pl.usos2.client.view.lecturer;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;


public class StudentListView extends VBox {
    public StudentListView(String groupName) {
        setPadding(new Insets(30));
        setSpacing(20);

        Label title = new Label("Lista Studentów: " + groupName);
        title.setFont(Font.font("System", FontWeight.BOLD, 24));

        TableView<String[]> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<String[], String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[0]));

        TableColumn<String[], String> nameCol = new TableColumn<>("Imię i Nazwisko");
        nameCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[1]));

        TableColumn<String[], String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[2]));

        table.getColumns().addAll(idCol, nameCol, emailCol);

        // Dane demonstracyjne
        table.getItems().addAll(
                new String[]{"320101", "Mateusz Lewandowski", "m.lewandowski@student.pl"},
                new String[]{"320102", "Igor Sikora", "i.sikora@student.pl"},
                new String[]{"320103", "Mykyta Lytvyn", "m.lytvyn@student.pl"}
        );

        Button backBtn = new Button("Powrót do kursów");

        getChildren().addAll(title, table, backBtn);
    }
}