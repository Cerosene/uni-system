package pl.usos2.client.view.student;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pl.usos2.client.util.MockDataProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Widok tygodniowego planu zajęć studenta.
 * Renderuje siatkę zajęć z obsługą dynamicznego tłumaczenia i18n na język polski i angielski.
 */
public class ScheduleView extends VBox {

    private final Label titleLabel;
    private final GridPane scheduleGrid;

    // Lista przechowująca nagłówki dni tygodnia do dynamicznej aktualizacji
    private final List<Label> dayHeaders = new ArrayList<>();
    // Lista przechowująca wpisy zajęć do ponownego przetłumaczenia nazw przedmiotów
    private final List<ScheduleEntryNode> entryNodes = new ArrayList<>();

    public ScheduleView() {
        setPadding(new Insets(30));
        setSpacing(20);
        setStyle("-fx-background-color: #f8fafc;");

        // Główny tytuł ekranu
        titleLabel = new Label();
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));

        scheduleGrid = new GridPane();
        scheduleGrid.setHgap(10);
        scheduleGrid.setVgap(10);

        // Inicjalizacja struktury tabeli planu zajęć
        buildScheduleStructure();

        getChildren().addAll(titleLabel, scheduleGrid);

        // Pierwsze ładowanie tekstów językowych
        refreshLocalization();

        // Rejestracja słuchacza globalnej zmiany języka w systemie
        MockDataProvider.currentLocaleProperty().addListener((obs, oldLocale, newLocale) -> refreshLocalization());
    }

    /**
     * Tworzy strukturę siatki zajęć i dodaje stałe wpisy przedmiotów.
     */
    private void buildScheduleStructure() {
        // Dodanie 6 kolumn (Czas + 5 dni roboczych)
        for (int i = 0; i < 6; i++) {
            Label dayLabel = new Label();
            dayLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            dayLabel.setPadding(new Insets(10));
            dayLabel.setMinWidth(160);
            dayLabel.setStyle("-fx-background-color: #e2e8f0; -fx-alignment: center; -fx-background-radius: 4;");

            dayHeaders.add(dayLabel);
            scheduleGrid.add(dayLabel, i, 0);
        }

        // Dodawanie wpisów do planu lekcji (kolumna, wiersz, czas, klucz i18n przedmiotu, sala, kolor)
        addScheduleEntry(1, 1, "09:00 - 10:30", "subject_algorithms", "A-301", "#dbeafe"); // Poniedziałek
        addScheduleEntry(2, 1, "11:00 - 12:30", "subject_databases", "B-205", "#dcfce7");   // Wtorek
        addScheduleEntry(3, 2, "10:00 - 11:30", "subject_networks", "C-102", "#f3e8ff");    // Środa
    }

    /**
     * Metoda pomocnicza dodająca pojedynczy kafel zajęć do siatki planu.
     */
    private void addScheduleEntry(int col, int row, String time, String subjectKey, String room, String colorHex) {
        VBox box = new VBox(5);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 6; -fx-border-color: rgba(0,0,0,0.05);");

        Label timeLbl = new Label(time);
        timeLbl.setFont(Font.font("System", FontWeight.SEMI_BOLD, 11));
        timeLbl.setStyle("-fx-text-fill: #475569;");

        Label nameLbl = new Label();
        nameLbl.setFont(Font.font("System", FontWeight.BOLD, 13));
        nameLbl.setStyle("-fx-text-fill: #1e293b;");

        Label roomLbl = new Label(room);
        roomLbl.setFont(Font.font("System", 11));
        roomLbl.setStyle("-fx-text-fill: #64748b;");

        box.getChildren().addAll(timeLbl, nameLbl, roomLbl);
        scheduleGrid.add(box, col, row);

        // Zapisujemy referencję do kontenera w celu późniejszej re-translacji
        entryNodes.add(new ScheduleEntryNode(nameLbl, subjectKey));
    }

    /**
     * Odświeża komponenty tekstowe na ekranie po zmianie języka aplikacji.
     */
    private void refreshLocalization() {
        titleLabel.setText(MockDataProvider.i18n("schedule_title_main"));

        // Tablica kluczy i18n dla nagłówków kolumn siatki
        String[] scheduleKeys = {"schedule_col_time", "day_monday", "day_tuesday", "day_wednesday", "day_thursday", "day_friday"};
        for (int i = 0; i < dayHeaders.size(); i++) {
            dayHeaders.get(i).setText(MockDataProvider.i18n(scheduleKeys[i]));
        }

        // Tłumaczenie dynamicznych nazw przedmiotów wewnątrz planu lekcji
        for (ScheduleEntryNode entry : entryNodes) {
            entry.label.setText(MockDataProvider.i18n(entry.subjectKey));
        }
    }

    /**
     * Klasa pomocnicza przechowująca powiązanie etykiety UI z kluczem lokalizacyjnym.
     */
    private static class ScheduleEntryNode {
        Label label;
        String subjectKey;

        ScheduleEntryNode(Label label, String subjectKey) {
            this.label = label;
            this.subjectKey = subjectKey;
        }
    }
}