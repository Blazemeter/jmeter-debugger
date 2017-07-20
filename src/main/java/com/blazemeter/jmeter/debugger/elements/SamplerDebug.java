package com.blazemeter.jmeter.debugger.elements;

import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.threads.JMeterContext;


public class SamplerDebug extends AbstractDebugElement<Sampler> implements Sampler {

    @Override
    public SampleResult sample(Entry e) {
        prepareBean();
        getHook().stepOn(this);
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

    @Override
    public void setProperty(JMeterProperty property) {
        super.setProperty(property);
        if (wrapped != null && !TestElement.GUI_CLASS.equals(property.getName())) {
            wrapped.setProperty(property);
        }
    }
}
