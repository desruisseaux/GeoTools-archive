/*
 * Created on Jun 30, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.getCapabilities;

import java.net.URL;

/**
 * @author rgould
 *
 * Attribution indicates the provider of a Layer or collection of Layers.
 * The provider's URL, descriptive title string, and/or logo image URL may
 * be supplied.  Client applications may choose to display one or more of these
 * items.  A format element indicates the MIME type of the logo image located
 * at LogoURL.  The logo image's width and height assist client application
 * in laying out space to display the logo.
 */
public class Attribution {
    private String title;
    private URL onlineResource;
    private LogoURL logoURL;
    
    public LogoURL getLogoURL() {
        return logoURL;
    }
    public void setLogoURL(LogoURL logoURL) {
        this.logoURL = logoURL;
    }
    public URL getOnlineResource() {
        return onlineResource;
    }
    public void setOnlineResource(URL onlineResource) {
        this.onlineResource = onlineResource;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
}
