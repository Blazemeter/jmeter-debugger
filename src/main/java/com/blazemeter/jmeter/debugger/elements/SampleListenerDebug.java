package com.blazemeter.jmeter.debugger.elements;

import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;

public class SampleListenerDebug extends AbstractDebugElement<SampleListener> implements SampleListener {
    public SampleListenerDebug(SampleListener te) {
        super(te);
    }

    @Override
    public void sampleOccurred(SampleEvent e) {
        prepareBean();
        getHook().notify(this);
        wrapped.sampleOccurred(e);
        getHook().notify(this); // special case for reporters to see the result
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
