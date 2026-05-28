package pl.usos2.client.view.student;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pl.usos2.client.util.MockDataProvider;
import pl.usos2.client.util.SchedulePlanStore;
import pl.usos2.server.model.academic.StudentGroup;
import pl.usos2.server.model.user.Lecturer;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.model.user.User;
import pl.usos2.server.service.course.CourseService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduleView extends VBox {

    private final Label titleLabel;
    private final GridPane scheduleGrid;
    private final List<Label> dayHeaders = new ArrayList<>();
    private final User currentUser;
    private final CourseService courseService;

    private static final String[] DAYS = {
            "schedule_col_time", "day_monday", "day_tuesday", "day_wednesday", "day_thursday", "day_friday"
    };

    private static final String[] TIME_SLOTS = {
            "08:00 - 09:30", "09:45 - 11:15", "11:30 - 13:00", "13:15 - 14:45", "15:00 - 16:30"
    };

    public ScheduleView() {
        this(null, null);
    }

    public ScheduleView(User currentUser, CourseService courseService) {
        this.currentUser = currentUser;
        this.courseService = courseService;

        setPadding(new Insets(30));
        setSpacing(20);
        setStyle("-fx-background-color: #f8fafc;");

        titleLabel = new Label();
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));

        scheduleGrid = new GridPane();
        scheduleGrid.setHgap(0);
        scheduleGrid.setVgap(0);
        scheduleGrid.setStyle("-fx-background-color: #cbd5e1;");
        scheduleGrid.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(scheduleGrid, Priority.ALWAYS);

        configureGridConstraints();
        rebuildGrid();

        getChildren().addAll(titleLabel, scheduleGrid);

        refreshLocalization();
        MockDataProvider.currentLocaleProperty().addListener((obs, oldLocale, newLocale) -> {
            refreshLocalization();
            rebuildGrid();
        });
    }

    private void configureGridConstraints() {
        scheduleGrid.getColumnConstraints().clear();
        scheduleGrid.getRowConstraints().clear();

        ColumnConstraints timeCol = new ColumnConstraints();
        timeCol.setPercentWidth(14);
        timeCol.setHgrow(Priority.ALWAYS);
        scheduleGrid.getColumnConstraints().add(timeCol);

        for (int i = 0; i < 5; i++) {
            ColumnConstraints dayCol = new ColumnConstraints();
            dayCol.setPercentWidth(17.2);
            dayCol.setHgrow(Priority.ALWAYS);
            scheduleGrid.getColumnConstraints().add(dayCol);
        }

        RowConstraints headerRow = new RowConstraints();
        headerRow.setPercentHeight(12);
        headerRow.setVgrow(Priority.ALWAYS);
        scheduleGrid.getRowConstraints().add(headerRow);

        for (int i = 0; i < TIME_SLOTS.length; i++) {
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(17.6);
            row.setVgrow(Priority.ALWAYS);
            scheduleGrid.getRowConstraints().add(row);
        }
    }

    private void rebuildGrid() {
        scheduleGrid.getChildren().clear();
        dayHeaders.clear();

        for (int col = 0; col < DAYS.length; col++) {
            Label header = new Label(MockDataProvider.i18n(DAYS[col]));
            header.setFont(Font.font("System", FontWeight.BOLD, 13));
            header.setWrapText(true);
            StackPane cell = createCell(header, "-fx-background-color: #e2e8f0; -fx-border-color: #cbd5e1;");
            scheduleGrid.add(cell, col, 0);
            dayHeaders.add(header);
        }

        for (int row = 0; row < TIME_SLOTS.length; row++) {
            Label timeLabel = new Label(TIME_SLOTS[row]);
            timeLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
            timeLabel.setWrapText(true);
            StackPane timeCell = createCell(timeLabel, "-fx-background-color: #f1f5f9; -fx-border-color: #cbd5e1;");
            scheduleGrid.add(timeCell, 0, row + 1);

            for (int col = 1; col < DAYS.length; col++) {
                Label empty = new Label("");
                StackPane emptyCell = createCell(empty, "-fx-background-color: white; -fx-border-color: #e2e8f0;");
                scheduleGrid.add(emptyCell, col, row + 1);
            }
        }

        Map<String, String> entries = buildScheduleEntriesForCurrentUser();
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            String[] split = entry.getKey().split("_");
            if (split.length != 2) {
                continue;
            }

            int day = Integer.parseInt(split[0]);
            int slot = Integer.parseInt(split[1]);
            if (day < 0 || day > 4 || slot < 0 || slot >= TIME_SLOTS.length) {
                continue;
            }

            VBox box = new VBox(6);
            box.setPadding(new Insets(10));
            box.setStyle("-fx-background-color: #dbeafe; -fx-background-radius: 6; -fx-border-color: #93c5fd; -fx-border-radius: 6; -fx-border-width: 1;");

            Label classLabel = new Label(entry.getValue());
            classLabel.setWrapText(true);
            classLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
            classLabel.setStyle("-fx-text-fill: #1e3a8a;");

            box.getChildren().add(classLabel);
            StackPane cell = new StackPane(box);
            cell.setPadding(new Insets(8));
            cell.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-width: 1;");
            scheduleGrid.add(cell, day + 1, slot + 1);
        }
    }

    private StackPane createCell(Label label, String style) {
        StackPane cell = new StackPane(label);
        cell.setPadding(new Insets(10));
        cell.setMinHeight(80);
        cell.setStyle(style + " -fx-border-width: 1;");
        StackPane.setAlignment(label, Pos.CENTER);
        return cell;
    }

    private Map<String, String> buildScheduleEntriesForCurrentUser() {
        Map<String, String> entries = new HashMap<>();
        if (currentUser == null || courseService == null) {
            return entries;
        }

        List<StudentGroup> groups = new ArrayList<>();
        if (currentUser instanceof Student student) {
            groups = courseService.getGroupsForStudent(student);
        } else if (currentUser instanceof Lecturer lecturer) {
            groups = courseService.getGroupsForLecturer(lecturer);
        }

        for (StudentGroup group : groups) {
            Map<String, String> groupPlan = SchedulePlanStore.getPlanForGroup(group.getId());

            if (groupPlan.isEmpty()) {
                int day = (int) (group.getId() % 5);
                int slot = (int) (group.getId() % TIME_SLOTS.length);
                String key = day + "_" + slot;
                entries.put(key, formatGroupEntry(group));
                continue;
            }

            for (Map.Entry<String, String> cell : groupPlan.entrySet()) {
                entries.put(cell.getKey(), cell.getValue());
            }
        }

        return entries;
    }

    private String formatGroupEntry(StudentGroup group) {
        if (group == null) {
            return "";
        }
        String courseName = group.getCourse() != null ? group.getCourse().getName() : group.getName();
        String lecturerName = group.getLecturer() != null ? group.getLecturer().getFullName() : "";
        if (lecturerName.isBlank()) {
            return courseName;
        }
        return courseName + "\n" + lecturerName;
    }

    private void refreshLocalization() {
        titleLabel.setText(MockDataProvider.i18n("schedule_title_main"));
        for (int i = 0; i < dayHeaders.size(); i++) {
            dayHeaders.get(i).setText(MockDataProvider.i18n(DAYS[i]));
        }
    }
}
