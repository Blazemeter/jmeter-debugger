package com.blazemeter.jmeter.debugger.engine;


import com.blazemeter.jmeter.debugger.elements.OriginalLink;
import com.blazemeter.jmeter.debugger.elements.TimerDebug;
import com.blazemeter.jmeter.debugger.elements.Wrapper;
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

public class Debugger implements StepTrigger {
    private static final Logger log = LoggingManager.getLoggerForClass();
    private final DebuggerFrontend frontend;
    private final TestTreeProvider treeProvider;
    private TreeClonerTG cloner;
    private boolean stopping;
    private Wrapper currentElement;
    private boolean isContinuing = false;
    protected DebuggerEngine engine;

    public Debugger(TestTreeProvider treeProvider, DebuggerFrontend frontend) {
        this.treeProvider = treeProvider;
        this.frontend = frontend;

        AbstractThreadGroup[] grps = getThreadGroups();
        if (grps.length > 0) {
            selectThreadGroup(grps[0]);
        } else {
            log.debug("Empty test plan");
        }
    }

    public AbstractThreadGroup[] getThreadGroups() {
        SearchClass<AbstractThreadGroup> searcher = new SearchClass<>(AbstractThreadGroup.class);
        treeProvider.getTestTree().traverse(searcher);
        return searcher.getSearchResults().toArray(new AbstractThreadGroup[0]);
    }

    public void selectThreadGroup(AbstractThreadGroup tg) {
        log.debug("Selecting thread group " + tg.getName() + ": " + tg);
        cloner = new TreeClonerTG(tg);
        treeProvider.getTestTree().traverse(cloner);
    }

    public HashTree getSelectedTree() {
        if (cloner == null) {
            throw new IllegalStateException();
        }
        return cloner.getClonedTree();
    }

    public void start() {
        log.debug("Start debugging");
        frontend.started();

        HashTree hashTree = getSelectedTree();
        StandardJMeterEngine.register(new StateListener()); // oh, dear, they use static field then clean it...
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
            currentElement = null;
        }
    }

    @Override
    public synchronized void notify(Wrapper wrapper) {
        if (stopping) {
            throw new JMeterStopThreadException();
        }

        TestElement wrappedElement = (TestElement) wrapper.getWrappedElement();

        if (wrapper instanceof TimerDebug) {
            ((TimerDebug) wrapper).setDelaying(isContinuing);
        }

        currentElement = wrapper;

        if (!isContinuing) {
            JMeterContext context = engine.getThreadContext();
            frontend.statusRefresh(context);
        }

        try {
            if (isContinuing && isBreakpoint(wrappedElement)) {
                pause();
            }

            if (!isContinuing) {
                frontend.frozenAt(wrapper);
                log.debug("Stopping before: " + wrappedElement);
                this.wait();
                frontend.continuing();
            }
        } catch (InterruptedException e) {
            log.debug("Interrupted", e);
            throw new JMeterStopThreadException(e);
        }
    }

    public void pause() {
        isContinuing = false;
    }

    public void continueRun() {
        isContinuing = true;
        proceed();
    }


    public Wrapper getCurrentElement() {
        return currentElement;
    }

    public Sampler getCurrentSampler() {
        return engine.getThreadContext().getCurrentSampler();
    }

    public boolean isContinuing() {
        return isContinuing;
    }

    public static boolean isBreakpoint(TestElement te) {
        if (te instanceof OriginalLink) {
            TestElement orig = (TestElement) ((OriginalLink) te).getOriginal();
            return orig.getPropertyAsBoolean(Debugger.class.getCanonicalName(), false);
        } else {
            return te.getPropertyAsBoolean(Debugger.class.getCanonicalName(), false);
        }
    }

    public synchronized void proceed() {
        notifyAll();
    }

    private class StateListener implements TestStateListener {
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
    }
}
