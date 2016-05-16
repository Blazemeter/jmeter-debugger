package com.blazemeter.jmeter.debugger.gui;

import org.apache.jmeter.threads.AbstractThreadGroup;

import javax.swing.*;
import java.awt.*;

public final class ThreadGroupItemRenderer implements ListCellRenderer<AbstractThreadGroup> {
    private final ListCellRenderer originalRenderer;

    public ThreadGroupItemRenderer(final ListCellRenderer originalRenderer) {
        this.originalRenderer = originalRenderer;
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends AbstractThreadGroup> list, AbstractThreadGroup value, int index, boolean isSelected, boolean cellHasFocus) {
        String name = "";
        if (value != null) {
            name = value.getName();
        }
        //noinspection unchecked
        return originalRenderer.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
    }
}
