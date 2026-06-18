package pl.usos2.client.view.student;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pl.usos2.client.util.MockDataProvider;
import pl.usos2.client.view.admin.AdminPaymentsView;
import pl.usos2.client.view.admin.AdminScheduleView;
import pl.usos2.client.view.admin.AdminTicketsView;
import pl.usos2.client.view.admin.EmployeeListView;
import pl.usos2.client.view.admin.SystemRequestsView;
import pl.usos2.client.view.admin.UserManagementView;
import pl.usos2.client.view.layout.MainLayout;
import pl.usos2.client.view.lecturer.LecturerCoursesView;
import pl.usos2.client.view.lecturer.LecturerGradesView;
import pl.usos2.client.view.lecturer.LecturerMessagesView;
import pl.usos2.client.view.rental.StudentRentalsView;
import pl.usos2.server.model.academic.StudentGroup;
import pl.usos2.server.model.enumtype.ServiceTicketStatus;
import pl.usos2.server.model.enumtype.UserRole;
import pl.usos2.server.model.user.Lecturer;
import pl.usos2.server.model.user.Student;
import java.util.List; 
import pl.usos2.server.model.academic.Grade; 


import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.stream.Collectors;

public class DashboardView extends ScrollPane {
    private final MainLayout mainLayout;
    private final UserRole role;
    private final VBox mainContainer;

    private Label welcomeLabel;
    private Label infoLabel;
    private Label statsTitleLabel;
    private Label actionsTitleLabel;

    public DashboardView(UserRole role, MainLayout mainLayout) {
        this.mainLayout = mainLayout;
        this.role = role;

        setFitToWidth(true);
        setStyle("-fx-background-color: #f8fafc; -fx-background: #f8fafc;");

        mainContainer = new VBox(30);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setStyle("-fx-background-color: #f8fafc;");

        createHeaderSection();
        createStatsSection();
        createQuickActionsSection();

        setContent(mainContainer);
    }

    private void createHeaderSection() {
        VBox headerBox = new VBox(8);
        headerBox.setPadding(new Insets(20));
        headerBox.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 10, 0, 0, 2);");

        welcomeLabel = new Label(MockDataProvider.i18n("dash_welcome") + ", " + mainLayout.getCurrentUser().getFullName() + "!");
        welcomeLabel.setFont(Font.font("System", FontWeight.BOLD, 26));
        welcomeLabel.setTextFill(Color.web("#1e293b"));

        infoLabel = new Label(MockDataProvider.i18n("dash_info_sub"));
        infoLabel.setFont(Font.font("System", 14));
        infoLabel.setTextFill(Color.web("#64748b"));

        headerBox.getChildren().addAll(welcomeLabel, infoLabel);
        mainContainer.getChildren().add(headerBox);
    }

    private void createStatsSection() {
        VBox statsWrapper = new VBox(15);

        statsTitleLabel = new Label(MockDataProvider.i18n("dash_stats_title"));
        statsTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        statsTitleLabel.setTextFill(Color.web("#334155"));
        statsWrapper.getChildren().add(statsTitleLabel);

        FlowPane statsGrid = new FlowPane(20, 20);

        if (role == UserRole.STUDENT) {
            Student student = (Student) mainLayout.getCurrentUser();
            String averageGradeValue = calculateAverageGradeLabel();
            String ectsValue = calculateEctsLabel(student);
            String studentGroupValue = calculateStudentGroupLabel(student);
            String unpaidBalanceValue = calculateUnpaidBalanceLabel(student);
            statsGrid.getChildren().addAll(
                    createStatCard("dash_stat_gpa", averageGradeValue, "dash_stat_gpa_sub", "#3b82f6"),
                    createStatCard("dash_stat_ects", ectsValue, "dash_stat_ects_sub", "#10b981"),
                    createStatCard("dash_stat_group", studentGroupValue, "", "#6d28d9"),
                    createStatCard("dash_stat_payments", unpaidBalanceValue, "dash_stat_payments_sub", "#ef4444")
            );
        } else if (role == UserRole.LECTURER) {
            Lecturer lecturer = (Lecturer) mainLayout.getCurrentUser();
            String coursesCount = String.valueOf(mainLayout.getContext().getCourseService().getCoursesForLecturer(lecturer).size());
            String studentsCount = String.valueOf(mainLayout.getContext().getCourseService().countActiveStudentsForLecturer(lecturer));
            String unreadMessagesCount = String.valueOf(mainLayout.getContext().getMessageService().getUnreadInbox(lecturer).size());
            statsGrid.getChildren().addAll(
                    createStatCard("dash_stat_courses", coursesCount, "dash_stat_courses_sub", "#8b5cf6"),
                    createStatCard("dash_stat_students", studentsCount, "dash_stat_students_sub", "#f59e0b"),
                    createStatCard("dash_stat_messages", unreadMessagesCount, "dash_stat_messages_sub", "#06b6d4")
            );
        } else if (role == UserRole.ADMINISTRATOR) {
            String totalUsers = String.valueOf(mainLayout.getContext().getAuthService().getAllUsers().size());
            String pendingRequests = String.valueOf(mainLayout.getContext().getRequestService().getPendingRequests().size());
            int openOrInProgress = mainLayout.getContext().getServiceTicketService().getTicketsByStatus(ServiceTicketStatus.OPEN).size()
                    + mainLayout.getContext().getServiceTicketService().getTicketsByStatus(ServiceTicketStatus.IN_PROGRESS).size();
            String openTickets = String.valueOf(openOrInProgress);

            statsGrid.getChildren().addAll(
                    createStatCard("dash_stat_total_users", totalUsers, "dash_stat_total_users_sub", "#3b82f6"),
                    createStatCard("dash_stat_active_req", pendingRequests, "dash_stat_active_req_sub", "#ec4899"),
                    createStatCard("dash_stat_system_status", openTickets, "dash_stat_system_status_sub", "#10b981")
            );
        }

        statsWrapper.getChildren().add(statsGrid);
        mainContainer.getChildren().add(statsWrapper);
    }

    private void createQuickActionsSection() {
        VBox actionsWrapper = new VBox(15);

        actionsTitleLabel = new Label(MockDataProvider.i18n("dash_actions_title"));
        actionsTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        actionsTitleLabel.setTextFill(Color.web("#334155"));
        actionsWrapper.getChildren().add(actionsTitleLabel);

        FlowPane actionsGrid = new FlowPane(15, 15);

        if (role == UserRole.STUDENT) {
            actionsGrid.getChildren().addAll(
                    createActionButton("nav_grades", "Oceny", "#3b82f6", () -> mainLayout.setContent(new GradesView(mainLayout.getCurrentUser(), mainLayout.getContext().getGradeService(), mainLayout.getContext().getCourseService()))),
                    createActionButton("nav_schedule", "Plan zajęć", "#10b981", () -> mainLayout.setContent(new ScheduleView(mainLayout.getCurrentUser(), mainLayout.getContext().getCourseService(), mainLayout.getContext().getScheduleService()))),
                    createActionButton("nav_applications", "Wnioski", "#8b5cf6", () -> mainLayout.setContent(new ApplicationsView(mainLayout.getCurrentUser(), mainLayout.getContext().getRequestService()))),
                    createActionButton("nav_payments", "Płatności", "#ef4444", () -> mainLayout.setContent(new PaymentsView(mainLayout.getCurrentUser(), mainLayout.getContext().getPaymentService()))),
                    createActionButton("nav_messages", "Wiadomości", "#06b6d4", () -> mainLayout.setContent(new MessagesView(mainLayout.getCurrentUser(), mainLayout.getContext().getMessageService(), mainLayout.getContext().getAuthService()))),
                    createActionButton("nav_tickets", "Zgłoszenia", "#f59e0b", () -> mainLayout.setContent(new TicketsView(mainLayout.getCurrentUser(), mainLayout.getContext().getServiceTicketService()))),
                    createActionButton("nav_rental", "Wypożyczenia", "#3b82f6", () -> mainLayout.setContent(new StudentRentalsView(mainLayout.getCurrentUser(), mainLayout.getContext().getRentalService())))

            );
        } else if (role == UserRole.LECTURER) {
            actionsGrid.getChildren().addAll(
                    createActionButton("nav_my_courses", "Moje kursy", "#8b5cf6", () -> mainLayout.setContent(new LecturerCoursesView(mainLayout.getCurrentUser(), mainLayout.getContext().getCourseService()))),
                    createActionButton("nav_add_grades", "Wystaw oceny", "#10b981", () -> mainLayout.setContent(new LecturerGradesView(mainLayout.getCurrentUser(), mainLayout.getContext().getGradeService()))),
                    createActionButton("nav_messages", "Wiadomości", "#06b6d4", () -> mainLayout.setContent(new LecturerMessagesView(mainLayout.getCurrentUser(), mainLayout.getContext().getMessageService(), mainLayout.getContext().getAuthService()))),
                    createActionButton("nav_schedule", "Plan zajęć", "#10b981", () -> mainLayout.setContent(new ScheduleView(mainLayout.getCurrentUser(), mainLayout.getContext().getCourseService(), mainLayout.getContext().getScheduleService())))
            );
        } else if (role == UserRole.ADMINISTRATOR) {
            actionsGrid.getChildren().addAll(
                    createActionButton("nav_manage_users", "Użytkownicy", "#2563eb", () -> mainLayout.setContent(new UserManagementView(mainLayout.getContext().getAuthService(), mainLayout.getContext().getCourseService()))),
                    createActionButton("nav_manage_schedule", "Edycja planu", "#f59e0b", () -> mainLayout.setContent(new AdminScheduleView(mainLayout.getContext().getCourseService(), mainLayout.getContext().getScheduleService(), mainLayout.getContext().getAuthService()))),
                    createActionButton("nav_admin_requests", "Wnioski", "#ec4899", () -> mainLayout.setContent(new SystemRequestsView(mainLayout.getContext().getRequestService()))),
                    createActionButton("nav_admin_tickets", "Zgłoszenia", "#f97316", () -> mainLayout.setContent(new AdminTicketsView(mainLayout.getContext().getServiceTicketService()))),
                    createActionButton("nav_admin_payments", "Płatności", "#14b8a6", () -> mainLayout.setContent(new AdminPaymentsView(mainLayout.getContext().getPaymentService()))),
                    createActionButton("nav_employee_dir", "Pracownicy", "#64748b", () -> mainLayout.setContent(new EmployeeListView(mainLayout.getContext().getEmployeeService())))
            );
        }

        actionsWrapper.getChildren().add(actionsGrid);
        mainContainer.getChildren().add(actionsWrapper);
    }

    private VBox createStatCard(String titleKey, String value, String subKey, String accentColor) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(18, 22, 18, 22));
        card.setPrefWidth(260);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 8, 0, 0, 4);");

        Pane accentBar = new Pane();
        accentBar.setPrefHeight(4);
        accentBar.setPrefWidth(40);
        accentBar.setMaxWidth(40);
        accentBar.setStyle("-fx-background-color: " + accentColor + "; -fx-background-radius: 2;");

        Label titleLbl = new Label(MockDataProvider.i18n(titleKey));
        titleLbl.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        titleLbl.setTextFill(Color.web("#64748b"));

        Label valueLbl = new Label(value);
        valueLbl.setFont(Font.font("System", FontWeight.BOLD, 24));
        valueLbl.setTextFill(Color.web("#0f172a"));

        Label subLbl = new Label(MockDataProvider.i18n(subKey));
        subLbl.setFont(Font.font("System", 11));
        subLbl.setTextFill(Color.web("#94a3b8"));

        card.getChildren().addAll(accentBar, titleLbl, valueLbl, subLbl);
        card.setUserData(new String[]{titleKey, subKey});

        return card;
    }

    private Button createActionButton(String textKey, String fallbackText, String colorHex, Runnable action) {
        Button btn = new Button(MockDataProvider.i18n(textKey));
        if (btn.getText().equals(textKey) || btn.getText().isEmpty()) {
            btn.setText(fallbackText);
        }

        btn.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        btn.setPrefSize(200, 50);
        btn.setStyle("-fx-background-color: white; -fx-text-fill: " + colorHex + "; -fx-border-color: " + colorHex + "; -fx-border-width: 1.5; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;");

        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + colorHex + "; -fx-text-fill: white; -fx-border-color: " + colorHex + "; -fx-border-width: 1.5; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: white; -fx-text-fill: " + colorHex + "; -fx-border-color: " + colorHex + "; -fx-border-width: 1.5; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;"));

        btn.setOnAction(e -> action.run());
        btn.setUserData(textKey);

        return btn;
    }

    private String calculateAverageGradeLabel() {
        if (!(mainLayout.getCurrentUser() instanceof Student student)) {
            return "-";
        }

        double average = mainLayout.getContext().getGradeService().getAverageGradeForStudent(student);
        if (average <= 0.0) {
            return MockDataProvider.getCurrentLocale().getLanguage().equals("en") ? "No grades" : "Brak ocen";
        }

        Locale locale = MockDataProvider.getCurrentLocale().getLanguage().equals("en")
                ? Locale.ENGLISH
                : new Locale("pl", "PL");
        DecimalFormat format = new DecimalFormat("0.00", new DecimalFormatSymbols(locale));
        return format.format(average);
    }

    private String calculateEctsLabel(Student student) {
      
        List<Grade> studentGrades = mainLayout.getContext().getGradeService().getGradesForStudent(student);

       
        int earnedEcts = studentGrades.stream()
                .filter(grade -> grade.getValue() >= 3.0) 
                .map(Grade::getCourse)
                .filter(course -> course != null)
                .mapToInt(course -> Math.max(course.getEcts(), 0))
                .sum();

        return earnedEcts + " / 210";
    }

    private String calculateStudentGroupLabel(Student student) {
        if (student == null) {
            return MockDataProvider.i18n("dash_stat_group_no_group");
        }
        // Assuming a student belongs to at most one primary group for display on the dashboard.
        // If a student can belong to multiple, this logic might need adjustment (e.g., concatenating names).
        return mainLayout.getContext().getCourseService().getGroupsForStudent(student).stream()
                .findFirst()
                .map(StudentGroup::getName)
                .orElse(MockDataProvider.i18n("dash_stat_group_no_group"));
    }


    private String calculateUnpaidBalanceLabel(Student student) {
        BigDecimal unpaid = mainLayout.getContext().getPaymentService().getUnpaidPaymentsForStudent(student).stream()
                .map(payment -> payment.getAmount() == null ? BigDecimal.ZERO : payment.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Locale locale = MockDataProvider.getCurrentLocale().getLanguage().equals("en")
                ? Locale.ENGLISH
                : new Locale("pl", "PL");
        DecimalFormat format = new DecimalFormat("0.00", new DecimalFormatSymbols(locale));
        return format.format(unpaid) + " PLN";
    }

    public void translate() {
        welcomeLabel.setText(MockDataProvider.i18n("dash_welcome") + ", " + mainLayout.getCurrentUser().getFullName() + "!");
        infoLabel.setText(MockDataProvider.i18n("dash_info_sub"));
        statsTitleLabel.setText(MockDataProvider.i18n("dash_stats_title"));
        actionsTitleLabel.setText(MockDataProvider.i18n("dash_actions_title"));

        for (Node node : mainContainer.getChildren()) {
            if (node instanceof VBox wrapperBox) {
                for (Node subNode : wrapperBox.getChildren()) {
                    if (subNode instanceof FlowPane flowPane) {
                        translateGroup(flowPane);
                    }
                }
            }
        }
    }

    private void translateGroup(FlowPane pane) {
        for (Node node : pane.getChildren()) {
            if (node instanceof VBox card && node.getUserData() instanceof String[] keys) {
                int labelIndex = 0;
                for (Node inner : card.getChildren()) {
                    if (inner instanceof Label label) {
                        if (labelIndex == 0) {
                            label.setText(MockDataProvider.i18n(keys[0]));
                            labelIndex++;
                        } else if (labelIndex == 1 && card.getChildren().size() == 4) {
                            labelIndex++;
                        } else {
                            label.setText(MockDataProvider.i18n(keys[1]));
                        }
                    }
                }
            } else if (node instanceof Button btn && node.getUserData() instanceof String key) {
                btn.setText(MockDataProvider.i18n(key));
            }
        }
    }
}
