/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.data;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.net.URI;

import org.geotools.catalog.AbstractMetadataEntity;
import org.geotools.cs.AxisInfo;
import org.geotools.cs.CoordinateSystemFactory;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.cs.HorizontalDatum;
import org.geotools.ct.CoordinateTransformation;
import org.geotools.ct.CoordinateTransformationFactory;
import org.geotools.ct.MathTransform;
import org.geotools.feature.FeatureType;
import org.geotools.pt.CoordinatePoint;
import org.geotools.units.Unit;
import org.geotools.util.InternationalString;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystem;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Metadata derived from a FeatureSource.
 * <p>
 * The description is in very simple terms:
 * <ul>
 * <li>displayName: often title case of typeName
 * <li>typeName: TypeName used to retrive resource</li>
 * <li>description: initially "Feature type typeName"</li> 
 * <li>bounds: extent in lat/long is available</li>
 * <li>count: number of features</li>
 * </ul>
 * </p>
 * <p>
 * Bounds & Count are make use of lazy calculation and are cached,
 * FeatureSource event notification is used to clear the cache.
 * </p>
 * <p>
 * A more mature solution would provide set a set methods for bounds,
 * allowing client code to specify the extent. And only make use
 * of extent calculation as required.
 * </p>
 * @author Jody Garnett, Refractions Research
 */
public class FeatureTypeMetadataEnity extends AbstractMetadataEntity {
    DataStore store;
    URI namespace;
    String typeName;
    InternationalString displayName;
    InternationalString description;
    
    FeatureSource source;
    int count;
    Envelope bounds;
    
    public FeatureTypeMetadataEnity( DataStore store, URI namespace, String typeName ){
        this.store = store;
        this.typeName = typeName;
        this.displayName = new InternationalString( typeName.substring(0,1).toUpperCase() + typeName.substring(1).toLowerCase() );
        this.description = new InternationalString( "Feature type "+typeName );
        try {
            source = store.getFeatureSource( typeName );
            source.addFeatureListener( new FeatureListener(){
                public void changed(FeatureEvent featureEvent) {
                    reset();
                }                
            });            
        } catch (IOException e) {            
        }        
    }    
    /** Called to reset the count and bounds, they will be recalculated as needed */     
    public synchronized void reset(){
        count = -1;
        bounds = null;
    }
    public InternationalString getDisplayName() {
        return displayName;
    }
    public InternationalString getDescription() {
        return description;
    }
    public URI getNamespace(){
        return namespace;
    }
    public String getTypeName() {
        return typeName;
    }
    /**
     * Bounding box for associated Feature Collection, will be calcualted as needed.
     * <p>
     * Note bounding box is returned in lat/long - the coordinate system of the default geometry
     * is used to provide this reprojection.
     * </p>
     */
    public synchronized Envelope getBounds() {        
        if( bounds != null ) return bounds;
        
        try {
            bounds = source.getBounds();
            if( bounds == null ){
                bounds = source.getFeatures().getBounds();
            }
            bounds = reBound( bounds );            
        } catch (Exception e) {
            bounds = new Envelope();
        }        
        return bounds;
    }
    /** Reproject provided bound evelope to lat/long */
    private Envelope reBound( Envelope env ) throws Exception {
        FeatureType schema = source.getSchema();
        CoordinateReferenceSystem crs = schema.getDefaultGeometry().getCoordinateSystem();
        CoordinateSystem cs = crs.getCoordinateSystem();
        String wkt = cs.toWKT();
        CoordinateSystemFactory csFactory = CoordinateSystemFactory.getDefault();
		org.geotools.cs.CoordinateSystem cs2 = csFactory.createFromWKT( wkt );
		Unit       angularUnit = Unit.DEGREE;
		HorizontalDatum  datum = HorizontalDatum.WGS84;
		org.geotools.cs.PrimeMeridian meridian = org.geotools.cs.PrimeMeridian.GREENWICH;
		GeographicCoordinateSystem geographic =
		    csFactory.createGeographicCoordinateSystem("geographic", angularUnit, datum, meridian, AxisInfo.LONGITUDE, AxisInfo.LATITUDE );
		CoordinateTransformationFactory trFactory = CoordinateTransformationFactory.getDefault();
		CoordinateTransformation transformation = trFactory.createFromCoordinateSystems(cs2, geographic );
		MathTransform transform = transformation.getMathTransform();
		CoordinatePoint p1 = new CoordinatePoint( env.getMinX(), env.getMinY());
		CoordinatePoint p2 = new CoordinatePoint( env.getMaxX(), env.getMaxY());
		transform.transform( p1, p1 );
		transform.transform( p2, p2 );
		Envelope rebounds = new Envelope();
		Point2D point = p1.toPoint2D();
		rebounds.expandToInclude( point.getX(), point.getY() );
		point = p2.toPoint2D();
		rebounds.expandToInclude( point.getX(), point.getY() );		
		return rebounds;
    }
    
    /** Number of features in associated Feature Collection, will be calcualted as needed */
    public synchronized int getCount() {
        if( count != -1 ) return count;
        try {
            count = source.getCount( Query.ALL );
            if( count == -1 ){
                count = source.getFeatures().getCount();
            }
        } catch (IOException e) {
            bounds = new Envelope();
        }
        return count;
    }
}