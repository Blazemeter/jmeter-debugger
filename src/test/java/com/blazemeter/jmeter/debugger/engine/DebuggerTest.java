package com.blazemeter.jmeter.debugger.engine;

import com.blazemeter.jmeter.debugger.FrontendMock;
import com.blazemeter.jmeter.debugger.TestProvider;
import kg.apc.emulators.TestJMeterUtils;
import org.apache.jmeter.control.Controller;
import org.junit.Assert;
import org.junit.Test;

public class DebuggerTest {
    @Test
    public void testMain() throws Exception {
        TestJMeterUtils.createJmeterEnv();
        TestProvider treeProvider = new TestProvider();
        Debugger dbg = new Debugger(treeProvider, new FrontendMock());
        dbg.selectThreadGroup(treeProvider.getTG(0));

        Debugger.toggleBreakpoint(treeProvider.getTG(0));

        dbg.start();
        Thread.sleep(5000);
        Assert.assertFalse(dbg.isContinuing());
        Assert.assertTrue(dbg.getCurrentElement() instanceof Controller);
        dbg.proceed();
        Thread.sleep(1000);
        dbg.proceed();
        Assert.assertTrue(dbg.getCurrentSampler() != null);
        Thread.sleep(1000);
        dbg.continueRun();
        Thread.sleep(2000);
        dbg.pause();
        Thread.sleep(1000);
        dbg.stop();
    }
}