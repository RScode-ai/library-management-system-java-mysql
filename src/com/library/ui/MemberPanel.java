package com.library.ui;

import com.library.dao.MemberDAO;
import com.library.model.Member;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class MemberPanel extends JPanel {

    private final MemberDAO memberDAO = new MemberDAO();

    private final JTextField nameField = new JTextField(15);
    private final JTextField emailField = new JTextField(15);
    private final JTextField phoneField = new JTextField(15);

    private final DefaultTableModel tableModel;
    private final JTable table;

    private Integer selectedMemberId = null;

    public MemberPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildFormPanel(), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Email", "Phone"}, 0) {
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

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; panel.add(nameField, gbc);
        gbc.gridx = 2; panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 3; panel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1; panel.add(phoneField, gbc);

        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");
        JButton clearBtn = new JButton("Clear");

        addBtn.addActionListener(e -> addMember());
        updateBtn.addActionListener(e -> updateMember());
        deleteBtn.addActionListener(e -> deleteMember());
        clearBtn.addActionListener(e -> clearForm());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(clearBtn);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4;
        panel.add(btnPanel, gbc);

        return panel;
    }

    private void loadSelectedRow() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return;
        selectedMemberId = (Integer) tableModel.getValueAt(viewRow, 0);
        nameField.setText(tableModel.getValueAt(viewRow, 1).toString());
        emailField.setText(String.valueOf(tableModel.getValueAt(viewRow, 2)));
        phoneField.setText(String.valueOf(tableModel.getValueAt(viewRow, 3)));
    }

    private void clearForm() {
        selectedMemberId = null;
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        table.clearSelection();
    }

    private void addMember() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showError("Name is required.");
            return;
        }
        try {
            memberDAO.addMember(new Member(name, emailField.getText().trim(), phoneField.getText().trim()));
            clearForm();
            refreshTable();
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    private void updateMember() {
        if (selectedMemberId == null) {
            showError("Select a member from the table first.");
            return;
        }
        try {
            Member member = new Member(selectedMemberId, nameField.getText().trim(),
                    emailField.getText().trim(), phoneField.getText().trim());
            memberDAO.updateMember(member);
            clearForm();
            refreshTable();
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    private void deleteMember() {
        if (selectedMemberId == null) {
            showError("Select a member from the table first.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this member?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            memberDAO.deleteMember(selectedMemberId);
            clearForm();
            refreshTable();
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    public void refreshTable() {
        try {
            List<Member> members = memberDAO.getAllMembers();
            tableModel.setRowCount(0);
            for (Member m : members) {
                tableModel.addRow(new Object[]{m.getId(), m.getName(), m.getEmail(), m.getPhone()});
            }
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
