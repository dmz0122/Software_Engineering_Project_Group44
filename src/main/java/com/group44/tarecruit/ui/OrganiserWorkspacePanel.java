package com.group44.tarecruit.ui;

import com.group44.tarecruit.model.ApplicantProfile;
import com.group44.tarecruit.model.ApplicationStatus;
import com.group44.tarecruit.model.JobPosting;
import com.group44.tarecruit.model.UserAccount;
import com.group44.tarecruit.service.ApplicationService;
import com.group44.tarecruit.service.AnalyticsService;
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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.nio.file.Path;
import java.util.List;

public class OrganiserWorkspacePanel extends JPanel {
    private static final String APPLICANTS_PAGE = "applicants";
    private static final String POST_JOB_PAGE = "postJob";

    private final ApplicationService applicationService;
    private final JobService jobService;
    private final AnalyticsService analyticsService;
    private final CvService cvService;
    private final Runnable accountAction;
    private final Runnable logoutAction;

    private final CardLayout pageLayout;
    private final JPanel pagePanel;
    private final JComboBox<JobPosting> jobSelector;
    private final JComboBox<String> statusFilterBox;
    private final JTextField applicantSearchField;
    private final JPanel applicantListPanel;
    private final JTextField roleField;
    private final JTextField hoursPerWeekField;
    private final JTextField moduleCodeField;
    private final JTextField moduleNameField;
    private final JTextField semesterField;
    private final JTextArea requiredSkillsArea;
    private final JTextArea descriptionArea;
    private final JTextField tagsField;
    private final JTextField openingsField;
    private final JLabel previewTitleLabel;
    private final JLabel previewSummaryLabel;
    private final JTextArea previewRequirementsArea;
    private final JTextArea previewDescriptionArea;

    private UserAccount currentUser;
    private boolean suppressApplicantRefresh;

    public OrganiserWorkspacePanel(
            ApplicationService applicationService,
            JobService jobService,
            AnalyticsService analyticsService,
            CvService cvService,
            Runnable accountAction,
            Runnable logoutAction
    ) {
        this.applicationService = applicationService;
        this.jobService = jobService;
        this.analyticsService = analyticsService;
        this.cvService = cvService;
        this.accountAction = accountAction;
        this.logoutAction = logoutAction;

        setLayout(new BorderLayout());
        setBackground(Theme.APP_BACKGROUND);
        add(buildSidebar(), BorderLayout.WEST);

        jobSelector = new JComboBox<>();
        jobSelector.setFont(Theme.BODY_FONT);
        statusFilterBox = new JComboBox<>();
        statusFilterBox.setFont(Theme.BODY_FONT);
        for (String label : List.of("All statuses", "Applied", "Under Review", "Shortlisted", "Interview", "Selected", "Rejected")) {
            statusFilterBox.addItem(label);
        }
        statusFilterBox.addActionListener(event -> refreshApplicantList());
        applicantSearchField = UiFactory.textField();
        applicantListPanel = new JPanel();
        applicantListPanel.setLayout(new BoxLayout(applicantListPanel, BoxLayout.Y_AXIS));
        applicantListPanel.setOpaque(false);

        roleField = UiFactory.textField();
        hoursPerWeekField = UiFactory.textField();
        moduleCodeField = UiFactory.textField();
        moduleNameField = UiFactory.textField();
        semesterField = UiFactory.textField();
        requiredSkillsArea = UiFactory.textArea(5);
        descriptionArea = UiFactory.textArea(7);
        tagsField = UiFactory.textField();
        openingsField = UiFactory.textField();
        previewTitleLabel = UiFactory.sectionLabel("Vacancy preview");
        previewSummaryLabel = UiFactory.mutedLabel("Enter vacancy details and use Preview to confirm the listing.");
        previewRequirementsArea = readOnlyArea(6);
        previewDescriptionArea = readOnlyArea(8);

        pageLayout = new CardLayout();
        pagePanel = new JPanel(pageLayout);
        pagePanel.setOpaque(false);
        pagePanel.add(UiFactory.scrollPane(buildApplicantsPage()), APPLICANTS_PAGE);
        pagePanel.add(UiFactory.scrollPane(buildPostJobPage()), POST_JOB_PAGE);
        add(pagePanel, BorderLayout.CENTER);

        applicantSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshApplicantList();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshApplicantList();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshApplicantList();
            }
        });
    }

    public void setCurrentUser(UserAccount currentUser) {
        this.currentUser = currentUser;
    }

    public void refreshAll() {
        applicantSearchField.setText("");
        statusFilterBox.setSelectedItem("All statuses");
        refreshJobSelector(null);
        refreshApplicantList();
        showPage(APPLICANTS_PAGE);
        updatePreviewFromForm();
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Theme.SURFACE);
        sidebar.setBorder(BorderFactory.createEmptyBorder(16, 14, 16, 14));
        sidebar.setPreferredSize(new Dimension(176, 620));

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
        sidebar.add(Box.createVerticalStrut(12));
        JButton accountButton = UiFactory.navButton("Account");
        accountButton.addActionListener(event -> accountAction.run());
        sidebar.add(accountButton);
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
        JPanel row = new JPanel(new GridLayout(2, 2, 10, 8));
        row.setOpaque(false);
        JPanel vacancyField = labeledField("Vacancy", jobSelector);
        jobSelector.addActionListener(event -> {
            if (!suppressApplicantRefresh) {
                refreshApplicantList();
            }
        });
        jobSelector.setRenderer((list, value, index, isSelected, cellHasFocus) ->
                new JLabel(value == null ? "" : value.title() + " — " + value.moduleCode()));
        row.add(vacancyField);
        row.add(labeledField("Status", statusFilterBox));
        row.add(labeledField("Search applicants", applicantSearchField));
        JPanel actionField = new JPanel();
        actionField.setOpaque(false);
        actionField.setLayout(new BoxLayout(actionField, BoxLayout.Y_AXIS));
        actionField.add(UiFactory.bodyLabel("Actions"));
        actionField.add(Box.createVerticalStrut(8));
        JButton clearSearchButton = UiFactory.lightButton("Clear search");
        clearSearchButton.addActionListener(event -> applicantSearchField.setText(""));
        actionField.add(clearSearchButton);
        actionField.add(Box.createVerticalStrut(8));
        JButton shortlistOnlyButton = UiFactory.secondaryButton("Shortlist list");
        shortlistOnlyButton.addActionListener(event -> statusFilterBox.setSelectedItem(ApplicationStatus.SHORTLISTED.label()));
        actionField.add(shortlistOnlyButton);
        row.add(actionField);
        controls.add(row, BorderLayout.CENTER);
        page.add(controls);
        page.add(Box.createVerticalStrut(16));
        page.add(applicantListPanel);
        return page;
    }

    private JPanel buildPostJobPage() {
        JPanel page = pageWrapper();
        page.add(UiFactory.titleLabel("Post Job (MO)"));
        page.add(Box.createVerticalStrut(8));
        page.add(UiFactory.mutedLabel("Create a new vacancy with the role, module details, required skills and workload information."));
        page.add(Box.createVerticalStrut(24));

        JPanel grid = new JPanel(new GridLayout(1, 2, 10, 0));
        grid.setOpaque(false);
        grid.add(buildPostJobFormCard());
        grid.add(buildPostJobPreviewCard());
        page.add(grid);
        return page;
    }

    private JPanel pageWrapper() {
        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        return page;
    }

    private void refreshApplicantList() {
        applicantListPanel.removeAll();
        JobPosting selectedJob = (JobPosting) jobSelector.getSelectedItem();
        if (selectedJob == null) {
            applicantListPanel.add(UiFactory.mutedLabel("No vacancies are available."));
        } else {
            ApplicationStatus statusFilter = selectedStatusFilter();
            List<ApplicationService.ApplicantReviewItem> items = applicationService.findApplicantsForJob(selectedJob.id(), applicantSearchField.getText(), statusFilter);
            if (items.isEmpty()) {
                applicantListPanel.add(UiFactory.mutedLabel(applicantSearchField.getText().isBlank()
                        ? (statusFilter == null ? "No applicants yet for this vacancy." : "No applicants match this status.")
                        : "No applicants matched the current search."));
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

    private JPanel buildPostJobFormCard() {
        JPanel card = UiFactory.card();

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JPanel fieldsGrid = new JPanel(new GridLayout(0, 2, 12, 10));
        fieldsGrid.setOpaque(false);
        fieldsGrid.add(labeledField("Role", roleField));
        fieldsGrid.add(labeledField("Hours/week", hoursPerWeekField));
        fieldsGrid.add(labeledField("Module code", moduleCodeField));
        fieldsGrid.add(labeledField("Module/activity", moduleNameField));
        fieldsGrid.add(labeledField("Semester", semesterField));
        fieldsGrid.add(labeledField("Openings", openingsField));
        fieldsGrid.add(labeledField("Tags", tagsField));
        fieldsGrid.add(scrollField("Required skills", requiredSkillsArea));

        content.add(fieldsGrid);
        content.add(Box.createVerticalStrut(16));
        content.add(scrollField("Description", descriptionArea));
        content.add(Box.createVerticalStrut(18));

        JPanel actions = new JPanel(new GridLayout(1, 3, 8, 0));
        actions.setOpaque(false);
        JButton publishButton = UiFactory.primaryButton("Publish Job");
        publishButton.addActionListener(event -> publishJob());
        JButton previewButton = UiFactory.secondaryButton("Preview");
        previewButton.addActionListener(event -> updatePreviewFromForm());
        JButton clearButton = UiFactory.lightButton("Clear");
        clearButton.addActionListener(event -> {
            clearPostJobForm();
            updatePreviewFromForm();
        });
        actions.add(publishButton);
        actions.add(previewButton);
        actions.add(clearButton);
        content.add(actions);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildPostJobPreviewCard() {
        JPanel card = UiFactory.card();
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(previewTitleLabel);
        content.add(Box.createVerticalStrut(8));
        content.add(previewSummaryLabel);
        content.add(Box.createVerticalStrut(16));
        content.add(UiFactory.bodyLabel("Required skills"));
        content.add(Box.createVerticalStrut(8));
        content.add(new JScrollPane(previewRequirementsArea));
        content.add(Box.createVerticalStrut(14));
        content.add(UiFactory.bodyLabel("Description"));
        content.add(Box.createVerticalStrut(8));
        content.add(new JScrollPane(previewDescriptionArea));

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private Component applicantCard(JobPosting job, ApplicationService.ApplicantReviewItem item) {
        JPanel card = UiFactory.card();
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = UiFactory.bodyLabel(item.applicant().displayName());
        title.setFont(Theme.BUTTON_FONT);
        content.add(title);
        content.add(Box.createVerticalStrut(6));
        JPanel metaRow = UiFactory.flowPanel(FlowLayout.LEFT, 10, 0);
        metaRow.add(statusPill(item.application().status()));
        metaRow.add(pillLabel(matchScore(job, item) + "% match", Theme.PRIMARY_DARK, Color.WHITE));
        if (item.application().hasInterviewScheduled()) {
            metaRow.add(pillLabel("Interview " + item.application().interviewAt().replace('T', ' '), Theme.ACCENT, Color.WHITE));
        }
        content.add(metaRow);
        content.add(Box.createVerticalStrut(8));

        ApplicantProfile profile = item.profile();
        content.add(UiFactory.mutedLabel(profile.studentId().isBlank()
                ? "Profile not completed yet."
                : profile.studentId() + " • " + profile.programme() + " • " + profile.year()));
        content.add(Box.createVerticalStrut(10));
        content.add(UiFactory.bodyLabel("Skills: " + (profile.skills().isBlank() ? "No skills provided" : profile.skills())));
        content.add(Box.createVerticalStrut(8));
        content.add(UiFactory.bodyLabel("Availability: " + (profile.availability().isBlank() ? "Not provided" : profile.availability())));
        content.add(Box.createVerticalStrut(8));
        if (!item.application().note().isBlank()) {
            content.add(Box.createVerticalStrut(8));
            content.add(UiFactory.mutedLabel("Notes: " + item.application().note()));
        }
        content.add(Box.createVerticalStrut(16));

        JPanel actions = new JPanel(new GridLayout(2, 3, 8, 8));
        actions.setOpaque(false);
        JButton openCvButton = UiFactory.lightButton("Open CV");
        openCvButton.setEnabled(!profile.cvStoredPath().isBlank());
        openCvButton.addActionListener(event -> openCv(profile.cvStoredPath()));
        JButton downloadCvButton = UiFactory.lightButton("Download CV");
        downloadCvButton.setEnabled(!profile.cvStoredPath().isBlank());
        downloadCvButton.addActionListener(event -> downloadCv(profile.cvStoredPath(), profile.cvOriginalFileName()));
        JButton shortlistButton = UiFactory.secondaryButton("Shortlist");
        shortlistButton.setEnabled(item.application().status() != ApplicationStatus.SHORTLISTED
                && item.application().status() != ApplicationStatus.SELECTED
                && item.application().status() != ApplicationStatus.REJECTED);
        shortlistButton.addActionListener(event -> shortlistApplicant(job, item.application().id()));
        JButton interviewButton = UiFactory.secondaryButton("Schedule Interview");
        interviewButton.setEnabled(item.application().status() != ApplicationStatus.SELECTED
                && item.application().status() != ApplicationStatus.REJECTED);
        interviewButton.addActionListener(event -> scheduleInterview(job, item.application().id()));
        JButton selectButton = UiFactory.primaryButton("Select");
        selectButton.setEnabled(item.application().status() != ApplicationStatus.SELECTED
                && item.application().status() != ApplicationStatus.REJECTED);
        selectButton.addActionListener(event -> selectApplicant(job, item.application().id()));
        JButton rejectButton = UiFactory.lightButton("Reject");
        rejectButton.setEnabled(item.application().status() != ApplicationStatus.REJECTED
                && item.application().status() != ApplicationStatus.SELECTED);
        rejectButton.addActionListener(event -> rejectApplicant(job, item.application().id()));

        actions.add(openCvButton);
        actions.add(downloadCvButton);
        actions.add(shortlistButton);
        actions.add(interviewButton);
        actions.add(selectButton);
        actions.add(rejectButton);
        content.add(actions);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JLabel statusPill(ApplicationStatus status) {
        return switch (status) {
            case APPLIED -> pillLabel(status.label(), new Color(210, 226, 247), Theme.PRIMARY_DARK);
            case UNDER_REVIEW -> pillLabel(status.label(), new Color(230, 236, 249), Theme.PRIMARY_DARK);
            case SHORTLISTED -> pillLabel(status.label(), new Color(235, 246, 214), new Color(67, 109, 21));
            case INTERVIEW_SCHEDULED -> pillLabel(status.label(), new Color(252, 238, 191), new Color(130, 79, 13));
            case SELECTED -> pillLabel(status.label(), new Color(208, 244, 220), new Color(29, 102, 68));
            case REJECTED -> pillLabel(status.label(), new Color(249, 217, 217), new Color(138, 42, 42));
            case WITHDRAWN -> pillLabel(status.label(), new Color(229, 231, 235), Theme.SUBTLE_TEXT);
        };
    }

    private ApplicationStatus selectedStatusFilter() {
        Object item = statusFilterBox.getSelectedItem();
        if (item == null || "All statuses".equalsIgnoreCase(item.toString())) {
            return null;
        }
        return ApplicationStatus.fromLabel(item.toString());
    }

    private int matchScore(JobPosting job, ApplicationService.ApplicantReviewItem item) {
        return analyticsService.getLocalJobMatchInsights(job.semester()).stream()
                .filter(insight -> insight.jobId().equals(job.id()))
                .flatMap(insight -> insight.applicants().stream())
                .filter(applicant -> applicant.applicationId().equals(item.application().id()))
                .mapToInt(AnalyticsService.ApplicantMatchInsight::matchScore)
                .findFirst()
                .orElse(item.fitScore());
    }

    private JLabel pillLabel(String text, Color background, Color foreground) {
        JLabel label = UiFactory.bodyLabel("  " + text + "  ");
        label.setOpaque(true);
        label.setBackground(background);
        label.setForeground(foreground);
        label.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        return label;
    }

    private JPanel labeledField(String labelText, Component component) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(UiFactory.bodyLabel(labelText));
        panel.add(Box.createVerticalStrut(8));
        panel.add(component);
        return panel;
    }

    private JPanel scrollField(String labelText, JTextArea area) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(UiFactory.bodyLabel(labelText));
        panel.add(Box.createVerticalStrut(8));
        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1, true));
        panel.add(scrollPane);
        return panel;
    }

    private JTextArea readOnlyArea(int rows) {
        JTextArea area = UiFactory.textArea(rows);
        area.setEditable(false);
        area.setBackground(Theme.SURFACE_MUTED);
        area.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        return area;
    }

    private void updatePreviewFromForm() {
        String role = roleField.getText().trim();
        String moduleCode = moduleCodeField.getText().trim();
        String moduleName = moduleNameField.getText().trim();
        String semester = semesterField.getText().trim();
        String hours = hoursPerWeekField.getText().trim();
        String openings = openingsField.getText().trim();
        String requiredSkills = requiredSkillsArea.getText().trim();
        String description = descriptionArea.getText().trim();

        previewTitleLabel.setText(role.isBlank() ? "Vacancy preview" : role);
        StringBuilder summaryBuilder = new StringBuilder();
        if (!moduleCode.isBlank()) {
            summaryBuilder.append(moduleCode);
        }
        if (!moduleName.isBlank()) {
            if (!summaryBuilder.isEmpty()) {
                summaryBuilder.append(" • ");
            }
            summaryBuilder.append(moduleName);
        }
        if (!semester.isBlank()) {
            if (!summaryBuilder.isEmpty()) {
                summaryBuilder.append(" • ");
            }
            summaryBuilder.append(semester);
        }
        if (!hours.isBlank()) {
            if (!summaryBuilder.isEmpty()) {
                summaryBuilder.append(" • ");
            }
            summaryBuilder.append(hours).append(" hrs/week");
        }
        if (!openings.isBlank()) {
            if (!summaryBuilder.isEmpty()) {
                summaryBuilder.append(" • ");
            }
            summaryBuilder.append(openings).append(" opening(s)");
        }
        previewSummaryLabel.setText(summaryBuilder.isEmpty()
                ? "Enter vacancy details and use Preview to confirm the listing."
                : summaryBuilder.toString());
        previewRequirementsArea.setText(requiredSkills.isBlank() ? "Required skills will appear here." : "• " + requiredSkills.replace(";", "\n• "));
        previewDescriptionArea.setText(description.isBlank() ? "Description will appear here." : description);
        previewRequirementsArea.setCaretPosition(0);
        previewDescriptionArea.setCaretPosition(0);
    }

    private void publishJob() {
        try {
            JobPosting savedJob = jobService.createJob(new JobPosting(
                    "",
                    roleField.getText(),
                    moduleCodeField.getText(),
                    moduleNameField.getText(),
                    semesterField.getText(),
                    hoursPerWeekField.getText(),
                    requiredSkillsArea.getText(),
                    tagsField.getText(),
                    descriptionArea.getText(),
                    parseOpenings()
            ));
            clearPostJobForm();
            refreshJobSelector(savedJob.id());
            updatePreview(savedJob);
            JOptionPane.showMessageDialog(this, "Job published successfully.");
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage());
        }
    }

    private int parseOpenings() {
        String value = openingsField.getText().trim();
        if (value.isBlank()) {
            throw new IllegalArgumentException("Openings is required.");
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Openings must be a whole number.");
        }
    }

    private void clearPostJobForm() {
        roleField.setText("");
        hoursPerWeekField.setText("");
        moduleCodeField.setText("");
        moduleNameField.setText("");
        semesterField.setText("");
        requiredSkillsArea.setText("");
        descriptionArea.setText("");
        tagsField.setText("");
        openingsField.setText("");
    }

    private void updatePreview(JobPosting job) {
        previewTitleLabel.setText(job.title());
        previewSummaryLabel.setText(job.moduleCode()
                + " • "
                + job.moduleName()
                + " • "
                + job.semester()
                + " • "
                + job.hoursPerWeek()
                + " hrs/week • "
                + job.openings()
                + " opening(s)");
        previewRequirementsArea.setText("• " + job.requiredSkills().replace("; ", "\n• "));
        previewDescriptionArea.setText(job.description());
    }

    private void refreshJobSelector(String selectedJobId) {
        suppressApplicantRefresh = true;
        jobSelector.removeAllItems();
        for (JobPosting job : jobService.getAllJobs()) {
            jobSelector.addItem(job);
        }
        if (selectedJobId != null) {
            for (int index = 0; index < jobSelector.getItemCount(); index++) {
                JobPosting job = jobSelector.getItemAt(index);
                if (job.id().equals(selectedJobId)) {
                    jobSelector.setSelectedIndex(index);
                    suppressApplicantRefresh = false;
                    return;
                }
            }
        }
        if (jobSelector.getItemCount() > 0) {
            jobSelector.setSelectedIndex(0);
        }
        suppressApplicantRefresh = false;
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

    private void shortlistApplicant(JobPosting job, String applicationId) {
        String note = promptForNote(
                "Shortlist Applicant",
                "Optional shortlist note for the applicant:",
                "Shortlisted for final review."
        );
        if (note == null) {
            return;
        }
        try {
            applicationService.shortlistApplicant(applicationId, note);
            refreshApplicantList();
            JOptionPane.showMessageDialog(this, "Applicant shortlisted for " + job.title() + ".");
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage());
        }
    }

    private void rejectApplicant(JobPosting job, String applicationId) {
        String note = promptForNote(
                "Reject Applicant",
                "Optional rejection feedback for the applicant:",
                "Thank you for applying. This application was not selected."
        );
        if (note == null) {
            return;
        }
        try {
            applicationService.rejectApplicant(applicationId, note);
            refreshApplicantList();
            JOptionPane.showMessageDialog(this, "Applicant rejected for " + job.title() + ".");
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage());
        }
    }

    private void scheduleInterview(JobPosting job, String applicationId) {
        JTextField dateTimeField = UiFactory.textField();
        dateTimeField.setText(LocalDateTime.now().plusDays(1).withMinute(0).withSecond(0).withNano(0).toString());
        JTextArea noteArea = UiFactory.textArea(3);
        noteArea.setText("Interview scheduled. Please arrive 10 minutes early.");

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 10));
        form.setOpaque(false);
        form.add(labeledField("Interview time (yyyy-MM-ddTHH:mm)", dateTimeField));
        form.add(scrollField("Applicant note", noteArea));

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                "Schedule Interview",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            applicationService.scheduleInterview(applicationId, dateTimeField.getText(), noteArea.getText());
            refreshApplicantList();
            JOptionPane.showMessageDialog(this, "Interview scheduled for " + job.title() + ".");
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage());
        }
    }

    private String promptForNote(String title, String label, String defaultText) {
        JTextArea noteArea = UiFactory.textArea(4);
        noteArea.setText(defaultText);
        int result = JOptionPane.showConfirmDialog(
                this,
                scrollField(label, noteArea),
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        return result == JOptionPane.OK_OPTION ? noteArea.getText() : null;
    }

    private void showPage(String page) {
        pageLayout.show(pagePanel, page);
    }
}
