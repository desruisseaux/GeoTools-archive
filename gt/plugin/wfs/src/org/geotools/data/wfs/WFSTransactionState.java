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

import org.geotools.data.Transaction;
import org.geotools.data.Transaction.State;

/**
 * @author dzwiers
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WFSTransactionState implements State {

	private Set fidSet;
	
	/* (non-Javadoc)
	 * @see org.geotools.data.Transaction.State#setTransaction(org.geotools.data.Transaction)
	 */
	public void setTransaction(Transaction transaction) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.geotools.data.Transaction.State#addAuthorization(java.lang.String)
	 */
	public void addAuthorization(String AuthID) throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.geotools.data.Transaction.State#commit()
	 */
	public void commit() throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.geotools.data.Transaction.State#rollback()
	 */
	public void rollback() throws IOException {
		fidSet = null;
		actions = new LinkedList();
	}
	
	public Set getFids(){
		return fidSet;
	}

	private LinkedList actions = new LinkedList();
	public void addAction(Action a){
		actions.add(a);
	}
	
	public List getActions(){
		return new LinkedList(actions);
	}
}
