package com.blazemeter.jmeter.debugger.elements;

import com.blazemeter.jmeter.debugger.engine.StepTrigger;
import org.apache.jmeter.timers.Timer;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class TimerDebug extends AbstractDebugElement<Timer> implements Timer {
    private static final Logger log = LoggingManager.getLoggerForClass();

    public TimerDebug(Timer te, StepTrigger hook) {
        super(te, hook);
    }

    @Override
    public long delay() {
        hook.notify(this);
        long delay = parent.delay();
        log.debug("Drop delay because of debug: " + delay);
        return 0;
    }
}
