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
