package com.blazemeter.jmeter.debugger.gui;


import com.blazemeter.jmeter.debugger.elements.TimerDebug;
import com.blazemeter.jmeter.debugger.elements.Wrapper;
import com.blazemeter.jmeter.debugger.engine.DebuggerEngine;
import com.blazemeter.jmeter.debugger.engine.SearchClass;
import com.blazemeter.jmeter.debugger.engine.StepTrigger;
import com.blazemeter.jmeter.debugger.engine.TreeClonerTG;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.engine.JMeterEngineException;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterContextServiceAccessor;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JMeterStopThreadException;
import org.apache.log.Logger;

import java.util.HashSet;
import java.util.Set;

public class Debugger implements StepTrigger, TestStateListener {
    private static final Logger log = LoggingManager.getLoggerForClass();
    private final HashTree tree;
    private final DebuggerFrontend frontend;
    private TreeClonerTG cloner;
    private boolean stopping;
    private Wrapper currentElement;
    private boolean isContinuing = false;
    protected DebuggerEngine engine;
    private Sampler currentSampler;
    protected Set<TestElement> breakpoints = new HashSet<>();

    public Debugger(HashTree testTree, DebuggerFrontend frontend) {
        tree = testTree;
        this.frontend = frontend;

        AbstractThreadGroup[] grps = getThreadGroups();
        if (grps.length > 0) {
            selectThreadGroup(grps[0]);
        } else {
            log.debug("Empty test plan " + testTree);
        }
    }

    private void setCurrentElement(Wrapper te) {
        currentElement = te;
    }

    public AbstractThreadGroup[] getThreadGroups() {
        SearchClass<AbstractThreadGroup> searcher = new SearchClass<>(AbstractThreadGroup.class);
        tree.traverse(searcher);
        return searcher.getSearchResults().toArray(new AbstractThreadGroup[0]);
    }

    public void selectThreadGroup(AbstractThreadGroup tg) {
        log.debug("Selecting thread group " + tg.getName() + ": " + tg);
        cloner = new TreeClonerTG(tg);
        tree.traverse(cloner);
    }

    public HashTree getSelectedTree() {
        if (cloner == null) {
            throw new IllegalStateException();
        }
        return cloner.getClonedTree();
    }

    public AbstractThreadGroup getThreadGroupClone() {
        return cloner.getClonedTG();
    }


    public void start() {
        log.debug("Start debugging");
        frontend.clear();
        frontend.started();

        HashTree hashTree = getSelectedTree();
        StandardJMeterEngine.register(this); // oh, dear, they use static field then clean it...
        engine = new DebuggerEngine(JMeterContextService.getContext());
        engine.setStepper(this);
        JMeter.convertSubTree(hashTree);
        engine.configure(hashTree);
        try {
            engine.runTest();
        } catch (JMeterEngineException e) {
            log.error("Failed to pauseContinue debug", e);
            stop();
        }
    }

    public void stop() {
        log.debug("Stop debugging");

        if (stopping) {
            throw new IllegalStateException("Already stopping");
        }

        stopping = true;
        try {
            if (engine != null && engine.isActive()) {
                engine.stopTest(true);
            }
        } finally {
            stopping = false;
            frontend.stopped();
            JMeterContextServiceAccessor.removeContext();
            setCurrentElement(null);
        }
    }

    @Override
    public void notify(Wrapper wrapper) {
        if (stopping) {
            throw new JMeterStopThreadException();
        }

        TestElement wrappedElement = (TestElement) wrapper.getWrappedElement();

        if (wrapper instanceof TimerDebug) {
            ((TimerDebug) wrapper).setDelaying(isContinuing);
        }

        setCurrentElement(wrapper);
        JMeterContext context = engine.getThreadContext();
        frontend.statusRefresh(context);

        try {
            synchronized (this) {
                if (isContinuing && breakpoints.contains(wrappedElement)) {
                    pause();
                }

                if (!isContinuing) {
                    frontend.frozenAt(wrapper);
                    log.debug("Stopping before: " + wrappedElement);
                    this.wait();
                } else {
                    frontend.positionChanged();
                }
            }
        } catch (InterruptedException e) {
            log.debug("Interrupted", e);
            throw new JMeterStopThreadException(e);
        } finally {
            if (step.isEnabled()) {
                step.setEnabled(false);
            }
        }
    }

    public void pause() {
        isContinuing = false;
        frontend.paused();
    }

    public void continueRun() {
        isContinuing = true;
        frontend.continuing();
        notifyAll();
    }


    public Wrapper getCurrentElement() {
        return currentElement;
    }

    public Sampler getCurrentSampler() {
        return currentSampler;
    }

    public boolean isContinuing() {
        return false; // TODO
    }

    @Override
    public void testEnded() {
        stop();
    }

    @Override
    public void testStarted() {

    }

    @Override
    public void testStarted(String host) {

    }

    @Override
    public void testEnded(String host) {

    }

    public boolean isBreakpoint(TestElement userObject) {
        return breakpoints.contains(userObject);
    }

    public void removeBreakpoint(TestElement te) {
        breakpoints.remove(te);
    }

    public void addBreakpoint(TestElement te) {
        breakpoints.add(te);
    }
}
