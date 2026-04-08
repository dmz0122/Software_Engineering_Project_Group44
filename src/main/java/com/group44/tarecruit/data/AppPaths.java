package com.group44.tarecruit.data;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class AppPaths {
    private AppPaths() {
    }

    public static Path dataDirectory() {
        return Paths.get("data");
    }
}
