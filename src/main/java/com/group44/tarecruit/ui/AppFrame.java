package com.group44.tarecruit.ui;

import com.group44.tarecruit.ui.components.Theme;

import javax.swing.JFrame;

public class AppFrame extends JFrame {
    public AppFrame() {
        setTitle("TA Recruit");
        setSize(1280, 840);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(Theme.APP_BACKGROUND);
        setContentPane(new LoginPanel(request -> { }));
    }
}
