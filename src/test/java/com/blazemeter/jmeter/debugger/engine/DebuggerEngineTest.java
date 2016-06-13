package com.blazemeter.jmeter.debugger.engine;

import com.blazemeter.jmeter.debugger.FrontendMock;
import com.blazemeter.jmeter.debugger.TestProvider;
import com.blazemeter.jmeter.debugger.elements.DebuggingThreadGroup;
import com.blazemeter.jmeter.debugger.StepTriggerCounter;
import kg.apc.emulators.TestJMeterUtils;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jorphan.collections.HashTree;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;


public class DebuggerEngineTest {

    @Test
    public void runRealEngine() throws Exception {
        TestJMeterUtils.createJmeterEnv();

        TestTreeProvider prov = new TestProvider();

        HashTree hashTree = prov.getTestTree();
        JMeter.convertSubTree(hashTree);

        StandardJMeterEngine engine = new StandardJMeterEngine();
        engine.configure(hashTree);
        engine.runTest();
        while (engine.isActive()) {
            Thread.sleep(1000);
        }
    }


    @Test
    public void runDebugEngine() throws Exception {
        TestProvider prov = new TestProvider();

        Debugger sel = new Debugger(prov, new FrontendMock());
        AbstractThreadGroup tg = prov.getTG(0);
        sel.selectThreadGroup(tg);
        HashTree testTree = sel.getSelectedTree();

        DebuggingThreadGroup tg2 = (DebuggingThreadGroup) getFirstTG(testTree);
        LoopController samplerController = (LoopController) tg2.getSamplerController();
        samplerController.setLoops(1);
        samplerController.setContinueForever(false);

        JMeter.convertSubTree(testTree);

        DebuggerEngine engine = new DebuggerEngine(JMeterContextService.getContext());
        StepTriggerCounter hook = new StepTriggerCounter();
        engine.setStepper(hook);
        engine.configure(testTree);
        engine.runTest();
        while (engine.isActive()) {
            Thread.sleep(1000);
        }
        Assert.assertEquals(92, hook.cnt);
    }

    private AbstractThreadGroup getFirstTG(HashTree tree) {
        SearchClass<AbstractThreadGroup> searcher = new SearchClass<>(AbstractThreadGroup.class);
        tree.traverse(searcher);
        Collection<AbstractThreadGroup> searchResults = searcher.getSearchResults();
        return searchResults.toArray(new AbstractThreadGroup[0])[0];
    }

}