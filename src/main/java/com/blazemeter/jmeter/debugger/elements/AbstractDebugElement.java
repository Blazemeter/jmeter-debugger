package com.blazemeter.jmeter.debugger.elements;


import com.blazemeter.jmeter.debugger.engine.DebuggerEngine;
import com.blazemeter.jmeter.debugger.engine.StepTrigger;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testbeans.TestBeanHelper;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.JMeterContextService;

public class AbstractDebugElement<T> extends AbstractTestElement {
    protected final T wrapped;

    public AbstractDebugElement(T te) {
        wrapped = te;
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
}
