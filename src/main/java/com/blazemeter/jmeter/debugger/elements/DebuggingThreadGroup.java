package com.blazemeter.jmeter.debugger.elements;

import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.ThreadGroup;

public class DebuggingThreadGroup extends ThreadGroup {
    public DebuggingThreadGroup(AbstractThreadGroup cloned) {
        super();
        setName(cloned.getName());
        setDelay(0);
        setNumThreads(1);
        setRampUp(0);
        LoopController ctl = new LoopController();
        ctl.setContinueForever(true);
        ctl.setLoops(-1);
        setSamplerController(ctl);
    }
}
