/*
 * Created on 28-Sep-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wfs;

import java.io.IOException;
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
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.Filter;

/**
 * @author dzwiers
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WFSFeatureStore extends WFSFeatureSource implements FeatureStore{
	
	protected Transaction trans;

	/* (non-Javadoc)
	 * @see org.geotools.data.AbstractFeatureSource#getTransaction()
	 */
	public Transaction getTransaction() {
		return trans;
	}
	
	public WFSFeatureStore(WFSDataStore ds, FeatureType ft){
		super(ds,ft);
	}

	/**
	 * NOTE: The fids returned are not persistent until the transaction is commited.
	 * 
	 * @see org.geotools.data.FeatureStore#addFeatures(org.geotools.data.FeatureReader)
	 */
	public Set addFeatures(FeatureReader reader) throws IOException {
		WFSTransactionState ts = null;
		if(trans == Transaction.AUTO_COMMIT){
			ts = new WFSTransactionState(ds);
		}else{
			ts = (WFSTransactionState)trans.getState(ds);
		}
		
		HashSet r = new HashSet();
		while(reader.hasNext())
			try {
				Feature f = reader.next();
				r.add(f.getID());
				ts.addAction(new InsertAction(f));
			} catch (NoSuchElementException e) {
				WFSDataStore.logger.warning(e.toString());
			} catch (IllegalAttributeException e) {
				WFSDataStore.logger.warning(e.toString());
			}
		
		if(trans == Transaction.AUTO_COMMIT){
			ts.commit();
			return ts.getFids();
		}
		return r;
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.FeatureStore#removeFeatures(org.geotools.filter.Filter)
	 */
	public void removeFeatures(Filter filter) throws IOException {
		WFSTransactionState ts = null;
		if(trans == Transaction.AUTO_COMMIT){
			ts = new WFSTransactionState(ds);
		}else{
			ts = (WFSTransactionState)trans.getState(ds);
		}
		
		ts.addAction(new DeleteAction(ft.getTypeName(),filter));
		
		if(trans == Transaction.AUTO_COMMIT){
			ts.commit();
		}
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.FeatureStore#modifyFeatures(org.geotools.feature.AttributeType[], java.lang.Object[], org.geotools.filter.Filter)
	 */
	public void modifyFeatures(AttributeType[] type, Object[] value,
			Filter filter) throws IOException {
		WFSTransactionState ts = null;
		if(trans == Transaction.AUTO_COMMIT){
			ts = new WFSTransactionState(ds);
		}else{
			ts = (WFSTransactionState)trans.getState(ds);
		}
		
		Map props = new HashMap();
		for(int i=0;i<type.length;i++){
			props.put(type[i].getName(),value[i]);
		}
		ts.addAction(new UpdateAction(ft.getTypeName(),filter,props));
		
		if(trans == Transaction.AUTO_COMMIT){
			ts.commit();
		}
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.FeatureStore#modifyFeatures(org.geotools.feature.AttributeType, java.lang.Object, org.geotools.filter.Filter)
	 */
	public void modifyFeatures(AttributeType type, Object value, Filter filter)
			throws IOException {
		modifyFeatures(new AttributeType[] {type,}, new Object[] {value,},filter);
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.FeatureStore#setFeatures(org.geotools.data.FeatureReader)
	 */
	public void setFeatures(FeatureReader reader) throws IOException {
		WFSTransactionState ts = null;
		if(trans == Transaction.AUTO_COMMIT){
			ts = new WFSTransactionState(ds);
		}else{
			ts = (WFSTransactionState)trans.getState(ds);
		}

		ts.addAction(new DeleteAction(ft.getTypeName(),Filter.NONE));

		while(reader.hasNext())
			try {
				ts.addAction(new InsertAction(reader.next()));
			} catch (NoSuchElementException e) {
				WFSDataStore.logger.warning(e.toString());
			} catch (IllegalAttributeException e) {
				WFSDataStore.logger.warning(e.toString());
			}
		
		if(trans == Transaction.AUTO_COMMIT){
			ts.commit();
		}
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.FeatureStore#setTransaction(org.geotools.data.Transaction)
	 */
	public void setTransaction(Transaction transaction) {
		trans = transaction;
	}
}
