/*
 * Geotools2 - OpenSource mapping toolkit http://geotools.org (C) 2002, Geotools Project Managment
 * Committee (PMC) This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software Foundation;
 * version 2.1 of the License. This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.geotools.renderer.lite;

import java.util.ArrayList;
import java.util.List;

import org.geotools.renderer.style.TextStyle2D;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * The Labelling information that is put in the label cache.
 * 
 * @author jeichar
 */
public class LabelCacheItem {
	TextStyle2D textStyle;
	List geoms=new ArrayList();
	
	/**
	 * Construct <code>LabelCacheItem</code>.
	 */
	public LabelCacheItem(TextStyle2D textStyle, LiteShape2 shape) {
		this.textStyle=textStyle;
		this.geoms.add(shape.getGeometry());
	}
	
	/**
	 * The list of geometries this item maintains
	 */
	public List getGeoms() {
		return geoms;
	}

	/**
	 * The textstyle that is used to label the shape.
	 */
	public TextStyle2D getTextStyle() {
		return textStyle;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object arg0) {
		if (arg0 instanceof String) {
			String label = (String) arg0;
			return label.equals(textStyle.getLabel());
		}
		if (arg0 instanceof LabelCacheItem) {
			LabelCacheItem item = (LabelCacheItem) arg0;
			return textStyle.getLabel().equals(((LabelCacheItem)arg0).getTextStyle().getLabel());
		}
		if (arg0 instanceof TextStyle2D) {
			TextStyle2D text = (TextStyle2D) arg0;
			return textStyle.getLabel().equals(text.getLabel());
		}
		return false;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return textStyle.getLabel().hashCode();
	}

	/**
	 * Returns an example geometry from the list of geometries.
	 */
	public Geometry getGeometry() {
		return (Geometry) geoms.get(0);
	}
}
