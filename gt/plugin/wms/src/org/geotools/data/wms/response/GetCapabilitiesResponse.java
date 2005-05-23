package org.geotools.data.wms.response;

import java.io.InputStream;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;

import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.xml.WMSSchema;
import org.geotools.ows.ServiceException;
import org.geotools.xml.DocumentFactory;
import org.xml.sax.SAXException;

import com.vividsolutions.xdo.Decoder;

public class GetCapabilitiesResponse extends AbstractResponse {

	private WMSCapabilities capabilities;

	public GetCapabilitiesResponse(String contentType, InputStream inputStream) throws ServiceException, SAXException {
		super(contentType, inputStream);
		
        Map hints = new HashMap();
//        hints.put(Decoder.DEFAULT_NAMESPACE_HINT_KEY, WMSSchema.getInstance());
        hints.put(Decoder.VALIDATION_HINT, Boolean.FALSE);

        Object object = DocumentFactory.getInstance(inputStream, hints, Level.WARNING);
        
        if (object instanceof ServiceException) {
        	throw (ServiceException) object;
        }

        WMSCapabilities capabilities = (WMSCapabilities) object;
        this.capabilities = capabilities;
	}
	
	/**
	 * Returns the capabilities object parsed during the response
	 */
	public WMSCapabilities getCapabilities() {
                
		return capabilities;
	}
	
	
//      OutputStream outputStream = urlConnection.getOutputStream();
//      Writer out = new BufferedWriter(new OutputStreamWriter(outputStream));
//      Map writerHints = new HashMap();
//      hints.put(DocumentWriter.BASE_ELEMENT, OGCSchema.getInstance().getElements()[OGCSchema.GET_CAPABILITIES]);
//      try {
//          DocumentWriter.writeDocument(GetCapabilitiesRequest.SECTION_ALL, OGCSchema.getInstance(), out, writerHints);
//      } catch (OperationNotSupportedException e) {
//          e.printStackTrace();
//      }      
}
