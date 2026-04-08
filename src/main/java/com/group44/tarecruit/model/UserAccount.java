package com.group44.tarecruit.model;

public record UserAccount(
        String id,
        Role role,
        String displayName,
        String email,
        String password
) {
}
