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
