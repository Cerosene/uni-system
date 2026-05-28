package pl.usos2.client.view.admin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pl.usos2.client.util.MockDataProvider;
import pl.usos2.client.util.SchedulePlanStore;
import pl.usos2.server.model.academic.Course;
import pl.usos2.server.model.academic.StudentGroup;
import pl.usos2.server.service.course.CourseService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminScheduleView extends VBox {

    private final CourseService courseService;
    private final List<StudentGroup> availableGroups = new ArrayList<>();
    private final List<Course> availableCourses = new ArrayList<>();

    private final Map<String, String> scheduleData = new HashMap<>();

    private ComboBox<StudentGroup> groupComboBox;
    private GridPane scheduleGrid;
    private Button saveButton;

    private final String[] days = {
            MockDataProvider.i18n("day_monday"),
            MockDataProvider.i18n("day_tuesday"),
            MockDataProvider.i18n("day_wednesday"),
            MockDataProvider.i18n("day_thursday"),
            MockDataProvider.i18n("day_friday")
    };

    private final String[] timeSlots = {
            "08:00 - 09:30",
            "09:45 - 11:15",
            "11:30 - 13:00",
            "13:15 - 14:45",
            "15:00 - 16:30"
    };

    public AdminScheduleView(CourseService courseService) {
        this.courseService = courseService;

        setPadding(new Insets(30));
        setSpacing(20);
        setStyle("-fx-background-color: #f8fafc;");

        reloadDomainData();

        Label title = new Label(MockDataProvider.i18n("global_schedule_title"));
        title.setFont(Font.font("System", FontWeight.BOLD, 24));

        HBox configHeader = new HBox(15);
        configHeader.setAlignment(Pos.CENTER_LEFT);

        Label selectGroupLabel = new Label(MockDataProvider.i18n("select_group_prompt"));
        selectGroupLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));

        groupComboBox = new ComboBox<>();
        groupComboBox.getItems().addAll(availableGroups);
        groupComboBox.setPromptText(MockDataProvider.i18n("choose_group_holder"));
        groupComboBox.setMinWidth(320);
        groupComboBox.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(StudentGroup item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        groupComboBox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(StudentGroup item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        groupComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadScheduleForGroup(newVal);
                saveButton.setDisable(false);
            } else {
                scheduleData.clear();
                saveButton.setDisable(true);
            }
            buildGrid();
        });

        saveButton = new Button(MockDataProvider.getCurrentLocale().getLanguage().equals("en") ? "Save schedule" : "Zapisz plan");
        saveButton.setDisable(true);
        saveButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 6;");
        saveButton.setOnAction(e -> saveScheduleForSelectedGroup());

        configHeader.getChildren().addAll(selectGroupLabel, groupComboBox, saveButton);

        scheduleGrid = new GridPane();
        scheduleGrid.setHgap(8);
        scheduleGrid.setVgap(8);

        buildGrid();

        getChildren().addAll(title, configHeader, scheduleGrid);
    }

    private void reloadDomainData() {
        availableGroups.clear();
        availableCourses.clear();
        availableGroups.addAll(courseService.getAllGroups());
        availableCourses.addAll(courseService.getAllCourses());
    }

    private void buildGrid() {
        scheduleGrid.getChildren().clear();

        Label timeHeader = new Label(MockDataProvider.i18n("schedule_time_col"));
        timeHeader.setFont(Font.font("System", FontWeight.BOLD, 14));
        timeHeader.setPadding(new Insets(10));
        timeHeader.setMinWidth(120);
        timeHeader.setStyle("-fx-background-color: #cbd5e1; -fx-alignment: center; -fx-background-radius: 6;");
        scheduleGrid.add(timeHeader, 0, 0);

        for (int i = 0; i < days.length; i++) {
            Label dayLabel = new Label(days[i]);
            dayLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            dayLabel.setPadding(new Insets(10));
            dayLabel.setMinWidth(160);
            dayLabel.setStyle("-fx-background-color: #e2e8f0; -fx-alignment: center; -fx-background-radius: 6;");
            scheduleGrid.add(dayLabel, i + 1, 0);
        }

        StudentGroup selectedGroup = groupComboBox.getValue();

        for (int slotIdx = 0; slotIdx < timeSlots.length; slotIdx++) {
            Label timeSlotLabel = new Label(timeSlots[slotIdx]);
            timeSlotLabel.setFont(Font.font("System", FontWeight.MEDIUM, 13));
            timeSlotLabel.setPadding(new Insets(15, 10, 15, 10));
            timeSlotLabel.setStyle("-fx-background-color: #f1f5f9; -fx-alignment: center; -fx-background-radius: 6;");
            scheduleGrid.add(timeSlotLabel, 0, slotIdx + 1);

            for (int dayIdx = 0; dayIdx < days.length; dayIdx++) {
                String key = dayIdx + "_" + slotIdx;
                String existingClass = scheduleData.get(key);

                Button cellBtn = new Button();
                cellBtn.setMinWidth(160);
                cellBtn.setPrefHeight(75);
                cellBtn.setWrapText(true);

                if (selectedGroup == null) {
                    cellBtn.setText(MockDataProvider.i18n("select_group_cell_msg"));
                    cellBtn.setDisable(true);
                    cellBtn.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 6; -fx-text-fill: #94a3b8;");
                } else if (existingClass != null) {
                    cellBtn.setText(existingClass);
                    cellBtn.setStyle("-fx-background-color: #dbeafe; -fx-border-color: #bfdbfe; -fx-border-radius: 6; -fx-text-fill: #1e40af; -fx-font-weight: bold; -fx-cursor: hand;");
                    cellBtn.setOnAction(e -> handleEditClass(key, existingClass));
                } else {
                    cellBtn.setText("+ " + MockDataProvider.i18n("add_class_btn_text"));
                    cellBtn.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-style: dashed; -fx-border-radius: 6; -fx-text-fill: #64748b; -fx-cursor: hand;");
                    cellBtn.setOnAction(e -> handleAddClass(key));
                }

                scheduleGrid.add(cellBtn, dayIdx + 1, slotIdx + 1);
            }
        }
    }

    private void loadScheduleForGroup(StudentGroup group) {
        scheduleData.clear();
        scheduleData.putAll(SchedulePlanStore.getPlanForGroup(group.getId()));

        if (scheduleData.isEmpty()) {
            String classText = formatGroupEntry(group);
            int dayIdx = (int) (group.getId() % 5);
            int slotIdx = (int) (group.getId() % timeSlots.length);
            scheduleData.put(dayIdx + "_" + slotIdx, classText);
        }
    }

    private void saveScheduleForSelectedGroup() {
        StudentGroup selected = groupComboBox.getValue();
        if (selected == null) {
            return;
        }

        SchedulePlanStore.savePlanForGroup(selected.getId(), scheduleData);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(MockDataProvider.i18n("alert_info_title"));
        alert.setHeaderText(null);
        alert.setContentText(MockDataProvider.getCurrentLocale().getLanguage().equals("en")
                ? "Schedule saved successfully."
                : "Plan zajęć został zapisany.");
        alert.showAndWait();
    }

    private void handleAddClass(String slotKey) {
        if (availableCourses.isEmpty()) {
            return;
        }

        ChoiceDialog<Course> dialog = new ChoiceDialog<>(availableCourses.get(0), availableCourses);
        dialog.setTitle(MockDataProvider.i18n("add_class_dialog_title"));
        dialog.setHeaderText(MockDataProvider.i18n("add_class_dialog_header"));
        dialog.setContentText(MockDataProvider.i18n("add_class_dialog_content"));

        dialog.showAndWait().ifPresent(course -> {
            String text = course.getName() + "\n" + (course.getLecturer() != null ? course.getLecturer().getFullName() : "");
            scheduleData.put(slotKey, text.trim());
            buildGrid();
        });
    }

    private void handleEditClass(String slotKey, String currentDetails) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(MockDataProvider.i18n("edit_class_title"));
        alert.setHeaderText(currentDetails);
        alert.setContentText(MockDataProvider.i18n("edit_class_confirm_delete"));

        ButtonType deleteBtn = new ButtonType(MockDataProvider.i18n("delete_btn_label"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType(MockDataProvider.i18n("cancel_btn_label"), ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(deleteBtn, cancelBtn);

        alert.showAndWait().ifPresent(type -> {
            if (type == deleteBtn) {
                scheduleData.remove(slotKey);
                buildGrid();
            }
        });
    }

    private String formatGroupEntry(StudentGroup group) {
        if (group == null) {
            return "";
        }
        String courseName = group.getCourse() != null ? group.getCourse().getName() : group.getName();
        String lecturerName = group.getLecturer() != null ? group.getLecturer().getFullName() : "";
        return lecturerName.isBlank() ? courseName : courseName + "\n" + lecturerName;
    }
}
