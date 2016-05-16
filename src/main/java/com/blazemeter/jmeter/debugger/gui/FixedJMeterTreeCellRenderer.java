package com.blazemeter.jmeter.debugger.gui;

import org.apache.jmeter.gui.tree.JMeterCellRenderer;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.NullProperty;
import org.apache.jmeter.testelement.property.StringProperty;

import javax.swing.*;
import java.awt.*;


public class FixedJMeterTreeCellRenderer extends JMeterCellRenderer {
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean p_hasFocus) {
        JMeterTreeNode node = (JMeterTreeNode) value;
        TestElement mc = node.getTestElement();
        JMeterProperty property = mc.getProperty(TestElement.GUI_CLASS);
        if (property == null || property instanceof NullProperty) {
            mc.setProperty(new StringProperty(TestElement.GUI_CLASS, this.getClass().getName()));
        }
        Component treeCellRendererComponent = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, p_hasFocus);
        if (node.isMarkedBySearch()) {
            setBorder(null);
            setFont(getFont().deriveFont(Font.BOLD | Font.ITALIC));
        } else {
            setFont(getFont().deriveFont(~Font.BOLD).deriveFont(~Font.ITALIC));
        }
        return treeCellRendererComponent;
    }
}
