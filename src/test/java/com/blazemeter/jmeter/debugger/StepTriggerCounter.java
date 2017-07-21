package com.blazemeter.jmeter.debugger;

import com.blazemeter.jmeter.debugger.elements.Wrapper;
import com.blazemeter.jmeter.debugger.engine.StepTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class StepTriggerCounter implements StepTrigger {
    private static final Logger log = LoggerFactory.getLogger(StepTriggerCounter.class);

    public int cnt;

    @Override
    public void stepOn(Wrapper t) {
        log.warn("Stop before: " + t.getWrappedElement());
        cnt += 1;
    }
}
