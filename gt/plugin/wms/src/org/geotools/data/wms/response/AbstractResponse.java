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
package org.geotools.data.wms.response;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.geotools.data.wms.xml.WMSSchema;
import org.geotools.ows.ServiceException;
import org.geotools.xml.DocumentFactory;
import org.xml.sax.SAXException;

import com.vividsolutions.xdo.Decoder;


/**
 * DOCUMENT ME!
 *
 * @author Richard Gould, Refractions Research
 */
public class AbstractResponse {
    protected InputStream inputStream;
    protected String contentType;

    public AbstractResponse(String contentType, InputStream inputStream) throws ServiceException, SAXException {
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
    
    protected ServiceException parseException(InputStream inputStream) throws SAXException {
        Map hints = new HashMap();
//        hints.put(DocumentHandler.DEFAULT_NAMESPACE_HINT_KEY, WMSSchema.getInstance());
        hints.put(Decoder.VALIDATION_HINT, Boolean.FALSE);

        Object object = DocumentFactory.getInstance(inputStream, hints, Level.WARNING);
        if (object instanceof ServiceException) {
        	return (ServiceException) object;
        }
        return null;
    }
}
