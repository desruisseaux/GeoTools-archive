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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.geotools.catalog.AbstractMetadataEntity;
import org.geotools.feature.FeatureType;
import org.geotools.util.InternationalString;
import org.opengis.catalog.MetadataEntity;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

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
public class FeatureSourceMetadataEnity extends AbstractMetadataEntity {
    DataStore store;
    String typeName;
    InternationalString displayName;
    InternationalString description;
    
    FeatureSource source;
    int count;
    Envelope bounds;
    
    public FeatureSourceMetadataEnity( DataStore store, String typeName ){
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
        FeatureType schema = source.getSchema();
        CoordinateReferenceSystem cs = schema.getDefaultGeometry().getCoordinateSystem();
        if ( cs == null ){
            // Cannot convert to lat/long
            bounds = new Envelope();
        }
        try {
            bounds = source.getBounds();
            if( bounds == null ){
                bounds = source.getFeatures().getBounds();
            }
            // reproject me :-)
        } catch (IOException e) {
            bounds = new Envelope();
        }        
        return bounds;
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