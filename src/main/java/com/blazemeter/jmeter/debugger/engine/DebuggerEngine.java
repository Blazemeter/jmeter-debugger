package com.blazemeter.jmeter.debugger.engine;

import org.apache.jmeter.JMeter;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterThreadMonitor;
import org.apache.jmeter.threads.ListenerNotifier;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.jorphan.collections.SearchByClass;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.util.*;


public class DebuggerEngine extends StandardJMeterEngine {
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

    public HashTree getFullTree(ThreadGroup tg) {
        log.debug("Making execution tree for " + tg.getName());


        Set<Object> testPlans = tree.keySet();
        for (Object testPlan : testPlans) {
            HashTree tpContents = tree.get(testPlan);
            Set<Object> toDelete = new HashSet<>();
            for (Object elm : tpContents.keySet()) {
                if (elm instanceof ThreadGroup && !elm.equals(tg)) {
                    toDelete.add(elm);
                }
            }

            for (Object del : toDelete) {
                tpContents.remove(del);
            }
        }

        return tree;
    }

    public void startDebugging(ThreadGroup tg, HashTree test, StepTrigger trigger, JMeterThreadMonitor stopListener) {
        log.debug("Start debugging engine");
        SampleEvent.initSampleVariables();
        JMeterContextService.startTest();
        JMeterContextService.getContext().setSamplingStarted(true);

        ListenerNotifier note = new ListenerNotifier();
        DebuggingThread target = new DebuggingThread(getThreadTestTree(test, tg), stopListener, note, trigger);
        target.setEngine(this);
        target.setThreadGroup(tg);
        thread = new Thread(target);
        thread.setName(target.getThreadName());
        thread.setDaemon(true);
        thread.start();
    }

    private HashTree getThreadTestTree(HashTree test, ThreadGroup tg) {
        List<?> testLevelElements = new LinkedList<>(test.list(test.getArray()[0]));
        Iterator it = testLevelElements.iterator();
        while (it.hasNext()) {
            if (it.next() instanceof ThreadGroup) {
                it.remove();
            }
        }

        SearchByClass<ThreadGroup> searcher = new SearchByClass<>(ThreadGroup.class);
        test.traverse(searcher);
        ListedHashTree threadGroupTree = (ListedHashTree) searcher.getSubTree(tg);
        threadGroupTree.add(tg, testLevelElements);
        return threadGroupTree;
    }

    public void stopDebugging() {
        log.debug("Stop debugging engine");
        if (thread != null) {
            if (thread.isAlive() && !thread.isInterrupted()) {
                thread.interrupt();
            }
        }

        JMeterContextService.getContext().setSamplingStarted(false);
        JMeterContextService.endTest();
    }
}
