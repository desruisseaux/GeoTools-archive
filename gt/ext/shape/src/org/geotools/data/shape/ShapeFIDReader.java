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
package org.geotools.data.shape;

import java.io.IOException;

import org.geotools.data.FIDReader;
import org.geotools.feature.FeatureType;

/**
 * @author Tommaso Nolli
 */
public class ShapeFIDReader implements FIDReader {
    protected static final String CLOSE_MESG = "Close has already been called"
        + " on this FIDReader";

    private boolean opened;
    private ShapefileDataStore.Reader reader;
    private int len;
    protected StringBuffer buffer;

    

    public ShapeFIDReader(String typeName, ShapefileDataStore.Reader reader) {
        buffer = new StringBuffer(typeName);
        buffer.append('.');
        len = typeName.length() + 1;
        this.opened = true;
        this.reader = reader;
    }

    public ShapeFIDReader(FeatureType featureType, 
                          ShapefileDataStore.Reader reader) {
        this(featureType.getTypeName(), reader);
    }

    /**
     * Release any resources associated with this reader
     */
    public void close() {
        this.opened = false;
    }

    /**
     * This method always returns true, since it is
     * built with a <code>ShapefileDataStore.Reader</code>
     * you have to call <code>ShapefileDataStore.Reader.hasNext()</code>
     *
     * @return always return <code>true</code>
     *
     * @throws IOException If closed
     */
    public boolean hasNext() throws IOException {
        if (!this.opened) {
            throw new IOException(CLOSE_MESG);
        }

        /* In DefaultFIDReader this is always called after
         * atttributesReader.hasNext so, as we use the same
         * attributeReader, we'll return true
         */
        return true;
    }

    /**
     * Read the feature id.
     *
     * @return the Feature Id
     *
     * @throws IOException If closed
     */
    public String next() throws IOException {
        if (!this.opened) {
            throw new IOException(CLOSE_MESG);
        }

        buffer.delete(len,buffer.length());
        buffer.append(reader.getRecordNumber());

        return buffer.toString();
    }
}
