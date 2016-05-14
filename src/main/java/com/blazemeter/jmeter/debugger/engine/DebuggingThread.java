package com.blazemeter.jmeter.debugger.engine;

import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.JMeterThreadMonitor;
import org.apache.jmeter.threads.ListenerNotifier;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.lang.reflect.Field;

public class DebuggingThread extends JMeterThread {
    private static final Logger log = LoggingManager.getLoggerForClass();
    private final DebuggerCompiler compiler;

    public DebuggingThread(HashTree test, JMeterThreadMonitor monitor, ListenerNotifier note, StepTrigger hook) {
        super(test, monitor, note);
        setThreadName("Debugging Thread 1-1");
        compiler = new DebuggerCompiler(test, hook);
        try {
            replaceCompiler();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to replace test compiler", e);
        }
    }

    private void replaceCompiler() throws NoSuchFieldException, IllegalAccessException {
        Field field = JMeterThread.class.getDeclaredField("compiler");
        if (!field.isAccessible()) {
            log.debug("Making field accessable: " + field);
            field.setAccessible(true);
        }
        field.set(this, compiler);
    }

    public Sampler getCurrentSampler() {
        DebuggerSamplerPackage lastSamplePackage = compiler.getLastSamplePackage();
        if (lastSamplePackage == null) {
            return null;
        }
        return lastSamplePackage.getSampler();
    }
}
