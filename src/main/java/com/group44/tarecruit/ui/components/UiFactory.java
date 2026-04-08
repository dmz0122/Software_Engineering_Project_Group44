package com.group44.tarecruit.ui.components;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

public final class UiFactory {
    private UiFactory() {
    }

    public static JLabel titleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.TITLE_FONT);
        label.setForeground(Theme.TEXT);
        return label;
    }

    public static JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.SECTION_FONT);
        label.setForeground(Theme.TEXT);
        return label;
    }

    public static JLabel bodyLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.BODY_FONT);
        label.setForeground(Theme.TEXT);
        return label;
    }

    public static JLabel mutedLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.SMALL_FONT);
        label.setForeground(Theme.SUBTLE_TEXT);
        return label;
    }

    public static JButton primaryButton(String text) {
        return button(text, Theme.PRIMARY, Color.WHITE);
    }

    public static JButton secondaryButton(String text) {
        return button(text, Theme.PRIMARY_DARK, Color.WHITE);
    }

    public static JButton lightButton(String text) {
        return button(text, Theme.SURFACE_MUTED, Theme.TEXT);
    }

    public static JButton navButton(String text) {
        JButton button = button(text, Theme.SURFACE_MUTED, Theme.TEXT);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setPreferredSize(new Dimension(190, 48));
        return button;
    }

    private static JButton button(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setFont(Theme.BUTTON_FONT);
        button.setFocusPainted(false);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        button.setOpaque(true);
        return button;
    }

    public static JTextField textField() {
        JTextField field = new JTextField();
        field.setFont(Theme.BODY_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        return field;
    }

    public static JTextArea textArea(int rows) {
        JTextArea area = new JTextArea(rows, 20);
        area.setFont(Theme.BODY_FONT);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        return area;
    }

    public static JPanel card() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.SURFACE);
        panel.setBorder(Theme.cardBorder());
        return panel;
    }

    public static JPanel flowPanel(int align, int hgap, int vgap) {
        JPanel panel = new JPanel(new FlowLayout(align, hgap, vgap));
        panel.setOpaque(false);
        return panel;
    }

    public static JScrollPane scrollPane(Component component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Theme.APP_BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    public static void fixedHeight(JComponent component, int height) {
        Dimension preferred = component.getPreferredSize();
        component.setPreferredSize(new Dimension(preferred.width, height));
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
    }
}
