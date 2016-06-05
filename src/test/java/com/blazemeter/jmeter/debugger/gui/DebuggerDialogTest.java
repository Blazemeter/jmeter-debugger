package com.blazemeter.jmeter.debugger.gui;

import com.blazemeter.jmeter.debugger.elements.Wrapper;
import com.blazemeter.jmeter.debugger.engine.*;
import kg.apc.emulators.TestJMeterUtils;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.functions.TimeFunction;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.MainFrame;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.RenderAsHTML;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.LogTarget;
import org.apache.log.Logger;
import org.apache.log.format.PatternFormatter;
import org.apache.log.output.io.WriterTarget;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;


public class DebuggerDialogTest implements TestTreeProvider {
    private static final Logger log = LoggingManager.getLoggerForClass();

    @BeforeClass
    public static void setUp() {
        PrintWriter writer = new PrintWriter(System.out, true);
        LoggingManager.addLogTargetToRootLogger(new LogTarget[]{new WriterTarget(writer, new PatternFormatter(LoggingManager.DEFAULT_PATTERN))});
        Properties props = new Properties();
        props.setProperty(LoggingManager.LOG_FILE, "");
        LoggingManager.initializeLogging(props);
    }

    @Test
    public void displayGUI() throws InterruptedException, IOException {
        if (!GraphicsEnvironment.getLocalGraphicsEnvironment().isHeadlessInstance()) {
            TestJMeterUtils.createJmeterEnv();

            JMeterTreeModel mdl = new JMeterTreeModel();
            JMeterTreeListener a = new JMeterTreeListener();
            a.setActionHandler(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    log.debug("Action " + actionEvent);
                }
            });
            a.setModel(mdl);
            try {
                mdl.addSubTree(getTestTree(), (JMeterTreeNode) mdl.getRoot());
            } catch (IllegalUserActionException e) {
                throw new RuntimeException(e);
            }

            GuiPackage.getInstance(a, mdl);
            String actions = ActionRouter.class.getProtectionDomain().getCodeSource().getLocation().getFile();
            String renderers = RenderAsHTML.class.getProtectionDomain().getCodeSource().getLocation().getFile();
            JMeterUtils.setProperty("search_paths", actions + ";" + renderers);
            MainFrame mf = new MainFrame(mdl, a);

            new TimeFunction();
            long now = System.currentTimeMillis();
            JMeterUtils.setProperty("START.MS", Long.toString(now));
            Date today = new Date(now);
            JMeterUtils.setProperty("START.YMD", new SimpleDateFormat("yyyyMMdd").format(today));
            JMeterUtils.setProperty("START.HMS", new SimpleDateFormat("HHmmss").format(today));

            DebuggerDialogMock frame = new DebuggerDialogMock(mdl);

            frame.setPreferredSize(new Dimension(800, 600));
            frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
            Thread.sleep(60000);
        }
    }

    @Override
    public HashTree getTestTree() {
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
        private final JMeterTreeModel mdl;

        public DebuggerDialogMock(JMeterTreeModel b) {
            mdl = b;
        }

        @Override
        public HashTree getTestTree() {
            return mdl.getTestPlan();
        }
    }

    @Test
    public void runRealEngine() throws Exception {
        TestJMeterUtils.createJmeterEnv();

        HashTree hashTree = getTestTree();
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

        JMeterTreeModel mdl = new JMeterTreeModel();
        mdl.addSubTree(getTestTree(), (JMeterTreeNode) mdl.getRoot());

        Debugger sel = new Debugger(this, new FrontendMock());
        HashTree hashTree = sel.getSelectedTree();
        JMeter.convertSubTree(hashTree);

        DebuggerEngine engine = new DebuggerEngine(JMeterContextService.getContext());
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
        public void notify(Wrapper t) {
            log.warn("Stop before: " + t.getWrappedElement());
            cnt += 1;
        }
    }

    private class FrontendMock implements DebuggerFrontend {
        @Override
        public void started() {

        }

        @Override
        public void stopped() {

        }

        @Override
        public void continuing() {

        }

        @Override
        public void frozenAt(Wrapper wrapper) {

        }

        @Override
        public void statusRefresh(JMeterContext context) {

        }
    }
}