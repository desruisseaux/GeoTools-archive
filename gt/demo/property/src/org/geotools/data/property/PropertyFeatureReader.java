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
package org.geotools.data.property;

import org.geotools.data.FeatureReader;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.logging.Logger;


/**
 * DOCUMENT ME!
 *
 * @author jgarnett
 * @source $URL$
 * @version $Id
 */
public class PropertyFeatureReader implements FeatureReader {
	private static final Logger LOGGER = Logger.getLogger("org.geotools.data.property");
    /** DOCUMENT ME! */
    PropertyAttributeReader reader;

    /**
     * Creates a new PropertyFeatureReader object.
     *
     * @param directory DOCUMENT ME!
     * @param typeName DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public PropertyFeatureReader(File directory, String typeName)
        throws IOException {
        File file = new File(directory, typeName + ".properties");
        reader = new PropertyAttributeReader(file);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public FeatureType getFeatureType() {
        return reader.type;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws IllegalAttributeException DOCUMENT ME!
     * @throws NoSuchElementException DOCUMENT ME!
     */
    public Feature next()
        throws IOException, IllegalAttributeException, NoSuchElementException {
        reader.next();

        FeatureType type = reader.type;
        String fid = reader.getFeatureID();
        Object[] values = new Object[reader.getAttributeCount()];

        for (int i = 0; i < reader.getAttributeCount(); i++) {
            try {
				values[i] = reader.read(i);
			} catch (RuntimeException e) {
				values[i] = null;
			} catch (IOException e) {
				throw e;
			}
        }

        return type.create(values, fid);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public boolean hasNext() throws IOException {
        return reader.hasNext();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void close() throws IOException {
        if (reader == null) {
            LOGGER.warning("Stream seems to be already closed.");
        }else{
        	reader.close();
        }

        reader = null;
    }
}
