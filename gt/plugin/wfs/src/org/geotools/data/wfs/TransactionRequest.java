/*
 * Created on 16-Sep-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wfs;

import java.util.HashSet;
import java.util.Set;

/**
 * @author dzwiers
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TransactionRequest {
	private String lockId;
	private Set actions;
	
	private TransactionRequest(){}
	protected TransactionRequest(String lockId){
		this.lockId = lockId;
	}
	public TransactionRequest(String lockId,Set actions){
		this.lockId = lockId;
		this.actions = actions;
	}
	
	public void addAction(Action action){
		if(actions == null)actions = new HashSet();
		actions.add(action);
	}
	/**
	 * @return Returns the actions.
	 */
	public Set getActions() {
		return actions;
	}
	/**
	 * @return Returns the lockId.
	 */
	public String getLockId() {
		return lockId;
	}
}
