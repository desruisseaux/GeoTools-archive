/*
 * Created on Jul 20, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.gce;

import java.net.MalformedURLException;
import java.net.URL;


import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.data.coverage.grid.GridCoverageReader;
import org.geotools.data.coverage.grid.GridCoverageWriter;
import org.geotools.data.wms.getCapabilities.WMT_MS_Capabilities;
import org.opengis.parameter.GeneralOperationParameter;

/**
 * @author rgould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WMSFormat extends AbstractGridFormat {
	
	private WMT_MS_Capabilities capabilities;

	public WMSFormat() {
		
	}
	
	/**
	 * @param capabilities
	 */
	public WMSFormat(WMT_MS_Capabilities capabilities) {
		this.capabilities = capabilities;
	}
	
	/* (non-Javadoc)
	 * @see org.geotools.data.coverage.grid.Format#getReader(java.lang.Object)
	 */
	public GridCoverageReader getReader(Object source) {
		WMSReader reader = new WMSReader (source);
		reader.setFormat(this);
		return reader;
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.coverage.grid.Format#getWriter(java.lang.Object)
	 */
	/**
	 * Web Map Servers are not capable of writing, as of version 1.1.1
	 * Returns null.
	 * @return null
	 */
	public GridCoverageWriter getWriter(Object destination) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.coverage.grid.Format#accepts(java.lang.Object)
	 */
	/**
	 * Determines if the input can be processed or not. Currently it accepts
	 * WebMapServers, WMT_MS_Capabilities, and URLs and Strings that point to the
	 * WMS's getCapabilities address.
	 */
	public boolean accepts(Object input) {
		if (input instanceof String) {
			try {
				URL url = new URL((String) input);
				return true;
			} catch (MalformedURLException e) {
				return false;
			}
		}
		
		if (input instanceof URL) {
			return true;
		}
		
		if (input instanceof WMT_MS_Capabilities) { 
			return true;
		}
		return false;
	}

	public GeneralOperationParameter[] getReadParameters() {
		readParameters = new GeneralOperationParameter[14];
		
		WMSParameterMaker maker = new WMSParameterMaker(capabilities);
		readParameters[0] = maker.createFormatReadParam();
		
		return readParameters;
	}
}
