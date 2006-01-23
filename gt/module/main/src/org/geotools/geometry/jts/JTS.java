/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.geometry.jts;

// J2SE dependencies
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.awt.geom.IllegalPathStateException;

// JTS dependencies
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;

// OpenGIS dependencies
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.geometry.ShapeUtilities;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.operation.TransformPathNotFoundException;
import org.geotools.geometry.GeneralDirectPosition;


/**
 * JTS Geometry utility methods, bringing Geotools to JTS.
 * <p>
 * Offers geotools based services such as reprojection.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>transformation</li>
 *   <li>coordinate sequence editing</li>
 *   <li>common coordinate sequence implementations for specific uses</li>
 * </ul>
 *
 * @since 2.2
 * @source $URL$
 * @version $Id$
 * @author Jody Garnett
 * @author Martin Desruisseaux
 */
public final class JTS {
    /**
     * A pool of direct positions for use in {@link #orthodromicDistance}.
     */
    private static final GeneralDirectPosition[] POSITIONS = new GeneralDirectPosition[4];
    static {
        for (int i=0; i<POSITIONS.length; i++) {
            POSITIONS[i] = new GeneralDirectPosition(i);
        }
    }

    /**
     * Geodetic calculators already created for a given coordinate reference system.
     * For use in {@link #orthodromicDistance}.
     *
     * Note: We would like to use {@link java.util.WeakHashSet}, but we can't because
     *       {@link GeodeticCalculator} keep a reference to the CRS which is used as the key.
     */
    private static final Map/*<CoordinateReferenceSystem,GeodeticCalculator>*/ CALCULATORS =
            new HashMap();

    /**
     * Do not allow instantiation of this class.
     */
    private JTS() {
    }

    /**
     * Transforms the envelope using the specified math transform.
     *
     * @param  envelope  The envelope to transform.
     * @param  transform The transform to use.
     * @return The transformed Envelope
     * @throws TransformException if at least one coordinate can't be transformed.
     */
    public static Envelope transform(final Envelope envelope, final MathTransform transform)
            throws TransformException
    {
        return transform(envelope, null, transform, 5);
    }

    /**
     * Transforms the densified envelope using the specified math transform.
     * The envelope is densified (extra points put around the outside edge)
     * to provide a better new envelope for high deformed situations.
     * <p>
     * If an optional target envelope is provided, this envelope will be
     * {@linkplain Envelope#expandToInclude expanded} with the transformation result. It will
     * <strong>not</strong> be {@linkplain Envelope#setToNull nullified} before the expansion.
     *
     * @param  sourceEnvelope  The envelope to transform.
     * @param  targetEnvelope  An envelope to expand with the transformation result, or {@code null}
     *                         for returning an new envelope.
     * @param  transform       The transform to use.
     * @param  npoints         Densification of each side of the rectange.
     * @return {@code targetEnvelope} if it was non-null, or a new envelope otherwise.
     *         In all case, the returned envelope fully contains the transformed envelope.
     * @throws TransformException if a coordinate can't be transformed.
     */
    public static Envelope transform(final Envelope sourceEnvelope, Envelope targetEnvelope,
                                     final MathTransform transform, int npoints)
            throws TransformException 
    {
        if (transform.getSourceDimensions()!=2 || transform.getTargetDimensions()!=2) {
            throw new MismatchedDimensionException(Errors.format(ErrorKeys.BAD_TRANSFORM_$1,
                                                   Utilities.getShortClassName(transform)));
        }
        npoints++; // for the starting point.
        final double[] coordinates = new double[ (4*npoints)*2 ];
        final double xmin   = sourceEnvelope.getMinX();
        final double xmax   = sourceEnvelope.getMaxX();
        final double ymin   = sourceEnvelope.getMinY();
        final double ymax   = sourceEnvelope.getMaxY();
        final double scaleX = (xmax - xmin) / npoints;
        final double scaleY = (ymax - ymin) / npoints;

        int offset = 0;
        for (int t=0; t<npoints; t++) {
            final double dx = scaleX*t;
            final double dy = scaleY*t;
            coordinates[offset++] = xmin;       // Left side, increasing toward top.
            coordinates[offset++] = ymin + dy;
            coordinates[offset++] = xmin + dx;  // Top side, increasing toward right.
            coordinates[offset++] = ymax;
            coordinates[offset++] = xmax;       // Right side, increasing toward bottom.
            coordinates[offset++] = ymax - dy;
            coordinates[offset++] = xmax - dx;  // Bottom side, increasing toward left.
            coordinates[offset++] = ymin;
        }
        assert offset == coordinates.length;
        xform(transform, coordinates, coordinates);

        // Now find the min/max of the result
        if (targetEnvelope == null) {
            targetEnvelope = new Envelope();
        }
        for (int t=0; t<offset;) {
            targetEnvelope.expandToInclude(coordinates[t++], coordinates[t++]);
        }
        return targetEnvelope;
    }

    /**
     * Transforms the geometry using the default transformer.
     * 
     * @param  geom The geom to transform
     * @param  transform the transform to use during the transformation.
     * @return the transformed geometry.  It will be a new geometry.
     * @throws MismatchedDimensionException if the geometry doesn't have the expected dimension
     *         for the specified transform.
     * @throws TransformException if a point can't be transformed.
     */
    public static Geometry transform(final Geometry geom, final MathTransform transform)
            throws MismatchedDimensionException, TransformException
    {
        final GeometryCoordinateSequenceTransformer transformer = new GeometryCoordinateSequenceTransformer();
        transformer.setMathTransform(transform);
        return transformer.transform(geom);
    }

    /**
     * Transforms the coordinate using the provided math transform.
     *
     * @param source the source coordinate that will be transformed
     * @param dest the coordinate that will be set.  May be null or the source coordinate
     *        (or new coordinate of course). 
     * @return the destination coordinate if not null or a new Coordinate.
     * @throws TransformException if the coordinate can't be transformed.
     */     
    public static Coordinate transform(Coordinate source, Coordinate dest, MathTransform transform)
            throws TransformException
    {
        if (dest == null) {
            dest = new Coordinate();
        }
        final double[] array = new double[transform.getSourceDimensions()];
        copy(source, array);
        transform.transform(array, 0, array, 0, 1);
        switch (transform.getTargetDimensions()) {
            case 3: dest.z = array[2];  // Fall through
            case 2: dest.y = array[1];  // Fall through
            case 1: dest.x = array[0];  // Fall through
            case 0: break;
        }
        return dest;
    }

    /**
     * Transforms the envelope from its current crs to WGS84 coordinate reference system.
     * If the specified envelope is already in WGS84, then it is returned unchanged.
     *
     * @param envelope The envelope to transform.
     * @param crs The CRS the envelope is currently in.
     * @return The envelope transformed to be in WGS84 CRS.
     * @throws TransformException If at least one coordinate can't be transformed.
     */
    public static Envelope toGeographic(final Envelope envelope, final CoordinateReferenceSystem crs)
            throws TransformException
    {
        if (CRSUtilities.equalsIgnoreMetadata(crs, DefaultGeographicCRS.WGS84)) {
            return envelope;
        }
        final MathTransform transform;
        try {
            transform = CRS.transform(crs, DefaultGeographicCRS.WGS84, true);
        } catch (FactoryException exception) {
            throw new TransformPathNotFoundException(Errors.format(
                      ErrorKeys.CANT_TRANSFORM_ENVELOPE, exception));
        }
        return transform(envelope, transform);
    }

    /**
     * Like a transform but eXtreme!
     *
     * Transforms an array of coordinates using the provided math transform.
     * Each coordinate is transformed separately. In case of a transform exception then
     * the new value of the coordinate is the last coordinate correctly transformed.
     *
     * @param mt    The math transform to apply.
     * @param src   The source coordinates.
     * @param dest  The destination array for transformed coordinates.
     * @throws TransformException if this method failed to transform any of the points.
     */
    public static void xform(final MathTransform mt, final double[] src, final double[] dest)
            throws TransformException
    {
        final int sourceDim = mt.getSourceDimensions();
        final int targetDim = mt.getTargetDimensions();
        if (targetDim != sourceDim) {
            throw new MismatchedDimensionException();
        }
        TransformException firstError = null;
        boolean startPointTransformed = false;
        for (int i=0; i<src.length; i+=sourceDim) {
            try {
                mt.transform(src, i, dest, i, 1);
                if (!startPointTransformed) {
                    startPointTransformed = true;
                    for (int j=0; j<i; j++) {
                        System.arraycopy(dest, j, dest, i, targetDim);
                    }
                }
            } catch (TransformException e) {
                if (firstError == null) {
                    firstError = e;
                }
                if (startPointTransformed) {
                    System.arraycopy(dest, i-targetDim, dest, i, targetDim);
                }
            }
        }
        if (!startPointTransformed && firstError!=null) {
            throw firstError;
        }
    }

    /**
     * Computes the orthodromic distance between two points. This method:
     * <p>
     * <ol>
     *   <li>Transforms both points to geographic coordinates
     *       (<var>latitude</var>,<var>longitude</var>).</li>
     *   <li>Computes the orthodromic distance between the two points using ellipsoidal
     *       calculations.</li>
     * </ol>
     * <p>
     * The real work is performed by {@link GeodeticCalculator}. This convenience method simply
     * manages a pool of pre-defined geodetic calculators for the given coordinate reference system
     * in order to avoid repetitive object creation. If a large amount of orthodromic distances
     * need to be computed, direct use of {@link GeodeticCalculator} provides better performance
     * than this convenience method.
     * 
     * @param p1  First point
     * @param p2  Second point
     * @param crs Reference system the two points are in.
     * @return    Orthodromic distance between the two points, in meters.
     * @throws    TransformException if the coordinates can't be transformed from the specified
     *            CRS to a {@linkplain org.opengis.referencing.crs.GeographicCRS geographic CRS}.
     */
    public static synchronized double orthodromicDistance(final Coordinate p1, final Coordinate p2,
                                                          final CoordinateReferenceSystem crs)
            throws TransformException
    {
        /*
         * Need to synchronize because we use a single instance of a Map (CALCULATORS) as well as
         * shared instances of GeodeticCalculator and GeneralDirectPosition (POSITIONS). None of
         * them are thread-safe.
         */
        GeodeticCalculator gc = (GeodeticCalculator) CALCULATORS.get(crs);
        if (gc == null) {
            try {
                gc = new GeodeticCalculator(crs);
            } catch (FactoryException exception) {
                throw new TransformPathNotFoundException(exception);
            }
            CALCULATORS.put(crs, gc);
        }
        assert crs.equals(gc.getCoordinateReferenceSystem()) : crs;
        final GeneralDirectPosition pos = POSITIONS[Math.min(POSITIONS.length-1,
                                          crs.getCoordinateSystem().getDimension())];
        copy(p1, pos.ordinates);
        gc.setAnchorPosition(pos);
        copy(p2, pos.ordinates);
        gc.setDestinationPosition(pos);
        return gc.getOrthodromicDistance();
    }

    /**
     * Copies the ordinates values from the specified JTS coordinates to the specified array. The
     * destination array can have any length. Only the relevant field of the source coordinate will
     * be copied. If the array length is greater than 3, then all extra dimensions will be set to
     * {@link Double#NaN NaN}.
     *
     * @param point The source coordinate.
     * @param ordinates The destination array.
     */
    public static void copy(final Coordinate point, final double[] ordinates) {
        switch (ordinates.length) {
            default: Arrays.fill(ordinates, 3, ordinates.length, Double.NaN); // Fall through
            case  3: ordinates[2] = point.z; // Fall through
            case  2: ordinates[1] = point.y; // Fall through
            case  1: ordinates[0] = point.x; // Fall through
            case  0: break;
        }
    }

    /**
     * Converts an arbitrary Java2D shape into a JTS geometry. The created JTS geometry
     * may be any of {@link LineString}, {@link LinearRing} or {@link MultiLineString}.
     *
     * @param  shape    The Java2D shape to create.
     * @param  factory  The JTS factory to use for creating geometry.
     * @return The JTS geometry.
     */
    public static Geometry shapeToGeometry(final Shape shape, final GeometryFactory factory) {
        final PathIterator iterator = shape.getPathIterator(null, ShapeUtilities.getFlatness(shape));
        final double[] buffer = new double[6];
        final List     coords = new ArrayList();
        final List     lines  = new ArrayList();
        while (!iterator.isDone()) {
            switch (iterator.currentSegment(buffer)) {
                /*
                 * Close the polygon: the last point is equal to
                 * the first point, and a LinearRing is created.
                 */
                case PathIterator.SEG_CLOSE: {
                    if (!coords.isEmpty()) {
                        coords.add((Coordinate[]) coords.get(0));
                        lines.add(factory.createLinearRing((Coordinate[]) coords.toArray(
                                                        new Coordinate[coords.size()])));
                        coords.clear();
                    }
                    break;
                }
                /*
                 * General case: A LineString is created from previous
                 * points, and a new LineString begin for next points.
                 */
                case PathIterator.SEG_MOVETO: {
                    if (!coords.isEmpty()) {
                        lines.add(factory.createLineString((Coordinate[]) coords.toArray(
                                                        new Coordinate[coords.size()])));
                        coords.clear();
                    }
                    // Fall through
                }
                case PathIterator.SEG_LINETO: {
                    coords.add(new Coordinate(buffer[0], buffer[1]));
                    break;
                }
                default: {
                    throw new IllegalPathStateException();
                }
            }
            iterator.next();
        }
        /*
         * End of loops: create the last LineString if any, then create the MultiLineString.
         */
        if (!coords.isEmpty()) {
            lines.add(factory.createLineString((Coordinate[]) coords.toArray(
                                            new Coordinate[coords.size()])));
        }
        switch (lines.size()) {
            case 0: return null;
            case 1: return (LineString) lines.get(0);
            default: {
                return factory.createMultiLineString(GeometryFactory.toLineStringArray(lines));
            }
        }
    }
}
