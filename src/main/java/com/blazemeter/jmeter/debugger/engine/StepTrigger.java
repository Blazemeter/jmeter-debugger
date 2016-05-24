package com.blazemeter.jmeter.debugger.engine;

import com.blazemeter.jmeter.debugger.elements.Wrapper;

public interface StepTrigger {
    void notify(Wrapper t);
}