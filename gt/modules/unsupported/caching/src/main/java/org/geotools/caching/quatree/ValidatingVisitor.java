package org.geotools.caching.quatree;

import org.geotools.caching.spatialindex.spatialindex.IData;
import org.geotools.caching.spatialindex.spatialindex.INode;
import org.geotools.caching.spatialindex.spatialindex.IVisitor;
import org.geotools.caching.spatialindex.spatialindex.Region;


public class ValidatingVisitor implements IVisitor {
    private final Region target;

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
        }
    }
}
