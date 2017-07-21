package com.blazemeter.jmeter.debugger.elements;

import org.apache.jmeter.timers.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimerDebug extends AbstractDebugElement<Timer> implements Timer {
    private static final Logger log = LoggerFactory.getLogger(TimerDebug.class);



    private boolean isDelaying = false;

    @Override
    public long delay() {
        prepareBean();
        getHook().stepOn(this);
        long delay = wrapped.delay();
        if (isDelaying) {
            return delay;
        } else {
            log.debug("Drop delay because of debug: " + delay);
            return 0;
        }
    }

    public void setDelaying(boolean delaying) {
        isDelaying = delaying;
    }
}
