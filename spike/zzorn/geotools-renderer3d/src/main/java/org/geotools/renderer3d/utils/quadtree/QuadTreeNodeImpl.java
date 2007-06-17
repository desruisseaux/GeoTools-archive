package org.geotools.renderer3d.utils.quadtree;

import org.geotools.renderer3d.utils.ParameterChecker;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * REFACTOR: Refactor to use a bounding box?
 *
 * @author Hans Häggström
 */
public class QuadTreeNodeImpl
        implements QuadTreeNode
{

    //======================================================================
    // Private Fields

    private final QuadTree myQuadTree;

    private QuadTreeNode[] myChildren = null;
    private QuadTreeNode myParent = null;
    private double myX1;
    private double myY1;
    private double myX2;
    private double myY2;
    private Set myElements = null;

    private Object myNodeData = null;

    //======================================================================
    // Public Methods

    //----------------------------------------------------------------------
    // Constructors

    public QuadTreeNodeImpl( QuadTree quadTree, double centerX, double centerY, double radius )
    {
        ParameterChecker.checkNotNull( quadTree, "quadTree" );
        ParameterChecker.checkPositiveNonZeroNormalNumber( radius, "radius" );
        ParameterChecker.checkNormalNumber( centerX, "centerX" );
        ParameterChecker.checkNormalNumber( centerY, "centerY" );

        myQuadTree = quadTree;
        myX1 = centerX - radius;
        myX2 = centerX + radius;
        myY1 = centerY - radius;
        myY2 = centerY + radius;
    }


    public QuadTreeNodeImpl( final QuadTree quadTree,
                             final double x1,
                             final double y1,
                             final double x2,
                             final double y2 )
    {
        ParameterChecker.checkNotNull( quadTree, "quadTree" );
        ParameterChecker.checkNormalNumber( x1, "x1" );
        ParameterChecker.checkNormalNumber( y1, "y1" );
        ParameterChecker.checkNormalNumber( x2, "x2" );
        ParameterChecker.checkNormalNumber( y2, "y2" );

        myQuadTree = quadTree;
        myX1 = x1;
        myY1 = y1;
        myX2 = x2;
        myY2 = y2;
    }

    //----------------------------------------------------------------------
    // QuadTreeNode Implementation

    public void addElement( QuadTreeElement element )
    {
        ParameterChecker.checkNotNull( element, "element" );

        if ( isInside( element ) )
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
        if ( overlaps( x1, y1, x2, y2 ) )
        {
            // Check our own element
            if ( myElements != null )
            {
                for ( Iterator it = myElements.iterator(); it.hasNext(); )
                {
                    final QuadTreeElement element = (QuadTreeElement) it.next();

                    if ( isInsideCoordinates( element.getX(),
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


    public boolean isInside( final LocatedDoublePrecisionObject locatedObject )
    {
        return isInsideCoordinates( locatedObject.getX(), locatedObject.getY(), myX1, myY1, myX2, myY2 );
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


    public double getCenterX()
    {
        return ( myX1 + myX2 ) / 2;
    }


    public double getCenterY()
    {
        return ( myY1 + myY2 ) / 2;
    }


    public double getX1()
    {
        return myX1;
    }


    public double getY1()
    {
        return myY1;
    }


    public double getX2()
    {
        return myX2;
    }


    public double getY2()
    {
        return myY2;
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

    /**
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     *
     * @return true if the specified rectangle overlaps this quad tree node.
     */
    public boolean overlaps( final double x1, final double y1,
                             final double x2, final double y2 )
    {
        return x2 > myX1 &&
               x1 < myX2 &&
               y2 > myY1 &&
               y1 < myY2;
    }

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
        final double elementX = element.getX();
        final double elementY = element.getY();

        final double xc = getCenterX();
        final double yc = getCenterY();

        if ( isInsideCoordinates( elementX, elementY, myX1, myY1, xc, yc ) )
        {
            addElementToChildNode( 0, myX1, myY1, xc, yc, element );
        }
        else if ( isInsideCoordinates( elementX, elementY, myX1, yc, xc, myY2 ) )
        {
            addElementToChildNode( 1, myX1, yc, xc, myY2, element );
        }
        else if ( isInsideCoordinates( elementX, elementY, xc, myY1, myX2, yc ) )
        {
            addElementToChildNode( 2, xc, myY1, myX2, yc, element );
        }
        else if ( isInsideCoordinates( elementX, elementY, xc, yc, myX2, myY2 ) )
        {
            addElementToChildNode( 3, xc, yc, myX2, myY2, element );
        }
        else
        {
            // Should not happen:
            throw new IllegalStateException( "Element '" + element + "' is not inside any child node of '" + this +
                                             "', although it is inside it." );
        }
    }


    private void addElementToChildNode( final int index,
                                        final double x1,
                                        final double y1,
                                        final double x2,
                                        final double y2,
                                        final QuadTreeElement element )
    {
        if ( myChildren == null )
        {
            //noinspection unchecked
            myChildren = new QuadTreeNode[4];
        }

        if ( myChildren[ index ] == null )
        {
            myChildren[ index ] = new QuadTreeNodeImpl( myQuadTree, x1, y1, x2, y2 );
        }

        myChildren[ index ].addElement( element );
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


    private boolean isInsideCoordinates( final double x,
                                         final double y,
                                         final double x1,
                                         final double y1,
                                         final double x2,
                                         final double y2 )
    {
        return x >= x1 &&
               x < x2 &&
               y >= y1 &&
               y < y2;
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
        // Calculate parent node coordinates
        final double elementX = element.getX();
        final double elementY = element.getY();
        final double centerX = getCenterX();
        final double centerY = getCenterY();

        final double xSize = myX2 - myX1;
        final double ySize = myY2 - myY1;

        double x1 = myX1;
        double y1 = myY1;
        double x2 = myX2 + xSize;
        double y2 = myY2 + ySize;
        if ( elementX <= centerX )
        {
            x1 = myX1 - xSize;
            x2 = myX2;
        }
        if ( elementY <= centerY )
        {
            y1 = myY1 - ySize;
            y2 = myY2;
        }

        // Create a new parent, with the sibling nodes in the direction of where the element to be added is supposed to be.
        final QuadTreeNodeImpl parentNode = new QuadTreeNodeImpl( myQuadTree, x1, y1, x2, y2 );

        // Add this node as one of the children of the parent node
        final int childNodeIndex = calculateChildNodeIndex( elementX, centerX, elementY, centerY );

        //noinspection unchecked
        parentNode.myChildren = new QuadTreeNode[4];
        parentNode.myChildren[ childNodeIndex ] = this;

        // Notify model that we have a new root node
        myQuadTree.setRootNode( parentNode );
    }


    private int calculateChildNodeIndex( final double elementX,
                                         final double centerX,
                                         final double elementY,
                                         final double centerY )
    {
        int childNodeIndex = 0;
        if ( elementX <= centerX )
        {
            childNodeIndex = 1;
        }
        if ( elementY <= centerY )
        {
            childNodeIndex = 2;

            if ( elementX <= centerX )
            {
                childNodeIndex = 3;
            }
        }
        return childNodeIndex;
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

