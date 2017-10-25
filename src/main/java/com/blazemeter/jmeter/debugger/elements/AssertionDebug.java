package com.blazemeter.jmeter.debugger.elements;

import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;


public class AssertionDebug extends AbstractDebugElement<Assertion> implements Assertion {

    @Override
    public void setProperty(JMeterProperty property) {
        super.setProperty(property);
        if (wrapped instanceof TestElement) {
            ((TestElement) wrapped).setProperty(property);
        }
    }

    @Override
    public AssertionResult getResult(SampleResult sampleResult) {
        prepareBean();
        getHook().stepOn(this);
        return wrapped.getResult(sampleResult);
    }

}
