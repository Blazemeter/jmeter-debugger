package com.blazemeter.jmeter.debugger.engine;

import com.blazemeter.jmeter.debugger.elements.AbstractDebugElement;

public interface StepTrigger {
    void notify(AbstractDebugElement t);
}