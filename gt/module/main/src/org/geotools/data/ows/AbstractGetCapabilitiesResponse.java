package org.geotools.data.ows;

import java.io.IOException;
import java.io.InputStream;

import org.geotools.ows.ServiceException;
import org.xml.sax.SAXException;

public abstract class AbstractGetCapabilitiesResponse extends AbstractResponse {

	protected Capabilities capabilities;

	public AbstractGetCapabilitiesResponse(String contentType, InputStream inputStream) throws ServiceException, IOException {
		super(contentType, inputStream);
	}
 
	/**
	 * Returns the capabilities object parsed during the response
	 */
	public Capabilities getCapabilities() {
		return capabilities;
	}
}
