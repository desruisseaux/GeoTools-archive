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

import org.geotools.ows.ServiceException;
import org.xml.sax.SAXException;

/**
 * Represents the results of a PutStyles request.
 * 
 * Success can be checked using the success() method.
 * 
 * @author Richard Gould
 */
public class PutStylesResponse extends AbstractResponse {

    private boolean success;

    /**
     * @param contentType
     * @param inputStream
     * @throws SAXException
     */
    public PutStylesResponse( String contentType, InputStream inputStream ) throws ServiceException, SAXException {
        super(contentType, inputStream);

        if ("application/vnd.ogc.success+xml".equals(contentType)) {
            success = true;
        }
    }
    
    /**
     * @return true if the request successfully executed, false otherwise
     */
    public boolean success() {
        return success;
    }

}
