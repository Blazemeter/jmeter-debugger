package com.blazemeter.jmeter.debugger.engine;

import com.blazemeter.jmeter.debugger.elements.*;
import org.apache.jmeter.control.*;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.util.LinkedList;

public class TreeClonerTG implements HashTreeTraverser {
    private static final Logger log = LoggingManager.getLoggerForClass();
    private AbstractThreadGroup onlyTG;

    private final ListedHashTree newTree = new ListedHashTree();
    private final LinkedList<Object> stack = new LinkedList<>();
    private boolean ignoring = false;
    private AbstractThreadGroup clonedTG;

    public TreeClonerTG(AbstractThreadGroup tg) {
        this.onlyTG = tg;
    }

    @Override
    public final void addNode(Object node, HashTree subTree) {
        if (!ignoring && isIgnored(node)) {
            ignoring = true;
        }

        if (!ignoring) {
            node = addNodeToTree(node);
        }
        stack.addLast(node);
    }

    private boolean isIgnored(Object node) {
        if (node instanceof JMeterTreeNode) {
            Object te = ((JMeterTreeNode) node).getUserObject();
            return te instanceof AbstractThreadGroup && !te.equals(onlyTG);
        }
        return false;
    }

    protected Object addNodeToTree(Object node) {
        if (node instanceof JMeterTreeNode) {
            node = getClonedNode((JMeterTreeNode) node);
            newTree.add(stack, node);
        } else {
            throw new IllegalArgumentException();
        }
        return node;
    }

    private JMeterTreeNode getClonedNode(JMeterTreeNode node) {
        TestElement te = (TestElement) (node).getUserObject();
        TestElement cloned = (TestElement) te.clone();
        boolean isWrappable = !(cloned instanceof TransactionController) && !(cloned instanceof TestFragmentController);
        JMeterTreeNode res = new JMeterTreeNode();

        if (cloned instanceof AbstractThreadGroup) {
            AbstractThreadGroup wrapped = new DebuggingThreadGroup();
            clonedTG = wrapped;
            wrapped.setProperty(TestElement.GUI_CLASS, DebuggingThreadGroupGui.class.getCanonicalName());
            wrapped.setName(cloned.getName());
            wrapped.setEnabled(cloned.isEnabled());
            res.setUserObject(wrapped);
        } else if (cloned instanceof Controller && isWrappable) {
            TestElement wrapped;
            if (cloned instanceof GenericController) {
                if (cloned instanceof ReplaceableController) {     // TODO: solve replaceable problem
                    log.warn("Not supported!: " + cloned);
                    wrapped = new ReplaceableGenericControllerDebug((GenericController) cloned);
                } else {
                    wrapped = new GenericControllerDebug((GenericController) cloned);
                }
            } else {
                if (cloned instanceof ReplaceableController) {
                    log.warn("Controller+Replaceable is unsupported: " + cloned);
                }
                wrapped = new ControllerDebug((Controller) cloned);
            }
            wrapped.setProperty(TestElement.GUI_CLASS, ControllerDebugGui.class.getCanonicalName());
            wrapped.setName(cloned.getName());
            wrapped.setEnabled(cloned.isEnabled());
            res.setUserObject(wrapped);
        } else {
            res.setUserObject(cloned);
        }

        return res;
    }

    @Override
    public void subtractNode() {
        if (isIgnored(stack.getLast())) {
            ignoring = false;
        }
        stack.removeLast();
    }

    @Override
    public void processPath() {
    }

    public HashTree getClonedTree() {
        return newTree;
    }

    public AbstractThreadGroup getClonedTG() {
        return clonedTG;
    }
}
