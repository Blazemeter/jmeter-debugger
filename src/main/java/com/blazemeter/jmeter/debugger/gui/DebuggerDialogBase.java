package com.blazemeter.jmeter.debugger.gui;

import org.apache.jmeter.gui.LoggerPanel;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.gui.ComponentUtil;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.LogTarget;
import org.apache.log.Logger;

import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.LinkedList;

abstract public class DebuggerDialogBase extends JDialog implements ComponentListener {
    private static final Logger log = LoggingManager.getLoggerForClass();

    protected JComboBox<ThreadGroup> tgCombo = new JComboBox<>();
    protected JTree tree;
    protected JButton start = new JButton("Start");
    protected JButton step = new JButton("Step Over");
    protected JButton stop = new JButton("Stop");
    protected LoggerPanel loggerPanel;
    protected PowerTableModel varsTableModel;
    protected PowerTableModel propsTableModel;
    protected JPanel elementContainer;

    public DebuggerDialogBase() {
        super((JFrame) null, "Step-by-Step Debugger", true);
        setLayout(new BorderLayout());
        setSize(new Dimension(800, 600));
        setPreferredSize(new Dimension(800, 600));
        setIconImage(DebuggerMenuItem.getBugIcon().getImage());
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
        //tabs.add("Samples", getSamplesTab());
        tabs.add("Variables", getVariablesTab());
        tabs.add("JMeter Properties", getPropertiesTab());
        tabs.add("Log", getLogTab());
        return tabs;
    }

    private Component getSamplesTab() {
        return new JPanel();
    }

    private Component getVariablesTab() {
        varsTableModel = new PowerTableModel(new String[]{"Name", "Value"}, new Class[]{String.class, String.class});
        JTable table = new JTable(varsTableModel);
        table.setDefaultEditor(Object.class, null); // TODO: allow editing vars
        setTableSorted(table);
        return new JScrollPane(table);
    }

    private Component getPropertiesTab() {
        propsTableModel = new PowerTableModel(new String[]{"Name", "Value"}, new Class[]{String.class, String.class});
        JTable table = new JTable(propsTableModel);
        table.setDefaultEditor(Object.class, null); // TODO: allow editing props
        setTableSorted(table);
        return new JScrollPane(table);
    }

    private void setTableSorted(JTable table) {
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
        sorter.setSortsOnUpdates(true);
        LinkedList<RowSorter.SortKey> sortKeys = new LinkedList<>();
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);
        sorter.sort();
        table.setRowSorter(sorter);
    }

    private Component getLogTab() {
        loggerPanel = new LoggerPanel();
        loggerPanel.setMinimumSize(new Dimension(0, 100));
        loggerPanel.setPreferredSize(new Dimension(0, 150));

        Component comp = loggerPanel.getComponent(0);
        if (comp instanceof JTextScrollPane) {
            comp = ((JTextScrollPane) comp).getComponent(0);
            if (comp instanceof JViewport) {
                comp = ((JViewport) comp).getComponent(0);
                if (comp instanceof JSyntaxTextArea) {
                    JSyntaxTextArea area = (JSyntaxTextArea) comp;
                    area.setLineWrap(true);
                }
            }
        }
        LoggingManager.addLogTargetToRootLogger(new LogTarget[]{loggerPanel,});
        return loggerPanel;
    }

    private Component getElementPane() {
        elementContainer = new JPanel(new BorderLayout());
        return elementContainer;
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

        start.setIcon(DebuggerMenuItem.getStartIcon());
        res.add(start);

        stop.setIcon(DebuggerMenuItem.getStopIcon());
        res.add(stop);
        stop.setEnabled(false);
        res.addSeparator();

        step.setIcon(DebuggerMenuItem.getStepIcon());
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
        tree = new JTree(new JMeterTreeModel());
        tree.setCellRenderer(new FixedJMeterTreeCellRenderer());
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);

        return tree;
    }
}
