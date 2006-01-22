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
package org.geotools.data.ows;

import org.geotools.data.wms.WMSOperationType;


/**
 * DOCUMENT ME!
 *
 * @author rgould Available WMS Operations are listed in a Request element.
 * @source $URL$
 */
public class WMSRequest {
    private WMSOperationType getCapabilities;
    private WMSOperationType getMap;
    private WMSOperationType getFeatureInfo;
    private WMSOperationType describeLayer;
    private WMSOperationType getLegendGraphic;
    private WMSOperationType getStyles;
    private WMSOperationType putStyles;

    /**
     * DOCUMENT ME!
     *
     * @return Returns the getCapabilities.
     */
    public WMSOperationType getGetCapabilities() {
        return getCapabilities;
    }

    /**
     * DOCUMENT ME!
     *
     * @param getCapabilities The getCapabilities to set.
     */
    public void setGetCapabilities(WMSOperationType getCapabilities) {
        this.getCapabilities = getCapabilities;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the getFeatureInfo.
     */
    public WMSOperationType getGetFeatureInfo() {
        return getFeatureInfo;
    }

    /**
     * DOCUMENT ME!
     *
     * @param getFeatureInfo The getFeatureInfo to set.
     */
    public void setGetFeatureInfo(WMSOperationType getFeatureInfo) {
        this.getFeatureInfo = getFeatureInfo;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the getMap.
     */
    public WMSOperationType getGetMap() {
        return getMap;
    }

    /**
     * DOCUMENT ME!
     *
     * @param getMap The getMap to set.
     */
    public void setGetMap(WMSOperationType getMap) {
        this.getMap = getMap;
    }
    public WMSOperationType getDescribeLayer() {
        return describeLayer;
    }
    public void setDescribeLayer( WMSOperationType describeLayer ) {
        this.describeLayer = describeLayer;
    }
    public WMSOperationType getGetLegendGraphic() {
        return getLegendGraphic;
    }
    public void setGetLegendGraphic( WMSOperationType getLegendGraphic ) {
        this.getLegendGraphic = getLegendGraphic;
    }
    public WMSOperationType getGetStyles() {
        return getStyles;
    }
    public void setGetStyles( WMSOperationType getStyles ) {
        this.getStyles = getStyles;
    }
    public WMSOperationType getPutStyles() {
        return putStyles;
    }
    public void setPutStyles( WMSOperationType putStyles ) {
        this.putStyles = putStyles;
    }
}
