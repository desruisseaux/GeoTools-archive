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
 * Nested list of zero or more map Layers offered by this server.
 */
public class Layer implements Comparable {
    /** A machine-readable (typically one word) identifier */
    private String name;
    
    /** The title is for informative display to a human. */
    private String title;
    
    /** A list of Strings representing SRSs */
    private List srs;
 
    /**
     * A list of type BoundingBox
     */
    private List boundingBoxes;
    
    
    /**
     * A list of type Style
     */
    private List styles;
    
    private boolean queryable = false;
    
    /**
     * @param title
     */
    public Layer(String title) {
        this.title = title;
    }
    
    public List getBoundingBoxes() {
        return boundingBoxes;
    }
    public void setBoundingBoxes(List boundingBoxes) {
        this.boundingBoxes = boundingBoxes;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public List getSrs() {
        return srs;
    }
    public void setSrs(List srs) {
        this.srs = srs;
    }
    public List getStyles() {
        return styles;
    }
    public void setStyles(List styles) {
        this.styles = styles;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isQueryable() {
        return queryable;
    }
    public void setQueryable(boolean queryable) {
        this.queryable = queryable;
    }
    
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object arg0) {
		Layer layer = (Layer) arg0;
		if (this.getName() != null && layer.getName() != null) {
			return this.getName().compareTo(layer.getName());
		}
		return this.getTitle().compareTo(layer.getTitle());
	}
}
