package org.geotools.renderer3d.utils.quadtree;

import org.geotools.renderer3d.utils.BoundingRectangle;
import org.geotools.renderer3d.utils.BoundingRectangleImpl;
import org.geotools.renderer3d.utils.MathUtils;
import org.geotools.renderer3d.utils.ParameterChecker;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Hans Häggström
 */
public class QuadTreeNodeImpl
        implements QuadTreeNode
{

    //======================================================================
    // Private Fields

    private final QuadTree myQuadTree;
    private final BoundingRectangle myBoundingRectangle;

    private QuadTreeNode[] myChildren = null;
    private QuadTreeNode myParent = null;
    private Set myElements = null;

    private Object myNodeData = null;

    //======================================================================
    // Public Methods

    //----------------------------------------------------------------------
    // Constructors

    public QuadTreeNodeImpl( QuadTree quadTree, double centerX, double centerY, double radius )
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


    public QuadTreeNodeImpl( final QuadTree quadTree,
                             final double x1,
                             final double y1,
                             final double x2,
                             final double y2 )
    {
        this( quadTree, new BoundingRectangleImpl( x1, y1, x2, y2 ) );
    }

    public QuadTreeNodeImpl( final QuadTree quadTree,
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

    //----------------------------------------------------------------------
    // QuadTreeNode Implementation

    public void addElement( QuadTreeElement element )
    {
        ParameterChecker.checkNotNull( element, "element" );

        if ( myBoundingRectangle.isInside( element ) )
        {
            if ( hasBeenSplit() )
            {
                addElementToChildNode( element );
            }
            else
            {
                addElementToThisNode( element );

                if ( getNumberOfelements() > myQuadTree.getMaximumNumberOfElementsInANode() )
                {
                    splitNode();
                }
            }
        }
        else
        {
            // Element is outside, add to parent
            addElementToParentNode( element );
        }
    }


    public void removeElement( QuadTreeElement element )
    {
        ParameterChecker.checkNotNull( element, "element" );

        final QuadTreeNode hostNode = element.getQuadTreeNode();
        ParameterChecker.checkNotNull( hostNode, "element.getQuadTreeNode()" );

        if ( this == hostNode )
        {
            if ( myElements != null )
            {
                myElements.remove( element );
                element.setQuadTreeNode( null );

                // Check if we need to collapse the node
                checkForNeedToCollapse();
            }
            else
            {
                throw new IllegalStateException( "The element '" + element + "' or the quad tree it is in is in " +
                                                 "an illegal state, the element specifies '" + toString() + "' as " +
                                                 "its host node, but '" + toString() + "' does not contain it." );
            }
        }
        else
        {
            // The element is in some other node
            hostNode.removeElement( element );
        }
    }

    public BoundingRectangle getBounds()
    {
        return myBoundingRectangle;
    }


    public QuadTreeNode getRootNode()
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


    public void getElements( double x, double y, double radius, Collection elementOutputCollection )
    {
        getElements( x - radius, y - radius, x + radius, y + radius, elementOutputCollection );
    }


    public void getElements( double x1, double y1,
                             double x2, double y2,
                             Collection elementOutputCollection )
    {
        // Check overlap
        if ( myBoundingRectangle.overlaps( x1, y1, x2, y2 ) )
        {
            // Check our own element
            if ( myElements != null )
            {
                for ( Iterator it = myElements.iterator(); it.hasNext(); )
                {
                    final QuadTreeElement element = (QuadTreeElement) it.next();

                    if ( MathUtils.isInsideRectangle( element.getX(),
                                                      element.getY(),
                                                      x1, y1, x2, y2 ) )
                    {
                        elementOutputCollection.add( element );
                    }
                }
            }

            // Check children
            if ( myChildren != null )
            {
                for ( int i = 0; i < myChildren.length; i++ )
                {
                    final QuadTreeNode child = myChildren[ i ];

                    if ( child != null )
                    {
                        child.getElements( x1, y1, x2, y2, elementOutputCollection );
                    }
                }
            }
        }
    }


    public void removeChildNode( final QuadTreeNode childNodeToRemove, final Set elementsToMoveToParent )
    {
        ParameterChecker.checkNotNull( childNodeToRemove, "childNodeToRemove" );

        if ( myChildren == null )
        {
            throw new IllegalStateException(
                    "removeChildNode can only be called from a child node that is contained in the called parent node." );
        }

        boolean foundChildToRemove = false;
        boolean someChildrenFound = false;
        for ( int i = 0; i < myChildren.length; i++ )
        {
            if ( myChildren[ i ] == childNodeToRemove )
            {
                myChildren[ i ] = null;

                getOrCreateElementsSet().addAll( elementsToMoveToParent );

                foundChildToRemove = true;
            }
            else if ( myChildren[ i ] != null )
            {
                someChildrenFound = true;
            }
        }

        if ( !foundChildToRemove )
        {
            throw new IllegalStateException(
                    "removeChildNode can only be called from a child node that is contained in the called parent node." );
        }

        if ( !someChildrenFound )
        {
            myChildren = null;
        }
    }


    public Object getNodeData()
    {
        // Lazy creation
        if ( myNodeData == null )
        {
            myNodeData = myQuadTree.getNodeDataFactory().createNodeDataObject( this );
        }

        return myNodeData;
    }


    public void setNodeData( final Object nodeData )
    {
        myNodeData = nodeData;
    }

    //----------------------------------------------------------------------
    // Other Public Methods

    //======================================================================
    // Private Methods

    private boolean hasBeenSplit()
    {
        return myChildren != null;
    }


    private void addElementToParentNode( final QuadTreeElement element )
    {
        if ( myParent == null )
        {
            createNewParent( element );
        }
        else
        {
            myParent.addElement( element );
        }
    }


    private void addElementToChildNode( final QuadTreeElement element )
    {
        // Get the subquadrant that the element is inside
        final int subquadrant = myBoundingRectangle.getSubquadrantAt( element );
        if ( subquadrant < 0 )
        {
            // Should not happen:
            throw new IllegalStateException( "Element '" + element + "' is not inside any child node of '" + this +
                                             "', although it is inside it." );
        }

        // Create children array if needed
        if ( myChildren == null )
        {
            //noinspection unchecked
            myChildren = new QuadTreeNode[4];
        }

        // Create the subnode if needed
        if ( myChildren[ subquadrant ] == null )
        {
            final BoundingRectangle rectangle = myBoundingRectangle.createSubquadrantBoundingRectangle( subquadrant );
            myChildren[ subquadrant ] = new QuadTreeNodeImpl( myQuadTree, rectangle );
        }

        // Add element to the subnode
        myChildren[ subquadrant ].addElement( element );
    }


    private void checkForNeedToCollapse()
    {
        if ( myElements.size() < myQuadTree.getMinimumNumberOfElementsInANode() )
        {
            if ( myChildren == null )
            {
                // If we have a parent, merge with it.  If not, we are the root, and can not be joined with parent anymore.
                if ( myParent != null )
                {
                    // No children, let's remove ourselves, and move the elements to the parent
                    myParent.removeChildNode( this, myElements );
                }
            }

            // I we do have children, do nothing
        }
    }


    private void splitNode()
    {
        for ( Iterator it = myElements.iterator(); it.hasNext(); )
        {
            addElementToChildNode( (QuadTreeElement) it.next() );
        }

        myElements.clear();
        myElements = null;
    }


    private void createNewParent( final QuadTreeElement element )
    {
        // Calcualte which direction the parent should expand into (towards the element)
        final int parentSubsector = myBoundingRectangle.getSubsectorAt( element );

        // Create a new parent
        final BoundingRectangle boundingRectangle = myBoundingRectangle.createParentBoundingRectangle( parentSubsector );
        final QuadTreeNodeImpl parentNode = new QuadTreeNodeImpl( myQuadTree, boundingRectangle );

        // Add this node as a child of the parent node (in the opposite corner of where we expanded)
        final int childSubquadrant = myBoundingRectangle.getOppositeSubquadrant( parentSubsector );
        parentNode.myChildren = new QuadTreeNode[4];
        parentNode.myChildren[ childSubquadrant ] = this;

        // Notify model that we have a new root node
        myQuadTree.setRootNode( parentNode );
    }


    private void addElementToThisNode( final QuadTreeElement element )
    {
        getOrCreateElementsSet().add( element );

        element.setQuadTreeNode( this );
    }


    private Set getOrCreateElementsSet()
    {
        if ( myElements == null )
        {
            myElements = new HashSet( myQuadTree.getMaximumNumberOfElementsInANode() );
        }

        return myElements;
    }


    private int getNumberOfelements()
    {
        if ( myElements == null )
        {
            return 0;
        }
        else
        {
            return myElements.size();
        }
    }

}

