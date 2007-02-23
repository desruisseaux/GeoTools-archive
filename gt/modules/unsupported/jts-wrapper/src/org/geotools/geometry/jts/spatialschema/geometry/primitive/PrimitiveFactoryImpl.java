/*******************************************************************************
 * $ * * $Id: PrimitiveFactoryImpl.java,v 1.14 2005/11/02 05:39:33 crossley Exp $ * *
 * $Source:
 * /cvs/ctree/LiteGO1/src/jar/com/polexis/lite/spatialschema/geometry/primitive/PrimitiveFactoryImpl.java,v $ * *
 * Copyright (C) 2003 Open GIS Consortium, Inc. All Rights Reserved.
 * http://www.opengis.org/Legal/ *
 ******************************************************************************/
package org.geotools.geometry.jts.spatialschema.geometry.primitive;

// J2SE direct dependencies
import org.geotools.geometry.jts.spatialschema.geometry.DirectPositionImpl;
import org.geotools.geometry.jts.spatialschema.geometry.geometry.GeometryFactoryImpl;

import java.util.List;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;
import org.opengis.spatialschema.geometry.MismatchedReferenceSystemException;
import org.opengis.spatialschema.geometry.geometry.GeometryFactory;
import org.opengis.spatialschema.geometry.geometry.Polygon;
import org.opengis.spatialschema.geometry.geometry.PolyhedralSurface;
import org.opengis.spatialschema.geometry.geometry.Position;
import org.opengis.spatialschema.geometry.primitive.Curve;
import org.opengis.spatialschema.geometry.primitive.Point;
import org.opengis.spatialschema.geometry.primitive.Primitive;
import org.opengis.spatialschema.geometry.primitive.PrimitiveFactory;
import org.opengis.spatialschema.geometry.primitive.Ring;
import org.opengis.spatialschema.geometry.primitive.Solid;
import org.opengis.spatialschema.geometry.primitive.SolidBoundary;
import org.opengis.spatialschema.geometry.primitive.Surface;
import org.opengis.spatialschema.geometry.primitive.SurfaceBoundary;
import org.opengis.spatialschema.geometry.primitive.SurfacePatch;

/**
 * Factory that knows how to create instances of the 19107 primitives as
 * implemented in LiteGO1.
 */
public class PrimitiveFactoryImpl implements PrimitiveFactory {

    //*************************************************************************
    //  Fields
    //*************************************************************************
    
    /**
     * a default CRS to use when creating primitives
     */
    private CoordinateReferenceSystem crs;

    private GeometryFactory geomFact;

    //*************************************************************************
    //  Constructors
    //*************************************************************************
    
    /**
     * DOCUMENT ME
     */
    public PrimitiveFactoryImpl() {
        this(null);
    }

    /**
     * DOCUMENT ME
     * 
     * @param crs
     */
    public PrimitiveFactoryImpl(final CoordinateReferenceSystem crs) {
        this.crs = crs;
        geomFact = new GeometryFactoryImpl(crs);
    }

    //*************************************************************************
    //  implement the PrimitiveFactory interface
    //*************************************************************************
    
    /**
     * Returns the coordinate reference system in use for all
     * {@linkPlain Primitive primitive}geometric objects to be created through
     * this interface.
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crs;
    }

    /**
     * Not implemented. Returns null.
     */
    public Primitive createPrimitive(final Envelope envelope) {
        return null;
    }

    /**
     * Create a direct position at the specified location specified by
     * coordinates. If the parameter is null, the position is left
     * uninitialized.
     * @param coordinates
     * @return
     */
    public DirectPosition createDirectPosition(final double[] coordinates) {
        if (coordinates != null) {
            return new DirectPositionImpl(crs, coordinates);
        } else {
            return new DirectPositionImpl(crs);
        }
    }

    /**
     * Creates a point at the specified location specified by coordinates.
     */
    public Point createPoint(final double[] coordinates) {
        return new PointImpl(createDirectPosition(coordinates), crs);
    }

    /**
     * Creates a point at the specified position.
     */
    public Point createPoint(final Position position) {
        return new PointImpl(position.getPosition(), crs);
    }

    /**
     * Takes a list of {@linkPlain CurveSegment curve segments}with the
     * appropriate end-to-start relationships and creates a
     * {@linkPlain Curve curve}. This may throw an IllegalArgumentException if
     * the List contains objects that are not instances of the CurveSegment
     * interface.
     */
    public Curve createCurve(final List/* <CurveSegment> */segments) {
        Curve result = new CurveImpl(crs);
        if (segments != null)
            result.getSegments().addAll(segments);
        return result;
    }

    /**
     * Creates a new Surface. This method can't possibly be used in the current
     * implementation since there are no implementations of the SurfacePatch
     * interface. Returns null.
     */
    public Surface createSurface(final List/* <SurfacePatch> */patches) {
        Surface result = new SurfaceImpl(crs);
        result.getPatches().addAll(patches);
        return result;
    }

    /**
     * @inheritDoc
     * @see org.opengis.spatialschema.geometry.primitive.PrimitiveFactory#createSurface(org.opengis.spatialschema.geometry.primitive.SurfaceBoundary)
     */
    public Surface createSurface(final SurfaceBoundary boundary) {
        // For now, our implementation has to assume that the boundary is a
        // polygon.
        Surface result = new SurfaceImpl(crs);
        Polygon poly = geomFact.createPolygon(boundary);
        // PENDING(jdc): the following line is 1.5 specific.
        // the result.getPatches() list is a generic list with a type of "? extends SurfacePatch"
        // we can compile without the generic if we cast down to List, but why do we need the cast?
        // Polygon extends SurfacePatch, so in theory this should work...
        //((List<SurfacePatch>) result.getPatches()).add(poly);
        ((List)result.getPatches()).add(poly);
        return result;
    }
    
    
    /**
     * @inheritDoc
     * @see org.opengis.spatialschema.geometry.primitive.PrimitiveFactory#createSurfaceBoundary(org.opengis.spatialschema.geometry.primitive.Ring, java.util.List)
     */
    public SurfaceBoundary createSurfaceBoundary(Ring exterior, List interiors)
            throws MismatchedReferenceSystemException, MismatchedDimensionException {
        return new SurfaceBoundaryImpl(crs, exterior, (Ring []) interiors.toArray(new Ring[interiors.size()]));
    }

    /**
     * Constructs a {@linkPlain Solid solid}by indicating its boundary as a
     * collection of {@linkPlain Shell shells}organized into a
     * {@linkPlain SolidBoundary solid boundary}. Since this specification is
     * limited to 3-dimensional coordinate reference systems, any solid is
     * definable by its boundary.
     * @param boundary
     * @return a {@code Solid} based on the given {@code boundary}
     */
    public Solid createSolid(final SolidBoundary boundary) {
        return null;
    }

    public Ring createRing(final List curves) {
        Ring result = new RingImpl(crs);
        if (curves != null)
            result.getGenerators().addAll(curves);
        return result;
    }

    /**
     * @inheritDoc
     * @see org.opengis.spatialschema.geometry.geometry.GeometryFactory#createPolyhedralSurface(java.util.List)
     */
    public PolyhedralSurface createPolyhedralSurface(List patches)
            throws MismatchedReferenceSystemException, MismatchedDimensionException {
        PolyhedralSurface result = new PolyhedralSurfaceImpl(crs);
        result.getPatches().addAll(patches);
        return result;
    }
}
