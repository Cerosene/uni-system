package pl.usos2.client.util;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import pl.usos2.server.model.finance.Payment;
import pl.usos2.server.model.request.Message;
import pl.usos2.server.model.request.Request;
import pl.usos2.server.model.enumtype.MessageStatus;
import pl.usos2.server.model.enumtype.RequestStatus;
import pl.usos2.server.model.enumtype.RequestType;
import pl.usos2.server.model.enumtype.Semester;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.model.user.Lecturer;
import pl.usos2.server.model.user.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Globalny dostawca danych demonstracyjnych (Mock Data) dla całej aplikacji.
 * Zapewnia spójność danych pomiędzy różnymi panelami oraz zarządza lokalizacją (językiem).
 */
public class MockDataProvider {

    // --- SYSTEM LOKALIZACJI (I18N) ---
    private static final ObjectProperty<Locale> currentLocale = new SimpleObjectProperty<>(new Locale("pl"));

    public static ObjectProperty<Locale> currentLocaleProperty() {
        return currentLocale;
    }

    public static Locale getCurrentLocale() {
        return currentLocale.get();
    }

    public static void setCurrentLocale(Locale locale) {
        currentLocale.set(locale);
    }

    /**
     * Tłumaczy klucz tekstowy na wybrany język systemu.
     */
    /**
     * Tłumaczy klucz tekstowy na wybrany język systemu (wersja rozszerzona dla Dashboardu).
     */
    public static String i18n(String key) {
        boolean isEn = currentLocale.get().getLanguage().equals("en");
        switch (key) {
            // --- MENU BOCZNE (SIDEBAR) ---
            case "dashboard": return isEn ? "Dashboard" : "Pulpit";
            case "schedule": return isEn ? "Schedule" : "Plan zajęć";
            case "grades": return isEn ? "Grades" : "Oceny";
            case "messages": return isEn ? "Messages" : "Wiadomości";
            case "requests": return isEn ? "Requests" : "Wnioski";
            case "payments": return isEn ? "Payments" : "Opłaty";
            case "tickets": return isEn ? "Service Tickets" : "Zgłoszenia";
            case "logout": return isEn ? "Log Out" : "Wyloguj";
            case "users": return isEn ? "Users" : "Użytkownicy";
            case "employees": return isEn ? "Employees" : "Pracownicy";
            case "course": return isEn ? "My Courses" : "Moje kursy";

            // --- PANEL GŁÓWNY (DASHBOARD GENERAL) ---
            // --- PRZYCISKI SZYBKICH AKCJI (QUICK ACTIONS NAV) ---
            case "nav_grades": return isEn ? "Grades" : "Oceny";
            case "nav_schedule": return isEn ? "Schedule" : "Plan zajęć";
            case "nav_applications": return isEn ? "Applications" : "Wnioski";
            case "nav_payments": return isEn ? "Payments" : "Płatności";
            case "nav_messages": return isEn ? "Messages" : "Wiadomości";
            case "nav_thesis": return isEn ? "Thesis Topic" : "Praca dyplomowa";

            // --- KARTY STATYSTYK STUDENTA (STUDENT STAT CARDS) ---
            case "gpa_title": return isEn ? "Current GPA" : "Aktualna Średnia";
            case "gpa_sub": return isEn ? "+0.3 od zeszłego semestru" : "+0.3 od zeszłego semestru";
            case "ects_title": return isEn ? "ECTS Credits" : "Punkty ECTS";
            case "ects_sub": return isEn ? "Required: 180" : "Wymagane: 180";
            case "finances_title": return isEn ? "Financial Balance" : "Stan Konta";
            case "finances_sub": return isEn ? "No pending payments" : "Brak zaległych opłat";

            // --- KARTY AKCJI STUDENTA (STUDENT ACTION CARDS) ---
            case "schedule_title": return isEn ? "Weekly Schedule" : "Plan Zajęć";
            case "schedule_desc": return isEn ? "Check your classes and hours" : "Sprawdź swoje zajęcia i godziny sal";
            case "grades_title": return isEn ? "My Grades" : "Moje Oceny";
            case "grades_desc": return isEn ? "View your semester achievements" : "Przejrzyj swoje osiągnięcia semestralne";
            case "messages_title": return isEn ? "Messages Box" : "Skrzynka Wiadomości";
            case "messages_desc": return isEn ? "Contact your university professors" : "Skontaktuj się ze swoimi prowadzącymi";
            case "requests_title": return isEn ? "Submit Request" : "Złóż Wniosek";
            case "requests_desc": return isEn ? "Apply for scholarships or leaves" : "Aplikuj o stypendia lub urlopy dziekańskie";

            // --- KARTY WYKŁADOWCY (LECTURER CARDS) ---
            case "lect_courses_count": return isEn ? "Active Courses" : "Aktywne Kursy";
            case "lect_courses_sub": return isEn ? "Current semester" : "Bieżący semestr";
            case "lect_students_count": return isEn ? "Total Students" : "Suma Studentów";
            case "lect_students_sub": return isEn ? "In all your courses" : "Na wszystkich Twoich kursach";

            // --- KARTY ADMINISTRATORA (ADMIN CARDS) ---
            case "admin_total_users": return isEn ? "Total Users" : "Wszyscy Użytkownicy";
            case "admin_users_sub": return isEn ? "Active accounts" : "Aktywne konta";
            case "admin_pending_req": return isEn ? "Pending Requests" : "Oczekujące Wnioski";
            case "admin_req_sub": return isEn ? "Requires manual review" : "Wymaga ręcznej weryfikacji";

            // --- EKRAN WNIOSKÓW (APPLICATIONS VIEW) ---
            case "requests_title_screen": return isEn ? "Student Applications" : "Składanie Wniosków";
            case "new_request_label": return isEn ? "Submit New Request:" : "Nowy wniosek:";
            case "select_request_type": return isEn ? "Select request type..." : "Wybierz rodzaj wniosku...";
            case "request_content_prompt": return isEn ? "Justification of the request..." : "Uzasadnienie wniosku...";
            case "submit_request_btn": return isEn ? "Submit Request" : "Złóż wniosek";
            case "my_requests_history": return isEn ? "My Submitted Requests:" : "Moje wnioski:";
            case "fill_all_fields_error": return isEn ? "Please fill in all fields!" : "Proszę wypełnić wszystkie pola!";
            case "request_success_msg": return isEn ? "The request has been submitted successfully." : "Wniosek został pomyślnie złożony.";

            // --- EKRAN OCEN (GRADES VIEW) ---
            case "semester_3_label": return isEn ? "Semester 3 (Current)" : "Semestr 3 (Bieżący)";
            case "status_passed": return isEn ? "PASSED" : "ZALICZONY";
            case "status_failed": return isEn ? "FAILED" : "DO POPRAWY";
            case "subject_algorithms": return isEn ? "Advanced Algorithms" : "Algorytmy Zaawansowane";
            case "subject_databases": return isEn ? "Database Systems" : "Systemy Baz Danych";
            case "subject_networks": return isEn ? "Computer Networks" : "Sieci Komputerowe";

            // --- EKRAN WIADOMOŚCI (MESSAGES VIEW) ---
            case "messages_title_main": return isEn ? "Messages Box" : "Skrzynka Wiadomości";
            case "tab_new_message": return isEn ? "New Message" : "Napisz wiadomość";
            case "select_lecturer_label": return isEn ? "Recipient (Lecturer):" : "Odbiorca (Wykładowca):";
            case "select_lecturer_prompt": return isEn ? "Choose lecturer..." : "Wybierz wykładowcę...";
            case "message_content_label": return isEn ? "Message content:" : "Treść wiadomości:";
            case "message_area_prompt": return isEn ? "Type your message here..." : "Wpisz treść wiadomości tutaj...";
            case "send_message_btn": return isEn ? "Send Message" : "Wyślij wiadomość";
            case "tab_inbox": return isEn ? "Inbox" : "Skrzynka odbiorcza";
            case "refresh_btn": return isEn ? "Refresh" : "Odśwież";
            case "received_messages_label": return isEn ? "Received messages:" : "Otrzymane wiadomości:";
            case "preview_content_label": return isEn ? "Message preview:" : "Treść wybranej wiadomości:";
            case "alert_warn_title": return isEn ? "Warning" : "Ostrzeżenie";
            case "alert_info_title": return isEn ? "Information" : "Informacja";
            case "message_sent_success": return isEn ? "Message has been sent successfully!" : "Wiadomość została pomyślnie wysłana!";

            // --- EKRAN PŁATNOŚCI (PAYMENTS VIEW) ---
            case "payments_title_main": return isEn ? "My Payments" : "Moje Płatności";
            case "account_balance_status": return isEn ? "Current Financial Account Balance:" : "Aktualny stan konta finansowego:";
            case "balance_sub_info": return isEn ? "All mandatory settlements are up to date" : "Wszystkie wymagane rozliczenia są uregulowane";
            case "payments_history_label": return isEn ? "History and Upcoming Fees:" : "Historia i nadchodzące opłaty:";
            case "col_payment_title": return isEn ? "Title" : "Tytuł zobowiązania";
            case "col_payment_amount": return isEn ? "Amount" : "Kwota";
            case "col_payment_date": return isEn ? "Due Date" : "Termin płatności";
            case "col_payment_status": return isEn ? "Status" : "Status";
            case "payment_status_paid": return isEn ? "PAID" : "OPŁACONE";
            case "payment_status_unpaid": return isEn ? "UNPAID" : "NIEOPŁACONE";

            // --- EKRAN HARMONOGRAMU (SCHEDULE VIEW) ---
            case "schedule_title_main": return isEn ? "Weekly Class Schedule" : "Tygodniowy Plan Zajęć";
            case "schedule_col_time": return isEn ? "Time" : "Godzina";
            case "day_monday": return isEn ? "Monday" : "Poniedziałek";
            case "day_tuesday": return isEn ? "Tuesday" : "Wtorek";
            case "day_wednesday": return isEn ? "Wednesday" : "Środa";
            case "day_thursday": return isEn ? "Thursday" : "Czwartek";
            case "day_friday": return isEn ? "Friday" : "Piątek";

            // --- EKRAN PRAC DYPLOMOWYCH (THESIS VIEW) ---
            case "thesis_title_main": return isEn ? "Thesis Advisor & Topic Selection" : "Wybór Promotora i Tematu Pracy";
            case "thesis_apply_btn": return isEn ? "Enroll in Topic" : "Zapisz się na temat";
            case "thesis_col_promotor": return isEn ? "Supervisor / Advisor" : "Promotor";
            case "thesis_col_topic": return isEn ? "Thesis Topic" : "Temat pracy";
            case "thesis_col_slots": return isEn ? "Available Slots" : "Miejsca";
            case "thesis_select_warning": return isEn ? "Please select a thesis topic from the table first!" : "Proszę najpierw wybrać temat pracy z tabeli!";
            case "thesis_enroll_success": return isEn ? "Successfully enrolled in the thesis group managed by:" : "Pomyślnie zapisano się na temat seminaryjny u:";
            case "thesis_topic_ai": return isEn ? "Application of Machine Learning in Academic Systems" : "Zastosowanie uczenia maszynowego w systemach akademickich";
            case "thesis_topic_iot": return isEn ? "Security Challenges in IoT Ecosystems" : "Bezpieczeństwo i wyzwania w ekosystemach IoT";

            // --- PANEL WYKŁADOWCY (LECTURER COURSES & GRADES) ---
            case "lecturer_courses_title": return isEn ? "My Conducted Courses" : "Moje Prowadzone Kursy";
            case "course_type_mix": return isEn ? "Lecture & Lab" : "Wykład i Laboratorium";
            case "course_type_lab": return isEn ? "Laboratory Class" : "Laboratorium";
            case "course_type_seminar": return isEn ? "Academic Seminar" : "Seminarium";
            case "course_students_suffix": return isEn ? "Students" : "Studentów";

            case "lecturer_grades_title": return isEn ? "Student Grade Sheet" : "Protokół Oceniania Studentów";
            case "lecturer_grades_save_btn": return isEn ? "Save All Grades" : "Zatwierdź i zapisz oceny";
            case "grades_col_student_id": return isEn ? "Student Card No." : "Nr albumu";
            case "grades_col_student_name": return isEn ? "Full Name" : "Imię i nazwisko";
            case "grades_col_value": return isEn ? "Final Grade" : "Ocena końcowa";
            case "grades_col_status": return isEn ? "Outcome / Description" : "Status zaliczenia";
            case "status_grade_passed": return isEn ? "Passed" : "Zaliczenie";
            case "status_grade_failed": return isEn ? "Failed" : "Brak zaliczenia";
            case "grades_save_success_msg": return isEn ? "All student grades and sheet statuses have been saved successfully!" : "Wszystkie oceny oraz statusy studentów zostały pomyślnie zapisane w bazie danych!";

            // --- PANEL WYKŁADOWCY - WIADOMOŚCI I LISTA STUDENTÓW ---
            case "lecturer_msg_title": return isEn ? "Lecturer Message Center" : "Centrum Wiadomości Wykładowcy";
            case "lecturer_msg_refresh_btn": return isEn ? "Refresh Inbox" : "Odśwież skrzynkę";
            case "lecturer_msg_inbox_lbl": return isEn ? "Received Messages:" : "Wiadomości otrzymane:";
            case "lecturer_msg_content_lbl": return isEn ? "Message Content Details:" : "Szczegóły treści wiadomości:";
            case "lecturer_msg_reply_btn": return isEn ? "Send Reply Message" : "Wyślij odpowiedź";
            case "lecturer_msg_new_lbl": return isEn ? "Compose New Message to Student:" : "Napisz nową wiadomość do studenta:";
            case "lecturer_msg_send_btn": return isEn ? "Send Message" : "Wyślij wiadomość";
            case "lecturer_msg_subject_prompt": return isEn ? "Message Subject..." : "Temat wiadomości...";
            case "lecturer_msg_text_prompt": return isEn ? "Type your message here..." : "Treść wiadomości do studenta...";
            case "lecturer_msg_reply_prompt": return isEn ? "Type your quick reply here..." : "Napisz szybką odpowiedź...";
            case "lecturer_msg_student_prompt": return isEn ? "Select Target Student" : "Wybierz studenta docelowego";
            case "msg_sent_success_msg": return isEn ? "The message has been sent successfully!" : "Wiadomość została pomyślnie wysłana!";

            case "lecturer_student_list_title": return isEn ? "Course Group Student List" : "Lista Studentów Grupy";
            case "student_list_col_id": return isEn ? "Album ID" : "Indeks / ID";
            case "student_list_col_name": return isEn ? "Student Full Name" : "Imię i Nazwisko";
            case "student_list_col_email": return isEn ? "University Email" : "Adres Email";


            case "global_schedule_title": return isEn ? "Global University Schedule" : "Globalny Harmonogram Uczelni";
            case "select_group_prompt": return isEn ? "Select Group:" : "Wybierz grupę:";
            case "choose_group_holder": return isEn ? "-- Choose Student Group --" : "-- Wybierz Grupę Studencką --";
            case "schedule_time_col": return isEn ? "Time / Hours" : "Godzina / Czas";
            case "select_group_cell_msg": return isEn ? "Select group first" : "Wybierz grupę";
            case "add_class_btn_text": return isEn ? "Plan Class" : "Zaplanuj zajęcia";
            case "add_class_dialog_title": return isEn ? "Scheduling" : "Planowanie zajęć";
            case "add_class_dialog_header": return isEn ? "Assign course to this slot" : "Przypisz przedmiot do tego slotu";
            case "add_class_dialog_content": return isEn ? "Choose course:" : "Wybierz przedmiot:";
            case "edit_class_title": return isEn ? "Manage Class Slot" : "Zarządzanie slotem zajęć";
            case "edit_class_confirm_delete": return isEn ? "Do you want to cancel and delete this class?" : "Czy chcesz odwołać i usunąć te zajęcia z planu?";
            case "delete_btn_label": return isEn ? "Delete" : "Usuń";
            case "cancel_btn_label": return isEn ? "Cancel" : "Anuluj";
            case "employee_directory_title": return isEn ? "Academic Employee Directory" : "Katalog Pracowników Naukowych";

            // Klawisze dla SystemRequestsView
            case "sys_requests_title": return isEn ? "System Requests & Applications" : "Zgłoszenia Systemowe i Wnioski";
            case "col_request_type": return isEn ? "Type" : "Typ zgłoszenia";
            case "col_request_from": return isEn ? "From" : "Od kogo";
            case "col_request_status": return isEn ? "Status" : "Status";
            case "btn_process_request": return isEn ? "Process Selected" : "Rozpatrz zgłoszenie";
            case "request_process_success": return isEn ? "The request has been processed successfully!" : "Zgłoszenie zostało pomyślnie rozpatrzone!";

// Klawisze dla UserManagementView
            case "user_management_title": return isEn ? "User Management Panel" : "Panel Zarządzania Użytkownikami";
            case "btn_add_new_user": return isEn ? "+ Add New User" : "+ Dodaj Użytkownika";
            case "search_users_holder": return isEn ? "Search by ID, name or role..." : "Szukaj po ID, imieniu lub roli...";
            case "label_search": return isEn ? "Search:" : "Szukaj:";
            case "col_user_name": return isEn ? "Full Name" : "Imię i Nazwisko";
            case "col_user_role": return isEn ? "System Role" : "Rola w systemie";
            case "col_user_status": return isEn ? "Account Status" : "Status konta";
            case "dialog_add_user_title": return isEn ? "User Creation" : "Tworzenie użytkownika";
            case "dialog_add_user_header": return isEn ? "Enter new user system details" : "Wprowadź dane nowego konta systemowego";
            case "btn_save_label": return isEn ? "Save" : "Zapisz";


            // Ogólne elementy Dashboard
            case "dash_welcome": return isEn ? "Welcome back" : "Witaj ponownie";
            case "dash_info_sub": return isEn ? "Academic USOS Portal | University Management System" : "Akademicki Portal USOS | System Zarządzania Uniwersytetem";
            case "dash_stats_title": return isEn ? "Your Quick Performance Overview" : "Szybki podgląd wydajności";
            case "dash_actions_title": return isEn ? "Available Quick Actions" : "Dostępne szybkie działania";

            // Statystyki - Student
            case "dash_stat_gpa": return isEn ? "Grade Point Average" : "Średnia ocen";
            case "dash_stat_gpa_sub": return isEn ? "Calculated from current semester" : "Obliczona z bieżącego semestru";
            case "dash_stat_ects": return isEn ? "Earned ECTS Points" : "Zdobyte punkty ECTS";
            case "dash_stat_ects_sub": return isEn ? "Required for graduation: 210" : "Wymagane do inżyniera: 210";
            case "dash_stat_payments": return isEn ? "Financial Balance" : "Bilans finansowy";
            case "dash_stat_payments_sub": return isEn ? "No outstanding debts" : "Brak zaległych należności";

            // Statystyki - Wykładowca
            case "dash_stat_courses": return isEn ? "Conducted Courses" : "Prowadzone kursy";
            case "dash_stat_courses_sub": return isEn ? "In current academic year" : "W bieżącym roku akademickim";
            case "dash_stat_students": return isEn ? "Total Students Assigned" : "Przypisanych studentów";
            case "dash_stat_students_sub": return isEn ? "Across all active groups" : "We wszystkich aktywnych grupach";
            case "dash_stat_messages": return isEn ? "Unread Messages" : "Nieprzeczytane wiadomości";
            case "dash_stat_messages_sub": return isEn ? "Requires your response" : "Wymaga Twojej odpowiedzi";

            // Statystyki - Administrator
            case "dash_stat_total_users": return isEn ? "Total Platform Users" : "Wszyscy użytkownicy";
            case "dash_stat_total_users_sub": return isEn ? "Active student & staff accounts" : "Aktywne konta studentów i kadry";
            case "dash_stat_active_req": return isEn ? "Pending Requests" : "Oczekujące wnioski";
            case "dash_stat_active_req_sub": return isEn ? "System applications to process" : "Zgłoszenia do rozpatrzenia";
            case "dash_stat_system_status": return isEn ? "Core Services Status" : "Status usług głównych";
            case "dash_stat_system_status_sub": return isEn ? "All modules functional" : "Wszystkie moduły działają poprawnie";

            // Przyciski nawigacyjne (Wykładowca i Admin)
            case "nav_my_courses": return isEn ? "My Courses" : "Moje kursy";
            case "nav_add_grades": return isEn ? "Enter Grades" : "Wystaw oceny";
            case "nav_manage_users": return isEn ? "Manage Users" : "Użytkownicy";
            case "nav_manage_schedule": return isEn ? "Edit Schedule" : "Edycja planu";
            case "nav_system_requests": return isEn ? "System Requests" : "Wnioski systemowe";
            case "nav_employee_dir": return isEn ? "Staff Directory" : "Pracownicy";

            case "submit_btn_txt": return isEn ? "Submit Application" : "Złóż wniosek";
            case "history_title_label": return isEn ? "Application History" : "Historia złożonych wniosków";
            case "alert_validation_title": return isEn ? "Validation Error" : "Błąd walidacji";
            case "alert_validation_header": return isEn ? "Missing Fields" : "Niekompletne dane";
            case "alert_validation_content": return isEn ? "Please select a request type and provide a justification." : "Proszę wybrać rodzaj wniosku oraz wprowadzić uzasadnienie.";
            case "alert_success_title": return isEn ? "Success" : "Sukces";
            case "alert_success_header": return isEn ? "Application Submitted" : "Wniosek został zarejestrowany";
            case "alert_success_content": return isEn ? "Your application has been successfully added to the system." : "Twój wniosek został pomyślnie dodany do systemu i oczekuje na rozpatrzenie.";
            default: return key;
        }
    }

    // --- LISTY DANYCH DEMONSTRACYJNYCH ---
    public static ObservableList<Student> students = FXCollections.observableArrayList();
    public static ObservableList<Lecturer> lecturers = FXCollections.observableArrayList();
    public static ObservableList<Payment> payments = FXCollections.observableArrayList();
    public static ObservableList<Message> messages = FXCollections.observableArrayList();
    public static ObservableList<Request> requests = FXCollections.observableArrayList();

    // Specjalna klasa modelowa dla Zgłoszeń Serwisowych (Service Tickets)
    public static ObservableList<ServiceTicket> tickets = FXCollections.observableArrayList();

    static {
        // Inicjalizacja prowadzących
        Lecturer nowak = new Lecturer(2L, "Tomasz", "Nowak", "lecturer@uni.pl", "password123", "EMP201", "Dr.");
        Lecturer kowalska = new Lecturer(5L, "Maria", "Kowalska", "m.kow@uni.pl", "pass", "EMP202", "Prof.");
        lecturers.addAll(nowak, kowalska);

        // Inicjalizacja studentów
        Student lewandowski = new Student(1001L, "Mateusz", "Lewandowski", "m.lew@uni.pl", "123", "320101", "Informatyka", Semester.THIRD);
        Student lytvyn = new Student(1003L, "Dmytro", "Lytvyn", "dmytro@uni.pl", "pass123", "320103", "Informatyka", Semester.THIRD);
        students.addAll(lewandowski, lytvyn);

        // Inicjalizacja opłat dla studenta Dmytro
        payments.add(new Payment(1L, "Opłata za legitymację studencką", new BigDecimal("22.00"), LocalDate.of(2025, 10, 15), true));
        payments.add(new Payment(2L, "Czesne - Semestr 3", new BigDecimal("2000.00"), LocalDate.of(2025, 11, 1), true));
        payments.add(new Payment(3L, "Opłata za powtarzanie przedmiotu: Algorytmy", new BigDecimal("250.00"), LocalDate.of(2026, 6, 15), false));

        // Inicjalizacja przykładowych wiadomości
        messages.add(new Message(1L, lewandowski, nowak, "Pytanie o projekt", "Dzień dobry, czy projekt musi być w JavaFX?", LocalDateTime.now().minusDays(2), MessageStatus.READ));
        messages.add(new Message(2L, lytvyn, nowak, "Nieobecność na zajęciach", "Proszę o usprawiedliwienie nieobecności.", LocalDateTime.now().minusHours(5), MessageStatus.SENT));

        // Inicjalizacja wniosków studenckich
        requests.add(new Request(1L, lytvyn, RequestType.OTHER, "Prośba o przedłużenie sesji z powodu choroby.", RequestStatus.SUBMITTED, LocalDateTime.now().minusDays(1)));

        // Inicjalizacja zgłoszeń serwisowych
        tickets.add(new ServiceTicket(1L, "Brak dostępu do WiFi", "Dydaktyka", "W sali 312 nie działa eduroam.", "Wysoki", "Nowy"));
    }

    // --- POMOCNICZA KLASA DLA ZGŁOSZEŃ SERWISOWYCH ---
    public static class ServiceTicket {
        private final Long id;
        private final String title;
        private final String category;
        private final String description;
        private final String priority;
        private String status;

        public ServiceTicket(Long id, String title, String category, String description, String priority, String status) {
            this.id = id;
            this.title = title;
            this.category = category;
            this.description = description;
            this.priority = priority;
            this.status = status;
        }

        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getCategory() { return category; }
        public String getDescription() { return description; }
        public String getPriority() { return priority; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}