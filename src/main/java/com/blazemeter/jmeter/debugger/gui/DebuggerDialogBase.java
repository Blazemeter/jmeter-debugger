package com.blazemeter.jmeter.debugger.gui;

import com.blazemeter.jmeter.debugger.engine.Debugger;
import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.LoggerPanel;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.WorkBench;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.visualizers.ViewResultsFullVisualizer;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.apache.jorphan.gui.ComponentUtil;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.LogTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;

abstract public class DebuggerDialogBase extends JDialog implements ComponentListener, NodeHighlighter, TreeSelectionListener {
    private static final Logger log = LoggerFactory.getLogger(DebuggerDialogBase.class);


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
    protected AbstractVisualizer lastSamplerResult;

    public DebuggerDialogBase() {
        super((JFrame) null, "Step-by-Step Debugger", true);
        setLayout(new BorderLayout());
        setSize(new Dimension(800, 600));
        setPreferredSize(new Dimension(800, 600));
        setIconImage(DebuggerMenuItem.getBugIcon(false).getImage());
        ComponentUtil.centerComponentInWindow(this);
        addComponentListener(this);

        add(getToolbar(), BorderLayout.NORTH);
        JSplitPane treeAndMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        treeAndMain.setDividerSize(5);
        treeAndMain.setLeftComponent(getTreePane());
        treeAndMain.setRightComponent(getMainPane());
        add(treeAndMain, BorderLayout.CENTER);
    }

    private Component getMainPane() {
        final JSplitPane topAndDown = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        topAndDown.setResizeWeight(.75);
        topAndDown.setDividerSize(5);
        topAndDown.setTopComponent(new JScrollPane(getElementPane()));
        topAndDown.setBottomComponent(getStatusPane());

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                topAndDown.setDividerLocation(0.7);
            }
        });
        return topAndDown;
    }

    private Component getStatusPane() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Variables", getVariablesTab());
        tabs.add("JMeter Properties", getPropertiesTab());
        tabs.add("Last Sample Result", getLastSamplerResultTab());
        tabs.add("Evaluate", getEvaluateTab());
        tabs.add("Log", getLogTab());
        return tabs;
    }

    protected void clearStatusPane() {
        lastSamplerResult.clearData();
        varsTableModel.clearData();
        propsTableModel.clearData();
        loggerPanel.clear();
    }

    private Component getEvaluateTab() {
        evaluatePanel = new EvaluatePanel();
        evaluatePanel.setEnabled(false);
        return evaluatePanel;
    }

    private Component getVariablesTab() {
        varsTableModel = new HighlightTableModel(new String[]{"Name", "Value"}, new Class[]{String.class, String.class});
        JTable table = new HighlightTable(varsTableModel);
        return new JScrollPane(table);
    }

    private Component getPropertiesTab() {
        propsTableModel = new HighlightTableModel(new String[]{"Name", "Value"}, new Class[]{String.class, String.class});
        JTable table = new HighlightTable(propsTableModel);
        return new JScrollPane(table);
    }


    private Component getLogTab() {
        loggerPanel = new LoggerPanelWrapping();
        loggerPanel.setMinimumSize(new Dimension(0, 50));
        loggerPanel.setPreferredSize(new Dimension(0, 150));
        try {
            if (!isJMeter32orLater()) {
                LoggingManager.addLogTargetToRootLogger(new LogTarget[]{loggerPanel});
            } else {
                Class cls = Class.forName("com.blazemeter.jmeter.debugger.logging.LoggerPanelAppender");
                Constructor constructor = cls.getConstructor(String.class, LoggerPanelWrapping.class);
                constructor.newInstance("debugger-logging-appender", loggerPanel);
            }
        } catch (Throwable ex) {
            log.error("Cannot hook into logging", ex);
        }
        return loggerPanel;
    }

    public static boolean isJMeter32orLater() {
        try {
            Class<?> cls = DebuggerDialogBase.class.getClassLoader().loadClass("org.apache.jmeter.gui.logging.GuiLogEventBus");
            if (cls != null) {
                return true;
            }
        } catch (ClassNotFoundException ex) {
            log.debug("Class 'org.apache.jmeter.gui.logging.GuiLogEventBus' not found", ex);
        } catch (Throwable ex) {
            log.warn("Fail to detect JMeter version", ex);
        }
        return false;
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
        res.add(new JLabel("Choose: "));

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

        stop.setVisible(false);
        /* TODO: revive it
        stop.setIcon(DebuggerMenuItem.getStopIcon());
        res.add(stop);
        stop.setEnabled(false);
        stop.setToolTipText(f5.toString());
        stop.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(f5, f5);
        stop.getActionMap().put(f5, toggle);
        */

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

        res.addSeparator();
        JButton help = new JButton("Help", DebuggerMenuItem.getHelpIcon());
        help.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        help.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (java.awt.Desktop.isDesktopSupported()) {
                    try {
                        java.awt.Desktop.getDesktop().browse(new URI("https://github.com/Blazemeter/jmeter-debugger"));
                    } catch (IOException | URISyntaxException ignored) {
                    }
                }
            }
        });
        res.add(help);
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

    public Component getLastSamplerResultTab() {
        lastSamplerResult = new ViewResultsFullVisualizer() {
            @Override
            public TestElement createTestElement() {
                this.collector = new ResultCollector() {
                    @Override
                    public void sampleOccurred(SampleEvent event) {
                        lastSamplerResult.clearData();
                        getVisualizer().add(event.getResult());
                    }
                };
                this.modifyTestElement(this.collector);
                return collector;
            }
        };
        lastSamplerResult.setName("Last Sampler Result");
        try {
            Field mainSplitField = lastSamplerResult.getClass().getSuperclass().getDeclaredField("mainSplit");
            mainSplitField.setAccessible(true);
            return (Component) mainSplitField.get(lastSamplerResult);
        } catch (Throwable ex) {
            log.warn("Failed to find 'mainSplit' field in visualizer");
            return lastSamplerResult;
        }
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

                    Debugger.toggleBreakpoint(te);

                    tree.repaint();
                }
            });

            item.setState(Debugger.isBreakpoint(te));
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

