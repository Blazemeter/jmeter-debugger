package com.blazemeter.jmeter.debugger.engine;

import com.blazemeter.jmeter.debugger.FrontendMock;
import com.blazemeter.jmeter.debugger.StepTriggerCounter;
import com.blazemeter.jmeter.debugger.TestProvider;
import com.blazemeter.jmeter.debugger.elements.DebuggingThreadGroup;
import kg.apc.emulators.TestJMeterUtils;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jorphan.collections.HashTree;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


public class DebuggerEngineTest {
    @BeforeClass
    public static void setup() {
        TestJMeterUtils.createJmeterEnv();
    }

    @Test
    public void runRealEngine() throws Exception {
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
        assertEquals(88, hook.cnt);
    }


    @Test
    public void runVariablesDebugEngine() throws Exception {
        TestProvider prov = new TestProvider("/com/blazemeter/jmeter/debugger/vars.jmx", "vars.jmx");

        Debugger sel = new Debugger(prov, new FrontendMock());
        AbstractThreadGroup tg = prov.getTG(0);
        sel.selectThreadGroup(tg);
        HashTree testTree = sel.getSelectedTree();

        TestSampleListener listener = new TestSampleListener();
        testTree.add(testTree.getArray()[0], listener);

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
        assertEquals(8, hook.cnt);

        assertEquals(3, listener.events.size());
        for (SampleEvent event : listener.events) {
            SampleResult res = event.getResult();
            String label = res.getSampleLabel();
            assertTrue("Label: " + label + " must end with '123'", label.endsWith("123"));
            assertFalse("Variable ${VAR} must be changed to '123' value. label: " + label, label.contains("${VAR}"));
            assertTrue("label: '" + label + "' response: '" + res.getResponseMessage() +"'", res.isSuccessful());
        }
    }

    public class TestSampleListener extends ResultCollector implements SampleListener {
        public List<SampleEvent> events = new ArrayList<>();

        @Override
        public void sampleOccurred(SampleEvent e) {
            events.add(e);
        }

        @Override
        public void sampleStarted(SampleEvent e) {
            events.add(e);
        }

        @Override
        public void sampleStopped(SampleEvent e) {
            events.add(e);
        }
    }

    private AbstractThreadGroup getFirstTG(HashTree tree) {
        SearchClass<AbstractThreadGroup> searcher = new SearchClass<>(AbstractThreadGroup.class);
        tree.traverse(searcher);
        Collection<AbstractThreadGroup> searchResults = searcher.getSearchResults();
        return searchResults.toArray(new AbstractThreadGroup[0])[0];
    }

    @Test
    public void runVariablesInAssertions() throws Exception {
        TestProvider prov = new TestProvider("/com/blazemeter/jmeter/debugger/debug.jmx", "debug.jmx");

        Debugger sel = new Debugger(prov, new FrontendMock());
        AbstractThreadGroup tg = prov.getTG(0);
        sel.selectThreadGroup(tg);
        HashTree testTree = sel.getSelectedTree();

        TestSampleListener listener = new TestSampleListener();
        testTree.add(testTree.getArray()[0], listener);

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
        assertEquals(4, hook.cnt);

        assertEquals(1, listener.events.size());
        SampleEvent event = listener.events.get(0);
        SampleResult result = event.getResult();
        AssertionResult[] assertionResults = result.getAssertionResults();
        assertEquals(1, assertionResults.length);

        AssertionResult assertionRes = assertionResults[0];
        assertNull(assertionRes.getFailureMessage());
    }

    @Test
    public void runVariablesInControllers() throws Exception {
        TestProvider prov = new TestProvider("/com/blazemeter/jmeter/debugger/loops.jmx", "loops.jmx");

        Debugger sel = new Debugger(prov, new FrontendMock());
        AbstractThreadGroup tg = prov.getTG(0);
        sel.selectThreadGroup(tg);
        HashTree testTree = sel.getSelectedTree();

        TestSampleListener listener = new TestSampleListener();
        testTree.add(testTree.getArray()[0], listener);

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
        assertEquals(12, hook.cnt);

        assertEquals(3, listener.events.size());
    }
}