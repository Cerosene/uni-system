# II kamień milowy — część Mateusz Lewandowski

## Zakres odpowiedzialności
- model obiektowy
- logika biznesowa
- testy jednostkowe

## Cel prac wykonanych w II kamieniu
W drugim kamieniu milowym moja część pracy koncentrowała się na rozwinięciu warstwy backendowej systemu, w szczególności na:
- rozbudowie logiki biznesowej,
- przygotowaniu i dopracowaniu modeli domenowych,
- rozszerzeniu testów jednostkowych dla serwisów i modeli,
- przygotowaniu kodu pod dalszą integrację z warstwą bazy danych oraz interfejsem użytkownika.

## Rozwinięte i utrzymane elementy modelu obiektowego
W ramach drugiego kamienia dalej rozwijane i wykorzystywane były klasy modelu domenowego, między innymi:
- BaseEntity
- User
- Student
- Employee
- Lecturer
- Administrator
- Course
- StudentGroup
- Grade
- Request
- Message
- ServiceTicket
- Payment
- Rental

## Rozwinięta logika biznesowa
W warstwie usług rozwijana była logika odpowiedzialna za kluczowe funkcjonalności systemu.

### Moduł kont użytkowników
- rejestracja użytkownika
- logowanie użytkownika
- wylogowanie użytkownika
- aktualizacja podstawowych danych konta
- zmiana adresu e-mail
- zmiana hasła
- aktywacja i dezaktywacja konta
- pobieranie użytkowników według roli
- wyszukiwanie użytkownika po identyfikatorze i adresie e-mail

### Moduł pracowników
- dodawanie pracownika
- edycja danych pracownika
- zmiana stanowiska
- zmiana pensji
- zmiana numeru pracownika
- aktywacja i dezaktywacja pracownika
- wyszukiwanie pracownika po identyfikatorze
- wyszukiwanie pracownika po adresie e-mail
- wyszukiwanie pracownika po numerze pracownika
- usuwanie pracownika
- pobieranie aktywnych pracowników

### Moduł opłat
- tworzenie płatności
- oznaczanie płatności jako opłaconej
- pobieranie płatności studenta
- pobieranie opłaconych i nieopłaconych płatności
- pobieranie zaległych płatności
- wyszukiwanie płatności po identyfikatorze
- usuwanie nieopłaconej płatności

### Moduł wypożyczeń
- tworzenie wypożyczenia
- zwrot wypożyczenia
- przedłużenie terminu zwrotu
- pobieranie aktywnych i zakończonych wypożyczeń
- pobieranie przeterminowanych wypożyczeń
- wyszukiwanie wypożyczeń po identyfikatorze, użytkowniku i zasobie

### Moduł zgłoszeń serwisowych
- tworzenie zgłoszenia
- przypisywanie zgłoszenia administratorowi
- zmiana przypisania administratora
- zamykanie zgłoszenia
- wyszukiwanie zgłoszeń po identyfikatorze i tytule
- pobieranie zgłoszeń po statusie
- pobieranie zgłoszeń po zgłaszającym
- pobieranie zgłoszeń przypisanych do administratora

### Dodatkowo utrzymane i rozwinięte moduły wspierające
- oceny
- wiadomości
- wnioski

## Zaimplementowane reguły biznesowe
W usługach uwzględniono między innymi:
- walidację danych wejściowych,
- kontrolę duplikatów identyfikatorów i adresów e-mail,
- kontrolę unikalności numeru pracownika,
- kontrolę poprawności zmian statusów,
- blokadę niepoprawnych operacji logicznych, np. ponownego zamknięcia zgłoszenia lub ponownego oznaczenia płatności jako opłaconej,
- walidację danych dla ocen, wiadomości, wniosków, wypożyczeń i płatności.

## Testy jednostkowe
### Testy usług
- AuthServiceTest
- EmployeeServiceTest
- PaymentServiceTest
- RentalServiceTest
- ServiceTicketServiceTest
- GradeServiceTest
- MessageServiceTest
- RequestServiceTest

### Testy modeli
- StudentTest
- EmployeeTest
- GradeTest
- PaymentTest
- RentalTest
- StudentGroupTest

## Efekt końcowy mojej części II kamienia
Moja część drugiego kamienia milowego przygotowuje backendową warstwę systemu do dalszego etapu prac, w szczególności do:
- podłączenia warstwy DAO i JDBC,
- integracji z bazą danych Oracle,
- połączenia logiki biznesowej z interfejsem graficznym,
- dalszego rozwoju architektury klient-serwer w kolejnym etapie projektu.

## Aktualizacja po integracji klient-serwer

Po integracji z warstwą klient-serwer moja część logiki biznesowej została wykorzystana przez aplikację serwerową oraz zdalne serwisy klienta.

Najważniejsze elementy po integracji:
- serwisy biznesowe działają po stronie serwera,
- klient JavaFX komunikuje się z serwerem przez sockety,
- operacje na ocenach, wiadomościach, wnioskach, płatnościach, wypożyczeniach i zgłoszeniach są wykonywane przez warstwę serwisową,
- testy jednostkowe logiki biznesowej zostały utrzymane na fake DAO,
- logika biznesowa nie jest bezpośrednio zależna od GUI.

Dodatkowo po stronie klienta dodano obsługę zdalnego liczenia średniej ocen studenta w RemoteGradeService.