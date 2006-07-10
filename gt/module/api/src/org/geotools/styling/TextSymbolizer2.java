/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.styling;

/**
 * Contains label shield hack
 * @source $URL$
 */
public interface TextSymbolizer2 extends TextSymbolizer {
	/**
	 * The nonstandard-SLD graphic element supports putting little graphical-bits onto labels.
     * Useful for things like interstate road shields or labeled logos
     * @return - the Graphic object to be rendered under the label text
     */
    public Graphic getGraphic();
    /**
     * The nonstandard-SLD graphic element supports putting little graphical-bits onto labels.
     * Useful for things like interstate road shields or labeled logos
    * @param g - the Graphic object which will be rendered under the label text
    */
    public void setGraphic(Graphic graphic);
}
