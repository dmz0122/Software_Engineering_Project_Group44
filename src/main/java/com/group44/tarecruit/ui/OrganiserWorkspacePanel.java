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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
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
    }

    public void setCurrentUser(UserAccount currentUser) {
        this.currentUser = currentUser;
    }

    public void refreshAll() {
        refreshJobSelector(null);
        refreshApplicantList();
        showPage(APPLICANTS_PAGE);
        updatePreviewFromForm();
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

    private JPanel buildPostJobPage() {
        JPanel page = pageWrapper();
        page.add(UiFactory.titleLabel("Post Job (MO)"));
        page.add(Box.createVerticalStrut(8));
        page.add(UiFactory.mutedLabel("Create a new vacancy with the role, module details, required skills and workload information."));
        page.add(Box.createVerticalStrut(24));

        JPanel grid = new JPanel(new GridLayout(1, 2, 20, 0));
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

    private JPanel buildPostJobFormCard() {
        JPanel card = UiFactory.card();

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JPanel fieldsGrid = new JPanel(new GridLayout(0, 2, 18, 16));
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

        JPanel actions = new JPanel(new GridLayout(1, 2, 12, 0));
        actions.setOpaque(false);
        JButton previewButton = UiFactory.secondaryButton("Preview");
        previewButton.addActionListener(event -> updatePreviewFromForm());
        JButton clearButton = UiFactory.lightButton("Clear");
        clearButton.addActionListener(event -> {
            clearPostJobForm();
            updatePreviewFromForm();
        });
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

    private void refreshJobSelector(String selectedJobId) {
        jobSelector.removeAllItems();
        for (JobPosting job : jobService.getAllJobs()) {
            jobSelector.addItem(job);
        }
        if (selectedJobId != null) {
            for (int index = 0; index < jobSelector.getItemCount(); index++) {
                JobPosting job = jobSelector.getItemAt(index);
                if (job.id().equals(selectedJobId)) {
                    jobSelector.setSelectedIndex(index);
                    return;
                }
            }
        }
        if (jobSelector.getItemCount() > 0) {
            jobSelector.setSelectedIndex(0);
        }
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