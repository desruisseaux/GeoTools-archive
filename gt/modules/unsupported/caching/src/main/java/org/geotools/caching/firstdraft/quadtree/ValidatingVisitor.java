package org.geotools.caching.firstdraft.quadtree;

import org.geotools.caching.firstdraft.spatialindex.spatialindex.IData;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.INode;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.IVisitor;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.Region;


public class ValidatingVisitor implements IVisitor {
    private final Region target;
    private Node lastNode = null;

    public ValidatingVisitor(Region target) {
        this.target = target;
    }

    public void visitData(IData d) {
        // do nothing
    }

    public void visitNode(INode n) {
        if (n instanceof Node) {
            Node node = (Node) n;

            if (target.contains(node.getShape())) {
                node.entry.setValid();
            }

            if (node.getShape().contains(target)) {
                lastNode = node;
            }
        }
    }

    public void updateTree() {
        if (lastNode != null) {
            lastNode.entry.setValid();
        }
    }
}
