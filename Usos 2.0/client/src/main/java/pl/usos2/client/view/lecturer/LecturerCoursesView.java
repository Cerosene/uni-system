package pl.usos2.client.view.lecturer;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pl.usos2.client.view.layout.MainLayout;
import pl.usos2.client.util.MockDataProvider;

/**
 * Widok zarządzania kursami prowadzonymi przez wykładowcę.
 * Obsługuje dynamiczne tłumaczenie i18n interfejsu graficznego.
 */
public class LecturerCoursesView extends ScrollPane {

    private final MainLayout mainLayout;
    private final Label titleLabel;
    private final FlowPane coursesGrid;

    public LecturerCoursesView(MainLayout mainLayout) {
        this.mainLayout = mainLayout;
        setFitToWidth(true);
        setStyle("-fx-background-color: #f8fafc; -fx-background: #f8fafc;");

        VBox container = new VBox(25);
        container.setPadding(new Insets(30));

        // Główny tytuł ekranu kursów
        titleLabel = new Label();
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 26));

        coursesGrid = new FlowPane(20, 20);

        // Generowanie struktury powiązanej z kluczami tłumaczeń i18n
        // Parametry: kod, klucz i18n nazwy, klucz i18n typu, liczba studentów
        coursesGrid.getChildren().addAll(
                createCourseCard("CS301", "subject_algorithms", "course_type_mix", 120),
                createCourseCard("CS302", "subject_databases", "course_type_lab", 45),
                createCourseCard("CS405", "subject_networks", "course_type_seminar", 20)
        );

        container.getChildren().addAll(titleLabel, coursesGrid);
        setContent(container);

        // Pierwsze ładowanie tekstów językowych
        refreshLocalization();

        // Nasłuchiwanie globalnej zmiany języka w aplikacji
        MockDataProvider.currentLocaleProperty().addListener((obs, oldLocale, newLocale) -> refreshLocalization());
    }

    /**
     * Buduje komponent karty pojedynczego przedmiotu akademickiego.
     */
    private VBox createCourseCard(String code, String nameKey, String typeKey, int studentCount) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setPrefWidth(280);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4);");

        // Zapisujemy klucze językowe w metadanych kontenera na potrzeby odświeżania na żywo
        card.setUserData(new Object[]{nameKey, typeKey, studentCount});

        Label codeLbl = new Label(code);
        codeLbl.setTextFill(Color.web("#3b82f6"));
        codeLbl.setFont(Font.font("System", FontWeight.BOLD, 12));

        Label nameLbl = new Label();
        nameLbl.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLbl.setWrapText(true);

        HBox infoRow = new HBox(10);
        Label typeLbl = new Label();
        typeLbl.setStyle("-fx-background-color: #f1f5f9; -fx-padding: 3 8; -fx-background-radius: 5; -fx-font-size: 12;");

        Label studentsLbl = new Label();
        studentsLbl.setStyle("-fx-background-color: #f1f5f9; -fx-padding: 3 8; -fx-background-radius: 5; -fx-font-size: 12;");

        infoRow.getChildren().addAll(typeLbl, studentsLbl);
        card.getChildren().addAll(codeLbl, nameLbl, infoRow);

        return card;
    }

    /**
     * Wywoływane automatycznie przy zmianie języka systemu w celu tłumaczenia elementów UI.
     */
    private void refreshLocalization() {
        titleLabel.setText(MockDataProvider.i18n("lecturer_courses_title"));

        // Przechodzimy przez wszystkie wygenerowane karty kursów
        for (javafx.scene.Node node : coursesGrid.getChildren()) {
            if (node instanceof VBox && node.getUserData() instanceof Object[]) {
                VBox card = (VBox) node;
                Object[] data = (Object[]) card.getUserData();

                String nameKey = (String) data[0];
                String typeKey = (String) data[1];
                int studentCount = (Integer) data[2];

                // Dynamiczna aktualizacja napisów wewnątrz karty
                Label nameLbl = (Label) card.getChildren().get(1);
                nameLbl.setText(MockDataProvider.i18n(nameKey));

                HBox infoRow = (HBox) card.getChildren().get(2);
                Label typeLbl = (Label) infoRow.getChildren().get(0);
                Label studentsLbl = (Label) infoRow.getChildren().get(1);

                typeLbl.setText(MockDataProvider.i18n(typeKey));
                studentsLbl.setText(studentCount + " " + MockDataProvider.i18n("course_students_suffix"));
            }
        }
    }
}