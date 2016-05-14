package com.blazemeter.jmeter.debugger.gui;

import com.blazemeter.jmeter.debugger.engine.DebuggerEngine;
import com.blazemeter.jmeter.debugger.engine.StepTrigger;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.LoggerPanel;
import org.apache.jmeter.gui.tree.JMeterCellRenderer;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.gui.ComponentUtil;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.LogTarget;
import org.apache.log.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DebuggerDialog extends JDialog implements ComponentListener {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private final JComboBox<ThreadGroup> tgCombo = new JComboBox<>();
    private DebuggerEngine engine;
    private JTree tree;
    private JButton start = new JButton("Start");
    private JButton step = new JButton("Step Over");
    private JButton stop = new JButton("Stop");
    private LoggerPanel loggerPanel;

    public DebuggerDialog() {
        super((JFrame) null, "Step-by-Step Debugger", true);
        setLayout(new BorderLayout());
        setSize(new Dimension(800, 600));
        setPreferredSize(new Dimension(800, 600));
        setIconImage(DebuggerMenuItem.getPluginsIcon().getImage());
        ComponentUtil.centerComponentInWindow(this, 30);
        addComponentListener(this);

        add(getToolbar(), BorderLayout.NORTH);
        JSplitPane treeAndMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        treeAndMain.setDividerSize(5);
        treeAndMain.setLeftComponent(getTreePane());
        treeAndMain.setRightComponent(getMainPane());
        add(treeAndMain, BorderLayout.CENTER);
    }

    private Component getMainPane() {
        JSplitPane topAndDown = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        topAndDown.setResizeWeight(.5);
        topAndDown.setDividerSize(5);
        topAndDown.setTopComponent(getElementPane());
        topAndDown.setBottomComponent(getStatusPane());
        return topAndDown;
    }

    private Component getStatusPane() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Current Sample", new JPanel());
        tabs.add("Variables", new JPanel());
        tabs.add("JMeter Properties", new JPanel());
        tabs.add("System Properties", new JPanel());
        tabs.add("Log", getLogTab());
        return tabs;
    }

    private Component getLogTab() {
        // TODO: make it word wrap
        loggerPanel = new LoggerPanel();
        loggerPanel.setMinimumSize(new Dimension(0, 100));
        loggerPanel.setPreferredSize(new Dimension(0, 150));

        LoggingManager.addLogTargetToRootLogger(new LogTarget[]{
                loggerPanel,
        });
        return loggerPanel;
    }

    private Component getElementPane() {
        return new JPanel();
    }

    private Component getTreePane() {
        JScrollPane panel = new JScrollPane(getTreeView());
        panel.setMinimumSize(new Dimension(100, 0));
        panel.setPreferredSize(new Dimension(250, 0));
        return panel;
    }

    private Component getToolbar() {
        JToolBar res = new JToolBar();
        res.setFloatable(false);
        res.add(new JLabel("Choose Thread Group: "));

        res.add(tgCombo);
        tgCombo.setRenderer(new ThreadGroupItemRenderer(tgCombo.getRenderer()));
        tgCombo.addItemListener(new ThreadGroupChoiceChanged());

        res.add(start);
        start.addActionListener(new StartDebugging());

        res.add(stop);
        res.addSeparator();

        res.add(step);
        step.setEnabled(false);
        step.addActionListener(new StepOver());

        return res;
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
        HashTree testTree = getTestTree();
        this.engine = new DebuggerEngine(testTree);
        tgCombo.removeAllItems();
        for (ThreadGroup group : engine.getThreadGroups()) {
            tgCombo.addItem(group);
        }
    }

    protected HashTree getTestTree() {
        GuiPackage gui = GuiPackage.getInstance();
        return gui.getTreeModel().getTestPlan();
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        log.debug("Closing dialog");
    }


    private JTree getTreeView() {
        tree = new JTree(new DebuggerTreeModel(new HashTree()));
        //tree.setToolTipText("");
        tree.setCellRenderer(new JMeterCellRenderer());
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);

        //treeListener=new JMeterTreeListener();
        //treeListener.setJTree(tree);
        //tree.addTreeSelectionListener(treeListener);
        //tree.addMouseListener(treeListener);
        //tree.addKeyListener(treeListener);

        return tree;
    }

    private class ThreadGroupChoiceChanged implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent event) {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                log.debug("Item choice changed: " + event.getItem());
                if (event.getItem() instanceof ThreadGroup) {
                    HashTree val = engine.getThreadGroupTree((ThreadGroup) event.getItem());
                    tree.setModel(new DebuggerTreeModel(val));
                    start.setEnabled(!val.isEmpty()); // TODO
                }
            }
        }
    }

    private class StartDebugging implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            log.debug("Start debugging");
            loggerPanel.clear();
            step.setEnabled(true);
            ThreadGroup tg = (ThreadGroup) tgCombo.getSelectedItem();
            engine.startDebugging(tg, engine.getExecutionTree(tg), new StepOver());
        }
    }

    private class StepOver implements ActionListener, StepTrigger {
        @Override
        public void actionPerformed(ActionEvent e) {
            log.debug("Step over");
            this.notifyAll();
        }

        @Override
        public void notify(Object t) {
            log.debug("Stopped before: " + t);
            try {
                wait();
            } catch (InterruptedException e) {
                engine.stopDebugging();
            }
        }
    }
}
