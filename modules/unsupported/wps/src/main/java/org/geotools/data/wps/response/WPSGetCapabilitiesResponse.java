/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
 *    
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.wps.response;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import net.opengis.wps.WPSCapabilitiesType;

import org.geotools.data.ows.AbstractWPSGetCapabilitiesResponse;
import org.geotools.data.ows.Capabilities;
import org.geotools.data.ows.GetCapabilitiesResponse;
import org.geotools.ows.ServiceException;
import org.geotools.wps.WPSConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.xml.sax.SAXException;

/**
 * Provides a hook up to parse the capabilities document from inputstream.
 * 
 * @author gdavis
 *
 */
public class WPSGetCapabilitiesResponse extends AbstractWPSGetCapabilitiesResponse {

	public WPSGetCapabilitiesResponse(String contentType, InputStream inputStream) throws ServiceException, IOException {
		super(contentType, inputStream);
		
		try {
	        //Map hints = new HashMap();
	        //hints.put(DocumentHandler.DEFAULT_NAMESPACE_HINT_KEY, WPSSchema.getInstance());
	        //hints.put(DocumentFactory.VALIDATION_HINT, Boolean.FALSE);
        	Configuration config = new WPSConfiguration();
        	Parser parser = new Parser(config);
	
	        Object object;
			try {
				//object = DocumentFactory.getInstance(inputStream, hints, Level.WARNING);
				object = parser.parse(inputStream);
			} catch (SAXException e) {
				throw (ServiceException) new ServiceException("Error while parsing XML.").initCause(e);
			} catch (ParserConfigurationException e) {
				throw (ServiceException) new ServiceException("Error while parsing XML.").initCause(e);
			}
	        
	        if (object instanceof ServiceException) {
	        	throw (ServiceException) object;
	        }
	        
	        //Class<? extends Object> class1 = object.getClass();
	        //System.out.println(class1);
	        // CapabilitiesBaseType / WPSCapabilitiesType
	        this.capabilities = (WPSCapabilitiesType) object;
		} finally {
			inputStream.close();
		}
	}

}
