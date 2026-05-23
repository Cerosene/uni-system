package pl.usos2.client.view.student;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pl.usos2.client.view.lecturer.LecturerGradesView;
import pl.usos2.server.model.academic.Grade;

import java.util.List;
import java.util.stream.Collectors;

public class GradesView extends ScrollPane {

    private final VBox semesterContentBox;
    // Identyfikator aktualnie zalogowanego studenta (np. Dmytro Lytvyn – 1003)
    private final Long currentStudentId = 1003L;

    public GradesView() {
        setFitToWidth(true);
        setStyle("-fx-background-color: #f8fafc; -fx-background: #f8fafc;");

        VBox container = new VBox(20);
        container.setPadding(new Insets(30));

        Label title = new Label("Moje Oceny");
        title.setFont(Font.font("System", FontWeight.BOLD, 26));

        Accordion accordion = new Accordion();

        // Tworzymy zakładkę dla bieżącego semestru
        TitledPane sem3 = new TitledPane();
        sem3.setText("Semestr 3 (Zima 2025/26)");
        sem3.setExpanded(true);

        semesterContentBox = new VBox(10);
        semesterContentBox.getChildren().add(createTableHeader());

        // POBRANIE RZECZYWISTYCH DANYCH Z GRADE_SERVICE
        refreshGrades();

        sem3.setContent(semesterContentBox);
        accordion.getPanes().add(sem3);

        // Przycisk ręcznej aktualizacji danych na ekranie
        Button refreshBtn = new Button("🔄 Odśwież oceny");
        refreshBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold;");
        refreshBtn.setOnAction(e -> refreshGrades());

        container.getChildren().addAll(title, refreshBtn, accordion);
        setContent(container);
    }

    private void refreshGrades() {
        // Usuwamy stare wiersze, pozostawiając jedynie nagłówek tabeli (indeks 0)
        while (semesterContentBox.getChildren().size() > 1) {
            semesterContentBox.getChildren().remove(1);
        }

        // Otrzymujemy WSZYSTKIE oceny
        List<Grade> allGrades = LecturerGradesView.getSharedGradeService().getAllGrades();

        // Filtrujemy oceny tylko dla TEGO studenta
        List<Grade> studentGrades = allGrades.stream()
                .filter(g -> g.getStudent().getId().equals(currentStudentId))
                .collect(Collectors.toList());

        if (studentGrades.isEmpty()) {
            Label noGradesLabel = new Label("Brak wystawionych ocen w tym semestrze.");
            noGradesLabel.setPadding(new Insets(15));
            noGradesLabel.setTextFill(Color.GRAY);
            semesterContentBox.getChildren().add(noGradesLabel);
        } else {
            // Tworzymy ciągi znaków dynamicznie
            for (Grade grade : studentGrades) {
                HBox row = createGradeRow(
                        grade.getCourse().getCode(),
                        grade.getCourse().getName(),
                        grade.getLecturer().getAcademicTitle() + " " + grade.getLecturer().getLastName(),
                        String.valueOf(grade.getCourse().getEcts()),
                        grade.getValue(),
                        grade.getDescription(),
                        grade.getValue() >= 3.0 ? "#10b981" : "#e11d48" // Zielony, jeśli zdałeś, czerwony, jeśli 2,0
                );
                semesterContentBox.getChildren().add(row);
            }
        }
    }

    private HBox createTableHeader() {
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(5, 15, 5, 15));
        header.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 6;");

        Label col1 = createHeaderLabel("PRZEDMIOT", 200);
        Label col2 = createHeaderLabel("PROWADZĄCY", 150);
        Label col3 = createHeaderLabel("ECTS", 60);
        Label col4 = createHeaderLabel("OCENA", 80);
        Label col5 = createHeaderLabel("STATUS / UWAGI", 120);

        header.getChildren().addAll(col1, col2, col3, col4, col5);
        return header;
    }

    private Label createHeaderLabel(String text, double width) {
        Label l = new Label(text);
        l.setMinWidth(width);
        l.setTextFill(Color.web("#64748b"));
        l.setFont(Font.font("System", FontWeight.BOLD, 11));
        return l;
    }

    private HBox createGradeRow(String code, String name, String lecturer, String ects, double grade, String status, String statusColor) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 15, 10, 15));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #f1f5f9; -fx-border-radius: 8;");

        VBox subjectBox = new VBox(2);
        subjectBox.setMinWidth(200);
        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-font-weight: bold;");
        Label codeLbl = new Label(code);
        codeLbl.setTextFill(Color.GRAY);
        subjectBox.getChildren().addAll(nameLbl, codeLbl);

        Label lectLbl = new Label(lecturer);
        lectLbl.setMinWidth(150);
        Label ectsLbl = new Label(ects);
        ectsLbl.setMinWidth(60);

        Label gradeLbl = new Label(String.format("%.1f", grade));
        gradeLbl.setMinWidth(80);
        gradeLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        if (grade < 3.0) gradeLbl.setTextFill(Color.web("#e11d48"));
        else gradeLbl.setTextFill(Color.web("#10b981"));

        Label statusLbl = new Label(status);
        statusLbl.setMinWidth(120);
        statusLbl.setTextFill(Color.web(statusColor));
        statusLbl.setStyle("-fx-font-weight: bold;");

        row.getChildren().addAll(subjectBox, lectLbl, ectsLbl, gradeLbl, statusLbl);
        return row;
    }
}