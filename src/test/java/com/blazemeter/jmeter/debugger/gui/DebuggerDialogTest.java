package com.blazemeter.jmeter.debugger.gui;

import kg.apc.emulators.TestJMeterUtils;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.MainFrame;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
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
            DebuggerDialogMock frame = new DebuggerDialogMock();
            //GuiPackage.getInstance(frame.getTreeListener(), frame.getTreeModel());
            //GuiPackage.getInstance().setMainFrame(new MainFrame(frame.getTreeModel(), frame.getTreeListener()));

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

        public JMeterTreeListener getTreeListener() {
            return treeListener;
        }

        public JMeterTreeModel getTreeModel() {
            return (JMeterTreeModel) tree.getModel();
        }
    }
}