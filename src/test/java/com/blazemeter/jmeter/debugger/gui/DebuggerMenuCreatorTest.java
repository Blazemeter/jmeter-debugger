package com.blazemeter.jmeter.debugger.gui;

import org.apache.jmeter.gui.plugin.MenuCreator;
import org.junit.Test;

import javax.swing.*;

import static org.junit.Assert.assertEquals;


public class DebuggerMenuCreatorTest {
    @Test
    public void test() throws Exception {
        DebuggerMenuCreator obj = new DebuggerMenuCreator();
        JMenuItem[] res = obj.getMenuItemsAtLocation(MenuCreator.MENU_LOCATION.RUN);
        assertEquals(1, res.length);
        obj.getTopLevelMenus();
        obj.localeChanged();
        obj.localeChanged(null);
    }
}