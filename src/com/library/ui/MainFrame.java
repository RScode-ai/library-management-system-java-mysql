package com.library.ui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private final BookPanel bookPanel = new BookPanel();
    private final MemberPanel memberPanel = new MemberPanel();
    private final IssueReturnPanel issueReturnPanel = new IssueReturnPanel();

    public MainFrame() {
        setTitle("Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Books", bookPanel);
        tabs.addTab("Members", memberPanel);
        tabs.addTab("Issue / Return", issueReturnPanel);

        // Keep dropdowns and tables in sync whenever the user switches tabs,
        // since data changed in one tab (e.g. a new book) affects another.
        tabs.addChangeListener(e -> {
            int index = tabs.getSelectedIndex();
            if (index == 0) bookPanel.refreshTable();
            else if (index == 1) memberPanel.refreshTable();
            else if (index == 2) issueReturnPanel.refreshAll();
        });

        add(tabs, BorderLayout.CENTER);
    }
}
