package com.blazemeter.jmeter.debugger.elements;

import com.blazemeter.jmeter.debugger.engine.DebuggerEngine;
import com.blazemeter.jmeter.debugger.engine.DebuggingThread;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.threads.*;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.ListedHashTree;

public class DebuggingThreadGroup extends ThreadGroup {
    private DebuggerEngine dbgEngine;

    public DebuggingThreadGroup() {
        super();
        setDelay(0);
        setNumThreads(1);
        setRampUp(0);
        LoopController ctl = new LoopController();
        ctl.setContinueForever(false);
        ctl.setLoops(1);
        setSamplerController(ctl);
    }

    @Override
    public void start(int groupCount, ListenerNotifier notifier, ListedHashTree threadGroupTree, StandardJMeterEngine engine) {
        JMeterContext context = JMeterContextService.getContext();
        DebuggingThread jmThread = makeThread(groupCount, notifier, threadGroupTree, engine, 0, context);
        Thread newThread = new Thread(jmThread, jmThread.getThreadName());
        if (engine instanceof DebuggerEngine) {
            dbgEngine = (DebuggerEngine) engine;
            dbgEngine.setTarget(jmThread);
            dbgEngine.setThread(newThread);
            
        }
        newThread.start();
    }

    private DebuggingThread makeThread(int groupCount, ListenerNotifier notifier, ListedHashTree threadGroupTree, StandardJMeterEngine engine, int i, JMeterContext context) {
        // had to copy whole method because of these lines
        DebuggingThread jmeterThread = new DebuggingThread(threadGroupTree, this, notifier);
        if (engine instanceof DebuggerEngine) {
            jmeterThread.setHook(((DebuggerEngine) engine).getStepper());
        }

        boolean onErrorStopTest = getOnErrorStopTest();
        boolean onErrorStopTestNow = getOnErrorStopTestNow();
        boolean onErrorStopThread = getOnErrorStopThread();
        boolean onErrorStartNextLoop = getOnErrorStartNextLoop();
        String groupName = getName();

        jmeterThread.setThreadNum(i);
        jmeterThread.setThreadGroup(this);
        jmeterThread.setInitialContext(context);
        String threadName = groupName + " " + (groupCount) + "-" + (i + 1);
        jmeterThread.setThreadName(threadName);
        jmeterThread.setEngine(engine);
        jmeterThread.setOnErrorStopTest(onErrorStopTest);
        jmeterThread.setOnErrorStopTestNow(onErrorStopTestNow);
        jmeterThread.setOnErrorStopThread(onErrorStopThread);
        jmeterThread.setOnErrorStartNextLoop(onErrorStartNextLoop);
        return jmeterThread;
    }

    @Override
    public void threadFinished(JMeterThread thread) {
        super.threadFinished(thread);
        if (dbgEngine!=null) {
            dbgEngine.threadFinished(thread);
        }
    }
}
