package com.blazemeter.jmeter.debugger.elements;


import com.blazemeter.jmeter.debugger.engine.DebuggerEngine;
import com.blazemeter.jmeter.debugger.engine.StepTrigger;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testbeans.TestBeanHelper;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestIterationListener;
import org.apache.jmeter.threads.JMeterContextService;

public abstract class AbstractDebugElement<T> extends AbstractTestElement implements Wrapper<T>, OriginalLink<T>, LoopIterationListener, TestIterationListener {
    protected T wrapped;
    private T original;

    @Override
    public void setWrappedElement(T wrapped) {
        this.wrapped = wrapped;
    }

    public T getWrappedElement() {
        return wrapped;
    }

    protected StepTrigger getHook() {
        StandardJMeterEngine engine = JMeterContextService.getContext().getEngine();
        if (engine instanceof DebuggerEngine) {
            return ((DebuggerEngine) engine).getStepper();
        }
        throw new IllegalStateException();
    }

    @Override
    public void addTestElement(TestElement el) {
        if (wrapped instanceof TestElement) {
            ((TestElement) wrapped).addTestElement(el);
        }
    }

    protected void prepareBean() {
        if (wrapped instanceof TestBean) {
            //noinspection deprecation
            TestBeanHelper.prepare((TestElement) wrapped); // the deprecation reason is not sufficient
        }
    }

    @Override
    public void setOriginal(T orig) {
        original = orig;
    }

    @Override
    public T getOriginal() {
        return original;
    }

    @Override
    public void iterationStart(LoopIterationEvent iterEvent) {
        if (wrapped instanceof LoopIterationListener) {
            ((LoopIterationListener) wrapped).iterationStart(iterEvent);
        }
    }

    @Override
    public void testIterationStart(LoopIterationEvent event) {
        if (wrapped instanceof TestIterationListener) {
            ((TestIterationListener) wrapped).testIterationStart(event);
        }
    }
}
