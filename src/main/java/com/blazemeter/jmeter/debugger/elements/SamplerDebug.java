package com.blazemeter.jmeter.debugger.elements;

import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterContext;


public class SamplerDebug extends AbstractDebugElement<Sampler> implements Sampler {
    public SamplerDebug(Sampler te) {
        super(te);
    }

    @Override
    public SampleResult sample(Entry e) {
        getHook().notify(this);
        return wrapped.sample(e);
    }

    @Override
    public void setThreadContext(JMeterContext inthreadContext) {
        wrapped.setThreadContext(inthreadContext);
    }

    @Override
    public void setThreadName(String inthreadName) {
        wrapped.setThreadName(inthreadName);
    }
}
