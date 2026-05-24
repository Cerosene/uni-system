package pl.usos2.client.view.student;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pl.usos2.client.util.MockDataProvider;

/**
 * Widok ocen studenta podzielony na semestry za pomocą komponentu Accordion.
 * Obsługuje wielojęzyczność aplikacji (i18n) i dynamiczne odświeżanie.
 */
public class GradesView extends ScrollPane {

    private final VBox container;
    private final Label titleLabel;
    private final Accordion accordion;

    // Zakładka semestru przechowywana jako pole w celu dynamicznej aktualizacji
    private TitledPane semester3Pane;

    public GradesView() {
        setFitToWidth(true);
        setStyle("-fx-background-color: #f8fafc; -fx-background: #f8fafc;");

        container = new VBox(20);
        container.setPadding(new Insets(30));

        titleLabel = new Label();
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 26));
        container.getChildren().add(titleLabel);

        accordion = new Accordion();
        container.getChildren().add(accordion);

        // Budowanie struktury ocen
        buildGradesStructure();

        setContent(container);

        // Pierwsza konfiguracja języka
        refreshLocalization();

        // Słuchacz zmian językowych w systemie
        MockDataProvider.currentLocaleProperty().addListener((obs, oldLocale, newLocale) -> refreshLocalization());
    }

    /**
     * Tworzy strukturę zakładek semestralnych w komponencie Accordion.
     */
    private void buildGradesStructure() {
        semester3Pane = new TitledPane();

        VBox semesterContentBox = new VBox(10);
        semesterContentBox.setPadding(new Insets(15));
        semesterContentBox.setStyle("-fx-background-color: #f8fafc;");

        // Dodawanie wierszy z ocenami (Klucze i18n przedmiotów przekazywane do metody pomocniczej)
        semesterContentBox.getChildren().addAll(
                createGradeRow("ALG01", "subject_algorithms", "dr inż. Janusz Nowak", "6", 4.5, true),
                createGradeRow("DB202", "subject_databases", "prof. Maria Kowalska", "5", 5.0, true),
                createGradeRow("NET03", "subject_networks", "dr Adam Wiśniewski", "4", 2.0, false)
        );

        semester3Pane.setContent(semesterContentBox);
        accordion.getPanes().add(semester3Pane);
        accordion.setExpandedPane(semester3Pane);
    }

    /**
     * Tworzy pojedynczy wiersz tabeli ocen dla danego przedmiotu.
     */
    private HBox createGradeRow(String code, String nameKey, String lecturer, String ects, double grade, boolean isPassed) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 20, 12, 20));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1;");

        VBox subjectBox = new VBox(2);
        subjectBox.setMinWidth(250);

        Label nameLbl = new Label(MockDataProvider.i18n(nameKey));
        nameLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label codeLbl = new Label(code);
        codeLbl.setTextFill(Color.GRAY);
        subjectBox.getChildren().addAll(nameLbl, codeLbl);

        Label lectLbl = new Label(lecturer);
        lectLbl.setMinWidth(180);
        lectLbl.setTextFill(Color.web("#475569"));

        Label ectsLbl = new Label("ECTS: " + ects);
        ectsLbl.setMinWidth(80);
        ectsLbl.setTextFill(Color.web("#64748b"));

        Label gradeLbl = new Label(String.format("%.1f", grade));
        gradeLbl.setMinWidth(60);
        gradeLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        // Status zaliczenia przedmiotu
        Label statusLbl = new Label(isPassed ? MockDataProvider.i18n("status_passed") : MockDataProvider.i18n("status_failed"));
        statusLbl.setPadding(new Insets(4, 8, 4, 8));

        if (isPassed) {
            gradeLbl.setTextFill(Color.web("#10b981"));
            statusLbl.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #15803d; -fx-background-radius: 4; -fx-font-size: 11; -fx-font-weight: bold;");
        } else {
            gradeLbl.setTextFill(Color.web("#ef4444"));
            statusLbl.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-background-radius: 4; -fx-font-size: 11; -fx-font-weight: bold;");
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(subjectBox, lectLbl, ectsLbl, gradeLbl, spacer, statusLbl);

        // Przechowywanie klucza nazwy przedmiotu i statusu w celach późniejszego tłumaczenia
        row.setUserData(new Object[]{nameKey, isPassed});

        return row;
    }

    /**
     * Odświeża komponenty tekstowe widoku po zmianie języka aplikacji.
     */
    private void refreshLocalization() {
        titleLabel.setText(MockDataProvider.i18n("grades_title"));
        semester3Pane.setText(MockDataProvider.i18n("semester_3_label"));

        // Aktualizacja wierszy wewnątrz TitledPane
        if (semester3Pane.getContent() instanceof VBox) {
            VBox contentBox = (VBox) semester3Pane.getContent();
            for (javafx.scene.Node node : contentBox.getChildren()) {
                if (node instanceof HBox && node.getUserData() instanceof Object[]) {
                    Object[] data = (Object[]) node.getUserData();
                    String nameKey = (String) data[0];
                    boolean isPassed = (boolean) data[1];

                    HBox row = (HBox) node;

                    // Aktualizacja nazwy przedmiotu (wewnątrz VBox)
                    if (row.getChildren().get(0) instanceof VBox) {
                        VBox sBox = (VBox) row.getChildren().get(0);
                        if (sBox.getChildren().get(0) instanceof Label) {
                            ((Label) sBox.getChildren().get(0)).setText(MockDataProvider.i18n(nameKey));
                        }
                    }

                    // Aktualizacja etykiety statusu (ostatni element HBox)
                    javafx.scene.Node lastNode = row.getChildren().get(row.getChildren().size() - 1);
                    if (lastNode instanceof Label) {
                        ((Label) lastNode).setText(isPassed ? MockDataProvider.i18n("status_passed") : MockDataProvider.i18n("status_failed"));
                    }
                }
            }
        }
    }
}