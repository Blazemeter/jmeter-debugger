package com.blazemeter.jmeter.debugger.gui;

import kg.apc.emulators.TestJMeterUtils;
import org.apache.jmeter.save.SaveService;
import org.apache.jorphan.collections.HashTree;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;


public class DebuggerDialogTest {
    @Test
    public void displayGUI() throws InterruptedException {
        if (!GraphicsEnvironment.getLocalGraphicsEnvironment().isHeadlessInstance()) {
            TestJMeterUtils.createJmeterEnv();
            JDialog frame = new DebuggerDialogMock();

            frame.setPreferredSize(new Dimension(800, 600));
            frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
            Thread.sleep(60000);
        }
    }

    private class DebuggerDialogMock extends DebuggerDialog {
        @Override
        protected HashTree getTestTree() {
            File file = new File(this.getClass().getResource("/com/blazemeter/jmeter/debugger/sample1.jmx").getFile());
            String basedir = TestJMeterUtils.fixWinPath(file.getParentFile().getAbsolutePath());

            File f = new File(basedir + "/sample1.jmx");
            try {
                return SaveService.loadTree(f);
            } catch (IOException e) {
                throw new RuntimeException("Failed", e);
            }
        }
    }
}