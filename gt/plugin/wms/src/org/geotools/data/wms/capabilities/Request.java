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
package org.geotools.data.wms.capabilities;

/**
 * @author rgould
 *
 * Available WMS Operations are listed in a Request element.
 */
public class Request {
    
    /** 
     * For each operation offered by the server, list the available output 
     * formats and the online resource.
     */
    
    private GetCapabilities getCapabilities;
    private GetMap getMap;
    private GetFeatureInfo getFeatureInfo;

    /** The following optional operations only apply to SLD-enabled WMS */
    private DescribeLayer describeLayer;
    private GetLegendGraphic getLegendGraphic;
    private GetStyles getStyles;
    private PutStyles putStyles;
    
    /**
     * @param getCapabilities
     * @param getMap
     */
    public Request(GetCapabilities getCapabilities, GetMap getMap) {
        super();
        this.getCapabilities = getCapabilities;
        this.getMap = getMap;
    }
    
    public DescribeLayer getDescribeLayer() {
        return describeLayer;
    }
    public void setDescribeLayer(DescribeLayer describeLayer) {
        this.describeLayer = describeLayer;
    }
    public GetCapabilities getGetCapabilities() {
        return getCapabilities;
    }
    public void setGetCapabilities(GetCapabilities getCapabilities) {
        this.getCapabilities = getCapabilities;
    }
    public GetFeatureInfo getGetFeatureInfo() {
        return getFeatureInfo;
    }
    public void setGetFeatureInfo(GetFeatureInfo getFeatureInfo) {
        this.getFeatureInfo = getFeatureInfo;
    }
    public GetLegendGraphic getGetLegendGraphic() {
        return getLegendGraphic;
    }
    public void setGetLegendGraphic(GetLegendGraphic getLegendGraphic) {
        this.getLegendGraphic = getLegendGraphic;
    }
    public GetMap getGetMap() {
        return getMap;
    }
    public void setGetMap(GetMap getMap) {
        this.getMap = getMap;
    }
    public GetStyles getGetStyles() {
        return getStyles;
    }
    public void setGetStyles(GetStyles getStyles) {
        this.getStyles = getStyles;
    }
    public PutStyles getPutStyles() {
        return putStyles;
    }
    public void setPutStyles(PutStyles putStyles) {
        this.putStyles = putStyles;
    }
}
