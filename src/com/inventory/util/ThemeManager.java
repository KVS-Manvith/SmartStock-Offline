package com.inventory.util;

import javafx.scene.Scene;

public final class ThemeManager {

    private static boolean darkMode;

    private ThemeManager() {
    }

    public static boolean isDarkMode() {
        return darkMode;
    }

    public static void setDarkMode(boolean enabled) {
        darkMode = enabled;
    }

    public static void applyTheme(Scene scene) {
        if (scene == null || scene.getRoot() == null) {
            return;
        }

        scene.getRoot().getStyleClass().remove("dark-mode");
        if (darkMode) {
            scene.getRoot().getStyleClass().add("dark-mode");
        }
    }
}
