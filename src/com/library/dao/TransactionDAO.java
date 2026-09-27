package com.library.dao;

import com.library.db.DBConnection;
import com.library.model.Transaction;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    private static final int LOAN_DAYS = 14;

    /** Issues a book to a member: creates the transaction and decrements available copies. */
    public void issueBook(int bookId, int memberId) throws SQLException {
        Connection conn = DBConnection.getConnection();
        try {
            conn.setAutoCommit(false);

            String checkSql = "SELECT available_copies FROM books WHERE id=?";
            int available = -1;
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, bookId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) available = rs.getInt("available_copies");
                }
            }
            if (available <= 0) {
                conn.rollback();
                throw new SQLException("No available copies of this book to issue.");
            }

            LocalDate today = LocalDate.now();
            LocalDate due = today.plusDays(LOAN_DAYS);

            String insertSql = "INSERT INTO transactions (book_id, member_id, issue_date, due_date, return_date, status) " +
                    "VALUES (?,?,?,?,NULL,'ISSUED')";
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setInt(1, bookId);
                ps.setInt(2, memberId);
                ps.setString(3, today.toString());
                ps.setString(4, due.toString());
                ps.executeUpdate();
            }

            String updateSql = "UPDATE books SET available_copies = available_copies - 1 WHERE id=?";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setInt(1, bookId);
                ps.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    /** Marks a transaction as returned and increments the book's available copies. */
    public void returnBook(int transactionId) throws SQLException {
        Connection conn = DBConnection.getConnection();
        try {
            conn.setAutoCommit(false);

            int bookId = -1;
            String status = null;
            String findSql = "SELECT book_id, status FROM transactions WHERE id=?";
            try (PreparedStatement ps = conn.prepareStatement(findSql)) {
                ps.setInt(1, transactionId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        bookId = rs.getInt("book_id");
                        status = rs.getString("status");
                    }
                }
            }
            if (bookId == -1) {
                conn.rollback();
                throw new SQLException("Transaction not found.");
            }
            if ("RETURNED".equals(status)) {
                conn.rollback();
                throw new SQLException("This book has already been returned.");
            }

            String updateTxSql = "UPDATE transactions SET status='RETURNED', return_date=? WHERE id=?";
            try (PreparedStatement ps = conn.prepareStatement(updateTxSql)) {
                ps.setString(1, LocalDate.now().toString());
                ps.setInt(2, transactionId);
                ps.executeUpdate();
            }

            String updateBookSql = "UPDATE books SET available_copies = available_copies + 1 WHERE id=?";
            try (PreparedStatement ps = conn.prepareStatement(updateBookSql)) {
                ps.setInt(1, bookId);
                ps.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public List<Transaction> getAllTransactions() throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.id, t.book_id, b.title AS book_title, t.member_id, m.name AS member_name, " +
                "t.issue_date, t.due_date, t.return_date, t.status " +
                "FROM transactions t " +
                "JOIN books b ON t.book_id = b.id " +
                "JOIN members m ON t.member_id = m.id " +
                "ORDER BY t.id DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Transaction(
                        rs.getInt("id"),
                        rs.getInt("book_id"),
                        rs.getString("book_title"),
                        rs.getInt("member_id"),
                        rs.getString("member_name"),
                        rs.getString("issue_date"),
                        rs.getString("due_date"),
                        rs.getString("return_date"),
                        rs.getString("status")
                ));
            }
        }
        return list;
    }

    public List<Transaction> getActiveIssues() throws SQLException {
        List<Transaction> all = getAllTransactions();
        all.removeIf(t -> !"ISSUED".equals(t.getStatus()));
        return all;
    }
}
