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
package org.geotools.data.vpf;

import java.io.File;
import java.io.IOException;

import java.util.NoSuchElementException;

import org.geotools.data.FeatureReader;
import org.geotools.data.vpf.readers.*;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;

/*
 * VPFFeatureReader.java
 *
 * Created on 13. april 2004, 14:35
 *
 * @author  <a href="mailto:knuterik@onemap.org">Knut-Erik Johnsen</a>, Project OneMap
 */
public class VPFFeatureReader implements FeatureReader {
    private VPFReader reader;
    private FeatureType type = null;
    private int attributeCount = 0;

    /** Creates a new instance of VPFFeatureReader */
    public VPFFeatureReader(String typeName, VPFDataBase dataBase, 
                            FeatureType type) {
        this.type = type;
        checkFeatureType(dataBase, typeName);
    }

    private void checkFeatureType(VPFDataBase dataBase, String typename) {
        try {
            System.out.println("Typename er: " + typename);

            VPFFeatureClass vpfclass = dataBase.getFeatureClass(typename);

            if (vpfclass != null) {
                reader = vpfclass.getReader();
                attributeCount = reader.getAttributeCount();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() throws IOException {
        reader.close();
    }

    public FeatureType getFeatureType() {
        return type;
    }

    public boolean hasNext() throws IOException {
        return reader.hasNext();
    }

    public Feature next() throws IOException, IllegalAttributeException, 
                                 NoSuchElementException {
        reader.next();

        String fid = reader.getFeatureID();
        Object[] values = new Object[attributeCount];

        for (int i = 0; i < attributeCount; i++) {
            values[i] = reader.read(i);
        }

        Feature f = null;

        try {
            f = type.create(values, fid);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return f;
    }
}