/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.gui.swing.event;

import java.util.EventObject;

import org.geotools.filter.Filter;


public class HighlightChangedEvent extends EventObject{
    Object source;
    
    Filter filter;
    public HighlightChangedEvent(Object source,Filter filter){
        super(source);
        this.source = source;
        
        this.filter = filter;
    }
    public Filter getFilter() {
        return filter;
    }
    public void setFilter(Filter filter) {
        this.filter = filter;
    }
    
    public Object getSource() {
        return source;
    }
    public void setSource(Object source) {
        this.source = source;
    }
}
