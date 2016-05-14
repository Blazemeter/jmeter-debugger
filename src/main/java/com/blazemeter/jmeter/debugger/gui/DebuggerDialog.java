package com.blazemeter.jmeter.debugger.gui;

import com.blazemeter.jmeter.debugger.engine.DebuggerEngine;
import com.blazemeter.jmeter.debugger.engine.StepTrigger;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.awt.event.*;

public class DebuggerDialog extends DebuggerDialogBase {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private DebuggerEngine engine;

    public DebuggerDialog() {
        super();
        StepOver stepper = new StepOver();
        start.addActionListener(new StartDebugging(stepper));
        step.addActionListener(stepper);
        tgCombo.addItemListener(new ThreadGroupChoiceChanged());
    }

    @Override
    public void componentShown(ComponentEvent e) {
        log.debug("Showing dialog");
        HashTree testTree = getTestTree();
        this.engine = new DebuggerEngine(testTree);
        tgCombo.removeAllItems();
        for (ThreadGroup group : engine.getThreadGroups()) {
            tgCombo.addItem(group);
        }
    }


    @Override
    public void componentHidden(ComponentEvent e) {
        log.debug("Closing dialog");
        engine.stopDebugging();
    }

    protected HashTree getTestTree() {
        GuiPackage gui = GuiPackage.getInstance();
        return gui.getTreeModel().getTestPlan();
    }

    private class ThreadGroupChoiceChanged implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent event) {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                log.debug("Item choice changed: " + event.getItem());
                if (event.getItem() instanceof ThreadGroup) {
                    HashTree val = engine.getThreadGroupTree((ThreadGroup) event.getItem());
                    tree.setModel(new DebuggerTreeModel(val));
                    start.setEnabled(!val.isEmpty()); // TODO
                }
            }
        }
    }

    private class StartDebugging implements ActionListener {
        private StepOver stepper;

        public StartDebugging(StepOver stepper) {
            this.stepper = stepper;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            log.debug("Start debugging");
            loggerPanel.clear();
            tgCombo.setEnabled(false);
            start.setEnabled(false);
            stop.setEnabled(true);
            ThreadGroup tg = (ThreadGroup) tgCombo.getSelectedItem();
            engine.startDebugging(tg, engine.getExecutionTree(tg), stepper);
        }
    }

    private class StepOver implements ActionListener, StepTrigger {
        @Override
        public void actionPerformed(ActionEvent e) {
            log.debug("Step over");
            synchronized (this) {
                this.notifyAll();
            }
        }

        @Override
        public void notify(Object t) {
            step.setEnabled(true);
            try {
                log.debug("Stopping before: " + t);
                synchronized (this) {
                    this.wait();
                }
                log.debug("Proceeding with: " + t);
            } catch (InterruptedException e) {
                engine.stopDebugging();
            }
            step.setEnabled(false);
        }
    }
}
