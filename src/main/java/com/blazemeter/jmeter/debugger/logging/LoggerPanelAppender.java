package com.blazemeter.jmeter.debugger.logging;

import com.blazemeter.jmeter.debugger.gui.LoggerPanelWrapping;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.StringLayout;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;


@Plugin(name = "Logger", category = "Core", elementType = "appender", printObject = true)
public class LoggerPanelAppender extends AbstractAppender {
    private static final Logger log = LoggerFactory.getLogger(LoggerPanelAppender.class);
    public static final String DEFAULT_PATTERN = "%d %p %c{1.}: %m%n";

    private final LoggerPanelWrapping panelWrapping;
    private Method processLogEventMethod;
    private Constructor logEventObjectConstructor;

    public LoggerPanelAppender(String name, LoggerPanelWrapping panelWrapping) {
        super(name, null, PatternLayout.newBuilder().withPattern(DEFAULT_PATTERN).build());
        start();
        Configuration configuration = ((LoggerContext) LogManager.getContext(false)).getConfiguration();
        configuration.getRootLogger().addAppender(this, Level.INFO, null);
        this.panelWrapping = panelWrapping;
        initializeProcessLogEventMethod();
        initializeLogEventObjectConstructor();
    }

    private void initializeProcessLogEventMethod() {
        try {
            Method[] methods = panelWrapping.getClass().getSuperclass().getMethods();
            for (Method method : methods) {
                if ("processLogEvent".equals(method.getName())) {
                    processLogEventMethod = method;
                    break;
                }
            }
        } catch (Throwable ex) {
            log.error("Cannot find 'processLogEventMethod' method for initialize logging panel", ex);
        }
    }

    private void initializeLogEventObjectConstructor() {
        try {
            Class cls = Class.forName("org.apache.jmeter.gui.logging.LogEventObject");
            logEventObjectConstructor = cls.getConstructor(Object.class, String.class);
        } catch (Throwable ex) {
            log.error("Cannot find constructor for class 'LogEventObject'", ex);
        }
    }

    @Override
    public void append(LogEvent logEvent) {
        if (processLogEventMethod != null && logEventObjectConstructor != null) {
            final String serializedString = getStringLayout().toSerializable(logEvent);
            if (serializedString != null && !serializedString.isEmpty()) {
                postLogEventObject(logEvent, serializedString);
            }
        }
    }

    private void postLogEventObject(LogEvent logEvent, String serializedString) {
        Object logEventObject = createLogEventObject(logEvent, serializedString);
        if (logEventObject != null) {
            try {
                processLogEventMethod.invoke(panelWrapping, logEventObject);
            } catch (Throwable ex) {
                log.error("Cannot post logEventObject", ex);
            }
        }
    }

    private Object createLogEventObject(LogEvent logEvent, String serializedString) {
        try {
            return logEventObjectConstructor.newInstance(logEvent, serializedString);
        } catch (Throwable ex) {
            log.error("Cannot create instance of class 'LogEventObject'", ex);
            return null;
        }
    }

    public StringLayout getStringLayout() {
        return (StringLayout) getLayout();
    }
}
