package com.blazemeter.jmeter.debugger.elements;

import com.blazemeter.jmeter.debugger.StepTrigger;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterContext;


public class SamplerDebug extends AbstractDebugElement<Sampler> implements Sampler {
    public SamplerDebug(Sampler te, StepTrigger hook) {
        super(te, hook);
    }

    @Override
    public SampleResult sample(Entry e) {
        hook.notify(this);
        return parent.sample(e);
    }

    @Override
    public void setThreadContext(JMeterContext inthreadContext) {
        parent.setThreadContext(inthreadContext);
    }

    @Override
    public void setThreadName(String inthreadName) {
        parent.setThreadName(inthreadName);
    }
}
