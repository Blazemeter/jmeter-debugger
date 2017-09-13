package com.blazemeter.jmeter.debugger.engine;

import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;

import java.util.LinkedList;
import java.util.List;

public class SearchParentClass<T> implements HashTreeTraverser {
    private final List<T> results = new LinkedList<>();

    private final JMeterTreeNode searchNode;
    private final Class<T> searchParentClass;

    public SearchParentClass(JMeterTreeNode searchNode, Class<T> searchParentClass) {
        this.searchNode = searchNode;
        this.searchParentClass = searchParentClass;
    }

    public List<T> getSearchResults() {
        return results;
    }

    public boolean hasResults() {
        return !results.isEmpty();
    }

    @Override
    public void addNode(Object node, HashTree subTree) {
        if (node instanceof JMeterTreeNode) {
            JMeterTreeNode jMeterTreeNode = ((JMeterTreeNode) node);
            if (node.equals(searchNode)) {
                T parent = findParentByClass(jMeterTreeNode);
                if (parent != null) {
                    results.add(parent);
                }
            }
        }
    }

    private T findParentByClass(JMeterTreeNode node) {
        if (node != null) {
            Object userObject = node.getUserObject();
            return searchParentClass.isAssignableFrom(userObject.getClass()) ?
                    (T) userObject :
                    findParentByClass((JMeterTreeNode) node.getParent());
        }

        return null;
    }

    @Override
    public void subtractNode() {
    }

    @Override
    public void processPath() {
    }
}
