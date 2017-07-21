package com.blazemeter.jmeter.debugger.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.LinkedList;

public class HighlightTable extends JTable {
    private static final Logger log = LoggerFactory.getLogger(HighlightTable.class);


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

    //@Override TODO: restore it
    public Component prepareRenderer1(TableCellRenderer renderer, int row, int column) {
        Component comp = super.prepareRenderer(renderer, row, column);
        if (getModel() instanceof HighlightTableModel) {
            HighlightTableModel model = (HighlightTableModel) getModel();
            try {
                Object valueAt = getValueAt(row, 0);
                if (valueAt != null && model.isRowHighlighted(valueAt.toString(), getValueAt(row, 1))) {
                    comp.setFont(comp.getFont().deriveFont(Font.BOLD));
                } else {
                    comp.setFont(comp.getFont().deriveFont(~Font.BOLD));
                }
            } catch (IndexOutOfBoundsException e) {
                log.debug("Problems rendering ", e);
            }
        }
        return comp;
    }
}
