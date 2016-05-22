package com.blazemeter.jmeter.debugger.engine;

import com.blazemeter.jmeter.debugger.elements.AbstractDebugElement;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.JMeterThreadMonitor;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;


public class DebuggerEngine extends StandardJMeterEngine implements JMeterThreadMonitor {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private Thread thread;
    private DebuggingThread target;
    private StepTrigger stepper = new StepTrigger() {
        @Override
        public void notify(AbstractDebugElement t) {
            throw new RuntimeException("Not initialized stepper");
        }
    };
    private JMeterThreadMonitor stopNotifier;

    public Sampler getCurrentSampler() {
        return target.getCurrentSampler();
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

    public void setTarget(DebuggingThread target) {
        if (this.target != null) {
            throw new IllegalStateException();
        }
        this.target = target;
    }

    public void setThread(Thread thread) {
        if (this.thread != null) {
            throw new IllegalStateException();
        }
        this.thread = thread;
    }

    @Override
    public void threadFinished(JMeterThread thread) {
        stopNotifier.threadFinished(thread);
    }

    public void setStopNotifier(JMeterThreadMonitor stopNotifier) {
        this.stopNotifier = stopNotifier;
    }
}
