package com.blazemeter.jmeter.debugger.engine;

import com.blazemeter.jmeter.debugger.elements.*;
import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.control.Controller;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.processor.PreProcessor;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.Sampler;
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
        List<PreProcessor> wrapped = new LinkedList<>();
        for (PreProcessor te : super.getPreProcessors()) {
            wrapped.add(new PreProcessorDebug(te, hook));
        }
        return wrapped;
    }

    @Override
    public List<PostProcessor> getPostProcessors() {
        List<PostProcessor> wrapped = new LinkedList<>();
        for (PostProcessor te : super.getPostProcessors()) {
            wrapped.add(new PostProcessorDebug(te, hook));
        }
        return wrapped;
    }

    @Override
    public List<Assertion> getAssertions() {
        List<Assertion> wrapped = new LinkedList<>();
        for (Assertion te : super.getAssertions()) {
            wrapped.add(new AssertionDebug(te, hook));
        }
        return wrapped;
    }

    @Override
    public List<Timer> getTimers() {
        List<Timer> wrapped = new LinkedList<>();
        for (Timer te : super.getTimers()) {
            wrapped.add(new TimerDebug(te, hook));
        }
        return wrapped;
    }

    @Override
    public List<SampleListener> getSampleListeners() {
        List<SampleListener> wrapped = new LinkedList<>();
        for (SampleListener te : super.getSampleListeners()) {
            wrapped.add(new SampleListenerDebug(te, hook));
        }
        return wrapped;
    }

    @Override
    public Sampler getSampler() {
        return new SamplerDebug(super.getSampler(), hook);
    }
}
