package com.blazemeter.jmeter.debugger.elements;

import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.control.ReplaceableController;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jorphan.collections.HashTree;

@Deprecated
public class ReplaceableGenericControllerDebug extends GenericControllerDebug implements ReplaceableController {
    public ReplaceableGenericControllerDebug(GenericController te) {
        super(te);
    }

    @Override
    public HashTree getReplacementSubTree() {
        if (wrapped instanceof ReplaceableController) {
            return ((ReplaceableController) wrapped).getReplacementSubTree();
        }
        return null;
    }

    @Override
    public void resolveReplacementSubTree(JMeterTreeNode context) {
        if (wrapped instanceof ReplaceableController) {
            ((ReplaceableController) wrapped).resolveReplacementSubTree(context);
        }
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public Object clone() {
        if (wrapped.getClass().getName().equals("org.apache.jmeter.control.ModuleController")) {
            return this;
        }
        return new GenericControllerDebug(wrapped);
    }
}
