package com.blazemeter.jmeter.debugger.engine;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.TransactionSampler;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.SamplePackage;
import org.apache.jmeter.threads.TestCompiler;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.lang.reflect.Field;
import java.util.List;

public class DebuggerCompiler extends TestCompiler {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private DebuggerSamplerPackage lastSamplePackage;

    public DebuggerCompiler(HashTree testTree) {
        super(testTree);
    }

    @Override
    public SamplePackage configureSampler(Sampler sampler) {
        return wrapSamplerPack(super.configureSampler(sampler));
    }

    @Override
    public SamplePackage configureTransactionSampler(TransactionSampler transactionSampler) {
        return wrapSamplerPack(super.configureTransactionSampler(transactionSampler));
    }

    private DebuggerSamplerPackage wrapSamplerPack(SamplePackage samplePackage) {
        try {
            lastSamplePackage = new DebuggerSamplerPackage(samplePackage, getControllers(samplePackage));
            return lastSamplePackage;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to access controllers");
        }
    }

    private List<Controller> getControllers(SamplePackage test) throws NoSuchFieldException, IllegalAccessException {
        Field field = SamplePackage.class.getDeclaredField("controllers");
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        //noinspection unchecked
        return (List<Controller>) field.get(test);
    }

    public DebuggerSamplerPackage getLastSamplePackage() {
        return lastSamplePackage;
    }
}
