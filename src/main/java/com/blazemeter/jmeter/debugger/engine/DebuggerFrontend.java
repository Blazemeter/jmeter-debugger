package com.blazemeter.jmeter.debugger.engine;

import com.blazemeter.jmeter.debugger.elements.Wrapper;
import org.apache.jmeter.threads.JMeterContext;

public interface DebuggerFrontend {
    void started();

    void stopped();

    void continuing();

    void frozenAt(Wrapper wrapper);

    void statusRefresh(JMeterContext context);
}
