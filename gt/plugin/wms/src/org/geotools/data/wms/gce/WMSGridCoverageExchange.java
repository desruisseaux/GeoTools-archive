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
package org.geotools.data.wms.gce;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidParameterException;

import org.geotools.data.coverage.grid.Format;
import org.geotools.data.coverage.grid.GridCoverageExchange;
import org.geotools.data.coverage.grid.GridCoverageReader;
import org.geotools.data.coverage.grid.GridCoverageWriter;
import org.geotools.data.wms.ParseCapabilitiesException;
import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wms.getCapabilities.WMT_MS_Capabilities;
import org.jdom.JDOMException;

/**
 * @author rgould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WMSGridCoverageExchange implements GridCoverageExchange {
    private Format[] formats;
    private WMT_MS_Capabilities capabilities;
    
    public WMSGridCoverageExchange (Object source) throws MalformedURLException, IOException, ParseCapabilitiesException {
    	if (source instanceof String || source instanceof URL) {
    		URL url = null;
    		if (source instanceof String) {
    			url = new URL ((String) source);
    		} else {
    			url = (URL) source;
    		}

   			WebMapServer wms = new WebMapServer(url, true);
   			capabilities = wms.getCapabilities();
   			if (capabilities == null) {
   				Exception e = wms.getProblem();
   				if (e instanceof JDOMException) {
   					throw new RuntimeException ("Data at the URL is not valid XML", e);
   				} else if (e instanceof IOException){
   					throw (IOException) e;
   				} else if (e instanceof MalformedURLException) {
   					throw (MalformedURLException) e;
   				} else if (e instanceof ParseCapabilitiesException) {
   					throw (ParseCapabilitiesException) e;
   				}
   			}
    	} else if (source instanceof WMT_MS_Capabilities) {
    		capabilities = (WMT_MS_Capabilities) source;
    	}
    	formats = new Format[1];
    	formats[0] = new WMSFormat(capabilities);
    }
	
	public void dispose() throws IOException {
		// TODO Auto-generated method stub

	}
	public Format[] getFormats() {
		return formats;
	}
	
	public GridCoverageReader getReader(Object source) throws IOException {
		if (source instanceof String || source instanceof URL) {
			if (source instanceof String) {
				try {
					new URL((String) source);
				} catch (MalformedURLException e) {
					throw new InvalidParameterException("Unable to convert source to a URL (it is malformed)");
				}
			}
			
			for (int i = 0; i < formats.length; i++) {
				Format format = formats[i];
				if (format.accepts(source)) {
					return format.getReader(capabilities);
				}
			}
		}
        throw new InvalidParameterException("Source is not of a support type");
	}
	
	/**
	 * WMS Specification does not permit writing!
	 */
	public GridCoverageWriter getWriter(Object destination, Format format)
			throws IOException {
		throw new RuntimeException("Writing is not supported for WMSs");
	}
	
	public boolean isAvailable() {
		return true;
	}
	
	public boolean setDataSource(Object datasource) {
		if (datasource instanceof String || datasource instanceof URL) {
			try {
				new URL((String) datasource);
				return true;
			} catch (MalformedURLException e) {
				return false;
			}
		}
		return false;
	}
	public WMT_MS_Capabilities getCapabilities() {
		return capabilities;
	}
}
