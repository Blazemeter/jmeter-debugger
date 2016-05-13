package com.blazemeter.jmeter.debugger.elements;


import com.blazemeter.jmeter.debugger.engine.StepTrigger;
import org.apache.jmeter.testelement.AbstractTestElement;

public class AbstractDebugElement<T> extends AbstractTestElement {
    protected final T parent;
    protected final StepTrigger hook;

    public AbstractDebugElement(T te, StepTrigger hook) {
        parent = te;
        this.hook = hook;
    }

    public T getWrappedElement() {
        return parent;
    }
}
