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
package org.geotools.data.wms.request;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;


/**
 * Functionality for performing basic requests
 *
 * @author Richard Gould
 */
public class AbstractRequest {
    /** Represents OGC Exception MIME types */
    public static final String EXCEPTION_XML = "application/vnd.ogc.se_xml";
    protected URL onlineResource;
    protected Properties properties;

    /**
     * DOCUMENT ME!
     *
     * @param onlineResource
     */
    public AbstractRequest(URL onlineResource) {
    	
    	properties = new Properties();
    	
        // Need to strip off the query, as getFinalURL will add it back
        // on, with all the other properties. If we don't, elements will
        // be duplicated.
        int index = onlineResource.toExternalForm().lastIndexOf("?");
        String urlWithoutQuery = null;

        if (index <= 0) {
            urlWithoutQuery = onlineResource.toExternalForm();
        } else {
            urlWithoutQuery = onlineResource.toExternalForm().substring(0, index);
        }

        try {
            this.onlineResource = new URL(urlWithoutQuery);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error parsing URL");
        }

        // Doing this preserves all of the query parameters while
        // enforcing the mandatory ones
        if (onlineResource.getQuery() != null) {
            StringTokenizer tokenizer = new StringTokenizer(onlineResource.getQuery(),
                    "&");

            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                String[] param = token.split("=");
                setProperty(param[0].toUpperCase(), param[1]);
            }
        }

        
    }

    public URL getFinalURL() {
        String url = onlineResource.toExternalForm();

        if (!url.endsWith("?")) {
            url = url.concat("?");
        }

        Iterator iter = properties.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String param = entry.getKey() + "=" + entry.getValue();

            if (iter.hasNext()) {
                param = param.concat("&");
            }

            url = url.concat(param);
        }

        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            //If something is wrong here, this is something wrong with the code above.
        }

        return null;
    }

    public void setProperty(String name, String value) {
        properties.setProperty(name, value);
    }
}
