package com.blazemeter.jmeter.debugger.gui;


import org.apache.jmeter.gui.LoggerPanel;
import org.apache.jmeter.gui.util.JSyntaxTextArea;

public class LoggerPanelWrapping extends LoggerPanel {
    private JSyntaxTextArea area;

    public LoggerPanelWrapping() {
        super();

        ComponentFinder<JSyntaxTextArea> finder = new ComponentFinder<>(JSyntaxTextArea.class);
        area = finder.findComponentIn(this);
        area.setLineWrap(true);
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
