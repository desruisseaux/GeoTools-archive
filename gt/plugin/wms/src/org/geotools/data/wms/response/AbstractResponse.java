/*
 * Created on Aug 18, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.response;

import java.io.InputStream;

/**
 * @author Richard Gould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AbstractResponse {

	protected InputStream inputStream;
	protected String contentType;

	public AbstractResponse(String contentType, InputStream inputStream) {
		this.inputStream = inputStream;
		this.contentType = contentType;
	}
	
	public String getContentType() {
		return contentType;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

}
