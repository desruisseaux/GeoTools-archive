/*
 * Created on Sep 17, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wfs;

import org.geotools.filter.FidFilter;

/**
 * @author dzwiers
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LockResult {
	protected String lockId;
	protected FidFilter supported;
	protected FidFilter notSupported;
	
	private LockResult(){}
	public LockResult(String lockId, FidFilter supported, FidFilter notSupported){
		this.lockId = lockId;
		this.supported = supported;
		this.notSupported = notSupported;
	}
	/**
	 * @return Returns the lockId.
	 */
	public String getLockId() {
		return lockId;
	}
	/**
	 * @return Returns the notSupported.
	 */
	public FidFilter getNotSupported() {
		return notSupported;
	}
	/**
	 * @return Returns the supported.
	 */
	public FidFilter getSupported() {
		return supported;
	}
}
