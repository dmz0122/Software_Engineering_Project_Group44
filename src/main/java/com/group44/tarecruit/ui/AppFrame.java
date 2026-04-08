package com.group44.tarecruit.ui;

import com.group44.tarecruit.data.AppPaths;
import com.group44.tarecruit.data.ApplicationRepository;
import com.group44.tarecruit.data.JobRepository;
import com.group44.tarecruit.data.NotificationRepository;
import com.group44.tarecruit.data.ProfileRepository;
import com.group44.tarecruit.data.SeedDataInitializer;
import com.group44.tarecruit.data.UserRepository;
import com.group44.tarecruit.model.Role;
import com.group44.tarecruit.model.UserAccount;
import com.group44.tarecruit.service.ApplicationService;
import com.group44.tarecruit.service.AuthService;
import com.group44.tarecruit.service.CvService;
import com.group44.tarecruit.service.JobService;
import com.group44.tarecruit.service.NotificationService;
import com.group44.tarecruit.service.ProfileService;
import com.group44.tarecruit.ui.components.Theme;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.nio.file.Path;

public class AppFrame extends JFrame {
    private static final String LOGIN_CARD = "login";
    private static final String APPLICANT_CARD = "applicant";

    private final AuthService authService;
    private final ProfileService profileService;
    private final JobService jobService;
    private final ApplicationService applicationService;
    private final NotificationService notificationService;
    private final CvService cvService;
    private final JPanel rootPanel;
    private final CardLayout cardLayout;
    private final LoginPanel loginPanel;
    private final ApplicantWorkspacePanel applicantWorkspacePanel;

    private UserAccount currentUser;

    public AppFrame() {
        Path dataDirectory = AppPaths.dataDirectory();
        new SeedDataInitializer(dataDirectory).ensureSeedData();

        UserRepository userRepository = new UserRepository(dataDirectory.resolve("users.csv"));
        ProfileRepository profileRepository = new ProfileRepository(dataDirectory.resolve("profiles.csv"));
        JobRepository jobRepository = new JobRepository(dataDirectory.resolve("jobs.csv"));
        ApplicationRepository applicationRepository = new ApplicationRepository(dataDirectory.resolve("applications.csv"));
        NotificationRepository notificationRepository = new NotificationRepository(dataDirectory.resolve("notifications.csv"));

        notificationService = new NotificationService(notificationRepository);
        authService = new AuthService(userRepository);
        profileService = new ProfileService(profileRepository);
        jobService = new JobService(jobRepository);
        applicationService = new ApplicationService(
                applicationRepository,
                jobRepository,
                profileRepository,
                userRepository,
                notificationService
        );
        cvService = new CvService(dataDirectory.resolve("uploads"));

        setTitle("TA Recruit");
        setSize(1360, 860);
        setMinimumSize(new java.awt.Dimension(1200, 760));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        rootPanel = new JPanel(cardLayout);
        rootPanel.setBackground(Theme.APP_BACKGROUND);

        loginPanel = new LoginPanel(this::attemptLogin);
        applicantWorkspacePanel = new ApplicantWorkspacePanel(
                profileService,
                jobService,
                applicationService,
                notificationService,
                cvService,
                this::logout
        );

        rootPanel.add(loginPanel, LOGIN_CARD);
        rootPanel.add(applicantWorkspacePanel, APPLICANT_CARD);
        setContentPane(rootPanel);
        showCard(LOGIN_CARD);
    }

    private void attemptLogin(LoginPanel.LoginRequest request) {
        authService.login(request.email(), request.password())
                .ifPresentOrElse(this::startSession, loginPanel::loginFailed);
    }

    private void startSession(UserAccount user) {
        currentUser = user;
        if (user.role() == Role.APPLICANT) {
            applicantWorkspacePanel.setCurrentUser(user);
            applicantWorkspacePanel.refreshAll();
            showCard(APPLICANT_CARD);
            return;
        }
        JOptionPane.showMessageDialog(this, "Organiser wiring lands in the next integration commit.");
    }

    private void logout() {
        currentUser = null;
        showCard(LOGIN_CARD);
        JOptionPane.showMessageDialog(this, "You have been signed out.");
    }

    private void showCard(String card) {
        cardLayout.show(rootPanel, card);
    }
}
