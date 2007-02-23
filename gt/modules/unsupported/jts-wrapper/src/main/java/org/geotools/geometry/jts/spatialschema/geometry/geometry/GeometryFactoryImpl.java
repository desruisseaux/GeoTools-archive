/*$************************************************************************************************
 **
 ** $Id: GeometryFactoryImpl.java,v 1.14 2005/11/02 05:39:33 crossley Exp $
 **
 ** $Source: /cvs/ctree/LiteGO1/src/jar/com/polexis/lite/spatialschema/geometry/geometry/GeometryFactoryImpl.java,v $
 **
 ** Copyright (C) 2003 Open GIS Consortium, Inc. All Rights Reserved. http://www.opengis.org/Legal/
 **
 *************************************************************************************************/
package org.geotools.geometry.jts.spatialschema.geometry.geometry;

// J2SE direct dependencies
import org.geotools.geometry.jts.spatialschema.geometry.DirectPositionImpl;
import org.geotools.geometry.jts.spatialschema.geometry.EnvelopeImpl;
import org.geotools.geometry.jts.spatialschema.geometry.primitive.PolyhedralSurfaceImpl;
import org.geotools.geometry.jts.spatialschema.geometry.primitive.SurfaceBoundaryImpl;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;
import org.opengis.spatialschema.geometry.MismatchedReferenceSystemException;
import org.opengis.spatialschema.geometry.aggregate.MultiPrimitive;
import org.opengis.spatialschema.geometry.geometry.Arc;
import org.opengis.spatialschema.geometry.geometry.ArcByBulge;
import org.opengis.spatialschema.geometry.geometry.ArcString;
import org.opengis.spatialschema.geometry.geometry.ArcStringByBulge;
import org.opengis.spatialschema.geometry.geometry.BSplineCurve;
import org.opengis.spatialschema.geometry.geometry.BSplineSurface;
import org.opengis.spatialschema.geometry.geometry.Geodesic;
import org.opengis.spatialschema.geometry.geometry.GeodesicString;
import org.opengis.spatialschema.geometry.geometry.GeometryFactory;
import org.opengis.spatialschema.geometry.geometry.KnotType;
import org.opengis.spatialschema.geometry.geometry.LineSegment;
import org.opengis.spatialschema.geometry.geometry.LineString;
import org.opengis.spatialschema.geometry.geometry.PointArray;
import org.opengis.spatialschema.geometry.geometry.Polygon;
import org.opengis.spatialschema.geometry.geometry.PolyhedralSurface;
import org.opengis.spatialschema.geometry.geometry.Position;
import org.opengis.spatialschema.geometry.geometry.Tin;
import org.opengis.spatialschema.geometry.primitive.Ring;
import org.opengis.spatialschema.geometry.primitive.Surface;
import org.opengis.spatialschema.geometry.primitive.SurfaceBoundary;


/**
 * The {@code GeometryFactoryImpl} class/interface...
 * 
 * @author SYS Technologies
 * @author crossley
 * @version $Revision $
 */
public class GeometryFactoryImpl implements GeometryFactory {
    
    //*************************************************************************
    //  Fields
    //*************************************************************************
    
    /**
     * Comment for {@code crs}.
     */
    private CoordinateReferenceSystem crs;

    //*************************************************************************
    //  Constructors
    //*************************************************************************
    
    /**
     * Creates a new {@code GeometryFactoryImpl}.
     * @param crs
     */
    public GeometryFactoryImpl(final CoordinateReferenceSystem crs) {
        this.crs = crs;
    }

    //*************************************************************************
    //  implement the GeometryFactory interface
    //*************************************************************************
    /**
     * @inheritDoc
     * @see org.opengis.spatialschema.geometry.geometry.GeometryFactory#getCoordinateReferenceSystem()
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crs;
    }
    

    public Position createPosition( DirectPosition point ) {
        return new DirectPositionImpl( point );
    }
    public DirectPosition createDirectPosition() {
        return new DirectPositionImpl(crs);
    }
    
    /**
     * @inheritDoc
     * @see org.opengis.spatialschema.geometry.geometry.GeometryFactory#createDirectPosition(double[])
     */
    public DirectPosition createDirectPosition(final double[] coordinates) {
        return new DirectPositionImpl(crs, coordinates);
    }
    
    
    /**
     * @inheritDoc
     * @see org.opengis.spatialschema.geometry.geometry.GeometryFactory#createEnvelope(org.opengis.spatialschema.geometry.DirectPosition, org.opengis.spatialschema.geometry.DirectPosition)
     */
    public Envelope createEnvelope(
            final DirectPosition lowerCorner, 
            final DirectPosition upperCorner) {
        return new EnvelopeImpl(lowerCorner, upperCorner);
    }
    /**
     * @inheritDoc
     * @see org.opengis.spatialschema.geometry.geometry.GeometryFactory#createLineSegment(org.opengis.spatialschema.geometry.geometry.Position, org.opengis.spatialschema.geometry.geometry.Position)
     */
    public LineSegment createLineSegment(final Position startPoint, final Position endPoint) {
        return null;
    }

    /**
     * @inheritDoc
     * @see org.opengis.spatialschema.geometry.geometry.GeometryFactory#createLineString(java.util.List)
     */
    public LineString createLineString(final List/*<Position>*/ points) {
        LineString result = new LineStringImpl();
        PointArray pa = result.getControlPoints();
        List list = pa.positions();
        Iterator it = points.iterator();
        while (it.hasNext()) {
//            Object o = it.next();
//            if (o instanceof DirectPosition) {
//                list.add(o);
//            } else if (o instanceof Position) {
//                Position p = (Position) o;
//                DirectPosition dp = p.getPosition();
//                /*if (dp == null) {
//                    dp = p.getIndirect().getPosition();
//                }*/
//                list.add(dp);
//            }
            Position position = (Position) it.next();
            DirectPosition directPosition = position.getPosition();
            list.add(directPosition);
        }
        return result;
    }

    /**
     * @inheritDoc
     * @see org.opengis.spatialschema.geometry.geometry.GeometryFactory#createGeodesic(org.opengis.spatialschema.geometry.geometry.Position, org.opengis.spatialschema.geometry.geometry.Position)
     */
    public Geodesic createGeodesic(final Position startPoint, final Position endPoint) {
        return null;
    }

    /**
     * @inheritDoc
     * @see org.opengis.spatialschema.geometry.geometry.GeometryFactory#createGeodesicString(java.util.List)
     */
    public GeodesicString createGeodesicString(final List/*<Position>*/ points) {
        return null;
    }

    /**
     * @inheritDoc
     * @see org.opengis.spatialschema.geometry.geometry.GeometryFactory#createArc(org.opengis.spatialschema.geometry.geometry.Position, org.opengis.spatialschema.geometry.geometry.Position, org.opengis.spatialschema.geometry.geometry.Position)
     */
    public Arc createArc(
            final Position startPoint, 
            final Position midPoint, 
            final Position endPoint) {
        return null;
    }

    /**
     * @inheritDoc
     * @see org.opengis.spatialschema.geometry.geometry.GeometryFactory#createArc(org.opengis.spatialschema.geometry.geometry.Position, org.opengis.spatialschema.geometry.geometry.Position, double, double[])
     */
    public Arc createArc(
            final Position startPoint, 
            final Position endPoint, 
            final double bulge, 
            final double[] normal) {
        return null;
    }

    /**
     * @inheritDoc
     * @see org.opengis.spatialschema.geometry.geometry.GeometryFactory#createArcString(java.util.List)
     */
    public ArcString createArcString(final List/*<Position>*/ points) {
        return null;
    }

    /**
     * @inheritDoc
     * @see org.opengis.spatialschema.geometry.geometry.GeometryFactory#createArcByBulge(org.opengis.spatialschema.geometry.geometry.Position, org.opengis.spatialschema.geometry.geometry.Position, double, double[])
     */
    public ArcByBulge createArcByBulge(
            final Position startPoint, 
            final Position endPoint, 
            final double bulge, 
            final double[] normal) {
        return null;
    }

    /**
     * @inheritDoc
     * @see org.opengis.spatialschema.geometry.geometry.GeometryFactory#createArcStringByBulge(java.util.List, double[], java.util.List)
     */
    public ArcStringByBulge createArcStringByBulge(
            final List/*<Position>*/ points, 
            final double[] bulges,
            final List/*<double[]>*/ normals) {
        return null;
    }
    
    
    /**
     * @inheritDoc
     * @see org.opengis.spatialschema.geometry.geometry.GeometryFactory#createBSplineCurve(int, org.opengis.spatialschema.geometry.geometry.PointArray, java.util.List, org.opengis.spatialschema.geometry.geometry.KnotType)
     */
    public BSplineCurve createBSplineCurve(int arg0, PointArray arg1, List arg2, KnotType arg3)
            throws MismatchedReferenceSystemException, MismatchedDimensionException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @inheritDoc
     * @see org.opengis.spatialschema.geometry.geometry.GeometryFactory#createPolygon(org.opengis.spatialschema.geometry.primitive.SurfaceBoundary)
     */
    public Polygon createPolygon(SurfaceBoundary boundary) throws MismatchedReferenceSystemException,
            MismatchedDimensionException {
        PolygonImpl result = new PolygonImpl(boundary);
        return result;
    }
    
    
    /**
     * @inheritDoc
     * @see org.opengis.spatialschema.geometry.geometry.GeometryFactory#createPolygon(org.opengis.spatialschema.geometry.primitive.SurfaceBoundary, org.opengis.spatialschema.geometry.primitive.Surface)
     */
    public Polygon createPolygon(SurfaceBoundary boundary, Surface spanningSurface)
            throws MismatchedReferenceSystemException, MismatchedDimensionException {
        PolygonImpl result = new PolygonImpl(boundary, Collections.singletonList(spanningSurface));
        return result;
    }

    /**
     * @inheritDoc
     * @see org.opengis.spatialschema.geometry.geometry.GeometryFactory#createTin(java.util.Set, java.util.Set, java.util.Set, double)
     */
    public Tin createTin(Set arg0, Set arg1, Set arg2, double arg3)
            throws MismatchedReferenceSystemException, MismatchedDimensionException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param exterior
     * @param interiors
     * @return
     * @throws MismatchedReferenceSystemException
     * @see org.opengis.spatialschema.geometry.geometry.GeometryFactory#createSurfaceBoundary(org.opengis.spatialschema.geometry.primitive.Ring, java.util.List)
     */
    public SurfaceBoundary createSurfaceBoundary(Ring exterior, List interiors) throws MismatchedReferenceSystemException {
        return new SurfaceBoundaryImpl(crs, exterior, (Ring []) interiors.toArray(new Ring[interiors.size()]));
    }
    
    
    /**
     * @inheritDoc
     * @see org.opengis.spatialschema.geometry.geometry.GeometryFactory#createMultiPrimitive()
     */
    public MultiPrimitive createMultiPrimitive() {
        throw new UnsupportedOperationException(
	    "GeoAPI 2.0 does not correctly support creation of MultiPrimitive subclasses.");
    }
    
    
    /**
     * @inheritDoc
     * @see org.opengis.spatialschema.geometry.geometry.GeometryFactory#createPolyhedralSurface(java.util.List)
     */
    public PolyhedralSurface createPolyhedralSurface(final List/*<Polygon>*/ polygons)
            throws MismatchedReferenceSystemException, MismatchedDimensionException {
        PolyhedralSurface result = new PolyhedralSurfaceImpl(crs);
        result.getPatches().addAll(polygons);
        return result;
    }

    public BSplineSurface createBSplineSurface( List arg0, int[] arg1, List[] arg2, KnotType arg3 ) throws MismatchedReferenceSystemException, MismatchedDimensionException {
        throw new UnsupportedOperationException(
            "This is the JTS Wrapper Factory which only supports implementations that align with the Simple Feature for SQL Specification.");
    }

}
