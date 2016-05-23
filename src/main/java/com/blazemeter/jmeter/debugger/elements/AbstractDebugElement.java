package com.blazemeter.jmeter.debugger.elements;


import com.blazemeter.jmeter.debugger.engine.DebuggerEngine;
import com.blazemeter.jmeter.debugger.engine.StepTrigger;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.testelement.AbstractTestElement;
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
}
