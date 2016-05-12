package org.jmeterplugins.debugger;

import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.control.Controller;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.processor.PreProcessor;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.threads.SamplePackage;
import org.apache.jmeter.timers.Timer;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.util.LinkedList;
import java.util.List;

public class DebuggerSamplerPackage extends SamplePackage {
    private static final Logger log = LoggingManager.getLoggerForClass();
    private StepTrigger hook;

    public DebuggerSamplerPackage(SamplePackage origin, List<Controller> controller, StepTrigger hook) {
        super(origin.getConfigs(),
                origin.getSampleListeners(),
                origin.getTimers(),
                origin.getAssertions(),
                origin.getPostProcessors(),
                origin.getPreProcessors(),
                controller);
        this.hook = hook;
        setSampler(origin.getSampler());
    }

    @Override
    public List<ConfigTestElement> getConfigs() {
        return super.getConfigs();
    }

    @Override
    public List<PreProcessor> getPreProcessors() {
        List<PreProcessor> preProcessors = new LinkedList<>();
        for (PreProcessor te : super.getPreProcessors()) {
            preProcessors.add(new PreProcessorDebug(te, hook));
        }
        return preProcessors;
    }

    @Override
    public List<PostProcessor> getPostProcessors() {
        List<PostProcessor> postProcessors = new LinkedList<>();
        for (PostProcessor te : super.getPostProcessors()) {
            postProcessors.add(new PostProcessorDebug(te, hook));
        }
        return postProcessors;
    }

    @Override
    public List<Assertion> getAssertions() {
        return super.getAssertions();
    }

    @Override
    public List<Timer> getTimers() {
        return super.getTimers();
    }

    @Override
    public List<SampleListener> getSampleListeners() {
        return super.getSampleListeners();
    }
}
