package com.blazemeter.jmeter.debugger.gui;


import org.apache.jmeter.gui.LoggerPanel;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;

import javax.swing.*;
import java.awt.*;

public class LoggerPanelWrapping extends LoggerPanel {
    private JSyntaxTextArea area;

    public LoggerPanelWrapping() {
        super();
        Component comp = this.getComponent(0);
        if (comp instanceof JTextScrollPane) {
            JTextScrollPane scroll = (JTextScrollPane) comp;
            comp = scroll.getComponent(0);
            if (comp instanceof JViewport) {
                comp = ((JViewport) comp).getComponent(0);
                if (comp instanceof JSyntaxTextArea) {
                    area = (JSyntaxTextArea) comp;
                    area.setLineWrap(true);
                }
            }
        }
    }

    public void setText(String txt) {
        if (area != null) {
            area.setText(txt);
        }
    }

    public void scrollToTop() {
        area.setCaretPosition(0);
    }
}
