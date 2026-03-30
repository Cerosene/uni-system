# I kamień milowy — część Mateusz Lewandowski

## Zakres odpowiedzialności
- model obiektowy
- logika biznesowa
- testy jednostkowe

## Zaimplementowane klasy modelu
- BaseEntity
- User
- Student
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

## Zaimplementowane enumy
- UserRole
- Semester
- RequestStatus
- RequestType
- MessageStatus
- ServiceTicketStatus

## Zaimplementowane serwisy
- AuthService
- GradeService
- RequestService
- MessageService
- ServiceTicketService

## Zaimplementowane reguły biznesowe
- rejestracja użytkownika
- logowanie użytkownika
- walidacja unikalności emaila
- walidacja hasła
- dodawanie ocen w dopuszczalnym zakresie
- składanie wniosków i zmiana ich statusu
- wysyłanie wiadomości między użytkownikami
- tworzenie, przypisywanie i zamykanie zgłoszeń serwisowych

## Testy jednostkowe
### Serwisy
- AuthServiceTest
- GradeServiceTest
- RequestServiceTest
- MessageServiceTest
- ServiceTicketServiceTest

### Modele
- StudentTest
- GradeTest