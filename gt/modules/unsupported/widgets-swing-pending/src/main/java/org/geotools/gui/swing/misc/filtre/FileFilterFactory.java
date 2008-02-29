/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2007, GeoTools Project Managment Committee (PMC)
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

package org.geotools.gui.swing.misc.filtre;

import java.io.FileFilter;

/**
 *
 * @author johann sorel
 */
public class FileFilterFactory {

    public static enum FILE_FORMAT{
        SHAPEFILE("",""),
        GEOTIFF("",""),
        GML("",""),
        WORLD_IMAGE("","");
            
        String desc;
        String[] ends;
        
        FILE_FORMAT(String i18n, String ... ends){
            desc = i18n;
        }
               
        
    };
    
    
    public static FileFilter createFileFilter(FILE_FORMAT format){
        return null;
    }
    
    
}
