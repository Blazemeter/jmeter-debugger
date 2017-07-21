package com.blazemeter.jmeter.debugger.engine;

import org.apache.jmeter.threads.*;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebuggingThread extends JMeterThread {
    private static final Logger log = LoggerFactory.getLogger(DebuggingThread.class);

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
