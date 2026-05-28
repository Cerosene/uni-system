package pl.usos2.client.view.lecturer;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pl.usos2.client.util.MockDataProvider;
import pl.usos2.server.model.user.Student; // Import modelu studenta
import pl.usos2.server.service.course.CourseService; // Import serwisu

import java.util.List;

/**
 * Widok wyświetlający listę studentów przypisanych do konkretnej grupy.
 * Pobiera dane bezpośrednio z CourseService.
 */
public class StudentListView extends VBox {

    private final String groupName;
    private final Long groupId;
    private final CourseService courseService;
    private final Label titleLabel = new Label();
    private final TableView<Student> table = new TableView<>();

    public StudentListView(CourseService courseService, Long groupId, String groupName) {
        this.courseService = courseService;
        this.groupId = groupId;
        this.groupName = groupName;


        setPadding(new Insets(30));
        setSpacing(20);
        setStyle("-fx-background-color: #f8fafc;");

        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));

        setupTable();
        refreshData();

        getChildren().addAll(titleLabel, table);

        refreshLocalization();
        MockDataProvider.currentLocaleProperty().addListener((obs, old, newL) -> refreshLocalization());
    }

    private void setupTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Student, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getId().toString()));

        TableColumn<Student, String> nameCol = new TableColumn<>("Imię i Nazwisko");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getFullName()));

        TableColumn<Student, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getEmail()));

        table.getColumns().addAll(idCol, nameCol, emailCol);
    }

    private void refreshData() {
        try {

            List<Student> students = courseService.getStudentsForGroup(groupId);

            table.setItems(FXCollections.observableArrayList(students));
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Błąd ładowania studentów: " + e.getMessage()).show();
        }
    }

    private void refreshLocalization() {
        titleLabel.setText(MockDataProvider.i18n("lecturer_student_list_title") + ": " + groupName);

    }
}