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
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.geotools.data.ows.LayerDescription;
import org.geotools.data.wms.xml.WMSSchema;
import org.geotools.xml.DocumentFactory;
import org.geotools.xml.handlers.DocumentHandler;
import org.xml.sax.SAXException;

/**
 * Represents the response from a server after a DescribeLayer request
 * has been issued.
 * 
 * @author Richard Gould
 */
public class DescribeLayerResponse extends AbstractResponse {

    private LayerDescription[] layerDescs;

    /**
     * @param contentType
     * @param inputStream
     * @throws SAXException
     */
    public DescribeLayerResponse( String contentType, InputStream inputStream ) throws SAXException {
        super(contentType, inputStream);
        
        Map hints = new HashMap();
        hints.put(DocumentHandler.DEFAULT_NAMESPACE_HINT_KEY, WMSSchema.getInstance());

        Object object = DocumentFactory.getInstance(inputStream, hints, Level.WARNING);
        
        layerDescs = (LayerDescription[]) object;
    }
    
    

    public LayerDescription[] getLayerDescs() {
        return layerDescs;
    }

}
