package com.blazemeter.jmeter.debugger.gui;

import org.apache.jmeter.gui.tree.JMeterCellRenderer;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.StringProperty;

import javax.swing.*;
import java.awt.*;


public class FixedJMeterTreeCellRenderer extends JMeterCellRenderer {
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean p_hasFocus) {
        JMeterTreeNode node = (JMeterTreeNode) value;
        TestElement mc = node.getTestElement();
        mc.setProperty(new StringProperty(TestElement.GUI_CLASS, this.getClass().getName()));
        mc.setProperty(new StringProperty(TestElement.TEST_CLASS, mc.getClass().getName()));
        return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, p_hasFocus);
    }
}
