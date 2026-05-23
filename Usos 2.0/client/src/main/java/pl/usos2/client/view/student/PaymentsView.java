package pl.usos2.client.view.student;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pl.usos2.server.model.finance.Payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PaymentsView extends VBox {

    public PaymentsView() {
        // Konfiguracja głównego kontenera (odstępy i jasne tło w stylu Tailwind)
        setPadding(new Insets(30));
        setSpacing(25);
        setStyle("-fx-background-color: #f8fafc;");

        // Nagłówek widoku
        Label title = new Label("Moje Płatności");
        title.setFont(Font.font("System", FontWeight.BOLD, 26));

        // --- KARTA BILANSOWA (Balance Card) ---
        HBox balanceCard = new HBox(40); // Odstęp między sekcjami wewnątrz karty
        balanceCard.setPadding(new Insets(25, 30, 25, 30));
        balanceCard.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 12; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 10, 0, 0, 4);");
        balanceCard.setAlignment(Pos.CENTER_LEFT);

        // Sekcja: Suma do zapłaty (Zadłużenie)
        VBox toPayBox = new VBox(5);
        Label toPayTitle = new Label("Do zapłaty:");
        toPayTitle.setTextFill(Color.web("#64748b")); // Szary tekst pomocniczy
        toPayTitle.setFont(Font.font("System", FontWeight.MEDIUM, 14));

        Label toPayValue = new Label("250.00 PLN");
        toPayValue.setTextFill(Color.web("#e11d48")); // Czerwony kolor oznaczający kwotę do zapłaty
        toPayValue.setFont(Font.font("System", FontWeight.BOLD, 24));
        toPayBox.getChildren().addAll(toPayTitle, toPayValue);

        // Pionowy separator rozdzielający dane na karcie
        Separator separator = new Separator();
        separator.setOrientation(javafx.geometry.Orientation.VERTICAL);
        separator.setPrefHeight(40);

        // Sekcja: Indywidualny numer konta bankowego studenta
        VBox accountBox = new VBox(5);
        Label accountTitle = new Label("Indywidualny numer konta do wpłat:");
        accountTitle.setTextFill(Color.web("#64748b"));
        accountTitle.setFont(Font.font("System", FontWeight.MEDIUM, 14));

        Label accountValue = new Label("12 1050 1025 0000 0022 3456 7890");
        accountValue.setTextFill(Color.web("#1e293b")); // Ciemny, czytelny tekst
        accountValue.setFont(Font.font("System", FontWeight.BOLD, 16));
        accountBox.getChildren().addAll(accountTitle, accountValue);

        // Dodanie elementów do karty bilansowej
        balanceCard.getChildren().addAll(toPayBox, separator, accountBox);


        // --- TABELA PŁATNOŚCI (TableView) ---
        TableView<Payment> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(350);
        table.setStyle("-fx-background-radius: 8;");

        // Kolumna 1: Tytuł płatności
        TableColumn<Payment, String> titleCol = new TableColumn<>("Tytuł płatności");
        titleCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getTitle()));

        // Kolumna 2: Kwota (z doklejoną walutą PLN)
        TableColumn<Payment, String> amountCol = new TableColumn<>("Kwota");
        amountCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getAmount() != null ? d.getValue().getAmount().toString() + " PLN" : "0.00 PLN"
        ));

        // Kolumna 3: Termin płatności (Formatowanie LocalDate do String rrrr-mm-dd)
        TableColumn<Payment, String> dateCol = new TableColumn<>("Termin");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        dateCol.setCellValueFactory(d -> {
            LocalDate date = d.getValue().getDueDate();
            return new javafx.beans.property.SimpleStringProperty(date != null ? date.format(formatter) : "—");
        });

        // Kolumna 4: Status płatności
        TableColumn<Payment, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().isPaid() ? "Zapłacone" : "Nieopłacone"
        ));

        // Customowe renderowanie komórek statusu (Dynamiczna zmiana kolorów czcionki)
        statusCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("Nieopłacone")) {
                        setTextFill(Color.web("#e11d48")); // Kolor czerwony dla nieopłaconych
                        setStyle("-fx-font-weight: bold;");
                    } else {
                        setTextFill(Color.web("#10b981")); // Kolor zielony dla zapłaconych
                        setStyle("-fx-font-weight: bold;");
                    }
                }
            }
        });

        // Dodanie wszystkich skonfigurowanych kolumn do tabeli
        table.getColumns().addAll(titleCol, amountCol, dateCol, statusCol);

        // Ładowanie testowych danych do tabeli (Mock Data)
        table.getItems().addAll(getMockPayments());

        // Etykieta sekcji historii płatności
        Label historyLabel = new Label("Historia i nadchodzące opłaty:");
        historyLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 15));
        historyLabel.setTextFill(Color.web("#334155"));

        // Złożenie całego widoku VBox z przygotowanych komponentów
        getChildren().addAll(title, balanceCard, historyLabel, table);
    }

    /**
     * Metoda pomocnicza generująca testowe dane płatności (Mock Data).
     * Wykorzystuje rzeczywiste obiekty klas BigDecimal oraz LocalDate.
     */
    private List<Payment> getMockPayments() {
        List<Payment> list = new ArrayList<>();

        // Konstruktor przyjmuje parametry: (Long id, String title, BigDecimal amount, LocalDate dueDate, boolean isPaid)
        list.add(new Payment(1L, "Opłata za legitymację studencką", new BigDecimal("22.00"), LocalDate.of(2025, 10, 15), true));
        list.add(new Payment(2L, "Czesne - Semestr 3 (Rata 1/1)", new BigDecimal("2000.00"), LocalDate.of(2025, 11, 1), true));
        list.add(new Payment(3L, "Opłata za powtarzanie przedmiotu: Algorytmy Zaawansowane", new BigDecimal("250.00"), LocalDate.of(2026, 6, 15), false));
        list.add(new Payment(4L, "Ubezpieczenie NNW studenckie", new BigDecimal("50.00"), LocalDate.of(2025, 11, 30), true));

        return list;
    }
}