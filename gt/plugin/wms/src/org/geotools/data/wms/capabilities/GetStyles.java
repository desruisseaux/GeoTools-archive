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

import java.util.List;

/**
 * @author rgould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class GetStyles extends RequestType {

    /**
     * @param formats
     * @param dcpTypes
     */
    public GetStyles(List formats, List dcpTypes) {
        super(formats, dcpTypes);
        // TODO Auto-generated constructor stub
    }
    /**
     * @param initialFormat
     * @param initialDCPType
     */
    public GetStyles(String initialFormat, DCPType initialDCPType) {
        super(initialFormat, initialDCPType);
        // TODO Auto-generated constructor stub
    }
}
