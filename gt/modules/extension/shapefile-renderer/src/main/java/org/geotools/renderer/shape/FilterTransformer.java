/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.renderer.shape;

import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.visitor.DuplicatingFilterVisitor;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.operation.matrix.Matrix2;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.spatial.BBOX;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Transforms all GeometryExpressions with the provided transform.
 * <p>
 * extraData may be a filterfactory2
 * </p>
 * @author Jesse
 */
public class FilterTransformer extends DuplicatingFilterVisitor {

    MathTransform mt;
    CoordinateReferenceSystem fromCRS;
    CoordinateReferenceSystem toCRS;
    
    public FilterTransformer(final MathTransform mt) {
        this.mt = mt;
    }
    
    /**
     * Alternate constructor, takes the source CRS, the destination CRS, and an affine transform to 
     * be concatenated to the geographic transfromation. This contructor allows for accurate envelope
     * transformations when the data set contains extreme points such as the poles or the Greenwitch
     * antimeridian.
     * @see ReferencedEnvelope#transform(CoordinateReferenceSystem, boolean)
     * @param fromCRS
     * @param toCRS
     * @param affineTransform
     * @throws FactoryException
     */
    public FilterTransformer(final CoordinateReferenceSystem fromCRS, final CoordinateReferenceSystem toCRS, final MathTransform affineTransform) throws FactoryException {
        this.fromCRS = fromCRS;
        this.toCRS = toCRS;
        
        try {
            mt = CRS.findMathTransform(fromCRS, toCRS);
        } catch (Exception e) {
            mt = null;
        }

         if (mt == null) {
            mt = affineTransform;
        } else {
            mt = ReferencingFactoryFinder.getMathTransformFactory(null).createConcatenatedTransform(
                    mt, affineTransform);
        }
         
        // if everything else failed, use the identity transform 
        if(mt == null)
            mt = ReferencingFactoryFinder.getMathTransformFactory(null).createAffineTransform(
                    new Matrix2(1,0,0,1));
    }
    
    public Object visit(BBOX filter, Object extraData) {
		String propertyName=filter.getPropertyName();
		double [] coords= new double[4];
		coords[0]=filter.getMinX();
		coords[1]=filter.getMinY();
		coords[2]=filter.getMaxX();
		coords[3]=filter.getMaxY();
		String srs=filter.getSRS();
		
		double[] dest=new double[4];
		try {
			mt.transform(coords, 0, dest, 0, 2);
		} catch (TransformException e) {
			throw new RuntimeException(e);
		}
		
		return getFactory(extraData).bbox(propertyName, dest[0], dest[1], dest[2], dest[3], srs);
		
    }
    
    public Object visit(Literal expression, Object extraData) {
        Object value = expression.getValue();
        try {
            if( value instanceof com.vividsolutions.jts.geom.Geometry ){
                return getFactory(extraData).literal(JTS.transform((com.vividsolutions.jts.geom.Geometry)value, mt));
            }
            if( value instanceof Envelope ){
                ReferencedEnvelope start = new ReferencedEnvelope((Envelope)value, fromCRS);
                return getFactory(extraData).literal(start.transform(toCRS, true));
            }
        } catch (MismatchedDimensionException e) {
            throw new RuntimeException(e);
        } catch (IllegalFilterException e) {
            throw new RuntimeException(e);
        } catch (TransformException e) {
            throw new RuntimeException(e);
        } catch (FactoryException e) {
            throw new RuntimeException(e);
        }
        return super.visit(expression, extraData);
    }
}
