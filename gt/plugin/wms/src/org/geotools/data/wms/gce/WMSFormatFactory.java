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

import org.geotools.data.coverage.grid.Format;
import org.geotools.data.coverage.grid.GridFormatFactorySpi;
import org.geotools.data.ows.WMSCapabilities;

/**
 * Factory for the creation of a Format for use with WebMapServer.
 * <p>
 * The level of separation afforded a Factory implementation is not currently used,
 * however we may need make use of this class to provide a specific WMSFormat for each
 * version of the WMS Specification.
 * </p>
 * @author Richard Gould, Refractions Research
 */
public class WMSFormatFactory implements GridFormatFactorySpi {
    private WMSCapabilities capabilities;

	/**
     * WMSFormatFactory constructions based on parsed CapabilitiesDocument.
     * <p>
     * Currently only WMSFormat is supported - my impression is that a given
     * WMS can understand several formats.
     * </p>
     * @param capabilities Capabilities Document used to determine supported formats 
     */
    public WMSFormatFactory(WMSCapabilities capabilities) {
        
    	this.capabilities = capabilities;
    }

    /** Constructs a WMSFormat for use */
	public Format createFormat() {
		return new WMSFormat(capabilities);
	}

	/** Ensures Format preconditions are met */
	public boolean isAvailable() {
		return true;
	}

}
