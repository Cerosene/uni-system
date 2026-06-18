package pl.usos2.client.view.student;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pl.usos2.client.util.MockDataProvider;
import pl.usos2.server.model.academic.Course;
import pl.usos2.server.model.academic.Grade;
import pl.usos2.server.model.academic.StudentGroup;
import pl.usos2.server.model.user.Lecturer;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.model.user.User;
import pl.usos2.server.service.course.CourseService;
import pl.usos2.server.service.grade.GradeService;

import java.util.List;

/**
 * Widok ocen studenta podzielony na semestry za pomocą komponentu Accordion.
 * Obsługuje wielojęzyczność aplikacji (i18n) i dynamiczne odświeżanie.
 */
public class GradesView extends ScrollPane {

    private final VBox container;
    private final Label titleLabel;
    private final VBox gradesBox;

    public GradesView(User currentUser, GradeService gradeService, CourseService courseService) {
        setFitToWidth(true);
        setStyle("-fx-background-color: #f8fafc; -fx-background: #f8fafc;");

        Student currentStudent = (Student) currentUser;
        List<Grade> studentGrades = gradeService.getGradesForStudent(currentStudent);
        List<StudentGroup> studentGroups = courseService.getGroupsForStudent(currentStudent);

        container = new VBox(20);
        container.setPadding(new Insets(30));

        titleLabel = new Label();
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 26));
        container.getChildren().add(titleLabel);

        gradesBox = new VBox(15);
        gradesBox.setPadding(new Insets(10, 0, 0, 0));

        if (studentGroups.isEmpty()) {
            Label noGrades = new Label(MockDataProvider.i18n("grades_empty_message"));
            noGrades.setFont(Font.font("System", 16));
            noGrades.setTextFill(Color.web("#475569"));
            gradesBox.getChildren().add(noGrades);
        } else {
            for (StudentGroup group : studentGroups) {
                Course course = group.getCourse();
                // Find grade for this specific course
                Grade grade = studentGrades.stream()
                        .filter(g -> g.getCourse().getId().equals(course.getId()))
                        .findFirst()
                        .orElse(null);

                gradesBox.getChildren().add(createGradeRow(course, group.getLecturer(), grade));
            }
        }

        container.getChildren().add(gradesBox);
        setContent(container);

        refreshLocalization();
        MockDataProvider.currentLocaleProperty().addListener((obs, oldLocale, newLocale) -> refreshLocalization());
    }

    private HBox createGradeRow(Course course, Lecturer lecturer, Grade grade) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 20, 12, 20));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1;");

        VBox subjectBox = new VBox(4);
        subjectBox.setMinWidth(260);

        Label courseName = new Label(course.getName());
        courseName.setFont(Font.font("System", FontWeight.BOLD, 14));
        courseName.setTextFill(Color.web("#1e293b"));

        Label courseCode = new Label(course.getCode());
        courseCode.setTextFill(Color.GRAY);
        subjectBox.getChildren().addAll(courseName, courseCode);

        Label lecturerLabel = new Label(lecturer != null ? lecturer.getFullName() : "");
        lecturerLabel.setMinWidth(180);
        lecturerLabel.setTextFill(Color.web("#475569"));

        Label ects = new Label("ECTS: " + course.getEcts());
        ects.setMinWidth(80);
        ects.setTextFill(Color.web("#64748b"));

        String gradeVal = (grade != null && grade.getValue() != 0.0) ? String.format("%.1f", grade.getValue()) : "-";
        Label value = new Label(gradeVal);
        value.setMinWidth(60);
        value.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        if (grade != null) {
            value.setTextFill(grade.getValue() >= 3.0 ? Color.web("#10b981") : Color.web("#ef4444"));
        } else {
            value.setTextFill(Color.web("#94a3b8"));
        }

        String statusKey = grade == null ? "status_in_progress" : (grade.getValue() >= 3.0 ? "status_passed" : "status_failed");
        Label status = new Label(MockDataProvider.i18n(statusKey));
        status.setPadding(new Insets(4, 8, 4, 8));
        
        if (grade == null) {
            status.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; -fx-background-radius: 4; -fx-font-size: 11; -fx-font-weight: bold;");
        } else {
            status.setStyle(grade.getValue() >= 3.0
                    ? "-fx-background-color: #dcfce7; -fx-text-fill: #15803d; -fx-background-radius: 4; -fx-font-size: 11; -fx-font-weight: bold;"
                    : "-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-background-radius: 4; -fx-font-size: 11; -fx-font-weight: bold;");
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(subjectBox, lecturerLabel, ects, value, spacer, status);
        row.setUserData(grade);
        return row;
    }

    private void refreshLocalization() {
        titleLabel.setText(MockDataProvider.i18n("grades_title"));

        for (javafx.scene.Node node : gradesBox.getChildren()) {
            if (node instanceof HBox && ((HBox) node).getUserData() instanceof Grade) {
                Grade grade = (Grade) ((HBox) node).getUserData();
                Label status = (Label) ((HBox) node).getChildren().get(((HBox) node).getChildren().size() - 1);
                status.setText(grade.getValue() >= 3.0 ? MockDataProvider.i18n("status_passed") : MockDataProvider.i18n("status_failed"));
            } else if (node instanceof HBox) {
                // For subjects without a grade
                Label status = (Label) ((HBox) node).getChildren().get(((HBox) node).getChildren().size() - 1);
                status.setText(MockDataProvider.i18n("status_in_progress"));
            }
        }
    }
}