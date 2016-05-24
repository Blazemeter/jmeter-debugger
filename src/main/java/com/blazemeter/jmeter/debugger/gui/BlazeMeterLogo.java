package com.blazemeter.jmeter.debugger.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


public class BlazeMeterLogo extends JLabel {
    public BlazeMeterLogo() {
        super(DebuggerMenuItem.getLogoIcon());
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (java.awt.Desktop.isDesktopSupported()) {
                    try {
                        java.awt.Desktop.getDesktop().browse(new URI("https://blazemeter.com/"));
                    } catch (IOException | URISyntaxException ignored) {
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {

            }
        });
    }
}
