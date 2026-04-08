package com.group44.tarecruit.data;

import com.group44.tarecruit.model.Role;
import com.group44.tarecruit.model.UserAccount;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {
    private static final List<String> HEADER = List.of("id", "role", "displayName", "email", "password");

    private final Path filePath;

    public UserRepository(Path filePath) {
        this.filePath = filePath;
    }

    public List<UserAccount> findAll() {
        List<List<String>> rows = CsvUtils.read(filePath);
        if (rows.isEmpty()) {
            return List.of();
        }
        List<UserAccount> users = new ArrayList<>();
        for (int index = 1; index < rows.size(); index++) {
            List<String> row = rows.get(index);
            users.add(new UserAccount(
                    valueAt(row, 0),
                    Role.valueOf(valueAt(row, 1)),
                    valueAt(row, 2),
                    valueAt(row, 3),
                    valueAt(row, 4)
            ));
        }
        return users;
    }

    public Optional<UserAccount> findByEmail(String email) {
        return findAll().stream()
                .filter(user -> user.email().equalsIgnoreCase(email))
                .findFirst();
    }

    public void saveAll(List<UserAccount> users) {
        List<List<String>> rows = users.stream()
                .map(user -> List.of(
                        user.id(),
                        user.role().name(),
                        user.displayName(),
                        user.email(),
                        user.password()
                ))
                .toList();
        CsvUtils.write(filePath, HEADER, rows);
    }

    private String valueAt(List<String> row, int index) {
        return index < row.size() ? row.get(index) : "";
    }
}
