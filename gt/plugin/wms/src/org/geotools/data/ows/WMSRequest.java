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
package org.geotools.data.ows;

import org.geotools.data.wms.WMSOperationType;

/**
 * @author rgould
 *
 * Available WMS Operations are listed in a Request element.
 */
public class WMSRequest {
    
    private WMSOperationType getCapabilities;
    private WMSOperationType getMap;
    private WMSOperationType getFeatureInfo;

    
	/**
	 * @return Returns the getCapabilities.
	 */
	public WMSOperationType getGetCapabilities() {
		return getCapabilities;
	}
	/**
	 * @param getCapabilities The getCapabilities to set.
	 */
	public void setGetCapabilities(WMSOperationType getCapabilities) {
		this.getCapabilities = getCapabilities;
	}
	/**
	 * @return Returns the getFeatureInfo.
	 */
	public WMSOperationType getGetFeatureInfo() {
		return getFeatureInfo;
	}
	/**
	 * @param getFeatureInfo The getFeatureInfo to set.
	 */
	public void setGetFeatureInfo(WMSOperationType getFeatureInfo) {
		this.getFeatureInfo = getFeatureInfo;
	}
	/**
	 * @return Returns the getMap.
	 */
	public WMSOperationType getGetMap() {
		return getMap;
	}
	/**
	 * @param getMap The getMap to set.
	 */
	public void setGetMap(WMSOperationType getMap) {
		this.getMap = getMap;
	}
}