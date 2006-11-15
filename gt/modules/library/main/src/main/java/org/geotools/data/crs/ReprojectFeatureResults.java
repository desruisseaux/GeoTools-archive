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
import java.util.NoSuchElementException;

import org.geotools.data.DataSourceException;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureResults;
import org.geotools.data.store.DataFeatureCollection;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypes;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.feature.collection.AbstractFeatureCollection;
import org.geotools.referencing.FactoryFinder;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Envelope;


/**
 * ReprojectFeatureReader provides a reprojection for FeatureTypes.
 * 
 * <p>
 * ReprojectFeatureResults  is a wrapper used to reproject  GeometryAttributes
 * to a user supplied CoordinateReferenceSystem from the original
 * CoordinateReferenceSystem supplied by the original FeatureResults.
 * </p>
 * 
 * <p>
 * Example Use:
 * <pre><code>
 * ReprojectFeatureResults results =
 *     new ReprojectFeatureResults( originalResults, reprojectCS );
 * 
 * CoordinateReferenceSystem originalCS =
 *     originalResults.getFeatureType().getDefaultGeometry().getCoordinateSystem();
 * 
 * CoordinateReferenceSystem newCS =
 *     results.getFeatureType().getDefaultGeometry().getCoordinateSystem();
 * 
 * assertEquals( reprojectCS, newCS );
 * </code></pre>
 * </p>
 *
 * @author aaime
 * @author $Author: jive $ (last modification)
 * @source $URL$
 * @version $Id$ TODO: handle the case where there is more than one geometry and the other geometries have a different CS than the default geometry
 */
public class ReprojectFeatureResults extends AbstractFeatureCollection {
    FeatureCollection results;
    FeatureType schema;
    MathTransform transform;

    /**
     * Creates a new reprojecting feature results
     *
     * @param results
     * @param destinationCS
     *
     * @throws IOException
     * @throws SchemaException
     * @throws TransformException 
     * @throws FactoryException 
     * @throws NoSuchElementException 
     * @throws OperationNotFoundException 
     * @throws CannotCreateTransformException
     * @throws NullPointerException DOCUMENT ME!
     * @throws IllegalArgumentException
     */
    public ReprojectFeatureResults(FeatureCollection results,
        CoordinateReferenceSystem destinationCS)
        throws IOException, SchemaException, TransformException, OperationNotFoundException, NoSuchElementException, FactoryException {
        
        super( forceType( origionalType( results ), destinationCS ) );
                        
        this.results = origionalCollection( results );        
        this.schema = getSchema();
        
        CoordinateReferenceSystem originalCs = results.getSchema().getDefaultGeometry().getCoordinateSystem();
        
        if (destinationCS.equals(originalCs)) {                       
            //this.transform = null; // identity?
            this.transform = FactoryFinder.getCoordinateOperationFactory(null).createOperation(originalCs,destinationCS).getMathTransform();
        }
        else {
            this.transform = FactoryFinder.getCoordinateOperationFactory(null).createOperation(originalCs,destinationCS).getMathTransform();            
        }
    }

    private static FeatureCollection origionalCollection( FeatureCollection results ){
        while( true ){
            if ( results instanceof ReprojectFeatureResults ) {
                results = ((ReprojectFeatureResults) results).getOrigin();
            }        
            if ( results instanceof ForceCoordinateSystemFeatureResults ) {
                results = ((ForceCoordinateSystemFeatureResults) results).getOrigin();
            }
            break;
        }
        return results;
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
    
    /**
     * @see org.geotools.data.FeatureResults#getSchema()
     */
    public FeatureType getSchema(){
        return schema;
    }
    
    protected Iterator openIterator() {
        return new ReprojectFeatureIterator( results.features(), getSchema(), this.transform );
    }
    
    protected void closeIterator( Iterator close ) {
        if( close == null ) return;
        if( close instanceof ReprojectFeatureIterator){
            ReprojectFeatureIterator iterator = (ReprojectFeatureIterator) close;
            iterator.close();
        }
    }

    /**
     * This method computes reprojected bounds the hard way, but computing them
     * feature by feature. This method could be faster if computed the
     * reprojected bounds by reprojecting the original feature bounds a Shape
     * object, thus getting the true shape of the reprojected envelope, and
     * then computing the minumum and maximum coordinates of that new shape.
     * The result would not a true representation of the new bounds, but it
     * would be guaranteed to be larger that the true representation.
     *
     * @see org.geotools.data.FeatureResults#getBounds()
     */
    public Envelope getBounds() {
        FeatureIterator r = features();
        try {            
            Envelope newBBox = new Envelope();
            Envelope internal;
            Feature feature;

            while ( r.hasNext()) {
                feature = r.next();
                internal = feature.getDefaultGeometry().getEnvelopeInternal();
                newBBox.expandToInclude(internal);
            }
            return newBBox;
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred while computing reprojected bounds",
                e);
        }
        finally {
            r.close();
        }
    }

    /**
     * @see org.geotools.data.FeatureResults#getCount()
     */
    public int size() {
        return results.size();
    }

    /**
     * @see org.geotools.data.FeatureResults#collection()
     *
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
    }*/

    /**
     * Returns the feature results wrapped by this reprojecting feature results
     *
     */
    public FeatureCollection getOrigin() {
        return results;
    }
}
