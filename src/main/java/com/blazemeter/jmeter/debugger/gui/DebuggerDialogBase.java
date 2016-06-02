package com.blazemeter.jmeter.debugger.gui;

import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.gui.LoggerPanel;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.WorkBench;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.gui.ComponentUtil;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.LogTarget;
import org.apache.log.Logger;

import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

abstract public class DebuggerDialogBase extends JDialog implements ComponentListener, TestStateListener, NodeHighlighter, TreeSelectionListener {
    private static final Logger log = LoggingManager.getLoggerForClass();

    protected JComboBox<AbstractThreadGroup> tgCombo = new JComboBox<>();
    protected JTree tree;
    protected JMeterTreeModel treeModel;
    protected JButton start = new JButton("Start");
    protected JButton step = new JButton("Step Over");
    protected JButton stop = new JButton("Stop");
    protected JButton pauseContinue = new JButton("Continue");
    protected LoggerPanel loggerPanel;
    protected PowerTableModel varsTableModel;
    protected PowerTableModel propsTableModel;
    protected JPanel elementContainer;
    protected EvaluatePanel evaluatePanel;
    protected Set<TestElement> breakpoints = new HashSet<>();

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
        topAndDown.setTopComponent(new JScrollPane(getElementPane()));
        topAndDown.setBottomComponent(getStatusPane());
        return topAndDown;
    }

    private Component getStatusPane() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Variables", getVariablesTab());
        tabs.add("JMeter Properties", getPropertiesTab());
        tabs.add("Evaluate", getEvaluateTab());
        tabs.add("Log", getLogTab());
        return tabs;
    }

    private Component getEvaluateTab() {
        evaluatePanel = new EvaluatePanel();
        evaluatePanel.setEnabled(false);
        return evaluatePanel;
    }

    private Component getVariablesTab() {
        varsTableModel = new HighlightTableModel(new String[]{"Name", "Value"}, new Class[]{String.class, String.class});
        JTable table = new HighlightTable(varsTableModel);
        table.setDefaultEditor(Object.class, null); // TODO: allow editing vars
        setTableSorted(table);
        return new JScrollPane(table);
    }

    private Component getPropertiesTab() {
        propsTableModel = new HighlightTableModel(new String[]{"Name", "Value"}, new Class[]{String.class, String.class});
        JTable table = new HighlightTable(propsTableModel);
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
        loggerPanel = new LoggerPanelWrapping();
        loggerPanel.setMinimumSize(new Dimension(0, 100));
        loggerPanel.setPreferredSize(new Dimension(0, 150));
        LoggingManager.addLogTargetToRootLogger(new LogTarget[]{loggerPanel,});
        return loggerPanel;
    }

    private Component getElementPane() {
        elementContainer = new JPanel(new BorderLayout());
        return elementContainer;
    }

    private Component getTreePane() {
        JScrollPane panel = new JScrollPane(getTreeView());
        panel.setMinimumSize(new Dimension(200, 0));
        panel.setPreferredSize(new Dimension(250, 0));
        return panel;
    }

    private Component getToolbar() {
        JToolBar res = new JToolBar();
        res.setFloatable(false);
        JLabel logo = new BlazeMeterLogo();
        res.add(logo);
        res.addSeparator(new Dimension(32, 26));
        res.add(new JLabel("Choose Thread Group: "));

        res.add(tgCombo);
        tgCombo.setRenderer(new ThreadGroupItemRenderer(tgCombo.getRenderer()));

        AbstractAction toggle = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (start.isEnabled()) {
                    start.doClick();
                } else {
                    stop.doClick();
                }
            }
        };
        KeyStroke f5 = KeyStroke.getKeyStroke(KeyEvent.VK_F5, InputEvent.SHIFT_DOWN_MASK);

        start.setIcon(DebuggerMenuItem.getStartIcon());
        res.add(start);
        start.setToolTipText(f5.toString());
        start.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(f5, f5);
        start.getActionMap().put(f5, toggle);

        stop.setIcon(DebuggerMenuItem.getStopIcon());
        res.add(stop);
        stop.setEnabled(false);
        stop.setToolTipText(f5.toString());
        stop.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(f5, f5);
        stop.getActionMap().put(f5, toggle);

        res.addSeparator();

        step.setIcon(DebuggerMenuItem.getStepIcon());
        res.add(step);
        step.setEnabled(false);
        KeyStroke f8 = KeyStroke.getKeyStroke(KeyEvent.VK_F8, InputEvent.SHIFT_DOWN_MASK);
        step.setToolTipText(f8.toString());
        step.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(f8, f8);
        step.getActionMap().put(f8, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                step.doClick();
            }
        });

        pauseContinue.setIcon(DebuggerMenuItem.getContinueIcon());
        res.add(pauseContinue);
        pauseContinue.setEnabled(false);
        KeyStroke f9 = KeyStroke.getKeyStroke(KeyEvent.VK_F9, InputEvent.SHIFT_DOWN_MASK);
        pauseContinue.setToolTipText(f9.toString());
        pauseContinue.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(f9, f9);
        pauseContinue.getActionMap().put(f8, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                pauseContinue.doClick();
            }
        });
        return res;
    }

    @Override
    public void componentResized(ComponentEvent e) {
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    private JTree getTreeView() {
        treeModel = new JMeterTreeModel();
        tree = new JTree(treeModel);
        tree.setCellRenderer(new FixedJMeterTreeCellRenderer(this));
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.addTreeSelectionListener(this);
        tree.addMouseListener(new TreeMouseListener());
        return tree;
    }

    @Override
    public void testStarted() {

    }

    @Override
    public void testStarted(String host) {

    }

    @Override
    public void testEnded(String host) {

    }

    private class TreeMouseListener implements MouseListener {
        @Override
        public void mousePressed(MouseEvent e) {
            int selRow = tree.getRowForLocation(e.getX(), e.getY());

            if (tree.getPathForLocation(e.getX(), e.getY()) != null) {
                final TreePath currentPath = tree.getPathForLocation(e.getX(), e.getY());

                if (selRow != -1 && currentPath != null) {
                    if (isRightClick(e)) {
                        if (tree.getSelectionCount() < 2) {
                            tree.setSelectionPath(currentPath);
                        }
                        final JMeterTreeNode node = (JMeterTreeNode) currentPath.getLastPathComponent();
                        TestElement te = (TestElement) node.getUserObject();
                        if (te instanceof ConfigElement || te instanceof TestPlan || te instanceof ThreadGroup || te instanceof WorkBench) {
                            log.debug("No breakpoint possible for " + te);
                            return;
                        }
                        JPopupMenu popup = getPopup(te);
                        popup.pack();
                        popup.show(tree, e.getX(), e.getY());
                        popup.setVisible(true);
                        popup.requestFocusInWindow();
                    }
                }
            }
        }

        private JPopupMenu getPopup(final TestElement te) {
            JPopupMenu popup = new JPopupMenu();
            JCheckBoxMenuItem item = new JCheckBoxMenuItem("Breakpoint", DebuggerMenuItem.getBPIcon());
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    log.debug("Toggle breakpoint on: " + te);
                    if (breakpoints.contains(te)) {
                        breakpoints.remove(te);
                    } else {
                        breakpoints.add(te);
                    }
                    tree.repaint();
                }
            });

            item.setState(breakpoints.contains(te));
            popup.add(item);
            return popup;
        }

        private boolean isRightClick(MouseEvent e) {
            return e.isPopupTrigger() || (InputEvent.BUTTON2_MASK & e.getModifiers()) > 0 || (InputEvent.BUTTON3_MASK == e.getModifiers());
        }

        @Override
        public void mouseClicked(MouseEvent mouseEvent) {

        }

        @Override
        public void mouseReleased(MouseEvent mouseEvent) {

        }

        @Override
        public void mouseEntered(MouseEvent mouseEvent) {

        }

        @Override
        public void mouseExited(MouseEvent mouseEvent) {

        }
    }

}

