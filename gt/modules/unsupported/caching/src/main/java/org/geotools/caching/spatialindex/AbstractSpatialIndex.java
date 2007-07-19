package org.geotools.caching.spatialindex;

import java.util.ArrayList;
import java.util.Stack;


public abstract class AbstractSpatialIndex implements SpatialIndex {
    //public static final int RtreeVariantQuadratic = 1;
    //public static final int RtreeVariantLinear = 2;
    //public static final int RtreeVariantRstar = 3;
    //public static final int PersistentIndex = 1;
    //public static final int PersistentLeaf = 2;
    public static final int ContainmentQuery = 1;
    public static final int IntersectionQuery = 2;
    protected Node root;
    protected int dimension;
    protected Region infiniteRegion;
    protected Statistics stats;
    protected ArrayList writeNodeCommands = new ArrayList();
    protected ArrayList readNodeCommands = new ArrayList();
    protected ArrayList deleteNodeCommands = new ArrayList();

    public void addDeleteNodeCommand(NodeCommand nc) {
        deleteNodeCommands.add(nc);
    }

    public void addReadNodeCommand(NodeCommand nc) {
        readNodeCommands.add(nc);
    }

    public void addWriteNodeCommand(NodeCommand nc) {
        writeNodeCommands.add(nc);
    }

    public void intersectionQuery(Shape query, Visitor v) {
        if (query.getDimension() != dimension) {
            throw new IllegalArgumentException(
                "intersectionQuery: Shape has the wrong number of dimensions.");
        }

        rangeQuery(IntersectionQuery, query, v);
    }

    public void containmentQuery(Shape query, Visitor v) {
        if (query.getDimension() != dimension) {
            throw new IllegalArgumentException(
                "containmentQuery: Shape has the wrong number of dimensions.");
        }

        rangeQuery(ContainmentQuery, query, v);
    }

    public void pointLocationQuery(Shape query, Visitor v) {
        if (query.getDimension() != dimension) {
            throw new IllegalArgumentException(
                "pointLocationQuery: Shape has the wrong number of dimensions.");
        }

        Region r = null;

        if (query instanceof Point) {
            r = new Region((Point) query, (Point) query);
        } else if (query instanceof Region) {
            r = (Region) query;
        } else {
            throw new IllegalArgumentException(
                "pointLocationQuery: Shape can be Point or Region only.");
        }

        rangeQuery(IntersectionQuery, r, v);
    }

    /**
     * @param type
     * @param query
     * @param v
     *
     * TODO: remember child index where to search from on next passage
     */
    protected void rangeQuery(int type, Shape query, Visitor v) {
        Node current = this.root;
        current.setVisited(false);

        Stack nodes = new Stack();

        if (relate(current.getShape(), query, type)) {
            nodes.push(current);
        }

        while (!nodes.isEmpty()) {
            current = (Node) nodes.pop();

            if (!current.isVisited()) {
                v.visitNode(current);

                for (int i = 0; i < current.getChildrenCount(); i++) {
                    current.getSubNode(i).setVisited(false);
                }

                visitData(current, v);

                current.setVisited(true);
            }

            // TODO: start from last child + 1 rather than from 0
            for (int i = 0; i < current.getChildrenCount(); i++) {
                Node child = current.getSubNode(i);

                if (!child.isVisited()) {
                    if (relate(child.getShape(), query, type)) {
                        // we will go back to this one later to examine other children
                        nodes.push(current);
                        // meanwhile, we put one child at a time into stack, so we do not waste space
                        nodes.push(child);

                        break;
                    } else {
                        // we won't have to compute intersection again and again
                        child.setVisited(true);
                    }
                }
            }
        }
    }

    protected abstract void visitData(Node n, Visitor v);

    protected static boolean relate(Shape candidate, Shape query, int type) {
        if (type == IntersectionQuery) {
            return candidate.intersects(query);
        } else if (type == ContainmentQuery) {
            return candidate.contains(query);
        } else {
            throw new UnsupportedOperationException(
                "Type must be either IntersectionQuery or ContainmentQuery");
        }
    }

    public void nearestNeighborQuery(int k, Shape query, Visitor v,
        NearestNeighborComparator nnc) {
        // TODO Auto-generated method stub
    }

    public void nearestNeighborQuery(int k, Shape query, Visitor v) {
        // TODO Auto-generated method stub
    }

    public void queryStrategy(QueryStrategy qs) {
        Node current = this.root;

        while (true) {
            boolean[] hasNext = new boolean[] { false };
            current = qs.getNextNode(current, hasNext);

            if (hasNext[0] == false) {
                break;
            }
        }
    }

    public boolean deleteData(Shape shape, int id) {
        if (shape.getDimension() != dimension) {
            throw new IllegalArgumentException(
                "deleteData: Shape has the wrong number of dimensions.");
        }

        if (this.root.getShape().contains(shape)) {
            return deleteData(this.root, shape, id);
        } else {
            return false;
        }
    }

    protected abstract boolean deleteData(Node n, Shape shape, int id);

    public void insertData(Object data, Shape shape, int id) {
        if (shape.getDimension() != dimension) {
            throw new IllegalArgumentException(
                "insertData: Shape has the wrong number of dimensions.");
        }

        if (this.root.getShape().contains(shape)) {
            insertData(this.root, data, shape, id);
        } else {
            insertDataOutOfBounds(data, shape, id);
        }
    }

    protected abstract void insertData(Node n, Object data, Shape shape, int id);

    protected abstract void insertDataOutOfBounds(Object data, Shape shape,
        int id);
}
