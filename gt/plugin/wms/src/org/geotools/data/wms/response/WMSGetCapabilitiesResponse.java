package org.geotools.data.wms.response;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.geotools.data.ows.AbstractGetCapabilitiesResponse;
import org.geotools.data.ows.Capabilities;
import org.geotools.data.wms.xml.WMSSchema;
import org.geotools.ows.ServiceException;
import org.geotools.xml.DocumentFactory;
import org.geotools.xml.handlers.DocumentHandler;
import org.xml.sax.SAXException;

public class WMSGetCapabilitiesResponse extends AbstractGetCapabilitiesResponse {

	public WMSGetCapabilitiesResponse(String contentType, InputStream inputStream) throws ServiceException, IOException {
		super(contentType, inputStream);
	
        Map hints = new HashMap();
        hints.put(DocumentHandler.DEFAULT_NAMESPACE_HINT_KEY, WMSSchema.getInstance());
        hints.put(DocumentFactory.VALIDATION_HINT, Boolean.FALSE);

        Object object;
		try {
			object = DocumentFactory.getInstance(inputStream, hints, Level.WARNING);
		} catch (SAXException e) {
			throw (ServiceException) new ServiceException("Error while parsing XML.").initCause(e);
		}
        
        if (object instanceof ServiceException) {
        	throw (ServiceException) object;
        }

        Capabilities capabilities = (Capabilities) object;
        this.capabilities = capabilities;
	}

}
