package com.blazemeter.jmeter.debugger.gui;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.MainFrame;
import org.apache.jmeter.gui.util.JMeterToolBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DebuggerMenuItem extends JMenuItem implements ActionListener {
    private static final Logger log = LoggerFactory.getLogger(DebuggerMenuItem.class);

    private static DebuggerDialog dialog;

    public DebuggerMenuItem() {
        super("Step-by-Step Debugger", getBugIcon(false));
        addActionListener(this);
        addToolbarIcon();
    }

    private void addToolbarIcon() {
        GuiPackage instance = GuiPackage.getInstance();
        if (instance != null) {
            final MainFrame mf = instance.getMainFrame();
            final ComponentFinder<JMeterToolBar> finder = new ComponentFinder<>(JMeterToolBar.class);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JMeterToolBar toolbar = null;
                    while (toolbar == null) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            log.debug("Did not add btn to toolbar", e);
                        }
                        log.debug("Searching for toolbar");
                        toolbar = finder.findComponentIn(mf);
                    }

                    int pos = 21;
                    Component toolbarButton = getToolbarButton();
                    toolbarButton.setSize(toolbar.getComponent(pos).getSize());
                    toolbar.add(toolbarButton, pos);
                }
            });
        }
    }

    private Component getToolbarButton() {
        JButton button = new JButton(getBugIcon(true));
        button.setToolTipText("Step-by-step Debugger");
        //button.setPressedIcon(new ImageIcon(imageURLPressed));
        button.addActionListener(this);
        //button.setActionCommand(iconBean.getActionNameResolve());
        return button;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (dialog == null) {
            dialog = new DebuggerDialog();
        }

        dialog.pack();

        dialog.setVisible(true);
    }

    // many from http://www.veryicon.com/icons/system/fugue/
    public static ImageIcon getBugIcon(boolean large) {
        if (large) {
            return new ImageIcon(DebuggerMenuItem.class.getResource("/com/blazemeter/jmeter/debugger/bug22.png"));
        } else {
            return new ImageIcon(DebuggerMenuItem.class.getResource("/com/blazemeter/jmeter/debugger/bug.png"));
        }
    }

    public static ImageIcon getStartIcon() {
        return new ImageIcon(DebuggerMenuItem.class.getResource("/com/blazemeter/jmeter/debugger/start.png"));
    }

    public static ImageIcon getStopIcon() {
        return new ImageIcon(DebuggerMenuItem.class.getResource("/com/blazemeter/jmeter/debugger/stop.png"));
    }

    public static ImageIcon getStepIcon() {
        return new ImageIcon(DebuggerMenuItem.class.getResource("/com/blazemeter/jmeter/debugger/step.png"));
    }

    public static ImageIcon getLogoIcon() {
        return new ImageIcon(DebuggerMenuItem.class.getResource("/com/blazemeter/jmeter/debugger/logo.png"));
    }

    public static ImageIcon getBPIcon() {
        return new ImageIcon(DebuggerMenuItem.class.getResource("/com/blazemeter/jmeter/debugger/breakpoint.png"));
    }

    public static ImageIcon getContinueIcon() {
        return new ImageIcon(DebuggerMenuItem.class.getResource("/com/blazemeter/jmeter/debugger/continue.png"));
    }

    public static ImageIcon getPauseIcon() {
        return new ImageIcon(DebuggerMenuItem.class.getResource("/com/blazemeter/jmeter/debugger/pause.png"));
    }

    public static Icon getHelpIcon() {
        return new ImageIcon(DebuggerMenuItem.class.getResource("/com/blazemeter/jmeter/debugger/help.png"));
    }

}
