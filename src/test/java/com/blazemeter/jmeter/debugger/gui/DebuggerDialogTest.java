package com.blazemeter.jmeter.debugger.gui;

import com.blazemeter.jmeter.debugger.TestProvider;
import com.blazemeter.jmeter.debugger.elements.SamplerDebug;
import kg.apc.emulators.TestJMeterUtils;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.functions.TimeFunction;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.MainFrame;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.util.JMeterToolBar;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.RenderAsHTML;
import org.apache.jorphan.collections.HashTree;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DebuggerDialogTest {
    private static final Logger log = LoggerFactory.getLogger(DebuggerDialogTest.class);

    @BeforeClass
    public static void setUp() {
        TestJMeterUtils.createJmeterEnv();
    }

    @Before
    public void setUpMethod() {
    }

    @Test
    public void testGui() throws Exception {
        if (GraphicsEnvironment.getLocalGraphicsEnvironment().isHeadlessInstance()) {
            return;
        }
        JMeterTreeListener a = new JMeterTreeListener();
        a.setActionHandler(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                log.debug("Action " + actionEvent);
            }
        });
        TestProvider prov = new TestProvider();
        JMeterTreeModel mdl = prov.getTreeModel();
        a.setModel(mdl);

        GuiPackage.initInstance(a, mdl);
        DebuggerDialog obj = new DebuggerDialogMock(prov.getTreeModel());

        obj.componentShown(null);
        obj.started();
        obj.statusRefresh(JMeterContextService.getContext());
        obj.frozenAt(new SamplerDebug());
        obj.continuing();
        obj.stopped();
        obj.componentHidden(null);
    }

    @Test
    public void displayGUI() throws InterruptedException, IOException, IllegalUserActionException {
        if (!GraphicsEnvironment.getLocalGraphicsEnvironment().isHeadlessInstance()) {
            TestProvider prov = new TestProvider("/com/blazemeter/jmeter/debugger/vars.jmx", "vars.jmx");
            JMeterTreeModel mdl = prov.getTreeModel();
            JMeterTreeListener a = new JMeterTreeListener();
            a.setActionHandler(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    log.debug("Action " + actionEvent);
                }
            });
            a.setModel(mdl);

            GuiPackage.initInstance(a, mdl);
            String actions = ActionRouter.class.getProtectionDomain().getCodeSource().getLocation().getFile();
            String renderers = RenderAsHTML.class.getProtectionDomain().getCodeSource().getLocation().getFile();
            JMeterUtils.setProperty("search_paths", actions + ";" + renderers);
            MainFrame mf = new MainFrame(mdl, a); // does important stuff inside
            ComponentFinder<JMeterToolBar> finder = new ComponentFinder<>(JMeterToolBar.class);
            JMeterToolBar tb = finder.findComponentIn(mf);
            tb.add(new JButton("test"));

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
            while (frame.isVisible()) {
                Thread.sleep(1000);
            }
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
}