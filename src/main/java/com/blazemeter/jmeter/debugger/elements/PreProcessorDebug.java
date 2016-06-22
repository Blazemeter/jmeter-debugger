package com.blazemeter.jmeter.debugger.elements;

import org.apache.jmeter.processor.PreProcessor;

public class PreProcessorDebug extends AbstractDebugElement<PreProcessor> implements PreProcessor {

    @Override
    public void process() {
        prepareBean();
        getHook().stepOn(this);
        wrapped.process();
    }
}
