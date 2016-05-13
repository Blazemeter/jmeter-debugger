package com.blazemeter.jmeter.debugger.gui;

import org.apache.jmeter.gui.plugin.MenuCreator;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import javax.swing.*;

public class DebuggerMenuCreator implements MenuCreator {
    private static final Logger log = LoggingManager.getLoggerForClass();

    @Override
    public JMenuItem[] getMenuItemsAtLocation(MENU_LOCATION location) {
        if (location == MENU_LOCATION.RUN) {
            try {
                return new JMenuItem[]{new DebuggerMenuItem()};
            } catch (Throwable e) {
                log.error("Failed to load debugger", e);
                return new JMenuItem[0];
            }

        } else {
            return new JMenuItem[0];
        }
    }

    @Override
    public JMenu[] getTopLevelMenus() {
        return new JMenu[0];
    }

    @Override
    public boolean localeChanged(MenuElement menu) {
        return false;
    }

    @Override
    public void localeChanged() {
    }
}
