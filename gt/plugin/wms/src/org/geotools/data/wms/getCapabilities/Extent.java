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
 * The Extent element indicates what values along a dimension are valid.
 */
public class Extent {
    private String value;
    
    private String name;
    private String _default;
    
    /**
     * @param value
     * @param name
     */
    public Extent(String value, String name) {
        super();
        this.value = value;
        this.name = name;
    }
    public String get_default() {
        return _default;
    }
    public void set_default(String _default) {
        this._default = _default;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
}
