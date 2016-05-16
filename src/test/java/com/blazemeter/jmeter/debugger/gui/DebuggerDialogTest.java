package com.blazemeter.jmeter.debugger.gui;

import com.blazemeter.jmeter.debugger.elements.AbstractDebugElement;
import com.blazemeter.jmeter.debugger.engine.DebuggerEngine;
import com.blazemeter.jmeter.debugger.engine.StepTrigger;
import com.blazemeter.jmeter.debugger.engine.ThreadGroupSelector;
import kg.apc.emulators.TestJMeterUtils;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.save.SaveService;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.LogTarget;
import org.apache.log.Logger;
import org.apache.log.format.SyslogFormatter;
import org.apache.log.output.io.WriterTarget;
import org.junit.Assert;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;


public class DebuggerDialogTest {
    private static final Logger log = LoggingManager.getLoggerForClass();

    @Test
    public void displayGUI() throws InterruptedException, IOException {
        PrintWriter writer = new PrintWriter(System.out);
        LoggingManager.addLogTargetToRootLogger(new LogTarget[]{new WriterTarget(writer, new SyslogFormatter())});
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

    private HashTree getHashTree() {
        File file = new File(this.getClass().getResource("/com/blazemeter/jmeter/debugger/sample1.jmx").getFile());
        String basedir = TestJMeterUtils.fixWinPath(file.getParentFile().getAbsolutePath());

        File f = new File(basedir + "/sample1.jmx");
        try {
            return SaveService.loadTree(f);
        } catch (IOException e) {
            throw new RuntimeException("Failed", e);
        }
    }

    private class DebuggerDialogMock extends DebuggerDialog {
        @Override
        protected HashTree getTestTree() {
            return getHashTree();
        }

        public JMeterTreeListener getTreeListener() {
            return treeListener;
        }

        public JMeterTreeModel getTreeModel() {
            return (JMeterTreeModel) tree.getModel();
        }
    }

    @Test
    public void runRealEngine() throws Exception {
        TestJMeterUtils.createJmeterEnv();

        HashTree hashTree = getHashTree();
        JMeter.convertSubTree(hashTree);

        StandardJMeterEngine engine = new StandardJMeterEngine();
        engine.configure(hashTree);
        engine.runTest();
        while (engine.isActive()) {
            Thread.sleep(1000);
        }
    }

    @Test
    public void runDebugEngine() throws Exception {
        TestJMeterUtils.createJmeterEnv();

        ThreadGroupSelector sel = new ThreadGroupSelector(getHashTree());
        HashTree hashTree = sel.getSelectedTree();
        JMeter.convertSubTree(hashTree);

        DebuggerEngine engine = new DebuggerEngine();
        StepTriggerCounter hook = new StepTriggerCounter();
        engine.setStepper(hook);
        engine.configure(hashTree);
        engine.runTest();
        while (engine.isActive()) {
            Thread.sleep(1000);
        }
        Assert.assertEquals(32, hook.cnt);
    }

    private class StepTriggerCounter implements StepTrigger {
        public int cnt;

        @Override
        public void notify(AbstractDebugElement t) {
            log.warn("Stop before: " + t.getWrappedElement());
            cnt += 1;
        }
    }
}