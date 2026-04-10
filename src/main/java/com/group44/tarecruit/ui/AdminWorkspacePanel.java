package com.group44.tarecruit.ui;

import com.group44.tarecruit.model.UserAccount;
import com.group44.tarecruit.service.WorkloadService;
import com.group44.tarecruit.ui.components.Theme;
import com.group44.tarecruit.ui.components.UiFactory;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;

public class AdminWorkspacePanel extends JPanel {
    private final WorkloadService workloadService;
    private final Runnable logoutAction;

    private final JComboBox<String> semesterFilterBox;
    private final JPanel summaryListPanel;
    private final JLabel detailHeaderLabel;
    private final JLabel detailMetaLabel;
    private final JTextArea detailArea;

    private UserAccount currentUser;

    public AdminWorkspacePanel(WorkloadService workloadService, Runnable logoutAction) {
        this.workloadService = workloadService;
        this.logoutAction = logoutAction;

        setLayout(new BorderLayout());
        setBackground(Theme.APP_BACKGROUND);
        add(buildSidebar(), BorderLayout.WEST);

        semesterFilterBox = new JComboBox<>();
        semesterFilterBox.setFont(Theme.BODY_FONT);
        semesterFilterBox.addActionListener(event -> refreshWorkload());

        summaryListPanel = new JPanel();
        summaryListPanel.setOpaque(false);
        summaryListPanel.setLayout(new BoxLayout(summaryListPanel, BoxLayout.Y_AXIS));

        detailHeaderLabel = UiFactory.sectionLabel("Workload details");
        detailMetaLabel = UiFactory.mutedLabel("Select a summary to inspect selected TA assignments.");
        detailArea = UiFactory.textArea(12);
        detailArea.setEditable(false);
        detailArea.setBackground(Theme.SURFACE_MUTED);
        detailArea.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        add(UiFactory.scrollPane(buildWorkloadPage()), BorderLayout.CENTER);
    }

    public void setCurrentUser(UserAccount currentUser) {
        this.currentUser = currentUser;
    }

    public void refreshAll() {
        reloadSemesters();
        refreshWorkload();
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Theme.SURFACE);
        sidebar.setBorder(BorderFactory.createEmptyBorder(26, 24, 26, 24));
        sidebar.setPreferredSize(new Dimension(250, 800));

        sidebar.add(UiFactory.sectionLabel("TA Recruit"));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(UiFactory.mutedLabel("Admin workload monitor"));
        sidebar.add(Box.createVerticalStrut(28));
        sidebar.add(UiFactory.navButton("Workload"));
        sidebar.add(Box.createVerticalGlue());

        JButton logoutButton = UiFactory.lightButton("Sign out");
        logoutButton.addActionListener(event -> logoutAction.run());
        sidebar.add(logoutButton);
        return sidebar;
    }

    private JPanel buildWorkloadPage() {
        JPanel page = pageWrapper();
        page.add(UiFactory.titleLabel("Admin Workload"));
        page.add(Box.createVerticalStrut(8));
        page.add(UiFactory.mutedLabel(currentUser == null
                ? "Monitor selected TA workload by semester and inspect assignment details."
                : "Signed in as " + currentUser.displayName() + ". Monitor selected TA workload by semester."));
        page.add(Box.createVerticalStrut(24));

        JPanel filterCard = UiFactory.card();
        JPanel filterRow = UiFactory.flowPanel(java.awt.FlowLayout.LEFT, 12, 0);
        filterRow.add(UiFactory.bodyLabel("Semester"));
        filterRow.add(semesterFilterBox);
        JButton refreshButton = UiFactory.lightButton("Refresh");
        refreshButton.addActionListener(event -> refreshWorkload());
        filterRow.add(refreshButton);
        filterCard.add(filterRow, BorderLayout.CENTER);
        page.add(filterCard);
        page.add(Box.createVerticalStrut(20));

        JPanel contentGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        contentGrid.setOpaque(false);

        JPanel summaryCard = UiFactory.card();
        JPanel summaryBody = new JPanel();
        summaryBody.setOpaque(false);
        summaryBody.setLayout(new BoxLayout(summaryBody, BoxLayout.Y_AXIS));
        summaryBody.add(UiFactory.sectionLabel("Selected TA load"));
        summaryBody.add(Box.createVerticalStrut(16));
        summaryBody.add(summaryListPanel);
        summaryCard.add(summaryBody, BorderLayout.CENTER);

        JPanel detailCard = UiFactory.card();
        JPanel detailBody = new JPanel();
        detailBody.setOpaque(false);
        detailBody.setLayout(new BoxLayout(detailBody, BoxLayout.Y_AXIS));
        detailBody.add(detailHeaderLabel);
        detailBody.add(Box.createVerticalStrut(8));
        detailBody.add(detailMetaLabel);
        detailBody.add(Box.createVerticalStrut(16));
        detailBody.add(detailArea);
        detailCard.add(detailBody, BorderLayout.CENTER);

        contentGrid.add(summaryCard);
        contentGrid.add(detailCard);
        page.add(contentGrid);
        return page;
    }

    private JPanel pageWrapper() {
        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));
        return page;
    }

    private void reloadSemesters() {
        Object currentSelection = semesterFilterBox.getSelectedItem();
        semesterFilterBox.removeAllItems();
        semesterFilterBox.addItem(WorkloadService.CURRENT_SEMESTER_FILTER);
        semesterFilterBox.addItem(WorkloadService.ALL_SEMESTERS_FILTER);
        for (String semester : workloadService.availableSemesters()) {
            if (!WorkloadService.CURRENT_SEMESTER_FILTER.equalsIgnoreCase(semester)
                    && !WorkloadService.ALL_SEMESTERS_FILTER.equalsIgnoreCase(semester)) {
                semesterFilterBox.addItem(semester);
            }
        }
        if (currentSelection != null) {
            semesterFilterBox.setSelectedItem(currentSelection);
        }
        if (semesterFilterBox.getSelectedItem() == null && semesterFilterBox.getItemCount() > 0) {
            semesterFilterBox.setSelectedIndex(0);
        }
    }

    private void refreshWorkload() {
        String filter = (String) semesterFilterBox.getSelectedItem();
        List<WorkloadService.WorkloadSummary> summaries = workloadService.getWorkload(filter);

        summaryListPanel.removeAll();
        if (summaries.isEmpty()) {
            summaryListPanel.add(UiFactory.mutedLabel("No selected TA assignments are available for this view."));
            showSummaryDetail(null);
        } else {
            for (int index = 0; index < summaries.size(); index++) {
                summaryListPanel.add(summaryCard(summaries.get(index)));
                if (index < summaries.size() - 1) {
                    summaryListPanel.add(Box.createVerticalStrut(12));
                }
            }
            showSummaryDetail(summaries.getFirst());
        }
        summaryListPanel.revalidate();
        summaryListPanel.repaint();
    }

    private JPanel summaryCard(WorkloadService.WorkloadSummary summary) {
        JPanel card = UiFactory.card();
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JLabel nameLabel = UiFactory.bodyLabel(summary.summaryLine());
        nameLabel.setFont(Theme.BUTTON_FONT);
        content.add(nameLabel);
        content.add(Box.createVerticalStrut(6));
        content.add(UiFactory.mutedLabel(summary.selectedRoles() + " selected role(s) in " + summary.semester()));
        content.add(Box.createVerticalStrut(14));

        JPanel actions = UiFactory.flowPanel(java.awt.FlowLayout.LEFT, 10, 0);
        JButton viewDetailsButton = UiFactory.primaryButton("View details");
        viewDetailsButton.addActionListener(event -> showSummaryDetail(summary));
        actions.add(viewDetailsButton);
        content.add(actions);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private void showSummaryDetail(WorkloadService.WorkloadSummary summary) {
        if (summary == null) {
            detailHeaderLabel.setText("Workload details");
            detailMetaLabel.setText("Select a summary to inspect selected TA assignments.");
            detailArea.setText("No workload data available.");
            return;
        }

        detailHeaderLabel.setText(summary.displayName());
        detailMetaLabel.setText(summary.totalHoursPerWeek()
                + " hrs/week across "
                + summary.selectedRoles()
                + " selected role(s) for "
                + summary.semester());

        StringBuilder builder = new StringBuilder();
        for (WorkloadService.WorkloadAssignment assignment : summary.assignments()) {
            if (!builder.isEmpty()) {
                builder.append("\n\n");
            }
            builder.append(assignment.jobTitle())
                    .append(" (")
                    .append(assignment.moduleCode())
                    .append(")\n")
                    .append(assignment.moduleName())
                    .append("\n")
                    .append(assignment.hoursPerWeek())
                    .append(" hrs/week • ")
                    .append(assignment.semester());
        }
        detailArea.setText(builder.toString());
        detailArea.setCaretPosition(0);
    }
}
