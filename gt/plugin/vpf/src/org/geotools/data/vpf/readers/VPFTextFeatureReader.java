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

import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;


/**
 *
 * @author  <a href="mailto:knuterik@onemap.org">Knut-Erik Johnsen</a>, Project OneMap
 * @deprecated
 */
public class VPFTextFeatureReader extends VPFReader {
    /** Creates a new instance of VPFTextFeatureReader */
    public VPFTextFeatureReader(File directory, String typename, 
                                java.util.HashMap tiles) {
        super(directory, typename, tiles);
    }

    public void close() throws IOException {
    }

    public int getAttributeCount() {
        return 0;
    }

    public AttributeType getAttributeType(int param)
                                   throws ArrayIndexOutOfBoundsException {
        return null;
    }

    public String getFeatureID() {
        return "";
    }

    public boolean hasNext() throws IOException {
        return false;
    }

    public void next() throws IOException {
    }

    public Object read(int param) throws IOException, 
                                         ArrayIndexOutOfBoundsException {
        return null;
    }

    public void initReader(File directory, String typename, FeatureType type, 
                           java.util.HashMap tiles) {
    }
}