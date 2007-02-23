/*$************************************************************************************************
 **
 ** $Id: PointImpl.java,v 1.13 2005/11/02 05:39:33 crossley Exp $
 **
 ** $Source: /cvs/ctree/LiteGO1/src/jar/com/polexis/lite/spatialschema/geometry/primitive/PointImpl.java,v $
 **
 ** Copyright (C) 2003 Open GIS Consortium, Inc. All Rights Reserved. http://www.opengis.org/Legal/
 **
 *************************************************************************************************/
package org.geotools.geometry.jts.spatialschema.geometry.primitive;

//J2SE dependencies
import java.util.Collections;
import java.util.Set;

// OpenGIS direct dependencies
import org.opengis.go.CommonFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.UnmodifiableGeometryException;
import org.opengis.spatialschema.geometry.geometry.Position;
import org.opengis.spatialschema.geometry.primitive.Bearing;
import org.opengis.spatialschema.geometry.primitive.OrientablePrimitive;
import org.opengis.spatialschema.geometry.primitive.Point;

//geotools dependencies
import org.geotools.factory.BasicFactories;

//import com.polexis.lite.referencing.operation.CoordinateOperationFactoryImpl;
import org.geotools.geometry.jts.spatialschema.geometry.DirectPositionImpl;
import org.geotools.geometry.jts.spatialschema.geometry.GeometryImpl;
import org.geotools.geometry.jts.JTSUtils;


/**
 * Basic data type for a geometric object consisting of one and only one point.
 * In most cases, the state of a {@code Point} is fully determined by its
 * position attribute. The only exception to this is if the {@code Point}
 * has been subclassed to provide additional non-geometric information such as
 * symbology.
 * 
 * @author SYS Technologies
 * @author crossley
 * @author cdillard
 * @version $Revision $
 */
public class PointImpl extends GeometryImpl implements Point {

    //*************************************************************************
    //  Members
    //*************************************************************************

    private DirectPosition position;

    //*************************************************************************
    //  Constructors
    //*************************************************************************

    /**
     * Creates a new {@code PointImpl}.
     */
    public PointImpl() {
        this(null, null);
    }

    /**
     * Creates a new {@code PointImpl}.
     * @param position
     */
    public PointImpl(final DirectPosition position) {
        this(position, position.getCoordinateReferenceSystem());
    }

    /**
     * Creates a new {@code PointImpl}.
     * @param position
     * @param crs
     */
    public PointImpl(final DirectPosition position, final CoordinateReferenceSystem crs) {
        super(crs);
        this.position = (position == null) ? new DirectPositionImpl(crs) : position;
    }

    //*************************************************************************
    //  Methods
    //*************************************************************************

    /**
     * Returns a copy of this point's position.  We must return a copy (and not
     * a reference to our internal object), otherwise the caller could modify
     * the values of the object and we would not know.
     */
    public DirectPosition getPosition() {
        return (DirectPosition) position.clone();
    }

    /**
     * Makes a copy of the given point and keeps that copy around.  If the given
     * point is not in the same coordinate reference system as this primitive,
     * then we attempt to convert it.
     */
    public void setPosition(final DirectPosition position) throws UnmodifiableGeometryException {
        if (isMutable()) {
            CoordinateReferenceSystem myCRS = getCoordinateReferenceSystem();
            CoordinateReferenceSystem pointCRS = position.getCoordinateReferenceSystem();
            DirectPosition copy = (DirectPosition) position.clone();
            if ((myCRS != null) && (pointCRS != null) && (!myCRS.equals(pointCRS))) {
                // Do the conversion.
                try {
                    CommonFactory commonFactory = BasicFactories.getDefault(); 
                    CoordinateOperationFactory cof = commonFactory.getCoordinateOperationFactory();
                    CoordinateOperation coordOp = cof.createOperation(pointCRS, myCRS);
                    MathTransform mt = coordOp.getMathTransform();
                    mt.transform(position, copy);
                }
                catch (OperationNotFoundException e) {
                    throw new RuntimeException("Unable to find an operation", e);
                }
                catch (FactoryException e) {
                    throw new RuntimeException("Factory exception", e);
                }
                catch (TransformException e) {
                    throw new RuntimeException("Error transforming", e);
                }
            }
            // Copy the position into our member.
            this.position = copy;
            // Let our cache know that something has changed so we can recompute.
            invalidateCachedJTSPeer();
        }
        else {
            throw new UnmodifiableGeometryException();
        }
    }

    /**
     * Not supported in this implementation.
     */
    public Bearing getBearing(final Position toPoint) {
        throw new UnsupportedOperationException("Bearing calculation is not supported");
    }

    /**
     * Computes the JTS equivalent of this geometry.
     */
    protected com.vividsolutions.jts.geom.Geometry computeJTSPeer() {
        return JTSUtils.directPositionToPoint(position);
    }

    public Set getContainedPrimitives() {
        return Collections.EMPTY_SET;
    }

    public Set getContainingPrimitives() {
        throw new UnsupportedOperationException();
    }

    public Set getComplexes() {
        throw new UnsupportedOperationException();
    }

    public OrientablePrimitive[] getProxy() {
        return null;
    }
}

