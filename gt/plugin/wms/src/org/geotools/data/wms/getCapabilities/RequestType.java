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

import java.util.ArrayList;
import java.util.List;

/**
 * @author rgould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RequestType {
    public List getDcpTypes() {
        return dcpTypes;
    }
    public void setDcpTypes(List dcpTypes) {
        this.dcpTypes = dcpTypes;
    }
    public List getFormats() {
        return formats;
    }
    public void setFormats(List formats) {
        this.formats = formats;
    }
    /** Contains Strings of available formats */
    private List formats;
    
    /** Available Distributed Computing Platforms, contains DCPType objects */
    private List dcpTypes;
    
    /**
     * @param formats
     * @param dcpTypes
     */
    public RequestType(List formats, List dcpTypes) {
        this.formats = formats;
        this.dcpTypes = dcpTypes;
    }
    
    public RequestType(String initialFormat, DCPType initialDCPType) {
        formats = new ArrayList();
        formats.add(initialFormat);
        
        dcpTypes = new ArrayList();
        dcpTypes.add(initialDCPType);        
    }
}
