package com.blazemeter.jmeter.debugger.engine;

import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;
import org.apache.jorphan.collections.ListedHashTree;

import java.util.LinkedList;

public class TreeClonerTG implements HashTreeTraverser {
    private ThreadGroup onlyTG;

    private ThreadGroup clonedOnlyTG;
    private final ListedHashTree newTree = new ListedHashTree();
    private final LinkedList<Object> stack = new LinkedList<>();
    private boolean ignoring = false;

    public TreeClonerTG(ThreadGroup tg) {
        this.onlyTG = tg;
    }

    @Override
    public final void addNode(Object node, HashTree subTree) {
        if (!ignoring && node instanceof ThreadGroup && !node.equals(onlyTG)) {
            ignoring = true;
        }

        if (!ignoring) {
            node = addNodeToTree(node);
        }

        stack.addLast(node);
    }

    protected Object addNodeToTree(Object node) {
        if (node instanceof TestElement) {
            Object cloned = ((TestElement) node).clone();
            if (node instanceof ThreadGroup && node.equals(onlyTG)) {
                clonedOnlyTG = (ThreadGroup) cloned;
            }
            node = cloned;
            newTree.add(stack, node);
        } else {
            newTree.add(stack, node);
        }
        return node;
    }

    @Override
    public void subtractNode() {
        if (stack.getLast() instanceof ThreadGroup && !stack.getLast().equals(onlyTG)) {
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

    public ThreadGroup getClonedTG() {
        return clonedOnlyTG;
    }

}
