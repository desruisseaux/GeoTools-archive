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
 * Optional user-defined symbolization (used only by SLD-enabled WMSes)
 */
public class UserDefinedSymbolization {
    private boolean supportSLD = false;
    private boolean userLayer = false;
    private boolean userStyle = false;
    private boolean remoteWFS = false;
    
    public boolean isRemoteWFS() {
        return remoteWFS;
    }
    public void setRemoteWFS(boolean removeWFS) {
        this.remoteWFS = removeWFS;
    }
    public boolean isSupportSLD() {
        return supportSLD;
    }
    public void setSupportSLD(boolean supportSLD) {
        this.supportSLD = supportSLD;
    }
    public boolean isUserLayer() {
        return userLayer;
    }
    public void setUserLayer(boolean userLayer) {
        this.userLayer = userLayer;
    }
    public boolean isUserStyle() {
        return userStyle;
    }
    public void setUserStyle(boolean userStyle) {
        this.userStyle = userStyle;
    }
}
