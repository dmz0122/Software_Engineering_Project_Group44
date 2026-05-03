package com.group44.tarecruit.service;

import com.group44.tarecruit.data.UserRepository;
import com.group44.tarecruit.model.Role;
import com.group44.tarecruit.model.UserAccount;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public class AuthService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<UserAccount> login(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(user -> user.password().equals(password));
    }

    public UserAccount registerApplicant(String displayName, String email, String password, String confirmPassword) {
        String trimmedName = displayName == null ? "" : displayName.trim();
        String trimmedEmail = email == null ? "" : email.trim().toLowerCase();
        validateCredentials(trimmedName, trimmedEmail, password, confirmPassword);
        if (userRepository.findByEmail(trimmedEmail).isPresent()) {
            throw new IllegalArgumentException("This email is already registered.");
        }

        UserAccount userAccount = new UserAccount(
                "ta-" + UUID.randomUUID().toString().substring(0, 8),
                Role.APPLICANT,
                trimmedName,
                trimmedEmail,
                password
        );
        userRepository.upsert(userAccount);
        return userAccount;
    }

    public UserAccount changePassword(String userId, String currentPassword, String newPassword, String confirmPassword) {
        UserAccount existing = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found."));
        if (!existing.password().equals(currentPassword)) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must contain at least 6 characters.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("New passwords do not match.");
        }

        UserAccount updated = new UserAccount(
                existing.id(),
                existing.role(),
                existing.displayName(),
                existing.email(),
                newPassword
        );
        userRepository.upsert(updated);
        return updated;
    }

    private void validateCredentials(String displayName, String email, String password, String confirmPassword) {
        if (displayName.isBlank() || email.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Name, email and password are required.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must contain at least 6 characters.");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match.");
        }
    }
}
