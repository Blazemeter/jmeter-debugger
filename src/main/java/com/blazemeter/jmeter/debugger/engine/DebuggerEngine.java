package com.blazemeter.jmeter.debugger.engine;

import com.blazemeter.jmeter.debugger.elements.AbstractDebugElement;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;


public class DebuggerEngine extends StandardJMeterEngine {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private Thread thread;
    private DebuggingThread target;
    private StepTrigger stepper = new StepTrigger() {
        @Override
        public void notify(AbstractDebugElement t) {
            throw new RuntimeException("Not initialized stepper");
        }
    };

    /*
        public void startDebugging(StepTrigger trigger, JMeterThreadMonitor stopListener) {
            log.debug("Start debugging engine");
            SampleEvent.initSampleVariables();

            JMeterContextService.startTest();

            // enable variable resolve
            HashTree test = getThreadTestTree();
            PreCompiler compiler = new PreCompiler();
            test.traverse(compiler);

            test.traverse(new TurnElementsOn());

            JMeterContextService.getContext().setSamplingStarted(true);

            ListenerNotifier note = new ListenerNotifier();
            target = new DebuggingThread(test, stopListener, note, trigger);
            target.setEngine(this);
            target.setThreadGroup(cloner.getClonedTG());

            thread = new Thread(target);
            thread.setName(target.getThreadName());
            thread.setDaemon(true);
            thread.start();
        }


        public void stopDebugging() {
            log.debug("Stop debugging engine");
            if (thread != null) {
                if (thread.isAlive() && !thread.isInterrupted()) {
                    thread.interrupt();
                }
            }

            JMeterContextService.getContext().setSamplingStarted(false);
            JMeterContextService.endTest();
        }
    */
    public Sampler getCurrentSampler() {
        Sampler currentSampler = target.getCurrentSampler();
        if (currentSampler instanceof AbstractDebugElement) {
            Object wrapped = ((AbstractDebugElement) currentSampler).getWrappedElement();
            if (wrapped instanceof Sampler) {
                return (Sampler) wrapped;
            }
        }
        return currentSampler;
    }

    public JMeterContext getThreadContext() {
        return target.getThreadContext();
    }


    public void setStepper(StepTrigger stepper) {
        this.stepper = stepper;
    }

    public StepTrigger getStepper() {
        return stepper;
    }
}
