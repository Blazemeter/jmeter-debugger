package org.jmeterplugins.debugger;

import org.apache.jmeter.processor.PostProcessor;

public class PostProcessorDebug extends AbstractDebugElement<PostProcessor> implements PostProcessor {


    public PostProcessorDebug(PostProcessor te, StepTrigger hook) {
        super(te, hook);
    }

    @Override
    public void process() {
        hook.notify(this);
        parent.process();
    }
}
