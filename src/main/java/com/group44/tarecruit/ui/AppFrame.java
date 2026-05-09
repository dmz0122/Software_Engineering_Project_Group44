package com.group44.tarecruit.ui;

import com.group44.tarecruit.data.AppPaths;
import com.group44.tarecruit.data.ActivityLogRepository;
import com.group44.tarecruit.data.ApplicationRepository;
import com.group44.tarecruit.data.JobRepository;
import com.group44.tarecruit.data.NotificationRepository;
import com.group44.tarecruit.data.ProfileRepository;
import com.group44.tarecruit.data.SeedDataInitializer;
import com.group44.tarecruit.data.UserRepository;
import com.group44.tarecruit.model.Role;
import com.group44.tarecruit.model.UserAccount;
import com.group44.tarecruit.service.ApplicationService;
import com.group44.tarecruit.service.ActivityLogService;
import com.group44.tarecruit.service.AiConfiguration;
import com.group44.tarecruit.service.AiConfigurationLoader;
import com.group44.tarecruit.service.AnalyticsService;
import com.group44.tarecruit.service.AuthService;
import com.group44.tarecruit.service.CvService;
import com.group44.tarecruit.service.DisabledLlmJsonService;
import com.group44.tarecruit.service.JobService;
import com.group44.tarecruit.service.LlmJsonService;
import com.group44.tarecruit.service.NotificationService;
import com.group44.tarecruit.service.OpenAiCompatibleLlmJsonService;
import com.group44.tarecruit.service.ProfileService;
import com.group44.tarecruit.service.WorkloadService;
import com.group44.tarecruit.ui.components.Theme;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.nio.file.Path;

public class AppFrame extends JFrame {
    private static final String LOGIN_CARD = "login";
    private static final String APPLICANT_CARD = "applicant";
    private static final String ORGANISER_CARD = "organiser";
    private static final String ADMIN_CARD = "admin";

    private final AuthService authService;
    private final ProfileService profileService;
    private final JobService jobService;
    private final ApplicationService applicationService;
    private final NotificationService notificationService;
    private final CvService cvService;
    private final WorkloadService workloadService;
    private final AnalyticsService analyticsService;
    private final JPanel rootPanel;
    private final CardLayout cardLayout;
    private final LoginPanel loginPanel;
    private final ApplicantWorkspacePanel applicantWorkspacePanel;
    private final OrganiserWorkspacePanel organiserWorkspacePanel;
    private final AdminWorkspacePanel adminWorkspacePanel;

    private UserAccount currentUser;

    public AppFrame() {
        Path dataDirectory = AppPaths.dataDirectory();
        new SeedDataInitializer(dataDirectory).ensureSeedData();

        UserRepository userRepository = new UserRepository(dataDirectory.resolve("users.csv"));
        ProfileRepository profileRepository = new ProfileRepository(dataDirectory.resolve("profiles.csv"));
        JobRepository jobRepository = new JobRepository(dataDirectory.resolve("jobs.csv"));
        ApplicationRepository applicationRepository = new ApplicationRepository(dataDirectory.resolve("applications.csv"));
        NotificationRepository notificationRepository = new NotificationRepository(dataDirectory.resolve("notifications.csv"));
        ActivityLogRepository activityLogRepository = new ActivityLogRepository(dataDirectory.resolve("activity_logs.csv"));

        ActivityLogService activityLogService = new ActivityLogService(activityLogRepository);
        AiConfiguration aiConfiguration = AiConfigurationLoader.load();
        LlmJsonService llmJsonService = aiConfiguration.isUsable()
                ? new OpenAiCompatibleLlmJsonService(aiConfiguration)
                : new DisabledLlmJsonService();
        notificationService = new NotificationService(notificationRepository, activityLogService);
        authService = new AuthService(userRepository);
        profileService = new ProfileService(profileRepository, activityLogService);
        jobService = new JobService(jobRepository, activityLogService);
        applicationService = new ApplicationService(
                applicationRepository,
                jobRepository,
                profileRepository,
                userRepository,
                notificationService
        );
        cvService = new CvService(dataDirectory.resolve("uploads"));
        workloadService = new WorkloadService(applicationRepository, jobRepository, userRepository);
        analyticsService = new AnalyticsService(
                applicationRepository,
                jobRepository,
                profileRepository,
                userRepository,
                workloadService,
                activityLogService,
                llmJsonService
        );

        setTitle("TA Recruit");
        setSize(1360, 860);
        setMinimumSize(new java.awt.Dimension(1024, 700));
        setResizable(true);
        setLocationByPlatform(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        rootPanel = new JPanel(cardLayout);
        rootPanel.setBackground(Theme.APP_BACKGROUND);

        loginPanel = new LoginPanel(this::attemptLogin, this::attemptRegistration);
        applicantWorkspacePanel = new ApplicantWorkspacePanel(
                profileService,
                jobService,
                applicationService,
                notificationService,
                analyticsService,
                cvService,
                this::openAccountDialog,
                this::logout
        );
        organiserWorkspacePanel = new OrganiserWorkspacePanel(
                applicationService,
                jobService,
                cvService,
                this::openAccountDialog,
                this::logout
        );
        adminWorkspacePanel = new AdminWorkspacePanel(
                workloadService,
                analyticsService,
                this::openAccountDialog,
                this::logout
        );

        rootPanel.add(loginPanel, LOGIN_CARD);
        rootPanel.add(applicantWorkspacePanel, APPLICANT_CARD);
        rootPanel.add(organiserWorkspacePanel, ORGANISER_CARD);
        rootPanel.add(adminWorkspacePanel, ADMIN_CARD);
        setContentPane(rootPanel);
        showCard(LOGIN_CARD);
    }

    private void attemptLogin(LoginPanel.LoginRequest request) {
        authService.login(request.email(), request.password())
                .ifPresentOrElse(this::startSession, loginPanel::loginFailed);
    }

    private void attemptRegistration(LoginPanel.RegistrationRequest request) {
        try {
            UserAccount user = authService.registerApplicant(
                    request.displayName(),
                    request.email(),
                    request.password(),
                    request.confirmPassword()
            );
            JOptionPane.showMessageDialog(this, "Account created successfully. You can now sign in with " + user.email() + ".");
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage());
        }
    }

    private void startSession(UserAccount user) {
        currentUser = user;
        if (user.role() == Role.APPLICANT) {
            applicantWorkspacePanel.setCurrentUser(user);
            applicantWorkspacePanel.refreshAll();
            showCard(APPLICANT_CARD);
        } else if (user.role() == Role.ORGANISER) {
            organiserWorkspacePanel.setCurrentUser(user);
            organiserWorkspacePanel.refreshAll();
            showCard(ORGANISER_CARD);
        } else {
            adminWorkspacePanel.setCurrentUser(user);
            adminWorkspacePanel.refreshAll();
            showCard(ADMIN_CARD);
        }
    }

    private void logout() {
        currentUser = null;
        showCard(LOGIN_CARD);
        JOptionPane.showMessageDialog(this, "You have been signed out.");
    }

    private void openAccountDialog() {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, "Please sign in first.");
            return;
        }

        javax.swing.JPasswordField currentPasswordField = new javax.swing.JPasswordField();
        javax.swing.JPasswordField newPasswordField = new javax.swing.JPasswordField();
        javax.swing.JPasswordField confirmPasswordField = new javax.swing.JPasswordField();
        currentPasswordField.setFont(com.group44.tarecruit.ui.components.Theme.BODY_FONT);
        newPasswordField.setFont(com.group44.tarecruit.ui.components.Theme.BODY_FONT);
        confirmPasswordField.setFont(com.group44.tarecruit.ui.components.Theme.BODY_FONT);

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 12));
        form.setOpaque(false);
        form.add(labelledField("Current password", currentPasswordField));
        form.add(labelledField("New password", newPasswordField));
        form.add(labelledField("Confirm new password", confirmPasswordField));

        int result = JOptionPane.showConfirmDialog(this, form, "Change Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            UserAccount updated = authService.changePassword(
                    currentUser.id(),
                    new String(currentPasswordField.getPassword()),
                    new String(newPasswordField.getPassword()),
                    new String(confirmPasswordField.getPassword())
            );
            currentUser = updated;
            JOptionPane.showMessageDialog(this, "Password updated successfully.");
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage());
        }
    }

    private JPanel labelledField(String label, javax.swing.JComponent field) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 6));
        panel.setOpaque(false);
        panel.add(com.group44.tarecruit.ui.components.UiFactory.bodyLabel(label));
        panel.add(field);
        return panel;
    }

    private void showCard(String card) {
        cardLayout.show(rootPanel, card);
    }
}
