package com.blazemeter.jmeter.debugger.gui;

import org.apache.jorphan.gui.ComponentUtil;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class DebuggerDialog extends JDialog implements ComponentListener {
    private static final Logger log = LoggingManager.getLoggerForClass();
    public static final Border SPACING = BorderFactory.createEmptyBorder(5, 5, 5, 5);


    public DebuggerDialog() {
        super((JFrame) null, "Step-by-Step Debugger", true);
        setLayout(new BorderLayout());
        setSize(new Dimension(800, 600));
        setIconImage(DebuggerMenuItem.getPluginsIcon().getImage());
        ComponentUtil.centerComponentInWindow(this, 30);
        addComponentListener(this);
    }

    @Override
    public void componentResized(ComponentEvent e) {

    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {
        log.debug("Showing dialog");
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        log.debug("Closing dialog");
    }
}
