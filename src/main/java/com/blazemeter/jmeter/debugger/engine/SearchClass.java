package com.blazemeter.jmeter.debugger.engine;

import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;
import org.apache.jorphan.collections.ListedHashTree;

import java.util.*;

public class SearchClass<T> implements HashTreeTraverser {
    private final List<T> results = new LinkedList<>();

    private final Map<Object, ListedHashTree> subTrees = new HashMap<>();

    private final Class<? extends T> searchClass;

    public SearchClass(Class<? extends T> searchClass) {
        this.searchClass = searchClass;
    }

    public Collection<T> getSearchResults() { // TODO specify collection type without breaking callers
        return results;
    }


    @Override
    public void addNode(Object node, HashTree subTree) {
        if (node instanceof JMeterTreeNode) {
            Object userObj = ((JMeterTreeNode) node).getUserObject();
            if (searchClass.isAssignableFrom(userObj.getClass())) {
                //noinspection unchecked
                results.add((T) userObj);
            }
        }
    }

    @Override
    public void subtractNode() {
    }

    @Override
    public void processPath() {
    }
}
