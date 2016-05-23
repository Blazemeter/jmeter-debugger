package com.blazemeter.jmeter.debugger.gui;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;

public class HighlightTable extends JTable {
    public HighlightTable(TableModel model) {
        super(model);
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component comp = super.prepareRenderer(renderer, row, column);
        if (getModel() instanceof HighlightTableModel) {
            if (((HighlightTableModel) getModel()).isRowHighlighted(row)) {
                comp.setFont(comp.getFont().deriveFont(Font.BOLD));
            } else {
                comp.setFont(comp.getFont().deriveFont(~Font.BOLD));
            }
        }
        return comp;
    }
}
