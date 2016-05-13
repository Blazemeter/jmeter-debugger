package com.blazemeter.jmeter.debugger.gui;

import org.apache.jorphan.gui.ComponentUtil;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class DebuggerDialog extends JDialog {
    private static final Logger log = LoggingManager.getLoggerForClass();
    public static final Border SPACING = BorderFactory.createEmptyBorder(5, 5, 5, 5);


    public DebuggerDialog() {
        super((JFrame) null, "Step-by-Step Debugger", true);
        setLayout(new BorderLayout());
        setSize(new Dimension(800, 600));
        setIconImage(DebuggerMenuItem.getPluginsIcon().getImage());
        ComponentUtil.centerComponentInWindow(this, 30);

    }

}
