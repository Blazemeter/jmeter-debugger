package com.blazemeter.jmeter.debugger.engine;

import com.blazemeter.jmeter.debugger.elements.Wrapper;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DebuggerEngine extends StandardJMeterEngine {
    private static final Logger log = LoggerFactory.getLogger(DebuggerEngine.class);

    private final JMeterContext context;

    private Thread thread;
    private DebuggingThread target;
    private StepTrigger stepper = new StepTrigger() {
        @Override
        public void stepOn(Wrapper t) {
            throw new RuntimeException("Not initialized stepper");
        }
    };

    public DebuggerEngine(JMeterContext context) {
        this.context = context;
    }

    public JMeterContext getThreadContext() {
        return context;
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
    public synchronized void stopTest(boolean now) {
        super.stopTest(now);

        if (thread != null && thread.isAlive()) {
            log.debug("Joining thread: " + thread);
            try {
                thread.join(10000); // last resort wait
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (thread.isAlive()) {
                log.warn("Thread not finished: " + thread);
            } else {
                log.debug("Thread finished: " + thread);
            }
        }
    }

    @Override
    public void run() {
        JMeterContextService.replaceContext(context);
        super.run();
    }
}
