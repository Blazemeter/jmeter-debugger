package com.blazemeter.jmeter.debugger.gui;

import com.blazemeter.jmeter.debugger.elements.OriginalLink;
import org.apache.jmeter.gui.tree.JMeterCellRenderer;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.TestElement;

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

        JMeterTreeNode fakeNode = (JMeterTreeNode) node.clone();
        if (mc instanceof OriginalLink) {
            fakeNode.setUserObject(((OriginalLink) mc).getOriginal());
        } else {
            fakeNode.setUserObject(mc);
        }
        Component treeCellRendererComponent = super.getTreeCellRendererComponent(tree, fakeNode, sel, expanded, leaf, row, p_hasFocus);
        hiliter.highlightNode(treeCellRendererComponent, node, mc);
        return treeCellRendererComponent;
    }
}
