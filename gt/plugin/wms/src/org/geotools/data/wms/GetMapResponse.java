/*
 * Created on Jul 13, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms;

import java.io.InputStream;

/**
 * @author rgould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class GetMapResponse {
	/** The format of the response */
	private String format;
	
	/** The actual response (usually an image or exception) */
	private InputStream response; 
	
	/**
	 * @param format
	 * @param response
	 */
	public GetMapResponse(String format, InputStream response) {
		super();
		this.format = format;
		this.response = response;
	}
	
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public InputStream getResponse() {
		return response;
	}
	public void setResponse(InputStream response) {
		this.response = response;
	}
}
