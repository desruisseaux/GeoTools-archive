package org.geotools.renderer3d.utils.quadtree;

import org.geotools.renderer3d.utils.ParameterChecker;

import java.util.Collection;


/**
 * @author Hans Häggström
 */
public class QuadTreeImpl
        implements QuadTree
{

    //======================================================================
    // Private Fields

    private final double myStartRadius;

    private final int myMinimumNumberOfElementsInANode;
    private final int myMaximumNumberOfElementsInANode;

    private final NodeDataFactory myNodeDataFactory;

    private QuadTreeNode myRootNode;

    //======================================================================
    // Private Constants

    private static final int DEFAULT_MINIMUM_NUMBER_OF_ELEMENTS_IN_A_NODE = 2;
    private static final int DEFAULT_MAXIMUM_NUMBER_OF_ELEMENTS_IN_A_NODE = 20;

    private static final NodeDataFactory NULL_NODE_DATA_FACTORY = new NodeDataFactory()
    {
        public Object createNodeDataObject( final QuadTreeNode node )
        {
            return null;
        }

    };

    //======================================================================
    // Public Methods

    //----------------------------------------------------------------------
    // Constructors

    public QuadTreeImpl( final double startRadius, NodeDataFactory nodeDataFactory )
    {
        this( startRadius,
              nodeDataFactory,
              DEFAULT_MINIMUM_NUMBER_OF_ELEMENTS_IN_A_NODE,
              DEFAULT_MAXIMUM_NUMBER_OF_ELEMENTS_IN_A_NODE );
    }


    /**
     * @param startRadius
     * @param nodeDataFactory                a factory for creating data objects for nodes.  May be null (in that case all node data objects will be null by default).
     * @param minimumNumberOfElementsInANode
     * @param maximumNumberOfElementsInANode
     */
    public QuadTreeImpl( final double startRadius,
                         NodeDataFactory nodeDataFactory,
                         final int minimumNumberOfElementsInANode,
                         final int maximumNumberOfElementsInANode )
    {
        ParameterChecker.checkPositiveNonZeroNormalNumber( startRadius, "startRadius" );

        ParameterChecker.checkPositiveNonZeroInteger( minimumNumberOfElementsInANode,
                                                      "minimumNumberOfElementsInANode" );
        ParameterChecker.checkPositiveNonZeroInteger( maximumNumberOfElementsInANode,
                                                      "maximumNumberOfElementsInANode" );
        ParameterChecker.checkIntegerEqualsOrLargerThan( maximumNumberOfElementsInANode,
                                                         "maximumNumberOfElementsInANode",
                                                         minimumNumberOfElementsInANode );

        myStartRadius = startRadius;

        myMinimumNumberOfElementsInANode = minimumNumberOfElementsInANode;
        myMaximumNumberOfElementsInANode = maximumNumberOfElementsInANode;

        if ( nodeDataFactory == null )
        {
            myNodeDataFactory = NULL_NODE_DATA_FACTORY;
        }
        else
        {
            myNodeDataFactory = nodeDataFactory;
        }
    }

    //----------------------------------------------------------------------
    // QuadTree Implementation

    public int getMaximumNumberOfElementsInANode()
    {
        return myMaximumNumberOfElementsInANode;
    }


    public int getMinimumNumberOfElementsInANode()
    {
        return myMinimumNumberOfElementsInANode;
    }


    public void addElement( QuadTreeElement element )
    {
        // Use the first element added as an indication of where other elements will be added.
        if ( myRootNode == null )
        {
            buildRootNodeIfNeeded( element.getX(), element.getY() );
        }

        myRootNode.addElement( element );
    }


    public void removeElement( QuadTreeElement element )
    {
        if ( myRootNode != null )
        {
            myRootNode.removeElement( element );
        }
    }


    public void getElements( double x, double y, double radius, Collection elementOutputCollection )
    {
        if ( myRootNode != null )
        {
            myRootNode.getElements( x, y, radius, elementOutputCollection );
        }
    }


    public void getElements( double x1, double y1, double x2, double y2, Collection elementOutputCollection )
    {
        if ( myRootNode != null )
        {
            myRootNode.getElements( x1, y1, x2, y2, elementOutputCollection );
        }
    }


    public QuadTreeNode getRootNode()
    {
        buildRootNodeIfNeeded( 0, 0 );

        return myRootNode;
    }


    public void setRootNode( QuadTreeNode newRootNode )
    {
        ParameterChecker.checkNotNull( newRootNode, "newRootNode" );

        myRootNode = newRootNode;
    }


    public NodeDataFactory getNodeDataFactory()
    {
        return myNodeDataFactory;
    }


    //======================================================================
    // Private Methods

    private void buildRootNodeIfNeeded( final double startCenterX, final double startCenterY )
    {
        if ( myRootNode == null )
        {
            myRootNode = new QuadTreeNodeImpl( this, startCenterX, startCenterY, myStartRadius );
        }
    }

}
