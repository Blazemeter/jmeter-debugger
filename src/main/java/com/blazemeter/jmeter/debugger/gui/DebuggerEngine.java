package com.blazemeter.jmeter.debugger.gui;

import com.blazemeter.jmeter.debugger.engine.DebuggingThread;
import com.blazemeter.jmeter.debugger.engine.StepTrigger;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.engine.TreeCloner;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.JMeterThreadMonitor;
import org.apache.jmeter.threads.ListenerNotifier;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.jorphan.collections.SearchByClass;


public class DebuggerEngine extends StandardJMeterEngine implements JMeterThreadMonitor {
    private final HashTree tree;
    private Thread thread;

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

    public HashTree getThreadGroupTree(ThreadGroup tg) {
        TreeCloner cloner = new TreeClonerOnlyFlow(tg);
        tree.traverse(cloner);
        ListedHashTree clonedTree = cloner.getClonedTree();
        for (Object key : clonedTree.keySet()) {
            return clonedTree.get(key);
        }
        return new HashTree();
    }

    public HashTree getExecutionTree(ThreadGroup selectedItem) {
        return null;
    }

    public void startDebugging(HashTree test, StepTrigger trigger) {
        ListenerNotifier note = new ListenerNotifier();
        thread = new Thread(new DebuggingThread(test, this, note, trigger));
        thread.setDaemon(true);
        thread.start();
    }

    public void stopDebugging() {
        if (thread.isAlive() && !thread.isInterrupted()) {
            thread.interrupt();
        }
    }

    @Override
    public void threadFinished(JMeterThread thread) {
        stopDebugging();
    }
}
