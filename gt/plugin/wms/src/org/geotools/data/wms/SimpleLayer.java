/*
 * Created on Aug 9, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
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
	
	public Set getValidStyles() {
		return validStyles;
	}
}
