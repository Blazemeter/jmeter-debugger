package com.blazemeter.jmeter.debugger;

import com.blazemeter.jmeter.debugger.elements.Wrapper;
import com.blazemeter.jmeter.debugger.engine.StepTrigger;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;


public class StepTriggerCounter implements StepTrigger {
    private static final Logger log = LoggingManager.getLoggerForClass();
    public int cnt;

    @Override
    public void stepOn(Wrapper t) {
        log.warn("Stop before: " + t.getWrappedElement());
        cnt += 1;
    }
}
