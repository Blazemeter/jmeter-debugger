package com.blazemeter.jmeter.debugger.elements;

import com.blazemeter.jmeter.debugger.engine.StepTrigger;
import org.apache.jmeter.processor.PreProcessor;

public class PreProcessorDebug extends AbstractDebugElement<PreProcessor> implements PreProcessor {

    @Override
    public void process() {
        prepareBean();
        getHook().notify(this);
        wrapped.process();
    }
}
