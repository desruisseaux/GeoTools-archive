package org.geotools.renderer3d.utils.quadtree;

/**
 * Creates the data object for a quad tree node, when needed.
 * <p/>
 * N is the type of a data object associated with each QuadTreeNode.
 *
 * @author Hans H�ggstr�m
 */
public interface NodeDataFactory<N>
{
    /**
     * Creates the data object for a quad tree node.
     * <p/>
     * Called when the node is created, or alternatively when the node data object is first requested.
     *
     * @param node The node to create the data object for.
     *
     * @return the data object for the node.  May be null.
     */
    N createNodeDataObject( QuadTreeNode<N> node );

}
