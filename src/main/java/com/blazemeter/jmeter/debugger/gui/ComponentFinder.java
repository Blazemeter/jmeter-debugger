package com.blazemeter.jmeter.debugger.gui;


import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.awt.*;

public class ComponentFinder<T extends Component> {
    private static final Logger log = LoggingManager.getLoggerForClass();
    private final Class<T> search;

    public ComponentFinder(Class<T> cls) {
        search = cls;
    }

    public T findComponentIn(Container container) {
        log.debug("Searching in " + container);
        for (Component a : container.getComponents()) {
            if (search.isAssignableFrom(a.getClass())) {
                log.debug("Found " + a);
                return (T) a;
            }

            if (a instanceof Container) {
                T res = findComponentIn((Container) a);
                if (res != null) {
                    return res;
                }
            }
        }

        return null;
    }
}
