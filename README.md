# Správa knih

Desktopová aplikace pro správu knih vytvořená v Javě s databází PostgreSQL.

## Features

* Přihlášení uživatelů
* Přidávání, úprava a mazání knih
* Správa uživatelských účtů
* Práce s databází
* CRUD operace
* JavaFX uživatelské rozhraní

## Technologies

* Java
* JavaFX
* PostgreSQL
* JDBC
* SQL
* IntelliJ IDEA

## Requirements

Pro spuštění projektu je potřeba:

* Java JDK
* PostgreSQL
* PostgreSQL JDBC Driver
* JavaFX
* IntelliJ IDEA nebo jiné Java IDE

## Database Setup

Aplikace očekává PostgreSQL databázi s následující konfigurací:

```text
Host: localhost
Port: 5432
Database: knihydb
User: postgres
```

### 1. Vytvoření databáze

V PostgreSQL vytvoř databázi:

```sql
CREATE DATABASE knihydb;
```

Struktura databáze je potřebná pro správné fungování aplikace.

### 2. Nastavení hesla

Heslo k PostgreSQL není uloženo přímo ve zdrojovém kódu.

Aplikace načítá heslo z environmentální proměnné:

```text
DB_PASSWORD
```

#### Windows

V systému vytvoř environmentální proměnnou:

```text
DB_PASSWORD=vaše_heslo
```

Poté restartuj IntelliJ IDEA, aby se nová proměnná načetla.

### 3. Spuštění projektu

1. Otevři projekt v IntelliJ IDEA.
2. Zkontroluj, že je nastaven správný JDK.
3. Zkontroluj, že jsou dostupné knihovny JavaFX a PostgreSQL JDBC Driver.
4. Ujisti se, že běží PostgreSQL server.
5. Ujisti se, že je vytvořena databáze `knihydb`.
6. Nastav environmentální proměnnou `DB_PASSWORD`.
7. Spusť `KnihaApp.java`.

## Project Structure

Hlavní části aplikace:

* `KnihaApp.java` – hlavní JavaFX aplikace
* `Kniha.java` – model knihy
* `KnihaDatabase.java` – práce s databází knih
* `UserDatabase.java` – práce s uživateli
* `KnihaClient.java` – klientská část aplikace
* `KnihaServerInterface.java` – rozhraní serverové části
* `AddKnihaDialog.java` – přidání knihy
* `EditKnihaDialog.java` – úprava knihy
* `DetailKnihaDialog.java` – detail knihy
