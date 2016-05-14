package com.blazemeter.jmeter.debugger.gui;

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
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

abstract public class DebuggerDialogBase extends JDialog implements ComponentListener {
    private static final Logger log = LoggingManager.getLoggerForClass();

    protected JComboBox<ThreadGroup> tgCombo = new JComboBox<>();
    protected JTree tree;
    protected JButton start = new JButton("Start");
    protected JButton step = new JButton("Step Over");
    protected JButton stop = new JButton("Stop");
    protected LoggerPanel loggerPanel;

    public DebuggerDialogBase() {
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

        res.add(start);

        res.add(stop);
        stop.setEnabled(false);
        res.addSeparator();

        res.add(step);
        step.setEnabled(false);

        return res;
    }

    @Override
    public void componentResized(ComponentEvent e) {

    }

    @Override
    public void componentMoved(ComponentEvent e) {

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
}
