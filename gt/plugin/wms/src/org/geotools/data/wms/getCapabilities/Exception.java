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
 * An exception element indicates which error-reporting formats are supported
 */
public class Exception {
    /** A list of Strings specifying available Exception formats */
    private List formats;
    
    /**
     * @param formats
     */
    public Exception(List formats) {
        this.formats = formats;
    }
    
    public Exception(String initialFormat) {
        this.formats = new ArrayList();
        this.formats.add(initialFormat);
    }
    
    public List getFormats() {
        return formats;
    }
    public void setFormats(List formats) {
        this.formats = formats;
    }
}
