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
package org.geotools.data;

import org.geotools.data.AbstractDataStore;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureType;
import org.geotools.filter.Filter;
import java.io.IOException;


/**
 * <p>
 * This class assumes the DataStore represents a single source,  represented by
 * a URL. In many cases the default functionality  is chained off to the
 * parent class (AbstractDataStore).
 * </p>
 *
 * @author dzwiers
 *
 * @see AbstractDataStore
 */
public abstract class AbstractFileDataStore extends AbstractDataStore {
    /**
     * Singular version, returns the FeatureType for the url being read.
     *
     * @see org.geotools.data.DataStore#getSchema(java.lang.String)
     */
    public abstract FeatureType getSchema() throws IOException;

    /**
     * Singular version, which must be implemented to represent a Reader  for
     * the url being read.
     *
     * @see org.geotools.data.DataStore#getFeatureReader(java.lang.String)
     */
    protected abstract FeatureReader getFeatureReader()
        throws IOException;

    /**
     * Singular version, calls parent with getSchema().getTypeName()
     *
     * @see org.geotools.data.DataStore#updateSchema(java.lang.String,
     *      org.geotools.feature.FeatureType)
     */
    public void updateSchema(FeatureType featureType) throws IOException {
        updateSchema(getSchema().getTypeName(), featureType);
    }

    /**
     * Singular version, calls parent with getSchema().getTypeName()
     *
     * @see org.geotools.data.DataStore#getFeatureSource(java.lang.String)
     */
    public FeatureSource getFeatureSource() throws IOException {
        return getFeatureSource(getSchema().getTypeName());
    }

    /**
     * Singular version, calls parent with getSchema().getTypeName()
     *
     * @see org.geotools.data.DataStore#getFeatureWriter(java.lang.String,
     *      org.geotools.filter.Filter, org.geotools.data.Transaction)
     */
    public FeatureWriter getFeatureWriter(Filter filter, Transaction transaction)
        throws IOException {
        return getFeatureWriter(getSchema().getTypeName(), filter, transaction);
    }

    /**
     * @see org.geotools.data.DataStore#getFeatureWriter(java.lang.String,
     *      org.geotools.data.Transaction)
     */
    public FeatureWriter getFeatureWriter(Transaction transaction)
        throws IOException {
        return getFeatureWriter(getSchema().getTypeName(), transaction);
    }

    /**
     * @see org.geotools.data.DataStore#getFeatureWriterAppend(java.lang.String,
     *      org.geotools.data.Transaction)
     */
    public FeatureWriter getFeatureWriterAppend(Transaction transaction)
        throws IOException {
        return getFeatureWriterAppend(getSchema().getTypeName(), transaction);
    }
}
