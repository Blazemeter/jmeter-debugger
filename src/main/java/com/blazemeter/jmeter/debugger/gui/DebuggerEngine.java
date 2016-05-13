package com.blazemeter.jmeter.debugger.gui;

import org.apache.jmeter.JMeter;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.engine.TreeCloner;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.jorphan.collections.SearchByClass;


public class DebuggerEngine extends StandardJMeterEngine {
    private final HashTree tree;

    public DebuggerEngine(HashTree testTree) {
        JMeter.convertSubTree(testTree);
        //testTree.add(testTree.getArray()[0], gui.getMainFrame()); // for what?
        configure(testTree);
        tree = testTree;
    }

    public ThreadGroup[] getThreadGroups() {
        SearchByClass<ThreadGroup> searcher = new SearchByClass<>(ThreadGroup.class);
        tree.traverse(searcher);
        return searcher.getSearchResults().toArray(new ThreadGroup[0]);
    }

    public HashTree getThreadgroupTree(ThreadGroup tg) {
        TreeCloner cloner = new TreeClonerOnlyFlow(tg);
        tree.traverse(cloner);
        ListedHashTree clonedTree = cloner.getClonedTree();
        for (Object key : clonedTree.keySet()) {
            return clonedTree.get(key);
        }
        return new HashTree();
    }
}
