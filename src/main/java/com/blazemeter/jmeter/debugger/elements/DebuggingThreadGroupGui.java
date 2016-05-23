package com.blazemeter.jmeter.debugger.elements;

import org.apache.jmeter.gui.AbstractJMeterGuiComponent;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.testelement.TestElement;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;

public class DebuggingThreadGroupGui extends AbstractJMeterGuiComponent {
    @Override
    public String getLabelResource() {
        return getClass().getCanonicalName();
    }

    @Override
    public String getStaticLabel() {
        return getClass().getCanonicalName();
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
