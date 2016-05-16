package com.blazemeter.jmeter.debugger;

import com.blazemeter.jmeter.debugger.elements.AbstractDebugElement;
import com.blazemeter.jmeter.debugger.engine.DebuggingThread;
import com.blazemeter.jmeter.debugger.engine.StepTrigger;
import kg.apc.emulators.TestJMeterUtils;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.JMeterThreadMonitor;
import org.apache.jmeter.threads.ListenerNotifier;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.jorphan.collections.SearchByClass;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class DebuggingThreadTest {
    private static final Logger log = LoggingManager.getLoggerForClass();
    public static final StepTrigger hook = new StepTrigger() {
        @Override
        public void notify(AbstractDebugElement o) {
            AbstractDebugElement te = (AbstractDebugElement) o;
            log.info(">>> Stopping before step: " + te.getWrappedElement() + " <<<");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    @BeforeClass
    public static void setUp() throws Exception {
        TestJMeterUtils.createJmeterEnv();
    }

    @Test
    public void testBasic() throws IOException {
        File file = new File(this.getClass().getResource("/com/blazemeter/jmeter/debugger/sample1.jmx").getFile());
        String basedir = TestJMeterUtils.fixWinPath(file.getParentFile().getAbsolutePath());

        File f = new File(basedir + "/sample1.jmx");
        HashTree tree = SaveService.loadTree(f);
        JMeterThreadMonitor monitor = new JMeterThreadMonitor() {
            @Override
            public void threadFinished(JMeterThread thread) {

            }
        };
        ListenerNotifier note = new ListenerNotifier();

        SearchByClass<AbstractThreadGroup> searcher = new SearchByClass<>(AbstractThreadGroup.class);
        tree.traverse(searcher);
        Collection<AbstractThreadGroup> iter = searcher.getSearchResults();
        for (AbstractThreadGroup tg : iter) {
            ListedHashTree tgTree = (ListedHashTree) searcher.getSubTree(tg);
            DebuggingThread thread = new DebuggingThread(tgTree, monitor, note, hook);
            thread.setThreadGroup(tg);
            thread.run();
        }

    }

}