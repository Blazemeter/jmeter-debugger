package com.blazemeter.jmeter.debugger.gui;

import org.apache.jmeter.JMeter;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.SearchByClass;


public class DebuggerEngine extends StandardJMeterEngine {
    private final HashTree test;

    public DebuggerEngine(HashTree testTree) {
        JMeter.convertSubTree(testTree);
        //testTree.add(testTree.getArray()[0], gui.getMainFrame()); // for what?
        configure(testTree);
        test = testTree;
    }

    public ThreadGroup[] getThreadGroups() {
        SearchByClass<ThreadGroup> searcher = new SearchByClass<>(ThreadGroup.class);
        test.traverse(searcher);
        return searcher.getSearchResults().toArray(new ThreadGroup[0]);
    }
}
