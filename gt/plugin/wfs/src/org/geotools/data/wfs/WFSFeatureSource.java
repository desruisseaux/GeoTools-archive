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
package org.geotools.data.wfs;

import java.io.IOException;

import org.geotools.data.AbstractFeatureSource;
import org.geotools.data.DataStore;
import org.geotools.data.DefaultFeatureResults;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureResults;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.filter.Filter;

import com.vividsolutions.jts.geom.Envelope;


/**
 * DOCUMENT ME!
 *
 * @author dzwiers 
 */
public class WFSFeatureSource extends AbstractFeatureSource {
    protected WFSDataStore ds;
    protected FeatureType ft;

    protected WFSFeatureSource(WFSDataStore ds, FeatureType ft) {
        this.ds = ds;
        this.ft = ft;
    }

    /**
     * 
     * @see org.geotools.data.FeatureSource#getDataStore()
     */
    public DataStore getDataStore() {
        return ds;
    }

    /**
     * 
     * @see org.geotools.data.FeatureSource#addFeatureListener(org.geotools.data.FeatureListener)
     */
    public void addFeatureListener(FeatureListener listener) {
        ds.listenerManager.addFeatureListener(this, listener);
    }

    /**
     * 
     * @see org.geotools.data.FeatureSource#removeFeatureListener(org.geotools.data.FeatureListener)
     */
    public void removeFeatureListener(FeatureListener listener) {
        ds.listenerManager.removeFeatureListener(this, listener);
    }

    /**
     * 
     * @see org.geotools.data.FeatureSource#getSchema()
     */
    public FeatureType getSchema() {
        return ft;
    }

    /**
     * 
     * @see org.geotools.data.FeatureSource#getBounds()
     */
    public Envelope getBounds() throws IOException {
        return getBounds((ft == null) ? Query.ALL
                                      : new DefaultQuery(ft.getTypeName()));
    }

    /**
     * 
     * @see org.geotools.data.FeatureSource#getBounds(org.geotools.data.Query)
     */
    public Envelope getBounds(Query query) throws IOException {
        return ds.getBounds(namedQuery(query));
    }

    /**
     * 
     * @see org.geotools.data.FeatureSource#getFeatures()
     */
    public FeatureCollection getFeatures(){
        return getFeatures(new DefaultQuery(ft.getTypeName(), Filter.NONE));
    }

    /**
     * 
     * @see org.geotools.data.FeatureSource#getFeatures(org.geotools.filter.Filter)
     */
    public FeatureCollection getFeatures(Filter filter){
        return getFeatures(new DefaultQuery(ft.getTypeName(), filter));
    }

    /**
     * 
     * @see org.geotools.data.FeatureSource#getFeatures(org.geotools.data.Query)
     */
    public FeatureCollection getFeatures(Query query) {
        return new WFSFeatureResults(this, query);
    }

    /**
     * 
     * @see org.geotools.data.AbstractFeatureSource#getTransaction()
     */
    public Transaction getTransaction() {
        return Transaction.AUTO_COMMIT;
    }

    /**
     * 
     * @author dzwiers
     */
    public static class WFSFeatureResults extends DefaultFeatureResults implements FeatureResults {
        private WFSFeatureSource fs;
        private Query query;

        /**
         * 
         * @param fs
         * @param query
         */
        public WFSFeatureResults(WFSFeatureSource fs, Query query) {
        	super(fs, query);
            this.query = query;
            this.fs = fs;
        }

        /**
         * 
         * @see org.geotools.data.FeatureResults#getSchema()
         */
        public FeatureType getSchema(){
            return fs.ft;
        }

        /**
         * 
         * @see org.geotools.data.FeatureResults#reader()
         */
        public FeatureReader reader() throws IOException {
            return fs.ds.getFeatureReader(query, fs.getTransaction());
        }

        /**
         * 
         * @see org.geotools.data.FeatureResults#getBounds()
         */
        public Envelope getBounds(){
            try {
				return fs.getBounds(query);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
        }

        /**
         * 
         * @see org.geotools.data.FeatureResults#getCount()
         */
        public int getCount() throws IOException {
            return fs.getCount(query);
        }
    }
}
