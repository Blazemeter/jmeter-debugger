package com.blazemeter.jmeter.debugger.elements;


import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestIterationListener;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.TestCompilerHelper;

public class GenericControllerDebug extends GenericController implements Wrapper<GenericController>, Controller, TestCompilerHelper, LoopIterationListener, TestIterationListener, TestStateListener {
    private final AbstractDebugElement<GenericController> helper;
    private final GenericController wrapped;

    public GenericControllerDebug(GenericController te) {
        helper = new AbstractDebugElement<GenericController>(te) {
        };
        this.wrapped = te;
    }

    @Override
    public Sampler next() {
        helper.getHook().notify(this);
        return wrapped.next();
    }

    @Override
    public boolean isDone() {
        return wrapped.isDone();
    }

    @Override
    public void addIterationListener(LoopIterationListener listener) {
        helper.prepareBean();
        wrapped.addIterationListener(listener);
    }

    @Override
    public void initialize() {
        wrapped.initialize();
    }

    @Override
    public void removeIterationListener(LoopIterationListener iterationListener) {
        wrapped.removeIterationListener(iterationListener);
    }

    @Override
    public void triggerEndOfLoop() {
        wrapped.triggerEndOfLoop();
    }

    @Override
    public void addTestElement(TestElement child) {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        if (stack[2].getMethodName().equals("addTestElementOnce")) { // final method with no reason, again
            TestCompilerHelper wrapped = this.wrapped;
            wrapped.addTestElementOnce(child);
        } else {
            wrapped.addTestElement(child);
        }
    }

    @Override
    public void iterationStart(LoopIterationEvent iterEvent) {
        if (wrapped instanceof LoopIterationListener) {
            ((LoopIterationListener) wrapped).iterationStart(iterEvent);
        }
    }

    @Override
    public void testIterationStart(LoopIterationEvent event) {
        if (wrapped instanceof TestIterationListener) {
            ((TestIterationListener) wrapped).testIterationStart(event);
        }
    }

    @Override
    public void testStarted() {
        if (wrapped instanceof TestStateListener) {
            ((TestStateListener) wrapped).testStarted();
        }
    }

    @Override
    public void testStarted(String host) {
        if (wrapped instanceof TestStateListener) {
            ((TestStateListener) wrapped).testStarted(host);
        }
    }

    @Override
    public void testEnded() {
        if (wrapped instanceof TestStateListener) {
            ((TestStateListener) wrapped).testEnded();
        }
    }

    @Override
    public void testEnded(String host) {
        if (wrapped instanceof TestStateListener) {
            ((TestStateListener) wrapped).testEnded(host);
        }
    }

    @Override
    public GenericController getWrappedElement() {
        return wrapped;
    }
}
