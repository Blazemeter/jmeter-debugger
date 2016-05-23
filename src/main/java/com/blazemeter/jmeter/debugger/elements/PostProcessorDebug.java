package com.blazemeter.jmeter.debugger.elements;

import com.blazemeter.jmeter.debugger.engine.StepTrigger;
import org.apache.jmeter.processor.PostProcessor;

public class PostProcessorDebug extends AbstractDebugElement<PostProcessor> implements PostProcessor {


    public PostProcessorDebug(PostProcessor te, StepTrigger hook) {
        super(te, hook);
    }

    @Override
    public void process() {
        hook.notify(this);
        wrapped.process();
    }
}
