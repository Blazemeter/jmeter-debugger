package com.blazemeter.jmeter.debugger.elements;

import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.samplers.SampleResult;
import com.blazemeter.jmeter.debugger.engine.StepTrigger;


public class AssertionDebug extends AbstractDebugElement<Assertion> implements Assertion {
    public AssertionDebug(Assertion te, StepTrigger hook) {
        super(te, hook);
    }

    @Override
    public AssertionResult getResult(SampleResult sampleResult) {
        hook.notify(this);
        return parent.getResult(sampleResult);
    }
}
