package com.blazemeter.jmeter.debugger.elements;

import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.samplers.SampleResult;


public class AssertionDebug extends AbstractDebugElement<Assertion> implements Assertion {

    @Override
    public AssertionResult getResult(SampleResult sampleResult) {
        prepareBean();
        getHook().notify(this);
        return wrapped.getResult(sampleResult);
    }

}
