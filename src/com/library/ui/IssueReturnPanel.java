package com.library.ui;

import com.library.dao.BookDAO;
import com.library.dao.MemberDAO;
import com.library.dao.TransactionDAO;
import com.library.model.Book;
import com.library.model.Member;
import com.library.model.Transaction;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class IssueReturnPanel extends JPanel {

    private final BookDAO bookDAO = new BookDAO();
    private final MemberDAO memberDAO = new MemberDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    private final JComboBox<BookItem> bookCombo = new JComboBox<>();
    private final JComboBox<MemberItem> memberCombo = new JComboBox<>();

    private final DefaultTableModel tableModel;
    private final JTable table;

    public IssueReturnPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildTopPanel(), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[]{"Txn ID", "Book", "Member", "Issue Date", "Due Date", "Return Date", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        refreshAll();
    }

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Book:"), gbc);
        gbc.gridx = 1; panel.add(bookCombo, gbc);
        gbc.gridx = 2; panel.add(new JLabel("Member:"), gbc);
        gbc.gridx = 3; panel.add(memberCombo, gbc);

        JButton issueBtn = new JButton("Issue Book");
        JButton returnBtn = new JButton("Return Selected");
        JButton refreshBtn = new JButton("Refresh");

        issueBtn.addActionListener(e -> issueBook());
        returnBtn.addActionListener(e -> returnBook());
        refreshBtn.addActionListener(e -> refreshAll());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(issueBtn);
        btnPanel.add(returnBtn);
        btnPanel.add(refreshBtn);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 4;
        panel.add(btnPanel, gbc);

        return panel;
    }

    private void issueBook() {
        BookItem book = (BookItem) bookCombo.getSelectedItem();
        MemberItem member = (MemberItem) memberCombo.getSelectedItem();
        if (book == null || member == null) {
            showError("Add at least one book and one member first.");
            return;
        }
        try {
            transactionDAO.issueBook(book.id, member.id);
            refreshAll();
        } catch (SQLException ex) {
            showError(ex.getMessage());
        }
    }

    private void returnBook() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            showError("Select a transaction from the table first.");
            return;
        }
        int txnId = (Integer) tableModel.getValueAt(viewRow, 0);
        String status = tableModel.getValueAt(viewRow, 6).toString();
        if ("RETURNED".equals(status)) {
            showError("This book has already been returned.");
            return;
        }
        try {
            transactionDAO.returnBook(txnId);
            refreshAll();
        } catch (SQLException ex) {
            showError(ex.getMessage());
        }
    }

    public void refreshAll() {
        refreshCombos();
        refreshTable();
    }

    private void refreshCombos() {
        bookCombo.removeAllItems();
        memberCombo.removeAllItems();
        try {
            List<Book> books = bookDAO.getAllBooks();
            for (Book b : books) {
                if (b.getAvailableCopies() > 0) {
                    bookCombo.addItem(new BookItem(b.getId(), b.getTitle() + " (" + b.getAvailableCopies() + " available)"));
                }
            }
            List<Member> members = memberDAO.getAllMembers();
            for (Member m : members) {
                memberCombo.addItem(new MemberItem(m.getId(), m.getName()));
            }
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    private void refreshTable() {
        try {
            List<Transaction> transactions = transactionDAO.getAllTransactions();
            tableModel.setRowCount(0);
            for (Transaction t : transactions) {
                tableModel.addRow(new Object[]{
                        t.getId(), t.getBookTitle(), t.getMemberName(),
                        t.getIssueDate(), t.getDueDate(),
                        t.getReturnDate() == null ? "-" : t.getReturnDate(),
                        t.getStatus()
                });
            }
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static class BookItem {
        final int id;
        final String label;
        BookItem(int id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; }
    }

    private static class MemberItem {
        final int id;
        final String label;
        MemberItem(int id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; }
    }
}
