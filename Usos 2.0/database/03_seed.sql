WHENEVER SQLERROR EXIT SQL.SQLCODE

INSERT INTO roles (role_id, role_code, role_name) VALUES (1, 'STUDENT', 'Student');
INSERT INTO roles (role_id, role_code, role_name) VALUES (2, 'LECTURER', 'Lecturer');
INSERT INTO roles (role_id, role_code, role_name) VALUES (3, 'ADMINISTRATOR', 'Administrator');

INSERT INTO users (user_id, first_name, last_name, email, password_hash, role_id, active_flag)
VALUES (1003, 'Dmytro', 'Lytvyn', 'dmytro@uni.pl', 'pass123', 1, 'Y');

INSERT INTO users (user_id, first_name, last_name, email, password_hash, role_id, active_flag)
VALUES (1001, 'Mateusz', 'Lewandowski', 'mateusz@uni.pl', 'password123', 1, 'Y');

INSERT INTO users (user_id, first_name, last_name, email, password_hash, role_id, active_flag)
VALUES (2, 'Tomasz', 'Nowak', 'lecturer@uni.pl', 'password123', 2, 'Y');

INSERT INTO users (user_id, first_name, last_name, email, password_hash, role_id, active_flag)
VALUES (5, 'Maria', 'Kowalska', 'm.kow@uni.pl', 'password123', 2, 'Y');

INSERT INTO users (user_id, first_name, last_name, email, password_hash, role_id, active_flag)
VALUES (3, 'Anna', 'Zielinska', 'admin@uni.pl', 'password123', 3, 'Y');

INSERT INTO students (student_id, student_number, field_of_study, semester)
VALUES (1003, '320103', 'Informatyka', 'THIRD');

INSERT INTO students (student_id, student_number, field_of_study, semester)
VALUES (1001, '320101', 'Informatyka', 'THIRD');

INSERT INTO employees (employee_id, employee_number, position_title, salary)
VALUES (2, 'EMP201', 'Lecturer', 7000.00);

INSERT INTO employees (employee_id, employee_number, position_title, salary)
VALUES (5, 'EMP202', 'Lecturer', 7000.00);

INSERT INTO employees (employee_id, employee_number, position_title, salary)
VALUES (3, 'ADM001', 'Administrator', 6000.00);

INSERT INTO lecturers (lecturer_id, academic_title)
VALUES (2, 'Dr.');

INSERT INTO lecturers (lecturer_id, academic_title)
VALUES (5, 'Prof.');

INSERT INTO admins (admin_id)
VALUES (3);

INSERT INTO subjects (subject_id, subject_name, subject_code, ects, lecturer_id)
VALUES (1, 'Zaawansowane Algorytmy', 'CS301', 6, 2);

INSERT INTO subjects (subject_id, subject_name, subject_code, ects, lecturer_id)
VALUES (2, 'Bazy Danych 2', 'CS302', 5, 5);

INSERT INTO course_groups (group_id, group_name, subject_id, lecturer_id)
VALUES (1, 'CS301-LAB-A', 1, 2);

INSERT INTO course_groups (group_id, group_name, subject_id, lecturer_id)
VALUES (2, 'CS302-LAB-A', 2, 5);

INSERT INTO enrollments (enrollment_id, student_id, group_id, active_flag)
VALUES (1, 1003, 1, 'Y');

INSERT INTO enrollments (enrollment_id, student_id, group_id, active_flag)
VALUES (2, 1003, 2, 'Y');

INSERT INTO enrollments (enrollment_id, student_id, group_id, active_flag)
VALUES (3, 1001, 1, 'Y');

INSERT INTO grades (grade_id, student_id, subject_id, lecturer_id, grade_value, description)
VALUES (1, 1003, 1, 2, 4.5, 'Kolokwium 1');

INSERT INTO grades (grade_id, student_id, subject_id, lecturer_id, grade_value, description)
VALUES (2, 1003, 2, 5, 5.0, 'Projekt semestralny');

INSERT INTO grades (grade_id, student_id, subject_id, lecturer_id, grade_value, description)
VALUES (3, 1001, 1, 2, 4.0, 'Kolokwium 1');

INSERT INTO payments (payment_id, student_id, amount, title, paid_flag, due_date)
VALUES (1, 1003, 22.00, 'Oplata za legitymacje studencka', 'Y', DATE '2025-10-15');

INSERT INTO payments (payment_id, student_id, amount, title, paid_flag, due_date)
VALUES (2, 1003, 2000.00, 'Czesne - Semestr 3', 'Y', DATE '2025-11-01');

INSERT INTO payments (payment_id, student_id, amount, title, paid_flag, due_date)
VALUES (3, 1003, 250.00, 'Oplata za powtarzanie przedmiotu: Algorytmy', 'N', DATE '2026-06-15');

INSERT INTO payments (payment_id, student_id, amount, title, paid_flag, due_date)
VALUES (4, 1001, 22.00, 'Oplata za legitymacje studencka', 'N', DATE '2025-10-15');

INSERT INTO payments (payment_id, student_id, amount, title, paid_flag, due_date)
VALUES (5, 1001, 250.00, 'Oplata za powtarzanie przedmiotu: Algorytmy', 'N', DATE '2026-06-15');

INSERT INTO applications (application_id, student_id, application_type, content, status, created_at)
VALUES (
    1,
    1001,
    'OTHER',
    'Prosba o wydanie zaswiadczenia o statusie studenta.',
    'SUBMITTED',
    TIMESTAMP '2026-01-10 10:15:00'
);

INSERT INTO messages (message_id, sender_user_id, recipient_user_id, subject, content, status, sent_at)
VALUES (
    1,
    2,
    1001,
    'Informacja o zajeciach',
    'Prosze pamietac o oddaniu projektu semestralnego.',
    'SENT',
    TIMESTAMP '2026-01-11 09:00:00'
);

INSERT INTO messages (message_id, sender_user_id, recipient_user_id, subject, content, status, sent_at)
VALUES (
    2,
    1003,
    2,
    'Pytanie o projekt',
    'Dzien dobry, czy projekt musi byc w JavaFX?',
    'READ',
    TIMESTAMP '2026-01-11 11:30:00'
);

INSERT INTO messages (message_id, sender_user_id, recipient_user_id, subject, content, status, sent_at)
VALUES (
    3,
    2,
    1003,
    'Odpowiedz: projekt',
    'Tak, projekt powinien miec interfejs JavaFX.',
    'SENT',
    TIMESTAMP '2026-01-11 12:10:00'
);

INSERT INTO resources (resource_id, resource_code, resource_name, category, total_quantity, available_flag)
VALUES (1, 'RES-LAP-001', 'Laptop Dell 15', 'EQUIPMENT', 3, 'Y');

INSERT INTO resources (resource_id, resource_code, resource_name, category, total_quantity, available_flag)
VALUES (2, 'RES-PROJ-001', 'Projektor Epson', 'EQUIPMENT', 2, 'Y');

INSERT INTO loans (loan_id, resource_id, borrower_user_id, loan_date, due_date, return_date, returned_flag)
VALUES (1, 1, 1003, DATE '2026-01-05', DATE '2026-01-20', NULL, 'N');

INSERT INTO loans (loan_id, resource_id, borrower_user_id, loan_date, due_date, return_date, returned_flag)
VALUES (2, 2, 1001, DATE '2025-12-01', DATE '2025-12-15', DATE '2025-12-14', 'Y');

INSERT INTO service_tickets (ticket_id, reporter_user_id, title, description, status, created_at, assigned_admin_id)
VALUES (
    1,
    1003,
    'Brak dostepu do WiFi',
    'W sali 312 nie dziala eduroam.',
    'OPEN',
    TIMESTAMP '2026-01-12 08:40:00',
    3
);

INSERT INTO audit_logs (audit_log_id, user_id, action_name, entity_name, entity_id, action_details, created_at)
VALUES (
    1,
    3,
    'SEED_DATA_INSERT',
    'DATABASE',
    NULL,
    'Initial USOS 2.0 seed created.',
    SYSTIMESTAMP
);

COMMIT;

