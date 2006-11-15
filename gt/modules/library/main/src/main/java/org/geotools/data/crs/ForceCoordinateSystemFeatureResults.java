/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data.crs;

import java.io.IOException;
import java.util.Iterator;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypes;
import org.geotools.feature.SchemaException;
import org.geotools.feature.collection.AbstractFeatureCollection;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;


/**
 * ForceCoordinateSystemFeatureResults provides a CoordinateReferenceSystem for
 * FeatureTypes.
 * 
 * <p>
 * ForceCoordinateSystemFeatureReader is a wrapper used to force
 * GeometryAttributes to a user supplied CoordinateReferenceSystem rather then
 * the default supplied by the DataStore.
 * </p>
 * 
 * <p>
 * Example Use:
 * <pre><code>
 * ForceCoordinateSystemFeatureResults results =
 *     new ForceCoordinateSystemFeatureResults( originalResults, forceCS );
 * 
 * CoordinateReferenceSystem originalCS =
 *     originalResults.getFeatureType().getDefaultGeometry().getCoordinateSystem();
 * 
 * CoordinateReferenceSystem newCS =
 *     reader.getFeatureType().getDefaultGeometry().getCoordinateSystem();
 * 
 * assertEquals( forceCS, newCS );
 * </code></pre>
 * </p>
 *
 * @author aaime
 * @source $URL$
 * @version $Id$
 */
public class ForceCoordinateSystemFeatureResults extends AbstractFeatureCollection {
    FeatureCollection results;
    //FeatureType schema;

    public ForceCoordinateSystemFeatureResults(FeatureCollection results,            
        CoordinateReferenceSystem forcedCS) throws IOException, SchemaException {
        super( forceType( origionalType( results ), forcedCS ) );
        
        this.results = results;

        if (results instanceof ForceCoordinateSystemFeatureResults) {
            // Optimization: if the source is again a ForceCoordinateSystemFeatureResults,
            // we just "eat" it since it does not do anything useful and creates unecessary
            // feature objects
            
            ForceCoordinateSystemFeatureResults forced = (ForceCoordinateSystemFeatureResults) results;
            this.results = forced.getOrigin();
        }
    }
    
    private static FeatureType origionalType( FeatureCollection results ){
        while( true ){
            if ( results instanceof ReprojectFeatureResults ) {
                results = ((ReprojectFeatureResults) results).getOrigin();
            }        
            if ( results instanceof ForceCoordinateSystemFeatureResults ) {
                results = ((ForceCoordinateSystemFeatureResults) results).getOrigin();
            }
            break;
        }
        return results.getSchema();
    }
    
    private static FeatureType forceType( FeatureType startingType, CoordinateReferenceSystem forcedCS ) throws SchemaException{
        if (forcedCS == null) {
            throw new NullPointerException("CoordinateSystem required");
        }
        CoordinateReferenceSystem originalCs = startingType.getDefaultGeometry().getCoordinateSystem();
        
        if (forcedCS.equals(originalCs)) {
            return startingType;
        }
        else {
            return FeatureTypes.transform(startingType, forcedCS);
        }
    }
   
    protected Iterator openIterator() {
        return new ForceCoordinateSystemIterator( results.features(), getSchema() );
    }
    protected void closeIterator( Iterator close ) {
        if( close == null ) return;
        if( close instanceof ForceCoordinateSystemIterator){
            ForceCoordinateSystemIterator iterator = (ForceCoordinateSystemIterator) close;
            iterator.close();
        }
    }

    /**
     * @see org.geotools.data.FeatureResults#getBounds()
     */
    public Envelope getBounds() {
        return results.getBounds();
    }

    public int size() {
        return results.size();
    }

    /**
     * @see org.geotools.data.FeatureResults#collection()
     */
//    public FeatureCollection collection() throws IOException {
//        FeatureCollection collection = FeatureCollections.newCollection();
//
//        try {
//            FeatureReader reader = reader();
//
//            while (reader.hasNext()) {
//                collection.add(reader.next());
//            }
//        } catch (NoSuchElementException e) {
//            throw new DataSourceException("This should not happen", e);
//        } catch (IllegalAttributeException e) {
//            throw new DataSourceException("This should not happen", e);
//        }
//
//        return collection;
//    }

    /**
     * Returns the feature results wrapped by this
     * ForceCoordinateSystemFeatureResults
     *
     */
    public FeatureCollection getOrigin() {
        return results;
    }
}
