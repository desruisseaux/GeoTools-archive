/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004, Geotools Project Managment Committee (PMC)
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


package org.geotools.data.vpf.readers;

import java.io.File;
import java.io.IOException;

import java.util.HashMap;

import org.geotools.data.AttributeReader;
import org.geotools.data.vpf.VPFSchemaCreator;
import org.geotools.data.vpf.io.TableInputStream;
import org.geotools.data.vpf.io.TableRow;
import org.geotools.data.vpf.util.PrimitiveDataFactory;

import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;

/*
 * VPFReader.java
 *
 * Created on 13. april 2004, 14:55
 *
 * @author  <a href="mailto:knuterik@onemap.org">Knut-Erik Johnsen</a>, Project OneMap
 */
public abstract class VPFReader extends PrimitiveDataFactory
    implements AttributeReader {
    protected Object[] currentData = null;
    protected HashMap tiles = null;
    protected FeatureType type = null;
    protected String typename = null;
    protected TableInputStream tableInput = null;
    protected TableRow tableRow = null;
    protected File directory = null;

    public VPFReader(File directory, String typename, HashMap tiles) {
        try {
            this.tiles = tiles;
            this.typename = typename;
            this.directory = directory;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //public abstract void initReader( File directory, String typename, HashMap tiles  );
    public abstract String getFeatureID();

    public void initReader() {
        try {
            this.type = VPFSchemaCreator.getSchema(typename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getAttributeCount() {
        return type.getAttributeCount();
    }

    public AttributeType getAttributeType(int index)
                                   throws ArrayIndexOutOfBoundsException {
        return type.getAttributeType(index);
    }

    public boolean hasNext() throws IOException {
        if (tableRow != null) {
            return true;
        }

        tableRow = (TableRow) tableInput.readRow();

        return tableRow != null;
    }

    public void close() throws IOException {
        if (tableInput != null) {
            tableInput.close();
            tableInput = null;
        }

        tableInput = null;
        tableRow = null;
    }
}