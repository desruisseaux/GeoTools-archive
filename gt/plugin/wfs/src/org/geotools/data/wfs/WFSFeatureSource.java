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
/*
 * Created on 28-Sep-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wfs;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.data.AbstractFeatureSource;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureResults;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.Filter;
import java.io.IOException;


/**
 * DOCUMENT ME!
 *
 * @author dzwiers TODO To change the template for this generated type comment
 *         go to Window - Preferences - Java - Code Style - Code Templates
 */
public class WFSFeatureSource extends AbstractFeatureSource {
    protected WFSDataStore ds;
    protected FeatureType ft;

    protected WFSFeatureSource(WFSDataStore ds, FeatureType ft) {
        this.ds = ds;
        this.ft = ft;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.FeatureSource#getDataStore()
     */
    public DataStore getDataStore() {
        return ds;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.FeatureSource#addFeatureListener(org.geotools.data.FeatureListener)
     */
    public void addFeatureListener(FeatureListener listener) {
        ds.listenerManager.addFeatureListener(this, listener);
    }

    /* (non-Javadoc)
     * @see org.geotools.data.FeatureSource#removeFeatureListener(org.geotools.data.FeatureListener)
     */
    public void removeFeatureListener(FeatureListener listener) {
        ds.listenerManager.removeFeatureListener(this, listener);
    }

    /* (non-Javadoc)
     * @see org.geotools.data.FeatureSource#getSchema()
     */
    public FeatureType getSchema() {
        return ft;
    }

    public Envelope getBounds() throws IOException {
        return getBounds((ft == null) ? Query.ALL
                                      : new DefaultQuery(ft.getTypeName()));
    }

    /* (non-Javadoc)
     * @see org.geotools.data.FeatureSource#getBounds(org.geotools.data.Query)
     */
    public Envelope getBounds(Query query) throws IOException {
        return ds.getBounds(namedQuery(query));
    }

    /* (non-Javadoc)
     * @see org.geotools.data.FeatureSource#getFeatures()
     */
    public FeatureResults getFeatures() throws IOException {
        return getFeatures(new DefaultQuery(ft.getTypeName(), Filter.NONE));
    }

    /* (non-Javadoc)
     * @see org.geotools.data.FeatureSource#getFeatures(org.geotools.filter.Filter)
     */
    public FeatureResults getFeatures(Filter filter) throws IOException {
        return getFeatures(new DefaultQuery(ft.getTypeName(), filter));
    }

    /* (non-Javadoc)
     * @see org.geotools.data.FeatureSource#getFeatures(org.geotools.data.Query)
     */
    public FeatureResults getFeatures(Query query) {
        return new WFSFeatureResults(this, query);
    }

    /* (non-Javadoc)
     * @see org.geotools.data.AbstractFeatureSource#getTransaction()
     */
    public Transaction getTransaction() {
        return Transaction.AUTO_COMMIT;
    }

    public static class WFSFeatureResults implements FeatureResults {
        private WFSFeatureSource fs;
        private Query query;

        private WFSFeatureResults() {
        }

        public WFSFeatureResults(WFSFeatureSource fs, Query query) {
            this.query = query;
            this.fs = fs;
        }

        /* (non-Javadoc)
         * @see org.geotools.data.FeatureResults#getSchema()
         */
        public FeatureType getSchema() throws IOException {
            return fs.ft;
        }

        /* (non-Javadoc)
         * @see org.geotools.data.FeatureResults#reader()
         */
        public FeatureReader reader() throws IOException {
            return fs.ds.getFeatureReader(query, fs.getTransaction());
        }

        /* (non-Javadoc)
         * @see org.geotools.data.FeatureResults#getBounds()
         */
        public Envelope getBounds() throws IOException {
            return fs.getBounds(query);
        }

        /* (non-Javadoc)
         * @see org.geotools.data.FeatureResults#getCount()
         */
        public int getCount() throws IOException {
            return fs.getCount(query);
        }

        /* (non-Javadoc)
         * @see org.geotools.data.FeatureResults#collection()
         */
        public FeatureCollection collection() throws IOException {
            try {
                FeatureCollection collection = FeatureCollections.newCollection();
                FeatureReader reader = fs.ds.getFeatureReader(query,
                        fs.getTransaction());

                while (reader.hasNext()) {
                    collection.add(reader.next());
                }

                reader.close();

                return collection;
            } catch (IllegalAttributeException e) {
                throw new DataSourceException("Could not read feature ", e);
            }
        }
    }
}
