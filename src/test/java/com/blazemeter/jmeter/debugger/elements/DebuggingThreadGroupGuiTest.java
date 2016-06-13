package com.blazemeter.jmeter.debugger.elements;

import kg.apc.emulators.TestJMeterUtils;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;

public class DebuggingThreadGroupGuiTest {
    @Test
    public void test() throws InterruptedException {
        if (!GraphicsEnvironment.getLocalGraphicsEnvironment().isHeadlessInstance()) {
            TestJMeterUtils.createJmeterEnv();
            
            DebuggingThreadGroupGui obj = new DebuggingThreadGroupGui();
            JFrame frame = new JFrame();
            frame.setSize(800, 600);
            frame.add(obj);
            frame.setVisible(true);
            Thread.sleep(10000);
        }
    }
}