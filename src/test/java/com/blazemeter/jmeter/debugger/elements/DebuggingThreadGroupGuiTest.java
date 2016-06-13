package com.blazemeter.jmeter.debugger.elements;

import kg.apc.emulators.TestJMeterUtils;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;

public class DebuggingThreadGroupGuiTest {
    @Test
    public void test() throws InterruptedException {
        TestJMeterUtils.createJmeterEnv();
        DebuggingThreadGroupGui obj = new DebuggingThreadGroupGui();
        
        if (!GraphicsEnvironment.getLocalGraphicsEnvironment().isHeadlessInstance()) {
            JFrame frame = new JFrame();
            frame.setSize(800, 600);
            frame.add(obj);
            frame.setVisible(true);
            Thread.sleep(10000);
        }
    }
}