package org.geotools.renderer3d.utils.quadtree;

import org.geotools.renderer3d.utils.ParameterChecker;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Hans Häggström
 */
public class QuadTreeImpl<N>
        implements QuadTree<N>
{

    //======================================================================
    // Private Fields

    private final double myStartRadius;

    private final NodeDataFactory<N> myNodeDataFactory;

    private final List<QuadTreeListener<N>> myListeners = new ArrayList<QuadTreeListener<N>>( 3 );

    private QuadTreeNode<N> myRootNode;

    //======================================================================
    // Private Constants

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

    /**
     * @param startRadius
     * @param nodeDataFactory a factory for creating data objects for nodes.  May be null (in that case all node data objects will be null by default).
     */
    public QuadTreeImpl( final double startRadius,
                         NodeDataFactory<N> nodeDataFactory )
    {
        ParameterChecker.checkPositiveNonZeroNormalNumber( startRadius, "startRadius" );

        myStartRadius = startRadius;


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

    public QuadTreeNode<N> getRootNode()
    {
        buildRootNodeIfNeeded( 0, 0 );

        return myRootNode;
    }


    public void setRootNode( QuadTreeNode<N> newRootNode )
    {
        ParameterChecker.checkNotNull( newRootNode, "newRootNode" );

        myRootNode = newRootNode;

        for ( QuadTreeListener<N> listener : myListeners )
        {
            listener.onRootChanged( myRootNode );
        }
    }


    public NodeDataFactory<N> getNodeDataFactory()
    {
        return myNodeDataFactory;
    }


    public void addQuadTreeListener( QuadTreeListener<N> addedQuadTreeListener )
    {
        ParameterChecker.checkNotNull( addedQuadTreeListener, "addedQuadTreeListener" );
        ParameterChecker.checkNotAlreadyContained( addedQuadTreeListener, myListeners, "myListeners" );

        myListeners.add( addedQuadTreeListener );
    }


    public void removeQuadTreeListener( QuadTreeListener<N> removedQuadTreeListener )
    {
        ParameterChecker.checkNotNull( removedQuadTreeListener, "removedQuadTreeListener" );
        ParameterChecker.checkContained( removedQuadTreeListener, myListeners, "myListeners" );

        myListeners.remove( removedQuadTreeListener );
    }

    //======================================================================
    // Private Methods

    private void buildRootNodeIfNeeded( final double startCenterX, final double startCenterY )
    {
        if ( myRootNode == null )
        {
            myRootNode = new QuadTreeNodeImpl<N>( this, startCenterX, startCenterY, myStartRadius );
        }
    }

}
