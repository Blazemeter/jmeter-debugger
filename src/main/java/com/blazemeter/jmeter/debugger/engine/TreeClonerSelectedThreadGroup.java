package com.blazemeter.jmeter.debugger.engine;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.engine.TreeCloner;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class TreeClonerSelectedThreadGroup extends TreeCloner {
    private static final Logger logger = LoggingManager.getLoggerForClass();
    private final ThreadGroup includeTg;

    public TreeClonerSelectedThreadGroup(ThreadGroup tg) {
        includeTg = tg;
    }

    @Override
    protected Object addNodeToTree(Object node) {
        if (node instanceof ThreadGroup && !node.equals(includeTg)) {
            return node;
        } else {
            return super.addNodeToTree(node);
        }
    }
}
