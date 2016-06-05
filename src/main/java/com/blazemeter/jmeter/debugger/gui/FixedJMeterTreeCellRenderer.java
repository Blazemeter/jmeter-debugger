package com.blazemeter.jmeter.debugger.gui;

import com.blazemeter.jmeter.debugger.elements.Wrapper;
import org.apache.jmeter.gui.tree.JMeterCellRenderer;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.NullProperty;
import org.apache.jmeter.testelement.property.StringProperty;

import javax.swing.*;
import java.awt.*;


public class FixedJMeterTreeCellRenderer extends JMeterCellRenderer {
    private final NodeHighlighter hiliter;

    public FixedJMeterTreeCellRenderer(NodeHighlighter hiliter) {
        super();
        this.hiliter = hiliter;
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean p_hasFocus) {
        JMeterTreeNode node = (JMeterTreeNode) value;
        TestElement mc = node.getTestElement();
        JMeterProperty property = mc.getProperty(TestElement.GUI_CLASS);
        /*
        if (property == null || property instanceof NullProperty) {
            mc.setProperty(new StringProperty(TestElement.GUI_CLASS, this.getClass().getName()));
        } */

        JMeterTreeNode fakeNode = (JMeterTreeNode) node.clone();
        if (mc instanceof Wrapper) {
            fakeNode.setUserObject(((Wrapper) mc).getWrappedElement());
        } else {
            fakeNode.setUserObject(mc);
        }
        Component treeCellRendererComponent = super.getTreeCellRendererComponent(tree, fakeNode, sel, expanded, leaf, row, p_hasFocus);
        hiliter.highlightNode(treeCellRendererComponent, node, mc);
        return treeCellRendererComponent;
    }
}
