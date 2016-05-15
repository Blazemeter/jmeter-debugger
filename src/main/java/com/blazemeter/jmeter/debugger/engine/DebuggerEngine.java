package com.blazemeter.jmeter.debugger.engine;

import com.blazemeter.jmeter.debugger.elements.AbstractDebugElement;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.*;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.jorphan.collections.SearchByClass;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public class DebuggerEngine extends StandardJMeterEngine {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private final HashTree tree;
    private Thread thread;
    private DebuggingThread target;
    private TreeClonerTG cloner;

    public DebuggerEngine(HashTree testTree) {
        JMeter.convertSubTree(testTree);
        configure(testTree);
        tree = testTree;
    }

    public ThreadGroup[] getThreadGroups() {
        SearchByClass<ThreadGroup> searcher = new SearchByClass<>(ThreadGroup.class);
        tree.traverse(searcher);
        return searcher.getSearchResults().toArray(new ThreadGroup[0]);
    }

    public void startDebugging(StepTrigger trigger, JMeterThreadMonitor stopListener) {
        log.debug("Start debugging engine");
        SampleEvent.initSampleVariables();
        JMeterContextService.startTest();
        JMeterContextService.getContext().setSamplingStarted(true);

        ListenerNotifier note = new ListenerNotifier();
        target = new DebuggingThread(getThreadTestTree(), stopListener, note, trigger);
        target.setEngine(this);
        target.setThreadGroup(cloner.getClonedTG());
        thread = new Thread(target);
        thread.setName(target.getThreadName());
        thread.setDaemon(true);
        thread.start();
    }

    private HashTree getThreadTestTree() {
        HashTree test = cloner.getClonedTree();
        List<?> testLevelElements = new LinkedList<>(test.list(test.getArray()[0]));
        Iterator it = testLevelElements.iterator();
        while (it.hasNext()) {
            if (it.next() instanceof ThreadGroup) {
                it.remove();
            }
        }

        SearchByClass<ThreadGroup> searcher = new SearchByClass<>(ThreadGroup.class);
        test.traverse(searcher);
        ThreadGroup tg = cloner.getClonedTG();
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

    public Sampler getCurrentSampler() {
        Sampler currentSampler = target.getCurrentSampler();
        if (currentSampler instanceof AbstractDebugElement) {
            Object wrapped = ((AbstractDebugElement) currentSampler).getWrappedElement();
            if (wrapped instanceof Sampler) {
                return (Sampler) wrapped;
            }
        }
        return currentSampler;
    }

    public JMeterContext getThreadContext() {
        return target.getThreadContext();
    }

    public void selectThreadGroup(ThreadGroup tg) {
        cloner = new TreeClonerTG(tg);
        tree.traverse(cloner);
    }

    public HashTree getFullTree() {
        return cloner.getClonedTree();
    }
}
