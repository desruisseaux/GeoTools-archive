package org.geotools.renderer3d.utils.quadtree;

import org.geotools.renderer3d.utils.BoundingRectangle;
import org.geotools.renderer3d.utils.BoundingRectangleImpl;
import org.geotools.renderer3d.utils.ParameterChecker;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hans Häggström
 */
public class QuadTreeNodeImpl<N>
        implements QuadTreeNode<N>
{

    //======================================================================
    // Private Fields

    private final QuadTree<N> myQuadTree;
    private final BoundingRectangle myBoundingRectangle;
    private final List<NodeListener<N>> myNodeListeners = new ArrayList<NodeListener<N>>( 3 );

    private QuadTreeNode<N>[] myChildren = null;
    private QuadTreeNode<N> myParent = null;

    private N myNodeData = null;
    private boolean myExpanded = false;

    //======================================================================
    // Private Constants

    private static final int NUMBER_OF_SUBNODES = 4;

    //======================================================================
    // Public Methods

    //----------------------------------------------------------------------
    // Constructors

    public QuadTreeNodeImpl( final QuadTree<N> quadTree,
                             BoundingRectangle boundingRectangle )
    {
        ParameterChecker.checkNotNull( quadTree, "quadTree" );
        ParameterChecker.checkNotNull( boundingRectangle, "boundingRectangle" );
        if ( boundingRectangle.isEmpty() )
        {
            throw new IllegalArgumentException( "The bounding rectangle '" + boundingRectangle +
                                                "' specified for the QuadTreeNode should not be empty." );
        }

        myQuadTree = quadTree;
        myBoundingRectangle = boundingRectangle;
    }


    public QuadTreeNodeImpl( QuadTree<N> quadTree, double centerX, double centerY, double radius )
    {
        this( quadTree,
              centerX - radius,
              centerY - radius,
              centerX + radius,
              centerY + radius );

        ParameterChecker.checkPositiveNonZeroNormalNumber( radius, "radius" );
        ParameterChecker.checkNormalNumber( centerX, "centerX" );
        ParameterChecker.checkNormalNumber( centerY, "centerY" );
    }


    public QuadTreeNodeImpl( final QuadTree<N> quadTree,
                             final double x1,
                             final double y1,
                             final double x2,
                             final double y2 )
    {
        this( quadTree, new BoundingRectangleImpl( x1, y1, x2, y2 ) );
    }

    //----------------------------------------------------------------------
    // QuadTreeNode Implementation

    public BoundingRectangle getBounds()
    {
        return myBoundingRectangle;
    }


    public QuadTreeNode<N> getRootNode()
    {
        if ( myParent != null )
        {
            return myParent.getRootNode();
        }
        else
        {
            return this;
        }
    }


    public N getNodeData()
    {
        // Lazy creation
        if ( myNodeData == null )
        {
            myNodeData = myQuadTree.getNodeDataFactory().createNodeDataObject( this );
        }

        return myNodeData;
    }


    public void setNodeData( final N nodeData )
    {
        myNodeData = nodeData;
    }


    public boolean visitChildren( final NodeVisitor<N> visitor )
    {
        if ( myChildren != null )
        {
            for ( QuadTreeNode<N> child : myChildren )
            {
                if ( !visitor.visitNode( child ) )
                {
                    return false;
                }
            }
        }

        return true;
    }


    public boolean visitDecendants( final NodeVisitor<N> visitor )
    {
        if ( myChildren != null )
        {
            for ( QuadTreeNode<N> child : myChildren )
            {
                if ( !child.visitSelfAndDecendants( visitor ) )
                {
                    return false;
                }
            }
        }

        return true;
    }


    public boolean visitSelfAndDecendants( final NodeVisitor<N> visitor )
    {
        if ( visitor.visitNode( this ) )
        {
            return visitDecendants( visitor );
        }
        else
        {
            return false;
        }
    }


    public int getNumberOfChildren()
    {
        return NUMBER_OF_SUBNODES;
    }


    public QuadTreeNode<N> getChild( final int index )
    {
        if ( myChildren == null )
        {
            return null;
        }
        else
        {
            return myChildren[ index ];
        }
    }


    public void setExpanded( final boolean expanded )
    {
        if ( expanded )
        {
            expand();
        }
        else
        {
            collapse();
        }
    }


    public boolean isExpanded()
    {
        return myExpanded;
    }


    public void delete()
    {
        myNodeData = null;
        myParent = null;
        collapse();

        for ( NodeListener<N> nodeListener : myNodeListeners )
        {
            nodeListener.onDeleted( this );
        }
    }


    public boolean covers( final double x, final double y, final double radius )
    {
        return getBounds().isInside( x, y, radius );
    }


    public void grow( double x, double y )
    {
        if ( isRootNode() )
        {
            // Calcualte which direction the new parent should expand into
            final int parentSubsector = myBoundingRectangle.getSubsectorAt( x, y );

            // Create a new parent
            final BoundingRectangle parentBounds = myBoundingRectangle.createParentBoundingRectangle( parentSubsector );
            final QuadTreeNodeImpl<N> parentNode = new QuadTreeNodeImpl<N>( myQuadTree, parentBounds );

            // Add this node as a child of the parent node (in the opposite corner of where we expanded)
            final int childSubquadrant = myBoundingRectangle.getOppositeSubquadrant( parentSubsector );
            parentNode.myChildren = new QuadTreeNode[NUMBER_OF_SUBNODES];
            parentNode.myChildren[ childSubquadrant ] = this;

            myParent = parentNode;

            // Notify model that we have a new root node
            myQuadTree.setRootNode( parentNode );
        }
        else
        {
            getRootNode().grow( x, y );
        }
    }


    public void growToInclude( double x, double y, final double radius )
    {
        while ( !getRootNode().covers( x, y, radius ) )
        {
            grow( x, y );
        }
    }


    public boolean isRootNode()
    {
        return myParent == null;
    }


    public void addNodeListener( NodeListener<N> addedNodeListener )

    {
        ParameterChecker.checkNotNull( addedNodeListener, "addedNodeListener" );
        ParameterChecker.checkNotAlreadyContained( addedNodeListener, myNodeListeners, "myNodeListeners" );

        myNodeListeners.add( addedNodeListener );
    }


    public void removeNodeListener(
            NodeListener<N> removedNodeListener )

    {
        ParameterChecker.checkNotNull( removedNodeListener, "removedNodeListener" );
        ParameterChecker.checkContained( removedNodeListener, myNodeListeners, "myNodeListeners" );

        myNodeListeners.remove( removedNodeListener );
    }

    //======================================================================
    // Private Methods

    private void expand()
    {
        if ( !myExpanded )
        {
            myExpanded = true;

            // Create the child array if needed
            if ( myChildren == null )
            {
                //noinspection unchecked
                myChildren = new QuadTreeNode[NUMBER_OF_SUBNODES];
            }

            // Create child nodes
            for ( int i = 0; i < NUMBER_OF_SUBNODES; i++ )
            {
                if ( myChildren[ i ] == null )
                {
                    final BoundingRectangle rectangle = myBoundingRectangle.createSubquadrantBoundingRectangle( i );
                    myChildren[ i ] = new QuadTreeNodeImpl<N>( myQuadTree, rectangle );
                }
            }

            for ( NodeListener<N> nodeListener : myNodeListeners )
            {
                nodeListener.onExpanded( this );
            }
        }
    }


    private void collapse()
    {
        if ( myExpanded )
        {
            myExpanded = false;

            // Delete all child nodes
            for ( int i = 0; i < NUMBER_OF_SUBNODES; i++ )
            {
                if ( myChildren != null )
                {
                    final QuadTreeNode<N> child = myChildren[ i ];
                    if ( child != null )
                    {
                        child.delete();
                    }
                }
            }

            myChildren = null;

            for ( NodeListener<N> nodeListener : myNodeListeners )
            {
                nodeListener.onCollapsed( this );
            }
        }
    }

}

