package com.blazemeter.jmeter.debugger.elements;

import com.blazemeter.jmeter.debugger.engine.StepTrigger;
import org.apache.jmeter.processor.PreProcessor;

public class PreProcessorDebug extends AbstractDebugElement<PreProcessor> implements PreProcessor {

    public PreProcessorDebug(PreProcessor te, StepTrigger hook) {
        super(te, hook);
    }

    @Override
    public void process() {
        hook.notify(this);
        parent.process();
    }
}
