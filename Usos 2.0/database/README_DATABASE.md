# USOS 2.0 - Database bootstrap (Oracle)

Ten zestaw skryptow przygotowuje pierwszy etap integracji bazy Oracle dla projektu USOS 2.0.

## Parametry polaczenia

- URL: `jdbc:oracle:thin:@//localhost:1521/FREEPDB1`
- User: `USOS`
- Password: `usos123`

## Kolejnosc uruchamiania

1. `01_create_user.sql` (uruchamiany jako `SYS AS SYSDBA`)
2. `02_schema.sql` (uruchamiany jako `USOS`)
3. `03_seed.sql` (uruchamiany jako `USOS`)

Do czyszczenia schematu:

4. `04_drop.sql` (uruchamiany jako `USOS`)

## Przyklad uruchomienia (SQL*Plus)

### 1) Tworzenie usera

```sql
sqlplus sys/<SYS_PASSWORD>@//localhost:1521/FREEPDB1 as sysdba
@01_create_user.sql
```

### 2) Schemat i dane

```sql
sqlplus USOS/usos123@//localhost:1521/FREEPDB1
@02_schema.sql
@03_seed.sql
```

### 3) Drop tabel

```sql
sqlplus USOS/usos123@//localhost:1521/FREEPDB1
@04_drop.sql
```

## Loginy testowe z seeda

- `dmytro@uni.pl` / `pass123`
- `mateusz@uni.pl` / `password123`
- `lecturer@uni.pl` / `password123`
- `m.kow@uni.pl` / `password123`
- `admin@uni.pl` / `password123`

## Mapa model -> tabela

- `User` -> `users`
- `Student` -> `students`
- `Employee` -> `employees`
- `Lecturer` -> `lecturers`
- `Administrator` -> `admins`
- `Course` -> `subjects`
- `StudentGroup` -> `course_groups`
- relacja grupa-student -> `enrollments`
- `Grade` -> `grades`
- `Payment` -> `payments`
- `Request` -> `applications`
- `Message` -> `messages`
- `Rental` -> `loans` + `resources`
- `ServiceTicket` -> `service_tickets`
- log techniczny -> `audit_logs`

