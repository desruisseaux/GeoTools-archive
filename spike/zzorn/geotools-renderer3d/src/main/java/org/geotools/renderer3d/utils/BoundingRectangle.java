package org.geotools.renderer3d.utils;

import org.geotools.renderer3d.utils.quadtree.LocatedDoublePrecisionObject;

/**
 * An axis aligned, rectangular region in 3D rendering space, aligned along the ground plane.
 *
 * @author Hans H�ggstr�m
 */
public interface BoundingRectangle
{

    /**
     * @return x coordinate for the corner with the lower x coordinate value.
     */
    double getX1();

    /**
     * @return y coordinate for the corner with the lower y coordinate value.
     */
    double getY1();

    /**
     * @return x coordinate for the corner with the higher x coordinate value.
     */
    double getX2();

    /**
     * @return y coordinate for the corner with the higher y coordinate value.
     */
    double getY2();

    /**
     * @return the x coordinate of the center of this bounding rectangle.
     */
    double getCenterX();

    /**
     * @return the y coordinate of the center of this bounding rectangle.
     */
    double getCenterY();

    /**
     * @return true if the specified located object is inside this QuadTreeNode area.
     */
    boolean isInside( LocatedDoublePrecisionObject locatedObject );

    /**
     * @return true if the specified coordinate is inside this bounding rectangle.
     */
    boolean isInside( double x, double y );

    /**
     * @return true if the bounding rectangle has no area because the corners lie on
     *         the same x or y directed line, or on the same point.  False if the area is greater than zero.
     */
    boolean isEmpty();

    /**
     * @param boundingRectangle the bounding rectangle to compare to
     *
     * @return true if the specified rectangle overlaps this rectangular region.
     */
    boolean overlaps( BoundingRectangle boundingRectangle );


    /**
     * @param x1 corner with lowest x and y coordinate
     * @param y1 corner with lowest x and y coordinate
     * @param x2 corner with highest x and y coordinate
     * @param y2 corner with highest x and y coordinate
     *
     * @return true if the specified rectangle overlaps this rectangular region.
     */
    boolean overlaps( double x1, double y1,
                      double x2, double y2 );

    /**
     * See javadoc for getSubquadrantAt(x,y)
     *
     * @param position the position to get the subquadrant at.
     *
     * @return the index of the subquadrant in y major order, or -1 if the point is outside this bounding rectangle.
     */
    int getSubquadrantAt( LocatedDoublePrecisionObject position );


    /**
     * A subquadrant is one of the spaces created by taking the center point and one of the four corner points as the
     * corner points of a bounding rectangle.
     * <p/>
     * The mapping from coordinates to index is in y major order:
     * <ul>
     * <li> x less than center and y less than center = 0
     * <li> x greater or equal to center and y less than center = 1
     * <li> x less than center and y greater or equal to center = 2
     * <li> x greater or equal to center and y greater or equal to center = 3
     * </ul>
     * <p/>
     * or graphically:
     * <pre>
     * +---+---+ x+
     * | 0 | 1 |
     * +---+---+
     * | 2 | 3 |
     * +---+---+
     * y+
     * </pre>
     *
     * @param x the coordinate to get the subquadrant index for.
     * @param y the coordinate to get the subquadrant index for.
     *
     * @return the index of the subquadrant at the specified position, or -1 if the position is outside this bounding rectangle.
     */
    int getSubquadrantAt( double x, double y );

    /**
     * Similar to getSubquadrantAt(x,y), but doesn't check if the point is inside the rectangle, instead it extends the
     * sectors out infinitely from the center of the bounding rectangle.
     *
     * @param position the position to get the subsector index for.
     *
     * @return the index of the subsector covering the specified position (range [0..3]).
     */
    int getSubsectorAt( LocatedDoublePrecisionObject position );

    /**
     * Similar to getSubquadrantAt(x,y), but doesn't check if the point is inside the rectangle, instead it extends the
     * sectors out infinitely from the center of the bounding rectangle.
     *
     * @param x the coordinate to get the subsector index for.
     * @param y the coordinate to get the subsector index for.
     *
     * @return the index of the subsector covering the specified position (range [0..3]).
     */
    int getSubsectorAt( double x, double y );

    /**
     * See getSubquadrantAt method javadoc for an explanation on subquadrants.
     *
     * @param subquadrantIndex the subquadrant to create a bounding rectangle for.  Should be in the range 0 to 3.
     *
     * @return a new bounding rectangle for the specified subquadrant of this bounding rectangle.
     */
    BoundingRectangle createSubquadrantBoundingRectangle( int subquadrantIndex );

    /**
     * @param subsector the subsector to extend the parent bounding rectangle towards.
     *
     * @return a parent bounding rectangle twice the size of this bounding rectangle,
     *         and containing this bounding rectangle as one of its subquadrants.
     */
    BoundingRectangle createParentBoundingRectangle( int subsector );

    /**
     * @return the subquadrant index for the subquadrant that is opposite to the specified subquadrant.
     */
    int getOppositeSubquadrant( int subquadrant );
}
