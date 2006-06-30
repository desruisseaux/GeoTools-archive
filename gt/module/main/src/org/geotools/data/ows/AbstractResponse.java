/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
package org.geotools.data.ows;

import java.io.IOException;
import java.io.InputStream;

import org.geotools.ows.ServiceException;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;


/**
 * DOCUMENT ME!
 *
 * @author Richard Gould, Refractions Research
 * @source $URL: http://svn.geotools.org/geotools/branches/2.2.x/plugin/wms/src/org/geotools/data/wms/response/AbstractResponse.java $
 */
public abstract class AbstractResponse {
    protected InputStream inputStream;
    protected String contentType;

    public AbstractResponse(String contentType, InputStream inputStream) throws ServiceException, IOException {
        this.inputStream = inputStream;
        this.contentType = contentType;
        
        /*
         * Intercept XML ServiceExceptions and throw them
         */
        if (contentType.toLowerCase().equals("application/vnd.ogc.se_xml")) {
        	throw parseException(inputStream);
        }
    }

    public String getContentType() {
        return contentType;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
    
    protected ServiceException parseException(InputStream inputStream) throws IOException {
    	try {
			return ServiceExceptionParser.parse(inputStream);
		} catch (JDOMException e) {
			throw (IOException) new IOException().initCause(e);
		} 
    }
}
