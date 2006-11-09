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
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypes;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
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
public class ReprojectFeatureResults extends DataFeatureCollection implements FeatureResults {
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
        throws IOException, SchemaException, TransformException, OperationNotFoundException, NoSuchElementException, FactoryException{
        super(results.getSchema().getNamespace()+"/"+results.getSchema().getTypeName(),results.getSchema());
        if (destinationCS == null) {
            throw new NullPointerException("CoordinateSystem required");
        }

        FeatureType type = results.getSchema();
        CoordinateReferenceSystem originalCs = type.getDefaultGeometry()
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
        
        if (destinationCS.equals(originalCs)) {
            throw new IllegalArgumentException("CoordinateSystem "
                + destinationCS + " already used (check before using wrapper)");
        }

        this.schema = FeatureTypes.transform(type, destinationCS);
        this.results = results;
        
        this.transform = FactoryFinder.getCoordinateOperationFactory(null).createOperation(originalCs,destinationCS).getMathTransform();

        // Optimization 1: if the wrapped results is a forced cs results we
        // "eat" it to avoid useless feature object creation
        if (results instanceof ForceCoordinateSystemFeatureResults) {
            ForceCoordinateSystemFeatureResults forced = (ForceCoordinateSystemFeatureResults) results;
            this.results = forced.getOrigin();
        }

        // Optimization 2: if the wrapped results is a reproject results we
        // concatenate the transforms and get the original results
        if (results instanceof ReprojectFeatureResults) {
            ReprojectFeatureResults reproject = (ReprojectFeatureResults) results;
            this.results = reproject.getOrigin();
            this.transform = FactoryFinder.getMathTransformFactory(null).createConcatenatedTransform(reproject.transform,
                    transform);
        }
    }

    /**
     * @see org.geotools.data.FeatureResults#getSchema()
     */
    public FeatureType getSchema(){
        return schema;
    }

    /**
     * @see org.geotools.data.FeatureResults#reader()
     */
    public FeatureReader reader() throws IOException {
        return new ReprojectFeatureReader(results.reader(), schema, transform);
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
        try {
            Envelope newBBox = new Envelope();
            Envelope internal;
            Feature feature;

            for (FeatureReader r = reader(); r.hasNext();) {
                feature = r.next();
                internal = feature.getDefaultGeometry().getEnvelopeInternal();
                newBBox.expandToInclude(internal);
            }

            return newBBox;
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred while computing reprojected bounds",
                e);
        }
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
     * Returns the feature results wrapped by this reprojecting feature results
     *
     */
    public FeatureCollection getOrigin() {
        return results;
    }
}
