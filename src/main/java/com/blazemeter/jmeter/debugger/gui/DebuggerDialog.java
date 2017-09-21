package com.blazemeter.jmeter.debugger.gui;

import com.blazemeter.jmeter.debugger.elements.DebuggingThreadGroup;
import com.blazemeter.jmeter.debugger.elements.OriginalLink;
import com.blazemeter.jmeter.debugger.elements.ThreadGroupWrapper;
import com.blazemeter.jmeter.debugger.elements.Wrapper;
import com.blazemeter.jmeter.debugger.engine.Debugger;
import com.blazemeter.jmeter.debugger.engine.DebuggerFrontend;
import com.blazemeter.jmeter.debugger.engine.SearchClass;
import com.blazemeter.jmeter.debugger.engine.TestTreeProvider;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.control.ReplaceableController;
import org.apache.jmeter.engine.TreeCloner;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.SearchByClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DebuggerDialog extends DebuggerDialogBase implements DebuggerFrontend, TestTreeProvider {
    private static final Logger log = LoggerFactory.getLogger(DebuggerDialog.class);

    private boolean savedDirty = false;
    protected Debugger debugger = null;
    protected ResultCollector lastResultListener;

    public DebuggerDialog() {
        super();
        start.addActionListener(new StartDebugging());
        stop.addActionListener(new StopDebugging());
        step.addActionListener(new StepOver());
        pauseContinue.addActionListener(new PauseContinue());
        tgCombo.addItemListener(new ThreadGroupChoiceChanged());
        lastResultListener = (ResultCollector) lastSamplerResult.createTestElement();
    }

    @Override
    public void componentShown(ComponentEvent e) {
        log.debug("Showing dialog");
        if (GuiPackage.getInstance() != null) {
            savedDirty = GuiPackage.getInstance().isDirty();
        }
        this.debugger = new Debugger(this, this);
        tgCombo.removeAllItems();
        for (AbstractThreadGroup group : debugger.getThreadGroups()) {
            tgCombo.addItem(group);
        }

        AbstractThreadGroup selectedThreadGroup = debugger.getSelectedThreadGroup();
        if (selectedThreadGroup != null) {
            changeComboValue(selectedThreadGroup);
        }

        tgCombo.setEnabled(tgCombo.getItemCount() > 0);
        start.setEnabled(tgCombo.getItemCount() > 0);
        start.requestFocus();
        clearListeners();
    }

    private void changeComboValue(AbstractThreadGroup selectedThreadGroup) {
        ComboBoxModel<AbstractThreadGroup> model = this.tgCombo.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            if (model.getElementAt(i).equals(selectedThreadGroup)) {
                tgCombo.setSelectedIndex(i);
            }
        }
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        log.debug("Closing dialog");
        lastSamplerResult.clearData();
        debugger.stop();
        if (GuiPackage.getInstance() != null) {
            GuiPackage.getInstance().setDirty(savedDirty);
        }
        clearListeners();
    }

    @Override
    public HashTree getTestTree() {
        GuiPackage gui = GuiPackage.getInstance();
        return gui.getTreeModel().getTestPlan();
    }

    private void clearListeners() {
        GuiPackage guiPackage = GuiPackage.getInstance();
        for (JMeterTreeNode node : guiPackage.getTreeModel().getNodesOfType(Clearable.class)) {
            JMeterGUIComponent guiComp = guiPackage.getGui(node.getTestElement());
            if (guiComp instanceof Clearable){
                Clearable item = (Clearable) guiComp;
                try {
                    item.clearData();
                } catch (Exception ex) {
                    log.error("Can't clear: {} {}", node, guiComp, ex);
                }
            }
        }
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
        if (context.getPreviousResult() != null) {
            lastResultListener.sampleOccurred(new SampleEvent(context.getPreviousResult(), context.getThreadGroup().getName()));
        }
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

        Sampler sampler = debugger.getCurrentSampler();
        if (sampler != null) {
            TreePath samplerPath = getTreePathFor(sampler);
            if (samplerPath != null) {
                log.debug("Expanding: " + samplerPath);
                tree.expandPath(samplerPath.getParentPath());
            }
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
        HashTree origTree = debugger.getSelectedTree();
        TreeCloner cloner = new TreeCloner();
        origTree.traverse(cloner);
        HashTree selectedTree = cloner.getClonedTree();

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
        SearchByClass<DebuggingThreadGroup> tgs = new SearchByClass<>(DebuggingThreadGroup.class);
        selectedTree.traverse(tgs);
        for (DebuggingThreadGroup forSel : tgs.getSearchResults()) {
            Wrapper<AbstractThreadGroup> wtg = new ThreadGroupWrapper();
            wtg.setWrappedElement(forSel);
            selectTargetInTree(wtg);
        }
    }

    @Override
    public void highlightNode(Component component, JMeterTreeNode node, TestElement mc) {
        component.setFont(component.getFont().deriveFont(~Font.BOLD).deriveFont(~Font.ITALIC));
        TestElement userObject = (TestElement) node.getUserObject();
        if (Debugger.isBreakpoint(userObject)) {
            component.setForeground(Color.RED);
        }

        if (debugger == null) {
            return;
        }

        Wrapper currentElement = debugger.getCurrentElement();
        if (currentElement == null) {
            return;
        }

        Font font = component.getFont();
        TestElement currentWrapped = (TestElement) currentElement.getWrappedElement();
        if (mc == currentElement || mc == currentWrapped) {
            setComponentStyle(component, font, Font.BOLD, Font.BOLD);
        }

        Sampler currentSampler = debugger.getCurrentSampler();
        if (mc == currentSampler) { // can this ever happen?
            setComponentStyle(component, font, Font.BOLD + Font.ITALIC, font.getStyle() | Font.ITALIC);
        } else if (currentSampler instanceof Wrapper && mc == ((Wrapper) currentSampler).getWrappedElement()) {
            setComponentStyle(component, font, Font.BOLD + Font.ITALIC, font.getStyle() | Font.ITALIC);
        }
    }

    private void setComponentStyle(Component component, Font font, int macStyle, int otherOsStyle) {
        if (isMac()) {
            component.setFont(new Font(font.getName(), macStyle, font.getSize()));
            component.setForeground(Color.BLACK);
        } else {
            component.setFont(font.deriveFont(otherOsStyle));
            component.setForeground(Color.BLUE);
        }
    }

    public static boolean isMac() {
        return (System.getProperty("os.name").toLowerCase().contains("mac"));
    }

    @Override
    public void valueChanged(TreeSelectionEvent treeSelectionEvent) {
        JMeterTreeNode node = (JMeterTreeNode) treeSelectionEvent.getPath().getLastPathComponent();
        TestElement wrpElm = node.getTestElement();
        if (wrpElm instanceof OriginalLink) {
            TestElement te = (TestElement) ((OriginalLink) wrpElm).getOriginal();
            if (!(te instanceof AbstractThreadGroup)) {
                wrpElm = te;
            }
        }

        displayElementGui(wrpElm);
    }

    private synchronized void displayElementGui(TestElement wrpElm) {
        GuiPackage gui = GuiPackage.getInstance();
        if (gui != null) {
            JMeterGUIComponent egui = gui.getGui(wrpElm);
            egui.configure(wrpElm);
            egui.modifyTestElement(wrpElm);

            elementContainer.removeAll();
            if (egui instanceof Component) {
//                egui.setEnabled(false);
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
        try {
            refreshVars(context);
            refreshProperties();
        } catch (Throwable e) {
            log.warn("Problem refreshing status pane", e);
        }
        evaluatePanel.refresh(context, debugger.isContinuing());
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                tree.repaint();
            }
        });
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
