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
        // setSorter(model); FIXME  produces exceptions on "continue" => TODO: sync with model updates
    }

    private void setSorter(TableModel model) {
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
        sorter.setSortsOnUpdates(true);
        LinkedList<RowSorter.SortKey> sortKeys = new LinkedList<>();
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);
        sorter.sort();
        setRowSorter(sorter);
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component comp = super.prepareRenderer(renderer, row, column);
        if (getModel() instanceof HighlightTableModel) {
            HighlightTableModel model = (HighlightTableModel) getModel();
            if (model.isRowHighlighted(getValueAt(row, 0).toString(), getValueAt(row, 1))) {
                comp.setFont(comp.getFont().deriveFont(Font.BOLD));
            } else {
                comp.setFont(comp.getFont().deriveFont(~Font.BOLD));
            }
        }
        return comp;
    }
}
