package com.blazemeter.jmeter.debugger.elements;


import org.apache.jmeter.control.Controller;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.testelement.TestIterationListener;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.TestCompilerHelper;

public interface FullController extends
        Controller,
        TestCompilerHelper,
        LoopIterationListener,
        TestIterationListener,
        TestStateListener {
}
