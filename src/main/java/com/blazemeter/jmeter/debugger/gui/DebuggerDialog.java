package com.blazemeter.jmeter.debugger.gui;

import com.blazemeter.jmeter.debugger.elements.AbstractDebugElement;
import com.blazemeter.jmeter.debugger.engine.DebuggerEngine;
import com.blazemeter.jmeter.debugger.engine.StepTrigger;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.JMeterThreadMonitor;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class DebuggerDialog extends DebuggerDialogBase implements JMeterThreadMonitor {
    private static final Logger log = LoggingManager.getLoggerForClass();
    protected final JMeterTreeListener treeListener;

    protected DebuggerEngine engine;
    private StepOver stepper = new StepOver();

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
        this.engine = new DebuggerEngine(testTree);
        tgCombo.removeAllItems();
        for (ThreadGroup group : engine.getThreadGroups()) {
            tgCombo.addItem(group);
        }
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        log.debug("Closing dialog");
        engine.stopDebugging();
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
        engine.startDebugging(stepper, this);
    }

    private void stop() {
        engine.stopDebugging();
        start.setEnabled(true);
        stop.setEnabled(false);
        tgCombo.setEnabled(true);
    }

    @Override
    public void threadFinished(JMeterThread thread) {
        stop();
    }

    private void refreshStatus() {
        // TODO: refresh vars and properties
        // TODO: show samples
    }

    private void selectTargetInTree(TestElement te, Sampler currentSampler) {
        tree.setSelectionPath(getTreePathFor(te));
        markCurrentSampler(currentSampler);
        tree.repaint();
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
            JMeterTreeNode treeNode = model.getNodeOf(currentSampler);
            treeNode.setMarkedBySearch(true);
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
                if (event.getItem() instanceof ThreadGroup) {
                    engine.selectThreadGroup((ThreadGroup) event.getItem());
                    tree.setModel(new DebuggerTreeModel(engine.getFullTree()));
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
        public void notify(Object t) {
            step.setEnabled(true);
            if (t instanceof AbstractDebugElement) {
                Object wrappedElement = ((AbstractDebugElement) t).getWrappedElement();
                log.debug("Stopping before: " + wrappedElement);
                if (wrappedElement instanceof TestElement) {
                    selectTargetInTree((TestElement) wrappedElement, engine.getCurrentSampler());
                }
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
