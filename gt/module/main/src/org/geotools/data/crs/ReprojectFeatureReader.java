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
/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003, Geotools Project Managment Committee (PMC)
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

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.ct.CannotCreateTransformException;
import org.geotools.ct.MathTransform;
import org.geotools.ct.MathTransform2D;
import org.geotools.data.DataSourceException;
import org.geotools.data.FeatureReader;
import org.geotools.data.crs.geometry.GeometryCoordinateSequenceTransformer;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import java.io.IOException;
import java.util.NoSuchElementException;


/**
 * ReprojectFeatureReader provides a reprojection for FeatureTypes.
 * 
 * <p>
 * ReprojectFeatureReader  is a wrapper used to reproject  GeometryAttributes
 * to a user supplied CoordinateReferenceSystem from the original
 * CoordinateReferenceSystem supplied by the original FeatureReader.
 * </p>
 * 
 * <p>
 * Example Use:
 * <pre><code>
 * ReprojectFeatureReader reader =
 *     new ReprojectFeatureReader( originalReader, reprojectCS );
 * 
 * CoordinateReferenceSystem originalCS =
 *     originalReader.getFeatureType().getDefaultGeometry().getCoordinateSystem();
 * 
 * CoordinateReferenceSystem newCS =
 *     reader.getFeatureType().getDefaultGeometry().getCoordinateSystem();
 * 
 * assertEquals( reprojectCS, newCS );
 * </code></pre>
 * </p>
 * TODO: handle the case where there is more than one geometry and the other
 * geometries have a different CS than the default geometry
 *
 * @author jgarnett, Refractions Research, Inc.
 * @author aaime
 * @author $Author: jive $ (last modification)
 * @version $Id: ReprojectFeatureReader.java,v 1.1 2003/12/19 01:08:58 jive Exp $
 */
public class ReprojectFeatureReader implements FeatureReader {
    FeatureReader reader;
    FeatureType schema;
    MathTransform transform;
    GeometryCoordinateSequenceTransformer transformer = new GeometryCoordinateSequenceTransformer();

    ReprojectFeatureReader(FeatureReader reader, FeatureType schema,
        MathTransform transform) {
        this.reader = reader;
        this.schema = schema;
        this.transform = transform;
        transformer.setMathTransform((MathTransform2D) transform);
    }

    public ReprojectFeatureReader(FeatureReader reader,
        CoordinateReferenceSystem cs)
        throws SchemaException, CannotCreateTransformException {
        if (cs == null) {
            throw new NullPointerException("CoordinateSystem required");
        }

        FeatureType type = reader.getFeatureType();
        CoordinateReferenceSystem original = type.getDefaultGeometry()
                                                 .getCoordinateSystem();

        if (cs.equals(original)) {
            throw new IllegalArgumentException("CoordinateSystem " + cs
                + " already used (check before using wrapper)");
        }

        this.transform = CRSService.reproject(original, cs, true);
        this.schema = CRSService.transform(type, cs);
        this.reader = reader;
        transformer.setMathTransform((MathTransform2D) transform);
    }

    /**
     * Implement getFeatureType.
     * 
     * <p>
     * Description ...
     * </p>
     *
     * @return
     *
     * @throws IllegalStateException DOCUMENT ME!
     *
     * @see org.geotools.data.FeatureReader#getFeatureType()
     */
    public FeatureType getFeatureType() {
        if (schema == null) {
            throw new IllegalStateException("Reader has already been closed");
        }

        return schema;
    }

    /**
     * Implement next.
     * 
     * <p>
     * Description ...
     * </p>
     *
     * @return
     *
     * @throws IOException
     * @throws IllegalAttributeException
     * @throws NoSuchElementException
     * @throws IllegalStateException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     *
     * @see org.geotools.data.FeatureReader#next()
     */
    public Feature next()
        throws IOException, IllegalAttributeException, NoSuchElementException {
        if (reader == null) {
            throw new IllegalStateException("Reader has already been closed");
        }

        Feature next = reader.next();
        Object[] attributes = next.getAttributes(null);

        try {
            for (int i = 0; i < attributes.length; i++) {
                if (attributes[i] instanceof Geometry) {
                    attributes[i] = transformer.transform((Geometry) attributes[i]);
                }
            }
        } catch (TransformException e) {
            throw new DataSourceException("A transformation exception occurred while reprojecting data on the fly",
                e);
        }

        return schema.create(attributes, next.getID());
    }

    /**
     * Implement hasNext.
     * 
     * <p>
     * Description ...
     * </p>
     *
     * @return
     *
     * @throws IOException
     * @throws IllegalStateException DOCUMENT ME!
     *
     * @see org.geotools.data.FeatureReader#hasNext()
     */
    public boolean hasNext() throws IOException {
        if (reader == null) {
            throw new IllegalStateException("Reader has already been closed");
        }

        return reader.hasNext();
    }

    /**
     * Implement close.
     * 
     * <p>
     * Description ...
     * </p>
     *
     * @throws IOException
     * @throws IllegalStateException DOCUMENT ME!
     *
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
