package com.library;

import com.library.db.DBConnection;
import com.library.ui.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Make sure the MySQL JDBC driver is available before connecting.
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null,
                    "MySQL JDBC driver not found.\n" +
                            "Reload the Maven project so mysql-connector-j is added to the classpath.\n" +
                            "See README.md for setup instructions.",
                    "Missing Driver", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            DBConnection.initializeDatabase();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(null,
                    "Could not set up the database:\n" + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fall back to the default look and feel if the system one isn't available.
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
