/*
 * Created on Sep 15, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wfs;

import java.util.Map;

import org.geotools.data.FeatureLock;
import org.geotools.filter.Filter;

/**
 * @author polio
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LockRequest implements FeatureLock {
	
	private long duration = 0;
	private String[] types = null;
	private Filter[] filters = null;
	private String lockId = null;
	
	private LockRequest(){}
	protected LockRequest(long duration, Map dataSets){
		this.duration = duration;
		types = (String[])dataSets.keySet().toArray(new String[dataSets.size()]);
		filters = new Filter[types.length];
		for(int i=0;i<types.length;i++)
			filters[i] = (Filter)dataSets.get(types[i]);
	}
	protected LockRequest(long duration, String[] types, Filter[] filters){
		this.duration = duration;
		this.types = types;
		this.filters = filters;
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.FeatureLock#getAuthorization()
	 */
	public String getAuthorization() {
		return lockId;
	}
	
	protected void setAuthorization(String auth){
		lockId = auth;
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.FeatureLock#getDuration()
	 */
	public long getDuration() {
		return duration;
	}
	
	public String[] getTypeNames(){
		return types;
	}
	
	public Filter[] getFilters(){
		return filters;
	}
}
