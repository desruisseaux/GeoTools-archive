/*
 * Created on Jul 15, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.gce;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.Iterator;
import java.util.TreeSet;

import org.geotools.data.coverage.grid.Format;
import org.geotools.data.coverage.grid.GridCoverageExchange;
import org.geotools.data.coverage.grid.GridCoverageReader;
import org.geotools.data.coverage.grid.GridCoverageWriter;
import org.geotools.data.coverage.grid.GridFormatFinder;
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
    		try {
    			capabilities = WebMapServer.getCapabilities(url);
    		} catch (JDOMException e) {
    			throw new RuntimeException ("Data at the URL is not valid XML", e);
    		}
    	} else if (source instanceof WMT_MS_Capabilities) {
    		capabilities = (WMT_MS_Capabilities) source;
    	}
    	formats = new Format[1];
    	formats[0] = new WMSFormat();
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
					return format.getReader(source);
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
