/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.data.wms.response;

import java.io.IOException;
import java.io.InputStream;

import org.geotools.data.wms.ParseCapabilitiesException;
import org.geotools.data.wms.WMSParser;
import org.geotools.data.wms.capabilities.Capabilities;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Represents the response of a getCapabilities request.
 * <p>
 * Feedback - this object actually starts the parsing? As part of the constructor (!).
 * How does this differ at all from just doing the work? Unless you wanted to go nuts
 * and support response encoding). Basically provide the contentType and inputStream to
 * the superclass and have it take care of providing you a Document. Wrapping the inputStream
 * with zip uncoding before passing it to SAXBuilder.build( inputStream).
 * </p>
 * <p>
 * Another suggestion - store the error. So this object either provides you with an exception or
 * a working Capabilities object when all is said and done.
 * </p>
 * @author Richard Gould, Refractions Research
 */
public class GetCapabilitiesResponse extends AbstractResponse {

	private Capabilities capabilities;

	public GetCapabilitiesResponse(WMSParser parser, String contentType, InputStream inputStream) throws JDOMException, ParseCapabilitiesException, IOException {
		super(contentType, inputStream);
		
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(inputStream);
		
		capabilities = parser.constructCapabilities( document );
	}
	
	/** Retrived parsed Capabilities */
	public Capabilities getCapabilities() {
		return capabilities;
	}

}
