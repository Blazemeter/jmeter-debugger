package com.blazemeter.jmeter.debugger.elements;

import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBeanHelper;
import org.apache.jmeter.testelement.TestElement;


public class AssertionDebug extends AbstractDebugElement<Assertion> implements Assertion {
    public AssertionDebug(Assertion te) {
        super(te);
    }

    @Override
    public AssertionResult getResult(SampleResult sampleResult) {
        prepareBean();
        getHook().notify(this);
        return wrapped.getResult(sampleResult);
    }

}
