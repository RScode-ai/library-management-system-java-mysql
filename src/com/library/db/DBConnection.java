package com.library.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Handles the single MySQL connection used by the whole application and
 * makes sure the required tables exist before the UI starts.
 */
public class DBConnection {

    private static final String DB_URL = setting(
            "library.db.url",
            "LIBRARY_DB_URL",
            "jdbc:mysql://localhost:3306/library_management?serverTimezone=UTC");
    private static final String DB_USER = setting("library.db.user", "LIBRARY_DB_USER", "library_app");
    private static final String DB_PASSWORD = setting("library.db.password", "LIBRARY_DB_PASSWORD", "");
    private static Connection connection;

    private DBConnection() {}

    private static String setting(String property, String environmentVariable, String defaultValue) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank()) {
            value = System.getenv(environmentVariable);
        }
        return value == null || value.isBlank() ? defaultValue : value;
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        }
        return connection;
    }

    public static void initializeDatabase() {
        String books = "CREATE TABLE IF NOT EXISTS books (" +
                "id INT PRIMARY KEY AUTO_INCREMENT," +
                "title VARCHAR(255) NOT NULL," +
                "author VARCHAR(255) NOT NULL," +
                "isbn VARCHAR(32)," +
                "total_copies INT NOT NULL," +
                "available_copies INT NOT NULL) ENGINE=InnoDB";

        String members = "CREATE TABLE IF NOT EXISTS members (" +
                "id INT PRIMARY KEY AUTO_INCREMENT," +
                "name VARCHAR(255) NOT NULL," +
                "email VARCHAR(255)," +
                "phone VARCHAR(64)) ENGINE=InnoDB";

        String transactions = "CREATE TABLE IF NOT EXISTS transactions (" +
                "id INT PRIMARY KEY AUTO_INCREMENT," +
                "book_id INT NOT NULL," +
                "member_id INT NOT NULL," +
                "issue_date DATE NOT NULL," +
                "due_date DATE NOT NULL," +
                "return_date DATE," +
                "status VARCHAR(16) NOT NULL," +
                "FOREIGN KEY(book_id) REFERENCES books(id)," +
                "FOREIGN KEY(member_id) REFERENCES members(id)) ENGINE=InnoDB";

        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            st.execute(books);
            st.execute(members);
            st.execute(transactions);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database: " + e.getMessage(), e);
        }
    }
}
