package com.blazemeter.jmeter.debugger.engine;

import com.blazemeter.jmeter.debugger.elements.DebuggingThreadGroupGui;
import com.blazemeter.jmeter.debugger.elements.DebuggingThreadGroup;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;
import org.apache.jorphan.collections.ListedHashTree;

import java.util.LinkedList;

public class TreeClonerTG implements HashTreeTraverser {
    private AbstractThreadGroup onlyTG;

    private AbstractThreadGroup clonedOnlyTG;
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

        if (te instanceof AbstractThreadGroup) {
            AbstractThreadGroup orig = (AbstractThreadGroup) cloned;
            clonedOnlyTG = new DebuggingThreadGroup();
            clonedOnlyTG.setProperty(TestElement.GUI_CLASS, DebuggingThreadGroupGui.class.getCanonicalName());
            clonedOnlyTG.setName(orig.getName());
            res.setUserObject(clonedOnlyTG);
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
        return clonedOnlyTG;
    }

}
