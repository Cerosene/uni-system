# USOS 2.0

Uproszczony system uczelniany tworzony w Javie/JavaFX na potrzeby projektu zaliczeniowego.

## Technologie

- Java 17+
- JavaFX 21
- Maven
- JUnit 5
- Docelowo: Oracle Database + JDBC

## Uruchomienie projektu

Projekt najlepiej uruchamiać przez Maven z katalogu głównego projektu:

```powershell
cd "C:\fork\uni-system\Usos 2.0"
mvn clean javafx:run
```

Alternatywnie w IntelliJ można uruchomić klasę launcher:

```text
pl.usos2.client.Launcher
```

Nie uruchamiaj bezpośrednio klasy:

```text
pl.usos2.client.MainApp
```

bo może pojawić się błąd:

```text
Error: JavaFX runtime components are missing, and are required to run this application
```

## Wymagana konfiguracja IntelliJ

Zalecana konfiguracja:

```text
Project SDK: JDK 21
Language level: 17
Maven Runner JRE: Project JDK 21
```

W projekcie używany jest JavaFX 21, dlatego najlepiej nie uruchamiać aplikacji na JDK 26.

## Przykładowe konta do logowania

Dane testowe są tworzone w klasie:

```text
server/src/main/java/pl/usos2/server/config/DemoDataInitializer.java
```

| Rola | Imię i nazwisko | E-mail / login | Hasło |
|---|---|---|---|
| Student | Dmytro Lytvyn | `dmytro@uni.pl` | `pass123` |
| Student | Mateusz Lewandowski | `mateusz@uni.pl` | `password123` |
| Prowadzący | Tomasz Nowak | `lecturer@uni.pl` | `password123` |
| Prowadzący | Maria Kowalska | `m.kow@uni.pl` | `password123` |
| Administrator | Anna Zielińska | `admin@uni.pl` | `password123` |

## Zakres kont testowych

### Student

Po zalogowaniu jako student można testować między innymi:

- panel studenta,
- przeglądanie ocen,
- wiadomości,
- wnioski studenckie,
- opłaty,
- zgłoszenia serwisowe.

Najlepsze konto do testowania widocznych danych studenta:

```text
Login: mateusz@uni.pl
Hasło: password123
```

oraz:

```text
Login: dmytro@uni.pl
Hasło: pass123
```

### Prowadzący

Po zalogowaniu jako prowadzący można testować między innymi:

- panel prowadzącego,
- grupy/przedmioty,
- wystawianie i podgląd ocen,
- wiadomości.

Konta prowadzących:

```text
Login: lecturer@uni.pl
Hasło: password123
```

```text
Login: m.kow@uni.pl
Hasło: password123
```

### Administrator

Po zalogowaniu jako administrator można testować między innymi:

- panel administratora,
- użytkowników,
- pracowników,
- zgłoszenia,
- wnioski,
- opłaty,
- widoki administracyjne.

Konto administratora:

```text
Login: admin@uni.pl
Hasło: password123
```

## Uruchomienie testów

Testy jednostkowe można uruchomić poleceniem:

```powershell
cd "C:\fork\uni-system\Usos 2.0"
mvn clean test
```

## Ważne uwagi

- Hasła w projekcie są zapisane jawnie, ponieważ jest to wersja demonstracyjna na potrzeby zaliczenia.
- Dane przykładowe są inicjalizowane przy starcie aplikacji przez `DemoDataInitializer`.
- Projekt nie korzysta jeszcze w pełni z docelowej bazy Oracle/JDBC w każdej części aplikacji.
- Na II kamień milowy najważniejsze są: działające GUI, podpięcie widoków pod logikę biznesową, testy usług oraz przygotowanie pod integrację z bazą danych.

## Główne osoby i odpowiedzialności

| Osoba | Zakres |
|---|---|
| Mateusz Lewandowski | model obiektowy, logika biznesowa, testy jednostkowe |
| Marek Sikora | baza danych Oracle, JDBC, DAO |
| Mykyta Lytvyn | GUI JavaFX, integracja widoków z logiką |
