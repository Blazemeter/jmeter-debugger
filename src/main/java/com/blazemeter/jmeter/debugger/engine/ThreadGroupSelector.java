package com.blazemeter.jmeter.debugger.engine;


import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.jorphan.collections.SearchByClass;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ThreadGroupSelector {
    private static final Logger log = LoggingManager.getLoggerForClass();
    private final HashTree tree;
    private TreeClonerTG cloner;

    public ThreadGroupSelector(HashTree testTree) {
        tree = testTree;

        AbstractThreadGroup[] grps = getThreadGroups();
        if (grps.length > 0) {
            selectThreadGroup(grps[0]);
        }
    }

    public void selectThreadGroup(AbstractThreadGroup tg) {
        log.debug("Selecting thread group " + tg.getName() + ": " + tg);
        cloner = new TreeClonerTG(tg);
        tree.traverse(cloner);
    }

    public AbstractThreadGroup[] getThreadGroups() {
        SearchByClass<AbstractThreadGroup> searcher = new SearchByClass<>(AbstractThreadGroup.class);
        tree.traverse(searcher);
        return searcher.getSearchResults().toArray(new AbstractThreadGroup[0]);
    }

    public HashTree getFullTree() {
        return cloner.getClonedTree();
    }

    private HashTree getThreadTestTree() {
        HashTree test = cloner.getClonedTree();
        List<?> testLevelElements = new LinkedList<>(test.list(test.getArray()[0]));
        Iterator it = testLevelElements.iterator();
        while (it.hasNext()) {
            if (it.next() instanceof AbstractThreadGroup) {
                it.remove();
            }
        }

        SearchByClass<AbstractThreadGroup> searcher = new SearchByClass<>(AbstractThreadGroup.class);
        test.traverse(searcher);
        AbstractThreadGroup tg = cloner.getClonedTG();
        ListedHashTree threadGroupTree = (ListedHashTree) searcher.getSubTree(tg);
        threadGroupTree.add(tg, testLevelElements);
        return threadGroupTree;
    }


}
