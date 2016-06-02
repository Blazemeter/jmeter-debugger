package com.blazemeter.jmeter.debugger.gui;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;


public class EvaluatePanel extends JPanel implements ActionListener {
    private JTextField exprField = new JTextField();
    private JButton doBtn = new JButton("Evaluate");
    private LoggerPanelWrapping result = new LoggerPanelWrapping();
    private JMeterContext context = JMeterContextService.getContext();

    public EvaluatePanel() {
        super(new BorderLayout());

        JPanel container = new JPanel(new BorderLayout());
        container.add(new JLabel("JMeter Expression: "), BorderLayout.WEST);
        container.add(exprField, BorderLayout.CENTER);
        container.add(doBtn, BorderLayout.EAST);

        add(container, BorderLayout.NORTH);
        add(new JScrollPane(result), BorderLayout.CENTER);

        exprField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                doBtn.doClick();
            }
        });

        doBtn.addActionListener(this);
    }

    public void refresh(JMeterContext ctx, boolean continuing) {
        result.clear();
        doBtn.setEnabled(!continuing);
        actionPerformed(new ActionEvent(this, 0, ""));
        this.context = ctx;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        //exprField.setEditable(enabled);
        result.setEnabled(enabled);
        doBtn.setEnabled(enabled);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        result.clear();
        if (exprField.getText().isEmpty()) {
            return;
        }

        CompoundVariable masterFunction = new CompoundVariable(exprField.getText());
        SampleResult previousResult = context.getPreviousResult();
        Sampler currentSampler = context.getCurrentSampler();
        try {
            result.setText(masterFunction.execute(previousResult, currentSampler));
        } catch (Throwable e) {
            ByteArrayOutputStream text = new ByteArrayOutputStream(1024);
            e.printStackTrace(new PrintStream(text));
            result.setText(text.toString());
            result.scrollToTop();
        }
    }
}
