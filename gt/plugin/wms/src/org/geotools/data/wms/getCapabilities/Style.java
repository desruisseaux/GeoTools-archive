/*
 * Created on Jun 30, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.getCapabilities;

import java.util.List;

/**
 * @author rgould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Style {
    private String name;
    private String title;
    private String _abstract;
    
    /**
     * A Map Server may use zero or more LegendURL elements to provide an
     * image(s) of a legend relevant to each Style of a Layer.  The Format element
     * indicates the MIME type of the legend. Width and height attributes are 
     * provided to assist client applications in laying out space to display
     * the legend.
     * 
     * A list of type LegendURL
     */
    private List legendURLs;
    
    /**
     * StyleSheetURL provides symbology information foreach Style of a Layer.
     */
    private StyleSheetURL styleSheetURL;
    
    /**
     * A Map Server may use StyleURL to offer more information about the data or
     * symbology underlying a particular Style. While the semantics are not 
     * well-defined, as long as the results of an HTTP GET request against the 
     * StyleURL are properly MIME-typed, Viewer Clients and Cascading Map Servers can
     * make use of this. A possible use could be to allow a Map Server to provide
     * legend information.
     */
    private StyleURL styleURL;
    
    
    /**
     * @param name
     * @param title
     */
    public Style(String name, String title) {
        super();
        this.name = name;
        this.title = title;
    }
    public String get_abstract() {
        return _abstract;
    }
    public void set_abstract(String _abstract) {
        this._abstract = _abstract;
    }
    public List getLegendURLs() {
        return legendURLs;
    }
    public void setLegendURLs(List legendURLs) {
        this.legendURLs = legendURLs;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public StyleSheetURL getStyleSheetURL() {
        return styleSheetURL;
    }
    public void setStyleSheetURL(StyleSheetURL styleSheetURL) {
        this.styleSheetURL = styleSheetURL;
    }
    public StyleURL getStyleURL() {
        return styleURL;
    }
    public void setStyleURL(StyleURL styleURL) {
        this.styleURL = styleURL;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
}
