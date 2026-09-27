# Library Management System

A desktop Library Management System built with Java Swing and MySQL. The app creates its tables automatically when it starts; the MySQL server and database must be available first.

## Features

- Add, update, delete, and search books
- Manage library members
- Issue books for 14 days and record returns
- View issue and return history

## Requirements

- JDK 17 or newer
- Maven 3.9 or newer
- MySQL Server 8.0 or newer, running locally or reachable over the network

## Set up MySQL

Connect to MySQL as an administrator and create a database and application user. Choose your own strong password:

```sql
CREATE DATABASE library_management
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

CREATE USER 'library_app'@'localhost' IDENTIFIED BY 'choose-a-strong-password';
GRANT ALL PRIVILEGES ON library_management.* TO 'library_app'@'localhost';
```

The application connects by default to `jdbc:mysql://localhost:3306/library_management` as `library_app`. Set the password in your environment; do not put real credentials in source code.

### Windows PowerShell

Set these for the current terminal session before running the app:

```powershell
$env:LIBRARY_DB_USER = "library_app"
$env:LIBRARY_DB_PASSWORD = "your-mysql-password"
```

For a different host or database, optionally set the complete JDBC URL:

```powershell
$env:LIBRARY_DB_URL = "jdbc:mysql://localhost:3306/library_management?serverTimezone=UTC"
```

### IntelliJ IDEA

1. Open the project and accept the prompt to load it as a Maven project.
2. Set the Project SDK to JDK 17 or newer (JDK 24 is configured in this project).
3. Open **Run → Edit Configurations**, select the `com.library.Main` application configuration (or create one), and set its **Environment variables** to `LIBRARY_DB_USER=library_app;LIBRARY_DB_PASSWORD=your-mysql-password`.
4. Run `com.library.Main`.

The application creates the `books`, `members`, and `transactions` tables on first start. If MySQL is not running, the database does not exist, or credentials are incorrect, it displays a database error.
Existing SQLite data is not imported; this creates empty MySQL tables.

## Build and run from PowerShell

From the project root, set the database environment variables as described above, then run:

```powershell
mvn clean compile
mvn exec:java
```

## Database configuration

The app reads these optional Java system properties first, then environment variables:

| Java system property | Environment variable | Default |
|---|---|---|
| `library.db.url` | `LIBRARY_DB_URL` | `jdbc:mysql://localhost:3306/library_management?serverTimezone=UTC` |
| `library.db.user` | `LIBRARY_DB_USER` | `library_app` |
| `library.db.password` | `LIBRARY_DB_PASSWORD` | Empty |

For MySQL on another host, create a MySQL user with permission to access the database from the app host and configure the JDBC URL and credentials accordingly.
