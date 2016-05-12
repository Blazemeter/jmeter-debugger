package org.jmeterplugins.debugger;

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
    private StepTrigger hook;

    public DebuggerCompiler(HashTree testTree, StepTrigger hook) {
        super(testTree);
        this.hook = hook;
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
            return new DebuggerSamplerPackage(samplePackage, getControllers(samplePackage), hook);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to access controllers");
        }
    }

    private List<Controller> getControllers(SamplePackage test) throws NoSuchFieldException, IllegalAccessException {
        Field field = SamplePackage.class.getDeclaredField("controllers");
        if (!field.isAccessible()) {
            log.debug("Making field accessable: " + field);
            field.setAccessible(true);
        }
        //noinspection unchecked
        return (List<Controller>) field.get(test);
    }
}
