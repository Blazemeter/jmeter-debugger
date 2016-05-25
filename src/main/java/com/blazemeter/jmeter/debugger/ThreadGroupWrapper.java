package com.blazemeter.jmeter.debugger;

import com.blazemeter.jmeter.debugger.elements.AbstractDebugElement;
import org.apache.jmeter.threads.AbstractThreadGroup;

public class ThreadGroupWrapper extends AbstractDebugElement<AbstractThreadGroup> {
    public ThreadGroupWrapper(AbstractThreadGroup te) {
        super(te);
    }
}
