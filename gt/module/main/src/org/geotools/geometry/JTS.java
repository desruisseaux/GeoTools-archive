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
package org.geotools.geometry;

// OpenGIS dependencies
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

// Geotools dependencies
import org.geotools.geometry.jts.GeometryCoordinateSequenceTransformer;
import org.geotools.geometry.jts.PreciseCoordinateSequenceTransformer;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.crs.GeographicCRS;

// JTS dependencies
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import java.util.NoSuchElementException;


/**
 * JTS Geometry utility methods, bringing geotools to JTS.
 * <p>
 * Offers geotools based services such as reprojection.
 * </p><p>
 * Responsibilities:
 * <ul>
 * <li>transformation
 * <li>coordinate sequence editing
 * <li>common coordinate sequence implementations for specific uses
 * </ul>
 * </p>
 * @author jgarnett
 * @since 0.6.0
 */
public class JTS {

    /**
     * Transforms the Envelope using the MathTransform.
     * @param envelope the envelope to transform
     * @param transform the transformation to use
     * @return a new Envelope
     * @throws TransformException 
     */
    public static Envelope transform(Envelope envelope, MathTransform transform) throws TransformException {
        double[] coords=new double[]{envelope.getMinX(), envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY()};
        double[] newcoords=new double[4];
        transform.transform(coords, 0, newcoords, 0, 2);
        return new Envelope(newcoords[0],newcoords[2],newcoords[1],newcoords[3]);
    }
    
    public static GeometryCoordinateSequenceTransformer createGeometryTransformer(){
    	return new GeometryCoordinateSequenceTransformer();
    }
    
    public static GeometryCoordinateSequenceTransformer createPreciseGeometryTransformer(){
    	return new GeometryCoordinateSequenceTransformer(new PreciseCoordinateSequenceTransformer());
    }
   
    public static Geometry transform( Geometry geom, MathTransform transform ) throws MismatchedDimensionException, TransformException{
    	GeometryCoordinateSequenceTransformer transformer=createGeometryTransformer();
    	transformer.setMathTransform(transform);
		return transformer.transform(geom);
	}
    
	public static Geometry preciseTransform( Geometry geom, MathTransform transform ) throws MismatchedDimensionException, TransformException{
    	GeometryCoordinateSequenceTransformer transformer=createPreciseGeometryTransformer();
    	transformer.setMathTransform(transform);
		return transformer.transform(geom);	    
	}
    
    /**
     * Transforms the envelope from its current crs to WGS84 coordinate system.
     *
     * @param env The envelope to transform.
     * @param crs The CRS the envelope is currently in.
     * @return The envelope transformed to be in WGS84 CRS.
     * @throws OperationNotFoundException
     * @throws NoSuchElementException
     * @throws FactoryException
     * @throws TransformException
     *
     * @todo We may catch some exceptions and rethrown them in a simplier one.
     *       After all, this is a convenience method (not an "official" one).
     */
    public static Envelope toGeographic(Envelope env, CoordinateReferenceSystem crs)
            throws OperationNotFoundException, NoSuchElementException, FactoryException, TransformException
    {
    	if (crs.equals(GeographicCRS.WGS84)) {
            return env;
        }
        MathTransform transform = FactoryFinder.getCoordinateOperationFactory()
                .createOperation(crs, GeographicCRS.WGS84).getMathTransform();
        return JTS.transform(env, transform);
    }
}
