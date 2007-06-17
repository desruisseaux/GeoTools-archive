package org.geotools.renderer3d.utils.quadtree;

/**
 * Creates the data object for a quad tree node, when needed.
 *
 * @author Hans Häggström
 */
public interface NodeDataFactory
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
    Object createNodeDataObject( QuadTreeNode node);

}
