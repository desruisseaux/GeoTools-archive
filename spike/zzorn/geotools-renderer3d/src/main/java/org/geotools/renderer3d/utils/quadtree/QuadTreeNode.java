package org.geotools.renderer3d.utils.quadtree;

import java.util.Collection;
import java.util.Set;

/**
 * A node in the quad tree.
 *
 * @author Hans Häggström
 */
public interface QuadTreeNode
{

    //======================================================================
    // Public Methods

    /**
     * Adds the specified element to this node, or to the parent or child node that it belongs to.
     * <p/>
     * Creates a new parent or child node if needed.
     *
     * @param element the element to add.
     */
    void addElement( QuadTreeElement element );

    /**
     * Removes the specified element from this node, or the parent or child node it is under.
     * <p/>
     * Deletes empty leaf or root nodes if necessary.
     *
     * @param element the element to remove.
     */
    void removeElement( QuadTreeElement element );


    /**
     * @return the highest parent node.
     */
    QuadTreeNode getRootNode();


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
     * @return true if the specified located object is inside this QuadTreeNode area.
     */
    boolean isInside( LocatedDoublePrecisionObject locatedObject );

    /**
     * Only called from other quad tree nodes, should not be called by client code.
     *
     * @param childNodeToRemove      the child node to remove
     * @param elementsToMoveToParent the elements of the child node to add to this node
     */
    void removeChildNode( final QuadTreeNode childNodeToRemove, final Set elementsToMoveToParent );

    double getCenterX();

    double getCenterY();

    double getX1();

    double getY1();

    double getX2();

    double getY2();

    /**
     * @return the data object associated with this quad tree node, or null if none available.
     */
    Object getNodeData();

    /**
     * @param nodeData the data object associated with this quad tree node, or null if none.
     */
    void setNodeData( Object nodeData );

}

