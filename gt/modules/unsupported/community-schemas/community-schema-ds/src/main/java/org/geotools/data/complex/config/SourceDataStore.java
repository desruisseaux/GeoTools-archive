package org.geotools.data.complex.config;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.geotools.util.CheckedHashMap;

public class SourceDataStore implements Serializable{
	private static final long serialVersionUID = 8540617713675342340L;
	private String id;
	private Map params = Collections.EMPTY_MAP;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Map getParams() {
		return new HashMap( params);
	}
	public void setParams(Map params) {
		this.params = new CheckedHashMap(Serializable.class, Serializable.class);
		if(params != null){
			this.params.putAll(params);
		}
	}
}
