package com.blazemeter.jmeter.debugger.elements;

import com.blazemeter.jmeter.debugger.engine.StepTrigger;
import org.apache.jmeter.control.Controller;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.samplers.Sampler;


public class ControllerDebug extends AbstractDebugElement<Controller> implements Controller {
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
}
