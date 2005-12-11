package org.geotools.styling;

import org.geotools.event.AbstractGTComponent;

public class RemoteOWSImpl extends AbstractGTComponent implements RemoteOWS {

	private String service;
	private String onlineResource;
	
	public String getService() {
		return service;
	}
	
	public void setService(String service) {
		String old = this.service;
		this.service = service;
		
		fireChildChanged("service",this.service,old);
	}
	
	public String getOnlineResource() {
		return onlineResource;
	}
	
	public void setOnlineResource(String onlineResource) {
		String old = this.onlineResource;
		this.onlineResource = onlineResource;
		
		fireChildChanged("onlineResource",this.onlineResource,old);
	}

}
