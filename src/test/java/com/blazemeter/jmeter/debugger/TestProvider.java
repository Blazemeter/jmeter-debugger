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
    private HashTree tree;

    public TestProvider() throws IllegalUserActionException, IOException {
        File file = new File(this.getClass().getResource("/com/blazemeter/jmeter/debugger/sample1.jmx").getFile());
        String basedir = TestJMeterUtils.fixWinPath(file.getParentFile().getAbsolutePath());

        File f = new File(basedir + "/sample1.jmx");
        JMeterTreeModel mdl = new JMeterTreeModel();
        mdl.addSubTree(SaveService.loadTree(f), (JMeterTreeNode) mdl.getRoot());
        tree = mdl.getTestPlan();
    }

    @Override
    public HashTree getTestTree() {
        return tree;
    }

    public AbstractThreadGroup getTG(int i) {
        SearchClass<AbstractThreadGroup> searcher = new SearchClass<>(AbstractThreadGroup.class);
        tree.traverse(searcher);
        Collection<AbstractThreadGroup> searchResults = searcher.getSearchResults();
        return searchResults.toArray(new AbstractThreadGroup[0])[i];
    }
}
