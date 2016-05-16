package com.blazemeter.jmeter.debugger.engine;

import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.*;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.lang.reflect.Field;

public class DebuggingThread extends JMeterThread {
    private static final Logger log = LoggingManager.getLoggerForClass();
    private DebuggerCompiler compiler;
    private final HashTree test;

    private JMeterContext threadContext;


    public DebuggingThread(HashTree test, JMeterThreadMonitor monitor, ListenerNotifier note) {
        super(test, monitor, note);
        this.test = test;
    }

    public void setHook(StepTrigger hook) {
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

    public JMeterContext getThreadContext() {
        return threadContext;
    }

    @Override
    public void run() {
        if (compiler == null) {
            throw new IllegalStateException("Compiler was not overridden");
        }
        threadContext = JMeterContextService.getContext();
        super.run();
    }
}
