package com.blazemeter.jmeter.debugger.elements;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.gui.AbstractControllerGui;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.testelement.TestElement;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;

public class ControllerDebugGui extends AbstractControllerGui implements OriginalLink<Controller> {
    private Controller original;

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

    @Override
    public Controller getOriginal() {
        return original;
    }

    @Override
    public void setOriginal(Controller orig) {
        original = orig;
    }
}
