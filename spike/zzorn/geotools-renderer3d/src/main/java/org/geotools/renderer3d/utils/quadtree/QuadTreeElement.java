package org.geotools.renderer3d.utils.quadtree;


/**
 * A located object stored in the quad tree.
 * Can store some user data object.
 * <p/>
 * TODO: Add move/relocated method, that allows it to be moved in the quad-tree.
 *
 * @author Hans Häggström
 */
public interface QuadTreeElement
        extends LocatedDoublePrecisionObject
{
    void setPosition( double x, double y );


    /**
     * @return the quad tree node that the element is in at the moment, or null if it is not in a quad tree.
     */
    QuadTreeNode getQuadTreeNode();

    /**
     * Called by the QuadTree implementation, do not call directly from client code.
     *
     * @param quadTreeNode the quad tree node that the element is in at the moment, or null if it is not in a quad tree.
     */
    void setQuadTreeNode( QuadTreeNode quadTreeNode );

    /**
     * @return a user specified data object that this quad tree element wraps.
     */
    Object getDataObject();

    /**
     * @param dataObject a user specified data object that this quad tree element wraps.
     */
    void setDataObject( Object dataObject );
}

