/*
 * Created on Jun 29, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
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
