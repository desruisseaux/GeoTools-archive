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
package org.geotools.data.arcsde;

import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeShape;
import org.geotools.data.AttributeReader;
import org.geotools.data.DataSourceException;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Implements an attribute reader that is aware of the particulars of ArcSDE.
 * This class sends its logging to the log named "org.geotools.data".
 *
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 */
class ArcSDEAttributeReader implements AttributeReader {
    /** DOCUMENT ME!  */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.data");

    /** DOCUMENT ME!  */
    private ArcSDEQuery query;

    /** DOCUMENT ME!  */
    private FeatureType schema;

    /** DOCUMENT ME!  */
    private SeRow currentRow;

    /** DOCUMENT ME!  */
    private SeShape currentShape;

    /** DOCUMENT ME!  */
    private GeometryBuilder geometryBuilder;

    /** DOCUMENT ME!  */
    private int geometryTypeIndex = -1;

    /** DOCUMENT ME! */
    StringBuffer fidPrefix;

    /** DOCUMENT ME! */
    int fidPrefixLen;

    /** DOCUMENT ME!  */
    private boolean hasNextAlreadyCalled = false;

    /**
     * The query that defines this readers interaction with an ArcSDE instance.
     *
     * @param query
     *
     * @throws IOException DOCUMENT ME!
     */
    public ArcSDEAttributeReader(ArcSDEQuery query) throws IOException {
        this.query = query;
        this.schema = query.getSchema();

        this.fidPrefix = new StringBuffer(schema.getTypeName()).append(".");
        this.fidPrefixLen = fidPrefix.length();

        Class geometryClass = schema.getDefaultGeometry().getType();
        this.geometryBuilder = GeometryBuilder.builderFor(geometryClass);

        String geometryTypeName = schema.getDefaultGeometry().getName();
        AttributeType[] types = schema.getAttributeTypes();

        for (int i = 0; i < types.length; i++) {
            if (types[i].getName().equals(geometryTypeName)) {
                geometryTypeIndex = i;

                break;
            }
        }
    }

    /**
     *
     */
    public int getAttributeCount() {
        return schema.getAttributeCount();
    }

    /**
     *
     */
    public AttributeType getAttributeType(int index)
        throws ArrayIndexOutOfBoundsException {
        return schema.getAttributeType(index);
    }

    /**
     * Closes the associated query object.
     *
     * @throws IOException DOCUMENT ME!
     */
    public void close() throws IOException {
        query.close();
    }

    /**
     *
     */
    public boolean hasNext() throws IOException {
        if (!hasNextAlreadyCalled) {
            try {
                currentRow = query.fetch();
                hasNextAlreadyCalled = true;

                //ensure closing the query to release the connection, may be the
                //user is not so smart to doing it itself
                if (currentRow == null) {
                    query.close();
                } else {
                    currentShape = currentRow.getShape(geometryTypeIndex);
                }
            } catch (SeException ex) {
                query.close();
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                throw new DataSourceException("Fetching row:" + ex.getMessage(),
                    ex);
            }
        }

        return currentRow != null;
    }

    /**
     * Retrieves the next row, or throws a DataSourceException if not more rows
     * are available.
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     */
    public void next() throws IOException {
        hasNextAlreadyCalled = false;

        if (currentRow == null) {
            throw new DataSourceException("There are no more rows");
        }
    }

    /**
     *
     */
    public Object read(int index)
        throws IOException, ArrayIndexOutOfBoundsException {
        try {
            if (index == geometryTypeIndex) {
                return geometryBuilder.construct(currentShape);
            } else {
                return currentRow.getObject(index);
            }
        } catch (SeException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            query.close();
            throw new DataSourceException("Error retrieveing column " + index
                + ": " + ex.getMessage(), ex);
        }
    }

    /**
     *
     */
    public String readFID() throws IOException {
        fidPrefix.setLength(fidPrefixLen);

        try {
            fidPrefix.append(currentShape.getFeatureId().longValue());
        } catch (SeException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            query.close();
            throw new DataSourceException("Can't read FID value: "
                + ex.getMessage(), ex);
        }

        return fidPrefix.toString();
    }
}
