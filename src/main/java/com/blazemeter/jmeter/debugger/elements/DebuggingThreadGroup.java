package com.blazemeter.jmeter.debugger.elements;

import com.blazemeter.jmeter.debugger.engine.DebuggerEngine;
import com.blazemeter.jmeter.debugger.engine.DebuggingThread;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.engine.TreeCloner;
import org.apache.jmeter.threads.*;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.ListedHashTree;

public class DebuggingThreadGroup extends ThreadGroup {
    public DebuggingThreadGroup() {
        super();
        setDelay(0);
        setNumThreads(1);
        setRampUp(0);
        LoopController ctl = new LoopController();
        ctl.setContinueForever(true);
        ctl.setLoops(-1);
        setSamplerController(ctl);
    }

    @Override
    public void start(int groupCount, ListenerNotifier notifier, ListedHashTree threadGroupTree, StandardJMeterEngine engine) {
        final JMeterContext context = JMeterContextService.getContext();
        JMeterThread jmThread = makeThread(groupCount, notifier, threadGroupTree, engine, 0, context);
        Thread newThread = new Thread(jmThread, jmThread.getThreadName());
        newThread.start();
    }

    private JMeterThread makeThread(int groupCount, ListenerNotifier notifier, ListedHashTree threadGroupTree, StandardJMeterEngine engine, int i, JMeterContext context) {
        // had to copy whole method because of this line
        DebuggingThread jmeterThread = new DebuggingThread(cloneTree(threadGroupTree), this, notifier);
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

    private ListedHashTree cloneTree(ListedHashTree tree) {
        TreeCloner cloner = new TreeCloner(true);
        tree.traverse(cloner);
        return cloner.getClonedTree();
    }
}
