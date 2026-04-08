package com.group44.tarecruit.service;

import com.group44.tarecruit.data.UserRepository;
import com.group44.tarecruit.model.UserAccount;

import java.util.Optional;

public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<UserAccount> login(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(user -> user.password().equals(password));
    }
}
