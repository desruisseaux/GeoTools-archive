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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureTypes;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


/**
 * ForceCoordinateSystemFeatureReader provides a CoordinateReferenceSystem for
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
 * ForceCoordinateSystemFeatureReader reader =
 *     new ForceCoordinateSystemFeatureReader( originalReader, forceCS );
 * 
 * CoordinateReferenceSystem originalCS =
 *     originalReader.getFeatureType().getDefaultGeometry().getCoordianteSystem();
 * 
 * CoordinateReferenceSystem newCS =
 *     reader.getFeatureType().getDefaultGeometry().getCoordianteSystem();
 * 
 * assertEquals( forceCS, newCS );
 * </code></pre>
 * </p>
 *
 * @author jgarnett, Refractions Research, Inc.
 * @author aaime
 * @author $Author: jive $ (last modification)
 * @source $URL$
 * @version $Id$
 */
public class ForceCoordinateSystemIterator implements Iterator {
    protected FeatureIterator<SimpleFeature> reader;
    protected SimpleFeatureType schema;

    /**
     * Shortcut constructor that can be used if the new schema has already been computed
     * @param reader
     * @param schema
     */
    ForceCoordinateSystemIterator(FeatureIterator<SimpleFeature> reader, SimpleFeatureType schema) {
        this.reader = reader;
        this.schema = schema;
    }

    /**
     * Builds a new ForceCoordinateSystemFeatureReader
     *
     * @param reader
     * @param cs
     *
     * @throws SchemaException
     * @throws NullPointerException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public ForceCoordinateSystemIterator(FeatureIterator<SimpleFeature> reader, SimpleFeatureType type,
        CoordinateReferenceSystem cs) throws SchemaException {
        if (cs == null) {
            throw new NullPointerException("CoordinateSystem required");
        }        
        CoordinateReferenceSystem originalCs = type.getDefaultGeometry()
                                                   .getCRS();

        if (cs.equals(originalCs)) {
            schema = FeatureTypes.transform(type, cs);
        }

        this.reader = reader;
    }

    /**
     * @see org.geotools.data.FeatureReader#getFeatureType()
     */
    public SimpleFeatureType getFeatureType() {
        if (reader == null || schema == null ) {
            throw new IllegalStateException("Reader has already been closed");
        }        
        return schema;
    }

    /**
     * @see org.geotools.data.FeatureReader#next()
     */
    public Object next()
        throws NoSuchElementException {
        if (reader == null) {
            throw new IllegalStateException("Reader has already been closed");
        }

        SimpleFeature next = reader.next();
        if( schema==null )
            return next;
        
        try {
            return SimpleFeatureBuilder.copy(next);
        }
        catch( IllegalAttributeException eep){
            throw (IllegalStateException) new IllegalStateException(eep.getMessage()).initCause(eep );
        }
    }

    /**
     * @see org.geotools.data.FeatureReader#hasNext()
     */
    public boolean hasNext() {
        if (reader == null) {
            throw new IllegalStateException("Reader has already been closed");
        }

        return reader.hasNext();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
    /**
     * @see org.geotools.data.FeatureReader#close()
     */
    public void close() {
        if (reader == null) {
            return;
        }
        reader.close();
        reader = null;
        schema = null;
    }
}
