package org.jmeterplugins.debugger;


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
