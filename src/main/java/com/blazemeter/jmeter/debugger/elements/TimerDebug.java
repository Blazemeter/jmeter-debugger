package com.blazemeter.jmeter.debugger.elements;

import org.apache.jmeter.timers.Timer;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class TimerDebug extends AbstractDebugElement<Timer> implements Timer {
    private static final Logger log = LoggingManager.getLoggerForClass();

    public TimerDebug(Timer te) {
        super(te);
    }

    @Override
    public long delay() {
        prepareBean();
        getHook().notify(this);
        long delay = wrapped.delay();
        log.debug("Drop delay because of debug: " + delay);
        return 0;
    }
}
