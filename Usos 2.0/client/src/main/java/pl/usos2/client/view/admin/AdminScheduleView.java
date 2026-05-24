package pl.usos2.client.view.admin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pl.usos2.server.model.academic.Course;
import pl.usos2.server.model.academic.StudentGroup;
import pl.usos2.client.util.MockDataProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminScheduleView extends VBox {

    // Emulacja bazy danych/serwera dla prezentacji logiki biznesowej panelu administratora
    private List<StudentGroup> availableGroups = new ArrayList<>();
    private List<Course> availableCourses = new ArrayList<>();

    // Macierz przechowująca wpisy planu zajęć: Klucz = "Dzień_Slot" (np. "1_2" -> Wtorek, trzeci slot)
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
        setStyle("-fx-background-color: #f8fafc;");

        // Inicjalizacja danych testowych (grup oraz przedmiotów)
        initMockData();

        Label title = new Label(MockDataProvider.i18n("global_schedule_title"));
        title.setFont(Font.font("System", FontWeight.BOLD, 24));

        // --- PANEL CONFIGURACYJNY (Wybór grupy i zarządzanie) ---
        HBox configHeader = new HBox(15);
        configHeader.setAlignment(Pos.CENTER_LEFT);

        Label selectGroupLabel = new Label(MockDataProvider.i18n("select_group_prompt"));
        selectGroupLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));

        groupComboBox = new ComboBox<>();
        groupComboBox.getItems().addAll(availableGroups);
        groupComboBox.setPromptText(MockDataProvider.i18n("choose_group_holder"));
        groupComboBox.setMinWidth(250);

        // Reakcja na zmianę wybranej grupy studenckiej - ładowanie przypisanego planu
        groupComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadScheduleForGroup(newVal);
            }
        });

        configHeader.getChildren().addAll(selectGroupLabel, groupComboBox);

        // --- SIATKA PLANU ZAJĘĆ (GridPane) ---
        scheduleGrid = new GridPane();
        scheduleGrid.setHgap(12);
        scheduleGrid.setVgap(12);

        // Zbudowanie początkowej pustej matrycy (zablokowanej do momentu wyboru grupy)
        buildEmptyGrid();

        getChildren().addAll(title, configHeader, scheduleGrid);
    }

    private void buildEmptyGrid() {
        scheduleGrid.getChildren().clear();

        // Dodawanie nagłówka kolumny czasu
        Label timeHeader = new Label(MockDataProvider.i18n("schedule_time_col"));
        timeHeader.setFont(Font.font("System", FontWeight.BOLD, 14));
        timeHeader.setPadding(new Insets(10));
        timeHeader.setMinWidth(120);
        timeHeader.setStyle("-fx-background-color: #cbd5e1; -fx-alignment: center; -fx-background-radius: 6;");
        scheduleGrid.add(timeHeader, 0, 0);

        // Dodawanie nagłówków dni tygodnia (kolumny 1-5)
        for (int i = 0; i < DAYS.length; i++) {
            Label dayLabel = new Label(DAYS[i]);
            dayLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            dayLabel.setPadding(new Insets(10));
            dayLabel.setMinWidth(160);
            dayLabel.setStyle("-fx-background-color: #e2e8f0; -fx-alignment: center; -fx-background-radius: 6;");
            scheduleGrid.add(dayLabel, i + 1, 0);
        }

        StudentGroup selectedGroup = groupComboBox.getValue();

        // Generowanie wierszy dla każdego przedziału godzinowego
        for (int slotIdx = 0; slotIdx < TIME_SLOTS.length; slotIdx++) {
            Label timeSlotLabel = new Label(TIME_SLOTS[slotIdx]);
            timeSlotLabel.setFont(Font.font("System", FontWeight.MEDIUM, 13));
            timeSlotLabel.setPadding(new Insets(15, 10, 15, 10));
            timeSlotLabel.setStyle("-fx-background-color: #f1f5f9; -fx-alignment: center; -fx-background-radius: 6;");
            scheduleGrid.add(timeSlotLabel, 0, slotIdx + 1);

            for (int dayIdx = 0; dayIdx < DAYS.length; dayIdx++) {
                String key = dayIdx + "_" + slotIdx;
                String existingClass = scheduleData.get(key);

                Button cellBtn = new Button();
                cellBtn.setMinWidth(160);
                cellBtn.setPrefHeight(75);
                cellBtn.setWrapText(true);

                // Jeśli administrator nie wybrał grupy, komórki harmonogramu pozostają nieaktywne
                if (selectedGroup == null) {
                    cellBtn.setText(MockDataProvider.i18n("select_group_cell_msg"));
                    cellBtn.setDisable(true);
                    cellBtn.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 6; -fx-text-fill: #94a3b8;");
                } else if (existingClass != null) {
                    // Komórka zajęta przez zaplanowany przedmiot
                    cellBtn.setText(existingClass);
                    cellBtn.setStyle("-fx-background-color: #dbeafe; -fx-border-color: #bfdbfe; -fx-border-radius: 6; -fx-text-fill: #1e40af; -fx-font-weight: bold; -fx-cursor: hand;");
                    cellBtn.setOnAction(e -> handleEditClass(key, existingClass));
                } else {
                    // Pusta komórka gotowa do zaplanowania nowego kursu
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

        // Emulacja filtrowania danych harmonogramu w zależności od wybranej struktury grupy kierunkowej
        if (group.getName().contains("Informatyka")) {
            scheduleData.put("0_0", "Zaawansowane Algorytmy\nSala: 105 A\ndr Nowak");
            scheduleData.put("1_2", "Bazy Danych 2\nSala: 211 Mech\nprof. Kowalski");
            scheduleData.put("3_1", "Programowanie GUI\nSala: Laboratorium 3\nmgr Wiśniewski");
        } else if (group.getName().contains("Mechatronika")) {
            scheduleData.put("0_1", "Podstawy Robotyki\nSala: Hala Maszyn\nprof. Kowalski");
            scheduleData.put("2_3", "Matematyka stosowana\nSala: 302\ndr Nowak");
        }

        // Odświeżenie widoku siatki z aktywnymi przyciskami zarządzania
        buildEmptyGrid();
    }

    private void handleAddClass(String slotKey) {
        // Logika okna modalnego dodawania nowych zajęć do siatki
        ChoiceDialog<Course> dialog = new ChoiceDialog<>(availableCourses.get(0), availableCourses);
        dialog.setTitle(MockDataProvider.i18n("add_class_dialog_title"));
        dialog.setHeaderText(MockDataProvider.i18n("add_class_dialog_header"));
        dialog.setContentText(MockDataProvider.i18n("add_class_dialog_content"));

        dialog.showAndWait().ifPresent(course -> {
            scheduleData.put(slotKey, course.getName() + "\nSala: 102 Dynamic\nZajęcia ogólne");
            buildEmptyGrid();
        });
    }

    private void handleEditClass(String slotKey, String currentDetails) {
        // Możliwość szybkiego usunięcia lub modyfikacji istniejącego rekordu zajęć
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
                buildEmptyGrid();
            }
        });
    }

    private void initMockData() {
        // Inicjalizacja grup studenckich za pomocą pełnego 4-argumentowego konstruktora domenowego.
        // Przekazujemy: ID, nazwę grupy, a jako kurs i wykładowcę wstawiamy tymczasowo null dla celów prezentacyjnych.
        StudentGroup g1 = new StudentGroup(1L, "Informatyka - Semestr 3", null, null);
        StudentGroup g2 = new StudentGroup(2L, "Mechatronika - Semestr 1", null, null);
        availableGroups.add(g1);
        availableGroups.add(g2);

        // Inicjalizacja kursów akademickich za pomocą poprawnego 5-argumentowego konstruktora klasy Course.
        // Ponieważ klasa Course nie posiada domyślnego pustego konstruktora ani metod typu setter (np. setName),
        // wszystkie kluczowe parametry (ID, nazwa, kod, punkty ECTS, wykładowca) musimy przekazać bezpośrednio tutaj.
        Course c1 = new Course(1L, "Zaawansowane Algorytmy", "CS301", 6, null);
        Course c2 = new Course(2L, "Bazy Danych 2", "CS302", 5, null);
        Course c3 = new Course(3L, "Programowanie GUI", "CS405", 4, null);

        availableCourses.add(c1);
        availableCourses.add(c2);
        availableCourses.add(c3);
    }
}