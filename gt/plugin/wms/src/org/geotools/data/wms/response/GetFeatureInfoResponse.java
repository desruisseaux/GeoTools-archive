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
public class GetFeatureInfoResponse extends AbstractResponse {

	/**
	 * @param contentType
	 * @param inputStream
	 */
	public GetFeatureInfoResponse(String contentType, InputStream inputStream) {
		super(contentType, inputStream);
	}
}
