package pl.usos2.client.view.lecturer;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pl.usos2.client.util.MockDataProvider;
import pl.usos2.server.model.academic.Grade;
import pl.usos2.server.model.user.Lecturer;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.model.user.User;
import pl.usos2.server.service.grade.GradeService;

import java.util.List;

/**
 * Panel wystawiania ocen semestralnych przez wykładowcę akademickiego.
 * Zintegrowany z mechanizmem i18n oraz komunikacją z serwisem ocen.
 */
public class LecturerGradesView extends VBox {

    private final GradeService gradeService;
    private final Lecturer currentLecturer;

    private final Label titleLabel;
    private final TableView<GradeRow> table;
    private final Button saveAllBtn;

    private final TableColumn<GradeRow, String> idCol;
    private final TableColumn<GradeRow, String> nameCol;
    private final TableColumn<GradeRow, String> courseCol;
    private final TableColumn<GradeRow, ComboBox<Double>> gradeCol;
    private final TableColumn<GradeRow, TextField> descCol;

    public LecturerGradesView(User currentUser, GradeService gradeService) {
        setPadding(new Insets(30));
        setSpacing(20);
        setStyle("-fx-background-color: #f8fafc;");

        this.gradeService = gradeService;
        this.currentLecturer = (Lecturer) currentUser;

        List<Grade> lecturerGrades = gradeService.getGradesForLecturer(currentLecturer);

        titleLabel = new Label();
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-background-radius: 8;");

        idCol = new TableColumn<>();
        idCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().grade.getStudent().getStudentNumber()));

        nameCol = new TableColumn<>();
        nameCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().grade.getStudent().getFullName()
        ));

        courseCol = new TableColumn<>();
        courseCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().grade.getCourse().getName()));

        gradeCol = new TableColumn<>();
        gradeCol.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().gradeBox));

        descCol = new TableColumn<>();
        descCol.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().descField));

        table.getColumns().addAll(idCol, nameCol, courseCol, gradeCol, descCol);

        for (Grade grade : lecturerGrades) {
            table.getItems().add(new GradeRow(grade));
        }

        saveAllBtn = new Button();
        saveAllBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 6;");
        saveAllBtn.setOnAction(e -> saveGrades());

        getChildren().addAll(titleLabel, table, saveAllBtn);

        refreshLocalization();
        MockDataProvider.currentLocaleProperty().addListener((obs, oldLocale, newLocale) -> refreshLocalization());
    }

    private void saveGrades() {
        for (GradeRow row : table.getItems()) {
            Double newValue = row.gradeBox.getValue();
            String description = row.descField.getText();
            if (newValue != null) {
                gradeService.updateGrade(row.grade, newValue, description != null ? description : "");
            }
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(MockDataProvider.i18n("alert_info_title"));
        alert.setHeaderText(null);
        alert.setContentText(MockDataProvider.i18n("grades_save_success_msg"));
        alert.showAndWait();
    }

    private void refreshLocalization() {
        titleLabel.setText(MockDataProvider.i18n("lecturer_grades_title") + " - " + currentLecturer.getFullName());
        saveAllBtn.setText(MockDataProvider.i18n("lecturer_grades_save_btn"));

        idCol.setText(MockDataProvider.i18n("grades_col_student_id"));
        nameCol.setText(MockDataProvider.i18n("grades_col_student_name"));
        courseCol.setText(MockDataProvider.i18n("grades_col_course"));
        gradeCol.setText(MockDataProvider.i18n("grades_col_value"));
        descCol.setText(MockDataProvider.i18n("grades_col_status"));
    }

    private static class GradeRow {
        final Grade grade;
        final ComboBox<Double> gradeBox;
        final TextField descField;

        GradeRow(Grade grade) {
            this.grade = grade;
            this.gradeBox = new ComboBox<>(FXCollections.observableArrayList(2.0, 3.0, 3.5, 4.0, 4.5, 5.0));
            this.descField = new TextField(grade.getDescription());

            this.gradeBox.setValue(grade.getValue());
            this.descField.setPrefWidth(150);
            this.gradeBox.valueProperty().addListener((obs, oldV, newV) -> updateStatusText());
            updateStatusText();
        }

        void updateStatusText() {
            if (gradeBox.getValue() != null && (grade.getDescription() == null || grade.getDescription().isBlank())) {
                if (gradeBox.getValue() < 3.0) {
                    descField.setText(MockDataProvider.i18n("status_grade_failed"));
                } else {
                    descField.setText(MockDataProvider.i18n("status_grade_passed"));
                }
            }
        }
    }
}