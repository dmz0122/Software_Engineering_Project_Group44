package com.group44.tarecruit.ui;

import com.group44.tarecruit.model.ApplicantProfile;
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
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
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

    private final ProfileService profileService;
    private final JobService jobService;
    private final ApplicationService applicationService;
    private final NotificationService notificationService;
    private final CvService cvService;
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
    private final JLabel profileTimestampLabel;
    private final JPanel jobsListPanel;
    private final JLabel detailTitleLabel;
    private final JLabel detailSummaryLabel;
    private final JLabel detailApplicationStatusLabel;
    private final JTextArea detailRequirementsArea;
    private final JTextArea detailDescriptionArea;
    private final JButton applyButton;
    private final JPanel applicationsListPanel;

    private UserAccount currentUser;
    private JobPosting selectedJob;
    private String currentCvOriginalFileName = "";
    private String currentCvStoredPath = "";
    private Map<String, JobApplication> applicationsByJobId = new LinkedHashMap<>();

    public ApplicantWorkspacePanel(
            ProfileService profileService,
            JobService jobService,
            ApplicationService applicationService,
            NotificationService notificationService,
            CvService cvService,
            Runnable logoutAction
    ) {
        this.profileService = profileService;
        this.jobService = jobService;
        this.applicationService = applicationService;
        this.notificationService = notificationService;
        this.cvService = cvService;
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
        studentIdField = UiFactory.textField();
        programmeField = UiFactory.textField();
        yearField = UiFactory.textField();
        skillsArea = UiFactory.textArea(4);
        availabilityField = UiFactory.textField();
        gpaField = UiFactory.textField();
        cvStatusLabel = UiFactory.mutedLabel("No CV uploaded yet.");
        profileTimestampLabel = UiFactory.mutedLabel("");

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

        applicationsListPanel = new JPanel();
        applicationsListPanel.setOpaque(false);
        applicationsListPanel.setLayout(new BoxLayout(applicationsListPanel, BoxLayout.Y_AXIS));

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
        refreshDashboard();
        refreshProfile();
        refreshJobs();
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
        panel.add(UiFactory.bodyLabel("CV"));
        panel.add(Box.createVerticalStrut(8));
        panel.add(cvStatusLabel);
        panel.add(Box.createVerticalStrut(12));
        JPanel buttonRow = new JPanel(new GridLayout(1, 3, 10, 0));
        buttonRow.setOpaque(false);
        JButton uploadButton = UiFactory.secondaryButton("Upload CV");
        uploadButton.addActionListener(event -> uploadCv());
        JButton openButton = UiFactory.lightButton("Open CV");
        openButton.addActionListener(event -> openCv());
        JButton downloadButton = UiFactory.lightButton("Download CV");
        downloadButton.addActionListener(event -> downloadCv());
        buttonRow.add(uploadButton);
        buttonRow.add(openButton);
        buttonRow.add(downloadButton);
        panel.add(buttonRow);
        return panel;
    }

    private JPanel buildJobsPage() {
        JPanel page = pageWrapper();
        page.add(UiFactory.titleLabel("Available Jobs"));
        page.add(Box.createVerticalStrut(8));
        page.add(UiFactory.mutedLabel("Browse the current vacancies and review full job requirements before applying."));
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
        page.add(Box.createVerticalStrut(24));
        page.add(applicationsListPanel);
        return page;
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
            cvStatusLabel.setText(profile.cvOriginalFileName().isBlank()
                    ? "No CV uploaded yet."
                    : "Current CV: " + profile.cvOriginalFileName());
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
            cvStatusLabel.setText("No CV uploaded yet.");
            profileTimestampLabel.setText("");
        }
    }

    private void refreshJobs() {
        applicationsByJobId = applicationService.findApplicationsForApplicant(currentUser.id()).stream()
                .collect(LinkedHashMap::new, (map, application) -> map.put(application.jobId(), application), LinkedHashMap::putAll);
        List<JobPosting> jobs = jobService.getAllJobs();
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
        rebuildVerticalList(jobsListPanel, jobs.stream().map(this::jobCard).toList(), "No jobs are available right now.");
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
        if (hasApplied(job)) {
            content.add(Box.createVerticalStrut(8));
            content.add(UiFactory.mutedLabel("Status: " + applicationsByJobId.get(job.id()).status().label()));
        }
        content.add(Box.createVerticalStrut(10));

        JPanel tags = UiFactory.flowPanel(java.awt.FlowLayout.LEFT, 8, 0);
        if (!job.tags().isBlank()) {
            for (String tag : job.tags().split("\\|")) {
                if (tag.isBlank()) {
                    continue;
                }
                JLabel chip = UiFactory.mutedLabel("  " + tag.trim() + "  ");
                chip.setOpaque(true);
                chip.setBackground(Theme.SURFACE_MUTED);
                chip.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
                tags.add(chip);
            }
        }
        JobApplication existingApplication = applicationsByJobId.get(job.id());
        if (existingApplication != null) {
            content.add(Box.createVerticalStrut(8));
            content.add(UiFactory.mutedLabel("Applied: " + existingApplication.status().label()));
        }
        content.add(tags);
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
            detailApplicationStatusLabel.setText("Application status: " + application.status().label() + " • submitted " + application.appliedAt().replace('T', ' '));
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
        List<JobApplication> applications = applicationService.findApplicationsForApplicant(currentUser.id());
        rebuildVerticalList(applicationsListPanel, applications.stream()
                .map(this::applicationCard)
                .toList(), "You have not submitted any applications yet.");
    }

    private Component applicationCard(JobApplication application) {
        JPanel card = UiFactory.card();
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 118));
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        String jobTitle = applicationService.findJobForApplication(application)
                .map(JobPosting::title)
                .orElse("Unknown role");
        JLabel title = UiFactory.bodyLabel(jobTitle + " — Status: " + application.status().label());
        title.setFont(Theme.BUTTON_FONT);
        content.add(title);
        content.add(Box.createVerticalStrut(6));
        content.add(UiFactory.mutedLabel("Applied at " + application.appliedAt().replace('T', ' ')));
        if (!application.note().isBlank()) {
            content.add(Box.createVerticalStrut(8));
            content.add(UiFactory.bodyLabel("<html>" + application.note() + "</html>"));
        }
        card.add(content, BorderLayout.CENTER);
        return card;
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
        int selection = chooser.showOpenDialog(this);
        if (selection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            CvService.StoredCv storedCv = cvService.storeCv(chooser.getSelectedFile().toPath());
            currentCvOriginalFileName = storedCv.originalFileName();
            currentCvStoredPath = storedCv.storedPath();
            cvStatusLabel.setText("Current CV: " + currentCvOriginalFileName + " (remember to save profile)");
            JOptionPane.showMessageDialog(this, "CV uploaded successfully.");
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage());
        }
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
