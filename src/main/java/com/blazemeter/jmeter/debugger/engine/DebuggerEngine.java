package com.blazemeter.jmeter.debugger.engine;

import org.apache.jmeter.JMeter;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.engine.TreeCloner;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.threads.*;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.jorphan.collections.SearchByClass;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;


public class DebuggerEngine extends StandardJMeterEngine implements JMeterThreadMonitor {
    private static final Logger log = LoggingManager.getLoggerForClass();

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
        //noinspection LoopStatementThatDoesntLoop
        for (Object key : clonedTree.keySet()) {
            return clonedTree.get(key);
        }
        return new HashTree();
    }

    public HashTree getExecutionTree(ThreadGroup tg) {
        TreeCloner cloner = new TreeClonerSelectedThreadGroup(tg);
        tree.traverse(cloner);
        ListedHashTree clonedTree = cloner.getClonedTree();
        //noinspection LoopStatementThatDoesntLoop
        for (Object key : clonedTree.keySet()) {
            return clonedTree.get(key);
        }
        return new HashTree();
    }

    public void startDebugging(ThreadGroup tg, HashTree test, StepTrigger trigger) {
        log.debug("Start debugging engine");
        SampleEvent.initSampleVariables();
        JMeterContextService.startTest();
        JMeterContextService.getContext().setSamplingStarted(true);

        ListenerNotifier note = new ListenerNotifier();
        DebuggingThread target = new DebuggingThread(test, this, note, trigger);
        target.setEngine(this);
        target.setThreadGroup(tg);
        thread = new Thread(target);
        thread.setName(target.getThreadName());
        thread.setDaemon(true);
        thread.start();
    }

    public void stopDebugging() {
        log.debug("Stop debugging engine");
        if (thread.isAlive() && !thread.isInterrupted()) {
            thread.interrupt();
        }

        JMeterContextService.getContext().setSamplingStarted(false);
        JMeterContextService.endTest();
    }

    @Override
    public void threadFinished(JMeterThread thread) {
        log.info("Debugger thread has finished");
        stopDebugging();
    }
}
