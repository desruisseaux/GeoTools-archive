/*
 * Created on Jun 29, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.getCapabilities;

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
