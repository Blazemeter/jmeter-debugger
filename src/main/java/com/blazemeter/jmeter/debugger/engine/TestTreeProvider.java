package com.blazemeter.jmeter.debugger.engine;


import org.apache.jorphan.collections.HashTree;

public interface TestTreeProvider {
    HashTree getTestTree();
}
