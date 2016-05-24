package com.blazemeter.jmeter.debugger.elements;


import org.apache.jmeter.testelement.TestElement;

public interface Wrapper<T> extends TestElement {
    T getWrappedElement();
}
