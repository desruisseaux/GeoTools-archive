/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
package org.geotools.data.wms;

import org.geotools.data.ows.OperationType;


/**
 * A subclass of OperationType that allows format getters and setters without
 * having to cast them.
 *
 * @author Richard Gould
 */
public class WMSOperationType extends OperationType {
    public WMSOperationType() {
        super();
    }

    public String[] getFormatStrings() {
        return (String[]) formats;
    }

    public void setFormatStrings(String[] formats) {
        this.formats = formats;
    }
}
