package com.group44.tarecruit.ui.components;

import javax.swing.BorderFactory;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Font;

public final class Theme {
    public static final Color APP_BACKGROUND = new Color(235, 241, 248);
    public static final Color SURFACE = Color.WHITE;
    public static final Color SURFACE_MUTED = new Color(226, 236, 245);
    public static final Color PRIMARY = new Color(18, 118, 113);
    public static final Color PRIMARY_DARK = new Color(19, 54, 86);
    public static final Color ACCENT = new Color(229, 139, 68);
    public static final Color TEXT = new Color(26, 37, 54);
    public static final Color SUBTLE_TEXT = new Color(91, 108, 130);
    public static final Color SUCCESS = new Color(35, 151, 103);
    public static final Color WARNING = new Color(229, 139, 68);
    public static final Color BORDER = new Color(199, 213, 229);

    public static final Font TITLE_FONT = new Font("Segoe UI Variable Display", Font.BOLD, 34);
    public static final Font SECTION_FONT = new Font("Segoe UI Variable Display", Font.BOLD, 22);
    public static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 15);
    public static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 15);

    private Theme() {
    }

    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(22, 22, 22, 22)
        );
    }
}
