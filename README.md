# USOS 2.0

Uproszczony system uczelniany tworzony w Javie/JavaFX na potrzeby projektu zaliczeniowego.

Projekt korzysta z lokalnej bazy **Oracle Database Free** uruchamianej w kontenerze Docker przez **Docker Desktop + WSL2**.

Projekt działa w architekturze **klient–serwer**:

```text
1. Oracle Database w Dockerze
2. Server USOS 2.0
3. Klient JavaFX
```

---

## Technologie

* Java 17+
* Zalecane: JDK 21
* JavaFX 21
* Maven
* JUnit 5
* Oracle Database Free
* JDBC
* Docker Desktop
* WSL2
* DBeaver
* Sockety TCP
* Architektura klient–serwer

---

## Wymagania systemowe

Do uruchomienia projektu wymagane są:

* Windows 10/11
* WSL2
* Docker Desktop z backendem WSL2
* Java JDK 21
* Maven
* DBeaver albo inne narzędzie do obsługi Oracle Database
* IntelliJ IDEA lub inne IDE obsługujące Maven/JavaFX

---

## Wymagana konfiguracja IntelliJ

Zalecana konfiguracja:

```text
Project SDK: JDK 21
Language level: 17
Maven Runner JRE: Project JDK 21
```

W projekcie używany jest JavaFX 21, dlatego najlepiej nie uruchamiać aplikacji na JDK 26.

Najbezpieczniej uruchamiać projekt przez Maven, a nie przez bezpośrednie odpalenie klasy `MainApp`.

---

## Konfiguracja bazy Oracle przez Docker Desktop + WSL2

Projekt korzysta z lokalnego kontenera Oracle Database Free.

---

### 1. Sprawdzenie WSL2

W PowerShellu można sprawdzić dostępne dystrybucje WSL:

```powershell
wsl -l -v
```

Przykładowy poprawny wynik:

```text
NAME      STATE           VERSION
Ubuntu    Stopped         2
```

Jeżeli dystrybucja działa na wersji 1, należy przełączyć ją na WSL2.

---

### 2. Pobranie obrazu Oracle Database Free

W PowerShellu albo terminalu uruchom:

```powershell
docker pull container-registry.oracle.com/database/free:latest
```

---

### 3. Uruchomienie kontenera Oracle

Jeżeli kontener Oracle jeszcze nie istnieje:

```powershell
docker run -d --name usos-oracle -p 1521:1521 -e ORACLE_PWD=Admin12345 container-registry.oracle.com/database/free:latest
```

Jeżeli kontener już istnieje:

```powershell
docker start usos-oracle
```

Po uruchomieniu można sprawdzić status kontenera:

```powershell
docker ps
```

Kontener powinien mieć status podobny do:

```text
Up ... (healthy)
```

Można też podejrzeć logi:

```powershell
docker logs -f usos-oracle
```

Baza jest gotowa, gdy w logach pojawi się komunikat:

```text
DATABASE IS READY TO USE!
```

---

## Przydatne komendy Dockera

Uruchomienie istniejącego kontenera:

```powershell
docker start usos-oracle
```

Zatrzymanie kontenera:

```powershell
docker stop usos-oracle
```

Sprawdzenie działających kontenerów:

```powershell
docker ps
```

Podgląd logów:

```powershell
docker logs -f usos-oracle
```

Wejście do SQL*Plus jako `system`:

```powershell
docker exec -it usos-oracle sqlplus system/Admin12345@localhost:1521/FREEPDB1
```

Wejście do SQL*Plus jako użytkownik projektu:

```powershell
docker exec -it usos-oracle sqlplus USOS/usos123@localhost:1521/FREEPDB1
```

---

## Konfiguracja bazy danych projektu

Projekt używa następujących parametrów połączenia:

```text
Host: localhost
Port: 1521
Service name: FREEPDB1
JDBC URL: jdbc:oracle:thin:@//localhost:1521/FREEPDB1

User: USOS
Password: usos123

System user: system
System password: Admin12345
```

---

## Utworzenie użytkownika USOS i załadowanie schematu

Skrypty bazy danych znajdują się w katalogu:

```text
database/
```

Kolejność uruchamiania:

```text
01_create_user.sql
02_schema.sql
03_seed.sql
```

Opcjonalnie do usunięcia obiektów:

```text
04_drop.sql
```

---

### Opcja A — uruchamianie skryptów przez DBeavera

Skrypt:

```text
01_create_user.sql
```

należy uruchomić jako:

```text
system
```

albo:

```text
SYS AS SYSDBA
```

Tworzy on użytkownika/schemat:

```text
USOS / usos123
```

Skrypty:

```text
02_schema.sql
03_seed.sql
```

należy uruchomić już jako użytkownik:

```text
USOS / usos123
```

---

### Opcja B — uruchamianie skryptów przez PowerShell i Docker

Najpierw wejdź do katalogu głównego projektu:

```powershell
cd "C:\fork\uni-system\Usos 2.0"
```

Skopiuj katalog `database` do kontenera:

```powershell
docker cp ".\database" usos-oracle:/tmp/database
```

Utwórz użytkownika `USOS`:

```powershell
docker exec -it usos-oracle bash -lc "sqlplus -L system/Admin12345@localhost:1521/FREEPDB1 @/tmp/database/01_create_user.sql"
```

Utwórz tabele jako `USOS`:

```powershell
docker exec -it usos-oracle bash -lc "sqlplus -L USOS/usos123@localhost:1521/FREEPDB1 @/tmp/database/02_schema.sql"
```

Załaduj dane testowe jako `USOS`:

```powershell
docker exec -it usos-oracle bash -lc "sqlplus -L USOS/usos123@localhost:1521/FREEPDB1 @/tmp/database/03_seed.sql"
```

---

## Reset bazy danych

Jeżeli baza była już wcześniej utworzona i trzeba ją wyczyścić, można uruchomić:

```powershell
docker exec -it usos-oracle bash -lc "sqlplus -L USOS/usos123@localhost:1521/FREEPDB1 @/tmp/database/04_drop.sql"
```

Następnie ponownie uruchomić:

```powershell
docker exec -it usos-oracle bash -lc "sqlplus -L USOS/usos123@localhost:1521/FREEPDB1 @/tmp/database/02_schema.sql"
docker exec -it usos-oracle bash -lc "sqlplus -L USOS/usos123@localhost:1521/FREEPDB1 @/tmp/database/03_seed.sql"
```

---

## Konfiguracja połączenia w DBeaverze

W DBeaverze należy utworzyć nowe połączenie Oracle:

```text
Database: Oracle
Host: localhost
Port: 1521
Service name: FREEPDB1
Username: USOS
Password: usos123
```

Po połączeniu można sprawdzić, czy dane zostały załadowane:

```sql
SELECT *
FROM users;
```

albo:

```sql
SELECT COUNT(*)
FROM users;
```

---

## Uruchomienie projektu

Projekt działa w architekturze klient–serwer, dlatego aplikację należy uruchamiać w odpowiedniej kolejności:

```text
1. Docker / Oracle Database
2. Skrypty bazy danych
3. Server USOS 2.0
4. Klient JavaFX
```

---

### 1. Uruchomienie Oracle w Dockerze

Jeżeli kontener Oracle jeszcze nie istnieje:

```powershell
docker run -d --name usos-oracle -p 1521:1521 -e ORACLE_PWD=Admin12345 container-registry.oracle.com/database/free:latest
```

Jeżeli kontener już istnieje:

```powershell
docker start usos-oracle
```

Sprawdź, czy kontener działa:

```powershell
docker ps
```

---

### 2. Wejście do katalogu projektu

W PowerShellu przejdź do katalogu głównego projektu:

```powershell
cd "C:\fork\uni-system\Usos 2.0"
```

---

### 3. Sprawdzenie testów

Przed uruchomieniem aplikacji warto sprawdzić testy:

```powershell
mvn clean test
```

Opcjonalnie można też zbudować projekt:

```powershell
mvn clean package
```

---

### 4. Uruchomienie servera

Server należy uruchomić w osobnym oknie PowerShell.

```powershell
cd "C:\fork\uni-system\Usos 2.0"
mvn exec:java "-Dexec.mainClass=pl.usos2.server.ServerLauncher" "-Dusos.demo.seed=false" "-Dusos.server.port=5555"
```

Tego okna nie należy zamykać podczas działania klienta.

Parametr:

```text
-Dusos.demo.seed=false
```

oznacza, że server nie będzie dodatkowo dodawał danych demonstracyjnych przy starcie. Dane testowe powinny pochodzić ze skryptu:

```text
database/03_seed.sql
```

---

### 5. Uruchomienie klienta JavaFX

Klienta należy uruchomić w drugim oknie PowerShell, gdy server już działa.

```powershell
cd "C:\fork\uni-system\Usos 2.0"
mvn javafx:run "-Dusos.server.host=localhost" "-Dusos.server.port=5555"
```

Klient połączy się wtedy z serverem działającym lokalnie na porcie:

```text
5555
```

---

### 6. Kolejność uruchamiania w skrócie

Najpierw Oracle:

```powershell
docker start usos-oracle
```

Potem server w pierwszym PowerShellu:

```powershell
cd "C:\fork\uni-system\Usos 2.0"
mvn exec:java "-Dexec.mainClass=pl.usos2.server.ServerLauncher" "-Dusos.demo.seed=false" "-Dusos.server.port=5555"
```

Potem klient w drugim PowerShellu:

```powershell
cd "C:\fork\uni-system\Usos 2.0"
mvn javafx:run "-Dusos.server.host=localhost" "-Dusos.server.port=5555"
```

---

## Uruchamianie przez pliki `.bat`

Można też używać gotowych plików:

```text
run-server.bat
run-client.bat
```

Zalecana zawartość pliku `run-server.bat`:

```bat
@echo off
mvn exec:java "-Dexec.mainClass=pl.usos2.server.ServerLauncher" "-Dusos.demo.seed=false" "-Dusos.server.port=5555"
pause
```

Zalecana zawartość pliku `run-client.bat`:

```bat
@echo off
mvn javafx:run "-Dusos.server.host=localhost" "-Dusos.server.port=5555"
pause
```

Najpierw należy uruchomić:

```text
run-server.bat
```

a dopiero potem:

```text
run-client.bat
```

---

## Uruchamianie w IntelliJ IDEA

Zalecane jest uruchamianie projektu przez Maven, ponieważ wtedy JavaFX jest poprawnie konfigurowany automatycznie.

Nie należy uruchamiać bezpośrednio klasy:

```text
pl.usos2.client.MainApp
```

bo może pojawić się błąd:

```text
Error: JavaFX runtime components are missing, and are required to run this application
```

Do uruchomienia klienta najlepiej używać:

```powershell
mvn javafx:run "-Dusos.server.host=localhost" "-Dusos.server.port=5555"
```

Server najlepiej uruchamiać przez:

```powershell
mvn exec:java "-Dexec.mainClass=pl.usos2.server.ServerLauncher" "-Dusos.demo.seed=false" "-Dusos.server.port=5555"
```

Alternatywnie w IntelliJ można uruchomić klasę launcher klienta:

```text
pl.usos2.client.Launcher
```

Nie uruchamiaj bezpośrednio klasy:

```text
pl.usos2.client.MainApp
```

---

## Diagnostyka połączenia z bazą

W projekcie znajdują się klasy diagnostyczne sprawdzające połączenie Java z Oracle.

Przykładowa klasa:

```text
server/src/main/java/pl/usos2/server/database/DatabaseConnectionDiagnostic.java
```

Diagnostyka sprawdza między innymi:

```sql
SELECT 1 FROM dual;
```

oraz liczbę użytkowników:

```sql
SELECT COUNT(*) FROM users;
```

Poprawny wynik powinien wyglądać podobnie do:

```text
DB ping OK: 1
Users count: 5
Database diagnostic finished successfully.
```

Dodatkowo istnieje pełna diagnostyka bazy:

```text
server/src/main/java/pl/usos2/server/database/DatabaseFullDiagnostic.java
```

Sprawdza ona liczbę rekordów w kluczowych tabelach projektu.

---

## Przykładowe konta do logowania

Dane testowe są ładowane do bazy przez skrypt:

```text
database/03_seed.sql
```

| Rola          | Imię i nazwisko     | E-mail / login    | Hasło         |
| ------------- | ------------------- | ----------------- | ------------- |
| Student       | Dmytro Lytvyn       | `dmytro@uni.pl`   | `pass123`     |
| Student       | Mateusz Lewandowski | `mateusz@uni.pl`  | `password123` |
| Prowadzący    | Tomasz Nowak        | `lecturer@uni.pl` | `password123` |
| Prowadzący    | Marek Kowalski      | `m.kow@uni.pl`    | `password123` |
| Administrator | Anna Zielińska      | `admin@uni.pl`    | `password123` |

---

## Zakres kont testowych

### Student

Po zalogowaniu jako student można testować między innymi:

* panel studenta,
* przeglądanie ocen,
* wiadomości,
* wnioski studenckie,
* opłaty,
* zgłoszenia serwisowe,
* plan zajęć,
* wypożyczenia.

Najlepsze konta do testowania widocznych danych studenta:

```text
Login: mateusz@uni.pl
Hasło: password123
```

oraz:

```text
Login: dmytro@uni.pl
Hasło: pass123
```

---

### Prowadzący

Po zalogowaniu jako prowadzący można testować między innymi:

* panel prowadzącego,
* grupy/przedmioty,
* wystawianie i podgląd ocen,
* wiadomości,
* plan zajęć prowadzącego.

Konta prowadzących:

```text
Login: lecturer@uni.pl
Hasło: password123
```

```text
Login: m.kow@uni.pl
Hasło: password123
```

---

### Administrator

Po zalogowaniu jako administrator można testować między innymi:

* panel administratora,
* użytkowników,
* pracowników,
* zgłoszenia,
* wnioski,
* opłaty,
* kursy,
* grupy,
* zapisy,
* widoki administracyjne,
* audit logi.

Konto administratora:

```text
Login: admin@uni.pl
Hasło: password123
```

---

## Moduły korzystające z Oracle/JDBC

Projekt został przepięty na Oracle/JDBC w następujących obszarach:

* logowanie i użytkownicy,
* oceny,
* wiadomości,
* wnioski studenckie,
* płatności,
* zgłoszenia serwisowe,
* wypożyczenia i zasoby,
* pracownicy,
* zarządzanie użytkownikami,
* kursy, grupy i zapisy,
* audit logi,
* diagnostyka bazy.

Główne tabele używane przez aplikację:

```text
users
roles
students
lecturers
admins
employees
subjects
course_groups
enrollments
grades
messages
applications
payments
service_tickets
resources
loans
audit_logs
```

---

## Uruchomienie testów

Testy jednostkowe można uruchomić poleceniem:

```powershell
cd "C:\fork\uni-system\Usos 2.0"
mvn clean test
```

---

## Typowe problemy

### Docker nie działa

Sprawdź, czy Docker Desktop jest uruchomiony oraz czy korzysta z backendu WSL2.

```powershell
docker ps
```

---

### Kontener Oracle nie działa

Uruchom kontener:

```powershell
docker start usos-oracle
```

Sprawdź logi:

```powershell
docker logs -f usos-oracle
```

---

### Błąd połączenia z bazą

Sprawdź, czy kontener działa:

```powershell
docker ps
```

Sprawdź parametry połączenia:

```text
localhost
1521
FREEPDB1
USOS
usos123
```

---

### DBeaver nie widzi tabel

Upewnij się, że jesteś połączony jako:

```text
USOS
```

a nie jako:

```text
system
```

Tabele projektu znajdują się w schemacie `USOS`.

---

### Błąd JavaFX runtime components are missing

Nie uruchamiaj bezpośrednio:

```text
pl.usos2.client.MainApp
```

Uruchamiaj klienta przez Maven:

```powershell
mvn javafx:run "-Dusos.server.host=localhost" "-Dusos.server.port=5555"
```

albo przez:

```text
pl.usos2.client.Launcher
```

---

### PowerShell zgłasza błąd `Unknown lifecycle phase ".mainClass=..."`

Jeżeli przy uruchamianiu servera pojawia się błąd podobny do:

```text
Unknown lifecycle phase ".mainClass=pl.usos2.server.ServerLauncher"
```

oznacza to, że PowerShell niepoprawnie zinterpretował parametr Mavena zaczynający się od `-D`.

Błędna komenda:

```powershell
mvn exec:java -Dexec.mainClass=pl.usos2.server.ServerLauncher -Dusos.demo.seed=false -Dusos.server.port=5555
```

Poprawna komenda w PowerShellu:

```powershell
mvn exec:java "-Dexec.mainClass=pl.usos2.server.ServerLauncher" "-Dusos.demo.seed=false" "-Dusos.server.port=5555"
```

Analogicznie klienta należy uruchamiać tak:

```powershell
mvn javafx:run "-Dusos.server.host=localhost" "-Dusos.server.port=5555"
```

Alternatywnie można użyć trybu `--%`, który wyłącza dalsze parsowanie argumentów przez PowerShell:

```powershell
mvn --% exec:java -Dexec.mainClass=pl.usos2.server.ServerLauncher -Dusos.demo.seed=false -Dusos.server.port=5555
```

Najbezpieczniejsza i najbardziej czytelna wersja to jednak komenda z cudzysłowami przy parametrach `-D`.

---

### Klient nie łączy się z serverem

Sprawdź, czy server nadal działa.

Server powinien być uruchomiony w osobnym oknie PowerShell komendą:

```powershell
mvn exec:java "-Dexec.mainClass=pl.usos2.server.ServerLauncher" "-Dusos.demo.seed=false" "-Dusos.server.port=5555"
```

Klient powinien być uruchomiony w drugim oknie PowerShell komendą:

```powershell
mvn javafx:run "-Dusos.server.host=localhost" "-Dusos.server.port=5555"
```

Oba muszą używać tego samego portu:

```text
5555
```

---

### Po starcie server dodaje dodatkowe dane demo

Jeżeli dane testowe zostały załadowane przez:

```text
database/03_seed.sql
```

to server należy uruchamiać z parametrem:

```text
-Dusos.demo.seed=false
```

Poprawna komenda:

```powershell
mvn exec:java "-Dexec.mainClass=pl.usos2.server.ServerLauncher" "-Dusos.demo.seed=false" "-Dusos.server.port=5555"
```

---

## Ważne uwagi

* Hasła w projekcie są zapisane jawnie, ponieważ jest to wersja demonstracyjna na potrzeby zaliczenia.
* Dane przykładowe są ładowane przez skrypt `03_seed.sql`.
* Baza działa lokalnie w kontenerze Docker `usos-oracle`.
* Aplikacja łączy się z bazą przez JDBC.
* Projekt działa jako aplikacja klient–serwer: najpierw należy uruchomić Oracle w Dockerze, potem server `ServerLauncher`, a dopiero na końcu klienta JavaFX.
* W PowerShellu parametry Mavena zaczynające się od `-D` najlepiej zapisywać w cudzysłowach, np. `"-Dexec.mainClass=..."`.
* Jeżeli dane testowe zostały załadowane przez `database/03_seed.sql`, server należy uruchamiać z parametrem `"-Dusos.demo.seed=false"`.
* Wartości techniczne enumów pozostają w bazie, np. `OPEN`, `CLOSED`, `SCHOLARSHIP`, `APPROVED`, a w UI powinny być formatowane na czytelne polskie etykiety.
* Do testowania bazy najlepiej używać DBeavera oraz klas diagnostycznych.
* Przy problemach z wydajnością można zatrzymać kontener Oracle:

```powershell
docker stop usos-oracle
```

oraz wyłączyć WSL:

```powershell
wsl --shutdown
```
