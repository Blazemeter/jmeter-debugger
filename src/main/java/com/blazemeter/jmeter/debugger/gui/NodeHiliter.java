package com.blazemeter.jmeter.debugger.gui;

import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.TestElement;

import java.awt.*;

public interface NodeHiliter {
    void highlightNode(Component treeCellRendererComponent, JMeterTreeNode node, TestElement mc);
}
