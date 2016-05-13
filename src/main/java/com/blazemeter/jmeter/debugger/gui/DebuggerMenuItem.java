package com.blazemeter.jmeter.debugger.gui;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DebuggerMenuItem extends JMenuItem implements ActionListener {
    private static final Logger log = LoggingManager.getLoggerForClass();
    private static DebuggerDialog dialog;

    public DebuggerMenuItem() {
        super("Plugins Manager", getPluginsIcon());
        addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (dialog == null) {
            dialog = new DebuggerDialog();
        }

        dialog.setVisible(true);
    }

    public static ImageIcon getPluginsIcon() {
        return new ImageIcon(DebuggerMenuItem.class.getResource("/org/jmeterplugins/logo.png"));
    }
}
