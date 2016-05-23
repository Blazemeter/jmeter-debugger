package com.blazemeter.jmeter.debugger.gui;

import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jorphan.collections.Data;

public class HighlightTableModel extends PowerTableModel {

    private Data oldData = null;

    public HighlightTableModel(String[] strings, Class[] classes) {
        super(strings, classes);
    }

    public boolean isRowHighlighted(int rowN) {
        if (oldData == null) {
            return false;
        }

        String nameCol = getColumnName(0);
        Object curName = getValueAt(rowN, 0);
        int rowOfValue = oldData.findValue(nameCol, curName);
        Object curValue = getValueAt(rowN, 1);
        Object oldValue = oldData.getColumnValue(1, rowOfValue);
        boolean changed = !curValue.equals(oldValue);
        return rowOfValue < 0 || changed;
    }

    @Override
    public void clearData() {
        if (oldData == null && getData().size() == 0) {
            super.clearData();
        } else {
            oldData = getData();
            super.clearData();
        }
    }
}
