/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data.store;

import java.io.IOException;
import java.util.Set;

import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureStore;
import org.geotools.feature.FeatureCollection;

public abstract class ContentFeatureStore extends ContentFeatureSource implements FeatureStore {
    public ContentFeatureStore(ContentEntry entry) {
        super(entry);
    }

    public Set addFeatures(FeatureCollection collection)
        throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public void modifyFeatures(AttributeDescriptor[] type, Object[] value, Filter filter)
        throws IOException {
        // TODO Auto-generated method stub
    }

    public void modifyFeatures(AttributeDescriptor type, Object value, Filter filter)
        throws IOException {
        // TODO Auto-generated method stub
    }

    public void removeFeatures(Filter filter) throws IOException {
        // TODO Auto-generated method stub
    }

    public void setFeatures(FeatureReader reader) throws IOException {
        // TODO Auto-generated method stub
    }
}
