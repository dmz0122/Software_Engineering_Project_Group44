package com.group44.tarecruit.ui;

import com.group44.tarecruit.model.UserAccount;
import com.group44.tarecruit.ui.components.Theme;
import com.group44.tarecruit.ui.components.UiFactory;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;

public class OrganiserWorkspacePanel extends JPanel {
    private static final String APPLICANTS_PAGE = "applicants";
    private static final String POST_JOB_PAGE = "postJob";

    private final Runnable logoutAction;
    private final CardLayout pageLayout;
    private final JPanel pagePanel;

    private UserAccount currentUser;

    public OrganiserWorkspacePanel(Runnable logoutAction) {
        this.logoutAction = logoutAction;

        setLayout(new BorderLayout());
        setBackground(Theme.APP_BACKGROUND);
        add(buildSidebar(), BorderLayout.WEST);

        pageLayout = new CardLayout();
        pagePanel = new JPanel(pageLayout);
        pagePanel.setOpaque(false);
        pagePanel.add(UiFactory.scrollPane(buildApplicantsPlaceholder()), APPLICANTS_PAGE);
        pagePanel.add(UiFactory.scrollPane(buildPostJobPlaceholder()), POST_JOB_PAGE);
        add(pagePanel, BorderLayout.CENTER);
    }

    public void setCurrentUser(UserAccount currentUser) {
        this.currentUser = currentUser;
    }

    public void refreshAll() {
        showPage(APPLICANTS_PAGE);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Theme.SURFACE);
        sidebar.setBorder(BorderFactory.createEmptyBorder(26, 24, 26, 24));
        sidebar.setPreferredSize(new Dimension(250, 800));

        sidebar.add(UiFactory.sectionLabel("TA Recruit"));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(UiFactory.mutedLabel("Module organiser workspace"));
        sidebar.add(Box.createVerticalStrut(28));

        JButton applicantsButton = UiFactory.navButton("Applications");
        applicantsButton.addActionListener(event -> showPage(APPLICANTS_PAGE));
        JButton postJobButton = UiFactory.navButton("Post Job");
        postJobButton.addActionListener(event -> showPage(POST_JOB_PAGE));

        sidebar.add(applicantsButton);
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(postJobButton);
        sidebar.add(Box.createVerticalGlue());

        JButton logoutButton = UiFactory.lightButton("Sign out");
        logoutButton.addActionListener(event -> logoutAction.run());
        sidebar.add(logoutButton);
        return sidebar;
    }

    private JPanel buildApplicantsPlaceholder() {
        JPanel page = pageWrapper();
        page.add(UiFactory.titleLabel("Review Applicants (MO)"));
        page.add(Box.createVerticalStrut(8));
        page.add(UiFactory.mutedLabel("Applicant review actions are being wired to the CSV-backed services in this branch."));
        page.add(Box.createVerticalStrut(24));
        JPanel card = UiFactory.card();
        JLabel message = UiFactory.bodyLabel("<html>The organiser workspace shell is ready. The next commit will populate job-specific applicant cards and CV actions.</html>");
        card.add(message, BorderLayout.CENTER);
        page.add(card);
        return page;
    }

    private JPanel buildPostJobPlaceholder() {
        JPanel page = pageWrapper();
        page.add(UiFactory.titleLabel("Post Job (Sprint 2)"));
        page.add(Box.createVerticalStrut(8));
        page.add(UiFactory.mutedLabel("Publishing new vacancies is still planned for Sprint 2."));
        return page;
    }

    private JPanel pageWrapper() {
        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));
        return page;
    }

    private void showPage(String page) {
        pageLayout.show(pagePanel, page);
    }
}
