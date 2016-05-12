package com.blazemeter.jmeter.debugger;

import com.blazemeter.jmeter.debugger.elements.AbstractDebugElement;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;

public class SampleListenerDebug extends AbstractDebugElement<SampleListener> implements SampleListener {
    public SampleListenerDebug(SampleListener te, StepTrigger hook) {
        super(te, hook);
    }

    @Override
    public void sampleOccurred(SampleEvent e) {
        hook.notify(this);
        parent.sampleOccurred(e);
    }

    @Override
    public void sampleStarted(SampleEvent e) {
        parent.sampleStarted(e);
    }

    @Override
    public void sampleStopped(SampleEvent e) {
        parent.sampleStopped(e);
    }
}
