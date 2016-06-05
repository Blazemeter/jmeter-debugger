package com.blazemeter.jmeter.debugger.elements;

public interface OriginalLink<T> {
    T getOriginal();

    void setOriginal(T orig);
}
