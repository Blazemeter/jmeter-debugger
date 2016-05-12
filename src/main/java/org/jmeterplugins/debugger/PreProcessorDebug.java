package org.jmeterplugins.debugger;

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
