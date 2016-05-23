package com.blazemeter.jmeter.debugger.elements;

import com.blazemeter.jmeter.debugger.engine.StepTrigger;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;

public class SampleListenerDebug extends AbstractDebugElement<SampleListener> implements SampleListener {
    public SampleListenerDebug(SampleListener te, StepTrigger hook) {
        super(te, hook);
    }

    @Override
    public void sampleOccurred(SampleEvent e) {
        hook.notify(this);
        wrapped.sampleOccurred(e);
        hook.notify(this);
    }

    @Override
    public void sampleStarted(SampleEvent e) {
        wrapped.sampleStarted(e);
    }

    @Override
    public void sampleStopped(SampleEvent e) {
        wrapped.sampleStopped(e);
    }
}
