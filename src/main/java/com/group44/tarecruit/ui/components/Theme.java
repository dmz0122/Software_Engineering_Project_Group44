package com.group44.tarecruit.ui.components;

import javax.swing.BorderFactory;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Font;

public final class Theme {
    public static final Color APP_BACKGROUND = new Color(241, 245, 252);
    public static final Color SURFACE = Color.WHITE;
    public static final Color SURFACE_MUTED = new Color(232, 238, 248);
    public static final Color PRIMARY = new Color(62, 123, 236);
    public static final Color PRIMARY_DARK = new Color(34, 71, 136);
    public static final Color TEXT = new Color(28, 39, 59);
    public static final Color SUBTLE_TEXT = new Color(102, 117, 144);
    public static final Color SUCCESS = new Color(39, 174, 96);
    public static final Color WARNING = new Color(243, 156, 18);
    public static final Color BORDER = new Color(210, 220, 235);

    public static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 32);
    public static final Font SECTION_FONT = new Font("SansSerif", Font.BOLD, 22);
    public static final Font BODY_FONT = new Font("SansSerif", Font.PLAIN, 15);
    public static final Font SMALL_FONT = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 15);

    private Theme() {
    }

    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        );
    }
}
