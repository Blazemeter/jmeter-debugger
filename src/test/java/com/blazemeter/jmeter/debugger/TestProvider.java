package com.blazemeter.jmeter.debugger;


import com.blazemeter.jmeter.debugger.engine.SearchClass;
import com.blazemeter.jmeter.debugger.engine.TestTreeProvider;
import kg.apc.emulators.TestJMeterUtils;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jorphan.collections.HashTree;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class TestProvider implements TestTreeProvider {
    private JMeterTreeModel mdl = new JMeterTreeModel();

    public TestProvider() throws IllegalUserActionException, IOException {
        File file = new File(this.getClass().getResource("/com/blazemeter/jmeter/debugger/sample1.jmx").getFile());
        String basedir = TestJMeterUtils.fixWinPath(file.getParentFile().getAbsolutePath());

        File f = new File(basedir + "/sample1.jmx");
        mdl.addSubTree(SaveService.loadTree(f), (JMeterTreeNode) mdl.getRoot());
    }

    public TestProvider(String path, String name) throws IllegalUserActionException, IOException {
        File file = new File(this.getClass().getResource(path).getFile());
        String basedir = TestJMeterUtils.fixWinPath(file.getParentFile().getAbsolutePath());

        File f = new File(basedir + '/' + name);
        mdl.addSubTree(SaveService.loadTree(f), (JMeterTreeNode) mdl.getRoot());
    }

    @Override
    public HashTree getTestTree() {
        return mdl.getTestPlan();
    }

    public AbstractThreadGroup getTG(int i) {
        SearchClass<AbstractThreadGroup> searcher = new SearchClass<>(AbstractThreadGroup.class);
        mdl.getTestPlan().traverse(searcher);
        Collection<AbstractThreadGroup> searchResults = searcher.getSearchResults();
        return searchResults.toArray(new AbstractThreadGroup[0])[i];
    }

    public JMeterTreeModel getTreeModel() {
        return mdl;
    }
}
