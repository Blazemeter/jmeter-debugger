package com.blazemeter.jmeter.debugger.engine;

import org.apache.jmeter.threads.*;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class DebuggingThread extends JMeterThread {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private JMeterContext threadContext;


    public DebuggingThread(HashTree test, JMeterThreadMonitor monitor, ListenerNotifier note, JMeterContext ctx) {
        super(test, monitor, note);
        threadContext = ctx;
    }

    @Override
    public void run() {
        log.debug("Replacing thread context with " + threadContext);
        JMeterContextService.replaceContext(threadContext);
        super.run();
    }
}
