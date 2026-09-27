package com.library.ui;

import com.library.dao.BookDAO;
import com.library.model.Book;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class BookPanel extends JPanel {

    private final BookDAO bookDAO = new BookDAO();

    private final JTextField titleField = new JTextField(15);
    private final JTextField authorField = new JTextField(15);
    private final JTextField isbnField = new JTextField(15);
    private final JTextField copiesField = new JTextField(5);
    private final JTextField searchField = new JTextField(15);

    private final DefaultTableModel tableModel;
    private final JTable table;

    private Integer selectedBookId = null;

    public BookPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildFormPanel(), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Title", "Author", "ISBN", "Total", "Available"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.getSelectionModel().addListSelectionListener(e -> loadSelectedRow());
        add(new JScrollPane(table), BorderLayout.CENTER);

        refreshTable();
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; panel.add(titleField, gbc);
        gbc.gridx = 2; panel.add(new JLabel("Author:"), gbc);
        gbc.gridx = 3; panel.add(authorField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("ISBN:"), gbc);
        gbc.gridx = 1; panel.add(isbnField, gbc);
        gbc.gridx = 2; panel.add(new JLabel("Total Copies:"), gbc);
        gbc.gridx = 3; panel.add(copiesField, gbc);

        row++;
        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");
        JButton clearBtn = new JButton("Clear");

        addBtn.addActionListener(e -> addBook());
        updateBtn.addActionListener(e -> updateBook());
        deleteBtn.addActionListener(e -> deleteBook());
        clearBtn.addActionListener(e -> clearForm());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(clearBtn);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 4;
        panel.add(btnPanel, gbc);
        gbc.gridwidth = 1;

        row++;
        JButton searchBtn = new JButton("Search");
        JButton resetBtn = new JButton("Show All");
        searchBtn.addActionListener(e -> searchBooks());
        resetBtn.addActionListener(e -> refreshTable());

        gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Search:"), gbc);
        gbc.gridx = 1; panel.add(searchField, gbc);
        gbc.gridx = 2; panel.add(searchBtn, gbc);
        gbc.gridx = 3; panel.add(resetBtn, gbc);

        return panel;
    }

    private void loadSelectedRow() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return;
        selectedBookId = (Integer) tableModel.getValueAt(viewRow, 0);
        titleField.setText(tableModel.getValueAt(viewRow, 1).toString());
        authorField.setText(tableModel.getValueAt(viewRow, 2).toString());
        isbnField.setText(String.valueOf(tableModel.getValueAt(viewRow, 3)));
        copiesField.setText(tableModel.getValueAt(viewRow, 4).toString());
    }

    private void clearForm() {
        selectedBookId = null;
        titleField.setText("");
        authorField.setText("");
        isbnField.setText("");
        copiesField.setText("");
        table.clearSelection();
    }

    private void addBook() {
        try {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String isbn = isbnField.getText().trim();
            int copies = Integer.parseInt(copiesField.getText().trim());

            if (title.isEmpty() || author.isEmpty()) {
                showError("Title and Author are required.");
                return;
            }

            bookDAO.addBook(new Book(title, author, isbn, copies));
            clearForm();
            refreshTable();
        } catch (NumberFormatException ex) {
            showError("Total Copies must be a number.");
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    private void updateBook() {
        if (selectedBookId == null) {
            showError("Select a book from the table first.");
            return;
        }
        try {
            Book book = bookDAO.getBookById(selectedBookId);
            if (book == null) {
                showError("Book no longer exists.");
                return;
            }
            int newTotal = Integer.parseInt(copiesField.getText().trim());
            int diff = newTotal - book.getTotalCopies();

            book.setTitle(titleField.getText().trim());
            book.setAuthor(authorField.getText().trim());
            book.setIsbn(isbnField.getText().trim());
            book.setTotalCopies(newTotal);
            book.setAvailableCopies(Math.max(0, book.getAvailableCopies() + diff));

            bookDAO.updateBook(book);
            clearForm();
            refreshTable();
        } catch (NumberFormatException ex) {
            showError("Total Copies must be a number.");
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    private void deleteBook() {
        if (selectedBookId == null) {
            showError("Select a book from the table first.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this book?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            bookDAO.deleteBook(selectedBookId);
            clearForm();
            refreshTable();
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    private void searchBooks() {
        try {
            String keyword = searchField.getText().trim();
            List<Book> books = keyword.isEmpty() ? bookDAO.getAllBooks() : bookDAO.searchBooks(keyword);
            populateTable(books);
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    public void refreshTable() {
        try {
            populateTable(bookDAO.getAllBooks());
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    private void populateTable(List<Book> books) {
        tableModel.setRowCount(0);
        for (Book b : books) {
            tableModel.addRow(new Object[]{
                    b.getId(), b.getTitle(), b.getAuthor(), b.getIsbn(), b.getTotalCopies(), b.getAvailableCopies()
            });
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
