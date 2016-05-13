package com.blazemeter.jmeter.debugger.gui;

import kg.apc.emulators.TestJMeterUtils;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;


public class DebuggerDialogTest {
    @Test
    public void displayGUI() throws InterruptedException {
        if (!GraphicsEnvironment.getLocalGraphicsEnvironment().isHeadlessInstance()) {
            TestJMeterUtils.createJmeterEnv();
            JDialog frame = new DebuggerDialog();

            frame.setPreferredSize(new Dimension(800, 600));
            frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
            Thread.sleep(60000);
        }
    }
}