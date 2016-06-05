package com.blazemeter.jmeter.debugger.elements;

import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.Visualizer;

public class SampleListenerDebug extends AbstractDebugElement<SampleListener> implements SampleListener, Visualizer {
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

    @Override
    public void add(SampleResult sample) {
        if (wrapped instanceof Visualizer) {
            ((Visualizer) wrapped).add(sample);
        }
    }

    @Override
    public boolean isStats() {
        if (wrapped instanceof Visualizer) {
            return ((Visualizer) wrapped).isStats();
        }
        return false;
    }
}
