package com.blazemeter.jmeter.debugger.engine;


import com.blazemeter.jmeter.debugger.elements.OriginalLink;
import com.blazemeter.jmeter.debugger.elements.TimerDebug;
import com.blazemeter.jmeter.debugger.elements.Wrapper;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.engine.JMeterEngineException;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterContextServiceAccessor;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.util.JMeterStopThreadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Debugger implements StepTrigger {
    private static final Logger log = LoggerFactory.getLogger(Debugger.class);
    private final DebuggerFrontend frontend;
    private final TestTreeProvider treeProvider;
    private TreeClonerTG cloner;
    private boolean stopping;
    private Wrapper currentElement;
    private boolean isContinuing = false;
    protected DebuggerEngine engine;
    private Sampler lastKnownSampler;
    private final TestElement lastSamplerResult;

    public Debugger(TestTreeProvider treeProvider, DebuggerFrontend frontend, TestElement lastSamplerResult) {
        this.treeProvider = treeProvider;
        this.frontend = frontend;
        this.lastSamplerResult = lastSamplerResult;
    }

    public AbstractThreadGroup getSelectedThreadGroup() {
        AbstractThreadGroup[] grps = getThreadGroups();
        if (grps.length > 0) {
            return getSelectedThreadGroup(grps);
        } else {
            log.debug("Empty test plan");
            return null;
        }
    }

    private AbstractThreadGroup getSelectedThreadGroup(AbstractThreadGroup[] grps) {
        JMeterTreeNode currentElement = GuiPackage.getInstance().getCurrentNode();
        if (currentElement != null) {
            SearchParentClass<AbstractThreadGroup> searcher = new SearchParentClass(currentElement, AbstractThreadGroup.class);
            treeProvider.getTestTree().traverse(searcher);
            return searcher.hasResults() ? searcher.getSearchResults().get(0) : grps[0];
        }
        return grps[0];
    }

    public AbstractThreadGroup[] getThreadGroups() {
        SearchClass<AbstractThreadGroup> searcher = new SearchClass<>(AbstractThreadGroup.class);
        treeProvider.getTestTree().traverse(searcher);
        return searcher.getSearchResults().toArray(new AbstractThreadGroup[0]);
    }

    public void selectThreadGroup(AbstractThreadGroup tg) {
        log.debug("Selecting thread group " + tg.getName() + ": " + tg);
        cloner = new TreeClonerTG(tg, lastSamplerResult);
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
    public synchronized void stepOn(Wrapper wrapper) {
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
            boolean isBP = isBreakpoint(wrapper) || isSamplerBreakpoint();
            if (isContinuing && isBP) {
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

    private boolean isSamplerBreakpoint() {
        boolean res = false;
        Sampler sampler = getCurrentSampler();
        if (sampler != lastKnownSampler) {
            res = isBreakpoint(sampler);
            lastKnownSampler = sampler;
        }
        return res;
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
        if (engine == null) {
            return null;
        }

        JMeterContext threadContext = engine.getThreadContext();
        return threadContext.getCurrentSampler();
    }

    public boolean isContinuing() {
        return isContinuing;
    }

    public static boolean isBreakpoint(TestElement te) {
        if (te instanceof OriginalLink) {
            te = (TestElement) ((OriginalLink) te).getOriginal();
        }
        return te.getPropertyAsBoolean(Debugger.class.getCanonicalName(), false);
    }

    public static void toggleBreakpoint(TestElement te) {
        if (te instanceof OriginalLink) {
            te = (TestElement) ((OriginalLink) te).getOriginal();
        }
        boolean isBP = te.getPropertyAsBoolean(Debugger.class.getCanonicalName(), false);
        te.setProperty(Debugger.class.getCanonicalName(), !isBP);
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
