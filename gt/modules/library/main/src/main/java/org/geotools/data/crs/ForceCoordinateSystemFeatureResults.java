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
import java.util.NoSuchElementException;

import org.geotools.data.DataSourceException;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureResults;
import org.geotools.data.store.DataFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypes;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
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
public class ForceCoordinateSystemFeatureResults extends DataFeatureCollection {
    FeatureCollection results;
    FeatureType schema;
    private FeatureType startingType;

    public ForceCoordinateSystemFeatureResults(FeatureCollection results,
        CoordinateReferenceSystem forcedCS) throws IOException, SchemaException {
        if (forcedCS == null) {
            throw new NullPointerException("CoordinateSystem required");
        }

        startingType = results.getSchema();
        CoordinateReferenceSystem originalCs = startingType.getDefaultGeometry()
                                                   .getCoordinateSystem();

        //make sure we have the real original 
        if ( results instanceof ReprojectFeatureResults ) {
        	originalCs = ((ReprojectFeatureResults) results).getOrigin().getSchema()
        		.getDefaultGeometry().getCoordinateSystem();
        }
        
        if ( results instanceof ForceCoordinateSystemFeatureResults ) {
        	originalCs = ((ForceCoordinateSystemFeatureResults) results).getOrigin().getSchema()
    			.getDefaultGeometry().getCoordinateSystem();
        }
        
        if (!forcedCS.equals(originalCs)) {
            this.schema = FeatureTypes.transform(startingType, forcedCS);
        }

        this.results = results;

        // Optimization: if the source is again a ForceCoordinateSystemFeatureResults,
        // we just "eat" it since it does not do anything useful and creates unecessary
        // feature objects
        if (results instanceof ForceCoordinateSystemFeatureResults) {
            ForceCoordinateSystemFeatureResults forced = (ForceCoordinateSystemFeatureResults) results;
            this.results = forced.getOrigin();
        }
    }

   
    public FeatureType getSchema(){
        if( schema==null )
            return startingType;
        return schema;
    }

    /**
     * @see org.geotools.data.FeatureResults#reader()
     */
    public FeatureReader reader() throws IOException {
        return new ForceCoordinateSystemFeatureReader(results.reader(), schema);
    }

    /**
     * @see org.geotools.data.FeatureResults#getBounds()
     */
    public Envelope getBounds() {
        return results.getBounds();
    }

    /**
     * @see org.geotools.data.FeatureResults#getCount()
     */
    public int getCount() throws IOException {
        return results.size();
    }

    /**
     * @see org.geotools.data.FeatureResults#collection()
     */
    public FeatureCollection collection() throws IOException {
        FeatureCollection collection = FeatureCollections.newCollection();

        try {
            FeatureReader reader = reader();

            while (reader.hasNext()) {
                collection.add(reader.next());
            }
        } catch (NoSuchElementException e) {
            throw new DataSourceException("This should not happen", e);
        } catch (IllegalAttributeException e) {
            throw new DataSourceException("This should not happen", e);
        }

        return collection;
    }

    /**
     * Returns the feature results wrapped by this
     * ForceCoordinateSystemFeatureResults
     *
     */
    public FeatureCollection getOrigin() {
        return results;
    }
}
