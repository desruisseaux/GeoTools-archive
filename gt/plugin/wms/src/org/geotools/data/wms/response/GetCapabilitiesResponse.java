/*
 * Created on Aug 18, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.response;

import java.io.IOException;
import java.io.InputStream;

import org.geotools.data.wms.CapabilitiesParser;
import org.geotools.data.wms.ParseCapabilitiesException;
import org.geotools.data.wms.getCapabilities.WMT_MS_Capabilities;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * @author Richard Gould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class GetCapabilitiesResponse extends AbstractResponse {

	private WMT_MS_Capabilities capabilities;

	public GetCapabilitiesResponse(String contentType, InputStream inputStream) throws JDOMException, ParseCapabilitiesException, IOException {
		super(contentType, inputStream);
		
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(inputStream);

		Element root = document.getRootElement(); //Root = "WMT_MS_Capabilities"
		
		capabilities = CapabilitiesParser.parseCapabilities(root);
	}
	
	public WMT_MS_Capabilities getCapabilities() {
		return capabilities;
	}

}
