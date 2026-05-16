package com.group44.tarecruit.ui.components;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;

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
        return button(text, Theme.ACCENT, Color.WHITE);
    }

    public static JButton lightButton(String text) {
        return button(text, Theme.SURFACE_MUTED, Theme.TEXT);
    }

    public static JButton navButton(String text) {
        JButton button = button(text, Theme.SURFACE_MUTED, Theme.TEXT);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setPreferredSize(new Dimension(148, 34));
        return button;
    }

    private static JButton button(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setFont(Theme.BUTTON_FONT);
        button.setFocusPainted(false);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        return button;
    }

    public static JTextField textField() {
        JTextField field = new JTextField();
        field.setFont(Theme.BODY_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(7, 9, 7, 9)
        ));
        return field;
    }

    public static JTextField numericTextField(int maxLength) {
        JTextField field = textField();
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                replace(fb, offset, 0, string, attr);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text == null) {
                    return;
                }
                String candidate = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()))
                        .replace(offset, offset + length, text)
                        .toString();
                if (candidate.length() <= maxLength && candidate.matches("\\d*")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
        return field;
    }

    public static JTextArea textArea(int rows) {
        JTextArea area = new JTextArea(rows, 20);
        area.setFont(Theme.BODY_FONT);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(7, 9, 7, 9));
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
        Component view = component instanceof Scrollable ? component : new ViewportWidthPanel(component);
        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(Theme.APP_BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    public static void fixedHeight(JComponent component, int height) {
        Dimension preferred = component.getPreferredSize();
        component.setPreferredSize(new Dimension(preferred.width, height));
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
    }

    private static final class ViewportWidthPanel extends JPanel implements Scrollable {
        private ViewportWidthPanel(Component component) {
            super(new BorderLayout());
            setOpaque(false);
            add(component, BorderLayout.CENTER);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(64, visibleRect.height - 32);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}
