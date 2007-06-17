package org.geotools.renderer3d.utils.quadtree;


/**
 * @author Hans Häggström
 */
public class QuadTreeElementImpl
        implements QuadTreeElement
{

    //======================================================================
    // Private Fields

    private double myX = 0.0;
    private double myY = 0.0;
    private Object myDataObject = null;
    private QuadTreeNode myQuadTreeNode = null;

    //======================================================================
    // Public Methods

    //----------------------------------------------------------------------
    // Constructors

    /**
     * Creates a new quad tree element, located at origo, with no data object.
     */
    public QuadTreeElementImpl()
    {
    }


    /**
     * Creates a new quad tree element, located at the specified position, and with the specified data object.
     */
    public QuadTreeElementImpl( final double x, final double y, final Object dataObject )
    {
        myX = x;
        myY = y;
        myDataObject = dataObject;
    }

    //----------------------------------------------------------------------
    // LocatedDoublePrecisionObject Implementation

    public double getX()
    {
        return myX;
    }


    public double getY()
    {
        return myY;
    }

    //----------------------------------------------------------------------
    // QuadTreeElement Implementation

    public void setPosition( final double x, final double y )
    {
        myX = x;
        myY = y;

        // TODO: Move in quad tree
        throw new UnsupportedOperationException( "This method has not yet been implemented." ); // IMPLEMENT
    }


    public QuadTreeNode getQuadTreeNode()
    {
        return myQuadTreeNode;
    }


    public void setQuadTreeNode( final QuadTreeNode quadTreeNode )
    {
        myQuadTreeNode = quadTreeNode;
    }


    public Object getDataObject()
    {
        return myDataObject;
    }


    public void setDataObject( final Object dataObject )
    {
        myDataObject = dataObject;
    }

}
