package com.blazemeter.jmeter.debugger.gui;

import com.blazemeter.jmeter.debugger.elements.AbstractDebugElement;
import com.blazemeter.jmeter.debugger.engine.DebuggerEngine;
import com.blazemeter.jmeter.debugger.engine.StepTrigger;
import com.blazemeter.jmeter.debugger.engine.ThreadGroupSelector;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.engine.JMeterEngineException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.JMeterThreadMonitor;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DebuggerDialog extends DebuggerDialogBase implements JMeterThreadMonitor {
    private static final Logger log = LoggingManager.getLoggerForClass();
    protected final JMeterTreeListener treeListener;

    protected DebuggerEngine engine;
    private StepOver stepper = new StepOver();
    private ThreadGroupSelector tgSelector = new ThreadGroupSelector(new HashTree());

    public DebuggerDialog() {
        super();
        start.addActionListener(new StartDebugging());
        stop.addActionListener(new StopDebugging());
        step.addActionListener(stepper);
        tgCombo.addItemListener(new ThreadGroupChoiceChanged());

        treeListener = new JMeterTreeListener();
        treeListener.setJTree(tree);
        //tree.addTreeSelectionListener(treeListener);
        //tree.addMouseListener(treeListener);
        //tree.addKeyListener(treeListener);
    }

    @Override
    public void componentShown(ComponentEvent e) {
        log.debug("Showing dialog");
        HashTree testTree = getTestTree();
        this.tgSelector = new ThreadGroupSelector(testTree);
        tgCombo.removeAllItems();
        for (AbstractThreadGroup group : tgSelector.getThreadGroups()) {
            tgCombo.addItem(group);
        }
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        log.debug("Closing dialog");
        //TODO engine.stopDebugging();
    }

    protected HashTree getTestTree() {
        GuiPackage gui = GuiPackage.getInstance();
        return gui.getTreeModel().getTestPlan();
    }

    private void start() {
        log.debug("Start debugging");
        loggerPanel.clear();
        tgCombo.setEnabled(false);
        start.setEnabled(false);
        stop.setEnabled(true);

        HashTree hashTree = tgSelector.getSelectedTree();
        JMeter.convertSubTree(hashTree);
        engine = new DebuggerEngine();
        engine.setStepper(stepper);
        engine.configure(hashTree);
        try {
            engine.runTest();
        } catch (JMeterEngineException e) {
            log.error("Failed to run debug", e);
            stop();
        }
    }

    private void stop() {
        if (engine.isActive()) {
            engine.stopTest(true);
        }
        start.setEnabled(true);
        stop.setEnabled(false);
        tgCombo.setEnabled(true);
    }

    @Override
    public void threadFinished(JMeterThread thread) {
        stop();
    }

    private void refreshStatus() {
        JMeterContext context = engine.getThreadContext();
        refreshVars(context);
        refreshProperties();

        // TODO: show samples
    }

    private void refreshVars(JMeterContext context) {
        // TODO: highlight changes in vars
        varsTableModel.clearData();
        for (Map.Entry<String, Object> var : context.getVariables().entrySet()) {
            varsTableModel.addRow(new String[]{var.getKey(), var.getValue().toString()});
        }
        varsTableModel.fireTableDataChanged();
    }

    private void refreshProperties() {
        // TODO: highlight changes in props
        propsTableModel.clearData();
        for (Map.Entry<Object, Object> var : JMeterUtils.getJMeterProperties().entrySet()) {
            propsTableModel.addRow(new String[]{var.getKey().toString(), var.getValue().toString()});
        }
        propsTableModel.fireTableDataChanged();
    }

    private void selectTargetInTree(TestElement te, Sampler currentSampler) {
        tree.setSelectionPath(getTreePathFor(te));
        markCurrentSampler(currentSampler);
        tree.repaint();

        GuiPackage gui = GuiPackage.getInstance();
        if (gui != null) {
            JMeterGUIComponent egui = gui.getGui(te);
            elementContainer.removeAll();
            if (egui instanceof Component) {
                egui.setEnabled(false);
                elementContainer.add((Component) egui, BorderLayout.CENTER);
            }
            elementContainer.updateUI();
        }
    }

    private void markCurrentSampler(Sampler currentSampler) {
        JMeterTreeModel model = (JMeterTreeModel) tree.getModel();

        for (JMeterTreeNode jMeterTreeNode : model.getNodesOfType(Sampler.class)) {
            if (jMeterTreeNode.getUserObject() instanceof Sampler) {
                List<JMeterTreeNode> matchingNodes = jMeterTreeNode.getPathToThreadGroup();
                for (JMeterTreeNode jMeterTreeNode2 : matchingNodes) {
                    jMeterTreeNode2.setMarkedBySearch(false);
                }
            }
        }

        if (currentSampler != null) {
            if (currentSampler instanceof AbstractDebugElement) {
                currentSampler = (Sampler) ((AbstractDebugElement) currentSampler).getWrappedElement();
            }

            JMeterTreeNode treeNode = model.getNodeOf(currentSampler);
            if (treeNode != null) {
                treeNode.setMarkedBySearch(true); // TODO: find better display for it
            } else {
                log.warn("Failed to find tree node for " + currentSampler.getName());
            }
        }
    }

    private TreePath getTreePathFor(TestElement te) {
        List<Object> nodes = new ArrayList<>();
        JMeterTreeModel model = (JMeterTreeModel) tree.getModel();

        TreeNode treeNode = model.getNodeOf(te);
        if (treeNode != null) {
            nodes.add(treeNode);
            treeNode = treeNode.getParent();
            while (treeNode != null) {
                nodes.add(0, treeNode);
                treeNode = treeNode.getParent();
            }
        }

        return nodes.isEmpty() ? null : new TreePath(nodes.toArray());
    }

    private class ThreadGroupChoiceChanged implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent event) {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                log.debug("Item choice changed: " + event.getItem());
                if (event.getItem() instanceof AbstractThreadGroup) {
                    tgSelector.selectThreadGroup((AbstractThreadGroup) event.getItem());
                    tree.setModel(new DebuggerTreeModel(tgSelector.getSelectedTree()));
                }
            }
        }
    }

    private class StartDebugging implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            start();
        }
    }

    private class StepOver implements ActionListener, StepTrigger {
        @Override
        public void actionPerformed(ActionEvent e) {
            synchronized (this) {
                this.notifyAll();
            }
        }

        @Override
        public void notify(AbstractDebugElement t) {
            step.setEnabled(true);
            Object wrappedElement = t.getWrappedElement();
            log.debug("Stopping before: " + wrappedElement);
            if (wrappedElement instanceof TestElement) {
                selectTargetInTree((TestElement) wrappedElement, engine.getCurrentSampler());
            }
            refreshStatus();
            try {
                synchronized (this) {
                    this.wait();
                }
            } catch (InterruptedException e) {
                stop();
            }
            step.setEnabled(false);
        }
    }


    private class StopDebugging implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            stop();
        }
    }
}
