package com.blazemeter.jmeter.debugger.engine;

import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class ThreadGroupSelector {
    private static final Logger log = LoggingManager.getLoggerForClass();
    private final HashTree tree;
    private TreeClonerTG cloner;

    public ThreadGroupSelector(HashTree testTree) {
        tree = testTree;

        AbstractThreadGroup[] grps = getThreadGroups();
        if (grps.length > 0) {
            selectThreadGroup(grps[0]);
        } else {
            log.debug("Empty test plan " + testTree);
        }
    }

    public AbstractThreadGroup[] getThreadGroups() {
        SearchClass<AbstractThreadGroup> searcher = new SearchClass<>(AbstractThreadGroup.class);
        tree.traverse(searcher);
        return searcher.getSearchResults().toArray(new AbstractThreadGroup[0]);
    }

    public void selectThreadGroup(AbstractThreadGroup tg) {
        log.debug("Selecting thread group " + tg.getName() + ": " + tg);
        cloner = new TreeClonerTG(tg);
        tree.traverse(cloner);
    }

    public HashTree getSelectedTree() {
        if (cloner == null) {
            throw new IllegalStateException();
        }
        HashTree clonedTree = cloner.getClonedTree();
        return clonedTree;
    }
}
