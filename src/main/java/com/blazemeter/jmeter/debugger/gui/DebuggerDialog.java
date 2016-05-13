package com.blazemeter.jmeter.debugger.gui;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterCellRenderer;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.gui.ComponentUtil;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class DebuggerDialog extends JDialog implements ComponentListener {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private final JComboBox<ThreadGroup> tgCombo = new JComboBox<>();
    private DebuggerEngine engine;
    private JTree tree;

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
        return tabs;
    }

    private Component getElementPane() {
        return new JPanel();
    }

    private Component getTreePane() {
        JScrollPane panel = new JScrollPane(getTreeView(new JMeterTreeListener()));
        panel.setMinimumSize(new Dimension(100, 0));
        panel.setPreferredSize(new Dimension(250, 0));
        return panel;
    }

    private Component getToolbar() {
        JToolBar res = new JToolBar();
        res.setFloatable(false);
        res.add(new JLabel("Choose Thread Group: "));
        tgCombo.setRenderer(new ThreadGroupItemRenderer(tgCombo.getRenderer()));
        tgCombo.addItemListener(new ThreadGroupChoiceChanged());
        res.add(tgCombo);
        res.add(new JButton("Start"));
        res.add(new JButton("Stop"));
        res.addSeparator();

        res.add(new JButton("Step Over"));
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


    private JTree getTreeView(JMeterTreeListener treeListener) {
        tree = new JTree(new DebuggerTreeModel(new HashTree()));
        //tree.setToolTipText("");
        tree.setCellRenderer(new JMeterCellRenderer());
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);

        treeListener.setJTree(tree);
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
                    HashTree val = engine.getThreadgroupTree((ThreadGroup) event.getItem());
                    tree.setModel(new DebuggerTreeModel(val));
                }
            }
        }
    }

}
