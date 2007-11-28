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

package org.geotools.gui.swing.map;

/**
 *
 * @author johann sorel
 */


public class MapConstants {

    public static enum ACTION_STATE{
        ZOOM_IN,
        ZOOM_OUT,
        PAN,
        SELECT,
        EDIT,
        NONE
    };
    
    public static enum EDIT_STATE{
        EDIT,
        POINT,
        MULTI_POINT,
        LINE,
        MULTI_LINE,
        POLYGON,
        MULTI_POLYGON,
        NONE
    };
    
    public static enum MAP_GEOMETRIE{
        POLYGON,
        LINE,
        POINT
    };
     
}
