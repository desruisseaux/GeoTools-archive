/*
 * Created on 28-Sep-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wfs;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.Transaction.State;
import org.xml.sax.SAXException;

/**
 * @author dzwiers
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WFSTransactionState implements State {

	private WFSDataStore ds = null;
	private WFSTransactionState(){}
	public WFSTransactionState(WFSDataStore ds){
		this.ds = ds;
	}
	
	private String[] fids;

//	private String authId;
	
	/* (non-Javadoc)
	 * @see org.geotools.data.Transaction.State#setTransaction(org.geotools.data.Transaction)
	 */
	public void setTransaction(Transaction transaction) {
		if(transaction!=null){
//			authId = null;
			fids = null;
		}
		actions = new LinkedList();
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.Transaction.State#addAuthorization(java.lang.String)
	 */
	public void addAuthorization(String AuthID) throws IOException {
//		authId = AuthID;
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.Transaction.State#commit()
	 */
	public void commit() throws IOException {
		// TODO deal with authID and locking ... WFS only allows one authID / transaction ...

	    TransactionResult tr = null;
	    if((ds.protos & WFSDataStore.POST_FIRST) == WFSDataStore.POST_FIRST && tr == null)
	        tr = commitPost();

	    if((ds.protos & WFSDataStore.GET_FIRST) == WFSDataStore.GET_FIRST && tr == null)
	        tr = commitGet();
	        
	    if((ds.protos & WFSDataStore.POST_OK) == WFSDataStore.POST_OK && tr == null)
	        tr = commitPost();

	    if((ds.protos & WFSDataStore.GET_OK) == WFSDataStore.GET_OK && tr == null)
	        tr = commitGet();
	    
	    if(tr == null)
	    	throw new IOException("An error occured");
	    if(tr.getStatus() == TransactionResult.FAILED)
	    	throw new IOException(tr.getError().toString());
	    
	    fids = tr.getInsertResult().getFids();
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.Transaction.State#rollback()
	 */
	public void rollback() throws IOException {
		fids = null;
		actions = new LinkedList();
	}
	
	public String[] getFids(){
		return fids;
	}

	private LinkedList actions = new LinkedList();
	public void addAction(Action a){
		actions.add(a);
	}
	
	public List getActions(){
		return new LinkedList(actions);
	}
}
