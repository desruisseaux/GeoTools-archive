/*
 * Created on Aug 16, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.request;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;


/**
 * @author Richard Gould
 *
 * Functionality for performing basic requests
 */
public class AbstractRequest {

	protected URL onlineResource;
	protected Properties properties;

	/**
	 * @param onlineResource
	 */
	public AbstractRequest(URL onlineResource) {
        this.onlineResource = onlineResource;
        
        properties = new Properties();
//      TODO remove this when more version support comes along
        setProperty("VERSION", "1.1.1");
	}

	public URL getFinalURL() {
	    String url = onlineResource.toExternalForm();
	    if (!url.endsWith("?")) {
	        url = url.concat("?");
	    }
	    
	    Iterator iter = properties.entrySet().iterator();
	    while (iter.hasNext()) {
	        Map.Entry entry = (Map.Entry) iter.next();
	        String param = entry.getKey() +"="+entry.getValue();
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

	/**
	 * Represents OGC Exception MIME types 
	 */
	public static final String EXCEPTION_XML = "application/vnd.ogc.se_xml";

}
