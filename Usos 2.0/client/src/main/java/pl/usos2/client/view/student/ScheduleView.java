package pl.usos2.client.view.student;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ScheduleView extends VBox {
    public ScheduleView() {
        setPadding(new Insets(30));
        setSpacing(20);

        Label title = new Label("Weekly Schedule");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));

        GridPane scheduleGrid = new GridPane();
        scheduleGrid.setHgap(10);
        scheduleGrid.setVgap(10);

        // Nagłówek tabeli (Dni tygodnia)
        String[] days = {"Time", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        for (int i = 0; i < days.length; i++) {
            Label dayLabel = new Label(days[i]);
            dayLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            dayLabel.setPadding(new Insets(10));
            dayLabel.setMinWidth(150);
            dayLabel.setStyle("-fx-background-color: #e2e8f0; -fx-alignment: center;");
            scheduleGrid.add(dayLabel, i, 0);
        }

        // Dodajemy kilka elementów
        addEntry(scheduleGrid, 1, 1, "09:00 - 10:30", "Advanced Algorithms\nA-301", "#dbeafe"); // Blue
        addEntry(scheduleGrid, 2, 1, "11:00 - 12:30", "Database Systems\nB-205", "#dcfce7"); // Green
        addEntry(scheduleGrid, 2, 2, "10:00 - 11:30", "Computer Networks\nC-102", "#f3e8ff"); // Purple

        getChildren().addAll(title, scheduleGrid);
    }

    private void addEntry(GridPane grid, int col, int row, String time, String info, String color) {
        VBox box = new VBox(5);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: " + color + "; -fx-border-color: #cbd5e1; -fx-border-radius: 5; -fx-background-radius: 5;");

        Label t = new Label(time);
        t.setFont(Font.font("System", FontWeight.BOLD, 11));
        Label i = new Label(info);
        i.setFont(Font.font("System", 12));

        box.getChildren().addAll(t, i);
        grid.add(box, col, row);
    }
}