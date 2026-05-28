package pl.usos2.client.view.lecturer;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pl.usos2.client.util.MockDataProvider;
import pl.usos2.server.model.academic.Course;
import pl.usos2.server.model.academic.StudentGroup;
import pl.usos2.server.model.user.Lecturer;
import pl.usos2.server.model.user.User;
import pl.usos2.server.service.course.CourseService;

import java.util.List;
import java.util.stream.Collectors;

public class LecturerCoursesView extends ScrollPane {

    private final Label titleLabel;
    private final FlowPane coursesGrid;
    private final CourseService courseService; // Pole klasy do użycia w przyciskach

    public LecturerCoursesView(User currentUser, CourseService courseService) {
        this.courseService = courseService;
        setFitToWidth(true);
        setStyle("-fx-background-color: #f8fafc; -fx-background: #f8fafc;");

        Lecturer lecturer = (Lecturer) currentUser;
        List<Course> courses = courseService.getAllCourses().stream()
                .filter(course -> course.getLecturer() != null && course.getLecturer().getId().equals(lecturer.getId()))
                .collect(Collectors.toList());

        VBox container = new VBox(25);
        container.setPadding(new Insets(30));

        titleLabel = new Label();
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 26));

        coursesGrid = new FlowPane(20, 20);
        coursesGrid.setPrefWidth(860);

        if (courses.isEmpty()) {
            Label emptyLabel = new Label(MockDataProvider.i18n("lecturer_courses_empty"));
            emptyLabel.setFont(Font.font("System", 16));
            emptyLabel.setTextFill(Color.web("#475569"));
            coursesGrid.getChildren().add(emptyLabel);
        } else {
            courses.forEach(course -> coursesGrid.getChildren().add(createCourseCard(course)));
        }

        container.getChildren().addAll(titleLabel, coursesGrid);
        setContent(container);

        refreshLocalization();
        MockDataProvider.currentLocaleProperty().addListener((obs, oldLocale, newLocale) -> refreshLocalization());
    }

    private VBox createCourseCard(Course course) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setPrefWidth(280);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4);");

        Label codeLbl = new Label(course.getCode());
        codeLbl.setTextFill(Color.web("#3b82f6"));
        codeLbl.setFont(Font.font("System", FontWeight.BOLD, 12));

        Label nameLbl = new Label(course.getName());
        nameLbl.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLbl.setWrapText(true);

        HBox infoRow = new HBox(10);
        Label lecturerLbl = new Label(course.getLecturer().getFullName());
        lecturerLbl.setStyle("-fx-background-color: #f1f5f9; -fx-padding: 3 8; -fx-background-radius: 5; -fx-font-size: 12;");

        int studentCount = courseService.getStudentsForGroup(course.getId()).size();
        Label studentsCountLbl = new Label(studentCount + " studentów");
        studentsCountLbl.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-padding: 3 8; -fx-background-radius: 5; -fx-font-size: 12;");

        infoRow.getChildren().addAll(lecturerLbl, studentsCountLbl);

        // Przycisk "Lista studentów" - dodany do karty
        Button viewStudentsBtn = new Button("Lista studentów");
        viewStudentsBtn.setMaxWidth(Double.MAX_VALUE);
        viewStudentsBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold;");
        viewStudentsBtn.setOnAction(e -> {

            List<StudentGroup> allGroups = courseService.getAllGroups();

            List<StudentGroup> courseGroups = allGroups.stream()
                    .filter(g -> g.getCourse() != null && g.getCourse().getId().equals(course.getId()))
                    .collect(Collectors.toList());

            if (!courseGroups.isEmpty()) {
                StudentGroup targetGroup = courseGroups.get(0);
                setContent(new StudentListView(
                        this.courseService,
                        targetGroup.getId(),
                        targetGroup.getName()
                ));
            } else {
                new Alert(Alert.AlertType.WARNING, "Brak przypisanej grupy dla tego kursu!").show();
            }
        });
        card.getChildren().addAll(codeLbl, nameLbl, infoRow, viewStudentsBtn);

        return card;
    }


    private void refreshLocalization() {
        titleLabel.setText(MockDataProvider.i18n("lecturer_courses_title"));
    }
}