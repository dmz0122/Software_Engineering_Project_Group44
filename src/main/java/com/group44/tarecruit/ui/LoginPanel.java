package com.group44.tarecruit.ui;

import com.group44.tarecruit.model.UserAccount;
import com.group44.tarecruit.ui.components.Theme;
import com.group44.tarecruit.ui.components.UiFactory;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.function.Consumer;

public class LoginPanel extends JPanel {
    private final Consumer<LoginRequest> loginHandler;
    private final JComponent emailField;
    private final JPasswordField passwordField;

    public LoginPanel(Consumer<LoginRequest> loginHandler) {
        this.loginHandler = loginHandler;
        this.emailField = UiFactory.textField();
        this.passwordField = new JPasswordField();
        passwordField.setFont(Theme.BODY_FONT);
        passwordField.setBorder(((JComponent) emailField).getBorder());

        setLayout(new BorderLayout());
        setBackground(Theme.APP_BACKGROUND);
        add(buildHeroPanel(), BorderLayout.WEST);
        add(buildFormPanel(), BorderLayout.CENTER);
    }

    private JPanel buildHeroPanel() {
        JPanel hero = new JPanel();
        hero.setBackground(Theme.PRIMARY_DARK);
        hero.setPreferredSize(new Dimension(420, 720));
        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
        hero.setBorder(javax.swing.BorderFactory.createEmptyBorder(80, 48, 80, 48));

        JLabel brand = UiFactory.titleLabel("TA Recruit");
        brand.setForeground(Theme.SURFACE);
        JLabel subtitle = UiFactory.sectionLabel("Sprint 2 Demo");
        subtitle.setForeground(new java.awt.Color(220, 232, 255));
        JLabel detail = UiFactory.bodyLabel("<html>Applicant applications, organiser vacancy publishing and admin workload monitoring now run locally with CSV persistence.</html>");
        detail.setForeground(new java.awt.Color(220, 232, 255));

        hero.add(brand);
        hero.add(Box.createVerticalStrut(16));
        hero.add(subtitle);
        hero.add(Box.createVerticalStrut(20));
        hero.add(detail);
        hero.add(Box.createVerticalStrut(30));
        hero.add(accountHint("Applicant demo", "newta@school.edu / password123"));
        hero.add(Box.createVerticalStrut(8));
        hero.add(accountHint("Existing applicants", "amy@school.edu / password123"));
        hero.add(Box.createVerticalStrut(8));
        hero.add(accountHint("Module organiser", "mo@school.edu / password123"));
        hero.add(Box.createVerticalStrut(8));
        hero.add(accountHint("Admin", "admin@school.edu / password123"));
        hero.add(Box.createVerticalGlue());
        return hero;
    }

    private JPanel accountHint(String title, String description) {
        JPanel panel = UiFactory.card();
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
        panel.setBackground(new java.awt.Color(245, 248, 255));
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JLabel titleLabel = UiFactory.bodyLabel(title);
        titleLabel.setFont(Theme.BUTTON_FONT);
        JLabel descriptionLabel = UiFactory.mutedLabel(description);
        content.add(titleLabel);
        content.add(Box.createVerticalStrut(6));
        content.add(descriptionLabel);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildFormPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(javax.swing.BorderFactory.createEmptyBorder(60, 60, 60, 80));

        JPanel card = UiFactory.card();
        card.setPreferredSize(new Dimension(520, 520));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(UiFactory.titleLabel("Login"));
        content.add(Box.createVerticalStrut(10));
        content.add(UiFactory.mutedLabel("Use a demo account or enter the seeded credentials manually."));
        content.add(Box.createVerticalStrut(24));
        content.add(labeledField("Email", emailField));
        content.add(Box.createVerticalStrut(16));
        content.add(labeledField("Password", passwordField));
        content.add(Box.createVerticalStrut(24));

        JPanel buttonRow = new JPanel(new GridLayout(1, 2, 12, 0));
        buttonRow.setOpaque(false);
        JButton loginButton = UiFactory.primaryButton("Login");
        loginButton.addActionListener(event -> submitLogin());
        JButton clearButton = UiFactory.lightButton("Clear");
        clearButton.addActionListener(event -> {
            ((javax.swing.JTextField) emailField).setText("");
            passwordField.setText("");
        });
        buttonRow.add(loginButton);
        buttonRow.add(clearButton);
        content.add(buttonRow);
        content.add(Box.createVerticalStrut(24));

        JPanel quickLoginRow = new JPanel(new GridLayout(1, 3, 12, 0));
        quickLoginRow.setOpaque(false);
        JButton applicantDemo = UiFactory.secondaryButton("Use Applicant Demo");
        applicantDemo.addActionListener(event -> fillCredentials("newta@school.edu", "password123"));
        JButton organiserDemo = UiFactory.secondaryButton("Use MO Demo");
        organiserDemo.addActionListener(event -> fillCredentials("mo@school.edu", "password123"));
        JButton adminDemo = UiFactory.secondaryButton("Use Admin Demo");
        adminDemo.addActionListener(event -> fillCredentials("admin@school.edu", "password123"));
        quickLoginRow.add(applicantDemo);
        quickLoginRow.add(organiserDemo);
        quickLoginRow.add(adminDemo);
        content.add(quickLoginRow);

        card.add(content, BorderLayout.CENTER);
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel labeledField(String labelText, JComponent field) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel label = UiFactory.bodyLabel(labelText);
        label.setFont(Theme.BUTTON_FONT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(8));
        UiFactory.fixedHeight(field, 44);
        panel.add(field);
        return panel;
    }

    private void fillCredentials(String email, String password) {
        ((javax.swing.JTextField) emailField).setText(email);
        passwordField.setText(password);
    }

    private void submitLogin() {
        String email = ((javax.swing.JTextField) emailField).getText().trim();
        String password = new String(passwordField.getPassword());
        if (email.isBlank() || password.isBlank()) {
            JOptionPane.showMessageDialog(this, "Please enter both email and password.");
            return;
        }
        loginHandler.accept(new LoginRequest(email, password));
    }

    public void loginFailed() {
        JOptionPane.showMessageDialog(this, "Invalid email or password. Please try one of the seeded accounts.");
    }

    public record LoginRequest(String email, String password) {
    }
}
