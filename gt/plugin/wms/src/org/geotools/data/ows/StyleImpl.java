/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
 *    
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.ows;

import java.util.List;

import org.opengis.layer.Style;
import org.opengis.layer.StyleSheetURL;
import org.opengis.layer.StyleURL;
import org.opengis.util.InternationalString;

/**
 * 
 * @author Richard Gould
 *
 */
public class StyleImpl implements Style {

	private String name;
	private InternationalString title;
	private InternationalString _abstract;
	private List legendURLs;
	private StyleSheetURL styleSheetURL;
	private StyleURL styleURL;
	private List featureStyles;
	private List graphicStyles;
	
	public StyleImpl() {
		
	}
	
	public StyleImpl(String name) {
		this.name = name;
	}
	
	public InternationalString getAbstract() {
		return _abstract;
	}
	public void setAbstract(InternationalString _abstract) {
		this._abstract = _abstract;
	}
	public List getFeatureStyles() {
		return featureStyles;
	}
	public void setFeatureStyles(List featureStyles) {
		this.featureStyles = featureStyles;
	}
	public List getGraphicStyles() {
		return graphicStyles;
	}
	public void setGraphicStyles(List graphicStyles) {
		this.graphicStyles = graphicStyles;
	}
	public List getLegendURLs() {
		return legendURLs;
	}
	public void setLegendURLs(List legendURLs) {
		this.legendURLs = legendURLs;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public StyleSheetURL getStyleSheetURL() {
		return styleSheetURL;
	}
	public void setStyleSheetURL(StyleSheetURL styleSheetURL) {
		this.styleSheetURL = styleSheetURL;
	}
	public StyleURL getStyleURL() {
		return styleURL;
	}
	public void setStyleURL(StyleURL styleURL) {
		this.styleURL = styleURL;
	}
	public InternationalString getTitle() {
		return title;
	}
	public void setTitle(InternationalString title) {
		this.title = title;
	}

    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

  /**
   * Because the style's name is declared as unique identifier in the
   * interface javadocs, we will use that as our equals comparison.
   * 
   * So if two Styles have the same name, they are considered equal.
   * 
   */
    public boolean equals( Object obj ) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final StyleImpl other = (StyleImpl) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }
    
}
