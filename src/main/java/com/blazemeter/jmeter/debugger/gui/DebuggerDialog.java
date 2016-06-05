package com.blazemeter.jmeter.debugger.gui;

import com.blazemeter.jmeter.debugger.elements.ThreadGroupWrapper;
import com.blazemeter.jmeter.debugger.elements.Wrapper;
import com.blazemeter.jmeter.debugger.engine.Debugger;
import com.blazemeter.jmeter.debugger.engine.DebuggerFrontend;
import com.blazemeter.jmeter.debugger.engine.SearchClass;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.control.ReplaceableController;
import org.apache.jmeter.engine.TreeCloner;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DebuggerDialog extends DebuggerDialogBase implements DebuggerFrontend {
    private static final Logger log = LoggingManager.getLoggerForClass();
    private boolean savedDirty = false;

    public DebuggerDialog() {
        super();
        start.addActionListener(new StartDebugging());
        stop.addActionListener(new StopDebugging());
        step.addActionListener(new StepOver());
        pauseContinue.addActionListener(new PauseContinue());
        tgCombo.addItemListener(new ThreadGroupChoiceChanged());
    }

    @Override
    public void componentShown(ComponentEvent e) {
        log.debug("Showing dialog");
        if (GuiPackage.getInstance() != null) {
            savedDirty = GuiPackage.getInstance().isDirty();
        }
        HashTree testTree = getTestTree();
        TreeCloner cloner = new TreeCloner();  // clone to not modify original JMX
        testTree.traverse(cloner);
        this.debugger = new Debugger(cloner.getClonedTree(), this);
        tgCombo.removeAllItems();
        for (AbstractThreadGroup group : debugger.getThreadGroups()) {
            tgCombo.addItem(group);
        }
        tgCombo.setEnabled(tgCombo.getItemCount() > 0);
        start.setEnabled(tgCombo.getItemCount() > 0);
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        log.debug("Closing dialog");
        debugger.stop();
        if (GuiPackage.getInstance() != null) {
            GuiPackage.getInstance().setDirty(savedDirty);
        }
    }

    /**
     * Get test tree from JMeter
     */
    protected HashTree getTestTree() {
        GuiPackage gui = GuiPackage.getInstance();
        return gui.getTreeModel().getTestPlan();
    }

    private void toggleControls(boolean state) {
        tgCombo.setEnabled(state);
        start.setEnabled(state);
        stop.setEnabled(!state);
        pauseContinue.setEnabled(!state);
        evaluatePanel.setEnabled(!state);
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
            if (treePath.equals(tree.getSelectionPath())) {
                tree.setSelectionPath(treePath.getParentPath());
            }
            tree.setSelectionPath(treePath);
        }
        tree.repaint();
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
        debugger.selectThreadGroup(tg);
        treeModel.clearTestPlan();
        HashTree selectedTree = debugger.getSelectedTree();

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
        // TODO selectTargetInTree(new ThreadGroupWrapper(debugger.getThreadGroupClone()));
    }

    @Override
    public void highlightNode(Component component, JMeterTreeNode node, TestElement mc) {
        component.setFont(component.getFont().deriveFont(~Font.BOLD).deriveFont(~Font.ITALIC));

        TestElement userObject = (TestElement) node.getUserObject();
        if (debugger.isBreakpoint(userObject)) {
            component.setForeground(Color.RED);
        }

        Wrapper currentElement = debugger.getCurrentElement();
        if (currentElement == null) {
            return;
        }

        TestElement currentWrapped = (TestElement) currentElement.getWrappedElement();
        if (mc == currentElement || mc == currentWrapped) {
            component.setFont(component.getFont().deriveFont(Font.BOLD));
            component.setForeground(Color.BLUE);
        }

        Sampler currentSampler = debugger.getCurrentSampler();
        Font font = component.getFont();
        if (mc == currentSampler) { // can this ever happen?
            component.setFont(font.deriveFont(font.getStyle() | Font.ITALIC));
            component.setForeground(Color.BLUE);
        } else if (currentSampler instanceof Wrapper && mc == ((Wrapper) currentSampler).getWrappedElement()) {
            component.setFont(font.deriveFont(font.getStyle() | Font.ITALIC));
            component.setForeground(Color.BLUE);
        }
    }

    @Override
    public void valueChanged(TreeSelectionEvent treeSelectionEvent) {
        JMeterTreeNode node = (JMeterTreeNode) treeSelectionEvent.getPath().getLastPathComponent();
        TestElement wrpElm = node.getTestElement();
        if (wrpElm instanceof Wrapper) {
            wrpElm = (TestElement) ((Wrapper) wrpElm).getWrappedElement();
        }

        displayElementGui(wrpElm);
    }

    private void displayElementGui(TestElement wrpElm) {
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

    @Override
    public void started() {
        loggerPanel.clear();
        toggleControls(false);
    }

    @Override
    public void stopped() {
        toggleControls(true);
        elementContainer.removeAll();
    }

    @Override
    public void frozenAt(Wrapper wrapper) {
        pauseContinue.setText("Continue");
        pauseContinue.setIcon(DebuggerMenuItem.getContinueIcon());

        step.setEnabled(true);
        selectTargetInTree(wrapper);
    }

    @Override
    public void statusRefresh(JMeterContext context) {
        refreshVars(context);
        refreshProperties();
        evaluatePanel.refresh(context, debugger.isContinuing());
        tree.repaint();
    }

    @Override
    public void continuing() {
        // to prevent buttons "jumping"
        pauseContinue.setMinimumSize(pauseContinue.getSize());
        pauseContinue.setPreferredSize(pauseContinue.getSize());
        pauseContinue.setSize(pauseContinue.getSize());

        pauseContinue.setText("Pause");
        pauseContinue.setIcon(DebuggerMenuItem.getPauseIcon());
        step.setEnabled(false);
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
            debugger.start();
        }
    }

    private class StepOver implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            synchronized (this) {
                debugger.proceed();
            }
        }
    }

    private class PauseContinue implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (debugger.isContinuing()) {
                debugger.pause();
            } else {
                debugger.continueRun();
            }
        }
    }

    private class StopDebugging implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            debugger.stop();
        }
    }

}
