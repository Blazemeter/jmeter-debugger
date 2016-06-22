package com.blazemeter.jmeter.debugger.elements;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestIterationListener;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.TestCompilerHelper;


public class ControllerDebug extends AbstractDebugElement<Controller> implements FullController {
    

    @Override
    public Sampler next() {
        getHook().stepOn(this);
        return wrapped.next();
    }

    @Override
    public boolean isDone() {
        return wrapped.isDone();
    }

    @Override
    public void addIterationListener(LoopIterationListener listener) {
        prepareBean();
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
    public boolean addTestElementOnce(TestElement child) {
        if (wrapped instanceof TestCompilerHelper) {
            TestCompilerHelper wrapped = (TestCompilerHelper) this.wrapped;
            return wrapped.addTestElementOnce(child);
        }

        return false;
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
}
