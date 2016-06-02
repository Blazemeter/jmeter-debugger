package com.blazemeter.jmeter.debugger.gui;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.LinkedList;

public class HighlightTable extends JTable {
    public HighlightTable(TableModel model) {
        super(model);
        setDefaultEditor(Object.class, null);

        TableRowSorter<TableModel> sorter = new SyncTableRowSorter(model);
        sorter.setSortsOnUpdates(true);
        LinkedList<RowSorter.SortKey> sortKeys = new LinkedList<>();
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);
        //sorter.sort();
        setRowSorter(sorter);
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component comp = super.prepareRenderer(renderer, row, column);
        if (getModel() instanceof HighlightTableModel) {
            HighlightTableModel model = (HighlightTableModel) getModel();
            int mdlRow = getRowSorter().convertRowIndexToModel(row);
            Object valueAt = model.getValueAt(mdlRow, 0);
            if (valueAt != null && model.isRowHighlighted(valueAt.toString(), model.getValueAt(mdlRow, 1))) {
                comp.setFont(comp.getFont().deriveFont(Font.BOLD));
            } else {
                comp.setFont(comp.getFont().deriveFont(~Font.BOLD));
            }
        }
        return comp;
    }

    private class SyncTableRowSorter extends TableRowSorter<TableModel> {
        public SyncTableRowSorter(TableModel model) {
            super(model);
        }

        @Override
        public synchronized int convertRowIndexToModel(int index) {
            return super.convertRowIndexToModel(index);
        }

        @Override
        public synchronized void sort() {
            super.sort();
        }
    }
}
