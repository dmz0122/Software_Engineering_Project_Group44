package com.group44.tarecruit.ui;

import com.group44.tarecruit.model.ApplicantProfile;
import com.group44.tarecruit.model.JobPosting;
import com.group44.tarecruit.model.UserAccount;
import com.group44.tarecruit.service.ApplicationService;
import com.group44.tarecruit.service.CvService;
import com.group44.tarecruit.service.JobService;
import com.group44.tarecruit.ui.components.Theme;
import com.group44.tarecruit.ui.components.UiFactory;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.nio.file.Path;
import java.util.List;

public class OrganiserWorkspacePanel extends JPanel {
    private static final String APPLICANTS_PAGE = "applicants";
    private static final String POST_JOB_PAGE = "postJob";

    private final ApplicationService applicationService;
    private final JobService jobService;
    private final CvService cvService;
    private final Runnable logoutAction;

    private final CardLayout pageLayout;
    private final JPanel pagePanel;
    private final JComboBox<JobPosting> jobSelector;
    private final JPanel applicantListPanel;

    private UserAccount currentUser;

    public OrganiserWorkspacePanel(
            ApplicationService applicationService,
            JobService jobService,
            CvService cvService,
            Runnable logoutAction
    ) {
        this.applicationService = applicationService;
        this.jobService = jobService;
        this.cvService = cvService;
        this.logoutAction = logoutAction;

        setLayout(new BorderLayout());
        setBackground(Theme.APP_BACKGROUND);
        add(buildSidebar(), BorderLayout.WEST);

        jobSelector = new JComboBox<>();
        jobSelector.setFont(Theme.BODY_FONT);
        applicantListPanel = new JPanel();
        applicantListPanel.setLayout(new BoxLayout(applicantListPanel, BoxLayout.Y_AXIS));
        applicantListPanel.setOpaque(false);

        pageLayout = new CardLayout();
        pagePanel = new JPanel(pageLayout);
        pagePanel.setOpaque(false);
        pagePanel.add(UiFactory.scrollPane(buildApplicantsPage()), APPLICANTS_PAGE);
        pagePanel.add(UiFactory.scrollPane(buildPostJobPlaceholder()), POST_JOB_PAGE);
        add(pagePanel, BorderLayout.CENTER);
    }

    public void setCurrentUser(UserAccount currentUser) {
        this.currentUser = currentUser;
    }

    public void refreshAll() {
        jobSelector.removeAllItems();
        for (JobPosting job : jobService.getAllJobs()) {
            jobSelector.addItem(job);
        }
        if (jobSelector.getItemCount() > 0) {
            jobSelector.setSelectedIndex(0);
        }
        refreshApplicantList();
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
        JLabel userHint = UiFactory.mutedLabel("Module organiser workspace");
        sidebar.add(userHint);
        sidebar.add(Box.createVerticalStrut(28));

        JButton postJobButton = UiFactory.navButton("Post Job");
        postJobButton.addActionListener(event -> showPage(POST_JOB_PAGE));
        JButton applicantsButton = UiFactory.navButton("Applications");
        applicantsButton.addActionListener(event -> showPage(APPLICANTS_PAGE));

        sidebar.add(postJobButton);
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(applicantsButton);
        sidebar.add(Box.createVerticalGlue());

        JButton logoutButton = UiFactory.lightButton("Sign out");
        logoutButton.addActionListener(event -> logoutAction.run());
        sidebar.add(logoutButton);
        return sidebar;
    }

    private JPanel buildApplicantsPage() {
        JPanel page = pageWrapper();
        page.add(UiFactory.titleLabel("Review Applicants (MO)"));
        page.add(Box.createVerticalStrut(8));
        page.add(UiFactory.mutedLabel("Open one vacancy at a time, access applicant CVs and update the selection decision."));
        page.add(Box.createVerticalStrut(24));

        JPanel controls = UiFactory.card();
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        row.setOpaque(false);
        row.add(UiFactory.bodyLabel("Vacancy"));
        jobSelector.addActionListener(event -> refreshApplicantList());
        jobSelector.setRenderer((list, value, index, isSelected, cellHasFocus) ->
                new JLabel(value == null ? "" : value.title() + " — " + value.moduleCode()));
        row.add(jobSelector);
        controls.add(row, BorderLayout.CENTER);
        page.add(controls);
        page.add(Box.createVerticalStrut(16));
        page.add(applicantListPanel);
        return page;
    }

    private JPanel buildPostJobPlaceholder() {
        JPanel page = pageWrapper();
        page.add(UiFactory.titleLabel("Post Job (Sprint 2)"));
        page.add(Box.createVerticalStrut(8));
        page.add(UiFactory.mutedLabel("This page is kept in the navigation to match the prototype, but vacancy publishing is scheduled for Sprint 2."));
        page.add(Box.createVerticalStrut(24));
        JPanel card = UiFactory.card();
        JLabel message = UiFactory.bodyLabel("<html>Use the Applicants page for Sprint 1 review work. Seeded vacancies are already loaded from CSV so you can test browsing, applicant review and selection end to end.</html>");
        card.add(message, BorderLayout.CENTER);
        page.add(card);
        return page;
    }

    private JPanel pageWrapper() {
        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));
        return page;
    }

    private void refreshApplicantList() {
        applicantListPanel.removeAll();
        JobPosting selectedJob = (JobPosting) jobSelector.getSelectedItem();
        if (selectedJob == null) {
            applicantListPanel.add(UiFactory.mutedLabel("No vacancies are available."));
        } else {
            List<ApplicationService.ApplicantReviewItem> items = applicationService.findApplicantsForJob(selectedJob.id());
            if (items.isEmpty()) {
                applicantListPanel.add(UiFactory.mutedLabel("No applicants yet for this vacancy."));
            } else {
                for (int index = 0; index < items.size(); index++) {
                    applicantListPanel.add(applicantCard(selectedJob, items.get(index)));
                    if (index < items.size() - 1) {
                        applicantListPanel.add(Box.createVerticalStrut(12));
                    }
                }
            }
        }
        applicantListPanel.revalidate();
        applicantListPanel.repaint();
    }

    private Component applicantCard(JobPosting job, ApplicationService.ApplicantReviewItem item) {
        JPanel card = UiFactory.card();
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 210));
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = UiFactory.bodyLabel(item.applicant().displayName() + " — " + item.application().status().label());
        title.setFont(Theme.BUTTON_FONT);
        content.add(title);
        content.add(Box.createVerticalStrut(6));

        ApplicantProfile profile = item.profile();
        content.add(UiFactory.mutedLabel(profile.studentId().isBlank()
                ? "Profile not completed yet."
                : profile.studentId() + " • " + profile.programme() + " • " + profile.year()));
        content.add(Box.createVerticalStrut(10));
        content.add(UiFactory.bodyLabel("Skills: " + (profile.skills().isBlank() ? "No skills provided" : profile.skills())));
        content.add(Box.createVerticalStrut(8));
        content.add(UiFactory.bodyLabel("Availability: " + (profile.availability().isBlank() ? "Not provided" : profile.availability())));
        content.add(Box.createVerticalStrut(8));
        content.add(UiFactory.mutedLabel("Fit preview: " + item.fitScore() + "% match"));
        if (!item.application().note().isBlank()) {
            content.add(Box.createVerticalStrut(8));
            content.add(UiFactory.mutedLabel("Notes: " + item.application().note()));
        }
        content.add(Box.createVerticalStrut(16));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        JButton openCvButton = UiFactory.lightButton("Open CV");
        openCvButton.setEnabled(!profile.cvStoredPath().isBlank());
        openCvButton.addActionListener(event -> openCv(profile.cvStoredPath()));
        JButton downloadCvButton = UiFactory.lightButton("Download CV");
        downloadCvButton.setEnabled(!profile.cvStoredPath().isBlank());
        downloadCvButton.addActionListener(event -> downloadCv(profile.cvStoredPath(), profile.cvOriginalFileName()));
        JButton selectButton = UiFactory.primaryButton("Select");
        selectButton.setEnabled(!"Selected".equalsIgnoreCase(item.application().status().label()));
        selectButton.addActionListener(event -> selectApplicant(job, item.application().id()));

        actions.add(openCvButton);
        actions.add(downloadCvButton);
        actions.add(selectButton);
        content.add(actions);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private void openCv(String storedPath) {
        try {
            cvService.openCv(storedPath);
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage());
        }
    }

    private void downloadCv(String storedPath, String originalFileName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File(originalFileName.isBlank() ? "cv.txt" : originalFileName));
        int selection = chooser.showSaveDialog(this);
        if (selection != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            cvService.exportCv(storedPath, Path.of(chooser.getSelectedFile().getAbsolutePath()));
            JOptionPane.showMessageDialog(this, "CV downloaded successfully.");
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage());
        }
    }

    private void selectApplicant(JobPosting job, String applicationId) {
        try {
            applicationService.selectApplicant(applicationId);
            refreshApplicantList();
            JOptionPane.showMessageDialog(this, "Applicant selected for " + job.title() + ".");
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage());
        }
    }

    private void showPage(String page) {
        pageLayout.show(pagePanel, page);
    }
}
