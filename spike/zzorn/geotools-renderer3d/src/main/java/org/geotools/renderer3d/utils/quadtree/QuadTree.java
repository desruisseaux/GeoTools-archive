package org.geotools.renderer3d.utils.quadtree;

import java.util.Collection;

/**
 * A quadtree datastructure for fast geometrical look-up of nodes in a certain area.
 * <p/>
 * Should also provide access to the quadtree structure itself, e.g. for density field visualization.
 * <p/>
 * Should not have a fixed root node, but instead expand the root as needed also.
 * <p/>
 * Nodes can be added and removed from the quadtree.  It has properties controlling the minimum and maximum number of
 * nodes in a sub-tree.
 *
 * @author Hans Häggström
 */
public interface QuadTree
{

    //======================================================================
    // Public Methods

    /**
     * @return maximum number of elements in one quad tree node at any time.  If there are mode, the quad tree node
     *         will be split.
     */
    int getMaximumNumberOfElementsInANode();

    /**
     * @return minimum number of elements in one quad tree node at any time.  If there are less, the quad tree node
     *         will be joined with other nodes, unless it is the only one left.
     */
    int getMinimumNumberOfElementsInANode();


    /**
     * Adds the specified element to this QuadTree.
     *
     * @param element the element to add.
     */
    void addElement( QuadTreeElement element );

    /**
     * Removes the specified element from this QuadTree.
     *
     * @param element the element to remove.
     */
    void removeElement( QuadTreeElement element );


    /**
     * Retrieves the elements at a certain maximum distance from the specified location.
     *
     * @param x                       center x
     * @param y                       center y
     * @param radius                  radius of the retrieval area.  Note that the retrieval area is not circular,
     *                                but rather a square with the side of 2*radius.
     * @param elementOutputCollection the collection to add matching elements to.
     */
    void getElements( double x, double y, double radius, Collection elementOutputCollection );

    /**
     * Retrieves the elements inside the specified rectangle.
     *
     * @param x1                      upper left corner
     * @param y1                      upper right corner
     * @param x2                      lower left corner
     * @param y2                      lower right corner
     * @param elementOutputCollection the collection to add matching elements to.
     */
    void getElements( double x1, double y1, double x2, double y2, Collection elementOutputCollection );


    /**
     * @return the root node of this QuadTree.
     */
    QuadTreeNode getRootNode();


    /**
     * Called by a QuadTreeNode when the root node is changed.
     * <p/>
     * Should not be called from client code.
     *
     * @param newRootNode the new root node.
     */
    void setRootNode( QuadTreeNode newRootNode );

    /**
     * @return a factory used to create node data for the quad tree nodes.  Does not return null.
     */
    NodeDataFactory getNodeDataFactory();

}
