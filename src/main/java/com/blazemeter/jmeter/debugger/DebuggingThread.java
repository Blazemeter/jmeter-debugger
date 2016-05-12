package com.blazemeter.jmeter.debugger;

import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.JMeterThreadMonitor;
import org.apache.jmeter.threads.ListenerNotifier;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.lang.reflect.Field;

public class DebuggingThread extends JMeterThread {
    private static final Logger log = LoggingManager.getLoggerForClass();

    public DebuggingThread(HashTree test, JMeterThreadMonitor monitor, ListenerNotifier note, StepTrigger hook) {
        super(test, monitor, note);
        setThreadName("Debugging Thread 1-1");
        try {
            replaceCompiler(test, hook);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to replace test compiler", e);
        }
    }

    private void replaceCompiler(HashTree test, StepTrigger hook) throws NoSuchFieldException, IllegalAccessException {
        Field field = JMeterThread.class.getDeclaredField("compiler");
        if (!field.isAccessible()) {
            log.debug("Making field accessable: " + field);
            field.setAccessible(true);
        }
        DebuggerCompiler compiler = new DebuggerCompiler(test, hook);
        field.set(this, compiler);
    }
}
