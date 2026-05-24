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
import pl.usos2.client.util.MockDataProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Panel wystawiania ocen semestralnych przez wykładowcę akademickiego.
 * Zintegrowany z mechanizmem i18n oraz polskimi komentarzami logiki biznesowej.
 */
public class LecturerGradesView extends VBox {

    private static final GradeService gradeService = new GradeService();

    private final Lecturer currentLecturer;
    private final Course currentCourse;
    private final List<Student> studentsInGroup = new ArrayList<>();

    private final Label titleLabel;
    private final TableView<StudentRow> table;
    private final Button saveAllBtn;

    // Kolumny tabeli wymagające lokalizacji nagłówków
    private final TableColumn<StudentRow, String> idCol;
    private final TableColumn<StudentRow, String> nameCol;
    private final TableColumn<StudentRow, ComboBox<Double>> gradeCol;
    private final TableColumn<StudentRow, TextField> descCol;

    public LecturerGradesView() {
        setPadding(new Insets(30));
        setSpacing(20);
        setStyle("-fx-background-color: #f8fafc;");

        // Inicjalizacja danych testowych (Mock Data)
        currentLecturer = new Lecturer(2L, "Tomasz", "Nowak", "lecturer@uni.pl", "password123", "EMP8821", "prof. dr hab.");
        currentCourse = new Course(301L, "Zaawansowane Algorytmy", "CS301", 5, Semester.THIRD);

        studentsInGroup.add(new Student(1003L, "Dmytro", "Lytvyn", "dmytro@uni.pl", "pass123", "320103", "Informatyka", Semester.THIRD));
        studentsInGroup.add(new Student(1004L, "Anna", "Zielińska", "anna@uni.pl", "pass456", "320104", "Informatyka", Semester.THIRD));

        // Tytuł ekranu
        titleLabel = new Label();
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));

        // Konfiguracja tabeli
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-background-radius: 8;");

        idCol = new TableColumn<>();
        idCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().student.getStudentNumber()));

        nameCol = new TableColumn<>();
        nameCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().student.getFirstName() + " " + d.getValue().student.getLastName()
        ));

        gradeCol = new TableColumn<>();
        gradeCol.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().gradeBox));

        descCol = new TableColumn<>();
        descCol.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().descField));

        table.getColumns().addAll(idCol, nameCol, gradeCol, descCol);

        // Mapowanie studentów na wiersze interaktywne tabeli
        for (Student s : studentsInGroup) {
            table.getItems().add(new StudentRow(s));
        }

        // Przycisk zapisu wszystkich ocen
        saveAllBtn = new Button();
        saveAllBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 6;");

        saveAllBtn.setOnAction(e -> {
            // Iteracja przez wszystkie wiersze studentów powiązane z tabelą interfejsu użytkownika
            for (StudentRow row : table.getItems()) {
                Double val = row.gradeBox.getValue();
                String desc = row.descField.getText();

                // Bezpieczna weryfikacja: przetwarzamy dane tylko wtedy, gdy ocena została wybrana
                if (val != null) {
                    // Tymczasowy log konsoli emulujący zapis danych do bazy (oczekiwanie na wdrożenie GradeService)
                    System.out.println("Zapisano ocenę: " + val + " [" + desc + "] dla studenta: " + row.student.getLastName());
                }
            }

            // Wyświetlenie okna informacyjnego o pomyślnym zapisie danych z uwzględnieniem mechanizmu i18n
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(MockDataProvider.i18n("alert_info_title"));
            alert.setHeaderText(null);
            alert.setContentText(MockDataProvider.i18n("grades_save_success_msg"));
            alert.showAndWait();
        });

        getChildren().addAll(titleLabel, table, saveAllBtn);

        // Konfiguracja językowa początkowa
        refreshLocalization();

        // Słuchacz zdarzeń re-lokalizacji językowej
        MockDataProvider.currentLocaleProperty().addListener((obs, oldLocale, newLocale) -> refreshLocalization());
    }

    /**
     * Odświeża nazwy i etykiety komponentów na ekranie oceniania.
     */
    private void refreshLocalization() {
        titleLabel.setText(MockDataProvider.i18n("lecturer_grades_title") + " - " + currentCourse.getName());
        saveAllBtn.setText(MockDataProvider.i18n("lecturer_grades_save_btn"));

        idCol.setText(MockDataProvider.i18n("grades_col_student_id"));
        nameCol.setText(MockDataProvider.i18n("grades_col_student_name"));
        gradeCol.setText(MockDataProvider.i18n("grades_col_value"));
        descCol.setText(MockDataProvider.i18n("grades_col_status"));

        // Aktualizacja opisów słownych bezpośrednio wewnątrz pól tekstowych tabeli
        for (StudentRow row : table.getItems()) {
            row.updateStatusText();
        }
    }

    /**
     * Pomocnicza klasa reprezentująca interaktywny wiersz tabeli oceniania studenta.
     */
    public static class StudentRow {
        final Student student;
        final ComboBox<Double> gradeBox;
        final TextField descField;

        public StudentRow(Student student) {
            this.student = student;
            this.gradeBox = new ComboBox<>(FXCollections.observableArrayList(2.0, 3.0, 3.5, 4.0, 4.5, 5.0));
            this.descField = new TextField();
            this.descField.setEditable(false);

            // Domyślna ocena na start
            this.gradeBox.setValue(5.0);
            updateStatusText();

            // Reakcja na zmianę wybranej oceny w ComboBox
            this.gradeBox.valueProperty().addListener((obs, oldV, newV) -> updateStatusText());
        }

        /**
         * Automatycznie aktualizuje status tekstowy zaliczenia na podstawie języka systemu.
         */
        void updateStatusText() {
            Double val = gradeBox.getValue();
            if (val != null) {
                if (val < 3.0) {
                    descField.setText(MockDataProvider.i18n("status_grade_failed"));
                } else {
                    descField.setText(MockDataProvider.i18n("status_grade_passed"));
                }
            }
        }
    }
}