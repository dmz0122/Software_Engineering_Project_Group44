package com.group44.tarecruit.ui;

import com.group44.tarecruit.model.ApplicantProfile;
import com.group44.tarecruit.model.ApplicationStatus;
import com.group44.tarecruit.model.JobApplication;
import com.group44.tarecruit.model.JobPosting;
import com.group44.tarecruit.model.NotificationItem;
import com.group44.tarecruit.model.UserAccount;
import com.group44.tarecruit.service.ApplicationService;
import com.group44.tarecruit.service.CvService;
import com.group44.tarecruit.service.JobService;
import com.group44.tarecruit.service.NotificationService;
import com.group44.tarecruit.service.ProfileService;
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
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ApplicantWorkspacePanel extends JPanel {
    private static final String DASHBOARD_PAGE = "dashboard";
    private static final String PROFILE_PAGE = "profile";
    private static final String JOBS_PAGE = "jobs";
    private static final String APPLICATIONS_PAGE = "applications";
    private static final String ALL_TAGS = "All tags";
    private static final String ALL_SEMESTERS = "All semesters";
    private static final String ALL_STATUSES = "All statuses";

    private final ProfileService profileService;
    private final JobService jobService;
    private final ApplicationService applicationService;
    private final NotificationService notificationService;
    private final CvService cvService;
    private final Runnable accountAction;
    private final Runnable logoutAction;

    private final CardLayout pageLayout;
    private final JPanel pagePanel;
    private final JPanel dashboardNotifications;
    private final JPanel dashboardJobs;
    private final JLabel dashboardGreeting;
    private final JLabel dashboardProfileStatus;
    private final JTextField fullNameField;
    private final JTextField studentIdField;
    private final JTextField programmeField;
    private final JTextField yearField;
    private final JTextArea skillsArea;
    private final JTextField availabilityField;
    private final JTextField gpaField;
    private final JLabel cvStatusLabel;
    private final JLabel avatarStatusLabel;
    private final JLabel resumeStatusLabel;
    private final JLabel profileTimestampLabel;
    private final JTextField jobSearchField;
    private final JComboBox<String> jobTagFilterBox;
    private final JComboBox<String> jobSemesterFilterBox;
    private final JPanel jobsListPanel;
    private final JLabel detailTitleLabel;
    private final JLabel detailSummaryLabel;
    private final JLabel detailApplicationStatusLabel;
    private final JTextArea detailRequirementsArea;
    private final JTextArea detailDescriptionArea;
    private final JButton applyButton;
    private final JComboBox<String> applicationSemesterFilterBox;
    private final JPanel applicationStatusFilterPanel;
    private final JPanel applicationsListPanel;

    private UserAccount currentUser;
    private JobPosting selectedJob;
    private String currentCvOriginalFileName = "";
    private String currentCvStoredPath = "";
    private String currentAvatarOriginalFileName = "";
    private String currentAvatarStoredPath = "";
    private Map<String, JobApplication> applicationsByJobId = new LinkedHashMap<>();
    private boolean suppressFilterRefresh;

    public ApplicantWorkspacePanel(
            ProfileService profileService,
            JobService jobService,
            ApplicationService applicationService,
            NotificationService notificationService,
            CvService cvService,
            Runnable accountAction,
            Runnable logoutAction
    ) {
        this.profileService = profileService;
        this.jobService = jobService;
        this.applicationService = applicationService;
        this.notificationService = notificationService;
        this.cvService = cvService;
        this.accountAction = accountAction;
        this.logoutAction = logoutAction;

        setLayout(new BorderLayout());
        setBackground(Theme.APP_BACKGROUND);

        add(buildSidebar(), BorderLayout.WEST);

        pageLayout = new CardLayout();
        pagePanel = new JPanel(pageLayout);
        pagePanel.setOpaque(false);

        dashboardGreeting = UiFactory.sectionLabel("");
        dashboardProfileStatus = UiFactory.mutedLabel("");
        dashboardNotifications = new JPanel();
        dashboardNotifications.setLayout(new BoxLayout(dashboardNotifications, BoxLayout.Y_AXIS));
        dashboardNotifications.setOpaque(false);
        dashboardJobs = new JPanel();
        dashboardJobs.setLayout(new BoxLayout(dashboardJobs, BoxLayout.Y_AXIS));
        dashboardJobs.setOpaque(false);

        fullNameField = UiFactory.textField();
        studentIdField = UiFactory.numericTextField(12);
        programmeField = UiFactory.textField();
        yearField = UiFactory.textField();
        skillsArea = UiFactory.textArea(4);
        availabilityField = UiFactory.textField();
        gpaField = UiFactory.textField();
        cvStatusLabel = UiFactory.mutedLabel("No CV uploaded yet.");
        avatarStatusLabel = UiFactory.mutedLabel("No avatar uploaded yet.");
        resumeStatusLabel = UiFactory.mutedLabel("No PDF resume generated yet.");
        profileTimestampLabel = UiFactory.mutedLabel("");

        jobSearchField = UiFactory.textField();
        jobTagFilterBox = new JComboBox<>();
        jobTagFilterBox.setFont(Theme.BODY_FONT);
        jobSemesterFilterBox = new JComboBox<>();
        jobSemesterFilterBox.setFont(Theme.BODY_FONT);

        jobsListPanel = new JPanel();
        jobsListPanel.setOpaque(false);
        jobsListPanel.setLayout(new BoxLayout(jobsListPanel, BoxLayout.Y_AXIS));
        detailTitleLabel = UiFactory.sectionLabel("Job Detail");
        detailSummaryLabel = UiFactory.mutedLabel("Select a role to review its details.");
        detailApplicationStatusLabel = UiFactory.mutedLabel("Apply to see this role appear in your application tracker.");
        detailRequirementsArea = readOnlyArea();
        detailDescriptionArea = readOnlyArea();
        applyButton = UiFactory.primaryButton("Apply now");
        applyButton.addActionListener(event -> applyForSelectedJob());

        applicationSemesterFilterBox = new JComboBox<>();
        applicationSemesterFilterBox.setFont(Theme.BODY_FONT);
        applicationStatusFilterPanel = UiFactory.flowPanel(FlowLayout.LEFT, 10, 0);
        applicationsListPanel = new JPanel();
        applicationsListPanel.setOpaque(false);
        applicationsListPanel.setLayout(new BoxLayout(applicationsListPanel, BoxLayout.Y_AXIS));

        attachRefreshOnChange(jobSearchField, this::refreshJobs);
        jobTagFilterBox.addActionListener(event -> {
            if (!suppressFilterRefresh) {
                refreshJobs();
            }
        });
        jobSemesterFilterBox.addActionListener(event -> {
            if (!suppressFilterRefresh) {
                refreshJobs();
            }
        });
        applicationSemesterFilterBox.addActionListener(event -> {
            if (!suppressFilterRefresh) {
                refreshApplications();
            }
        });

        pagePanel.add(UiFactory.scrollPane(buildDashboardPage()), DASHBOARD_PAGE);
        pagePanel.add(UiFactory.scrollPane(buildProfilePage()), PROFILE_PAGE);
        pagePanel.add(UiFactory.scrollPane(buildJobsPage()), JOBS_PAGE);
        pagePanel.add(UiFactory.scrollPane(buildApplicationsPage()), APPLICATIONS_PAGE);
        add(pagePanel, BorderLayout.CENTER);
    }

    public void setCurrentUser(UserAccount currentUser) {
        this.currentUser = currentUser;
    }

    public void refreshAll() {
        refreshProfile();
        refreshJobs();
        refreshDashboard();
        refreshApplications();
        showPage(DASHBOARD_PAGE);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Theme.SURFACE);
        sidebar.setBorder(BorderFactory.createEmptyBorder(26, 24, 26, 24));
        sidebar.setPreferredSize(new Dimension(250, 800));

        JLabel brand = UiFactory.sectionLabel("TA Recruit");
        sidebar.add(brand);
        sidebar.add(Box.createVerticalStrut(28));

        sidebar.add(navButton("Dashboard", DASHBOARD_PAGE));
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(navButton("My Profile", PROFILE_PAGE));
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(navButton("Jobs", JOBS_PAGE));
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(navButton("Applications", APPLICATIONS_PAGE));
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

    private JButton navButton(String label, String page) {
        JButton button = UiFactory.navButton(label);
        button.addActionListener(event -> showPage(page));
        return button;
    }

    private JPanel buildDashboardPage() {
        JPanel page = pageWrapper();
        page.add(UiFactory.titleLabel("Dashboard"));
        page.add(Box.createVerticalStrut(12));
        page.add(dashboardGreeting);
        page.add(Box.createVerticalStrut(4));
        page.add(dashboardProfileStatus);
        page.add(Box.createVerticalStrut(24));

        JPanel grid = new JPanel(new GridLayout(1, 2, 20, 0));
        grid.setOpaque(false);

        JPanel jobsCard = UiFactory.card();
        jobsCard.add(sectionWithBody("Open teaching assistant vacancies", dashboardJobs), BorderLayout.CENTER);
        JPanel notificationCard = UiFactory.card();
        notificationCard.add(sectionWithBody("Latest updates", dashboardNotifications), BorderLayout.CENTER);

        grid.add(jobsCard);
        grid.add(notificationCard);
        page.add(grid);
        return page;
    }

    private JPanel buildProfilePage() {
        JPanel page = pageWrapper();
        page.add(UiFactory.titleLabel("Applicant Profile"));
        page.add(Box.createVerticalStrut(8));
        page.add(UiFactory.mutedLabel("Create or update your reusable profile. Required fields must be completed before saving."));
        page.add(Box.createVerticalStrut(24));

        JPanel card = UiFactory.card();
        JPanel grid = new JPanel(new GridLayout(0, 2, 18, 16));
        grid.setOpaque(false);
        grid.add(labeledField("Full name", fullNameField));
        grid.add(labeledField("Student ID", studentIdField));
        grid.add(labeledField("Programme", programmeField));
        grid.add(labeledField("Year", yearField));
        grid.add(labeledField("Availability", availabilityField));
        grid.add(labeledField("GPA", gpaField));
        JPanel skillsField = new JPanel(new BorderLayout());
        skillsField.setOpaque(false);
        skillsField.add(UiFactory.bodyLabel("Skills"), BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(skillsArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1, true));
        skillsField.add(scrollPane, BorderLayout.CENTER);
        grid.add(skillsField);
        grid.add(buildCvCard());

        card.add(grid, BorderLayout.CENTER);
        page.add(card);
        page.add(Box.createVerticalStrut(16));

        JPanel actionRow = UiFactory.flowPanel(java.awt.FlowLayout.LEFT, 12, 0);
        JButton saveButton = UiFactory.primaryButton("Save profile");
        saveButton.addActionListener(event -> saveProfile());
        actionRow.add(saveButton);
        actionRow.add(profileTimestampLabel);
        page.add(actionRow);
        return page;
    }

    private JPanel buildCvCard() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(UiFactory.bodyLabel("Resume & Photo"));
        panel.add(Box.createVerticalStrut(8));
        panel.add(avatarStatusLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(cvStatusLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(resumeStatusLabel);
        panel.add(Box.createVerticalStrut(12));
        JPanel buttonRow = new JPanel(new GridLayout(0, 2, 10, 10));
        buttonRow.setOpaque(false);
        JButton uploadButton = UiFactory.secondaryButton("Upload PDF CV");
        uploadButton.addActionListener(event -> uploadCv());
        JButton uploadPhotoButton = UiFactory.lightButton("Upload Photo");
        uploadPhotoButton.addActionListener(event -> uploadAvatar());
        JButton generateButton = UiFactory.primaryButton("Generate PDF");
        generateButton.addActionListener(event -> generateResumePdf());
        JButton openButton = UiFactory.lightButton("Open Resume");
        openButton.addActionListener(event -> openGeneratedResume());
        buttonRow.add(uploadButton);
        buttonRow.add(uploadPhotoButton);
        buttonRow.add(generateButton);
        buttonRow.add(openButton);
        panel.add(buttonRow);
        return panel;
    }

    private JPanel buildJobsPage() {
        JPanel page = pageWrapper();
        page.add(UiFactory.titleLabel("Available Jobs"));
        page.add(Box.createVerticalStrut(8));
        page.add(UiFactory.mutedLabel("Browse the current vacancies and review full job requirements before applying."));
        page.add(Box.createVerticalStrut(18));
        page.add(buildJobFiltersCard());
        page.add(Box.createVerticalStrut(24));

        JPanel layout = new JPanel(new GridLayout(1, 2, 20, 0));
        layout.setOpaque(false);
        JPanel listCard = UiFactory.card();
        listCard.add(jobsListPanel, BorderLayout.NORTH);
        JPanel detailCard = buildJobDetailCard();
        layout.add(listCard);
        layout.add(detailCard);
        page.add(layout);
        return page;
    }

    private JPanel buildJobFiltersCard() {
        JPanel card = UiFactory.card();
        JPanel content = new JPanel(new GridLayout(1, 4, 14, 0));
        content.setOpaque(false);
        content.add(labeledField("Search", jobSearchField));
        content.add(labeledField("Skill tag", jobTagFilterBox));
        content.add(labeledField("Semester", jobSemesterFilterBox));

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
        actions.add(UiFactory.bodyLabel("Actions"));
        actions.add(Box.createVerticalStrut(8));
        JButton clearButton = UiFactory.lightButton("Reset filters");
        clearButton.addActionListener(event -> resetJobFilters());
        actions.add(clearButton);
        card.add(content, BorderLayout.CENTER);
        content.add(actions);
        return card;
    }

    private JPanel buildJobDetailCard() {
        JPanel detailCard = UiFactory.card();
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(detailTitleLabel);
        content.add(Box.createVerticalStrut(8));
        content.add(detailSummaryLabel);
        content.add(Box.createVerticalStrut(8));
        content.add(detailApplicationStatusLabel);
        content.add(Box.createVerticalStrut(16));
        content.add(UiFactory.bodyLabel("Requirements"));
        content.add(Box.createVerticalStrut(8));
        content.add(detailRequirementsArea);
        content.add(Box.createVerticalStrut(14));
        content.add(UiFactory.bodyLabel("Description"));
        content.add(Box.createVerticalStrut(8));
        content.add(detailDescriptionArea);
        content.add(Box.createVerticalStrut(18));

        JPanel actions = new JPanel(new GridLayout(1, 2, 12, 0));
        actions.setOpaque(false);
        JButton saveForLater = UiFactory.lightButton("Save for later");
        saveForLater.setEnabled(false);
        actions.add(applyButton);
        actions.add(saveForLater);
        content.add(actions);

        detailCard.add(content, BorderLayout.CENTER);
        return detailCard;
    }

    private JPanel buildApplicationsPage() {
        JPanel page = pageWrapper();
        page.add(UiFactory.titleLabel("My Applications"));
        page.add(Box.createVerticalStrut(8));
        page.add(UiFactory.mutedLabel("Any status changes made by the organiser are reflected here after you reload or log back in."));
        page.add(Box.createVerticalStrut(18));
        page.add(buildApplicationFiltersCard());
        page.add(Box.createVerticalStrut(24));
        page.add(applicationsListPanel);
        return page;
    }

    private JPanel buildApplicationFiltersCard() {
        JPanel card = UiFactory.card();
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JPanel topRow = UiFactory.flowPanel(FlowLayout.LEFT, 14, 0);
        topRow.add(UiFactory.bodyLabel("Semester"));
        topRow.add(applicationSemesterFilterBox);
        JButton clearButton = UiFactory.lightButton("Show all");
        clearButton.addActionListener(event -> resetApplicationFilters());
        topRow.add(clearButton);
        content.add(topRow);
        content.add(Box.createVerticalStrut(14));
        content.add(UiFactory.bodyLabel("Status"));
        content.add(Box.createVerticalStrut(10));
        content.add(applicationStatusFilterPanel);
        card.add(content, BorderLayout.CENTER);
        buildApplicationStatusButtons();
        return card;
    }

    private JPanel pageWrapper() {
        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));
        return page;
    }

    private JPanel sectionWithBody(String title, JPanel contentBody) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(UiFactory.sectionLabel(title));
        panel.add(Box.createVerticalStrut(16));
        panel.add(contentBody);
        return panel;
    }

    private JPanel labeledField(String labelText, Component field) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(UiFactory.bodyLabel(labelText));
        panel.add(Box.createVerticalStrut(8));
        panel.add(field);
        return panel;
    }

    private JTextArea readOnlyArea() {
        JTextArea area = UiFactory.textArea(5);
        area.setEditable(false);
        area.setBackground(Theme.SURFACE_MUTED);
        area.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        return area;
    }

    private void buildApplicationStatusButtons() {
        applicationStatusFilterPanel.removeAll();
        for (String label : List.of(
                ALL_STATUSES,
                ApplicationStatus.APPLIED.label(),
                ApplicationStatus.UNDER_REVIEW.label(),
                ApplicationStatus.SHORTLISTED.label(),
                ApplicationStatus.INTERVIEW_SCHEDULED.label(),
                ApplicationStatus.SELECTED.label(),
                ApplicationStatus.REJECTED.label()
        )) {
            JButton button = label.equals(ALL_STATUSES)
                    ? UiFactory.secondaryButton(label)
                    : UiFactory.lightButton(label);
            button.addActionListener(event -> {
                setSelectedApplicationStatus(label);
                refreshApplications();
            });
            applicationStatusFilterPanel.add(button);
        }
        applicationStatusFilterPanel.revalidate();
        applicationStatusFilterPanel.repaint();
    }

    private void setSelectedApplicationStatus(String selectedStatus) {
        for (Component component : applicationStatusFilterPanel.getComponents()) {
            if (!(component instanceof JButton button)) {
                continue;
            }
            boolean selected = button.getText().equalsIgnoreCase(selectedStatus);
            button.setBackground(selected ? Theme.ACCENT : Theme.SURFACE_MUTED);
            button.setForeground(selected ? Color.WHITE : Theme.TEXT);
        }
    }

    private void attachRefreshOnChange(JTextField field, Runnable action) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                action.run();
            }
        });
    }

    private void refreshDashboard() {
        dashboardGreeting.setText("Welcome back, " + currentUser.displayName());
        Optional<ApplicantProfile> profile = profileService.findProfile(currentUser.id());
        dashboardProfileStatus.setText(profile.filter(ApplicantProfile::isComplete).isPresent()
                ? "Your profile is complete and ready for applications."
                : "Your profile is incomplete. Finish it before applying for a new role.");

        rebuildVerticalList(dashboardJobs, jobService.getAllJobs().stream()
                .map(this::dashboardJobCard)
                .toList(), "No jobs are available right now.");
        rebuildVerticalList(dashboardNotifications, notificationService.getNotificationsForUser(currentUser.id()).stream()
                .map(this::notificationCard)
                .toList(), "No updates yet.");
    }

    private Component dashboardJobCard(JobPosting job) {
        JPanel card = UiFactory.card();
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JLabel title = UiFactory.bodyLabel(job.title());
        title.setFont(Theme.BUTTON_FONT);
        content.add(title);
        content.add(Box.createVerticalStrut(4));
        content.add(UiFactory.mutedLabel(job.summaryLine()));
        if (hasApplied(job)) {
            content.add(Box.createVerticalStrut(8));
            content.add(UiFactory.mutedLabel("Status: " + applicationsByJobId.get(job.id()).status().label()));
        }
        content.add(Box.createVerticalStrut(10));
        JButton viewButton = UiFactory.lightButton("View detail");
        viewButton.addActionListener(event -> {
            selectedJob = job;
            updateJobDetailPanel();
            showPage(JOBS_PAGE);
        });
        JPanel row = UiFactory.flowPanel(java.awt.FlowLayout.LEFT, 0, 0);
        row.add(viewButton);
        content.add(row);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private Component notificationCard(NotificationItem item) {
        JPanel card = UiFactory.card();
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 108));
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JLabel title = UiFactory.bodyLabel(item.title());
        title.setFont(Theme.BUTTON_FONT);
        content.add(title);
        content.add(Box.createVerticalStrut(6));
        content.add(UiFactory.bodyLabel("<html>" + item.message() + "</html>"));
        content.add(Box.createVerticalStrut(8));
        content.add(UiFactory.mutedLabel(item.createdAt().replace('T', ' ')));
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private void refreshProfile() {
        Optional<ApplicantProfile> profileOptional = profileService.findProfile(currentUser.id());
        if (profileOptional.isPresent()) {
            ApplicantProfile profile = profileOptional.get();
            fullNameField.setText(profile.fullName());
            studentIdField.setText(profile.studentId());
            programmeField.setText(profile.programme());
            yearField.setText(profile.year());
            skillsArea.setText(profile.skills());
            availabilityField.setText(profile.availability());
            gpaField.setText(profile.gpa());
            currentCvOriginalFileName = profile.cvOriginalFileName();
            currentCvStoredPath = profile.cvStoredPath();
            currentAvatarOriginalFileName = profile.avatarOriginalFileName();
            currentAvatarStoredPath = profile.avatarStoredPath();
            cvStatusLabel.setText(profile.cvOriginalFileName().isBlank()
                    ? "No CV uploaded yet."
                    : "Current CV: " + profile.cvOriginalFileName());
            avatarStatusLabel.setText(profile.avatarOriginalFileName().isBlank()
                    ? "No avatar uploaded yet."
                    : "Avatar: " + profile.avatarOriginalFileName());
            resumeStatusLabel.setText(profile.cvStoredPath().isBlank()
                    ? "No PDF resume generated yet."
                    : "Generated resume ready.");
            profileTimestampLabel.setText("Last updated: " + profile.updatedAt().replace('T', ' '));
        } else {
            fullNameField.setText("");
            studentIdField.setText("");
            programmeField.setText("");
            yearField.setText("");
            skillsArea.setText("");
            availabilityField.setText("");
            gpaField.setText("");
            currentCvOriginalFileName = "";
            currentCvStoredPath = "";
            currentAvatarOriginalFileName = "";
            currentAvatarStoredPath = "";
            cvStatusLabel.setText("No CV uploaded yet.");
            avatarStatusLabel.setText("No avatar uploaded yet.");
            resumeStatusLabel.setText("No PDF resume generated yet.");
            profileTimestampLabel.setText("");
        }
    }

    private void refreshJobs() {
        applicationsByJobId = applicationService.findApplicationsForApplicant(currentUser.id()).stream()
                .collect(LinkedHashMap::new, (map, application) -> map.put(application.jobId(), application), LinkedHashMap::putAll);
        reloadJobFilters();
        List<JobPosting> jobs = jobService.filterJobs(
                jobSearchField.getText(),
                selectedComboValue(jobTagFilterBox, ALL_TAGS),
                selectedComboValue(jobSemesterFilterBox, ALL_SEMESTERS)
        );
        if (selectedJob != null) {
            String selectedJobId = selectedJob.id();
            selectedJob = jobs.stream()
                    .filter(job -> job.id().equals(selectedJobId))
                    .findFirst()
                    .orElse(null);
        }
        if (!jobs.isEmpty() && selectedJob == null) {
            selectedJob = jobs.getFirst();
        }
        if (jobs.isEmpty()) {
            selectedJob = null;
        }
        rebuildVerticalList(
                jobsListPanel,
                jobs.stream().map(this::jobCard).toList(),
                hasActiveJobFilters() ? "No jobs match your current filters." : "No jobs are available right now."
        );
        updateJobDetailPanel();
    }

    private Component jobCard(JobPosting job) {
        JPanel card = UiFactory.card();
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = UiFactory.bodyLabel(job.title());
        title.setFont(Theme.BUTTON_FONT);
        content.add(title);
        content.add(Box.createVerticalStrut(4));
        content.add(UiFactory.mutedLabel(job.moduleCode() + " • " + job.moduleName() + " • " + job.hoursPerWeek() + " hrs/week"));
        content.add(Box.createVerticalStrut(10));

        JPanel tags = UiFactory.flowPanel(java.awt.FlowLayout.LEFT, 8, 0);
        if (!job.tags().isBlank()) {
            for (String tag : job.tags().split("\\|")) {
                if (tag.isBlank()) {
                    continue;
                }
                tags.add(pillLabel(tag.trim(), Theme.SURFACE_MUTED, Theme.TEXT));
            }
        }
        content.add(tags);
        JobApplication existingApplication = applicationsByJobId.get(job.id());
        if (existingApplication != null) {
            content.add(Box.createVerticalStrut(8));
            content.add(statusPill(existingApplication.status()));
        }
        content.add(Box.createVerticalStrut(12));

        JPanel buttonRow = UiFactory.flowPanel(java.awt.FlowLayout.LEFT, 10, 0);
        JButton viewButton = UiFactory.lightButton("View");
        viewButton.addActionListener(event -> {
            selectedJob = job;
            updateJobDetailPanel();
        });
        JButton quickApplyButton = UiFactory.secondaryButton("Apply");
        if (existingApplication != null) {
            quickApplyButton.setText("Applied");
            quickApplyButton.setEnabled(false);
        }
        quickApplyButton.addActionListener(event -> {
            selectedJob = job;
            updateJobDetailPanel();
            applyForSelectedJob();
        });
        buttonRow.add(viewButton);
        buttonRow.add(quickApplyButton);
        content.add(buttonRow);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private void updateJobDetailPanel() {
        if (selectedJob == null) {
            detailTitleLabel.setText("Job Detail");
            detailSummaryLabel.setText("Select a role to review its details.");
            detailApplicationStatusLabel.setText("Apply to see this role appear in your application tracker.");
            detailRequirementsArea.setText("No job selected.");
            detailDescriptionArea.setText("");
            applyButton.setText("Apply now");
            applyButton.setEnabled(false);
            return;
        }

        detailTitleLabel.setText(selectedJob.title());
        detailSummaryLabel.setText(selectedJob.moduleCode() + " • " + selectedJob.moduleName() + " • " + selectedJob.semester() + " • " + selectedJob.hoursPerWeek() + " hrs/week");
        detailRequirementsArea.setText("• " + selectedJob.requiredSkills().replace("; ", "\n• "));
        detailDescriptionArea.setText(selectedJob.description());
        Optional<JobApplication> applicationOptional = applicationFor(selectedJob);
        if (applicationOptional.isPresent()) {
            JobApplication application = applicationOptional.get();
            detailApplicationStatusLabel.setText("<html>Application status: "
                    + application.status().label()
                    + " • submitted "
                    + application.appliedAt().replace('T', ' ')
                    + (application.hasInterviewScheduled()
                    ? "<br>Interview: " + application.interviewAt().replace('T', ' ')
                    : "")
                    + "</html>");
            applyButton.setText("Already applied");
            applyButton.setEnabled(false);
            return;
        }

        detailApplicationStatusLabel.setText(profileService.findProfile(currentUser.id()).filter(ApplicantProfile::isComplete).isPresent()
                ? "You can submit an application for this vacancy now."
                : "Complete your profile before you submit an application.");
        applyButton.setText("Apply now");
        applyButton.setEnabled(true);
    }

    private void refreshApplications() {
        reloadApplicationFilters();
        List<JobApplication> applications = applicationService.findApplicationsForApplicant(
                currentUser.id(),
                selectedComboValue(applicationSemesterFilterBox, ALL_SEMESTERS),
                selectedApplicationStatus()
        );
        rebuildVerticalList(applicationsListPanel, applications.stream()
                .map(this::applicationCard)
                .toList(), hasActiveApplicationFilters()
                ? "No applications match the selected filters."
                : "You have not submitted any applications yet.");
    }

    private Component applicationCard(JobApplication application) {
        JPanel card = UiFactory.card();
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JobPosting job = applicationService.findJobForApplication(application).orElse(null);
        String jobTitle = job == null ? "Unknown role" : job.title();
        JLabel title = UiFactory.bodyLabel(jobTitle);
        title.setFont(Theme.BUTTON_FONT);
        content.add(title);
        content.add(Box.createVerticalStrut(6));
        JPanel statusRow = UiFactory.flowPanel(FlowLayout.LEFT, 10, 0);
        statusRow.add(statusPill(application.status()));
        if (application.hasInterviewScheduled()) {
            statusRow.add(pillLabel("Interview " + application.interviewAt().replace('T', ' '), Theme.PRIMARY_DARK, Color.WHITE));
        }
        content.add(statusRow);
        content.add(Box.createVerticalStrut(8));
        content.add(UiFactory.mutedLabel("Applied at " + application.appliedAt().replace('T', ' ')));
        if (job != null) {
            content.add(Box.createVerticalStrut(6));
            content.add(UiFactory.mutedLabel(job.moduleCode() + " • " + job.moduleName() + " • " + job.semester()));
        }
        if (!application.note().isBlank()) {
            content.add(Box.createVerticalStrut(8));
            content.add(UiFactory.bodyLabel("<html>" + application.note() + "</html>"));
        }
        if (application.status() != ApplicationStatus.SELECTED) {
            content.add(Box.createVerticalStrut(14));
            JPanel actions = UiFactory.flowPanel(FlowLayout.LEFT, 10, 0);
            JButton withdrawButton = UiFactory.lightButton("Withdraw");
            withdrawButton.addActionListener(event -> withdrawApplication(application));
            actions.add(withdrawButton);
            content.add(actions);
        }
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private void reloadJobFilters() {
        reloadComboBox(jobTagFilterBox, ALL_TAGS, jobService.availableTags());
        reloadComboBox(jobSemesterFilterBox, ALL_SEMESTERS, jobService.availableSemesters());
    }

    private void resetJobFilters() {
        jobSearchField.setText("");
        jobTagFilterBox.setSelectedItem(ALL_TAGS);
        jobSemesterFilterBox.setSelectedItem(ALL_SEMESTERS);
        refreshJobs();
    }

    private void reloadApplicationFilters() {
        reloadComboBox(applicationSemesterFilterBox, ALL_SEMESTERS, applicationService.availableApplicationSemestersForApplicant(currentUser.id()));
        setSelectedApplicationStatus(selectedApplicationStatusLabel());
    }

    private void resetApplicationFilters() {
        applicationSemesterFilterBox.setSelectedItem(ALL_SEMESTERS);
        setSelectedApplicationStatus(ALL_STATUSES);
        refreshApplications();
    }

    private void reloadComboBox(JComboBox<String> comboBox, String defaultItem, List<String> dynamicItems) {
        suppressFilterRefresh = true;
        Object previousSelection = comboBox.getSelectedItem();
        comboBox.removeAllItems();
        comboBox.addItem(defaultItem);
        for (String item : dynamicItems) {
            if (!defaultItem.equalsIgnoreCase(item)) {
                comboBox.addItem(item);
            }
        }
        if (previousSelection != null) {
            comboBox.setSelectedItem(previousSelection);
        }
        if (comboBox.getSelectedItem() == null) {
            comboBox.setSelectedItem(defaultItem);
        }
        suppressFilterRefresh = false;
    }

    private String selectedComboValue(JComboBox<String> comboBox, String fallback) {
        Object selectedItem = comboBox.getSelectedItem();
        return selectedItem == null ? fallback : selectedItem.toString();
    }

    private boolean hasActiveJobFilters() {
        return !jobSearchField.getText().isBlank()
                || !ALL_TAGS.equalsIgnoreCase(selectedComboValue(jobTagFilterBox, ALL_TAGS))
                || !ALL_SEMESTERS.equalsIgnoreCase(selectedComboValue(jobSemesterFilterBox, ALL_SEMESTERS));
    }

    private boolean hasActiveApplicationFilters() {
        return !ALL_SEMESTERS.equalsIgnoreCase(selectedComboValue(applicationSemesterFilterBox, ALL_SEMESTERS))
                || selectedApplicationStatus() != null;
    }

    private ApplicationStatus selectedApplicationStatus() {
        String selected = selectedApplicationStatusLabel();
        return ALL_STATUSES.equalsIgnoreCase(selected) ? null : ApplicationStatus.fromLabel(selected);
    }

    private String selectedApplicationStatusLabel() {
        for (Component component : applicationStatusFilterPanel.getComponents()) {
            if (component instanceof JButton button && Theme.ACCENT.equals(button.getBackground())) {
                return button.getText();
            }
        }
        return ALL_STATUSES;
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

    private JLabel pillLabel(String text, Color background, Color foreground) {
        JLabel label = UiFactory.bodyLabel("  " + text + "  ");
        label.setOpaque(true);
        label.setBackground(background);
        label.setForeground(foreground);
        label.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        return label;
    }

    private void withdrawApplication(JobApplication application) {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Withdraw this application? This cannot be undone.",
                "Confirm Withdrawal",
                JOptionPane.YES_NO_OPTION
        );
        if (result != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            applicationService.withdrawApplication(application.id(), currentUser.id());
            refreshAll();
            showPage(APPLICATIONS_PAGE);
            JOptionPane.showMessageDialog(this, "Application withdrawn successfully.");
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage());
        }
    }

    private void saveProfile() {
        try {
            ApplicantProfile savedProfile = profileService.saveProfile(new ApplicantProfile(
                    currentUser.id(),
                    fullNameField.getText(),
                    studentIdField.getText(),
                    programmeField.getText(),
                    yearField.getText(),
                    skillsArea.getText(),
                    availabilityField.getText(),
                    gpaField.getText(),
                    currentCvOriginalFileName,
                    currentCvStoredPath,
                    currentAvatarOriginalFileName,
                    currentAvatarStoredPath,
                    ""
            ));
            currentCvOriginalFileName = savedProfile.cvOriginalFileName();
            currentCvStoredPath = savedProfile.cvStoredPath();
            refreshAll();
            showPage(PROFILE_PAGE);
            JOptionPane.showMessageDialog(this, "Profile saved successfully.");
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage());
        }
    }

    private void uploadCv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("PDF resumes (*.pdf)", "pdf"));
        chooser.setAcceptAllFileFilterUsed(false);
        int selection = chooser.showOpenDialog(this);
        if (selection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            CvService.StoredCv storedCv = cvService.storeCv(chooser.getSelectedFile().toPath());
            ApplicantProfile savedProfile = profileService.saveCvReference(
                    currentUser.id(),
                    storedCv.originalFileName(),
                    storedCv.storedPath()
            );
            currentCvOriginalFileName = savedProfile.cvOriginalFileName();
            currentCvStoredPath = savedProfile.cvStoredPath();
            cvStatusLabel.setText("Current CV: " + currentCvOriginalFileName);
            resumeStatusLabel.setText("Resume ready: " + currentCvOriginalFileName);
            profileTimestampLabel.setText("Last updated: " + savedProfile.updatedAt().replace('T', ' '));
            JOptionPane.showMessageDialog(this, "CV uploaded successfully.");
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage());
        }
    }

    private void uploadAvatar() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Profile photos (*.jpg, *.jpeg, *.png)", "jpg", "jpeg", "png"));
        chooser.setAcceptAllFileFilterUsed(false);
        int selection = chooser.showOpenDialog(this);
        if (selection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            CvService.StoredCv storedAvatar = cvService.storeAvatar(chooser.getSelectedFile().toPath());
            ApplicantProfile savedProfile = profileService.saveAvatarReference(
                    currentUser.id(),
                    storedAvatar.originalFileName(),
                    storedAvatar.storedPath()
            );
            currentAvatarOriginalFileName = savedProfile.avatarOriginalFileName();
            currentAvatarStoredPath = savedProfile.avatarStoredPath();
            avatarStatusLabel.setText("Avatar: " + currentAvatarOriginalFileName);
            profileTimestampLabel.setText("Last updated: " + savedProfile.updatedAt().replace('T', ' '));
            JOptionPane.showMessageDialog(this, "Profile photo uploaded successfully.");
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage());
        }
    }

    private void generateResumePdf() {
        try {
            ApplicantProfile profile = profileService.saveProfile(new ApplicantProfile(
                    currentUser.id(),
                    fullNameField.getText(),
                    studentIdField.getText(),
                    programmeField.getText(),
                    yearField.getText(),
                    skillsArea.getText(),
                    availabilityField.getText(),
                    gpaField.getText(),
                    currentCvOriginalFileName,
                    currentCvStoredPath,
                    currentAvatarOriginalFileName,
                    currentAvatarStoredPath,
                    ""
            ));
            CvService.StoredCv generatedResume = cvService.generateResumePdf(profile);
            ApplicantProfile savedProfile = profileService.saveCvReference(
                    currentUser.id(),
                    generatedResume.originalFileName(),
                    generatedResume.storedPath()
            );
            currentCvOriginalFileName = savedProfile.cvOriginalFileName();
            currentCvStoredPath = savedProfile.cvStoredPath();
            cvStatusLabel.setText("Current CV: " + currentCvOriginalFileName);
            resumeStatusLabel.setText("Generated PDF resume: " + currentCvOriginalFileName);
            profileTimestampLabel.setText("Last updated: " + savedProfile.updatedAt().replace('T', ' '));
            JOptionPane.showMessageDialog(this, "PDF resume generated successfully.");
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage());
        }
    }

    private void openGeneratedResume() {
        openCv();
    }

    private void openCv() {
        if (currentCvStoredPath.isBlank()) {
            JOptionPane.showMessageDialog(this, "Upload or save a CV first.");
            return;
        }
        try {
            cvService.openCv(currentCvStoredPath);
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage());
        }
    }

    private void downloadCv() {
        if (currentCvStoredPath.isBlank()) {
            JOptionPane.showMessageDialog(this, "Upload or save a CV first.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File(currentCvOriginalFileName.isBlank() ? "cv.txt" : currentCvOriginalFileName));
        int selection = chooser.showSaveDialog(this);
        if (selection != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            cvService.exportCv(currentCvStoredPath, Path.of(chooser.getSelectedFile().getAbsolutePath()));
            JOptionPane.showMessageDialog(this, "CV downloaded successfully.");
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage());
        }
    }

    private void applyForSelectedJob() {
        if (selectedJob == null) {
            JOptionPane.showMessageDialog(this, "Select a job before applying.");
            return;
        }
        if (profileService.findProfile(currentUser.id()).filter(ApplicantProfile::isComplete).isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please complete your profile before applying.");
            showPage(PROFILE_PAGE);
            return;
        }
        try {
            applicationService.applyForJob(selectedJob.id(), currentUser.id());
            refreshAll();
            showPage(APPLICATIONS_PAGE);
            JOptionPane.showMessageDialog(this, "Application submitted successfully.");
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage());
        }
    }

    private boolean hasApplied(JobPosting job) {
        return applicationsByJobId.containsKey(job.id());
    }

    private Optional<JobApplication> applicationFor(JobPosting job) {
        return Optional.ofNullable(applicationsByJobId.get(job.id()));
    }

    private void showPage(String page) {
        pageLayout.show(pagePanel, page);
    }

    private void rebuildVerticalList(JPanel container, List<Component> cards, String emptyMessage) {
        container.removeAll();
        if (cards.isEmpty()) {
            container.add(UiFactory.mutedLabel(emptyMessage));
        } else {
            for (int index = 0; index < cards.size(); index++) {
                container.add(cards.get(index));
                if (index < cards.size() - 1) {
                    container.add(Box.createVerticalStrut(12));
                }
            }
        }
        container.revalidate();
        container.repaint();
    }
}
