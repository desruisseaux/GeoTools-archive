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
package org.geotools.data.wms;

import java.util.Set;

/**
 * @author Richard Gould
 *
 * A simple bean that represents a layer name paired with a style name.
 */
public class SimpleLayer {
	private String name;
	private String style;
	/**
	 * @param name
	 * @param style
	 */
	public SimpleLayer(String name, String style) {
		super();
		this.name = name;
		this.style = style;
	}
	/**
	 * @param name
	 * @param style
	 * @param validStyles
	 */
	public SimpleLayer(String name, Set validStyles) {
		super();
		this.name = name;
		this.validStyles = validStyles;
	}
	private Set validStyles;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getStyle() {
		return style;
	}
	public void setStyle(String style) {
		this.style = style;
	}
	
	/**
	 * Returns a Set of type <code>String</code> containing the names of
	 * all the styles that are valid for this layer.
	 * @return
	 */
	public Set getValidStyles() {
		return validStyles;
	}
}
