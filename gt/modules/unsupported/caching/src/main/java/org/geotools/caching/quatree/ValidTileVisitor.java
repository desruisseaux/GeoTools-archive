package org.geotools.caching.quatree;

import org.geotools.caching.spatialindex.spatialindex.IData;
import org.geotools.caching.spatialindex.spatialindex.INode;
import org.geotools.caching.spatialindex.spatialindex.IVisitor;


public class ValidTileVisitor implements IVisitor {
    protected boolean isCovered = false;
    protected Node lastNode = null;

    public void visitData(IData d) {
        // do nothing
    }

    public void visitNode(INode n) {
        if (n instanceof Node) {
            Node node = (Node) n;
            isCovered = isCovered || node.isValid();
            lastNode = node;
        }
    }

    public boolean isCovered() {
        return isCovered;
    }

    public Node getLastNode() {
        return lastNode;
    }
}
