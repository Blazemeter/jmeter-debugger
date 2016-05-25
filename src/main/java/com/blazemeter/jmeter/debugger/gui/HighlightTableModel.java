package com.blazemeter.jmeter.debugger.gui;

import org.apache.jmeter.gui.util.PowerTableModel;

import java.util.HashMap;
import java.util.Map;

public class HighlightTableModel extends PowerTableModel {

    private Map<String, Object> oldData = null;

    public HighlightTableModel(String[] strings, Class[] classes) {
        super(strings, classes);
    }

    public boolean isRowHighlighted(String curName, Object curValue) {
        if (oldData == null) {
            return false;
        }

        Object oldValue = oldData.get(curName);
        return !curValue.equals(oldValue);
    }

    @Override
    public void clearData() {
        if (oldData == null && getData().size() == 0) {
            super.clearData();
        } else {
            oldData = new HashMap<>();
            for (int row = 0; row < getRowCount(); row++) {
                Object[] rowData = getRowData(row);
                oldData.put(rowData[0].toString(), rowData[1]);
            }
            super.clearData();
        }
    }
}
