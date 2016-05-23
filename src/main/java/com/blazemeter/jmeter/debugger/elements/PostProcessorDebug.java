package com.blazemeter.jmeter.debugger.elements;

import org.apache.jmeter.processor.PostProcessor;

public class PostProcessorDebug extends AbstractDebugElement<PostProcessor> implements PostProcessor {

    public PostProcessorDebug(PostProcessor te) {
        super(te);
    }

    @Override
    public void process() {
        prepareBean();
        getHook().notify(this);
        wrapped.process();
    }
}
