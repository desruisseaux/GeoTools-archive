/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.wfs.v_1_0_0.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.LenientBuilder;
import org.geotools.feature.LenientFeatureFactory;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.wfs.v_1_0_0.data.Action.DeleteAction;
import org.geotools.wfs.v_1_0_0.data.Action.InsertAction;
import org.geotools.wfs.v_1_0_0.data.Action.UpdateAction;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;


/**
 * DOCUMENT ME!
 *
 * @author dzwiers 
 * @source $URL$
 */
public class WFSFeatureStore extends WFSFeatureSource implements FeatureStore<SimpleFeatureType, SimpleFeature> {
    protected Transaction trans = Transaction.AUTO_COMMIT;

    /**
     * 
     * @param ds
     * @param typeName
     */
    public WFSFeatureStore(WFS_1_0_0_DataStore ds, String typeName) {
        super(ds, typeName);
    }

    /**
     * 
     * @see org.geotools.data.AbstractFeatureSource#getTransaction()
     */
    public Transaction getTransaction() {
        return trans;
    }

    public Set addFeatures(final  FeatureReader<SimpleFeatureType, SimpleFeature> reader) throws IOException {
        List features=new ArrayList();
        while(reader.hasNext()){
            try {
                SimpleFeature next = reader.next();
                features.add(next);
            } catch (Exception e) {
                throw (IOException) new IOException( ).initCause( e );
            }
        }
        return addFeatures(DataUtilities.collection((SimpleFeature[]) features.toArray(new SimpleFeature[0])));
    }

	public Set addFeatures(FeatureCollection<SimpleFeatureType, SimpleFeature> collection) throws IOException {
        WFSTransactionState ts = null;

        if (trans == Transaction.AUTO_COMMIT) {
            ts = new WFSTransactionState(ds);
        } else {
            ts = (WFSTransactionState) trans.getState(ds);
        }

        HashSet r = new HashSet();
        
        SimpleFeatureType schema = getSchema();
        
        LenientBuilder build = new LenientBuilder( schema );
        
        boolean isLenient = true;
        if( schema.getUserData().containsKey("lenient")){
            isLenient = (Boolean) schema.getUserData().get("lenient");
        }
        
        if( isLenient ){
            build.setFeatureFactory( new LenientFeatureFactory());
        }
        
        List<AttributeDescriptor> atrs = schema.getAttributes();
        FeatureIterator<SimpleFeature> iter=collection.features();
        try{
            ReferencedEnvelope bounds=null;
            
        while (iter.hasNext()){
            try {
                SimpleFeature newFeature;
                try {
                    SimpleFeature f = iter.next();
                    
                    String nextFid = ts.nextFid(schema.getTypeName());
                    Object[] values = f.getAttributes().toArray();
                    
                    build.addAll( values );
                    newFeature = build.buildFeature( nextFid );
                    
                    r.add(newFeature.getID());
                } catch (IllegalAttributeException e) {
                    throw (IOException) new IOException( e.getLocalizedMessage() );
                }

                for(int i=0;i<atrs.size();i++){
                    AttributeDescriptor att = atrs.get(i);
                    if(att instanceof GeometryDescriptor){
                        Geometry g = (Geometry) newFeature.getAttribute(i);
                        CoordinateReferenceSystem cs = ((GeometryDescriptor)att).getCRS();
                        if( g==null )
                            continue;
                        if( cs!=null && !cs.getIdentifiers().isEmpty() )
                            g.setUserData(cs.getIdentifiers().iterator().next().toString());
                        if( bounds==null ){
                            bounds=new ReferencedEnvelope(g.getEnvelopeInternal(), schema.getCRS() );
                        }else{
                            bounds.expandToInclude(g.getEnvelopeInternal());
                        }
                    }
                }
                
                
                ts.addAction(schema.getTypeName(), new InsertAction(newFeature));

            } catch (NoSuchElementException e) {
                WFS_1_0_0_DataStore.LOGGER.warning(e.toString());
                throw new IOException(e.toString());
            }
        }

        // Fire a notification.
        // JE
        if( bounds==null){
            // if bounds are null then send an envelope to say that features were added but
            // at an unknown location.
            bounds = new ReferencedEnvelope( getSchema().getCRS() );
            ((WFS_1_0_0_DataStore)getDataStore()).listenerManager.fireFeaturesRemoved(schema.getTypeName(),
                    getTransaction(), bounds, false);
        }else{
            ((WFS_1_0_0_DataStore)getDataStore()).listenerManager.fireFeaturesRemoved(schema.getTypeName(),
                    getTransaction(), bounds, false);                   
        }

        }finally{ 
            iter.close();
        }
        if (trans == Transaction.AUTO_COMMIT) {
            ts.commit();

            String[] fids = ts.getFids(schema.getTypeName());
            r = new HashSet(Arrays.asList(fids));

            return r;
        }

        return r;
	}

	/**
     * 
     * @see org.geotools.data.FeatureStore#removeFeatures(org.geotools.filter.Filter)
     */
    public void removeFeatures(Filter filter2) throws IOException {
    	Filter filter=ds.processFilter(filter2);
        WFSTransactionState ts = null;

        if (trans == Transaction.AUTO_COMMIT) {
            ts = new WFSTransactionState(ds);
        } else {
            ts = (WFSTransactionState) trans.getState(ds);
        }

        ts.addAction(getSchema().getTypeName(), new DeleteAction(getSchema().getTypeName(), filter));
        
        // Fire a notification.  I don't know a way of quickly getting the bounds of
        // an arbitrary filter so I'm sending a NULL envelope to say "some features were removed but I don't
        // know what."  Can't be null because the convention states that null is sent on commits only.
        // JE
        ((WFS_1_0_0_DataStore)getDataStore()).listenerManager.fireFeaturesRemoved(getSchema().getTypeName(),
        		getTransaction(), null, false);

        if (trans == Transaction.AUTO_COMMIT) {
            ts.commit();
        }
    }
    /**
     * 
     * @see org.geotools.data.FeatureStore#modifyFeatures(org.geotools.feature.AttributeType[], java.lang.Object[], org.geotools.filter.Filter)
     */
    public void modifyFeatures(AttributeDescriptor[] type, Object[] value,
        Filter filter2) throws IOException {
    	Filter filter=ds.processFilter(filter2);
        WFSTransactionState ts = null;

        if (trans == Transaction.AUTO_COMMIT) {
            ts = new WFSTransactionState(ds);
        } else {
            ts = (WFSTransactionState) trans.getState(ds);
        }

        Map props = new HashMap();
        
        ReferencedEnvelope bounds=null;
        for (int i = 0; i < type.length; i++) {
        	if(type[i] instanceof GeometryDescriptor){
        		Geometry g = (Geometry)value[i];
        		CoordinateReferenceSystem cs = ((GeometryDescriptor)type[i]).getCRS();

                if( cs!=null && !cs.getIdentifiers().isEmpty() )
                    g.setUserData(cs.getIdentifiers().iterator().next().toString());
        		g.setUserData(cs.getIdentifiers().iterator().next().toString());
                if( cs!=null && !cs.getIdentifiers().isEmpty() )
                    g.setUserData(cs.getIdentifiers().iterator().next().toString());
                // set/expand the bounds that are being changed.
                if( g==null )
                	continue;
                if( bounds==null ){
                    bounds=new ReferencedEnvelope(g.getEnvelopeInternal(),cs);
                }else{
                    bounds.expandToInclude(g.getEnvelopeInternal());
                }
        	}
            props.put(type[i].getLocalName(), value[i]);
        }

        ts.addAction(getSchema().getTypeName(), new UpdateAction(getSchema().getTypeName(), filter, props));

        // Fire a notification.
        // JE
        if( bounds==null ){
            // if bounds are null then send an envelope to say that features were modified but
            // at an unknown location.
            bounds = new ReferencedEnvelope(getSchema().getCRS());
            ((WFS_1_0_0_DataStore)getDataStore()).listenerManager.fireFeaturesRemoved(getSchema().getTypeName(),
                    getTransaction(), bounds, false);
        }else{
            ((WFS_1_0_0_DataStore)getDataStore()).listenerManager.fireFeaturesRemoved(getSchema().getTypeName(),
                    getTransaction(), bounds, false);                   
        }    
        
        if (trans == Transaction.AUTO_COMMIT) {
            ts.commit();
        }
    }
    /**
     * 
     * @see org.geotools.data.FeatureStore#modifyFeatures(org.geotools.feature.AttributeType, java.lang.Object, org.geotools.filter.Filter)
     */
    public void modifyFeatures(AttributeDescriptor type, Object value, Filter filter)
        throws IOException {
        modifyFeatures(new AttributeDescriptor[] { type, }, new Object[] { value, },
            filter);
    }

    /**
     * 
     * @see org.geotools.data.FeatureStore#setFeatures(org.geotools.data.FeatureReader)
     */
    public void setFeatures(FeatureReader <SimpleFeatureType, SimpleFeature> reader) throws IOException {
        WFSTransactionState ts = null;

        if (trans == Transaction.AUTO_COMMIT) {
            ts = new WFSTransactionState(ds);
        } else {
            ts = (WFSTransactionState) trans.getState(ds);
        }

        ts.addAction(getSchema().getTypeName(), new DeleteAction(getSchema().getTypeName(), Filter.INCLUDE));
        
        ReferencedEnvelope bounds=null;
        while (reader.hasNext()){

            try {
            	SimpleFeature f = reader.next();
            	List<AttributeDescriptor> atrs = f.getFeatureType().getAttributes();
            	for(int i=0;i<atrs.size();i++){
            		if(atrs.get(i) instanceof GeometryDescriptor){
            			Geometry g = (Geometry)f.getAttribute(i);
                		CoordinateReferenceSystem cs = ((GeometryDescriptor)atrs.get(i)).getCRS();
                        if( cs!=null && !cs.getIdentifiers().isEmpty() )
                            g.setUserData(cs.getIdentifiers().iterator().next().toString());
                        if( g==null )
                        	continue;
                        if( bounds==null ){
                            bounds=new ReferencedEnvelope(g.getEnvelopeInternal(),cs);
                        }else{
                            bounds.expandToInclude(g.getEnvelopeInternal());
                        }
            		}
            	}
                ts.addAction(getSchema().getTypeName(), new InsertAction(f));
            } catch (NoSuchElementException e) {
                WFS_1_0_0_DataStore.LOGGER.warning(e.toString());
            } catch (IllegalAttributeException e) {
                WFS_1_0_0_DataStore.LOGGER.warning(e.toString());
            }
        }
            
        // Fire a notification.
        // JE
        if( bounds==null){
            // if bounds are null then send an envelope to say that features were added but
            // at an unknown location.
            bounds = new ReferencedEnvelope( getSchema().getCRS() );
            ((WFS_1_0_0_DataStore)getDataStore()).listenerManager.fireFeaturesRemoved(getSchema().getTypeName(),
                    getTransaction(), bounds, false);
        }else{
            ((WFS_1_0_0_DataStore)getDataStore()).listenerManager.fireFeaturesRemoved(getSchema().getTypeName(),
                    getTransaction(), bounds, false);                   
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
