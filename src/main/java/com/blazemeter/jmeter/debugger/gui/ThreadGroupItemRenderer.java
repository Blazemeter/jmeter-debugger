package com.blazemeter.jmeter.debugger.gui;

import org.apache.jmeter.threads.ThreadGroup;

import javax.swing.*;
import java.awt.*;

public final class ThreadGroupItemRenderer implements ListCellRenderer<ThreadGroup> {
    private final ListCellRenderer originalRenderer;

    public ThreadGroupItemRenderer(final ListCellRenderer originalRenderer) {
        this.originalRenderer = originalRenderer;
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends ThreadGroup> list, ThreadGroup value, int index, boolean isSelected, boolean cellHasFocus) {
        //noinspection unchecked
        String name = "";
        if (value != null) {
            name = value.getName();
        }
        return originalRenderer.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
    }
}
