package com.blazemeter.jmeter.debugger.elements;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.TestCompilerHelper;


public class ControllerDebug extends AbstractDebugElement<Controller> implements Controller, TestCompilerHelper {
    public ControllerDebug(Controller te) {
        super(te);
    }

    @Override
    public Sampler next() {
        getHook().notify(this);
        return wrapped.next();
    }

    @Override
    public boolean isDone() {
        return wrapped.isDone();
    }

    @Override
    public void addIterationListener(LoopIterationListener listener) {
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
            ((TestCompilerHelper) wrapped).addTestElementOnce(child);
        }
        
        return false;
    }
}
