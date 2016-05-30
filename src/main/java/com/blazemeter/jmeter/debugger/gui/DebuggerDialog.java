package com.blazemeter.jmeter.debugger.gui;

import com.blazemeter.jmeter.debugger.ThreadGroupWrapper;
import com.blazemeter.jmeter.debugger.elements.Wrapper;
import com.blazemeter.jmeter.debugger.engine.DebuggerEngine;
import com.blazemeter.jmeter.debugger.engine.SearchClass;
import com.blazemeter.jmeter.debugger.engine.StepTrigger;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.control.ReplaceableController;
import org.apache.jmeter.engine.JMeterEngineException;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterContextServiceAccessor;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JMeterStopThreadException;
import org.apache.log.Logger;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DebuggerDialog extends DebuggerDialogBase {
    private static final Logger log = LoggingManager.getLoggerForClass();

    protected DebuggerEngine engine;
    private final StepOver stepper = new StepOver();
    private ThreadGroupSelector tgSelector = new ThreadGroupSelector(new HashTree());
    private boolean savedDirty = false;
    private Wrapper currentElement;

    public DebuggerDialog() {
        super();
        start.addActionListener(new StartDebugging());
        stop.addActionListener(new StopDebugging());
        step.addActionListener(stepper);
        tgCombo.addItemListener(new ThreadGroupChoiceChanged());
    }

    @Override
    public void componentShown(ComponentEvent e) {
        log.debug("Showing dialog");
        if (GuiPackage.getInstance() != null) {
            savedDirty = GuiPackage.getInstance().isDirty();
        }
        HashTree testTree = getTestTree();
        this.tgSelector = new ThreadGroupSelector(testTree);
        tgCombo.removeAllItems();
        for (AbstractThreadGroup group : tgSelector.getThreadGroups()) {
            tgCombo.addItem(group);
        }
        tgCombo.setEnabled(tgCombo.getItemCount() > 0);
        start.setEnabled(tgCombo.getItemCount() > 0);
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        log.debug("Closing dialog");
        stop();
        if (GuiPackage.getInstance() != null) {
            GuiPackage.getInstance().setDirty(savedDirty);
        }
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
        evaluatePanel.setEnabled(true);

        HashTree hashTree = tgSelector.getSelectedTree();
        StandardJMeterEngine.register(this); // oh, dear, they use static field then clean it...
        engine = new DebuggerEngine(JMeterContextService.getContext());
        engine.setStepper(stepper);
        JMeter.convertSubTree(hashTree);
        engine.configure(hashTree);
        try {
            engine.runTest();
        } catch (JMeterEngineException e) {
            log.error("Failed to run debug", e);
            stop();
        }
    }

    private void stop() {
        log.debug("Stop debugging");

        if (stepper.isStopping()) {
            throw new IllegalStateException("Already stopping");
        }

        stepper.setStopping(true);
        try {
            if (engine != null && engine.isActive()) {
                engine.stopTest(true);
            }
        } finally {
            stepper.setStopping(false);
            start.setEnabled(true);
            stop.setEnabled(false);
            tgCombo.setEnabled(true);
            evaluatePanel.setEnabled(false);
            elementContainer.removeAll();
            JMeterContextServiceAccessor.removeContext();
            setCurrentElement(null);
        }
    }

    @Override
    public void testEnded() {
        stop();
    }

    private void refreshStatus() {
        JMeterContext context = engine.getThreadContext();
        refreshVars(context);
        refreshProperties();
        evaluatePanel.refresh(context);
    }

    private void refreshVars(JMeterContext context) {
        varsTableModel.clearData();
        for (Map.Entry<String, Object> var : context.getVariables().entrySet()) {
            varsTableModel.addRow(new String[]{var.getKey(), var.getValue().toString()});
        }
        varsTableModel.fireTableDataChanged();
    }

    private void refreshProperties() {
        propsTableModel.clearData();
        for (Map.Entry<Object, Object> var : JMeterUtils.getJMeterProperties().entrySet()) {
            propsTableModel.addRow(new String[]{var.getKey().toString(), var.getValue().toString()});
        }
        propsTableModel.fireTableDataChanged();
    }

    private void setCurrentElement(Wrapper te) {
        currentElement = te;
    }

    private void selectTargetInTree(Wrapper dbgElm) {
        TestElement wrpElm = (TestElement) dbgElm.getWrappedElement();
        TreePath treePath = getTreePathFor(wrpElm);
        if (treePath == null) {
            // case for wrapped controllers
            treePath = getTreePathFor(dbgElm);
        }

        if (treePath == null) {
            log.debug("Did not find tree path for element");
        } else {
            tree.setSelectionPath(treePath);
        }
        tree.repaint();

        GuiPackage gui = GuiPackage.getInstance();
        if (gui != null) {
            JMeterGUIComponent egui = gui.getGui(wrpElm);
            egui.configure(wrpElm);
            elementContainer.removeAll();
            if (egui instanceof Component) {
                egui.setEnabled(false);

                elementContainer.add((Component) egui, BorderLayout.CENTER);
            }
            elementContainer.updateUI();
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

    private void selectThreadGroup(AbstractThreadGroup tg) {
        tgSelector.selectThreadGroup(tg);
        treeModel.clearTestPlan();
        HashTree selectedTree = tgSelector.getSelectedTree();

        // Hack to resolve ModuleControllers from JMeter.java
        SearchClass<ReplaceableController> replaceableControllers = new SearchClass<>(ReplaceableController.class);
        selectedTree.traverse(replaceableControllers);
        Collection<ReplaceableController> replaceableControllersRes = replaceableControllers.getSearchResults();
        for (ReplaceableController replaceableController : replaceableControllersRes) {
            replaceableController.resolveReplacementSubTree((JMeterTreeNode) treeModel.getRoot());
        }

        JMeter.convertSubTree(selectedTree);
        try {
            treeModel.addSubTree(selectedTree, (JMeterTreeNode) treeModel.getRoot());
        } catch (IllegalUserActionException e) {
            throw new RuntimeException(e);
        }

        // select TG for visual convenience
        selectTargetInTree(new ThreadGroupWrapper(tgSelector.getThreadGroupClone()));
    }

    @Override
    public void highlightNode(Component component, JMeterTreeNode node, TestElement mc) {
        component.setFont(component.getFont().deriveFont(~Font.BOLD).deriveFont(~Font.ITALIC));
        if (engine != null) {
            if (mc.equals(currentElement) || mc.equals(currentElement.getWrappedElement())) {
                component.setFont(component.getFont().deriveFont(Font.BOLD));
                component.setForeground(Color.BLUE);
            } 
            
            Sampler currentSampler = engine.getCurrentSampler();
            if (mc.equals(currentSampler)) { // can this ever happen?
                component.setFont(component.getFont().deriveFont(Font.ITALIC));
                component.setForeground(Color.BLUE);
            } else if (currentSampler instanceof Wrapper && mc.equals(((Wrapper) currentSampler).getWrappedElement())) {
                component.setFont(component.getFont().deriveFont(Font.ITALIC));
                component.setForeground(Color.BLUE);
            }
        }
    }

    private class ThreadGroupChoiceChanged implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent event) {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                log.debug("Item choice changed: " + event.getItem());
                if (event.getItem() instanceof AbstractThreadGroup) {
                    selectThreadGroup((AbstractThreadGroup) event.getItem());
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
        private boolean stopping;

        @Override
        public void actionPerformed(ActionEvent e) {
            synchronized (this) {
                this.notifyAll();
            }
        }

        @Override
        public void notify(Wrapper wrapper) {
            step.setEnabled(true);
            TestElement wrappedElement = (TestElement) wrapper.getWrappedElement();
            log.debug("Stopping before: " + wrappedElement);
            setCurrentElement(wrapper);
            selectTargetInTree(wrapper);
            refreshStatus();
            if (!stopping) {
                try {
                    synchronized (this) {
                        this.wait();
                    }
                } catch (InterruptedException e) {
                    log.debug("Interrupted", e);
                    throw new JMeterStopThreadException(e);
                } finally {
                    step.setEnabled(false);
                }
            } else {
                throw new JMeterStopThreadException();
            }
        }

        public void setStopping(boolean stopping) {
            this.stopping = stopping;
        }

        public boolean isStopping() {
            return stopping;
        }
    }

    private class StopDebugging implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            stop();
        }
    }
}
