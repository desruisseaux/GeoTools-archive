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

    public static enum MAP_STATE{
        ZOOM_IN,
        ZOOM_OUT,
        PAN,
        SELECT,
        EDIT
    };
        
    public static enum MAP_TYPE{
        MAP_2D,
        MAP_3D,
        MAP_PRINT
    };
    
    
    public static enum MAP_GEOMETRIE{
        POLYGON,
        LINE,
        POINT
    };
     
}
