package com.blazemeter.jmeter.debugger.engine;

import com.blazemeter.jmeter.debugger.elements.ControllerDebug;
import com.blazemeter.jmeter.debugger.elements.DebuggingThreadGroup;
import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.TestFragmentController;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;
import org.apache.jorphan.collections.ListedHashTree;

import java.util.LinkedList;

public class TreeClonerTG implements HashTreeTraverser {
    private AbstractThreadGroup onlyTG;

    private final ListedHashTree newTree = new ListedHashTree();
    private final LinkedList<Object> stack = new LinkedList<>();
    private boolean ignoring = false;

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
        JMeterTreeNode res = new JMeterTreeNode();

        if (cloned instanceof AbstractThreadGroup) {
            AbstractThreadGroup orig = (AbstractThreadGroup) cloned;
            AbstractThreadGroup wrapped = new DebuggingThreadGroup();
            wrapped.setProperty(TestElement.GUI_CLASS, cloned.getPropertyAsString(TestElement.GUI_CLASS));
            wrapped.setName(orig.getName());
            res.setUserObject(wrapped);
        } else if (cloned instanceof Controller && !(cloned instanceof TestFragmentController)) {
            ControllerDebug wrapped = new ControllerDebug((Controller) cloned);
            wrapped.setProperty(TestElement.GUI_CLASS, cloned.getPropertyAsString(TestElement.GUI_CLASS));
            wrapped.setName(cloned.getName());
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
}
