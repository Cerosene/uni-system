package pl.usos2.client.view.lecturer;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.model.user.Lecturer;
import pl.usos2.server.model.academic.Course;
import pl.usos2.server.model.academic.Grade;
import pl.usos2.server.model.enumtype.Semester;
import pl.usos2.server.service.grade.GradeService;

import java.util.ArrayList;
import java.util.List;

public class LecturerGradesView extends VBox {

    // Ogólna usługa synchronizacji danych w pamięci
    private static final GradeService gradeService = new GradeService();

    private final Lecturer currentLecturer;
    private final Course currentCourse;
    private final List<Student> studentsInGroup = new ArrayList<>();

    public LecturerGradesView() {
        setPadding(new Insets(30));
        setSpacing(20);
        setStyle("-fx-background-color: #f8fafc;");

        // Dane surowe
        currentLecturer = new Lecturer(2L, "Tomasz", "Nowak", "lecturer@uni.pl", "password123", "EMP201", "Dr.");
        currentCourse = new Course(101L, "Advanced Algorithms", "CS301", 6, currentLecturer);

        studentsInGroup.add(new Student(1001L, "Mateusz", "Lewandowski", "mateusz@uni.pl", "pass123", "320101", "Informatyka", Semester.THIRD));
        studentsInGroup.add(new Student(1002L, "Igor", "Sikora", "igor@uni.pl", "pass123", "320102", "Informatyka", Semester.THIRD));
        studentsInGroup.add(new Student(1003L, "Dmytro", "Lytvyn", "dmytro@uni.pl", "pass123", "320103", "Informatyka", Semester.THIRD));

        // Tytuł
        Label title = new Label("Wystawianie Ocen: " + currentCourse.getName());
        title.setFont(Font.font("System", FontWeight.BOLD, 24));

        // Tabela
        TableView<StudentRow> table = new TableView<>();
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<StudentRow, String> idCol = new TableColumn<>("ID Studenta");
        idCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().student.getStudentNumber()));

        TableColumn<StudentRow, String> nameCol = new TableColumn<>("Imię i Nazwisko");
        nameCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().student.getFirstName() + " " + d.getValue().student.getLastName()
        ));

        // Kolumna oceny
        TableColumn<StudentRow, Double> gradeCol = new TableColumn<>("Ocena");
        gradeCol.setMinWidth(120);
        gradeCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(getTableView().getItems().get(getIndex()).gradeBox);
            }
        });

        // Pole komentarza
        TableColumn<StudentRow, String> descCol = new TableColumn<>("Komentarz");
        descCol.setMinWidth(200);
        descCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(getTableView().getItems().get(getIndex()).descField);
            }
        });

        table.getColumns().addAll(idCol, nameCol, gradeCol, descCol);

        // Wprowadzamy dane
        for (Student student : studentsInGroup) {
            table.getItems().add(new StudentRow(student));
        }

        // Przycisk zapisu
        Button saveAllBtn = new Button("Zapisz wszystkie zmiany");
        saveAllBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");

        saveAllBtn.setOnAction(e -> {
            for (StudentRow row : table.getItems()) {
                double selectedGradeValue = row.gradeBox.getValue();
                String descValue = row.descField.getText().trim();

                // Przekazujemy wszystko do serwerowej usługi GradeService
                gradeService.addGrade(
                        row.student,
                        currentCourse,
                        currentLecturer,
                        selectedGradeValue,
                        descValue
                );
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sukces");
            alert.setHeaderText(null);
            alert.setContentText("Oceny i statusy zostały pomyślnie zapisane!");
            alert.showAndWait();
        });

        getChildren().addAll(title, table, saveAllBtn);
    }

    // Klasa-ciąg znaków z logiką automatycznej zmiany statusu
    public static class StudentRow {
        final Student student;
        final ComboBox<Double> gradeBox;
        final TextField descField;

        public StudentRow(Student student) {
            this.student = student;
            this.gradeBox = new ComboBox<>(FXCollections.observableArrayList(2.0, 3.0, 3.5, 4.0, 4.5, 5.0));
            this.descField = new TextField("Zaliczenie");

            this.gradeBox.setValue(5.0); // Domyślnie ustawiamy wartość 5.0

            // Automatycznie zmienia tekst w zależności od wybranej oceny
            this.gradeBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    if (newValue < 3.0) {
                        this.descField.setText("Brak zaliczenia");
                    } else {
                        this.descField.setText("Zaliczenie");
                    }
                }
            });
        }
    }

    public static GradeService getSharedGradeService() {
        return gradeService;
    }
}