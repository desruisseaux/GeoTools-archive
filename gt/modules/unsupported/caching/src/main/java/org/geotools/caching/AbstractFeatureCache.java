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
package org.geotools.caching;

import java.io.IOException;
import java.util.Set;
import com.vividsolutions.jts.geom.Envelope;
import org.opengis.filter.Filter;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;


public abstract class AbstractFeatureCache implements FeatureCache {
    public void clear() {
        // TODO Auto-generated method stub
    }

    public FeatureCollection get(Envelope e) {
        // TODO Auto-generated method stub
        return null;
    }

    public FeatureCollection peek(Envelope e) {
        // TODO Auto-generated method stub
        return null;
    }

    public void put(FeatureCollection fc, Envelope e) {
        // TODO Auto-generated method stub
    }

    public void remove(Envelope e) {
        // TODO Auto-generated method stub
    }

    public Set addFeatures(FeatureCollection collection)
        throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public Transaction getTransaction() {
        // TODO Auto-generated method stub
        return null;
    }

    public void modifyFeatures(AttributeType[] type, Object[] value, Filter filter)
        throws IOException {
        // TODO Auto-generated method stub
    }

    public void modifyFeatures(AttributeType type, Object value, Filter filter)
        throws IOException {
        // TODO Auto-generated method stub
    }

    public void removeFeatures(Filter filter) throws IOException {
        // TODO Auto-generated method stub
    }

    public void setFeatures(FeatureReader reader) throws IOException {
        // TODO Auto-generated method stub
    }

    public void setTransaction(Transaction transaction) {
        // TODO Auto-generated method stub
    }

    public void addFeatureListener(FeatureListener listener) {
        // TODO Auto-generated method stub
    }

    public Envelope getBounds() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public Envelope getBounds(Query query) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public int getCount(Query query) throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    public DataStore getDataStore() {
        // TODO Auto-generated method stub
        return null;
    }

    public FeatureCollection getFeatures() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public FeatureCollection getFeatures(Query query) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public FeatureCollection getFeatures(Filter filter)
        throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public FeatureType getSchema() {
        // TODO Auto-generated method stub
        return null;
    }

    public void removeFeatureListener(FeatureListener listener) {
        // TODO Auto-generated method stub
    }
}
