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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.data.wfs.Action.DeleteAction;
import org.geotools.data.wfs.Action.InsertAction;
import org.geotools.data.wfs.Action.UpdateAction;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;


/**
 * DOCUMENT ME!
 *
 * @author dzwiers 
 */
public class WFSFeatureStore extends WFSFeatureSource implements FeatureStore {
    protected Transaction trans = Transaction.AUTO_COMMIT;

    /**
     * 
     * @param ds
     * @param ft
     */
    public WFSFeatureStore(WFSDataStore ds, FeatureType ft) {
        super(ds, ft);
    }

    /**
     * 
     * @see org.geotools.data.AbstractFeatureSource#getTransaction()
     */
    public Transaction getTransaction() {
        return trans;
    }

    /**
     * NOTE: The fids returned are not persistent until the transaction is
     * commited.
     *
     * @see org.geotools.data.FeatureStore#addFeatures(org.geotools.data.FeatureReader)
     */
    public Set addFeatures(FeatureReader reader) throws IOException {
        WFSTransactionState ts = null;

        if (trans == Transaction.AUTO_COMMIT) {
            ts = new WFSTransactionState(ds);
        } else {
            ts = (WFSTransactionState) trans.getState(ds);
        }

        HashSet r = new HashSet();

        while (reader.hasNext()){
            try {
                Feature f = reader.next();
                r.add(f.getID());
            	AttributeType[] atrs = f.getFeatureType().getAttributeTypes();
            	for(int i=0;i<atrs.length;i++){
            		if(atrs[i] instanceof GeometryAttributeType){
            			Geometry g = (Geometry)f.getAttribute(i);
                		CoordinateReferenceSystem cs = ((GeometryAttributeType)atrs[i]).getCoordinateSystem();
                		g.setUserData(cs.getIdentifiers().iterator().next().toString());
            		}
            	}
                ts.addAction(new InsertAction(f));
            } catch (NoSuchElementException e) {
                WFSDataStoreFactory.logger.warning(e.toString());
                throw new IOException(e.toString());
            } catch (IllegalAttributeException e) {
                WFSDataStoreFactory.logger.warning(e.toString());
                throw new IOException(e.toString());
            }
        }
        
        if (trans == Transaction.AUTO_COMMIT) {
            ts.commit();

            String[] fids = ts.getFids();
            r = new HashSet(Arrays.asList(fids));

            return r;
        }

        return r;
    }

    /**
     * 
     * @see org.geotools.data.FeatureStore#removeFeatures(org.geotools.filter.Filter)
     */
    public void removeFeatures(Filter filter) throws IOException {
        WFSTransactionState ts = null;

        if (trans == Transaction.AUTO_COMMIT) {
            ts = new WFSTransactionState(ds);
        } else {
            ts = (WFSTransactionState) trans.getState(ds);
        }

        ts.addAction(new DeleteAction(ft.getTypeName(), filter));

        if (trans == Transaction.AUTO_COMMIT) {
            ts.commit();
        }
    }

    /**
     * 
     * @see org.geotools.data.FeatureStore#modifyFeatures(org.geotools.feature.AttributeType[], java.lang.Object[], org.geotools.filter.Filter)
     */
    public void modifyFeatures(AttributeType[] type, Object[] value,
        Filter filter) throws IOException {
        WFSTransactionState ts = null;

        if (trans == Transaction.AUTO_COMMIT) {
            ts = new WFSTransactionState(ds);
        } else {
            ts = (WFSTransactionState) trans.getState(ds);
        }

        Map props = new HashMap();

        for (int i = 0; i < type.length; i++) {
        	if(type[i] instanceof GeometryAttributeType){
        		Geometry g = (Geometry)value[i];
        		CoordinateReferenceSystem cs = ((GeometryAttributeType)type[i]).getCoordinateSystem();
        		g.setUserData(cs.getIdentifiers().iterator().next().toString());
        	}
            props.put(type[i].getName(), value[i]);
        }

        ts.addAction(new UpdateAction(ft.getTypeName(), filter, props));

        if (trans == Transaction.AUTO_COMMIT) {
            ts.commit();
        }
    }

    /**
     * 
     * @see org.geotools.data.FeatureStore#modifyFeatures(org.geotools.feature.AttributeType, java.lang.Object, org.geotools.filter.Filter)
     */
    public void modifyFeatures(AttributeType type, Object value, Filter filter)
        throws IOException {
        modifyFeatures(new AttributeType[] { type, }, new Object[] { value, },
            filter);
    }

    /**
     * 
     * @see org.geotools.data.FeatureStore#setFeatures(org.geotools.data.FeatureReader)
     */
    public void setFeatures(FeatureReader reader) throws IOException {
        WFSTransactionState ts = null;

        if (trans == Transaction.AUTO_COMMIT) {
            ts = new WFSTransactionState(ds);
        } else {
            ts = (WFSTransactionState) trans.getState(ds);
        }

        ts.addAction(new DeleteAction(ft.getTypeName(), Filter.NONE));

        while (reader.hasNext())

            try {
            	Feature f = reader.next();
            	AttributeType[] atrs = f.getFeatureType().getAttributeTypes();
            	for(int i=0;i<atrs.length;i++){
            		if(atrs[i] instanceof GeometryAttributeType){
            			Geometry g = (Geometry)f.getAttribute(i);
                		CoordinateReferenceSystem cs = ((GeometryAttributeType)atrs[i]).getCoordinateSystem();
                		g.setUserData(cs.getIdentifiers().iterator().next().toString());
            		}
            	}
                ts.addAction(new InsertAction(f));
            } catch (NoSuchElementException e) {
                WFSDataStoreFactory.logger.warning(e.toString());
            } catch (IllegalAttributeException e) {
                WFSDataStoreFactory.logger.warning(e.toString());
            }

        if (trans == Transaction.AUTO_COMMIT) {
            ts.commit();
        }
    }

    /**
     * 
     * @see org.geotools.data.FeatureStore#setTransaction(org.geotools.data.Transaction)
     */
    public void setTransaction(Transaction transaction) {
        if(transaction == null)
            throw new NullPointerException("Should this not be Transaction.AutoCommit?");
        trans = transaction;
        if(trans != Transaction.AUTO_COMMIT){
        	WFSTransactionState ts = (WFSTransactionState) trans.getState(ds);
        	if(ts == null){
        		trans.putState(ds,new WFSTransactionState(ds));
        	}
        }
    }
}
