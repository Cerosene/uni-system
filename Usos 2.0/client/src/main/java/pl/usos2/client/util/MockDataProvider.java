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
 * Globalny dostawca danych demonstracyjnych (Mock Data) dla caÄŹĹĽËťej aplikacji.
 * Zapewnia spÄŹĹĽËťjnoÄŹĹĽËťÄŹĹĽËť danych pomiÄŹĹĽËťdzy rÄŹĹĽËťnymi panelami oraz zarzÄŹĹĽËťdza lokalizacjÄŹĹĽËť (jÄŹĹĽËťzykiem).
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
     * TÄŹĹĽËťumaczy klucz tekstowy na wybrany jÄŹĹĽËťzyk systemu.
     */
    /**
     * TÄŹĹĽËťumaczy klucz tekstowy na wybrany jÄŹĹĽËťzyk systemu (wersja rozszerzona dla Dashboardu).
     */
    public static String i18n(String key) {
        boolean isEn = currentLocale.get().getLanguage().equals("en");

                if (!isEn) {
            switch (key) {
                case "schedule": return "Plan zajęć";
                case "messages": return "Wiadomości";
                case "payments": return "Opłaty";
                case "tickets": return "Zgłoszenia";
                case "users": return "Użytkownicy";
                case "requests": return "Wnioski";
                case "nav_schedule": return "Plan zajęć";
                case "nav_payments": return "Płatności";
                case "nav_messages": return "Wiadomości";
                case "nav_tickets": return "Zgłoszenia";
                case "nav_manage_users": return "Użytkownicy";
                case "nav_manage_schedule": return "Edycja planu";
                case "nav_employee_dir": return "Pracownicy";
                case "nav_admin_requests": return "Wnioski";
                case "nav_admin_tickets": return "Zgłoszenia";
                case "nav_admin_payments": return "Płatności";
                case "sys_requests_title": return "Wnioski studenckie";
                case "col_request_type": return "Typ";
                case "col_request_from": return "Od";
                case "col_request_status": return "Status";
                case "col_request_date": return "Data złożenia";
                case "btn_process_request": return "Rozpatrz wniosek";
                case "request_process_success": return "Wniosek został pomyślnie rozpatrzony!";
                case "user_management_title": return "Panel zarządzania użytkownikami";
                case "btn_add_new_user": return "+ Dodaj użytkownika";
                case "col_user_name": return "Imię i nazwisko";
                case "col_user_role": return "Rola w systemie";
                case "col_user_status": return "Status konta";
                case "dialog_add_user_title": return "Tworzenie użytkownika";
                case "dialog_add_user_header": return "Wprowadź dane nowego konta systemowego";
                case "dash_welcome": return "Witaj ponownie";
                case "dash_info_sub": return "Akademicki Portal USOS | System Zarządzania Uniwersytetem";
                case "dash_stats_title": return "Szybki podgląd wydajności";
                case "dash_actions_title": return "Dostępne szybkie działania";
                case "dash_stat_gpa": return "Średnia ocen";
                case "dash_stat_gpa_sub": return "Obliczona z aktualnych ocen";
                case "dash_stat_ects": return "Zdobyte punkty ECTS";
                case "dash_stat_ects_sub": return "Wymagane do ukończenia: 210";
                case "dash_stat_payments": return "Bilans finansowy";
                case "dash_stat_payments_sub": return "Suma zaległych należności";
                case "dash_stat_courses": return "Prowadzone kursy";
                case "dash_stat_courses_sub": return "Przypisane do wykładowcy";
                case "dash_stat_students": return "Przypisani studenci";
                case "dash_stat_students_sub": return "Aktywne zapisy";
                case "dash_stat_messages": return "Nieprzeczytane wiadomości";
                case "dash_stat_messages_sub": return "Wymagają odpowiedzi";
                case "dash_stat_total_users": return "Wszyscy użytkownicy";
                case "dash_stat_total_users_sub": return "Aktywne konta studentów i kadry";
                case "dash_stat_active_req": return "Oczekujące wnioski";
                case "dash_stat_active_req_sub": return "Wnioski do rozpatrzenia";
                case "dash_stat_system_status": return "Otwarte zgłoszenia";
                case "dash_stat_system_status_sub": return "OPEN + IN_PROGRESS";
                case "alert_validation_title": return "Błąd walidacji";
                case "alert_validation_header": return "Niekompletne dane";
                case "alert_validation_content": return "Proszę wybrać rodzaj wniosku oraz wprowadzić uzasadnienie.";
                case "alert_success_header": return "Wniosek został zarejestrowany";
                case "alert_error_title": return "Błąd";
                case "label_first_name": return "Imię";
                case "label_last_name": return "Nazwisko";
                case "label_password": return "Hasło";
                case "label_search": return "Szukaj:";
                case "btn_save_label": return "Zapisz";
                case "search_users_holder": return "Szukaj po ID, nazwisku lub roli...";
                case "submit_btn_txt": return "Złóż wniosek";
                case "history_title_label": return "Historia złożonych wniosków";
                case "requests_title_screen": return "Składanie wniosków";
                case "new_request_label": return "Nowy wniosek:";
                case "select_request_type": return "Wybierz rodzaj wniosku...";
                case "request_content_prompt": return "Uzasadnienie wniosku...";
                case "fill_all_fields_error": return "Proszę wypełnić wszystkie pola!";
                case "messages_title_main": return "Skrzynka wiadomości";
                case "tab_new_message": return "Napisz wiadomość";
                case "tab_inbox": return "Skrzynka odbiorcza";
                case "select_lecturer_label": return "Odbiorca (wykładowca):";
                case "select_lecturer_prompt": return "Wybierz wykładowcę...";
                case "message_content_label": return "Treść wiadomości:";
                case "message_area_prompt": return "Wpisz treść wiadomości tutaj...";
                case "send_message_btn": return "Wyślij wiadomość";
                case "refresh_btn": return "Odśwież";
                case "received_messages_label": return "Odebrane wiadomości:";
                case "preview_content_label": return "Podgląd wiadomości:";
                case "message_sent_success": return "Wiadomość została wysłana pomyślnie.";
                case "alert_info_title": return "Informacja";
                case "payments_title_main": return "Moje płatności";
                case "account_balance_status": return "Aktualny stan konta finansowego:";
                case "balance_sub_info": return "Wszystkie wymagane rozliczenia są uregulowane";
                case "payments_history_label": return "Historia i nadchodzące opłaty:";
                case "col_payment_title": return "Tytuł zobowiązania";
                case "col_payment_amount": return "Kwota";
                case "col_payment_date": return "Termin płatności";
                case "col_payment_status": return "Status";
                case "payment_status_paid": return "OPŁACONE";
                case "payment_status_unpaid": return "NIEOPŁACONE";
                case "schedule_title_main": return "Tygodniowy plan zajęć";
                case "schedule_col_time": return "Godzina";
                case "day_monday": return "Poniedziałek";
                case "day_tuesday": return "Wtorek";
                case "day_wednesday": return "Środa";
                case "day_thursday": return "Czwartek";
                case "day_friday": return "Piątek";
                case "lecturer_grades_title": return "Protokół ocen studentów";
                case "lecturer_grades_save_btn": return "Zatwierdź i zapisz oceny";
                case "grades_col_student_id": return "Nr albumu";
                case "grades_col_student_name": return "Imię i nazwisko";
                case "grades_col_course": return "Przedmiot";
                case "grades_col_value": return "Ocena";
                case "grades_col_status": return "Status";
                case "status_grade_passed": return "Zaliczenie";
                case "status_grade_failed": return "Brak zaliczenia";
                case "grades_save_success_msg": return "Wszystkie oceny zostały zapisane.";
                case "role_lecturer": return "Wykładowca";
                case "status_active": return "Aktywne";
                case "status_inactive": return "Nieaktywne";
                default: break;
            }
        }
        switch (key) {
            // --- MENU BOCZNE (SIDEBAR) ---
            case "dashboard": return isEn ? "Dashboard" : "Pulpit";
            case "schedule": return isEn ? "Schedule" : "Plan zajÄ™Ä‡";
            case "grades": return isEn ? "Grades" : "Oceny";
            case "messages": return isEn ? "Messages" : "WiadomoĹ›ci";
            case "requests": return isEn ? "Requests" : "Wnioski";
            case "payments": return isEn ? "Payments" : "OpĹ‚aty";
            case "tickets": return isEn ? "Service Tickets" : "ZgĹ‚oszenia";
            case "logout": return isEn ? "Log Out" : "Wyloguj";
            case "users": return isEn ? "Users" : "UĹĽytkownicy";
            case "employees": return isEn ? "Employees" : "Pracownicy";
            case "course": return isEn ? "My Courses" : "Moje kursy";
            case "rentals": return isEn ? "My Rentals" : "Moje wypożyczenia";
            case "admin_rentals": return isEn ? "Rental Management" : "Zarządzanie wypożyczeniami";

            // --- PANEL GÄŹĹĽËťÄŹĹĽËťWNY (DASHBOARD GENERAL) ---
            // --- PRZYCISKI SZYBKICH AKCJI (QUICK ACTIONS NAV) ---
            case "nav_grades": return isEn ? "Grades" : "Oceny";
            case "nav_schedule": return isEn ? "Schedule" : "Plan zajÄ™Ä‡";
            case "nav_applications": return isEn ? "Applications" : "Wnioski";
            case "nav_payments": return isEn ? "Payments" : "PĹ‚atnoĹ›ci";
            case "nav_messages": return isEn ? "Messages" : "WiadomoĹ›ci";
            case "nav_thesis": return isEn ? "Thesis Topic" : "Praca dyplomowa";
            case "nav_tickets": return isEn ? "Service Tickets" : "ZgĹ‚oszenia";
            case "nav_rental": return isEn ? "Wypożyczenia" : "Rentals";


            // --- KARTY STATYSTYK STUDENTA (STUDENT STAT CARDS) ---
            case "gpa_title": return isEn ? "Current GPA" : "Aktualna ÄŹĹĽËťrednia";
            case "gpa_sub": return isEn ? "+0.3 od zeszÄŹĹĽËťego semestru" : "+0.3 od zeszÄŹĹĽËťego semestru";
            case "ects_title": return isEn ? "ECTS Credits" : "Punkty ECTS";
            case "ects_sub": return isEn ? "Required: 180" : "Wymagane: 180";
            case "finances_title": return isEn ? "Financial Balance" : "Stan Konta";
            case "finances_sub": return isEn ? "No pending payments" : "Brak zalegÄŹĹĽËťych opÄŹĹĽËťat";

            // --- KARTY AKCJI STUDENTA (STUDENT ACTION CARDS) ---
            case "schedule_title": return isEn ? "Weekly Schedule" : "Plan ZajÄŹĹĽËťÄŹĹĽËť";
            case "schedule_desc": return isEn ? "Check your classes and hours" : "SprawdÄŹĹĽËť swoje zajÄŹĹĽËťcia i godziny sal";
            case "grades_title": return isEn ? "My Grades" : "Moje Oceny";
            case "grades_desc": return isEn ? "View your semester achievements" : "Przejrzyj swoje osiÄŹĹĽËťgniÄŹĹĽËťcia semestralne";
            case "messages_title": return isEn ? "Messages Box" : "Skrzynka WiadomoÄŹĹĽËťci";
            case "messages_desc": return isEn ? "Contact your university professors" : "Skontaktuj siÄŹĹĽËť ze swoimi prowadzÄŹĹĽËťcymi";
            case "requests_title": return isEn ? "Submit Request" : "ZÄŹĹĽËťÄŹĹĽËť Wniosek";
            case "requests_desc": return isEn ? "Apply for scholarships or leaves" : "Aplikuj o stypendia lub urlopy dziekaÄŹĹĽËťskie";

            // --- KARTY WYKÄŹĹĽËťADOWCY (LECTURER CARDS) ---
            case "lect_courses_count": return isEn ? "Active Courses" : "Aktywne Kursy";
            case "lect_courses_sub": return isEn ? "Current semester" : "BieÄŹĹĽËťÄŹĹĽËťcy semestr";
            case "lect_students_count": return isEn ? "Total Students" : "Suma StudentÄŹĹĽËťw";
            case "lect_students_sub": return isEn ? "In all your courses" : "Na wszystkich Twoich kursach";

            // --- KARTY ADMINISTRATORA (ADMIN CARDS) ---
            case "admin_total_users": return isEn ? "Total Users" : "Wszyscy UÄŹĹĽËťytkownicy";
            case "admin_users_sub": return isEn ? "Active accounts" : "Aktywne konta";
            case "admin_pending_req": return isEn ? "Pending Requests" : "OczekujÄŹĹĽËťce Wnioski";
            case "admin_req_sub": return isEn ? "Requires manual review" : "Wymaga rÄŹĹĽËťcznej weryfikacji";

            // --- EKRAN WNIOSKÄŹĹĽËťW (APPLICATIONS VIEW) ---
            case "requests_title_screen": return isEn ? "Student Applications" : "SkÄŹĹĽËťadanie WnioskÄŹĹĽËťw";
            case "new_request_label": return isEn ? "Submit New Request:" : "Nowy wniosek:";
            case "select_request_type": return isEn ? "Select request type..." : "Wybierz rodzaj wniosku...";
            case "request_content_prompt": return isEn ? "Justification of the request..." : "Uzasadnienie wniosku...";
            case "submit_request_btn": return isEn ? "Submit Request" : "ZÄŹĹĽËťÄŹĹĽËť wniosek";
            case "my_requests_history": return isEn ? "My Submitted Requests:" : "Moje wnioski:";
            case "fill_all_fields_error": return isEn ? "Please fill in all fields!" : "ProszÄŹĹĽËť wypeÄŹĹĽËťniÄŹĹĽËť wszystkie pola!";
            case "request_success_msg": return isEn ? "The request has been submitted successfully." : "Wniosek zostaÄŹĹĽËť pomyÄŹĹĽËťlnie zÄŹĹĽËťoÄŹĹĽËťony.";

            // --- EKRAN OCEN (GRADES VIEW) ---
            case "semester_3_label": return isEn ? "Semester 3 (Current)" : "Semestr 3 (BieÄŹĹĽËťÄŹĹĽËťcy)";
            case "status_passed": return isEn ? "PASSED" : "ZALICZONY";
            case "status_failed": return isEn ? "FAILED" : "DO POPRAWY";
            case "subject_algorithms": return isEn ? "Advanced Algorithms" : "Algorytmy Zaawansowane";
            case "subject_databases": return isEn ? "Database Systems" : "Systemy Baz Danych";
            case "subject_networks": return isEn ? "Computer Networks" : "Sieci Komputerowe";

            // --- EKRAN WIADOMOÄŹĹĽËťCI (MESSAGES VIEW) ---
            case "messages_title_main": return isEn ? "Messages Box" : "Skrzynka WiadomoÄŹĹĽËťci";
            case "tab_new_message": return isEn ? "New Message" : "Napisz wiadomoÄŹĹĽËťÄŹĹĽËť";
            case "select_lecturer_label": return isEn ? "Recipient (Lecturer):" : "Odbiorca (WykÄŹĹĽËťadowca):";
            case "select_lecturer_prompt": return isEn ? "Choose lecturer..." : "Wybierz wykÄŹĹĽËťadowcÄŹĹĽËť...";
            case "message_content_label": return isEn ? "Message content:" : "TreÄŹĹĽËťÄŹĹĽËť wiadomoÄŹĹĽËťci:";
            case "message_area_prompt": return isEn ? "Type your message here..." : "Wpisz treÄŹĹĽËťÄŹĹĽËť wiadomoÄŹĹĽËťci tutaj...";
            case "send_message_btn": return isEn ? "Send Message" : "WyÄŹĹĽËťlij wiadomoÄŹĹĽËťÄŹĹĽËť";
            case "tab_inbox": return isEn ? "Inbox" : "Skrzynka odbiorcza";
            case "refresh_btn": return isEn ? "Refresh" : "OdÄŹĹĽËťwieÄŹĹĽËť";
            case "received_messages_label": return isEn ? "Received messages:" : "Otrzymane wiadomoÄŹĹĽËťci:";
            case "preview_content_label": return isEn ? "Message preview:" : "TreÄŹĹĽËťÄŹĹĽËť wybranej wiadomoÄŹĹĽËťci:";
            case "alert_warn_title": return isEn ? "Warning" : "OstrzeÄŹĹĽËťenie";
            case "alert_info_title": return isEn ? "Information" : "Informacja";
            case "message_sent_success": return isEn ? "Message has been sent successfully!" : "WiadomoÄŹĹĽËťÄŹĹĽËť zostaÄŹĹĽËťa pomyÄŹĹĽËťlnie wysÄŹĹĽËťana!";

            // --- EKRAN PÄŹĹĽËťATNOÄŹĹĽËťCI (PAYMENTS VIEW) ---
            case "payments_title_main": return isEn ? "My Payments" : "Moje PÄŹĹĽËťatnoÄŹĹĽËťci";
            case "account_balance_status": return isEn ? "Current Financial Account Balance:" : "Aktualny stan konta finansowego:";
            case "balance_sub_info": return isEn ? "All mandatory settlements are up to date" : "Wszystkie wymagane rozliczenia sÄŹĹĽËť uregulowane";
            case "payments_history_label": return isEn ? "History and Upcoming Fees:" : "Historia i nadchodzÄŹĹĽËťce opÄŹĹĽËťaty:";
            case "col_payment_title": return isEn ? "Title" : "TytuÄŹĹĽËť zobowiÄŹĹĽËťzania";
            case "col_payment_amount": return isEn ? "Amount" : "Kwota";
            case "col_payment_date": return isEn ? "Due Date" : "Termin pÄŹĹĽËťatnoÄŹĹĽËťci";
            case "col_payment_status": return isEn ? "Status" : "Status";
            case "payment_status_paid": return isEn ? "PAID" : "OPÄŹĹĽËťACONE";
            case "payment_status_unpaid": return isEn ? "UNPAID" : "NIEOPÄŹĹĽËťACONE";

            // --- EKRAN HARMONOGRAMU (SCHEDULE VIEW) ---
            case "schedule_title_main": return isEn ? "Weekly Class Schedule" : "Tygodniowy Plan ZajÄŹĹĽËťÄŹĹĽËť";
            case "schedule_col_time": return isEn ? "Time" : "Godzina";
            case "day_monday": return isEn ? "Monday" : "PoniedziaÄŹĹĽËťek";
            case "day_tuesday": return isEn ? "Tuesday" : "Wtorek";
            case "day_wednesday": return isEn ? "Wednesday" : "ÄŹĹĽËťroda";
            case "day_thursday": return isEn ? "Thursday" : "Czwartek";
            case "day_friday": return isEn ? "Friday" : "PiÄŹĹĽËťtek";

            // --- EKRAN PRAC DYPLOMOWYCH (THESIS VIEW) ---
            case "thesis_title_main": return isEn ? "Thesis Advisor & Topic Selection" : "WybÄŹĹĽËťr Promotora i Tematu Pracy";
            case "thesis_apply_btn": return isEn ? "Enroll in Topic" : "Zapisz siÄŹĹĽËť na temat";
            case "thesis_col_promotor": return isEn ? "Supervisor / Advisor" : "Promotor";
            case "thesis_col_topic": return isEn ? "Thesis Topic" : "Temat pracy";
            case "thesis_col_slots": return isEn ? "Available Slots" : "Miejsca";
            case "thesis_select_warning": return isEn ? "Please select a thesis topic from the table first!" : "ProszÄŹĹĽËť najpierw wybraÄŹĹĽËť temat pracy z tabeli!";
            case "thesis_enroll_success": return isEn ? "Successfully enrolled in the thesis group managed by:" : "PomyÄŹĹĽËťlnie zapisano siÄŹĹĽËť na temat seminaryjny u:";
            case "thesis_topic_ai": return isEn ? "Application of Machine Learning in Academic Systems" : "Zastosowanie uczenia maszynowego w systemach akademickich";
            case "thesis_topic_iot": return isEn ? "Security Challenges in IoT Ecosystems" : "BezpieczeÄŹĹĽËťstwo i wyzwania w ekosystemach IoT";

            // --- PANEL WYKÄŹĹĽËťADOWCY (LECTURER COURSES & GRADES) ---
            case "lecturer_courses_title": return isEn ? "My Conducted Courses" : "Moje Prowadzone Kursy";
            case "course_type_mix": return isEn ? "Lecture & Lab" : "WykÄŹĹĽËťad i Laboratorium";
            case "course_type_lab": return isEn ? "Laboratory Class" : "Laboratorium";
            case "course_type_seminar": return isEn ? "Academic Seminar" : "Seminarium";
            case "course_students_suffix": return isEn ? "Students" : "StudentÄŹĹĽËťw";

            case "lecturer_grades_title": return isEn ? "Student Grade Sheet" : "ProtokÄŹĹĽËť Oceniania StudentÄŹĹĽËťw";
            case "lecturer_grades_save_btn": return isEn ? "Save All Grades" : "ZatwierdÄŹĹĽËť i zapisz oceny";
            case "grades_col_student_id": return isEn ? "Student Card No." : "Nr albumu";
            case "grades_col_student_name": return isEn ? "Full Name" : "ImiÄŹĹĽËť i nazwisko";
            case "grades_col_value": return isEn ? "Final Grade" : "Ocena koÄŹĹĽËťcowa";
            case "grades_col_status": return isEn ? "Outcome / Description" : "Status zaliczenia";
            case "status_grade_passed": return isEn ? "Passed" : "Zaliczenie";
            case "status_grade_failed": return isEn ? "Failed" : "Brak zaliczenia";
            case "grades_save_success_msg": return isEn ? "All student grades and sheet statuses have been saved successfully!" : "Wszystkie oceny oraz statusy studentÄŹĹĽËťw zostaÄŹĹĽËťy pomyÄŹĹĽËťlnie zapisane w bazie danych!";

            // --- PANEL WYKÄŹĹĽËťADOWCY - WIADOMOÄŹĹĽËťCI I LISTA STUDENTÄŹĹĽËťW ---
            case "lecturer_msg_title": return isEn ? "Lecturer Message Center" : "Centrum WiadomoÄŹĹĽËťci WykÄŹĹĽËťadowcy";
            case "lecturer_msg_refresh_btn": return isEn ? "Refresh Inbox" : "OdÄŹĹĽËťwieÄŹĹĽËť skrzynkÄŹĹĽËť";
            case "lecturer_msg_inbox_lbl": return isEn ? "Received Messages:" : "WiadomoÄŹĹĽËťci otrzymane:";
            case "lecturer_msg_content_lbl": return isEn ? "Message Content Details:" : "SzczegÄŹĹĽËťy treÄŹĹĽËťci wiadomoÄŹĹĽËťci:";
            case "lecturer_msg_reply_btn": return isEn ? "Send Reply Message" : "WyÄŹĹĽËťlij odpowiedÄŹĹĽËť";
            case "lecturer_msg_new_lbl": return isEn ? "Compose New Message to Student:" : "Napisz nowÄŹĹĽËť wiadomoÄŹĹĽËťÄŹĹĽËť do studenta:";
            case "lecturer_msg_send_btn": return isEn ? "Send Message" : "WyÄŹĹĽËťlij wiadomoÄŹĹĽËťÄŹĹĽËť";
            case "lecturer_msg_subject_prompt": return isEn ? "Message Subject..." : "Temat wiadomoÄŹĹĽËťci...";
            case "lecturer_msg_text_prompt": return isEn ? "Type your message here..." : "TreÄŹĹĽËťÄŹĹĽËť wiadomoÄŹĹĽËťci do studenta...";
            case "lecturer_msg_reply_prompt": return isEn ? "Type your quick reply here..." : "Napisz szybkÄŹĹĽËť odpowiedÄŹĹĽËť...";
            case "lecturer_msg_student_prompt": return isEn ? "Select Target Student" : "Wybierz studenta docelowego";
            case "msg_sent_success_msg": return isEn ? "The message has been sent successfully!" : "WiadomoÄŹĹĽËťÄŹĹĽËť zostaÄŹĹĽËťa pomyÄŹĹĽËťlnie wysÄŹĹĽËťana!";

            case "lecturer_student_list_title": return isEn ? "Course Group Student List" : "Lista StudentÄŹĹĽËťw Grupy";
            case "student_list_col_id": return isEn ? "Album ID" : "Indeks / ID";
            case "student_list_col_name": return isEn ? "Student Full Name" : "ImiÄŹĹĽËť i Nazwisko";
            case "student_list_col_email": return isEn ? "University Email" : "Adres Email";


            case "global_schedule_title": return isEn ? "Global University Schedule" : "Globalny Harmonogram Uczelni";
            case "select_group_prompt": return isEn ? "Select Group:" : "Wybierz grupÄŹĹĽËť:";
            case "choose_group_holder": return isEn ? "-- Choose Student Group --" : "-- Wybierz GrupÄŹĹĽËť StudenckÄŹĹĽËť --";
            case "schedule_time_col": return isEn ? "Time / Hours" : "Godzina / Czas";
            case "select_group_cell_msg": return isEn ? "Select group first" : "Wybierz grupÄŹĹĽËť";
            case "add_class_btn_text": return isEn ? "Plan Class" : "Zaplanuj zajÄŹĹĽËťcia";
            case "add_class_dialog_title": return isEn ? "Scheduling" : "Planowanie zajÄŹĹĽËťÄŹĹĽËť";
            case "add_class_dialog_header": return isEn ? "Assign course to this slot" : "Przypisz przedmiot do tego slotu";
            case "add_class_dialog_content": return isEn ? "Choose course:" : "Wybierz przedmiot:";
            case "edit_class_title": return isEn ? "Manage Class Slot" : "Zarządzanie slotem zajęć";
            case "edit_class_confirm_delete": return isEn ? "Do you want to cancel and delete this class?" : "Czy chcesz odwoÄŹĹĽËťaÄŹĹĽËť i usunÄŹĹĽËťÄŹĹĽËť te zajÄŹĹĽËťcia z planu?";
            case "delete_btn_label": return isEn ? "Delete" : "Usuń";
            case "cancel_btn_label": return isEn ? "Cancel" : "Anuluj";
            case "employee_directory_title": return isEn ? "Academic Employee Directory" : "Katalog Pracowników Naukowych";

            // Klawisze dla SystemRequestsView
            case "sys_requests_title": return isEn ? "System Requests & Applications" : "ZgÄŹĹĽËťoszenia Systemowe i Wnioski";
            case "col_request_type": return isEn ? "Type" : "Typ zgÄŹĹĽËťoszenia";
            case "col_request_from": return isEn ? "From" : "Od kogo";
            case "col_request_status": return isEn ? "Status" : "Status";
            case "btn_process_request": return isEn ? "Process Selected" : "Rozpatrz zgÄŹĹĽËťoszenie";
            case "request_process_success": return isEn ? "The request has been processed successfully!" : "ZgÄŹĹĽËťoszenie zostaÄŹĹĽËťo pomyÄŹĹĽËťlnie rozpatrzone!";

            // Klawisze dla UserManagementView
            case "user_management_title": return isEn ? "User Management Panel" : "Panel ZarzÄŹĹĽËťdzania UÄŹĹĽËťytkownikami";
            case "btn_add_new_user": return isEn ? "+ Add New User" : "+ Dodaj UÄŹĹĽËťytkownika";
            case "search_users_holder": return isEn ? "Search by ID, name or role..." : "Szukaj po ID, imieniu lub roli...";
            case "label_search": return isEn ? "Search:" : "Szukaj:";
            case "col_user_name": return isEn ? "Full Name" : "ImiÄŹĹĽËť i Nazwisko";
            case "col_user_role": return isEn ? "System Role" : "Rola w systemie";
            case "col_user_status": return isEn ? "Account Status" : "Status konta";
            case "dialog_add_user_title": return isEn ? "User Creation" : "Tworzenie uÄŹĹĽËťytkownika";
            case "dialog_add_user_header": return isEn ? "Enter new user system details" : "WprowadÄŹĹĽËť dane nowego konta systemowego";
            case "btn_save_label": return isEn ? "Save" : "Zapisz";


            // OgÄŹĹĽËťlne elementy Dashboard
            case "dash_welcome": return isEn ? "Welcome back" : "Witaj ponownie";
            case "dash_info_sub": return isEn ? "Academic USOS Portal | University Management System" : "Akademicki Portal USOS | System ZarzÄŹĹĽËťdzania Uniwersytetem";
            case "dash_stats_title": return isEn ? "Your Quick Performance Overview" : "Szybki podglÄŹĹĽËťd wydajnoÄŹĹĽËťci";
            case "dash_actions_title": return isEn ? "Available Quick Actions" : "DostÄŹĹĽËťpne szybkie dziaÄŹĹĽËťania";

            // Statystyki - Student
            case "dash_stat_gpa": return isEn ? "Grade Point Average" : "ÄŹĹĽËťrednia ocen";
            case "dash_stat_gpa_sub": return isEn ? "Calculated from current semester" : "Obliczona z bieÄŹĹĽËťÄŹĹĽËťcego semestru";
            case "dash_stat_ects": return isEn ? "Earned ECTS Points" : "Zdobyte punkty ECTS";
            case "dash_stat_ects_sub": return isEn ? "Required for graduation: 210" : "Wymagane do inÄŹĹĽËťyniera: 210";
            case "dash_stat_payments": return isEn ? "Financial Balance" : "Bilans finansowy";
            case "dash_stat_payments_sub": return isEn ? "No outstanding debts" : "Brak zalegÄŹĹĽËťych naleÄŹĹĽËťnoÄŹĹĽËťci";

            // Statystyki - WykÄŹĹĽËťadowca
            case "dash_stat_courses": return isEn ? "Conducted Courses" : "Prowadzone kursy";
            case "dash_stat_courses_sub": return isEn ? "In current academic year" : "W bieÄŹĹĽËťÄŹĹĽËťcym roku akademickim";
            case "dash_stat_students": return isEn ? "Total Students Assigned" : "Przypisanych studentÄŹĹĽËťw";
            case "dash_stat_students_sub": return isEn ? "Across all active groups" : "We wszystkich aktywnych grupach";
            case "dash_stat_messages": return isEn ? "Unread Messages" : "Nieprzeczytane wiadomoÄŹĹĽËťci";
            case "dash_stat_messages_sub": return isEn ? "Requires your response" : "Wymaga Twojej odpowiedzi";

            // Statystyki - Administrator
            case "dash_stat_total_users": return isEn ? "Total Platform Users" : "Wszyscy uÄŹĹĽËťytkownicy";
            case "dash_stat_total_users_sub": return isEn ? "Active student & staff accounts" : "Aktywne konta studentÄŹĹĽËťw i kadry";
            case "dash_stat_active_req": return isEn ? "Pending Requests" : "OczekujÄŹĹĽËťce wnioski";
            case "dash_stat_active_req_sub": return isEn ? "System applications to process" : "ZgÄŹĹĽËťoszenia do rozpatrzenia";
            case "dash_stat_system_status": return isEn ? "Core Services Status" : "Status usÄŹĹĽËťug gÄŹĹĽËťÄŹĹĽËťwnych";
            case "dash_stat_system_status_sub": return isEn ? "All modules functional" : "Wszystkie moduÄŹĹĽËťy dziaÄŹĹĽËťajÄŹĹĽËť poprawnie";

            // Przyciski nawigacyjne (WykÄŹĹĽËťadowca i Admin)
            case "nav_my_courses": return isEn ? "My Courses" : "Moje kursy";
            case "nav_add_grades": return isEn ? "Enter Grades" : "Wystaw oceny";
            case "nav_manage_users": return isEn ? "Manage Users" : "UĹĽytkownicy";
            case "nav_manage_schedule": return isEn ? "Edit Schedule" : "Edycja planu";
            case "nav_system_requests": return isEn ? "System Requests" : "Wnioski systemowe";
            case "nav_admin_requests": return isEn ? "Applications" : "Wnioski";
            case "nav_admin_tickets": return isEn ? "Tickets" : "ZgĹ‚oszenia";
            case "nav_admin_payments": return isEn ? "Payments" : "PĹ‚atnoĹ›ci";
            case "nav_employee_dir": return isEn ? "Staff Directory" : "Pracownicy";

            case "submit_btn_txt": return isEn ? "Submit Application" : "ZÄŹĹĽËťÄŹĹĽËť wniosek";
            case "history_title_label": return isEn ? "Application History" : "Historia zÄŹĹĽËťoÄŹĹĽËťonych wnioskÄŹĹĽËťw";
            case "alert_validation_title": return isEn ? "Validation Error" : "BÄŹĹĽËťÄŹĹĽËťd walidacji";
            case "alert_validation_header": return isEn ? "Missing Fields" : "Niekompletne dane";
            case "alert_validation_content": return isEn ? "Please select a request type and provide a justification." : "ProszÄŹĹĽËť wybraÄŹĹĽËť rodzaj wniosku oraz wprowadziÄŹĹĽËť uzasadnienie.";
            case "alert_success_title": return isEn ? "Success" : "Sukces";
            case "alert_success_header": return isEn ? "Application Submitted" : "Wniosek zostaÄŹĹĽËť zarejestrowany";
            case "alert_success_content": return isEn ? "Your application has been successfully added to the system." : "TwÄŹĹĽËťj wniosek zostaÄŹĹĽËť pomyÄŹĹĽËťlnie dodany do systemu i oczekuje na rozpatrzenie.";
            
            // --- FORMULARZE I POLA TEKSTOWE (FORM FIELDS) ---
            case "label_first_name": return isEn ? "First Name" : "ImiÄŹĹĽËť";
            case "label_last_name": return isEn ? "Last Name" : "Nazwisko";
            case "label_email": return isEn ? "Email" : "E-mail";
            case "label_password": return isEn ? "Password" : "HasÄŹĹĽËťo";
            case "alert_error_title": return isEn ? "Error" : "BÄŹĹĽËťÄŹĹĽËťd";
            
            // --- ROLA UÄŹĹĽËťYTKOWNIKA (USER ROLES) ---
            case "role_student": return isEn ? "Student" : "Student";
            case "role_lecturer": return isEn ? "Lecturer" : "WykÄŹĹĽËťadowca";
            case "role_administrator": return isEn ? "Administrator" : "Administrator";
            
            // --- STATUS UÄŹĹĽËťYTKOWNIKA (USER STATUS) ---
            case "status_active": return isEn ? "Active" : "Aktywne";
            case "status_inactive": return isEn ? "Inactive" : "Nieaktywne";
            
            // --- REGUÄŹĹĽËť I DODATKOWE KLUCZE ---
            case "grades_empty_message": return isEn ? "No grades available" : "Brak ocen do wyÄŹĹĽËťwietlenia";
            case "lecturer_courses_empty": return isEn ? "No courses assigned" : "Brak przypisanych kursÄŹĹĽËťw";
            case "col_request_date": return isEn ? "Submission Date" : "Data zÄŹĹĽËťoÄŹĹĽËťenia";
            case "course_students_count": return isEn ? "Students" : "StudentÄŹĹĽËťw";
            case "no_courses_message": return isEn ? "No courses available" : "Brak dostÄŹĹĽËťpnych kursÄŹĹĽËťw";
            case "no_messages_message": return isEn ? "No messages available" : "Brak wiadomoÄŹĹĽËťci";
            case "no_groups_message": return isEn ? "No groups available" : "Brak dostÄŹĹĽËťpnych grup";

            // -- RENTALS
            case "my_rentals_title": return isEn ? "My rentals" : "Moje wypożyczenia";
            case "rental_col_item": return isEn ? "Item" : "Przedmiot";
            case "rental_col_date": return isEn ? "Rental date" : "Data wypożyczenia";
            case "rental_col_status": return isEn ? "Status" : "Status";

            default: return key;
        }
    }

    // --- LISTY DANYCH DEMONSTRACYJNYCH ---
    public static ObservableList<Student> students = FXCollections.observableArrayList();
    public static ObservableList<Lecturer> lecturers = FXCollections.observableArrayList();
    public static ObservableList<Payment> payments = FXCollections.observableArrayList();
    public static ObservableList<Message> messages = FXCollections.observableArrayList();
    public static ObservableList<Request> requests = FXCollections.observableArrayList();

    // Specjalna klasa modelowa dla ZgÄŹĹĽËťoszeÄŹĹĽËť Serwisowych (Service Tickets)
    public static ObservableList<ServiceTicket> tickets = FXCollections.observableArrayList();

    static {
        // Inicjalizacja prowadzÄŹĹĽËťcych
        Lecturer nowak = new Lecturer(2L, "Tomasz", "Nowak", "lecturer@uni.pl", "password123", "EMP201", "Dr.");
        Lecturer kowalska = new Lecturer(5L, "Maria", "Kowalska", "m.kow@uni.pl", "pass", "EMP202", "Prof.");
        lecturers.addAll(nowak, kowalska);

        // Inicjalizacja studentÄŹĹĽËťw
        Student lewandowski = new Student(1001L, "Mateusz", "Lewandowski", "m.lew@uni.pl", "123", "320101", "Informatyka", Semester.THIRD);
        Student lytvyn = new Student(1003L, "Dmytro", "Lytvyn", "dmytro@uni.pl", "pass123", "320103", "Informatyka", Semester.THIRD);
        students.addAll(lewandowski, lytvyn);

        // Inicjalizacja opÄŹĹĽËťat dla studenta Dmytro
        payments.add(new Payment(1L, "OpÄŹĹĽËťata za legitymacjÄŹĹĽËť studenckÄŹĹĽËť", new BigDecimal("22.00"), LocalDate.of(2025, 10, 15), true));
        payments.add(new Payment(2L, "Czesne - Semestr 3", new BigDecimal("2000.00"), LocalDate.of(2025, 11, 1), true));
        payments.add(new Payment(3L, "OpÄŹĹĽËťata za powtarzanie przedmiotu: Algorytmy", new BigDecimal("250.00"), LocalDate.of(2026, 6, 15), false));

        // Inicjalizacja przykÄŹĹĽËťadowych wiadomoÄŹĹĽËťci
        messages.add(new Message(1L, lewandowski, nowak, "Pytanie o projekt", "DzieÄŹĹĽËť dobry, czy projekt musi byÄŹĹĽËť w JavaFX?", LocalDateTime.now().minusDays(2), MessageStatus.READ));
        messages.add(new Message(2L, lytvyn, nowak, "NieobecnoÄŹĹĽËťÄŹĹĽËť na zajÄŹĹĽËťciach", "ProszÄŹĹĽËť o usprawiedliwienie nieobecnoÄŹĹĽËťci.", LocalDateTime.now().minusHours(5), MessageStatus.SENT));

        // Inicjalizacja wnioskÄŹĹĽËťw studenckich
        requests.add(new Request(1L, lytvyn, RequestType.OTHER, "ProÄŹĹĽËťba o przedÄŹĹĽËťuÄŹĹĽËťenie sesji z powodu choroby.", RequestStatus.SUBMITTED, LocalDateTime.now().minusDays(1)));

        // Inicjalizacja zgÄŹĹĽËťoszeÄŹĹĽËť serwisowych
        tickets.add(new ServiceTicket(1L, "Brak dostÄŹĹĽËťpu do WiFi", "Dydaktyka", "W sali 312 nie dziaÄŹĹĽËťa eduroam.", "Wysoki", "Nowy"));
    }

    // --- POMOCNICZA KLASA DLA ZGÄŹĹĽËťOSZEÄŹĹĽËť SERWISOWYCH ---
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


