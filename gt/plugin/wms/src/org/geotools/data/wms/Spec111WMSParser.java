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
package org.geotools.data.wms;


/**
 * Generate a Capabilities bean from a WMS 1.1.1 complient GetCapabilities document.
 * 
 * @author Richard Gould, Refractions Research
 * @see OPENGIS PROJECT DOCUMENT 00-028 OpenGIS® Web Map Server Interface Implementation Specification
 */
public class Spec111WMSParser extends AbstractWMSParser implements WMSParser {
    
	/* (non-Javadoc)
	 * @see org.geotools.data.wms.AbstractWMSParser#getVersion()
	 */
	public String getVersion() {
		return "1.1.1";
	}
	
}
