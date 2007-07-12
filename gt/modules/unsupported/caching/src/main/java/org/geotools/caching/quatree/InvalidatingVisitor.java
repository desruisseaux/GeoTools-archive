package org.geotools.caching.quatree;

import org.geotools.caching.spatialindex.spatialindex.IData;
import org.geotools.caching.spatialindex.spatialindex.INode;
import org.geotools.caching.spatialindex.spatialindex.IVisitor;


public class InvalidatingVisitor implements IVisitor {
    protected Node lastNode = null;

    public void visitData(IData d) {
        // do nothing
    }

    public void visitNode(INode n) {
        if (n instanceof Node) {
            Node node = (Node) n;
            node.entry.invalidate();
            lastNode = node;
        }
    }

    public void updateTree() {
        lastNode.subNodes.clear();

        Node parent = lastNode.parent;

        while (parent != null) {
            if (isInvalid(parent)) {
                parent.entry.invalidate();
                parent.subNodes.clear();
                parent = parent.parent;
            } else {
                // we don't have to climb the tree any more
                break;
            }
        }
    }

    protected boolean isInvalid(Node n) {
        boolean ret = true;

        for (int i = 0; i < n.getChildrenCount(); i++) {
            Node child = n.getSubNode(i);

            if ((!child.isLeaf()) || child.isValid()) {
                ret = false;

                break;
            }
        }

        return ret;
    }
}
