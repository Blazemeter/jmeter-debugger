package com.blazemeter.jmeter.debugger.elements;

import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.gui.AbstractThreadGroupGui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

public class DebuggingThreadGroupGui extends AbstractThreadGroupGui {
    public DebuggingThreadGroupGui() {
        super();

        removeAll();
        
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        Box box = Box.createVerticalBox();
        box.add(makeTitlePanel());
        add(box, BorderLayout.NORTH);
    }

    @Override
    public String getLabelResource() {
        return getClass().getCanonicalName();
    }

    @Override
    public String getStaticLabel() {
        return "Debugging Thread Group";
    }

    @Override
    public TestElement createTestElement() {
        return new DebuggingThreadGroup();
    }

    @Override
    public void modifyTestElement(TestElement element) {
    }

    @Override
    public JPopupMenu createPopupMenu() {
        return MenuFactory.getDefaultMenu();
    }

    @Override
    public Collection<String> getMenuCategories() {
        return new ArrayList<>();
    }
}
