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
package org.geotools.data.crs;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.geotools.data.FeatureReader;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.referencing.FactoryFinder;
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
 * @version $Id: ForceCoordinateSystemFeatureReader.java,v 1.1 2003/12/19 01:05:08 jive Exp $
 */
public class ForceCoordinateSystemFeatureReader implements FeatureReader {
    protected FeatureReader reader;
    protected FeatureType schema;

    /**
     * Shortcut constructor that can be used if the new schema has already been computed
     * @param reader
     * @param schema
     */
    ForceCoordinateSystemFeatureReader(FeatureReader reader, FeatureType schema) {
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
    public ForceCoordinateSystemFeatureReader(FeatureReader reader,
        CoordinateReferenceSystem cs) throws SchemaException {
        if (cs == null) {
            throw new NullPointerException("CoordinateSystem required");
        }

        FeatureType type = reader.getFeatureType();
        CoordinateReferenceSystem originalCs = type.getDefaultGeometry()
                                                   .getCoordinateSystem();

        if (cs.equals(originalCs)) {
            throw new IllegalArgumentException("CoordinateSystem " + cs
                + " already used (check before using wrapper)");
        }

        schema = FactoryFinder.transform(type, cs);
        this.reader = reader;
    }

    /**
     * @see org.geotools.data.FeatureReader#getFeatureType()
     */
    public FeatureType getFeatureType() {
        if (schema == null) {
            throw new IllegalStateException("Reader has already been closed");
        }

        return schema;
    }

    /**
     * @see org.geotools.data.FeatureReader#next()
     */
    public Feature next()
        throws IOException, IllegalAttributeException, NoSuchElementException {
        if (reader == null) {
            throw new IllegalStateException("Reader has already been closed");
        }

        Feature next = reader.next();

        return schema.create(next.getAttributes(null), next.getID());
    }

    /**
     * @see org.geotools.data.FeatureReader#hasNext()
     */
    public boolean hasNext() throws IOException {
        if (reader == null) {
            throw new IllegalStateException("Reader has already been closed");
        }

        return reader.hasNext();
    }

    /**
     * @see org.geotools.data.FeatureReader#close()
     */
    public void close() throws IOException {
        if (reader == null) {
            throw new IllegalStateException("Reader has already been closed");
        }

        reader.close();
        reader = null;
        schema = null;
    }
}
