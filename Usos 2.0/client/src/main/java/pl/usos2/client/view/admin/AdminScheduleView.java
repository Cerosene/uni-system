package pl.usos2.client.view.admin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pl.usos2.server.model.academic.Course;
import pl.usos2.server.model.academic.StudentGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminScheduleView extends VBox {

    // Эмуляция bazy danych/serwera dla demonstracji (zamień na swoje serwisy)
    private List<StudentGroup> availableGroups = new ArrayList<>();
    private List<Course> availableCourses = new ArrayList<>();

    // Matryca przechowująca wpisy: Klucz = "Dzień_Sロット" (np. "1_2" -> Wtorek, drugi trzeci slot)
    // Wartość = Szczegóły zajęć w formacie tekstowym lub jako obiekt klasy modelowej
    private Map<String, String> scheduleData = new HashMap<>();

    private ComboBox<StudentGroup> groupComboBox;
    private GridPane scheduleGrid;

    private final String[] DAYS = {"Poniedziałek", "Wtorek", "Środa", "Czwartek", "Piątek"};
    private final String[] TIME_SLOTS = {
            "08:00 - 09:30",
            "09:45 - 11:15",
            "11:30 - 13:00",
            "13:15 - 14:45",
            "15:00 - 16:30"
    };

    public AdminScheduleView() {
        setPadding(new Insets(30));
        setSpacing(20);
        setStyle("-fx-background-color: #f8fafc;"); // Jasne, nowoczesne tło z Tailwind

        // 1. Dane demonstracyjne (w realnym projekcie pobierane z serwera przez API/Service)
        initMockData();

        // 2. Nagłówek panelu
        Label titleLabel = new Label("Panel Administratora: Zarządzanie Planem Zajęć");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));

        // 3. Górny pasek wyboru grupy dziekańskiej
        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(15));
        topBar.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        Label selectLabel = new Label("Wybierz grupę studencką:");
        selectLabel.setFont(Font.font("System", FontWeight.MEDIUM, 14));

        groupComboBox = new ComboBox<>();
        groupComboBox.setPromptText("-- Wybierz grupę --");
        groupComboBox.getItems().addAll(availableGroups);
        groupComboBox.setPrefWidth(250);

        // Konwerter, żeby ładnie wyświetlać nazwę grupy w ComboBox
        groupComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(StudentGroup item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
        groupComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(StudentGroup item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });

        // Obsługa zmiany grupy - ładujemy odpowiedni plan
        groupComboBox.setOnAction(e -> reloadScheduleForGroup());

        topBar.getChildren().addAll(selectLabel, groupComboBox);

        // 4. Inicjalizacja siatki GridPane dla planu zajęć
        scheduleGrid = new GridPane();
        scheduleGrid.setHgap(10);
        scheduleGrid.setVgap(10);
        scheduleGrid.setPadding(new Insets(10, 0, 0, 0));

        // Column Constraints (żeby kolumny miały równą szerokość)
        ColumnConstraints timeCol = new ColumnConstraints(120);
        scheduleGrid.getColumnConstraints().add(timeCol);
        for (int i = 0; i < DAYS.length; i++) {
            ColumnConstraints dayCol = new ColumnConstraints(180);
            dayCol.setHgrow(Priority.ALWAYS);
            scheduleGrid.getColumnConstraints().add(dayCol);
        }

        // Dodanie widoku do layoutu
        getChildren().addAll(titleLabel, topBar, scheduleGrid);

        // Na start budujemy pustą siatkę (póki nie wybrano grupy)
        buildEmptyGrid();
    }

    private void buildEmptyGrid() {
        scheduleGrid.getChildren().clear();

        // Kącik pusty w lewym górnym rogu
        Label emptyCorner = new Label("Godzina / Dzień");
        emptyCorner.setFont(Font.font("System", FontWeight.BOLD, 12));
        scheduleGrid.add(emptyCorner, 0, 0);

        // Nagłówki Dni Tygodnia (Kolumny)
        for (int d = 0; d < DAYS.length; d++) {
            Label dayLabel = new Label(DAYS[d]);
            dayLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setMaxWidth(Double.MAX_VALUE);
            dayLabel.setStyle("-fx-background-color: #1e293b; -fx-text-fill: white; -fx-padding: 8; -fx-background-radius: 5;");
            scheduleGrid.add(dayLabel, d + 1, 0);
        }

        // Nagłówki Godzin (Wiersze) + Przyciski slotów
        for (int t = 0; t < TIME_SLOTS.length; t++) {
            Label timeLabel = new Label(TIME_SLOTS[t]);
            timeLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
            timeLabel.setAlignment(Pos.CENTER);
            timeLabel.setMaxWidth(Double.MAX_VALUE);
            timeLabel.setStyle("-fx-background-color: #e2e8f0; -fx-padding: 10; -fx-background-radius: 5;");
            scheduleGrid.add(timeLabel, 0, t + 1);

            // Generowanie interaktywnych komórek dla każdego dnia w tym slocie czasowym
            for (int d = 0; d < DAYS.length; d++) {
                int dayIndex = d;
                int slotIndex = t;
                String cellKey = dayIndex + "_" + slotIndex;

                Button cellButton = new Button();
                cellButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                cellButton.setPrefHeight(70);

                // Sprawdzamy czy coś jest w tym slocie
                String entry = scheduleData.get(cellKey);
                if (entry == null || groupComboBox.getValue() == null) {
                    cellButton.setText("[ Puste pole ]\n+ Kliknij aby dodać");
                    cellButton.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-text-fill: #94a3b8; -fx-font-size: 11px;");
                } else {
                    cellButton.setText(entry);
                    cellButton.setStyle("-fx-background-color: #eff6ff; -fx-border-color: #2563eb; -fx-border-radius: 6; -fx-background-radius: 6; -fx-text-fill: #1e40af; -fx-font-weight: bold; -fx-font-size: 12px;");
                }

                // Blokada edycji jeżeli nie wybrano żadnej grupy
                if (groupComboBox.getValue() == null) {
                    cellButton.setDisable(true);
                }

                // Główna akcja administratora — edycja klikniętego slotu
                cellButton.setOnAction(e -> openEditDialog(cellKey, dayIndex, slotIndex));

                scheduleGrid.add(cellButton, d + 1, t + 1);
            }
        }
    }

    private void openEditDialog(String cellKey, int dayIndex, int slotIndex) {
        StudentGroup selectedGroup = groupComboBox.getValue();
        if (selectedGroup == null) return;

        // Tworzenie standardowego okna dialogowego JavaFX (Dialog)
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Edycja slotu: " + DAYS[dayIndex] + " (" + TIME_SLOTS[slotIndex] + ")");
        dialog.setHeaderText("Przypisz zajęcia dla grupy: " + selectedGroup.getName());

        ButtonType saveButtonType = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        ButtonType deleteButtonType = new ButtonType("Usuń zajęcia", ButtonBar.ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, deleteButtonType, ButtonType.CANCEL);

        // Formularz wewnątrz dialogu
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Course> courseCombo = new ComboBox<>();
        courseCombo.getItems().addAll(availableCourses);
        courseCombo.setPromptText("Wybierz przedmiot");
        // Wyświetlanie samej nazwy przedmiotu
        courseCombo.setCellFactory(p -> new ListCell<>(){
            protected void updateItem(Course item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        TextField roomField = new TextField();
        roomField.setPromptText("np. A-105, Magna");

        grid.add(new Label("Przedmiot / Kurs:"), 0, 0);
        grid.add(courseCombo, 1, 0);
        grid.add(new Label("Sala wykładowa:"), 0, 1);
        grid.add(roomField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Logika przycisków
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Course selectedCourse = courseCombo.getValue();
                String room = roomField.getText().trim();
                if (selectedCourse != null && !room.isEmpty()) {
                    // Łączymy dane (Kurs + Wykładowca z Twojej encji Course + Sala)
                    String lecturerName = (selectedCourse.getLecturer() != null) ?
                            selectedCourse.getLecturer().getLastName() : "Brak przypisanego";
                    return selectedCourse.getName() + "\nSala: " + room + "\n" + lecturerName;
                }
            } else if (dialogButton == deleteButtonType) {
                return "__DELETE__";
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result.equals("__DELETE__")) {
                scheduleData.remove(cellKey);
            } else {
                scheduleData.put(cellKey, result);
            }
            // Odświeżamy siatkę graficzną
            buildEmptyGrid();

            // Tutaj w prawdziwym systemie wysyłasz dane na serwer / zapisujesz do SQL:
            // scheduleService.saveEntry(selectedGroup.getId(), dayIndex, slotIndex, ...)
        });
    }

    private void reloadScheduleForGroup() {
        StudentGroup selectedGroup = groupComboBox.getValue();
        if (selectedGroup == null) return;

        // Czyszczenie starego widoku na ekranie
        scheduleData.clear();

        // W prawdziwej aplikacji byłoby wywołanie: scheduleData = database.getScheduleFor(selectedGroup.getId());
        // Na potrzeby demo generujemy automatycznie losowe zajęcia dla wybranej grupy:
        if (selectedGroup.getName().contains("Informatyka")) {
            scheduleData.put("0_0", "Zaawansowane Algorytmy\nSala: 105 A\ndr Nowak");
            scheduleData.put("1_2", "Bazy Danych 2\nSala: 211 Mech\nprof. Kowalski");
            scheduleData.put("3_1", "Programowanie GUI\nSala: Laboratorium 3\nmgr Wiśniewski");
        } else if (selectedGroup.getName().contains("Mechatronika")) {
            scheduleData.put("0_1", "Podstawy Robotyki\nSala: Hala Maszyn\nprof. Kowalski");
            scheduleData.put("2_3", "Matematyka stosowana\nSala: 302\ndr Nowak");
        }

        // Przebudowujemy siatkę z odblokowanymi przyciskami
        buildEmptyGrid();
    }

    private void initMockData() {
        // Tworzenie przykładowych danych na podstawie Twoich istniejących encji (Course, StudentGroup)
        // Wykorzystuje struktury konstruktorów, które masz w załączonych plikach (.java)

        availableCourses.add(new Course(1L, "Zaawansowane Algorytmy", "CS301", 6, null));
        availableCourses.add(new Course(2L, "Bazy Danych 2", "CS302", 5, null));
        availableCourses.add(new Course(3L, "Programowanie Interfejsów Graficznych", "CS303", 4, null));

        availableGroups.add(new StudentGroup(101L, "Informatyka - Grupa Laboratoryjna G1", null, null));
        availableGroups.add(new StudentGroup(102L, "Informatyka - Grupa Laboratoryjna G2", null, null));
        availableGroups.add(new StudentGroup(103L, "Mechatronika - Semestr 3", null, null));
    }
}