# Bookaroo

**Bookaroo** to aplikacja typu 'Social Reading' wzorowaną na serwisie GoodReads/Lubimyczytać. System umożliwia użytkownikom katalogowanie przeczytanych książek, tworzenie własnych półek tematycznych (wirtualna biblioteczka), ocenianie pozycji oraz pisanie recenzji. Kluczowym aspektem jest moduł statystyk (agregacja danych o popularności książek) oraz panel administratora do zarządzania bazą literacką. Aplikacja również obsługuje import/eksport danych profilowych. 
Projekt wykorzystuje **Spring Boot** i składa się z widoków Thymeleaf oraz części REST-owej.

---

## Główne Funkcjonalności

### Użytkownicy i Bezpieczeństwo
* **Rejestracja i Logowanie**: Pełna obsługa uwierzytelniania.
* **Custom Security**: Implementacja `CustomUserDetailsService` oraz separacja modelu sesji (`BookarooUserDetails`) od encji bazodanowej.
* **Role**: Podział na `USER` (czytelnik) i `ADMIN` (administrator).
* **Profile**: Zarządzanie danymi użytkownika (awatary, bio).

### Katalog Książek
* **CRUD**: Tworzenie, edycja i usuwanie książek (zabezpieczone dla Admina).
* **Paginacja i Sortowanie**: Wydajne pobieranie list książek (`Page<BookDTO>`).
* **Zaawansowane Wyszukiwanie**: Szukanie po frazie (tytuł, ISBN, autor).
* **Filtrowanie**: Po autorze, gatunku, roku wydania.
* **Rankingi**: Endpointy dla najlepiej ocenianych książek (`/top`).
* **Optymalizacja**: Wykorzystanie **JdbcTemplate** do operacji wymagających wysokiej wydajności (tzw. bulk operations), obok standardowego **Spring Data JPA**.

### Półki i Organizacja
* **Wirtualne Półki**: Domyślne (np. "Przeczytane") oraz tworzone przez użytkownika (Custom).
* **Zarządzanie Stanem**: Dodawanie, przenoszenie i usuwanie książek z półek.
* **Statystyki**: Możliwość wzięcia udziału w `Reading challenge` - informujący o ilości przeczytanych książek w roku.

### Obsługa Plików
* **Upload**: Ustawianie awatarów na swoim profilu.
* **Resource Handler**: Skonfigurowany `WebConfig` udostępniający pliki statyczne z katalogu `uploads/` pod publicznym adresem URL.

### Obsługa Błędów
* **Global Exception Handler**: Scentralizowana obsługa wyjątków.
* **Spójne komunikaty**: Ujednolicony format błędów JSON (status, message, timestamp) dla kodów 400, 404, 409, 500.

---

## Tech Stack

* **Java:** 17+
* **Framework:** Spring Boot 3
* **Baza danych:** H2 (dev)
* **ORM / JDBC:** Hibernate, Spring Data JPA, JdbcTemplate
* **Bezpieczeństwo:** Spring Security
* **API Documentation:** OpenAPI 3 (Swagger UI)
* **Narzędzia:** Maven, Lombok

---

## Instalacja i Konfiguracja

### 1. Wymagania
* Java JDK 17+
* Maven

### 2. Klonowanie repozytorium
```bash
git clone [https://github.com/ahajkowska/Bookaroo.git](https://github.com/ahajkowska/Bookaroo.git)
```

### 3. Uruchomienie aplikacji
```bash
mvn spring-boot:run
```
Aplikacja będzie dostępna pod adresem: `http://localhost:8080`

### 4. Dokumentacja API (Swagger)

Projekt posiada automatycznie generowaną dokumentację endpointów. Po uruchomieniu wejdź na:

```bash
http://localhost:8080/swagger-ui/index.html
```

Znajdziesz tam:

* Zakładki (Book Management, Bookshelf Management, User Management).
* Przykładowe JSON-y dla żądań i odpowiedzi (Examples).
* Możliwość przetestowania API ("Try it out").

### 5. Testowanie aplikacji

Projekt wykorzystuje JUnit 5 oraz MockMvc do testów integracyjnych i jednostkowych. Zaimplementowano również customowe mocki bezpieczeństwa (@WithMockCustomUser) dla wiernego odwzorowania kontekstu Spring Security.

Aby wygenerować testy:
```bash
mvn test
```

Aby wygenerować raport z pokryciem kodu:
```bash
mvn clean test jacoco:report
```

### Autor

@ahajkowska | Amelia Hajkowska